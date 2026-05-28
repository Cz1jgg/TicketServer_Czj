package com.itticket.repository;

import com.itticket.entity.TicketEvaluate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketEvaluateRepository extends JpaRepository<TicketEvaluate, Long> {
    
    Optional<TicketEvaluate> findByTicketId(Long ticketId);
}