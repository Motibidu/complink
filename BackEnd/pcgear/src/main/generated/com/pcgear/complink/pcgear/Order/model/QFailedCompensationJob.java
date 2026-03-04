package com.pcgear.complink.pcgear.Order.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFailedCompensationJob is a Querydsl query type for FailedCompensationJob
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFailedCompensationJob extends EntityPathBase<FailedCompensationJob> {

    private static final long serialVersionUID = 472886747L;

    public static final QFailedCompensationJob failedCompensationJob = new QFailedCompensationJob("failedCompensationJob");

    public final EnumPath<FailedCompensationJob.CompensationType> compensationType = createEnum("compensationType", FailedCompensationJob.CompensationType.class);

    public final DateTimePath<java.time.LocalDateTime> completedAt = createDateTime("completedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath errorMessage = createString("errorMessage");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> nextRetryAt = createDateTime("nextRetryAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> orderId = createNumber("orderId", Integer.class);

    public final NumberPath<Integer> retryCount = createNumber("retryCount", Integer.class);

    public final EnumPath<FailedCompensationJob.JobStatus> status = createEnum("status", FailedCompensationJob.JobStatus.class);

    public QFailedCompensationJob(String variable) {
        super(FailedCompensationJob.class, forVariable(variable));
    }

    public QFailedCompensationJob(Path<? extends FailedCompensationJob> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFailedCompensationJob(PathMetadata metadata) {
        super(FailedCompensationJob.class, metadata);
    }

}

