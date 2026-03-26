package com.pcgear.complink.pcgear.Dashboard;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTopItemSalesCache is a Querydsl query type for TopItemSalesCache
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTopItemSalesCache extends EntityPathBase<TopItemSalesCache> {

    private static final long serialVersionUID = 468640542L;

    public static final QTopItemSalesCache topItemSalesCache = new QTopItemSalesCache("topItemSalesCache");

    public final NumberPath<Integer> availableQuantity = createNumber("availableQuantity", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> cachedAt = createDateTime("cachedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> itemId = createNumber("itemId", Integer.class);

    public final StringPath itemName = createString("itemName");

    public final NumberPath<Integer> rankPosition = createNumber("rankPosition", Integer.class);

    public final NumberPath<Integer> totalQuantity = createNumber("totalQuantity", Integer.class);

    public final NumberPath<Long> totalRevenue = createNumber("totalRevenue", Long.class);

    public QTopItemSalesCache(String variable) {
        super(TopItemSalesCache.class, forVariable(variable));
    }

    public QTopItemSalesCache(Path<? extends TopItemSalesCache> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTopItemSalesCache(PathMetadata metadata) {
        super(TopItemSalesCache.class, metadata);
    }

}

