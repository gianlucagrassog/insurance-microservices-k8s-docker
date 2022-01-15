package com.insurance.policyservice.repository;

import com.insurance.policyservice.model.Policy;
import org.bson.types.ObjectId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ReactivePolicyRepository extends ReactiveCrudRepository<Policy, ObjectId> {
}
