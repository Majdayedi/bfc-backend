package bfc.bfc.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "service_pages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicePage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(nullable = false)
    private String title;

    private String subtitle;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    private String layoutType;

    @Column(columnDefinition = "LONGTEXT")
    private String contentJson;

    @Builder.Default
    private Integer displayOrder = 0;

    @OneToMany(mappedBy = "servicePage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceCard> cards;
}
