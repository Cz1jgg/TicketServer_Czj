package com.itticket.util;

import com.itticket.common.Constants;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TicketNoGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final AtomicInteger counter = new AtomicInteger(0);

    public String generateTicketNo() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        int sequence = counter.incrementAndGet() % 1000;
        return String.format("%s%s%03d", Constants.TICKET_PREFIX, timestamp, sequence);
    }
}