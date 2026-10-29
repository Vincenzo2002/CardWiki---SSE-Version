package at.ac.tuwien.sepm.groupphase.benchmarks;

import at.ac.tuwien.sepm.groupphase.backend.service.impl.SimpleImageService;
import org.openjdk.jmh.annotations.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class SimpleImageServiceBenchmark {

    // Resolve private access once, outside the measured operation. Keep this
    // invocation unchanged when comparing application implementations.
    private static final MethodHandle BYTES_TO_HEX = findBytesToHex();

    private SimpleImageService service;
    private byte[] hash;

    @Setup
    public void setup() {
        // bytesToHex does not use the filesystem or either service dependency.
        service = new SimpleImageService(".", null, null);
        hash = new byte[32];
        new Random(42).nextBytes(hash);
    }

    private static MethodHandle findBytesToHex() {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(
                SimpleImageService.class, MethodHandles.lookup());
            return lookup.findVirtual(SimpleImageService.class, "bytesToHex",
                MethodType.methodType(String.class, byte[].class));
        } catch (ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    @Benchmark
    public String bytesToHex() throws Throwable {
        return (String) BYTES_TO_HEX.invokeExact(service, hash);
    }
}
