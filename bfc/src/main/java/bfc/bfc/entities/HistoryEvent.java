package bfc.bfc.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "history_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer eventYear;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "background_color")
    private String backgroundColor;

    @Column(name = "country_flag")
    private String countryFlag;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "photo_url")
    private String photoUrl;
}
