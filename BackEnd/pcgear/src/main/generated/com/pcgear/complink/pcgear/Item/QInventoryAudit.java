package com.pcgear.complink.pcgear.Item;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInventoryAudit is a Querydsl query type for InventoryAudit
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInventoryAudit extends EntityPathBase<InventoryAudit> {

    private static final long serialVersionUID = -134927276L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInventoryAudit inventoryAudit = new QInventoryAudit("inventoryAudit");

    public final NumberPath<Integer> correctedAvailableQuantity = createNumber("correctedAvailableQuantity", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> discrepancy = createNumber("discrepancy", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QItem item;

    public final NumberPath<Integer> previousAvailableQuantity = createNumber("previousAvailableQuantity", Integer.class);

    public final StringPath reason = createString("reason");

    public QInventoryAudit(String variable) {
        this(InventoryAudit.class, forVariable(variable), INITS);
    }

    public QInventoryAudit(Path<? extends InventoryAudit> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInventoryAudit(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInventoryAudit(PathMetadata metadata, PathInits inits) {
        this(InventoryAudit.class, metadata, inits);
    }

    public QInventoryAudit(Class<? extends InventoryAudit> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.item = inits.isInitialized("item") ? new QItem(forProperty("item")) : null;
    }

}

