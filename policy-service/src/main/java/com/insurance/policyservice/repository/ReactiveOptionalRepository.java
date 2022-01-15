package com.insurance.policyservice.repository;

import com.insurance.policyservice.model.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ReactiveOptionalRepository extends ReactiveCrudRepository<Optional, ObjectId> {
}
