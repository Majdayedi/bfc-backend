package bfc.bfc.dto;

import bfc.bfc.entities.ExtraFlag;
import bfc.bfc.entities.TeamMemberRole;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Role is required")
    private String role;

    private TeamMemberRole roleType;

    private String img;

    private String email;

    private String phone;

    private String cvUrl;

    private String countryName;

    private String countryFlagUrl;

    private Integer displayOrder;

    private Boolean showPrimaryFlag;

    private List<TeamMemberRole> roleTypes;

    private List<ExtraFlag> extraFlags;
}
