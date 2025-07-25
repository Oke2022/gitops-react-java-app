package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

@RestController
class HelloController {
    
    @GetMapping("/hello")
    public String hello() {
        return "Hello from Java Backend! GitOps is working!";
    }
    
    @GetMapping("/health")
    public String health() {
        return "Backend is healthy";
    }
}
