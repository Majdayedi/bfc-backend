package bfc.bfc.controllers;

import bfc.bfc.entities.HistoryEvent;
import bfc.bfc.repositories.HistoryEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history-events")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class HistoryEventController {

    private final HistoryEventRepository repository;

    @GetMapping
    public List<HistoryEvent> getAllEvents() {
        return repository.findAllByOrderByEventYearAsc();
    }

    @PostMapping
    public ResponseEntity<HistoryEvent> createEvent(@RequestBody HistoryEvent event) {
        return ResponseEntity.ok(repository.save(event));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<HistoryEvent> updateEvent(@PathVariable Long id, @RequestBody HistoryEvent eventDetails) {
        return repository.findById(id).map(event -> {
            event.setEventYear(eventDetails.getEventYear());
            event.setTitle(eventDetails.getTitle());
            event.setDescription(eventDetails.getDescription());
            event.setBackgroundColor(eventDetails.getBackgroundColor());
            event.setCountryFlag(eventDetails.getCountryFlag());
            event.setLogoUrl(eventDetails.getLogoUrl());
            event.setPhotoUrl(eventDetails.getPhotoUrl());
            return ResponseEntity.ok(repository.save(event));
        }).orElse(ResponseEntity.notFound().build());
    }
}
