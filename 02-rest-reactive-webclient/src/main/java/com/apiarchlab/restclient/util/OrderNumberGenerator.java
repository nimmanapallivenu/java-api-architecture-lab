package com.apiarchlab.restclient.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class OrderNumberGenerator {
    private static final AtomicLong counter = new AtomicLong(1);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(formatter);
        long sequence = counter.getAndIncrement();
        return String.format("ORD-%s-%06d", timestamp, sequence);
    }
}

