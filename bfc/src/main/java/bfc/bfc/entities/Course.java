package bfc.bfc.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String institution;
    private String country;
    private String year;
    private String category;

    @Column(columnDefinition = "LONGTEXT")
    private String topics;

    @Column(columnDefinition = "LONGTEXT")
    private String logo;

    private Boolean isAccredited;
    private String programs;
    private String accreditation;
    private String intake;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Column(columnDefinition = "LONGTEXT")
    private String certificationDescription;

    @Column(columnDefinition = "LONGTEXT")
    private String brochureUrl;

    @Column(columnDefinition = "LONGTEXT")
    private String intro;

    @Column(columnDefinition = "LONGTEXT")
    private String participants;

    private String duration;
    private String location;
    private String language;

    @ElementCollection
    @CollectionTable(name = "course_learn_points", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "point", columnDefinition = "LONGTEXT")
    private List<String> learnPoints;

    @ElementCollection
    @CollectionTable(name = "course_journey_steps", joinColumns = @JoinColumn(name = "course_id"))
    private List<JourneyStep> journeySteps;
}
