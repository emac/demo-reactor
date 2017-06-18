package cn.emac.demo.reactor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Emac
 * @since 2017-06-19
 */
public class BackpressureTests {

    private Flux<Integer> timerPublisher;

    @BeforeEach
    public void beforeEach() {
        // initialize publisher
        AtomicInteger count = new AtomicInteger();
        timerPublisher = Flux.create(s ->
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        s.next(count.getAndIncrement());
                        if (count.get() == 10) {
                            s.complete();
                        }
                    }
                }, 100, 100)
        );
    }

    @Test
    public void testNormal() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        timerPublisher
                .subscribe(r -> System.out.println("Continuous consuming " + r),
                        e -> latch.countDown(),
                        latch::countDown);
        latch.await();
    }

    @Test
    public void testBackpressure() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Subscription> timerSubscription = new AtomicReference<>();
        Subscriber<Integer> subscriber = new BaseSubscriber<Integer>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                timerSubscription.set(subscription);
            }

            @Override
            protected void hookOnNext(Integer value) {
                System.out.println("consuming " + value);
            }

            @Override
            protected void hookOnComplete() {
                latch.countDown();
            }

            @Override
            protected void hookOnError(Throwable throwable) {
                latch.countDown();
            }
        };
        timerPublisher.onBackpressureDrop().subscribe(subscriber);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                timerSubscription.get().request(1);
            }
        }, 100, 200);
        latch.await();
    }
}
