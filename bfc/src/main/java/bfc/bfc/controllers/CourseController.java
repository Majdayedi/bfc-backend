package bfc.bfc.controllers;

import bfc.bfc.entities.Course;
import bfc.bfc.repositories.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*") // Adjust based on your security setup
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping("/show")
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @GetMapping("/show/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        return courseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public Course createCourse(@RequestBody Course course) {
        return courseRepository.save(course);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody Course courseDetails) {
        return courseRepository.findById(id)
                .map(course -> {
                    course.setTitle(courseDetails.getTitle());
                    course.setInstitution(courseDetails.getInstitution());
                    course.setCountry(courseDetails.getCountry());
                    course.setYear(courseDetails.getYear());
                    course.setCategory(courseDetails.getCategory());
                    course.setTopics(courseDetails.getTopics());
                    course.setLogo(courseDetails.getLogo());
                    course.setIsAccredited(courseDetails.getIsAccredited());
                    course.setPrograms(courseDetails.getPrograms());
                    course.setAccreditation(courseDetails.getAccreditation());
                    course.setIntake(courseDetails.getIntake());
                    course.setDescription(courseDetails.getDescription());
                    course.setCertificationDescription(courseDetails.getCertificationDescription());
                    course.setBrochureUrl(courseDetails.getBrochureUrl());
                    course.setIntro(courseDetails.getIntro());
                    course.setParticipants(courseDetails.getParticipants());
                    course.setDuration(courseDetails.getDuration());
                    course.setLocation(courseDetails.getLocation());
                    course.setLanguage(courseDetails.getLanguage());
                    course.setLearnPoints(courseDetails.getLearnPoints());
                    course.setJourneySteps(courseDetails.getJourneySteps());
                    return ResponseEntity.ok(courseRepository.save(course));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        if (courseRepository.existsById(id)) {
            courseRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
