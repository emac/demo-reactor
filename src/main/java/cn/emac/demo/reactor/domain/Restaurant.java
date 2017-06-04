package cn.emac.demo.reactor.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author Emac
 * @since 2017-05-29
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Restaurant {

    @NonNull
    private String name;
    @NonNull
    private String address;
    @NonNull
    private String telephone;
}
