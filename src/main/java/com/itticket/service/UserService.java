package com.itticket.service;

import com.itticket.common.BusinessException;
import com.itticket.dto.UserCreateRequest;
import com.itticket.entity.SysRole;
import com.itticket.entity.SysUser;
import com.itticket.repository.SysRoleRepository;
import com.itticket.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserRepository sysUserRepository;
    private final SysRoleRepository sysRoleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public SysUser createUser(UserCreateRequest request) {
        if (sysUserRepository.findByUsername(request.getUsername()).isPresent()) {
            throw BusinessException.of(400, "用户名已存在");
        }
        
        SysRole role = sysRoleRepository.findByIdAndDeleted(request.getRoleId(), 0)
                .orElseThrow(() -> BusinessException.of(404, "角色不存在"));
        
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(role);
        user.setStatus(1);
        
        SysUser saved = sysUserRepository.save(user);
        
        log.info("用户创建成功: {}", user.getUsername());
        
        return saved;
    }

    @Transactional
    public SysUser updateUser(Long id, UserCreateRequest request) {
        SysUser user = sysUserRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> BusinessException.of(404, "用户不存在"));
        
        if (!user.getUsername().equals(request.getUsername()) && 
            sysUserRepository.findByUsername(request.getUsername()).isPresent()) {
            throw BusinessException.of(400, "用户名已存在");
        }
        
        user.setUsername(request.getUsername());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        
        if (request.getRoleId() != null) {
            SysRole role = sysRoleRepository.findByIdAndDeleted(request.getRoleId(), 0)
                    .orElseThrow(() -> BusinessException.of(404, "角色不存在"));
            user.setRole(role);
        }
        
        SysUser saved = sysUserRepository.save(user);
        
        log.info("用户更新成功: {}", user.getUsername());
        
        return saved;
    }

    @Transactional
    public void deleteUser(Long id) {
        SysUser user = sysUserRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> BusinessException.of(404, "用户不存在"));
        
        user.setDeleted(1);
        sysUserRepository.save(user);
        
        log.info("用户删除成功: {}", user.getUsername());
    }

    public List<SysUser> getAllUsers() {
        return sysUserRepository.findByDeleted(0);
    }

    public SysUser getUserById(Long id) {
        return sysUserRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> BusinessException.of(404, "用户不存在"));
    }

    public List<SysRole> getAllRoles() {
        return sysRoleRepository.findByDeleted(0);
    }

    public SysRole getRoleById(Long id) {
        return sysRoleRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> BusinessException.of(404, "角色不存在"));
    }

    @Transactional
    public SysRole createRole(SysRole role) {
        if (sysRoleRepository.findByRoleCode(role.getRoleCode()).isPresent()) {
            throw BusinessException.of(400, "角色编码已存在");
        }
        return sysRoleRepository.save(role);
    }
}