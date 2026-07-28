package com.counselor.controller;

import com.counselor.dto.psych.PsychAlertUpdateRequest;
import com.counselor.dto.psych.PsychBatchRequest;
import com.counselor.enums.PsychBatchStatus;
import com.counselor.security.JwtUtil;
import com.counselor.service.PsychAssessmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/psych")
@RequiredArgsConstructor
public class PsychAdminController {
    private final PsychAssessmentService service;
    private final JwtUtil jwtUtil;
    private Long userId(HttpServletRequest request) { return jwtUtil.getUserIdFromToken(request.getHeader("Authorization").substring(7)); }

    @GetMapping("/batches") public ResponseEntity<?> batches() { return ResponseEntity.ok(service.getAdminBatches()); }
    @PostMapping("/batches") public ResponseEntity<?> create(HttpServletRequest request, @Valid @RequestBody PsychBatchRequest body) { return ResponseEntity.ok(service.createBatch(userId(request), body)); }
    @PutMapping("/batches/{id}") public ResponseEntity<?> update(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody PsychBatchRequest body) { return ResponseEntity.ok(service.updateBatch(userId(request), id, body)); }
    @PutMapping("/batches/{id}/status") public ResponseEntity<?> status(HttpServletRequest request, @PathVariable Long id, @RequestParam PsychBatchStatus status) { return ResponseEntity.ok(service.changeBatchStatus(userId(request), id, status)); }
    @GetMapping("/dashboard") public ResponseEntity<?> dashboard() { return ResponseEntity.ok(service.getDashboard()); }
    @GetMapping("/alerts") public ResponseEntity<?> alerts(HttpServletRequest request) { return ResponseEntity.ok(service.getAlerts(userId(request))); }
    @GetMapping("/alerts/{id}") public ResponseEntity<?> alert(HttpServletRequest request, @PathVariable Long id) { return ResponseEntity.ok(service.getAlert(userId(request), id)); }
    @PutMapping("/alerts/{id}") public ResponseEntity<?> updateAlert(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody PsychAlertUpdateRequest body) { return ResponseEntity.ok(service.updateAlert(userId(request), id, body)); }
}
