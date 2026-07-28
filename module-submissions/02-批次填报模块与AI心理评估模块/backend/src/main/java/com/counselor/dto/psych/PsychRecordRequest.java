package com.counselor.dto.psych;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Map;

@Data
public class PsychRecordRequest {
    @NotNull private Long batchId;
    @AssertTrue(message = "必须同意隐私说明后才能开始评估") private boolean consent;
    private Map<Long, Integer> answers;
}
