package cn.emac.demo.reactor;

import cn.emac.demo.reactor.domain.Restaurant;
import cn.emac.demo.reactor.repositories.ReactiveRestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.Exceptions;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Emac
 * @since 2017-06-18
 */
public class ConnectableFluxTests extends BaseTests {

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
    public void testNonConnectable() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Flux<Restaurant> flux = ReactiveRestaurantRepository.INSTANCE.findAll();
        _addNappingSubscriber(flux, 1, latch);
        _addConitinuousSubscriber(flux, latch);
        latch.await();
    }

    @Test
    public void testNonConnectable2() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        Flux<Restaurant> flux = ReactiveRestaurantRepository.INSTANCE.findAll();
        _addNappingSubscriber(flux, 1, latch);
        flux = flux.map(r -> {
            r.setName(r.getName().toUpperCase());
            return r;
        });
        TimeUnit.SECONDS.sleep(3);
        _addNappingSubscriber(flux, 2, latch);
        latch.await();
    }

    @Test
    public void testSingular() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ConnectableFlux<Restaurant> connectableFlux = ReactiveRestaurantRepository.INSTANCE.findAll().publish();
        _addConitinuousSubscriber(connectableFlux, latch);
        connectableFlux.connect();
        latch.await();
    }

    @Test
    public void testMultiple() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        ConnectableFlux<Restaurant> connectableFlux = ReactiveRestaurantRepository.INSTANCE.findAll().publish();
        _addNappingSubscriber(connectableFlux, 1, latch);
        _addConitinuousSubscriber(connectableFlux, latch);
        connectableFlux.connect();
        latch.await();
    }

    private void _addConitinuousSubscriber(Flux<Restaurant> flux, CountDownLatch latch) {
        flux.subscribe(r -> System.out.println("Continuous consuming " + r),
                e -> latch.countDown(),
                latch::countDown);
    }

    private void _addNappingSubscriber(Flux<Restaurant> flux, long seconds, CountDownLatch latch) {
        flux.subscribe(r -> {
                    try {
                        TimeUnit.SECONDS.sleep(seconds);
                        System.out.println("Napping consuming " + r);
                    } catch (InterruptedException e) {
                        Exceptions.propagate(e);
                    }
                },
                e -> latch.countDown(),
                latch::countDown);
    }
}
