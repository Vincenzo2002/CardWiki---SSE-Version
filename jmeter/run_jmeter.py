#!/usr/bin/env python3
"""Run the CardWiki JMeter profiles and preserve reproducible measurement artifacts."""

import argparse
import csv
import hashlib
import json
import os
import platform
import re
import shutil
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path
from urllib.parse import urlencode
from urllib.request import Request, urlopen

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent


def capture(*command):
    result = subprocess.run(command, capture_output=True, text=True, check=False)
    return (result.stdout + result.stderr).strip()


def positive(value):
    number = int(value)
    if number < 1:
        raise argparse.ArgumentTypeError("must be a positive integer")
    return number


def nonnegative(value):
    number = int(value)
    if number < 0:
        raise argparse.ArgumentTypeError("must be a nonnegative integer")
    return number


def parse_args():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("scenario", choices=("load", "stress", "spike", "soak"))
    parser.add_argument("version", choices=("baseline", "sse-improvements"))
    parser.add_argument("run_id", help="unique directory name, e.g. users20-run01")
    parser.add_argument("--app-commit", required=True, help="full SHA of the RUNNING backend")
    parser.add_argument("--dataset", required=True, help="dataset version or reproducible description")
    parser.add_argument("--host", default="localhost")
    parser.add_argument("--port", type=positive, default=8080)
    parser.add_argument("--protocol", choices=("http", "https"), default="http")
    parser.add_argument("--search", default="Haupt")
    parser.add_argument("--deck-id", type=positive, default=1)
    parser.add_argument("--users", type=positive, help="users in the main phase")
    parser.add_argument("--ramp-up", type=nonnegative, help="main phase ramp-up in seconds")
    parser.add_argument("--duration", type=positive, help="main phase total seconds, INCLUDING ramp-up")
    parser.add_argument("--base-users", type=positive, default=5)
    parser.add_argument("--base-ramp-up", type=nonnegative, default=5)
    parser.add_argument("--base-duration", type=positive, default=60)
    parser.add_argument("--think-ms", type=nonnegative, default=500,
                        help="pause before EACH request, not per loop")
    parser.add_argument("--warmup", type=nonnegative, default=10,
                        help="seconds after each phase ramp-up excluded from steady metrics")
    parser.add_argument("--purpose", choices=("measurement", "validation"), default="measurement")
    parser.add_argument("--output-root", type=Path, default=ROOT / "sse-reports")
    parser.add_argument("--jmeter", default="jmeter", help="JMeter executable")
    args = parser.parse_args()
    if not re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9_.-]*", args.run_id):
        parser.error("run_id must contain only letters, digits, dots, underscores or hyphens")
    if not re.fullmatch(r"[0-9a-fA-F]{40}", args.app_commit):
        parser.error("--app-commit must be a full 40-character commit SHA")
    if not args.dataset.strip() or not args.search.strip():
        parser.error("--dataset and --search must not be empty")
    if not re.fullmatch(r"[A-Za-z0-9.-]+", args.host) or args.port > 65535:
        parser.error("use a hostname/IPv4 address without a URL, and a port from 1 to 65535")
    defaults = {"load": (20, 10, 300), "stress": (50, 10, 180),
                "spike": (50, 1, 30), "soak": (20, 30, 3600)}
    users, ramp, duration = defaults[args.scenario]
    args.users = args.users if args.users is not None else users
    args.ramp_up = args.ramp_up if args.ramp_up is not None else ramp
    args.duration = args.duration if args.duration is not None else duration
    if args.duration <= args.ramp_up + args.warmup:
        parser.error("--duration must exceed --ramp-up + --warmup")
    if args.scenario in ("stress", "spike"):
        if args.users <= args.base_users:
            parser.error("stress/spike users must exceed --base-users")
        if args.base_duration <= args.base_ramp_up + args.warmup:
            parser.error("--base-duration must exceed --base-ramp-up + --warmup")
    if args.scenario == "spike" and args.ramp_up > 2:
        parser.error("spike ramp-up must be at most 2 seconds")
    return args


def phases_for(args):
    main = {"users": args.users, "ramp_up": args.ramp_up, "duration": args.duration}
    if args.scenario not in ("stress", "spike"):
        return {args.scenario: main}
    base = {"users": args.base_users, "ramp_up": args.base_ramp_up,
            "duration": args.base_duration}
    return {"before": dict(base), args.scenario: main, "recovery": dict(base)}


