package com.pcgear.complink.pcgear.Delivery.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QDelivery is a Querydsl query type for Delivery
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDelivery extends EntityPathBase<Delivery> {

    private static final long serialVersionUID = 1187393515L;

    public static final QDelivery delivery = new QDelivery("delivery");

    public final StringPath carrierId = createString("carrierId");

    public final DateTimePath<java.time.LocalDateTime> completedAt = createDateTime("completedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath customerId = createString("customerId");

    public final NumberPath<Integer> deliveryId = createNumber("deliveryId", Integer.class);

    public final EnumPath<com.pcgear.complink.pcgear.Delivery.model.DeliveryStatus> deliveryStatus = createEnum("deliveryStatus", com.pcgear.complink.pcgear.Delivery.model.DeliveryStatus.class);

    public final NumberPath<Integer> orderId = createNumber("orderId", Integer.class);

    public final StringPath recipientAddr = createString("recipientAddr");

    public final StringPath recipientName = createString("recipientName");

    public final StringPath recipientPhone = createString("recipientPhone");

    public final StringPath trackingNumber = createString("trackingNumber");

    public QDelivery(String variable) {
        super(Delivery.class, forVariable(variable));
    }

    public QDelivery(Path<? extends Delivery> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDelivery(PathMetadata metadata) {
        super(Delivery.class, metadata);
    }

}

