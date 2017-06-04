package cn.emac.demo.reactor;

import cn.emac.demo.reactor.domain.Restaurant;
import cn.emac.demo.reactor.repositories.ImperativeRestaurantRepository;
import cn.emac.demo.reactor.repositories.ReactiveRestaurantRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Emac
 * @since 2017-06-04
 */
public class PerformanceTests extends BaseTests {

    private static List<Restaurant> load;

    @BeforeAll
    public static void beforeAll() {
        // initialize load
        load = IntStream.range(0, PACK_SIZE * 10)
                .mapToObj(i -> new Restaurant("hello" + i, "hello" + i, "hello" + i))
                .collect(Collectors.toList());
    }

    @RepeatedTest(3)
    public void testImperative() {
        // insert
        ImperativeRestaurantRepository.INSTANCE.insert(load);
        // findAll
        Assertions.assertEquals(load.size(), ImperativeRestaurantRepository.INSTANCE.findAll().size());
    }

    @RepeatedTest(3)
    public void testReactive() {
        // insert
        ReactiveRestaurantRepository.INSTANCE.insert(load).blockLast();
        // findAll
        Assertions.assertEquals(load.size(), ReactiveRestaurantRepository.INSTANCE.findAll().count().block().intValue());
    }
}
