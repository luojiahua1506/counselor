package com.counselor.service;

import com.counselor.dto.admin.BatchRequest;
import com.counselor.dto.admin.BatchApproveRequest;
import com.counselor.dto.admin.RejectRequest;
import com.counselor.entity.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface AdminService {
    List<Batch> getBatches();
    Batch createBatch(BatchRequest request);
    Batch updateBatch(Long id, BatchRequest request);
    void deleteBatch(Long id);
    Batch toggleBatchStatus(Long id);
    Map<String, Object> getBatchSubmissions(Long batchId, int page, int size, String status);
    Map<String, Object> getSubmissionDetail(Long submissionId);
    void approveSubmissions(Long adminId, BatchApproveRequest request);
    void rejectSubmission(Long adminId, Long submissionId, RejectRequest request);
    Page<Counselor> getCounselors(int page, int size, String keyword, String status);
    Map<String, Object> getCounselorDetail(Long counselorId);
    void approveRegistration(Long adminId, Long counselorId);
    void rejectRegistration(Long adminId, Long counselorId, RejectRequest request);
    Counselor updateCounselor(Long adminId, Long counselorId, Object request);
    void disableCounselor(Long adminId, Long counselorId);
    List<Map<String, Object>> getProfileEditRequests();
    void approveProfileEdit(Long adminId, Long requestId);
    void rejectProfileEdit(Long adminId, Long requestId, RejectRequest request);
    List<College> getColleges();
    College createCollege(String name);
    College updateCollege(Long id, String name);
    void deleteCollege(Long id);
    byte[] exportCounselors();
    byte[] exportSubmissions(Long batchId);
    Map<String, Object> getLogs(int page, int size);
    Map<String,Object> getBatchProgress(Long batchId);
    List<Map<String,Object>> getUnsubmitted(Long batchId,String keyword,Long collegeId);
    int remindUnsubmitted(Long adminId,Long batchId,List<Long> counselorIds);
    byte[] exportUnsubmitted(Long batchId);
    String resetCounselorPassword(Long adminId,Long counselorId);
    void changeAdminPassword(Long adminId,com.counselor.dto.counselor.ChangePasswordRequest request);
}
