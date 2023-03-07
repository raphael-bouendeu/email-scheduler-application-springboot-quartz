package com.itbcafrica.emailschedulerprocess.payload;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Setter
@Getter
public class EmailRequest {
    @Email
    @NotEmpty
    private String email;

    @NotEmpty
    private String subject;

    @NotEmpty
    private String body;


    @CreationTimestamp
    private LocalDateTime dateTime;


    private ZoneId timeZone;
}
