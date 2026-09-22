package bfc.bfc.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicePageResponse {
    private Long id;
    private String slug;
    private String title;
    private String subtitle;
    private String description;
    private String layoutType;
    private String contentJson;
    private Integer displayOrder;
}
