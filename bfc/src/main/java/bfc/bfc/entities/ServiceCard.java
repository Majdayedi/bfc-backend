package bfc.bfc.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "service_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String categoryName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "service_card_items", joinColumns = @JoinColumn(name = "card_id"))
    @Column(name = "item", columnDefinition = "TEXT")
    private List<String> items;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_page_id", nullable = false)
    private ServicePage servicePage;
}
