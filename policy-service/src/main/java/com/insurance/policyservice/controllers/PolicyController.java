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
import org.springframework.beans.factory.annotation.Value;
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
@RequestMapping("/policies")
public class PolicyController {

    @Autowired
    ReactivePolicyRepository repository;

    private final PolicyMetrics metrics;

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
        metrics.increment();
        return repository.save(p);
    }

}

@Component
@RequiredArgsConstructor
class PolicyMetrics {
    @Qualifier("policyCounter")
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
class PolicyMetricsConfig {
    @Bean
    @Qualifier("policyCounter")
    Counter policyCounter(MeterRegistry registry) {
        return Counter.builder("policy_counter")
                .description("Number of policies")
                .register(registry);
    }
}
