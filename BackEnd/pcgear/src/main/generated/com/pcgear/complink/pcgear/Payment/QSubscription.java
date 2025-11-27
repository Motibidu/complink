package com.pcgear.complink.pcgear.Payment;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSubscription is a Querydsl query type for Subscription
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSubscription extends EntityPathBase<Subscription> {

    private static final long serialVersionUID = -2072771765L;

    public static final QSubscription subscription = new QSubscription("subscription");

    public final NumberPath<Integer> amount = createNumber("amount", Integer.class);

    public final StringPath billingKey = createString("billingKey");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> nextBillingTime = createDateTime("nextBillingTime", java.time.LocalDateTime.class);

    public final StringPath orderName = createString("orderName");

    public final DateTimePath<java.time.LocalDateTime> startTime = createDateTime("startTime", java.time.LocalDateTime.class);

    public final StringPath status = createString("status");

    public final NumberPath<Integer> subscriptionId = createNumber("subscriptionId", Integer.class);

    public final StringPath trackingId = createString("trackingId");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final StringPath userId = createString("userId");

    public QSubscription(String variable) {
        super(Subscription.class, forVariable(variable));
    }

    public QSubscription(Path<? extends Subscription> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSubscription(PathMetadata metadata) {
        super(Subscription.class, metadata);
    }

}

