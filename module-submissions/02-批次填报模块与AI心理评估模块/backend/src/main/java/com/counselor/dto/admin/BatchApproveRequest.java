package com.counselor.dto.admin;

import lombok.Data;
import java.util.List;

@Data
public class BatchApproveRequest {
    private List<Long> ids;
}
