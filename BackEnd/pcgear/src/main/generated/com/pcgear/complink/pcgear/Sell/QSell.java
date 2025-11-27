package com.pcgear.complink.pcgear.Sell;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSell is a Querydsl query type for Sell
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSell extends EntityPathBase<Sell> {

    private static final long serialVersionUID = 1440713000L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSell sell = new QSell("sell");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final com.pcgear.complink.pcgear.Customer.QCustomer customer;

    public final NumberPath<java.math.BigDecimal> grandAmount = createNumber("grandAmount", java.math.BigDecimal.class);

    public final com.pcgear.complink.pcgear.User.entity.QUserEntity manager;

    public final StringPath memo = createString("memo");

    public final com.pcgear.complink.pcgear.Order.model.QOrder order;

    public final DateTimePath<java.time.LocalDateTime> sellDate = createDateTime("sellDate", java.time.LocalDateTime.class);

    public final NumberPath<Integer> sellId = createNumber("sellId", Integer.class);

    public final NumberPath<java.math.BigDecimal> totalAmount = createNumber("totalAmount", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> vatAmount = createNumber("vatAmount", java.math.BigDecimal.class);

    public QSell(String variable) {
        this(Sell.class, forVariable(variable), INITS);
    }

    public QSell(Path<? extends Sell> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSell(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSell(PathMetadata metadata, PathInits inits) {
        this(Sell.class, metadata, inits);
    }

    public QSell(Class<? extends Sell> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customer = inits.isInitialized("customer") ? new com.pcgear.complink.pcgear.Customer.QCustomer(forProperty("customer")) : null;
        this.manager = inits.isInitialized("manager") ? new com.pcgear.complink.pcgear.User.entity.QUserEntity(forProperty("manager")) : null;
        this.order = inits.isInitialized("order") ? new com.pcgear.complink.pcgear.Order.model.QOrder(forProperty("order"), inits.get("order")) : null;
    }

}

