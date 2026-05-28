package com.itticket.service;

import com.itticket.common.BusinessException;
import com.itticket.common.TicketStatus;
import com.itticket.common.RoleCode;
import com.itticket.dto.TicketDetailResponse;
import com.itticket.entity.*;
import com.itticket.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketStatusService {

    private final TicketMainRepository ticketMainRepository;
    private final TicketProcessRepository ticketProcessRepository;
    private final TicketLogRepository ticketLogRepository;
    private final SysUserRepository sysUserRepository;

    private static final Set<String> ALLOWED_PENDING_TRANSITIONS = Set.of("PROCESSING", "CANCELLED");
    private static final Set<String> ALLOWED_PROCESSING_TRANSITIONS = Set.of("SUSPENDED", "CONFIRMING", "TRANSFERRED", "CLOSED");
    private static final Set<String> ALLOWED_SUSPENDED_TRANSITIONS = Set.of("PROCESSING");
    private static final Set<String> ALLOWED_CONFIRMING_TRANSITIONS = Set.of("PROCESSING", "CLOSED");
    private static final Set<String> ALLOWED_TRANSFERRED_TRANSITIONS = Set.of("PROCESSING");

    @Transactional
    public TicketMain claimTicket(Long ticketId, Long userId) {
        TicketMain ticket = ticketMainRepository.findByIdAndDeleted(ticketId, 0)
                .orElseThrow(() -> BusinessException.of(404, "工单不存在"));
        
        if (!TicketStatus.PENDING.name().equals(ticket.getStatus())) {
            throw BusinessException.of(400, "只有待分配状态的工单才能被认领");
        }
        
        SysUser user = sysUserRepository.findByIdAndDeleted(userId, 0)
                .orElseThrow(() -> BusinessException.of(404, "用户不存在"));
        
        String roleCode = user.getRole().getRoleCode();
        if (!RoleCode.IT_SUPPORT.getCode().equals(roleCode)) {
            throw BusinessException.of(403, "只有IT支持人员才能认领工单");
        }
        
        ticket.setAssignee(user);
        ticket.setStatus(TicketStatus.PROCESSING.name());
        ticket.setSlaStartTime(LocalDateTime.now());
        
        TicketMain savedTicket = ticketMainRepository.save(ticket);
        
        saveProcessLog(ticket, "CLAIM", TicketStatus.PENDING.name(), TicketStatus.PROCESSING.name(), user, null);
        saveAuditLog(ticketId, ticket.getTicketNo(), userId, user.getRealName(), "CLAIM", "工单认领");
        
        log.info("工单 {} 被用户 {} 认领", ticket.getTicketNo(), user.getUsername());
        
        return savedTicket;
    }

    @Transactional
    public TicketMain assignTicket(Long ticketId, Long assigneeId, Long operatorId, String remark) {
        TicketMain ticket = ticketMainRepository.findByIdAndDeleted(ticketId, 0)
                .orElseThrow(() -> BusinessException.of(404, "工单不存在"));
        
        if (!TicketStatus.PENDING.name().equals(ticket.getStatus())) {
            throw BusinessException.of(400, "只有待分配状态的工单才能被分配");
        }
        
        SysUser assignee = sysUserRepository.findByIdAndDeleted(assigneeId, 0)
                .orElseThrow(() -> BusinessException.of(404, "分配用户不存在"));
        
        String assigneeRole = assignee.getRole().getRoleCode();
        if (!RoleCode.IT_SUPPORT.getCode().equals(assigneeRole)) {
            throw BusinessException.of(400, "只能分配给IT支持人员");
        }
        
        SysUser operator = sysUserRepository.findByIdAndDeleted(operatorId, 0)
                .orElseThrow(() -> BusinessException.of(404, "操作人不存在"));
        
        ticket.setAssignee(assignee);
        ticket.setStatus(TicketStatus.PROCESSING.name());
        ticket.setSlaStartTime(LocalDateTime.now());
        
        TicketMain savedTicket = ticketMainRepository.save(ticket);
        
        saveProcessLog(ticket, "ASSIGN", TicketStatus.PENDING.name(), TicketStatus.PROCESSING.name(), operator, remark);
        saveAuditLog(ticketId, ticket.getTicketNo(), operatorId, operator.getRealName(), "ASSIGN", "工单分配给: " + assignee.getRealName());
        
        log.info("工单 {} 被分配给用户 {}", ticket.getTicketNo(), assignee.getUsername());
        
        return savedTicket;
    }

    @Transactional
    public TicketMain cancelTicket(Long ticketId, Long userId, String remark) {
        TicketMain ticket = ticketMainRepository.findByIdAndDeleted(ticketId, 0)
                .orElseThrow(() -> BusinessException.of(404, "工单不存在"));
        
        if (!TicketStatus.PENDING.name().equals(ticket.getStatus())) {
            throw BusinessException.of(400, "只有待分配状态的工单才能被取消");
        }
        
        if (!ticket.getReporter().getId().equals(userId)) {
            throw BusinessException.of(403, "只能取消自己创建的工单");
        }
        
        ticket.setStatus(TicketStatus.CANCELLED.name());
        
        TicketMain savedTicket = ticketMainRepository.save(ticket);
        
        SysUser operator = sysUserRepository.findByIdAndDeleted(userId, 0).orElse(null);
        
        saveProcessLog(ticket, "CANCEL", TicketStatus.PENDING.name(), TicketStatus.CANCELLED.name(), operator, remark);
        saveAuditLog(ticketId, ticket.getTicketNo(), userId, operator != null ? operator.getRealName() : "unknown", "CANCEL", "工单取消");
        
        log.info("工单 {} 被取消", ticket.getTicketNo());
        
        return savedTicket;
    }

    @Transactional
    public TicketMain suspendTicket(Long ticketId, Long userId, String remark) {
        TicketMain ticket = ticketMainRepository.findByIdAndDeleted(ticketId, 0)
                .orElseThrow(() -> BusinessException.of(404, "工单不存在"));
        
        if (!TicketStatus.PROCESSING.name().equals(ticket.getStatus())) {
            throw BusinessException.of(400, "只有处理中的工单才能被挂起");
        }
        
        if (!ticket.getAssignee().getId().equals(userId)) {
            throw BusinessException.of(403, "只能挂起自己负责的工单");
        }
        
        ticket.setStatus(TicketStatus.SUSPENDED.name());
        ticket.setSlaPauseTime(LocalDateTime.now());
        
        TicketMain savedTicket = ticketMainRepository.save(ticket);
        
        SysUser operator = sysUserRepository.findByIdAndDeleted(userId, 0).orElse(null);
        
        saveProcessLog(ticket, "SUSPEND", TicketStatus.PROCESSING.name(), TicketStatus.SUSPENDED.name(), operator, remark);
        saveAuditLog(ticketId, ticket.getTicketNo(), userId, operator != null ? operator.getRealName() : "unknown", "SUSPEND", "工单挂起");
        
        log.info("工单 {} 被挂起", ticket.getTicketNo());
        
        return savedTicket;
    }

    @Transactional
    public TicketMain resumeTicket(Long ticketId, Long userId, String remark) {
        TicketMain ticket = ticketMainRepository.findByIdAndDeleted(ticketId, 0)
                .orElseThrow(() -> BusinessException.of(404, "工单不存在"));
        
        if (!TicketStatus.SUSPENDED.name().equals(ticket.getStatus())) {
            throw BusinessException.of(400, "只有挂起状态的工单才能被恢复");
        }
        
        if (!ticket.getAssignee().getId().equals(userId)) {
            throw BusinessException.of(403, "只能恢复自己负责的工单");
        }
        
        LocalDateTime pauseTime = ticket.getSlaPauseTime();
        if (pauseTime != null) {
            long pauseDuration = java.time.Duration.between(pauseTime, LocalDateTime.now()).toMillis();
            ticket.setSlaPauseDuration(ticket.getSlaPauseDuration() + pauseDuration);
        }
        
        ticket.setStatus(TicketStatus.PROCESSING.name());
        ticket.setSlaPauseTime(null);
        
        TicketMain savedTicket = ticketMainRepository.save(ticket);
        
        SysUser operator = sysUserRepository.findByIdAndDeleted(userId, 0).orElse(null);
        
        saveProcessLog(ticket, "RESUME", TicketStatus.SUSPENDED.name(), TicketStatus.PROCESSING.name(), operator, remark);
        saveAuditLog(ticketId, ticket.getTicketNo(), userId, operator != null ? operator.getRealName() : "unknown", "RESUME", "工单恢复");
        
        log.info("工单 {} 被恢复", ticket.getTicketNo());
        
        return savedTicket;
    }

    @Transactional
    public TicketMain completeTicket(Long ticketId, Long userId, String remark) {
        TicketMain ticket = ticketMainRepository.findByIdAndDeleted(ticketId, 0)
                .orElseThrow(() -> BusinessException.of(404, "工单不存在"));
        
        if (!TicketStatus.PROCESSING.name().equals(ticket.getStatus())) {
            throw BusinessException.of(400, "只有处理中的工单才能提交完成");
        }
        
        if (!ticket.getAssignee().getId().equals(userId)) {
            throw BusinessException.of(403, "只能处理自己负责的工单");
        }
        
        ticket.setStatus(TicketStatus.CONFIRMING.name());
        ticket.setResolvedAt(LocalDateTime.now());
        
        TicketMain savedTicket = ticketMainRepository.save(ticket);
        
        SysUser operator = sysUserRepository.findByIdAndDeleted(userId, 0).orElse(null);
        
        saveProcessLog(ticket, "COMPLETE", TicketStatus.PROCESSING.name(), TicketStatus.CONFIRMING.name(), operator, remark);
        saveAuditLog(ticketId, ticket.getTicketNo(), userId, operator != null ? operator.getRealName() : "unknown", "COMPLETE", "工单提交完成");
        
        log.info("工单 {} 提交完成，等待用户确认", ticket.getTicketNo());
        
        return savedTicket;
    }

    @Transactional
    public TicketMain rejectTicket(Long ticketId, Long userId, String remark) {
        TicketMain ticket = ticketMainRepository.findByIdAndDeleted(ticketId, 0)
                .orElseThrow(() -> BusinessException.of(404, "工单不存在"));
        
        if (!TicketStatus.CONFIRMING.name().equals(ticket.getStatus())) {
            throw BusinessException.of(400, "只有待确认状态的工单才能被驳回");
        }
        
        if (!ticket.getReporter().getId().equals(userId)) {
            throw BusinessException.of(403, "只有工单创建人才能驳回");
        }
        
        ticket.setStatus(TicketStatus.PROCESSING.name());
        ticket.setResolvedAt(null);
        
        TicketMain savedTicket = ticketMainRepository.save(ticket);
        
        SysUser operator = sysUserRepository.findByIdAndDeleted(userId, 0).orElse(null);
        
        saveProcessLog(ticket, "REJECT", TicketStatus.CONFIRMING.name(), TicketStatus.PROCESSING.name(), operator, remark);
        saveAuditLog(ticketId, ticket.getTicketNo(), userId, operator != null ? operator.getRealName() : "unknown", "REJECT", "工单被驳回: " + remark);
        
        log.info("工单 {} 被驳回", ticket.getTicketNo());
        
        return savedTicket;
    }

    @Transactional
    public TicketMain closeTicket(Long ticketId, Long userId) {
        TicketMain ticket = ticketMainRepository.findByIdAndDeleted(ticketId, 0)
                .orElseThrow(() -> BusinessException.of(404, "工单不存在"));
        
        if (!TicketStatus.CONFIRMING.name().equals(ticket.getStatus())) {
            throw BusinessException.of(400, "只有待确认状态的工单才能被关闭");
        }
        
        if (!ticket.getReporter().getId().equals(userId)) {
            throw BusinessException.of(403, "只有工单创建人才能关闭工单");
        }
        
        ticket.setStatus(TicketStatus.CLOSED.name());
        ticket.setClosedAt(LocalDateTime.now());
        
        TicketMain savedTicket = ticketMainRepository.save(ticket);
        
        SysUser operator = sysUserRepository.findByIdAndDeleted(userId, 0).orElse(null);
        
        saveProcessLog(ticket, "CLOSE", TicketStatus.CONFIRMING.name(), TicketStatus.CLOSED.name(), operator, null);
        saveAuditLog(ticketId, ticket.getTicketNo(), userId, operator != null ? operator.getRealName() : "unknown", "CLOSE", "工单关闭");
        
        log.info("工单 {} 被关闭", ticket.getTicketNo());
        
        return savedTicket;
    }

    @Transactional
    public TicketMain forceCloseTicket(Long ticketId, Long userId, String remark) {
        TicketMain ticket = ticketMainRepository.findByIdAndDeleted(ticketId, 0)
                .orElseThrow(() -> BusinessException.of(404, "工单不存在"));
        
        if (TicketStatus.CLOSED.name().equals(ticket.getStatus()) || 
            TicketStatus.CANCELLED.name().equals(ticket.getStatus())) {
            throw BusinessException.of(400, "工单已处于关闭或取消状态");
        }
        
        SysUser operator = sysUserRepository.findByIdAndDeleted(userId, 0)
                .orElseThrow(() -> BusinessException.of(404, "操作人不存在"));
        
        String roleCode = operator.getRole().getRoleCode();
        if (!RoleCode.ADMIN.getCode().equals(roleCode)) {
            throw BusinessException.of(403, "只有管理员才能强制关闭工单");
        }
        
        ticket.setStatus(TicketStatus.CLOSED.name());
        ticket.setClosedAt(LocalDateTime.now());
        
        TicketMain savedTicket = ticketMainRepository.save(ticket);
        
        saveProcessLog(ticket, "FORCE_CLOSE", ticket.getStatus(), TicketStatus.CLOSED.name(), operator, remark);
        saveAuditLog(ticketId, ticket.getTicketNo(), userId, operator.getRealName(), "FORCE_CLOSE", "管理员强制关闭工单: " + remark);
        
        log.info("工单 {} 被管理员强制关闭", ticket.getTicketNo());
        
        return savedTicket;
    }

    private void saveProcessLog(TicketMain ticket, String action, String fromStatus, String toStatus, SysUser operator, String remark) {
        TicketProcess process = new TicketProcess();
        process.setTicket(ticket);
        process.setAction(action);
        process.setFromStatus(fromStatus);
        process.setToStatus(toStatus);
        process.setOperator(operator);
        process.setRemark(remark);
        ticketProcessRepository.save(process);
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
    
    public boolean isValidTransition(String fromStatus, String toStatus) {
        return switch (fromStatus) {
            case "PENDING" -> ALLOWED_PENDING_TRANSITIONS.contains(toStatus);
            case "PROCESSING" -> ALLOWED_PROCESSING_TRANSITIONS.contains(toStatus);
            case "SUSPENDED" -> ALLOWED_SUSPENDED_TRANSITIONS.contains(toStatus);
            case "CONFIRMING" -> ALLOWED_CONFIRMING_TRANSITIONS.contains(toStatus);
            case "TRANSFERRED" -> ALLOWED_TRANSFERRED_TRANSITIONS.contains(toStatus);
            default -> false;
        };
    }
}