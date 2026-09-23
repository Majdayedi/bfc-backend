package bfc.bfc.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "team_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    @Enumerated(EnumType.STRING)
    private TeamMemberRole roleType;

    private String img;

    private String email;

    private String phone;

    private String cvUrl;

    private String countryName;

    private String countryFlagUrl;

    private Integer displayOrder;

    @Column(nullable = false)
    private Boolean showPrimaryFlag = true;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "team_member_roles", joinColumns = @JoinColumn(name = "team_member_id"))
    @Column(name = "role_type", nullable = false)
    private List<TeamMemberRole> roleTypes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "team_member_extra_flags", joinColumns = @JoinColumn(name = "team_member_id"))
    private List<ExtraFlag> extraFlags = new ArrayList<>();
}
