package com.itticket.repository;

import com.itticket.entity.SlaRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlaRuleRepository extends JpaRepository<SlaRule, Long> {
    
    Optional<SlaRule> findByPriorityLevel(Integer priorityLevel);
    
    List<SlaRule> findByDeleted(Integer deleted);
    
    Optional<SlaRule> findByIdAndDeleted(Long id, Integer deleted);
}