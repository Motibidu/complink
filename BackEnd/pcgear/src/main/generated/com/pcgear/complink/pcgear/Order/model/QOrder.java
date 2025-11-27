package com.pcgear.complink.pcgear.Order.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrder is a Querydsl query type for Order
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrder extends EntityPathBase<Order> {

    private static final long serialVersionUID = -959359965L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrder order = new QOrder("order1");

    public final EnumPath<com.pcgear.complink.pcgear.Assembly.AssemblyStatus> assemblyStatus = createEnum("assemblyStatus", com.pcgear.complink.pcgear.Assembly.AssemblyStatus.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final com.pcgear.complink.pcgear.Customer.QCustomer customer;

    public final DatePath<java.time.LocalDate> deliveryDate = createDate("deliveryDate", java.time.LocalDate.class);

    public final NumberPath<java.math.BigDecimal> grandAmount = createNumber("grandAmount", java.math.BigDecimal.class);

    public final StringPath impUid = createString("impUid");

    public final com.pcgear.complink.pcgear.User.entity.QUserEntity manager;

    public final StringPath merchantUid = createString("merchantUid");

    public final DatePath<java.time.LocalDate> orderDate = createDate("orderDate", java.time.LocalDate.class);

    public final NumberPath<Integer> orderId = createNumber("orderId", Integer.class);

    public final ListPath<OrderItem, QOrderItem> orderItems = this.<OrderItem, QOrderItem>createList("orderItems", OrderItem.class, QOrderItem.class, PathInits.DIRECT2);

    public final EnumPath<OrderStatus> orderStatus = createEnum("orderStatus", OrderStatus.class);

    public final DateTimePath<java.time.LocalDateTime> paidAt = createDateTime("paidAt", java.time.LocalDateTime.class);

    public final StringPath paymentLink = createString("paymentLink");

    public final NumberPath<java.math.BigDecimal> totalAmount = createNumber("totalAmount", java.math.BigDecimal.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final NumberPath<java.math.BigDecimal> vatAmount = createNumber("vatAmount", java.math.BigDecimal.class);

    public QOrder(String variable) {
        this(Order.class, forVariable(variable), INITS);
    }

    public QOrder(Path<? extends Order> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrder(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrder(PathMetadata metadata, PathInits inits) {
        this(Order.class, metadata, inits);
    }

    public QOrder(Class<? extends Order> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customer = inits.isInitialized("customer") ? new com.pcgear.complink.pcgear.Customer.QCustomer(forProperty("customer")) : null;
        this.manager = inits.isInitialized("manager") ? new com.pcgear.complink.pcgear.User.entity.QUserEntity(forProperty("manager")) : null;
    }

}

