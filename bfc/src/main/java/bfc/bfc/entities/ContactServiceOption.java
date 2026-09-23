package bfc.bfc.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contact_service_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactServiceOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String value;

    @Column(nullable = false)
    private String label;

    @Builder.Default
    private Integer displayOrder = 0;
}
