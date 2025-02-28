package com.app.webnongsan.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "order_detail")
@Getter
@Setter
public class OrderDetail {
    @EmbeddedId
    private OrderDetailId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("orderId")
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    private Product product;

    @Min(value = 1)
    private int quantity;

    private Double unit_price;
}
