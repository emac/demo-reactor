package cn.emac.demo.reactor.repositories;

import cn.emac.demo.reactor.domain.Restaurant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Emac
 * @since 2017-06-04
 */
@Slf4j
public class RestaurantTransfer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public static List<Document> toDocuments(List<Restaurant> restaurants) {
        return restaurants.stream().map(r -> {
            try {
                return Document.parse(OBJECT_MAPPER.writeValueAsString(r));
            } catch (JsonProcessingException e) {
                log.error(e.getMessage(), e);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static Optional<Restaurant> toDomainObject(Document document) {
        try {
            return Optional.of(OBJECT_MAPPER.readValue(document.toJson(), Restaurant.class));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }
}
