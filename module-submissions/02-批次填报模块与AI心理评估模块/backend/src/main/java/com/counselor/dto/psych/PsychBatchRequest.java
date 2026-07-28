package com.counselor.dto.psych;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PsychBatchRequest {
    @NotBlank private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean anonymousStatistics = true;
}
