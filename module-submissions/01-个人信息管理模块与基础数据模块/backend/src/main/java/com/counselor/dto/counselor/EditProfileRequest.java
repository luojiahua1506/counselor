package com.counselor.dto.counselor;

import lombok.Data;

@Data
public class EditProfileRequest {
    private String name;
    private String gender;
    private Long collegeId;
    private String politicalStatus;
    private String highestEducation;
    private String officeAddress;
    private String phone;
    private String email;    private String photoUrl;
}
