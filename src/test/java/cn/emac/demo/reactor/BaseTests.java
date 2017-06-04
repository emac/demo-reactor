package cn.emac.demo.reactor;

import cn.emac.demo.reactor.repositories.ImperativeRestaurantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author Emac
 * @since 2017-06-04
 */
public class BaseTests {

    public static final int CONCURRENT_SIZE = 100;
    public static final int PACK_SIZE = 10_000;

    private LocalDateTime start;

    @BeforeEach
    public void beforeEach() {
        // start from scratch
        ImperativeRestaurantRepository.INSTANCE.deleteAll();
        start = LocalDateTime.now();
    }

    @AfterEach
    public void afterEach() {
        System.out.println(Duration.between(start, LocalDateTime.now()).toMillis());
    }
}
