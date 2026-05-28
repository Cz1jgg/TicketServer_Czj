package com.itticket.repository;

import com.itticket.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    
    Optional<TicketType> findByTypeCode(String typeCode);
    
    List<TicketType> findByDeleted(Integer deleted);
    
    Optional<TicketType> findByIdAndDeleted(Long id, Integer deleted);
}