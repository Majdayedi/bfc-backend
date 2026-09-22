package bfc.bfc.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "representatives")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Representative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String title;

    private String subtitle;

    @Column(name = "creation_year")
    private Integer creationYear;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String location;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private TeamMember manager;

    @Column(name = "globe_marker_top")
    private String globeMarkerTop;

    @Column(name = "globe_marker_left")
    private String globeMarkerLeft;

    @Column(name = "globe_view_rotate_y")
    private String globeViewRotateY;

    @Column(name = "globe_view_map_x")
    private String globeViewMapX;

    @Column(name = "flag_icon_url")
    private String flagIconUrl;

    @Column(name = "image_url")
    private String imageUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "representative_project_countries", joinColumns = @JoinColumn(name = "representative_id"))
    @Column(name = "country")
    private List<String> projectCountries;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "representative_fallback_countries", joinColumns = @JoinColumn(name = "representative_id"))
    @Column(name = "country")
    private List<String> fallbackCountries;
}
