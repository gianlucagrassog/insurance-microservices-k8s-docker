package com.insurance.purchaseservice.repository;

import com.insurance.purchaseservice.model.Purchase;
import org.bson.types.ObjectId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ReactivePurchaseRepository extends ReactiveCrudRepository<Purchase, ObjectId> {
}