def preflight(args):
    origin = f"{args.protocol}://{args.host}:{args.port}"
    endpoints = ("/api/v1/decks?" + urlencode({"name": args.search, "limit": 10, "offset": 0}),
                 f"/api/v1/decks/{args.deck_id}")
    for index, endpoint in enumerate(endpoints):
        request = Request(origin + endpoint, headers={"Accept": "application/json"})
        with urlopen(request, timeout=10) as response:
            if response.status != 200 or "application/json" not in response.headers.get("Content-Type", ""):
                raise ValueError(f"preflight requires HTTP 200 and JSON: {endpoint}")
            data = json.load(response)
        if index == 0:
            valid = (isinstance(data, dict) and isinstance(data.get("content"), list)
                     and bool(data["content"]) and all(isinstance(deck, dict)
                     and isinstance(deck.get("id"), int) and isinstance(deck.get("name"), str)
                     for deck in data["content"]))
        else:
            valid = isinstance(data, dict) and data.get("id") == args.deck_id and isinstance(data.get("name"), str)
        if not valid:
            raise ValueError(f"preflight response does not match the required fixture: {endpoint}")


def metrics(rows):
    if not rows:
        return {"samples": 0}
    times = sorted(float(row["elapsed"]) for row in rows)
    seconds = (max(int(row["timeStamp"]) + int(row["elapsed"]) for row in rows)
               - min(int(row["timeStamp"]) for row in rows)) / 1000
    errors = sum(row["success"].lower() != "true" for row in rows)
    return {"samples": len(rows), "errors": errors, "error_percent": 100 * errors / len(rows),
            "mean_ms": sum(times) / len(times), "p95_ms": times[(95 * len(times) + 99) // 100 - 1],
            "observed_seconds": seconds, "throughput_rps": len(rows) / seconds if seconds else None}


def summarize(path, phases, warmup):
    with path.open(newline="") as stream:
        rows = list(csv.DictReader(stream))
    summary = {"overall_including_ramps_and_warmup": metrics(rows), "phases": {}}
    for name, config in phases.items():
        samples = [row for row in rows if row["threadName"].split(" ")[0] == name]
        if not samples:
            summary["phases"][name] = {"configured": config, "samples": 0,
                                      "concurrency_confirmed": False}
            continue
        start = min(int(row["timeStamp"]) for row in samples)
        steady = [row for row in samples
                  if int(row["timeStamp"]) >= start + (config["ramp_up"] + warmup) * 1000
                  and int(row["grpThreads"]) == config["users"]]
        peak = max(int(row["grpThreads"]) for row in samples)
        summary["phases"][name] = {
            "configured": config, "peak_recorded_threads": peak,
            "distinct_thread_names": len({row["threadName"] for row in samples}),
            "concurrency_confirmed": peak == config["users"] and bool(steady),
            "all_samples": metrics(samples), "steady_at_target_threads": metrics(steady),
            "steady_by_endpoint": {label: metrics([row for row in steady if row["label"] == label])
                                   for label in sorted({row["label"] for row in steady})}}
    return summary


def main():
    args = parse_args()
    if not shutil.which(args.jmeter):
        raise ValueError("JMeter executable not found; install it or use --jmeter /path/to/jmeter")
    destination = args.output_root.resolve() / args.version / "jmeter" / (args.scenario + "_test") / args.run_id
    if destination.exists():
        raise ValueError(f"output already exists; choose a new run_id: {destination}")
    preflight(args)
    phases = phases_for(args)
    destination.mkdir(parents=True, exist_ok=False)
    plans = destination / "plans"
    plans.mkdir()
    for name in (args.scenario + "_test.jmx", "workload.jmx"):
        shutil.copy2(HERE / name, plans / name)
    properties = {"host": args.host, "port": args.port, "protocol": args.protocol,
                  "search": args.search, "deck_id": args.deck_id, "users": args.users,
                  "ramp_up": args.ramp_up, "duration": args.duration, "base_users": args.base_users,
                  "base_ramp_up": args.base_ramp_up, "base_duration": args.base_duration,
                  "think_ms": args.think_ms, "includecontroller.prefix": str(plans) + os.sep,
                  "jmeter.save.saveservice.output_format": "csv",
                  "jmeter.save.saveservice.print_field_names": "true",
                  "jmeter.save.saveservice.thread_counts": "true",
                  "jmeter.save.saveservice.thread_name": "true",
                  "jmeter.save.saveservice.timestamp_format": "ms",
                  "jmeter.save.saveservice.response_data": "false",
                  "jmeter.save.saveservice.assertion_results_failure_message": "true",
                  "jmeter.reportgenerator.overall_granularity": "1000"}
    command = [args.jmeter, "-n", "-t", str(plans / (args.scenario + "_test.jmx")),
               "-l", str(destination / "results.jtl"), "-j", str(destination / "jmeter.log")]
    command.extend(f"-J{key}={value}" for key, value in properties.items())
    metadata = {"purpose": args.purpose, "started_utc": datetime.now(timezone.utc).isoformat(),
                "status": "running", "application_commit_declared": args.app_commit.lower(),
                "application_commit_note": "User-declared; the running backend does not expose its commit.",
                "harness_checkout_commit": capture("git", "-C", str(ROOT), "rev-parse", "HEAD"),
                "harness_worktree_status": capture("git", "-C", str(ROOT), "status", "--short"),
                "plan_sha256": {p.name: hashlib.sha256(p.read_bytes()).hexdigest() for p in plans.iterdir()},
                "runner_sha256": hashlib.sha256(Path(__file__).read_bytes()).hexdigest(),
                "dataset": args.dataset, "scenario": args.scenario, "version": args.version,
                "phases": phases, "steady_exclusion_after_ramp_seconds": args.warmup,
                "os": platform.platform(), "architecture": platform.machine(), "cpu_count": os.cpu_count(),
                "cpu_model": capture("sysctl", "-n", "machdep.cpu.brand_string") if sys.platform == "darwin" else platform.processor(),
                "memory": capture("sysctl", "-n", "hw.memsize") if sys.platform == "darwin" else "record manually",
                "java_version": capture("java", "-version"), "jmeter_version": capture(args.jmeter, "-v"),
                "command_argv": command, "properties": properties,
                "notes": "Preflight requests precede measurement; JSON assertions are included in the workload. "
                         "HTTP timers pause before each sampler. Steady metrics exclude each ramp and warmup. "
                         "No response-time SLA or saturation threshold is assumed."}
    metadata_path = destination / "metadata.json"
    def save_metadata():
        metadata_path.write_text(json.dumps(metadata, indent=2) + "\n")
    save_metadata()
    print(f"Running {args.scenario}: {destination}", flush=True)
    try:
        with (destination / "console.log").open("w") as console:
            run = subprocess.run(command, cwd=plans, stdout=console, stderr=subprocess.STDOUT, check=False)
        metadata["jmeter_exit_code"] = run.returncode
        if run.returncode:
            raise ValueError("JMeter execution failed; inspect console.log and jmeter.log")
        result = summarize(destination / "results.jtl", phases, args.warmup)
        (destination / "summary.json").write_text(json.dumps(result, indent=2) + "\n")
        report_command = [args.jmeter, "-g", str(destination / "results.jtl"),
                          "-o", str(destination / "report"), "-j", str(destination / "report-generation.log"),
                          "-Jjmeter.reportgenerator.overall_granularity=1000"]
        metadata["report_command_argv"] = report_command
        with (destination / "report-console.log").open("w") as console:
            report = subprocess.run(report_command, cwd=plans, stdout=console, stderr=subprocess.STDOUT, check=False)
        metadata["report_exit_code"] = report.returncode
        if report.returncode or not (destination / "report/index.html").is_file():
            raise ValueError("HTML report generation failed; raw results and summary are preserved")
        unconfirmed = [name for name, data in result["phases"].items() if not data["concurrency_confirmed"]]
        metadata["status"] = "concurrency_unconfirmed" if unconfirmed else "completed"
        if unconfirmed:
            print("Concurrency not confirmed in phases: " + ", ".join(unconfirmed), file=sys.stderr)
        print("Artifacts saved. Inspect summary.json and report/index.html; completion is not an SLA pass.")
        return 2 if unconfirmed else 0
    except BaseException as error:
        metadata["status"] = "interrupted" if isinstance(error, KeyboardInterrupt) else "failed"
        metadata["error"] = str(error)
        raise
    finally:
        metadata["ended_utc"] = datetime.now(timezone.utc).isoformat()
        save_metadata()


if __name__ == "__main__":
    try:
        sys.exit(main())
    except KeyboardInterrupt:
        sys.exit(130)
    except (ValueError, OSError, KeyError, csv.Error) as error:
        print(f"Error: {error}", file=sys.stderr)
        sys.exit(1)
