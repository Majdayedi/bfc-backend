package bfc.bfc.controller;

import bfc.bfc.entities.Partner;
import bfc.bfc.repository.PartnerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/partners")
public class PartnerController {

    private final PartnerRepository partnerRepository;

    public PartnerController(PartnerRepository partnerRepository) {
        this.partnerRepository = partnerRepository;
    }

    @GetMapping
    public List<Partner> getAllPartners() {
        return partnerRepository.findAllByOrderByDisplayOrderAsc();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Partner> getPartnerById(@PathVariable Long id) {
        return partnerRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Partner createPartner(@RequestBody Partner partner) {
        if (partner.getDisplayOrder() == null) {
            partner.setDisplayOrder((int) partnerRepository.count() + 1);
        }
        return partnerRepository.save(partner);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Partner> updatePartner(@PathVariable Long id, @RequestBody Partner details) {
        return partnerRepository.findById(id).map(existing -> {
            existing.setName(details.getName());
            existing.setLogoUrl(details.getLogoUrl());
            if (details.getDisplayOrder() != null) {
                existing.setDisplayOrder(details.getDisplayOrder());
            }
            return ResponseEntity.ok(partnerRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePartner(@PathVariable Long id) {
        if (!partnerRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        partnerRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reorder")
    public ResponseEntity<List<Partner>> reorderPartners(@RequestBody List<Partner> reorderedList) {
        for (int i = 0; i < reorderedList.size(); i++) {
            Partner p = reorderedList.get(i);
            int order = i + 1;
            if (p.getId() != null) {
                partnerRepository.findById(p.getId()).ifPresent(item -> {
                    item.setDisplayOrder(order);
                    partnerRepository.save(item);
                });
            }
        }
        return ResponseEntity.ok(partnerRepository.findAllByOrderByDisplayOrderAsc());
    }
}
