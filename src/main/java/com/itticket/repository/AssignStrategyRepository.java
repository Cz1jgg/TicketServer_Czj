package com.itticket.repository;

import com.itticket.entity.AssignStrategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignStrategyRepository extends JpaRepository<AssignStrategy, Long> {
    
    Optional<AssignStrategy> findByStrategyCode(String strategyCode);
    
    List<AssignStrategy> findByIsActive(Integer isActive);
    
    List<AssignStrategy> findByDeleted(Integer deleted);
}