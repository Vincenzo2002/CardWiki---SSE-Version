package at.ac.tuwien.sepm.groupphase.benchmarks;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.AttemptInputDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Card;
import at.ac.tuwien.sepm.groupphase.backend.entity.Progress;
import at.ac.tuwien.sepm.groupphase.backend.entity.User;
import at.ac.tuwien.sepm.groupphase.backend.repository.ProgressRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.UserService;
import at.ac.tuwien.sepm.groupphase.backend.service.impl.SimpleLearnService;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.openjdk.jmh.annotations.*;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class SimpleLearnServiceBenchmark {

    @Param({"NEW", "LEARNING", "REVIEWING"})
    public String progressState;

    @Param({"AGAIN", "GOOD", "EASY"})
    public AttemptInputDto.Status answer;

    private SimpleLearnService service;
    private AttemptInputDto attempt;
    private Progress existingProgress;
    private Progress.Status initialStatus;
    private int initialInterval;
    private Progress lastSaved;

    @Setup
    public void setup() {
        // Measure service logic without console output from each invocation.
        ((Logger) SimpleLearnService.LOGGER).setLevel(Level.WARN);

        User user = new User(1L);
        Card card = new Card();
        card.setId(7L);

        attempt = new AttemptInputDto();
        attempt.setCardId(card.getId());
        attempt.setReverse(false);
        attempt.setStatus(answer);

        if ("NEW".equals(progressState)) {
            existingProgress = null;
        } else {
            initialStatus = Progress.Status.valueOf(progressState);
            initialInterval = initialStatus == Progress.Status.LEARNING ? 10 : 4;
            existingProgress = new Progress(new Progress.Id(user, card, false));
        }
        Optional<Progress> lookupResult = Optional.ofNullable(existingProgress);

        // Lightweight stubs replace external dependencies. They never store
        // invocation history or accumulate progress entries during measurement.
        ProgressRepository repository = (ProgressRepository) Proxy.newProxyInstance(
            ProgressRepository.class.getClassLoader(),
            new Class<?>[]{ProgressRepository.class},
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "findById":
                        return lookupResult;
                    case "saveAndFlush":
                        lastSaved = (Progress) args[0];
                        return lastSaved;
                    default:
                        throw new UnsupportedOperationException(method.getName());
                }
            });

        UserService users = (UserService) Proxy.newProxyInstance(
            UserService.class.getClassLoader(),
            new Class<?>[]{UserService.class},
            (proxy, method, args) -> {
                if ("loadCurrentUserOrThrow".equals(method.getName())) {
                    return user;
                }
                throw new UnsupportedOperationException(method.getName());
            });

        // saveAttempt does not use the deck or card repository.
        service = new SimpleLearnService(repository, null, users, null);
    }

    @Benchmark
    public Progress saveAttempt() {
        // Reset inside the timed operation to keep every attempt independent.
        // This fixed fixture cost is included in both application versions.
        if (existingProgress != null) {
            existingProgress.setStatus(initialStatus);
            existingProgress.setInterval(initialInterval);
            existingProgress.setEasinessFactor(2.5);
            existingProgress.setDue(null);
        }
        lastSaved = null;

        service.saveAttempt(attempt);

        // Expose the saved result to JMH even though the service returns void.
        return lastSaved;
    }
}
