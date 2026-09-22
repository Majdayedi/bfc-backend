package bfc.bfc.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String category;

    private String client;

    private String country;

    @Column(name = "representative_slug")
    private String representativeSlug;

    private String flag;

    private String year;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    private String accent;

    @Column(columnDefinition = "LONGTEXT")
    private String imageUrl;

    @Column(columnDefinition = "LONGTEXT")
    private String clientImageUrl;

    private String startDate;

    private String endDate;

    @Column(columnDefinition = "LONGTEXT")
    private String contentJson;
}
