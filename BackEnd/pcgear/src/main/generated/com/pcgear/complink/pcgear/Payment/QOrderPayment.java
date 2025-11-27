package com.pcgear.complink.pcgear.Payment;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrderPayment is a Querydsl query type for OrderPayment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrderPayment extends EntityPathBase<OrderPayment> {

    private static final long serialVersionUID = 1984346374L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrderPayment orderPayment = new QOrderPayment("orderPayment");

    public final NumberPath<Integer> amount = createNumber("amount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final com.pcgear.complink.pcgear.Order.model.QOrder order;

    public final DateTimePath<java.time.LocalDateTime> paidAt = createDateTime("paidAt", java.time.LocalDateTime.class);

    public final StringPath paymentId = createString("paymentId");

    public final StringPath paymentMethod = createString("paymentMethod");

    public final EnumPath<com.pcgear.complink.pcgear.Payment.model.PaymentStatus> paymentStatus = createEnum("paymentStatus", com.pcgear.complink.pcgear.Payment.model.PaymentStatus.class);

    public final StringPath userId = createString("userId");

    public QOrderPayment(String variable) {
        this(OrderPayment.class, forVariable(variable), INITS);
    }

    public QOrderPayment(Path<? extends OrderPayment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrderPayment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrderPayment(PathMetadata metadata, PathInits inits) {
        this(OrderPayment.class, metadata, inits);
    }

    public QOrderPayment(Class<? extends OrderPayment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.order = inits.isInitialized("order") ? new com.pcgear.complink.pcgear.Order.model.QOrder(forProperty("order"), inits.get("order")) : null;
    }

}

