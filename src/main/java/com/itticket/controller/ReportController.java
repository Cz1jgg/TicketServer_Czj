package com.itticket.controller;

import com.itticket.common.Result;
import com.itticket.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "报表接口", description = "报表相关接口")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/daily")
    @Operation(summary = "获取日报", description = "获取指定日期的工单日报")
    public ResponseEntity<Result<Map<String, Object>>> getDailyReport(
            @RequestParam(required = false) String date) {
        
        LocalDateTime dateTime = date != null 
                ? LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : LocalDateTime.now();
        
        Map<String, Object> report = reportService.getDailyReport(dateTime);
        return ResponseEntity.ok(Result.success(report));
    }

    @GetMapping("/team")
    @Operation(summary = "获取团队报表", description = "获取指定时间段的团队绩效报表")
    public ResponseEntity<Result<Map<String, Object>>> getTeamReport(
            @RequestParam String roleCode,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        LocalDateTime start = LocalDateTime.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime end = LocalDateTime.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        Map<String, Object> report = reportService.getTeamReport(roleCode, start, end);
        return ResponseEntity.ok(Result.success(report));
    }

    @GetMapping("/evaluate")
    @Operation(summary = "获取评价报表", description = "获取工单评价统计报表")
    public ResponseEntity<Result<Map<String, Object>>> getEvaluateReport() {
        Map<String, Object> report = reportService.getEvaluateReport();
        return ResponseEntity.ok(Result.success(report));
    }

    @PostMapping("/export")
    @Operation(summary = "导出工单", description = "批量导出工单到Excel")
    public ResponseEntity<byte[]> exportTickets(@RequestBody List<Long> ticketIds) throws IOException {
        byte[] excelData = reportService.exportTicketsToExcel(ticketIds);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "tickets.xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }
}