package bfc.bfc.controllers;

import bfc.bfc.dto.EnrollmentRequest;
import bfc.bfc.entities.Enrollment;
import bfc.bfc.repositories.EnrollmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @PostMapping("/submit")
    public ResponseEntity<Enrollment> submitEnrollment(@RequestBody EnrollmentRequest request) {
        try {
            // Convert courses list to JSON string
            ObjectMapper mapper = new ObjectMapper();
            String coursesJson = mapper.writeValueAsString(request.getCourses());

            Enrollment enrollment = Enrollment.builder()
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .country(request.getCountry())
                    .phone(request.getPhone())
                    .jobTitle(request.getJobTitle())
                    .organization(request.getOrganization())
                    .courses(coursesJson)
                    .message(request.getMessage())
                    .build();

            Enrollment saved = enrollmentRepository.save(enrollment);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<Enrollment>> getAllEnrollments() {
        return ResponseEntity.ok(enrollmentRepository.findAll());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Enrollment> updateEnrollment(@PathVariable Long id, @RequestBody EnrollmentRequest request) {
        var existingOpt = enrollmentRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var existing = existingOpt.get();
        try {
            ObjectMapper mapper = new ObjectMapper();
            String coursesJson = mapper.writeValueAsString(request.getCourses());

            existing.setFullName(request.getFullName());
            existing.setEmail(request.getEmail());
            existing.setCountry(request.getCountry());
            existing.setPhone(request.getPhone());
            existing.setJobTitle(request.getJobTitle());
            existing.setOrganization(request.getOrganization());
            existing.setCourses(coursesJson);
            existing.setMessage(request.getMessage());

            return ResponseEntity.ok(enrollmentRepository.save(existing));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Long id) {
        if (enrollmentRepository.existsById(id)) {
            enrollmentRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
