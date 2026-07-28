package com.counselor.service;

import com.counselor.dto.psych.PsychAlertUpdateRequest;
import com.counselor.dto.psych.PsychBatchRequest;
import com.counselor.dto.psych.PsychRecordRequest;
import com.counselor.entity.*;
import com.counselor.enums.*;
import com.counselor.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PsychAssessmentService {
    private static final String CONSENT_VERSION = "2026-01";

    private final PsychAssessmentBatchRepository batchRepository;
    private final PsychScaleQuestionRepository questionRepository;
    private final PsychAssessmentRecordRepository recordRepository;
    private final PsychAssessmentAnswerRepository answerRepository;
    private final PsychAiReportRepository reportRepository;
    private final PsychRiskAlertRepository alertRepository;
    private final CounselorRepository counselorRepository;
    private final AdminRepository adminRepository;
    private final OperationLogRepository operationLogRepository;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @Value("${ai.psych.base-url:}") private String aiBaseUrl;
    @Value("${ai.psych.api-key:}") private String aiApiKey;
    @Value("${ai.psych.model:}") private String aiModel;

    public List<Map<String, Object>> getCounselorBatches(Long counselorId) {
        LocalDateTime now = LocalDateTime.now();
        return batchRepository.findByStatusOrderByCreatedAtDesc(PsychBatchStatus.PUBLISHED).stream()
                .filter(b -> (b.getStartTime() == null || !now.isBefore(b.getStartTime())) && (b.getEndTime() == null || !now.isAfter(b.getEndTime())))
                .map(b -> batchMap(b, recordRepository.findByCounselorIdAndBatchId(counselorId, b.getId()).orElse(null)))
                .toList();
    }

    public Map<String, Object> getBatchDetail(Long counselorId, Long batchId) {
        PsychAssessmentBatch batch = requireAvailableBatch(batchId);
        Map<String, Object> result = new LinkedHashMap<>(batchMap(batch, recordRepository.findByCounselorIdAndBatchId(counselorId, batchId).orElse(null)));
        result.put("consentVersion", CONSENT_VERSION);
        result.put("privacyNotice", "评估用于自我了解和必要的人工关怀，不构成医学诊断。具体答案和完整报告仅本人可见；如结果达到较高或高风险，系统会将姓名、风险等级和预警原因通知管理员。个人答案和AI报告保存两年。继续答题表示您已阅读并同意以上规则。");
        result.put("questions", questionRepository.findAllByOrderBySortOrderAsc().stream().map(this::questionMap).toList());
        recordRepository.findByCounselorIdAndBatchId(counselorId, batchId).ifPresent(record -> {
            if (record.getStatus() == PsychRecordStatus.DRAFT) {
                Map<Long, Integer> savedAnswers = new LinkedHashMap<>();
                answerRepository.findByRecordId(record.getId()).forEach(a -> savedAnswers.put(a.getQuestion().getId(), a.getSelectedValue()));
                result.put("answers", savedAnswers);
            }
        });
        return result;
    }

    @Transactional
    public Map<String, Object> saveRecord(Long counselorId, PsychRecordRequest request) {
        if (!request.isConsent()) throw new IllegalArgumentException("必须同意隐私说明后才能开始评估");
        PsychAssessmentBatch batch = requireAvailableBatch(request.getBatchId());
        Counselor counselor = counselorRepository.findById(counselorId).orElseThrow(() -> new IllegalArgumentException("辅导员不存在"));
        PsychAssessmentRecord record = recordRepository.findByCounselorIdAndBatchId(counselorId, batch.getId()).orElseGet(() -> recordRepository.save(
                PsychAssessmentRecord.builder().counselor(counselor).batch(batch).consentVersion(CONSENT_VERSION).consentTime(LocalDateTime.now()).build()));
        if (record.getStatus() == PsychRecordStatus.SUBMITTED) throw new IllegalStateException("本批次已经提交，不能重复填写");
        saveAnswers(record, request.getAnswers());
        return recordSummary(record);
    }

    @Transactional
    public Map<String, Object> updateRecord(Long counselorId, Long recordId, PsychRecordRequest request) {
        PsychAssessmentRecord record = requireOwnRecord(counselorId, recordId);
        requireAvailableBatch(record.getBatch().getId());
        if (record.getStatus() == PsychRecordStatus.SUBMITTED) throw new IllegalStateException("已提交的评估不能修改");
        saveAnswers(record, request.getAnswers());
        return recordSummary(record);
    }

    @Transactional
    public Map<String, Object> submitRecord(Long counselorId, Long recordId) {
        PsychAssessmentRecord record = requireOwnRecord(counselorId, recordId);
        requireAvailableBatch(record.getBatch().getId());
        if (record.getStatus() == PsychRecordStatus.SUBMITTED) throw new IllegalStateException("本批次已经提交");
        List<PsychAssessmentAnswer> answers = answerRepository.findByRecordId(recordId);
        int required = questionRepository.findAllByOrderBySortOrderAsc().size();
        if (answers.size() != required) throw new IllegalArgumentException("请完成全部题目后再提交");

        int pss = score(answers, "PSS10");
        int gad = score(answers, "GAD7");
        int phq = score(answers, "PHQ9");
        boolean selfHarm = answers.stream().anyMatch(a -> Boolean.TRUE.equals(a.getQuestion().getSelfHarmItem()) && a.getSelectedValue() > 0);
        PsychRiskLevel risk = calculateRisk(pss, gad, phq, selfHarm);
        record.setPssScore(pss); record.setGadScore(gad); record.setPhqScore(phq);
        record.setSelfHarmFlag(selfHarm); record.setRiskLevel(risk);
        record.setStatus(PsychRecordStatus.SUBMITTED); record.setSubmittedAt(LocalDateTime.now()); record.setAiStatus("GENERATING");
        recordRepository.save(record);

        generateReport(record);
        if (risk == PsychRiskLevel.ELEVATED || risk == PsychRiskLevel.HIGH) createAlert(record);
        return getReport(counselorId, recordId);
    }

    public List<Map<String, Object>> getRecords(Long counselorId) {
        return recordRepository.findByCounselorIdOrderByCreatedAtDesc(counselorId).stream().map(this::recordSummary).toList();
    }

    public Map<String, Object> getReport(Long counselorId, Long recordId) {
        PsychAssessmentRecord record = requireOwnRecord(counselorId, recordId);
        if (record.getStatus() != PsychRecordStatus.SUBMITTED) throw new IllegalStateException("评估尚未提交");
        Map<String, Object> result = new LinkedHashMap<>(recordSummary(record));
        reportRepository.findByRecordId(recordId).ifPresent(report -> {
            result.put("statusSummary", report.getStatusSummary());
            result.put("stressAnalysis", report.getStressAnalysis());
            result.put("emotionAnalysis", report.getEmotionAnalysis());
            result.put("suggestions", report.getSuggestions());
            result.put("modelName", report.getModelName());
        });
        result.put("disclaimer", "本结果仅用于心理状态筛查和自我了解，不构成医学诊断。如持续感到痛苦或影响正常生活，请联系学校心理中心或专业医疗机构。");
        return result;
    }

    public List<Map<String, Object>> getAdminBatches() {
        return batchRepository.findAllByOrderByCreatedAtDesc().stream().map(b -> batchMap(b, null)).toList();
    }

    @Transactional
    public Map<String, Object> createBatch(Long adminId, PsychBatchRequest request) {
        Admin admin = adminRepository.findById(adminId).orElseThrow(() -> new IllegalArgumentException("管理员不存在"));
        validateTimes(request.getStartTime(), request.getEndTime());
        PsychAssessmentBatch batch = batchRepository.save(PsychAssessmentBatch.builder().title(request.getTitle()).description(request.getDescription())
                .startTime(request.getStartTime()).endTime(request.getEndTime()).anonymousStatistics(request.getAnonymousStatistics()).createdBy(admin).build());
        log(admin, "创建心理评估批次", "PSYCH_BATCH", batch.getId(), batch.getTitle());
        return batchMap(batch, null);
    }

    @Transactional
    public Map<String, Object> updateBatch(Long adminId, Long id, PsychBatchRequest request) {
        PsychAssessmentBatch batch = requireBatch(id);
        if (batch.getStatus() != PsychBatchStatus.DRAFT) throw new IllegalStateException("只有草稿批次可以修改");
        validateTimes(request.getStartTime(), request.getEndTime());
        batch.setTitle(request.getTitle()); batch.setDescription(request.getDescription()); batch.setStartTime(request.getStartTime());
        batch.setEndTime(request.getEndTime()); batch.setAnonymousStatistics(request.getAnonymousStatistics());
        log(requireAdmin(adminId), "修改心理评估批次", "PSYCH_BATCH", id, batch.getTitle());
        return batchMap(batchRepository.save(batch), null);
    }

    @Transactional
    public Map<String, Object> changeBatchStatus(Long adminId, Long id, PsychBatchStatus status) {
        PsychAssessmentBatch batch = requireBatch(id);
        if (status == PsychBatchStatus.DRAFT || (batch.getStatus() == PsychBatchStatus.ENDED && status != PsychBatchStatus.ENDED))
            throw new IllegalStateException("批次状态不能逆向修改");
        batch.setStatus(status);
        if(status==PsychBatchStatus.PUBLISHED)notificationService.sendToActiveCounselors("新的心理评估批次","“"+batch.getTitle()+"”已发布，请自愿参加评估。","PSYCH_BATCH","/psych");
        log(requireAdmin(adminId), status == PsychBatchStatus.PUBLISHED ? "发布心理评估批次" : "结束心理评估批次", "PSYCH_BATCH", id, batch.getTitle());
        return batchMap(batchRepository.save(batch), null);
    }

    public Map<String, Object> getDashboard() {
        long total = recordRepository.countByStatus(PsychRecordStatus.SUBMITTED);
        Map<String, Long> risks = new LinkedHashMap<>();
        for (PsychRiskLevel level : PsychRiskLevel.values()) risks.put(level.name(), recordRepository.countByRiskLevel(level));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("submitted", total); result.put("batches", batchRepository.count()); result.put("riskDistribution", risks);
        result.put("pendingAlerts", alertRepository.countByStatus(PsychAlertStatus.PENDING));
        result.put("processingAlerts", alertRepository.countByStatus(PsychAlertStatus.PROCESSING));
        result.put("completedAlerts", alertRepository.countByStatus(PsychAlertStatus.COMPLETED));
        long activeCounselors = counselorRepository.countByAccountStatus(AccountStatus.ACTIVE);
        result.put("activeCounselors", activeCounselors);
        result.put("participationRate", activeCounselors == 0 ? 0 : Math.round(total * 1000.0 / activeCounselors) / 10.0);
        Map<String, Long> colleges = new LinkedHashMap<>();
        Map<String, Long> months = new TreeMap<>();
        recordRepository.findAll().stream().filter(r -> r.getStatus() == PsychRecordStatus.SUBMITTED).forEach(r -> {
            String college = r.getCounselor().getCollege() == null ? "未设置学院" : r.getCounselor().getCollege().getName();
            colleges.merge(college, 1L, Long::sum);
            String month = r.getSubmittedAt() == null ? "未知" : r.getSubmittedAt().toLocalDate().withDayOfMonth(1).toString().substring(0, 7);
            months.merge(month, 1L, Long::sum);
        });
        result.put("collegeDistribution", colleges);
        result.put("monthlyTrend", months);
        return result;
    }

    public List<Map<String, Object>> getAlerts(Long adminId) {
        Admin admin = requireAdmin(adminId);
        log(admin, "查看心理风险预警", "PSYCH_ALERT", null, "查看高风险名单");
        return alertRepository.findAllByOrderByCreatedAtDesc().stream().map(this::alertMap).toList();
    }

    public Map<String, Object> getAlert(Long adminId, Long id) {
        PsychRiskAlert alert = requireAlert(id);
        log(requireAdmin(adminId), "查看心理风险预警详情", "PSYCH_ALERT", id, alert.getReason());
        return alertMap(alert);
    }

    @Transactional
    public Map<String, Object> updateAlert(Long adminId, Long id, PsychAlertUpdateRequest request) {
        Admin admin = requireAdmin(adminId);
        PsychRiskAlert alert = requireAlert(id);
        alert.setStatus(request.getStatus()); alert.setFollowUpNote(request.getFollowUpNote()); alert.setContactedAt(request.getContactedAt()); alert.setAssignedAdmin(admin);
        if (request.getStatus() == PsychAlertStatus.COMPLETED) alert.setCompletedAt(LocalDateTime.now());
        else alert.setCompletedAt(null);
        alertRepository.save(alert);
        log(admin, "更新心理风险跟进", "PSYCH_ALERT", id, request.getStatus().name());
        return alertMap(alert);
    }

    private void saveAnswers(PsychAssessmentRecord record, Map<Long, Integer> values) {
        if (values == null) return;
        Map<Long, PsychScaleQuestion> questions = new HashMap<>();
        questionRepository.findAll().forEach(q -> questions.put(q.getId(), q));
        Map<Long, PsychAssessmentAnswer> existingAnswers = new HashMap<>();
        answerRepository.findByRecordId(record.getId()).forEach(answer ->
                existingAnswers.put(answer.getQuestion().getId(), answer));
        List<PsychAssessmentAnswer> answers = new ArrayList<>();
        values.forEach((id, value) -> {
            PsychScaleQuestion q = questions.get(id);
            if (q == null || value == null || value < 0 || value > q.getMaxScore()) throw new IllegalArgumentException("存在无效的答题选项");
            int calculated = Boolean.TRUE.equals(q.getReverseScored()) ? q.getMaxScore() - value : value;
            PsychAssessmentAnswer answer = existingAnswers.remove(id);
            if (answer == null) answer = PsychAssessmentAnswer.builder().record(record).question(q).build();
            answer.setSelectedValue(value);
            answer.setCalculatedScore(calculated);
            answers.add(answer);
        });
        if (!existingAnswers.isEmpty()) answerRepository.deleteAll(existingAnswers.values());
        answerRepository.saveAll(answers);
    }

    private void generateReport(PsychAssessmentRecord record) {
        PsychAiReport report = fallbackReport(record);
        if (StringUtils.hasText(aiBaseUrl) && StringUtils.hasText(aiApiKey) && StringUtils.hasText(aiModel)) {
            try {
                SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                requestFactory.setConnectTimeout(10000);
                requestFactory.setReadTimeout(60000);
                RestClient client = RestClient.builder().baseUrl(aiBaseUrl).requestFactory(requestFactory).build();
                Map<String, Object> schemaPrompt = Map.of("role", "system", "content", "你是高校心理健康筛查报告助手。只能根据分数提供温和、非诊断性的中文解释，不得诊断疾病、推荐药物或使用绝对化结论。只返回JSON，字段必须为statusSummary、stressAnalysis、emotionAnalysis、suggestions。建议必须包含可执行的日常调节方法和必要时寻求专业帮助。不要输出姓名、学院、电话等身份信息。");
                Map<String, Object> scorePrompt = Map.of("role", "user", "content", "PSS-10压力分=" + record.getPssScore() + "/40，GAD-7焦虑分=" + record.getGadScore() + "/21，PHQ-9抑郁分=" + record.getPhqScore() + "/27，自伤题是否非零=" + record.getSelfHarmFlag() + "，综合风险=" + record.getRiskLevel());
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("model", aiModel); body.put("temperature", 0.2); body.put("response_format", Map.of("type", "json_object")); body.put("messages", List.of(schemaPrompt, scorePrompt));
                String raw = client.post().uri("/chat/completions").contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + aiApiKey)
                        .body(body).retrieve().body(String.class);
                JsonNode root = objectMapper.readTree(raw);
                JsonNode content = objectMapper.readTree(root.path("choices").path(0).path("message").path("content").asText());
                String summary = safeAiText(requiredText(content, "statusSummary"));
                String stress = safeAiText(requiredText(content, "stressAnalysis"));
                String emotion = safeAiText(requiredText(content, "emotionAnalysis"));
                String suggestions = safeAiText(requiredText(content, "suggestions"));
                report.setStatusSummary(summary); report.setStressAnalysis(stress);
                report.setEmotionAnalysis(emotion); report.setSuggestions(suggestions);
                report.setModelName(aiModel); record.setAiStatus("SUCCESS");
            } catch (Exception ex) {
                report.setFailureReason(ex.getMessage()); record.setAiStatus("FALLBACK");
            }
        } else record.setAiStatus("FALLBACK");
        report.setGeneratedAt(LocalDateTime.now()); reportRepository.save(report); recordRepository.save(record);
    }

    private PsychAiReport fallbackReport(PsychAssessmentRecord record) {
        String summary = switch (record.getRiskLevel()) {
            case LOW -> "目前量表结果整体处于较低风险范围，请继续保持规律作息和稳定的社会支持。";
            case MEDIUM -> "近期可能存在一定心理压力或情绪困扰，建议主动安排休息并观察状态变化。";
            case ELEVATED -> "量表提示近期心理负担较明显，建议尽快联系学校心理中心进行专业沟通。";
            case HIGH -> "量表提示当前需要优先获得支持，请尽快联系学校心理中心、可信赖的人或专业医疗机构。";
        };
        String stress = "PSS-10 得分为 " + record.getPssScore() + " 分。" + (record.getPssScore() >= 27 ? "当前感受到的压力较高。" : record.getPssScore() >= 14 ? "当前感受到一定程度的压力。" : "当前压力感受处于较低范围。");
        String emotion = "GAD-7 得分为 " + record.getGadScore() + " 分，PHQ-9 得分为 " + record.getPhqScore() + " 分。结果只反映最近一段时间的自评状态，不代表医学诊断。";
        String suggestions = "建议保持规律睡眠和饮食，每天安排适量运动，主动与可信赖的人交流，并减少连续超负荷工作。若不适持续、加重或影响生活，请及时联系学校心理中心或专业医疗机构。";
        if (Boolean.TRUE.equals(record.getSelfHarmFlag())) suggestions = "请不要独自承受，立即联系可信赖的人陪伴，并尽快联系学校心理中心或专业医疗机构；如存在马上伤害自己的危险，请立即拨打 120 或 110。";
        PsychAiReport report = reportRepository.findByRecordId(record.getId()).orElseGet(PsychAiReport::new);
        report.setRecord(record); report.setStatusSummary(summary); report.setStressAnalysis(stress); report.setEmotionAnalysis(emotion);
        report.setSuggestions(suggestions); report.setModelName("规则解读"); report.setFailureReason(null);
        return report;
    }

    private void createAlert(PsychAssessmentRecord record) {
        if (alertRepository.findByRecordId(record.getId()).isPresent()) return;
        String reason = Boolean.TRUE.equals(record.getSelfHarmFlag()) ? "自伤相关题目选择了非零选项，需要立即人工关怀" : "综合心理筛查结果达到" + riskLabel(record.getRiskLevel());
        alertRepository.save(PsychRiskAlert.builder().record(record).riskLevel(record.getRiskLevel()).reason(reason).build());
    }

    private int score(List<PsychAssessmentAnswer> answers, String type) { return answers.stream().filter(a -> type.equals(a.getQuestion().getScaleType())).mapToInt(PsychAssessmentAnswer::getCalculatedScore).sum(); }
    private PsychRiskLevel calculateRisk(int pss, int gad, int phq, boolean selfHarm) {
        if (selfHarm || gad >= 15 || phq >= 20) return PsychRiskLevel.HIGH;
        if (pss >= 27 || gad >= 10 || phq >= 15) return PsychRiskLevel.ELEVATED;
        if (pss >= 14 || gad >= 5 || phq >= 5) return PsychRiskLevel.MEDIUM;
        return PsychRiskLevel.LOW;
    }

    private Map<String, Object> batchMap(PsychAssessmentBatch b, PsychAssessmentRecord record) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", b.getId()); m.put("title", b.getTitle()); m.put("description", b.getDescription()); m.put("status", b.getStatus());
        m.put("startTime", b.getStartTime()); m.put("endTime", b.getEndTime()); m.put("anonymousStatistics", b.getAnonymousStatistics());
        m.put("recordId", record == null ? null : record.getId()); m.put("recordStatus", record == null ? null : record.getStatus());
        return m;
    }

    private Map<String, Object> questionMap(PsychScaleQuestion q) {
        Map<String, Object> m = new LinkedHashMap<>(); m.put("id", q.getId()); m.put("code", q.getCode()); m.put("scaleType", q.getScaleType()); m.put("content", q.getContent()); m.put("sortOrder", q.getSortOrder());
        try { m.put("options", objectMapper.readValue(q.getOptionsJson(), List.class)); } catch (Exception e) { m.put("options", List.of()); }
        return m;
    }

    private Map<String, Object> recordSummary(PsychAssessmentRecord r) {
        Map<String, Object> m = new LinkedHashMap<>(); m.put("id", r.getId()); m.put("batchId", r.getBatch().getId()); m.put("batchTitle", r.getBatch().getTitle()); m.put("status", r.getStatus());
        m.put("pssScore", r.getPssScore()); m.put("gadScore", r.getGadScore()); m.put("phqScore", r.getPhqScore()); m.put("riskLevel", r.getRiskLevel()); m.put("selfHarmFlag", r.getSelfHarmFlag());
        m.put("aiStatus", r.getAiStatus()); m.put("submittedAt", r.getSubmittedAt()); m.put("createdAt", r.getCreatedAt()); return m;
    }

    private Map<String, Object> alertMap(PsychRiskAlert a) {
        Counselor c = a.getRecord().getCounselor(); Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId()); m.put("recordId", a.getRecord().getId()); m.put("counselorId", c.getId()); m.put("name", c.getName()); m.put("photoUrl", c.getPhotoUrl());
        m.put("collegeName", c.getCollege() == null ? "未设置" : c.getCollege().getName()); m.put("riskLevel", a.getRiskLevel()); m.put("reason", a.getReason()); m.put("status", a.getStatus());
        m.put("followUpNote", a.getFollowUpNote()); m.put("contactedAt", a.getContactedAt()); m.put("completedAt", a.getCompletedAt()); m.put("createdAt", a.getCreatedAt());
        m.put("assignedAdminName", a.getAssignedAdmin() == null ? null : a.getAssignedAdmin().getName()); return m;
    }

    private PsychAssessmentBatch requireAvailableBatch(Long id) {
        PsychAssessmentBatch b = requireBatch(id); LocalDateTime now = LocalDateTime.now();
        if (b.getStatus() != PsychBatchStatus.PUBLISHED || (b.getStartTime() != null && now.isBefore(b.getStartTime())) || (b.getEndTime() != null && now.isAfter(b.getEndTime()))) throw new IllegalStateException("该评估批次当前不可填写");
        return b;
    }
    private PsychAssessmentBatch requireBatch(Long id) { return batchRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("心理评估批次不存在")); }
    private PsychAssessmentRecord requireOwnRecord(Long counselorId, Long id) { PsychAssessmentRecord r = recordRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("评估记录不存在")); if (!r.getCounselor().getId().equals(counselorId)) throw new SecurityException("无权访问该评估记录"); return r; }
    private Admin requireAdmin(Long id) { return adminRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("管理员不存在")); }
    private PsychRiskAlert requireAlert(Long id) { return alertRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("预警记录不存在")); }
    private String requiredText(JsonNode node, String field) { String value = node.path(field).asText(); if (!StringUtils.hasText(value)) throw new IllegalArgumentException("AI返回内容缺少字段：" + field); return value; }
    private String safeAiText(String value) {
        String[] forbidden = {"诊断为", "确诊", "建议服用", "用药", "药物治疗", "一定会", "绝对"};
        for (String word : forbidden) if (value.contains(word)) throw new IllegalArgumentException("AI返回了不允许的诊断或用药表述");
        return value;
    }
    private void validateTimes(LocalDateTime start, LocalDateTime end) { if (start != null && end != null && !end.isAfter(start)) throw new IllegalArgumentException("结束时间必须晚于开始时间"); }
    private String riskLabel(PsychRiskLevel level) { return switch (level) { case LOW -> "低风险"; case MEDIUM -> "中等风险"; case ELEVATED -> "较高风险"; case HIGH -> "高风险"; }; }
    private void log(Admin admin, String action, String type, Long targetId, String detail) { operationLogRepository.save(OperationLog.builder().admin(admin).action(action).targetType(type).targetId(targetId).detail(detail).build()); }

    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void purgeExpiredPrivateData() {
        LocalDateTime cutoff = LocalDateTime.now().minusYears(2);
        for (PsychAssessmentRecord record : recordRepository.findBySubmittedAtBefore(cutoff)) {
            answerRepository.deleteByRecordId(record.getId()); reportRepository.deleteByRecordId(record.getId());
            record.setAiStatus("PURGED"); recordRepository.save(record);
        }
    }

    @Scheduled(fixedDelay = 600000, initialDelay = 120000)
    @Transactional
    public void retryFailedReports() {
        if (!StringUtils.hasText(aiBaseUrl) || !StringUtils.hasText(aiApiKey) || !StringUtils.hasText(aiModel)) return;
        for (PsychAssessmentRecord record : recordRepository.findTop20ByAiStatusAndStatusOrderByUpdatedAtAsc("FALLBACK", PsychRecordStatus.SUBMITTED)) generateReport(record);
    }
}
