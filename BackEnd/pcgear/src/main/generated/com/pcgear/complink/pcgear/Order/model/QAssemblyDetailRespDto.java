package com.pcgear.complink.pcgear.Order.model;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.pcgear.complink.pcgear.Order.model.QAssemblyDetailRespDto is a Querydsl Projection type for AssemblyDetailRespDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QAssemblyDetailRespDto extends ConstructorExpression<AssemblyDetailRespDto> {

    private static final long serialVersionUID = 823643373L;

    public QAssemblyDetailRespDto(com.querydsl.core.types.Expression<Integer> orderId, com.querydsl.core.types.Expression<OrderStatus> orderStatus, com.querydsl.core.types.Expression<com.pcgear.complink.pcgear.Assembly.AssemblyStatus> assemblyStatus, com.querydsl.core.types.Expression<? extends com.pcgear.complink.pcgear.Customer.Customer> customer, com.querydsl.core.types.Expression<? extends java.util.List<OrderItem>> orderItems) {
        super(AssemblyDetailRespDto.class, new Class<?>[]{int.class, OrderStatus.class, com.pcgear.complink.pcgear.Assembly.AssemblyStatus.class, com.pcgear.complink.pcgear.Customer.Customer.class, java.util.List.class}, orderId, orderStatus, assemblyStatus, customer, orderItems);
    }

}

