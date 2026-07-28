package com.counselor.service;

import com.counselor.dto.counselor.SubmissionRequest;
import com.counselor.dto.counselor.EditProfileRequest;
import com.counselor.dto.counselor.ChangePasswordRequest;
import com.counselor.entity.*;

import java.util.List;
import java.util.Map;

public interface CounselorService {
    Map<String, Object> getProfile(Long counselorId);
    void submitProfileEdit(Long counselorId, EditProfileRequest request);
    void changePassword(Long counselorId, ChangePasswordRequest request);
    List<Map<String, Object>> getEditRequests(Long counselorId);
    List<Map<String, Object>> getSubmissionRecords(Long counselorId);
    List<Batch> getAvailableBatches();
    Submission createSubmission(Long counselorId, Long batchId, SubmissionRequest request);
    Submission updateSubmission(Long counselorId, Long submissionId, SubmissionRequest request);
    void submitSubmission(Long counselorId, Long submissionId);
    Map<String, Object> getSubmissionDetail(Long counselorId, Long submissionId);
    List<Map<String, Object>> getContacts(Long counselorId);
}
