package com.counselor.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "psych_scale_question", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PsychScaleQuestion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 20) private String code;
    @Column(name = "scale_type", nullable = false, length = 20) private String scaleType;
    @Column(nullable = false, length = 500) private String content;
    @Column(name = "options_json", nullable = false, columnDefinition = "TEXT") private String optionsJson;
    @Column(name = "sort_order", nullable = false) private Integer sortOrder;
    @Column(name = "reverse_scored", nullable = false) @Builder.Default private Boolean reverseScored = false;
    @Column(name = "max_score", nullable = false) private Integer maxScore;
    @Column(name = "self_harm_item", nullable = false) @Builder.Default private Boolean selfHarmItem = false;
}
