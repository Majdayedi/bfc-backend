package bfc.bfc.service;

import bfc.bfc.dto.TeamMemberRequest;
import bfc.bfc.dto.TeamMemberResponse;
import bfc.bfc.entities.ExtraFlag;
import bfc.bfc.entities.TeamMember;
import bfc.bfc.repository.TeamMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TeamMemberService {

    private final TeamMemberRepository repository;

    public TeamMemberService(TeamMemberRepository repository) {
        this.repository = repository;
    }

    public List<TeamMemberResponse> getAll() {
        return repository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TeamMemberResponse getById(Long id) {
        TeamMember member = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team member not found with id: " + id));
        return toResponse(member);
    }

    @Transactional
    public TeamMemberResponse create(TeamMemberRequest request) {
        TeamMember member = toEntity(request);
        if (member.getDisplayOrder() == null) {
            member.setDisplayOrder(repository.findMaxDisplayOrder() + 1);
        }
        return toResponse(repository.save(member));
    }

    @Transactional
    public TeamMemberResponse update(Long id, TeamMemberRequest request) {
        TeamMember existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team member not found with id: " + id));

        existing.setName(request.getName());
        existing.setRole(request.getRole());
        existing.setRoleType(request.getRoleType());
        existing.setImg(request.getImg());
        existing.setEmail(request.getEmail());
        existing.setPhone(request.getPhone());
        existing.setCvUrl(request.getCvUrl());
        existing.setCountryName(request.getCountryName());
        existing.setCountryFlagUrl(request.getCountryFlagUrl());
        existing.setDisplayOrder(request.getDisplayOrder());
        existing.setShowPrimaryFlag(request.getShowPrimaryFlag() != null ? request.getShowPrimaryFlag() : true);
        existing.setExtraFlags(request.getExtraFlags() != null ? request.getExtraFlags() : new ArrayList<>());

        return toResponse(repository.save(existing));
    }

    @Transactional
    public List<TeamMemberResponse> reorder(List<? extends Number> ids) {
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i).longValue();
            TeamMember member = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Team member not found with id: " + id));
            member.setDisplayOrder(i + 1);
            repository.save(member);
        }
        return getAll();
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Team member not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private TeamMemberResponse toResponse(TeamMember member) {
        List<ExtraFlag> flags = member.getExtraFlags();
        return TeamMemberResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .role(member.getRole())
                .roleType(member.getRoleType())
                .img(member.getImg())
                .email(member.getEmail())
                .phone(member.getPhone())
                .cvUrl(member.getCvUrl())
                .countryName(member.getCountryName())
                .countryFlagUrl(member.getCountryFlagUrl())
                .displayOrder(member.getDisplayOrder())
                .showPrimaryFlag(member.getShowPrimaryFlag())
                .extraFlags(flags != null ? new ArrayList<>(flags) : new ArrayList<>())
                .build();
    }

    private TeamMember toEntity(TeamMemberRequest request) {
        return TeamMember.builder()
                .name(request.getName())
                .role(request.getRole())
                .roleType(request.getRoleType())
                .img(request.getImg())
                .email(request.getEmail())
                .phone(request.getPhone())
                .cvUrl(request.getCvUrl())
                .countryName(request.getCountryName())
                .countryFlagUrl(request.getCountryFlagUrl())
                .displayOrder(request.getDisplayOrder())
                .showPrimaryFlag(request.getShowPrimaryFlag() != null ? request.getShowPrimaryFlag() : true)
                .extraFlags(request.getExtraFlags() != null ? request.getExtraFlags() : new ArrayList<>())
                .build();
    }
}
