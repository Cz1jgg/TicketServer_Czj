package com.itticket.repository;

import com.itticket.entity.TicketProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketProcessRepository extends JpaRepository<TicketProcess, Long> {
    
    List<TicketProcess> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
}