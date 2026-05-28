package com.itticket.service;

import com.itticket.common.BusinessException;
import com.itticket.dto.LoginRequest;
import com.itticket.dto.LoginResponse;
import com.itticket.entity.SysUser;
import com.itticket.repository.SysUserRepository;
import com.itticket.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserRepository sysUserRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserRepository.findByUsernameAndDeleted(request.getUsername(), 0)
                .orElseThrow(() -> BusinessException.of(401, "用户名或密码错误"));
        
        if (user.getStatus() != 1) {
            throw BusinessException.of(401, "用户已禁用");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw BusinessException.of(401, "用户名或密码错误");
        }
        
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().getRoleCode());
        
        log.info("用户登录成功: {}", user.getUsername());
        
        return new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getRole().getRoleCode(),
                user.getRole().getRoleName()
        );
    }
    
    public SysUser getUserById(Long userId) {
        return sysUserRepository.findByIdAndDeleted(userId, 0)
                .orElseThrow(() -> BusinessException.of(404, "用户不存在"));
    }
    
    public SysUser getUserByUsername(String username) {
        return sysUserRepository.findByUsernameAndDeleted(username, 0)
                .orElseThrow(() -> BusinessException.of(404, "用户不存在"));
    }
}