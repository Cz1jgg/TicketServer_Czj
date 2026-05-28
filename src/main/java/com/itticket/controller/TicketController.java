package com.itticket.controller;

import com.itticket.common.Result;
import com.itticket.dto.*;
import com.itticket.entity.TicketMain;
import com.itticket.entity.TicketLog;
import com.itticket.service.TicketService;
import com.itticket.service.TicketStatusService;
import com.itticket.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "工单接口", description = "工单管理相关接口")
public class TicketController {

    private final TicketService ticketService;
    private final TicketStatusService ticketStatusService;
    private final JwtUtil jwtUtil;

    @PostMapping
    @Operation(summary = "创建工单", description = "员工创建新工单")
    public ResponseEntity<Result<TicketDetailResponse>> createTicket(
            @Valid @RequestBody TicketCreateRequest request,
            @RequestHeader("Authorization") String token) {
        
        Long userId = jwtUtil.getUserIdFromToken(token.substring(7));
        TicketMain ticket = ticketService.createTicket(request, userId);
        TicketDetailResponse response = ticketService.getTicketDetail(ticket.getId());
        
        return ResponseEntity.ok(Result.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取工单详情", description = "根据工单ID获取工单详情")
    public ResponseEntity<Result<TicketDetailResponse>> getTicketDetail(@PathVariable Long id) {
        TicketDetailResponse response = ticketService.getTicketDetail(id);
        return ResponseEntity.ok(Result.success(response));
    }

    @GetMapping
    @Operation(summary = "查询工单列表", description = "多条件分页查询工单")
    public ResponseEntity<Result<Page<TicketDetailResponse>>> searchTickets(TicketQueryRequest request) {
        Page<TicketDetailResponse> response = ticketService.searchTickets(request);
        return ResponseEntity.ok(Result.success(response));
    }

    @PostMapping("/{id}/claim")
    @Operation(summary = "认领工单", description = "IT支持人员认领待分配工单")
    public ResponseEntity<Result<TicketDetailResponse>> claimTicket(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        
        Long userId = jwtUtil.getUserIdFromToken(token.substring(7));
        TicketMain ticket = ticketStatusService.claimTicket(id, userId);
        TicketDetailResponse response = ticketService.getTicketDetail(ticket.getId());
        
        return ResponseEntity.ok(Result.success(response));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消工单", description = "员工取消待分配工单")
    public ResponseEntity<Result<TicketDetailResponse>> cancelTicket(
            @PathVariable Long id,
            @RequestBody(required = false) TicketProcessRequest request,
            @RequestHeader("Authorization") String token) {
        
        Long userId = jwtUtil.getUserIdFromToken(token.substring(7));
        String remark = request != null ? request.getRemark() : null;
        TicketMain ticket = ticketStatusService.cancelTicket(id, userId, remark);
        TicketDetailResponse response = ticketService.getTicketDetail(ticket.getId());
        
        return ResponseEntity.ok(Result.success(response));
    }

    @PostMapping("/{id}/suspend")
    @Operation(summary = "挂起工单", description = "IT支持人员挂起处理中工单")
    public ResponseEntity<Result<TicketDetailResponse>> suspendTicket(
            @PathVariable Long id,
            @RequestBody(required = false) TicketProcessRequest request,
            @RequestHeader("Authorization") String token) {
        
        Long userId = jwtUtil.getUserIdFromToken(token.substring(7));
        String remark = request != null ? request.getRemark() : null;
        TicketMain ticket = ticketStatusService.suspendTicket(id, userId, remark);
        TicketDetailResponse response = ticketService.getTicketDetail(ticket.getId());
        
        return ResponseEntity.ok(Result.success(response));
    }

    @PostMapping("/{id}/resume")
    @Operation(summary = "恢复工单", description = "IT支持人员恢复挂起工单")
    public ResponseEntity<Result<TicketDetailResponse>> resumeTicket(
            @PathVariable Long id,
            @RequestBody(required = false) TicketProcessRequest request,
            @RequestHeader("Authorization") String token) {
        
        Long userId = jwtUtil.getUserIdFromToken(token.substring(7));
        String remark = request != null ? request.getRemark() : null;
        TicketMain ticket = ticketStatusService.resumeTicket(id, userId, remark);
        TicketDetailResponse response = ticketService.getTicketDetail(ticket.getId());
        
        return ResponseEntity.ok(Result.success(response));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "提交完成", description = "IT支持人员提交工单处理结果")
    public ResponseEntity<Result<TicketDetailResponse>> completeTicket(
            @PathVariable Long id,
            @RequestBody(required = false) TicketProcessRequest request,
            @RequestHeader("Authorization") String token) {
        
        Long userId = jwtUtil.getUserIdFromToken(token.substring(7));
        String remark = request != null ? request.getRemark() : null;
        TicketMain ticket = ticketStatusService.completeTicket(id, userId, remark);
        TicketDetailResponse response = ticketService.getTicketDetail(ticket.getId());
        
        return ResponseEntity.ok(Result.success(response));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "驳回工单", description = "员工驳回待确认工单")
    public ResponseEntity<Result<TicketDetailResponse>> rejectTicket(
            @PathVariable Long id,
            @RequestBody(required = false) TicketProcessRequest request,
            @RequestHeader("Authorization") String token) {
        
        Long userId = jwtUtil.getUserIdFromToken(token.substring(7));
        String remark = request != null ? request.getRemark() : null;
        TicketMain ticket = ticketStatusService.rejectTicket(id, userId, remark);
        TicketDetailResponse response = ticketService.getTicketDetail(ticket.getId());
        
        return ResponseEntity.ok(Result.success(response));
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "关闭工单", description = "员工确认关闭工单")
    public ResponseEntity<Result<TicketDetailResponse>> closeTicket(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        
        Long userId = jwtUtil.getUserIdFromToken(token.substring(7));
        TicketMain ticket = ticketStatusService.closeTicket(id, userId);
        TicketDetailResponse response = ticketService.getTicketDetail(ticket.getId());
        
        return ResponseEntity.ok(Result.success(response));
    }

    @PostMapping("/{id}/evaluate")
    @Operation(summary = "工单评价", description = "员工评价已关闭工单")
    public ResponseEntity<Result<Void>> evaluateTicket(
            @PathVariable Long id,
            @Valid @RequestBody TicketEvaluateRequest request,
            @RequestHeader("Authorization") String token) {
        
        Long userId = jwtUtil.getUserIdFromToken(token.substring(7));
        ticketService.evaluateTicket(id, userId, request.getScore(), request.getComment());
        
        return ResponseEntity.ok(Result.success());
    }

    @GetMapping("/{id}/logs")
    @Operation(summary = "获取工单日志", description = "获取工单操作日志")
    public ResponseEntity<Result<List<TicketLog>>> getTicketLogs(@PathVariable Long id) {
        List<TicketLog> logs = ticketService.getTicketLogs(id);
        return ResponseEntity.ok(Result.success(logs));
    }
}