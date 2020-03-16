package makarenko.interview.clientrisk;

import makarenko.interview.clientrisk.repository.ConcurrentMapProfileRepository;
import makarenko.interview.clientrisk.repository.RiskProfileRepository;
import makarenko.interview.clientrisk.repository.SynchronizedMapProfileRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class ClientRiskConcurrencyTest {
    private static final int THREAD_POOL_SIZE = 5;
    private static final int CREATE_OPERATIONS = 200;
    private static final int UPDATE_OPERATIONS = CREATE_OPERATIONS * 2;
    private static final int DELETE_OPERATIONS = CREATE_OPERATIONS / 10;
    private static final int READ_OPERATIONS = CREATE_OPERATIONS * 10000;
    private static final int MERGE_OPERATIONS = CREATE_OPERATIONS / 10;
    private static final float ITERATIONS = 5;
    private static final Random rnd = new Random(1);
    private static final AtomicLong maxId = new AtomicLong(0);

    private static Stream<RiskProfileRepository> generator() {
        return Stream.of(new ConcurrentMapProfileRepository(), new SynchronizedMapProfileRepository());
    }

    private static void basicParallelCRUD(final RiskProfileRepository riskProfileRepository) throws InterruptedException {
        System.out.println("Test started for: " + riskProfileRepository.getClass());
        long averageTime = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            long startTime = System.nanoTime();
            final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            for (int j = 0; j < THREAD_POOL_SIZE; j++) {
                executor.execute(() -> {
                    for (int k = 0; k < CREATE_OPERATIONS; k++) {
                        maxId.set(Math.max(maxId.get(), riskProfileRepository.create()));
                    }
                });
            }
            for (int j = 0; j < THREAD_POOL_SIZE; j++) {
                executor.execute(() -> {
                    for (int k = 0; k < UPDATE_OPERATIONS; k++) {
                        final RiskProfile riskValue = RiskProfile.of(rnd.nextInt(RiskProfile.LENGTH));
                        riskProfileRepository.update(rnd.nextInt((int) maxId.get()), riskValue);
                    }
                });
            }
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

            long endTime = System.nanoTime();
            long totalTime = (endTime - startTime) / 1000000L;
            averageTime += totalTime;
            System.out.println(CREATE_OPERATIONS + " entries added/retrieved in " + totalTime + " ms");
        }
        System.out.println("For " + riskProfileRepository.getClass() + " the average time is " + averageTime / ITERATIONS + " ms\n");
    }

    private void mergeParallel(final RiskProfileRepository riskProfileRepository) throws InterruptedException {
        System.out.println("Test started for: " + riskProfileRepository.getClass());
        long averageTime = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            long startTime = System.nanoTime();
            final ExecutorService[] executors = {Executors.newFixedThreadPool(THREAD_POOL_SIZE),
                    Executors.newFixedThreadPool(THREAD_POOL_SIZE),
                    Executors.newFixedThreadPool(THREAD_POOL_SIZE),
                    Executors.newFixedThreadPool(THREAD_POOL_SIZE),
                    Executors.newFixedThreadPool(THREAD_POOL_SIZE)};
            for (int j = 0; j < THREAD_POOL_SIZE; j++) {
                executors[0].execute(() -> {
                    for (int k = 0; k < CREATE_OPERATIONS; k++) {
                        maxId.set(Math.max(maxId.get(), riskProfileRepository.create()));
                    }
                });
            }
            for (int j = 0; j < THREAD_POOL_SIZE; j++) {
                executors[1].execute(() -> {
                    for (int k = 0; k < UPDATE_OPERATIONS; k++) {
                        final RiskProfile riskValue = RiskProfile.of(rnd.nextInt(RiskProfile.LENGTH));
                        riskProfileRepository.update(rnd.nextInt((int) maxId.get()), riskValue);
                    }
                });
            }
            for (int j = 0; j < THREAD_POOL_SIZE; j++) {
                executors[2].execute(() -> {
                    for (int k = 0; k < DELETE_OPERATIONS; k++) {
                        riskProfileRepository.delete(rnd.nextInt((int) maxId.get()));
                    }
                });
            }
            for (int j = 0; j < THREAD_POOL_SIZE; j++) {
                executors[3].execute(() -> {
                    for (int k = 0; k < READ_OPERATIONS; k++) {
                        riskProfileRepository.findRiskProfile(rnd.nextInt((int) maxId.get()));
                    }
                });
            }
            for (int j = 0; j < THREAD_POOL_SIZE; j++) {
                executors[4].execute(() -> {
                    for (int k = 0; k < MERGE_OPERATIONS; k++) {
                        final int size = (int) maxId.get();
                        riskProfileRepository.merge(rnd.nextInt(size),
                                Set.of((long) rnd.nextInt(size), (long) rnd.nextInt(size), (long) rnd.nextInt(size)));
                    }
                });
            }
            for (ExecutorService executor : executors) {
                executor.shutdown();
            }
            for (ExecutorService executor : executors) {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            }
            long endTime = System.nanoTime();
            long totalTime = (endTime - startTime) / 1000000L;
            averageTime += totalTime;
            System.out.println(CREATE_OPERATIONS + " entries added/retrieved in " + totalTime + " ms");
        }
        System.out.println("For " + riskProfileRepository.getClass() + " the average time is " + averageTime / ITERATIONS + " ms\n");
    }

    @ParameterizedTest
    @MethodSource("generator")
    public void complicatedFunctionalTest(RiskProfileRepository riskProfileRepository) throws InterruptedException {
        basicParallelCRUD(riskProfileRepository);
    }

    @ParameterizedTest
    @MethodSource("generator")
    public void mergeFunctionalTest(RiskProfileRepository riskProfileRepository) throws InterruptedException {
        mergeParallel(riskProfileRepository);
    }
}

