package com.itticket.repository;

import com.itticket.entity.TicketLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketLogRepository extends JpaRepository<TicketLog, Long> {
    
    List<TicketLog> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
    
    Page<TicketLog> findByOperatorIdOrderByCreatedAtDesc(Long operatorId, Pageable pageable);
    
    Page<TicketLog> findByOperationTypeOrderByCreatedAtDesc(String operationType, Pageable pageable);
}