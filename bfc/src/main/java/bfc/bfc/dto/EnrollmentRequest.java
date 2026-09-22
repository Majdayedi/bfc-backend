package bfc.bfc.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentRequest {
    private String fullName;
    private String email;
    private String country;
    private String phone;
    private String jobTitle;
    private String organization;
    private List<String> courses;
    private String message;
}
