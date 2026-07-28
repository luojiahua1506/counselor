package com.counselor.dto.counselor;

import lombok.Data;
import java.util.List;

@Data
public class SubmissionRequest {
    private String name;
    private String gender;
    private Long collegeId;
    private String politicalStatus;
    private String highestEducation;
    private String officeAddress;
    private String phone;
    private String email;
    private String photoUrl;
    private List<ExperienceItem> workExperiences;
    private List<ExperienceItem> educationExperiences;

    @Data
    public static class ExperienceItem {
        private String organization;
        private String position;
        private String startDate;
        private String endDate;
        private String description;
        private String school;
        private String major;
        private String degree;
        private Integer sortOrder;
    }
}
