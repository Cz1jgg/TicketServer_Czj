package com.itticket.service;

import com.itticket.common.BusinessException;
import com.itticket.common.Constants;
import com.itticket.common.TicketStatus;
import com.itticket.entity.SlaRule;
import com.itticket.entity.TicketMain;
import com.itticket.repository.SlaRuleRepository;
import com.itticket.repository.TicketMainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlaService {

    private final TicketMainRepository ticketMainRepository;
    private final SlaRuleRepository slaRuleRepository;

    @Scheduled(fixedDelayString = "${sla.check-interval}")
    public void checkSlaTimeout() {
        List<TicketMain> activeTickets = ticketMainRepository.findActiveTicketsForSlaCheck();
        
        for (TicketMain ticket : activeTickets) {
            checkTicketSla(ticket);
        }
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void checkSuspendedTickets() {
        LocalDateTime expireTime = LocalDateTime.now().minusNanos(Constants.SUSPEND_MAX_DURATION * 1_000_000);
        List<TicketMain> expiredTickets = ticketMainRepository.findExpiredSuspendedTickets(expireTime);
        
        for (TicketMain ticket : expiredTickets) {
            resumeExpiredSuspendedTicket(ticket);
        }
    }

    private void checkTicketSla(TicketMain ticket) {
        if (ticket.getSlaStartTime() == null) {
            return;
        }
        
        SlaRule slaRule = ticket.getSlaRule();
        if (slaRule == null) {
            return;
        }
        
        long elapsedMillis = calculateElapsedTime(ticket);
        long resolveTimeMillis = slaRule.getResolveTime() * 60 * 1000L;
        
        if (elapsedMillis >= resolveTimeMillis) {
            log.warn("工单 {} SLA超时，应在 {} 分钟内解决", ticket.getTicketNo(), slaRule.getResolveTime());
        }
    }

    private long calculateElapsedTime(TicketMain ticket) {
        LocalDateTime slaStartTime = ticket.getSlaStartTime();
        LocalDateTime now = LocalDateTime.now();
        
        long totalMillis = Duration.between(slaStartTime, now).toMillis();
        long pauseDuration = ticket.getSlaPauseDuration() != null ? ticket.getSlaPauseDuration() : 0L;
        
        return totalMillis - pauseDuration;
    }

    @Transactional
    public void resumeExpiredSuspendedTicket(TicketMain ticket) {
        if (TicketStatus.SUSPENDED.name().equals(ticket.getStatus())) {
            LocalDateTime pauseTime = ticket.getSlaPauseTime();
            if (pauseTime != null) {
                long pauseDuration = Duration.between(pauseTime, LocalDateTime.now()).toMillis();
                ticket.setSlaPauseDuration(ticket.getSlaPauseDuration() + pauseDuration);
            }
            
            ticket.setStatus(TicketStatus.PROCESSING.name());
            ticket.setSlaPauseTime(null);
            ticketMainRepository.save(ticket);
            
            log.info("工单 {} 挂起超时自动恢复", ticket.getTicketNo());
        }
    }

    public long getRemainingSlaTime(TicketMain ticket) {
        if (ticket.getSlaStartTime() == null) {
            return -1;
        }
        
        SlaRule slaRule = ticket.getSlaRule();
        if (slaRule == null) {
            return -1;
        }
        
        long elapsedMillis = calculateElapsedTime(ticket);
        long resolveTimeMillis = slaRule.getResolveTime() * 60 * 1000L;
        
        return Math.max(0, resolveTimeMillis - elapsedMillis);
    }

    public SlaRule getSlaRuleById(Long id) {
        return slaRuleRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> BusinessException.of(404, "SLA规则不存在"));
    }

    public List<SlaRule> getAllSlaRules() {
        return slaRuleRepository.findByDeleted(0);
    }
}