package com.itticket.repository;

import com.itticket.entity.TicketAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketAttachmentRepository extends JpaRepository<TicketAttachment, Long> {
    
    List<TicketAttachment> findByTicketIdAndDeleted(Long ticketId, Integer deleted);
    
    void deleteByTicketId(Long ticketId);
}