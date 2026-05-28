package com.itticket.service;

import com.itticket.common.BusinessException;
import com.itticket.common.TicketStatus;
import com.itticket.dto.TicketCreateRequest;
import com.itticket.dto.TicketDetailResponse;
import com.itticket.dto.TicketQueryRequest;
import com.itticket.entity.*;
import com.itticket.repository.*;
import com.itticket.util.TicketNoGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketMainRepository ticketMainRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final SlaRuleRepository slaRuleRepository;
    private final SysUserRepository sysUserRepository;
    private final TicketEvaluateRepository ticketEvaluateRepository;
    private final TicketLogRepository ticketLogRepository;
    private final TicketNoGenerator ticketNoGenerator;

    @Transactional
    public TicketMain createTicket(TicketCreateRequest request, Long reporterId) {
        SysUser reporter = sysUserRepository.findByIdAndDeleted(reporterId, 0)
                .orElseThrow(() -> BusinessException.of(404, "用户不存在"));
        
        TicketType ticketType = ticketTypeRepository.findByIdAndDeleted(request.getTypeId(), 0)
                .orElseThrow(() -> BusinessException.of(404, "工单类型不存在"));
        
        SlaRule slaRule = slaRuleRepository.findByIdAndDeleted(1L, 0)
                .orElseThrow(() -> BusinessException.of(404, "SLA规则不存在"));
        
        int priority = request.getUrgency() * request.getImpactScope();
        
        TicketMain ticket = new TicketMain();
        ticket.setTicketNo(ticketNoGenerator.generateTicketNo());
        ticket.setTicketType(ticketType);
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setReporter(reporter);
        ticket.setSlaRule(slaRule);
        ticket.setUrgency(request.getUrgency());
        ticket.setImpactScope(request.getImpactScope());
        ticket.setPriority(priority);
        ticket.setStatus(TicketStatus.PENDING.name());
        
        TicketMain savedTicket = ticketMainRepository.save(ticket);
        
        saveAuditLog(savedTicket.getId(), savedTicket.getTicketNo(), reporterId, reporter.getRealName(), "CREATE", "创建工单");
        
        log.info("工单创建成功: {}", savedTicket.getTicketNo());
        
        return savedTicket;
    }

    public TicketDetailResponse getTicketDetail(Long ticketId) {
        TicketMain ticket = ticketMainRepository.findByIdAndDeleted(ticketId, 0)
                .orElseThrow(() -> BusinessException.of(404, "工单不存在"));
        
        return convertToDetailResponse(ticket);
    }

    public Page<TicketDetailResponse> searchTickets(TicketQueryRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPageNum() - 1, 
                request.getPageSize(), 
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        
        if (request.getStartTime() != null && !request.getStartTime().isEmpty()) {
            startTime = LocalDateTime.parse(request.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (request.getEndTime() != null && !request.getEndTime().isEmpty()) {
            endTime = LocalDateTime.parse(request.getEndTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        
        Page<TicketMain> ticketPage = ticketMainRepository.searchTickets(
                request.getTicketNo(),
                request.getTitle(),
                request.getTypeId(),
                request.getStatus(),
                request.getReporterId(),
                request.getAssigneeId(),
                request.getPriority(),
                startTime,
                endTime,
                pageable
        );
        
        return ticketPage.map(this::convertToDetailResponse);
    }

    @Transactional
    public TicketEvaluate evaluateTicket(Long ticketId, Long userId, Integer score, String comment) {
        TicketMain ticket = ticketMainRepository.findByIdAndDeleted(ticketId, 0)
                .orElseThrow(() -> BusinessException.of(404, "工单不存在"));
        
        if (!TicketStatus.CLOSED.name().equals(ticket.getStatus())) {
            throw BusinessException.of(400, "只有已关闭的工单才能评价");
        }
        
        if (!ticket.getReporter().getId().equals(userId)) {
            throw BusinessException.of(403, "只有工单创建人才能评价");
        }
        
        TicketEvaluate existing = ticketEvaluateRepository.findByTicketId(ticketId).orElse(null);
        if (existing != null) {
            throw BusinessException.of(400, "该工单已评价过");
        }
        
        TicketEvaluate evaluate = new TicketEvaluate();
        evaluate.setTicket(ticket);
        evaluate.setScore(score);
        evaluate.setComment(comment);
        
        TicketEvaluate saved = ticketEvaluateRepository.save(evaluate);
        
        SysUser operator = sysUserRepository.findByIdAndDeleted(userId, 0).orElse(null);
        saveAuditLog(ticketId, ticket.getTicketNo(), userId, operator != null ? operator.getRealName() : "unknown", "EVALUATE", 
                "评价工单，评分: " + score + (comment != null ? ", 评价: " + comment : ""));
        
        log.info("工单 {} 评价成功", ticket.getTicketNo());
        
        return saved;
    }

    public List<TicketLog> getTicketLogs(Long ticketId) {
        return ticketLogRepository.findByTicketIdOrderByCreatedAtDesc(ticketId);
    }

    private TicketDetailResponse convertToDetailResponse(TicketMain ticket) {
        TicketDetailResponse response = new TicketDetailResponse();
        response.setId(ticket.getId());
        response.setTicketNo(ticket.getTicketNo());
        response.setTypeId(ticket.getTicketType().getId());
        response.setTypeName(ticket.getTicketType().getTypeName());
        response.setTitle(ticket.getTitle());
        response.setDescription(ticket.getDescription());
        response.setReporterId(ticket.getReporter().getId());
        response.setReporterName(ticket.getReporter().getRealName());
        
        if (ticket.getAssignee() != null) {
            response.setAssigneeId(ticket.getAssignee().getId());
            response.setAssigneeName(ticket.getAssignee().getRealName());
        }
        
        response.setUrgency(ticket.getUrgency());
        response.setImpactScope(ticket.getImpactScope());
        response.setPriority(ticket.getPriority());
        response.setStatus(ticket.getStatus());
        response.setStatusDesc(TicketStatus.valueOf(ticket.getStatus()).getDescription());
        response.setCreatedAt(ticket.getCreatedAt());
        response.setUpdatedAt(ticket.getUpdatedAt());
        response.setSlaStartTime(ticket.getSlaStartTime());
        response.setResolvedAt(ticket.getResolvedAt());
        response.setClosedAt(ticket.getClosedAt());
        
        return response;
    }

    private void saveAuditLog(Long ticketId, String ticketNo, Long operatorId, String operatorName, String operationType, String operationDetail) {
        TicketLog logEntry = new TicketLog();
        logEntry.setTicketId(ticketId);
        logEntry.setTicketNo(ticketNo);
        logEntry.setOperatorId(operatorId);
        logEntry.setOperatorName(operatorName);
        logEntry.setOperationType(operationType);
        logEntry.setOperationDetail(operationDetail);
        ticketLogRepository.save(logEntry);
    }
}