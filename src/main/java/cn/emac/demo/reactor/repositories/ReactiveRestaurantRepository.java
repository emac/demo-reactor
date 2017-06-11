package cn.emac.demo.reactor.repositories;

import cn.emac.demo.reactor.domain.Restaurant;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.Success;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

/**
 * @author Emac
 * @since 2017-06-04
 */
@Slf4j
public class ReactiveRestaurantRepository {

    public static ReactiveRestaurantRepository INSTANCE = ReactiveRestaurantRepositoryHolder.INSTANCE;

    private static class ReactiveRestaurantRepositoryHolder {
        static final ReactiveRestaurantRepository INSTANCE = new ReactiveRestaurantRepository();
    }

    private MongoCollection<Document> collection;

    private ReactiveRestaurantRepository() {
        collection = MongoClients.create("mongodb://localhost:27017/?maxPoolSize=20").getDatabase("test").getCollection("restaurant");
    }

    public Flux<Success> insert(List<Restaurant> restaurants) {
        List<Document> docs = RestaurantTransfer.toDocuments(restaurants);
        return Flux.from(collection.insertMany(docs));
    }

    public Flux<Restaurant> findAll() {
        return Flux.from(collection.find())
                .map(RestaurantTransfer::toDomainObject)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }
}
