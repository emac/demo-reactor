package cn.emac.demo.reactor;

import cn.emac.demo.reactor.domain.Restaurant;
import cn.emac.demo.reactor.repositories.ReactiveRestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.Exceptions;
import reactor.core.publisher.ConnectableFlux;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Emac
 * @since 2017-06-11
 */
public class BackpressureTests extends BaseTests {

    @BeforeEach
    public void beforeEach2() {
        // initialize load
        List<Restaurant> load = IntStream.range(0, 10)
                .mapToObj(i -> new Restaurant("hello" + i, "hello" + i, "hello" + i))
                .collect(Collectors.toList());
        // insert
        ReactiveRestaurantRepository.INSTANCE.insert(load).blockLast();
    }

    @Test
    public void testNormandy() throws InterruptedException {
        // findAll mimic Backpressure
        CountDownLatch latch = new CountDownLatch(1);
        ConnectableFlux<Restaurant> connectableFlux = ReactiveRestaurantRepository.INSTANCE.findAll().publish();
        _addConitinuousSubscriber(connectableFlux, latch);
        connectableFlux.connect();
        latch.await();
    }

    @Test
    public void testBackpressure() throws InterruptedException {
        // findAll mimic Backpressure
        CountDownLatch latch = new CountDownLatch(1);
        ConnectableFlux<Restaurant> connectableFlux = ReactiveRestaurantRepository.INSTANCE.findAll().publish();
        connectableFlux.doOnEach(r -> latch.countDown());
        _addNappingSubscriber(connectableFlux);
        _addConitinuousSubscriber(connectableFlux, latch);
        connectableFlux.connect();
        latch.await();
    }

    private void _addConitinuousSubscriber(ConnectableFlux<Restaurant> connectableFlux, CountDownLatch latch) {
        connectableFlux.subscribe(r -> System.out.println("Continuous consuming " + r),
                e -> latch.countDown(),
                latch::countDown);
    }

    private void _addNappingSubscriber(ConnectableFlux<Restaurant> connectableFlux) {
        connectableFlux.subscribe(r -> {
            try {
                TimeUnit.SECONDS.sleep(1);
                System.out.println("Napping consuming " + r);
            } catch (InterruptedException e) {
                Exceptions.propagate(e);
            }
        });
    }
}
