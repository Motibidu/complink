package com.pcgear.complink.pcgear.Item;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QItem is a Querydsl query type for Item
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QItem extends EntityPathBase<Item> {

    private static final long serialVersionUID = 818526664L;

    public static final QItem item = new QItem("item");

    public final NumberPath<Integer> AvailableQuantity = createNumber("AvailableQuantity", Integer.class);

    public final EnumPath<ItemCategory> itemCategory = createEnum("itemCategory", ItemCategory.class);

    public final NumberPath<Integer> itemId = createNumber("itemId", Integer.class);

    public final StringPath itemName = createString("itemName");

    public final NumberPath<Integer> purchasePrice = createNumber("purchasePrice", Integer.class);

    public final NumberPath<Integer> QuantityOnHand = createNumber("QuantityOnHand", Integer.class);

    public final NumberPath<Integer> sellingPrice = createNumber("sellingPrice", Integer.class);

    public QItem(String variable) {
        super(Item.class, forVariable(variable));
    }

    public QItem(Path<? extends Item> path) {
        super(path.getType(), path.getMetadata());
    }

    public QItem(PathMetadata metadata) {
        super(Item.class, metadata);
    }

}

