package bfc.bfc.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JourneyStep {

    private String title;
    
    @Column(columnDefinition = "LONGTEXT")
    private String detail;
}
