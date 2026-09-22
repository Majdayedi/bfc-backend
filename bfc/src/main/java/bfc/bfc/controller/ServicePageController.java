package bfc.bfc.controller;

import bfc.bfc.dto.ServicePageRequest;
import bfc.bfc.dto.ServicePageResponse;
import bfc.bfc.service.ServicePageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
public class ServicePageController {

    private final ServicePageService service;

    public ServicePageController(ServicePageService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ServicePageResponse>> getAll() {
        return ResponseEntity.ok(service.getAllServicePages());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ServicePageResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(service.getServicePageBySlug(slug));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicePageResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getServicePageById(id));
    }

    @PostMapping
    public ResponseEntity<ServicePageResponse> create(@RequestBody ServicePageRequest request) {
        return ResponseEntity.ok(service.createServicePage(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicePageResponse> update(@PathVariable Long id, @RequestBody ServicePageRequest request) {
        return ResponseEntity.ok(service.updateServicePage(id, request));
    }

    @PutMapping("/reorder")
    public ResponseEntity<Void> reorder(@RequestBody List<? extends Number> ids) {
        service.reorderServicePages(ids);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        service.deleteServicePage(id);
        return ResponseEntity.ok(Map.of("message", "Service page deleted successfully"));
    }
}
