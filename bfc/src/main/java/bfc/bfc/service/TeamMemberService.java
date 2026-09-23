package bfc.bfc.service;

import bfc.bfc.dto.TeamMemberRequest;
import bfc.bfc.dto.TeamMemberResponse;
import bfc.bfc.entities.ExtraFlag;
import bfc.bfc.entities.TeamMember;
import bfc.bfc.entities.TeamMemberRole;
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
    private final bfc.bfc.repositories.RepresentativeRepository representativeRepository;

    public TeamMemberService(TeamMemberRepository repository, bfc.bfc.repositories.RepresentativeRepository representativeRepository) {
        this.repository = repository;
        this.representativeRepository = representativeRepository;
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
        if (member.getRoleTypes() != null && !member.getRoleTypes().isEmpty() && member.getRoleType() == null) {
            member.setRoleType(member.getRoleTypes().get(0));
        }
        return toResponse(repository.save(member));
    }

    @Transactional
    public TeamMemberResponse update(Long id, TeamMemberRequest request) {
        TeamMember existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team member not found with id: " + id));

        existing.setName(request.getName());
        existing.setRole(request.getRole());
        existing.setRoleType(request.getRoleTypes() != null && !request.getRoleTypes().isEmpty()
                ? request.getRoleTypes().get(0) : existing.getRoleType());
        existing.setImg(request.getImg());
        existing.setEmail(request.getEmail());
        existing.setPhone(request.getPhone());
        existing.setCvUrl(request.getCvUrl());
        existing.setCountryName(request.getCountryName());
        existing.setCountryFlagUrl(request.getCountryFlagUrl());
        existing.setDisplayOrder(request.getDisplayOrder());
        existing.setShowPrimaryFlag(request.getShowPrimaryFlag() != null ? request.getShowPrimaryFlag() : true);
        existing.setRoleTypes(request.getRoleTypes() != null ? request.getRoleTypes() : existing.getRoleTypes());
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
        TeamMember member = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team member not found with id: " + id));

        // Unlink from any Representative where this member is set as manager
        representativeRepository.findAll().forEach(rep -> {
            if (rep.getManager() != null && id.equals(rep.getManager().getId())) {
                rep.setManager(null);
                representativeRepository.save(rep);
            }
        });

        repository.delete(member);
    }

    private TeamMemberResponse toResponse(TeamMember member) {
        List<ExtraFlag> flags = member.getExtraFlags();
        List<TeamMemberRole> roleTypes = member.getRoleTypes() != null ? member.getRoleTypes() : new ArrayList<>();
        return TeamMemberResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .role(member.getRole())
                .roleType(member.getRoleType())
                .roleTypes(roleTypes)
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
        List<TeamMemberRole> roleTypes = request.getRoleTypes() != null ? request.getRoleTypes() : new ArrayList<>();
        return TeamMember.builder()
                .name(request.getName())
                .role(request.getRole())
                .roleType(roleTypes.isEmpty() ? request.getRoleType() : roleTypes.get(0))
                .roleTypes(roleTypes)
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
