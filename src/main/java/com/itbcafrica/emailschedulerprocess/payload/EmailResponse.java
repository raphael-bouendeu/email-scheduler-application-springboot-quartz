package com.itbcafrica.emailschedulerprocess.payload;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailResponse {
    private boolean success;
    private String jobId;
    private String jobGroup;
    private String message;
}
