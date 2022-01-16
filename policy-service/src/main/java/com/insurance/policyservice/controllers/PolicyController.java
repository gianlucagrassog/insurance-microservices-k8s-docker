package com.insurance.policyservice.controllers;

import com.insurance.policyservice.model.Policy;
import com.insurance.policyservice.repository.ReactivePolicyRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/policies")
public class PolicyController {

    @Autowired
    ReactivePolicyRepository repository;

    @GetMapping("/{id}")
    public ResponseEntity<Mono<Policy>> getPolicy(@PathVariable("id") String id) {
        Mono<Policy> p = repository.findById(new ObjectId(id));
        if (p == null) {
            return new ResponseEntity<>(p, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(p, HttpStatus.OK);
    }
    @DeleteMapping("/{id}")
    public @ResponseBody ResponseEntity<Boolean> deletePolicy(@PathVariable("id") String id) {

        boolean exists = exists(id).getBody().block();
        if (exists) {
            repository.deleteById(new ObjectId(id)).subscribe();
        } else
            return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(
                true, HttpStatus.OK
        );
    }
    @GetMapping("/")
    public Flux<Policy> getPolicies() {
        return repository.findAll();
    }

    @GetMapping("/{id}/exists")
    public @ResponseBody ResponseEntity<Mono<Boolean>> exists(@PathVariable("id") String id) {
        Mono<Boolean> ret = repository.existsById(new ObjectId(id));
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }




    @PostMapping(path = "/", consumes = {"application/JSON"}, produces = "application/json")
    public Mono<Policy> createPolicy(@RequestBody Policy p) {
        return repository.save(p);
    }

}
