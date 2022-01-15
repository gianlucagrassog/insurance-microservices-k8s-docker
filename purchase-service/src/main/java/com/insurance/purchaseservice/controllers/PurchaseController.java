package com.insurance.purchaseservice.controllers;

import com.insurance.purchaseservice.model.Purchase;
import com.insurance.purchaseservice.repository.ReactivePurchaseRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
public class PurchaseController {

    @Autowired
    ReactivePurchaseRepository repository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value(value="${KAFKA_TOPIC_1}")
    private String kafkaTopic1;


    @GetMapping("/")
    public Flux<Purchase> getPurchases() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Purchase> getPurchase(@PathVariable("id") String id) {
        Purchase p = repository.findById(new ObjectId(id)).block();
        if (p == null) {
            return new ResponseEntity<>(p, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(p, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deletePurchase(@PathVariable("id") String id) {
        Boolean ret = repository.existsById(new ObjectId(id)).block();
        if (ret) {
            repository.deleteById(new ObjectId(id)).subscribe();
            return new ResponseEntity<>(true, HttpStatus.OK);
        }
        return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
    }

    @PutMapping(path="/{id}", consumes={"application/JSON"}, produces="application/json")
    public ResponseEntity<Mono<Purchase>> editPurchase(@PathVariable("id") String id, @RequestBody Purchase p) {
        ObjectId purchase_id = new ObjectId(id);
        Purchase oldp = repository.findById(purchase_id).block();
        if (oldp == null)
            return new ResponseEntity<>(Mono.just(new Purchase(null, null, null, null)), HttpStatus.NOT_FOUND);

        p.set_id(purchase_id);

        return new ResponseEntity<>(repository.save(p), HttpStatus.OK);
    }

    @PostMapping(path="/", consumes = "application/JSON", produces = "application/JSON")
    public Mono<Purchase> newPurchase(@RequestBody Purchase p) {
        return repository.save(p).flatMap(purchase -> {
            kafkaTemplate.send(kafkaTopic1, "PolicyPurchase: Checking User|" + p.get_user_string() + "|" + p.get_id_string());
            return Mono.just(p);
        });

    }

    @GetMapping(path="/{id}/exists")
    public Mono<Boolean> exists(@PathVariable("id") String id) {
        ObjectId purchase_id = new ObjectId(id);
        return repository.existsById(purchase_id);
    }
}
