package com.demo.metrics.web;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class DemoController {

    private final Counter ordersCreated;
    private final Timer orderProcessing;

    public DemoController(MeterRegistry registry) {
        this.ordersCreated = Counter.builder("demo.orders.created")
                .description("Pedidos simulados criados")
                .tag("source", "demo-api")
                .register(registry);
        this.orderProcessing = Timer.builder("demo.orders.processing")
                .description("Tempo de processamento simulado")
                .register(registry);
    }

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("status", "ok");
    }

    @PostMapping("/orders/simulate")
    public Map<String, Object> simulateOrder() throws InterruptedException {
        long delayMs = ThreadLocalRandom.current().nextLong(50, 400);
        orderProcessing.record(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });
        ordersCreated.increment();
        return Map.of(
                "orderId", ThreadLocalRandom.current().nextInt(1000, 9999),
                "delayMs", delayMs,
                "message", "pedido simulado"
        );
    }

    @GetMapping("/load")
    public Map<String, String> generateLoad() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            simulateOrder();
        }
        return Map.of("status", "generated 5 orders");
    }
}
