package com.pcgear.complink.pcgear.Delivery.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QDeliveryWebhookRegisterRetry is a Querydsl query type for DeliveryWebhookRegisterRetry
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDeliveryWebhookRegisterRetry extends EntityPathBase<DeliveryWebhookRegisterRetry> {

    private static final long serialVersionUID = -1233935079L;

    public static final QDeliveryWebhookRegisterRetry deliveryWebhookRegisterRetry = new QDeliveryWebhookRegisterRetry("deliveryWebhookRegisterRetry");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath lastError = createString("lastError");

    public final NumberPath<Integer> maxRetries = createNumber("maxRetries", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> nextRunAt = createDateTime("nextRunAt", java.time.LocalDateTime.class);

    public final StringPath payload = createString("payload");

    public final NumberPath<Integer> retryCount = createNumber("retryCount", Integer.class);

    public final EnumPath<com.pcgear.complink.pcgear.Delivery.enums.RetryStatus> status = createEnum("status", com.pcgear.complink.pcgear.Delivery.enums.RetryStatus.class);

    public final NumberPath<Long> targetId = createNumber("targetId", Long.class);

    public QDeliveryWebhookRegisterRetry(String variable) {
        super(DeliveryWebhookRegisterRetry.class, forVariable(variable));
    }

    public QDeliveryWebhookRegisterRetry(Path<? extends DeliveryWebhookRegisterRetry> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDeliveryWebhookRegisterRetry(PathMetadata metadata) {
        super(DeliveryWebhookRegisterRetry.class, metadata);
    }

}

