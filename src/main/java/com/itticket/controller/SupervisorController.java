package com.itticket.controller;

import com.itticket.common.Result;
import com.itticket.dto.TicketAssignRequest;
import com.itticket.dto.TicketDetailResponse;
import com.itticket.entity.AssignStrategy;
import com.itticket.entity.TicketMain;
import com.itticket.service.AssignStrategyService;
import com.itticket.service.TicketService;
import com.itticket.service.TicketStatusService;
import com.itticket.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supervisor")
@RequiredArgsConstructor
@Tag(name = "主管接口", description = "服务台主管专属接口")
public class SupervisorController {

    private final TicketStatusService ticketStatusService;
    private final TicketService ticketService;
    private final AssignStrategyService assignStrategyService;
    private final JwtUtil jwtUtil;

    @PostMapping("/tickets/{id}/assign")
    @Operation(summary = "手工分配工单", description = "主管手工分配工单给IT支持人员")
    public ResponseEntity<Result<TicketDetailResponse>> assignTicket(
            @PathVariable Long id,
            @Valid @RequestBody TicketAssignRequest request,
            @RequestHeader("Authorization") String token) {
        
        Long userId = jwtUtil.getUserIdFromToken(token.substring(7));
        TicketMain ticket = ticketStatusService.assignTicket(id, request.getAssigneeId(), userId, request.getRemark());
        TicketDetailResponse response = ticketService.getTicketDetail(ticket.getId());
        
        return ResponseEntity.ok(Result.success(response));
    }

    @GetMapping("/assign-strategies")
    @Operation(summary = "获取分配策略列表", description = "获取所有分配策略")
    public ResponseEntity<Result<List<AssignStrategy>>> getAssignStrategies() {
        List<AssignStrategy> strategies = assignStrategyService.getStrategies();
        return ResponseEntity.ok(Result.success(strategies));
    }

    @PostMapping("/assign-strategies")
    @Operation(summary = "创建分配策略", description = "创建新的分配策略")
    public ResponseEntity<Result<AssignStrategy>> createAssignStrategy(@RequestBody AssignStrategy strategy) {
        AssignStrategy saved = assignStrategyService.saveStrategy(strategy);
        return ResponseEntity.ok(Result.success(saved));
    }

    @PutMapping("/assign-strategies/{id}")
    @Operation(summary = "更新分配策略", description = "更新分配策略")
    public ResponseEntity<Result<AssignStrategy>> updateAssignStrategy(
            @PathVariable Long id,
            @RequestBody AssignStrategy strategy) {
        AssignStrategy updated = assignStrategyService.updateStrategy(id, strategy);
        return ResponseEntity.ok(Result.success(updated));
    }
}