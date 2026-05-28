package com.itticket.common;

public enum TicketStatus {
    PENDING("待分配"),
    PROCESSING("处理中"),
    SUSPENDED("挂起"),
    CONFIRMING("待确认"),
    TRANSFERRED("已转交"),
    CANCELLED("已取消"),
    CLOSED("已关闭");

    private final String description;

    TicketStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}