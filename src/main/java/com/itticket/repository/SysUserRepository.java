package com.itticket.repository;

import com.itticket.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long> {
    
    Optional<SysUser> findByUsername(String username);
    
    Optional<SysUser> findByUsernameAndDeleted(String username, Integer deleted);
    
    List<SysUser> findByRoleRoleCodeAndDeleted(String roleCode, Integer deleted);
    
    List<SysUser> findByDeleted(Integer deleted);
    
    Optional<SysUser> findByIdAndDeleted(Long id, Integer deleted);
    
    @Query("SELECT u FROM SysUser u WHERE u.role.roleCode IN :roleCodes AND u.deleted = 0 AND u.status = 1")
    List<SysUser> findActiveUsersByRoleCodes(@Param("roleCodes") List<String> roleCodes);
}