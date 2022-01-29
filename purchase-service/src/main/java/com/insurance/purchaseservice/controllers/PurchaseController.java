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
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PurchaseController {
    @Autowired
    ReactivePurchaseRepository repository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value(value = "${KAFKA_TOPIC_1}")
    private String kafkaTopic1;

    private final PurchaseMetrics metrics;

    @GetMapping("/")
    public Flux<Purchase> getPurchases() {
        return repository.findAll();
    }

    @GetMapping(path = "/{id}/exists")
    public Mono<Boolean> exists(@PathVariable("id") String id) {
        ObjectId purchase_id = new ObjectId(id);
        return repository.existsById(purchase_id);
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

    @GetMapping("/{id}")
    public ResponseEntity<Purchase> getPurchase(@PathVariable("id") String id) {
        Purchase p = repository.findById(new ObjectId(id)).block();
        if (p == null) {
            return new ResponseEntity<>(p, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(p, HttpStatus.OK);
    }

    @PostMapping(path = "/", consumes = "application/JSON", produces = "application/JSON")
    public Mono<Purchase> newPurchase(@RequestBody Purchase p) {
        metrics.increment();
        return repository.save(p).flatMap(purchase -> {
            kafkaTemplate.send(kafkaTopic1, "PolicyPurchase: Checking User|" + p.get_user_string() + "|" + p.get_id_string());
            return Mono.just(p);
        });

    }

}

@Component
@RequiredArgsConstructor
class PurchaseMetrics {
    @Qualifier("purchaseCounter")
    private final Counter counter;

    void increment() {
        counter.increment();
    }

    long value() {
        return (long) counter.count();
    }
}

@Configuration
@RequiredArgsConstructor
class PurchaseMetricsConfig {
    @Bean
    @Qualifier("purchaseCounter")
    Counter purchaseCounter(MeterRegistry registry) {
        return Counter.builder("purchase_counter")
                .description("Number of purchases")
                .register(registry);
    }
}
