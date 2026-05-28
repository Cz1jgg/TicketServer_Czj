package com.itticket.common;

public enum RoleCode {
    EMPLOYEE("EMPLOYEE", "普通员工"),
    IT_SUPPORT("IT_SUPPORT", "IT支持人员"),
    SUPERVISOR("SUPERVISOR", "服务台主管"),
    ADMIN("ADMIN", "系统管理员");

    private final String code;
    private final String description;

    RoleCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}