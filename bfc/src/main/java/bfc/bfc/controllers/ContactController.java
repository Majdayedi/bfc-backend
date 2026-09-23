package bfc.bfc.controllers;

import bfc.bfc.dto.ContactRequest;
import bfc.bfc.entities.ContactMessage;
import bfc.bfc.entities.ContactServiceOption;
import bfc.bfc.repositories.ContactRepository;
import bfc.bfc.repositories.ContactServiceOptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ContactServiceOptionRepository contactServiceOptionRepository;

    @GetMapping("/service-options")
    public ResponseEntity<List<ContactServiceOption>> getServiceOptions() {
        return ResponseEntity.ok(contactServiceOptionRepository.findAllByOrderByDisplayOrderAsc());
    }

    @PostMapping("/submit")
    public ResponseEntity<ContactMessage> submitContact(@RequestBody ContactRequest request) {
        try {
            ContactMessage message = ContactMessage.builder()
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .company(request.getCompany())
                    .phone(request.getPhone())
                    .phonePrefix(request.getPhonePrefix())
                    .service(request.getService())
                    .message(request.getMessage())
                    .build();

            ContactMessage saved = contactRepository.save(message);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<ContactMessage>> getAllMessages() {
        return ResponseEntity.ok(contactRepository.findAll());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ContactMessage> updateMessage(@PathVariable Long id, @RequestBody ContactRequest request) {
        var existingOpt = contactRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var existing = existingOpt.get();
        try {
            existing.setFullName(request.getFullName());
            existing.setEmail(request.getEmail());
            existing.setCompany(request.getCompany());
            existing.setPhone(request.getPhone());
            existing.setPhonePrefix(request.getPhonePrefix());
            existing.setService(request.getService());
            existing.setMessage(request.getMessage());

            return ResponseEntity.ok(contactRepository.save(existing));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        if (contactRepository.existsById(id)) {
            contactRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
