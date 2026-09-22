package bfc.bfc.controllers;

import bfc.bfc.repositories.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping("/api/test-error")
    public String testError() {
        try {
            courseRepository.findAll();
            return "Success";
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append(e.toString()).append("\n");
            for (StackTraceElement element : e.getStackTrace()) {
                sb.append(element.toString()).append("\n");
            }
            Throwable cause = e.getCause();
            if (cause != null) {
                sb.append("Caused by: ").append(cause.toString()).append("\n");
                for (StackTraceElement element : cause.getStackTrace()) {
                    sb.append(element.toString()).append("\n");
                }
            }
            return sb.toString();
        }
    }
}
