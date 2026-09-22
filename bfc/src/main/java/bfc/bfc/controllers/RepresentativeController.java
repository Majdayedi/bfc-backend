package bfc.bfc.controllers;

import bfc.bfc.entities.Representative;
import bfc.bfc.repositories.RepresentativeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/representatives")
@CrossOrigin(origins = "*") // Adjust based on your security config
public class RepresentativeController {

    @Autowired
    private RepresentativeRepository representativeRepository;

    @GetMapping
    public List<Representative> getAllRepresentatives() {
        return representativeRepository.findAll();
    }

    @GetMapping("/{idOrSlug}")
    public ResponseEntity<Representative> getRepresentative(@PathVariable String idOrSlug) {
        try {
            Long id = Long.parseLong(idOrSlug);
            Optional<Representative> repById = representativeRepository.findById(id);
            if (repById.isPresent()) {
                return ResponseEntity.ok(repById.get());
            }
        } catch (NumberFormatException e) {
            // It's not a number, try by slug
        }
        
        Optional<Representative> repBySlug = representativeRepository.findBySlug(idOrSlug);
        return repBySlug.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Representative createRepresentative(@RequestBody Representative representative) {
        return representativeRepository.save(representative);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Representative> updateRepresentative(@PathVariable Long id, @RequestBody Representative representativeDetails) {
        return representativeRepository.findById(id)
                .map(existingRep -> {
                    existingRep.setSlug(representativeDetails.getSlug());
                    existingRep.setTitle(representativeDetails.getTitle());
                    existingRep.setSubtitle(representativeDetails.getSubtitle());
                    existingRep.setDescription(representativeDetails.getDescription());
                    existingRep.setLocation(representativeDetails.getLocation());
                    existingRep.setManager(representativeDetails.getManager());
                    existingRep.setGlobeMarkerTop(representativeDetails.getGlobeMarkerTop());
                    existingRep.setGlobeMarkerLeft(representativeDetails.getGlobeMarkerLeft());
                    existingRep.setGlobeViewRotateY(representativeDetails.getGlobeViewRotateY());
                    existingRep.setGlobeViewMapX(representativeDetails.getGlobeViewMapX());
                    existingRep.setFlagIconUrl(representativeDetails.getFlagIconUrl());
                    existingRep.setImageUrl(representativeDetails.getImageUrl());
                    existingRep.setProjectCountries(representativeDetails.getProjectCountries());
                    existingRep.setFallbackCountries(representativeDetails.getFallbackCountries());
                    return ResponseEntity.ok(representativeRepository.save(existingRep));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRepresentative(@PathVariable Long id) {
        if (representativeRepository.existsById(id)) {
            representativeRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
