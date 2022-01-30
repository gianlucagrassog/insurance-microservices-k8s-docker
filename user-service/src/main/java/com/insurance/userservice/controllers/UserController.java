package com.insurance.userservice.controllers;
import com.insurance.userservice.model.User;
import com.insurance.userservice.repository.ReactiveUserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    @Autowired
    ReactiveUserRepository repository;

    private final UserMetrics metrics;

    @GetMapping("/")
    public Flux<User> getUsers() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mono<User>> getUser(@PathVariable("id") String id) {
        Mono<User> u = repository.findById(new ObjectId(id));
        if (u == null) {
            return new ResponseEntity<>(u, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(u, HttpStatus.OK);
    }

    @GetMapping("/{id}/exists")
    public @ResponseBody ResponseEntity<Mono<Boolean>> exists(@PathVariable("id") String id) {
        Mono<Boolean> ret = repository.existsById(new ObjectId(id));
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public @ResponseBody ResponseEntity<Boolean> deleteUser(@PathVariable("id") String id) {

        boolean exists = exists(id).getBody().block();
        if (exists) {
            repository.deleteById(new ObjectId(id)).subscribe();
        } else
            return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(
                true, HttpStatus.OK
        );
    }

    @PostMapping(path="/", consumes={"application/JSON"}, produces="application/json")
    public Mono<User> createUser(@RequestBody User u) {
        metrics.increment();
        return repository.save(u);
    }

}

@Component
@RequiredArgsConstructor
class UserMetrics {
    @Qualifier("userCounter")
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
class UserMetricsConfig {
    @Bean
    @Qualifier("userCounter")
    Counter userCounter(MeterRegistry registry) {
        return Counter.builder("user_counter")
                .description("Number of counters")
                .register(registry);
    }
}
