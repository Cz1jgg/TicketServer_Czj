package com.itticket.controller;

import com.itticket.common.Result;
import com.itticket.dto.TicketProcessRequest;
import com.itticket.dto.UserCreateRequest;
import com.itticket.entity.SysRole;
import com.itticket.entity.SysUser;
import com.itticket.entity.TicketMain;
import com.itticket.dto.TicketDetailResponse;
import com.itticket.service.TicketService;
import com.itticket.service.TicketStatusService;
import com.itticket.service.UserService;
import com.itticket.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "管理员接口", description = "管理员专属接口")
public class AdminController {

    private final UserService userService;
    private final TicketStatusService ticketStatusService;
    private final TicketService ticketService;
    private final JwtUtil jwtUtil;

    @PostMapping("/users")
    @Operation(summary = "创建用户", description = "管理员创建新用户")
    public ResponseEntity<Result<SysUser>> createUser(@Valid @RequestBody UserCreateRequest request) {
        SysUser user = userService.createUser(request);
        return ResponseEntity.ok(Result.success(user));
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "更新用户", description = "管理员更新用户信息")
    public ResponseEntity<Result<SysUser>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserCreateRequest request) {
        SysUser user = userService.updateUser(id, request);
        return ResponseEntity.ok(Result.success(user));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "删除用户", description = "管理员删除用户")
    public ResponseEntity<Result<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Result.success());
    }

    @GetMapping("/users")
    @Operation(summary = "获取用户列表", description = "管理员获取所有用户")
    public ResponseEntity<Result<List<SysUser>>> getAllUsers() {
        List<SysUser> users = userService.getAllUsers();
        return ResponseEntity.ok(Result.success(users));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "获取用户详情", description = "管理员获取用户详情")
    public ResponseEntity<Result<SysUser>> getUserById(@PathVariable Long id) {
        SysUser user = userService.getUserById(id);
        return ResponseEntity.ok(Result.success(user));
    }

    @GetMapping("/roles")
    @Operation(summary = "获取角色列表", description = "管理员获取所有角色")
    public ResponseEntity<Result<List<SysRole>>> getAllRoles() {
        List<SysRole> roles = userService.getAllRoles();
        return ResponseEntity.ok(Result.success(roles));
    }

    @PostMapping("/roles")
    @Operation(summary = "创建角色", description = "管理员创建角色")
    public ResponseEntity<Result<SysRole>> createRole(@RequestBody SysRole role) {
        SysRole saved = userService.createRole(role);
        return ResponseEntity.ok(Result.success(saved));
    }

    @PostMapping("/tickets/{id}/force-close")
    @Operation(summary = "强制关闭工单", description = "管理员强制关闭工单")
    public ResponseEntity<Result<TicketDetailResponse>> forceCloseTicket(
            @PathVariable Long id,
            @RequestBody TicketProcessRequest request,
            @RequestHeader("Authorization") String token) {
        
        Long userId = jwtUtil.getUserIdFromToken(token.substring(7));
        String remark = request != null ? request.getRemark() : null;
        TicketMain ticket = ticketStatusService.forceCloseTicket(id, userId, remark);
        TicketDetailResponse response = ticketService.getTicketDetail(ticket.getId());
        
        return ResponseEntity.ok(Result.success(response));
    }
}