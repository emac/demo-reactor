package cn.emac.demo.reactor.repositories;

import cn.emac.demo.reactor.domain.Restaurant;
import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.util.List;

/**
 * @author Emac
 * @since 2017-06-04
 */
@Slf4j
public class ImperativeRestaurantRepository {

    public static ImperativeRestaurantRepository INSTANCE = ImperativeRestaurantRepositoryHolder.INSTANCE;

    private static class ImperativeRestaurantRepositoryHolder {
        static final ImperativeRestaurantRepository INSTANCE = new ImperativeRestaurantRepository();
    }

    private MongoCollection<Document> collection;

    private ImperativeRestaurantRepository() {
        MongoClientOptions options = MongoClientOptions.builder().connectionsPerHost(20).build();
        collection = new MongoClient("localhost", options).getDatabase("test").getCollection("restaurant");
    }

    public void deleteAll() {
        collection.drop();
    }

    public void insert(List<Restaurant> restaurants) {
        List<Document> docs = RestaurantTransfer.toDocuments(restaurants);
        collection.insertMany(docs);
    }

    public List<Restaurant> findAll() {
        List<Restaurant> restaurants = Lists.newArrayList();
        MongoCursor<Document> cursor = collection.find().iterator();
        try {
            while (cursor.hasNext()) {
                RestaurantTransfer.toDomainObject(cursor.next()).ifPresent(r -> restaurants.add(r));
            }
        } finally {
            cursor.close();
        }
        return restaurants;
    }
}
