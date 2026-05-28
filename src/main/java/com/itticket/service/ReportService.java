package com.itticket.service;

import com.itticket.entity.*;
import com.itticket.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final TicketMainRepository ticketMainRepository;
    private final TicketEvaluateRepository ticketEvaluateRepository;

    public Map<String, Object> getDailyReport(LocalDateTime date) {
        Map<String, Object> report = new HashMap<>();
        
        Pageable pageable = PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        List<TicketMain> tickets = ticketMainRepository.findByDeleted(0, pageable).getContent();
        
        List<TicketMain> dayTickets = tickets.stream()
                .filter(t -> t.getCreatedAt().toLocalDate().equals(date.toLocalDate()))
                .collect(Collectors.toList());
        
        long totalCount = dayTickets.size();
        
        long pendingCount = dayTickets.stream()
                .filter(t -> "PENDING".equals(t.getStatus()))
                .count();
        
        long processingCount = dayTickets.stream()
                .filter(t -> "PROCESSING".equals(t.getStatus()))
                .count();
        
        long closedCount = dayTickets.stream()
                .filter(t -> "CLOSED".equals(t.getStatus()))
                .count();
        
        report.put("date", date.toLocalDate());
        report.put("totalCount", totalCount);
        report.put("pendingCount", pendingCount);
        report.put("processingCount", processingCount);
        report.put("closedCount", closedCount);
        
        return report;
    }

    public Map<String, Object> getTeamReport(String roleCode, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> report = new HashMap<>();
        
        Pageable pageable = PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        List<TicketMain> tickets = ticketMainRepository.findByDeleted(0, pageable).getContent();
        
        List<TicketMain> periodTickets = tickets.stream()
                .filter(t -> !t.getCreatedAt().isBefore(startDate) && !t.getCreatedAt().isAfter(endDate))
                .collect(Collectors.toList());
        
        Map<Long, List<TicketMain>> ticketsByAssignee = periodTickets.stream()
                .filter(t -> t.getAssignee() != null)
                .collect(Collectors.groupingBy(t -> t.getAssignee().getId()));
        
        Map<String, Object> assigneeStats = new HashMap<>();
        
        for (Map.Entry<Long, List<TicketMain>> entry : ticketsByAssignee.entrySet()) {
            List<TicketMain> assigneeTickets = entry.getValue();
            long total = assigneeTickets.size();
            long resolved = assigneeTickets.stream()
                    .filter(t -> "CLOSED".equals(t.getStatus()) || "CONFIRMING".equals(t.getStatus()))
                    .count();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", total);
            stats.put("resolved", resolved);
            stats.put("rate", total > 0 ? (double) resolved / total * 100 : 0);
            
            assigneeStats.put(assigneeTickets.get(0).getAssignee().getRealName(), stats);
        }
        
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalTickets", periodTickets.size());
        report.put("assigneeStats", assigneeStats);
        
        return report;
    }

    public byte[] exportTicketsToExcel(List<Long> ticketIds) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("工单列表");
        
        Row headerRow = sheet.createRow(0);
        String[] headers = {"工单编号", "类型", "标题", "状态", "优先级", "创建人", "处理人", "创建时间", "关闭时间"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        final int[] rowNum = {1};
        for (Long ticketId : ticketIds) {
            ticketMainRepository.findByIdAndDeleted(ticketId, 0).ifPresent(ticket -> {
                Row row = sheet.createRow(rowNum[0]++);
                
                row.createCell(0).setCellValue(ticket.getTicketNo());
                row.createCell(1).setCellValue(ticket.getTicketType().getTypeName());
                row.createCell(2).setCellValue(ticket.getTitle());
                row.createCell(3).setCellValue(ticket.getStatus());
                row.createCell(4).setCellValue(ticket.getPriority());
                row.createCell(5).setCellValue(ticket.getReporter().getRealName());
                row.createCell(6).setCellValue(ticket.getAssignee() != null ? ticket.getAssignee().getRealName() : "");
                row.createCell(7).setCellValue(ticket.getCreatedAt() != null ? ticket.getCreatedAt().toString() : "");
                row.createCell(8).setCellValue(ticket.getClosedAt() != null ? ticket.getClosedAt().toString() : "");
            });
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }

    public Map<String, Object> getEvaluateReport() {
        Map<String, Object> report = new HashMap<>();
        
        List<TicketEvaluate> evaluates = ticketEvaluateRepository.findAll();
        
        long total = evaluates.size();
        double avgScore = evaluates.stream()
                .filter(e -> e.getScore() != null)
                .mapToInt(TicketEvaluate::getScore)
                .average()
                .orElse(0);
        
        Map<Integer, Long> scoreDistribution = evaluates.stream()
                .filter(e -> e.getScore() != null)
                .collect(Collectors.groupingBy(TicketEvaluate::getScore, Collectors.counting()));
        
        report.put("totalEvaluates", total);
        report.put("avgScore", avgScore);
        report.put("scoreDistribution", scoreDistribution);
        
        return report;
    }
}