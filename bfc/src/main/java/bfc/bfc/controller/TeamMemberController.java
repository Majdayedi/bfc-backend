package bfc.bfc.controller;

import bfc.bfc.dto.TeamMemberRequest;
import bfc.bfc.dto.TeamMemberResponse;
import bfc.bfc.service.TeamMemberService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/team-members")
public class TeamMemberController {

    private final TeamMemberService service;

    public TeamMemberController(TeamMemberService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<TeamMemberResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamMemberResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<TeamMemberResponse> create(@Valid @RequestBody TeamMemberRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamMemberResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody TeamMemberRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PutMapping("/reorder")
    public ResponseEntity<List<TeamMemberResponse>> reorder(@RequestBody List<? extends Number> ids) {
        return ResponseEntity.ok(service.reorder(ids));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Team member deleted successfully"));
    }
}
