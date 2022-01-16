package com.insurance.policyservice.controllers;

import com.insurance.policyservice.model.Optional;
import com.insurance.policyservice.repository.ReactiveOptionalRepository;
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
@RequestMapping("/optionals")
public class OptionalController {

    @Autowired
    ReactiveOptionalRepository optional_repository;

    @GetMapping("/{id}")
    public ResponseEntity<Mono<Optional>> getOptional(@PathVariable("id") String id) {
        Mono<Optional> o = optional_repository.findById(new ObjectId(id));
        if (o == null) {
            return new ResponseEntity<>(o, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(o, HttpStatus.OK);
    }

    @GetMapping("/")
    public Flux<Optional> getOptionals() {
        return optional_repository.findAll();
    }

    @GetMapping("/{id}/exists")
    public @ResponseBody ResponseEntity<Mono<Boolean>> exists(@PathVariable("id") String id) {
        Mono<Boolean> ret = optional_repository.existsById(new ObjectId(id));
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public @ResponseBody ResponseEntity<Boolean> deleteOptional(@PathVariable("id") String id) {

        boolean exists = exists(id).getBody().block();
        if (exists) {
            optional_repository.deleteById(new ObjectId(id)).subscribe();
        } else
            return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(
                true, HttpStatus.OK
        );
    }

    @PostMapping(path = "/", consumes = {"application/JSON"}, produces = "application/json")
    public Mono<Optional> createOptional(@RequestBody Optional o) {
        return optional_repository.save(o);
    }

}
