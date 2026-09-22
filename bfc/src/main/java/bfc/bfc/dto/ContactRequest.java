package bfc.bfc.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactRequest {
    private String fullName;
    private String email;
    private String company;
    private String phone;
    private String phonePrefix;
    private String service;
    private String message;
}
