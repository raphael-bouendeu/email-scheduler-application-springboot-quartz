package com.itbcafrica.emailschedulerprocess.web;

import com.itbcafrica.emailschedulerprocess.payload.EmailRequest;
import com.itbcafrica.emailschedulerprocess.payload.EmailResponse;
import com.itbcafrica.emailschedulerprocess.quartz.job.EmailJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@RestController
public class EmailSchedulerController {

    @Autowired
    private Scheduler scheduler;

    @PostMapping("/schedule/email")
    public ResponseEntity<EmailResponse> scheduleEmail(@Valid @RequestBody EmailRequest emailRequest) {
        try {
            ZonedDateTime dateTime = ZonedDateTime.of ( emailRequest.getDateTime (), emailRequest.getTimeZone () );
            if (dateTime.isBefore ( ZonedDateTime.now () )) {
                EmailResponse emailResponse = EmailResponse.builder ()
                        .success ( false )
                        .message ( "dateTime must be after current time" )
                        .build ();
                ResponseEntity<EmailResponse> emailResponseResponseEntity = ResponseEntity.status ( HttpStatus.INTERNAL_SERVER_ERROR ).body ( emailResponse );
                return emailResponseResponseEntity;
            }
            JobDetail jobDetail = buildJobDetail ( emailRequest );
            Trigger trigger = buildTrigger ( jobDetail, dateTime );
            scheduler.scheduleJob ( jobDetail, trigger );
            EmailResponse emailResponse = EmailResponse.builder ()
                    .success ( true )
                    .jobId ( jobDetail.getKey ().getName () )
                    .jobGroup ( jobDetail.getKey ().getGroup () )
                    .message ( "Email Scheduled Successfully !" )
                    .build ();
            return ResponseEntity.ok ( emailResponse );
        } catch (SchedulerException se) {
            log.error ( "Error while scheduling email :", se );
            EmailResponse emailResponse = EmailResponse.builder ().success ( false )
                    .message ( "Error while scheduling email. please try again later" )
                    .build ();
            ResponseEntity<EmailResponse> emailResponseResponseEntity = ResponseEntity.status ( HttpStatus.INTERNAL_SERVER_ERROR ).body ( emailResponse );
            return emailResponseResponseEntity;
        }
    }


    @GetMapping("/get")
    ResponseEntity<String> getApiTest() {
        return ResponseEntity.ok ( "Get Api Test -- pass" );
    }

    private JobDetail buildJobDetail(EmailRequest emailRequest) {
        JobDataMap jobDataMap = new JobDataMap ();
        jobDataMap.put ( "email", emailRequest.getEmail () );
        jobDataMap.put ( "subject", emailRequest.getSubject () );
        jobDataMap.put ( "body", emailRequest.getBody () );
        return JobBuilder.newJob ( EmailJob.class )
                .withIdentity ( UUID.randomUUID ().toString (), "email-jobs" )
                .withDescription ( "Send Email Job" )
                .usingJobData ( jobDataMap )
                .storeDurably ()
                .build ();
    }

    private Trigger buildTrigger(JobDetail jobDetail, ZonedDateTime startAt) {
        return TriggerBuilder.newTrigger ()
                .forJob ( jobDetail )
                .withIdentity ( jobDetail.getKey ().getName (), "email-triggers" )
                .withDescription ( "Send Email Trigger" )
                .startAt ( Date.from ( startAt.toInstant () ) )
                .withSchedule ( SimpleScheduleBuilder.simpleSchedule ().withMisfireHandlingInstructionFireNow () )
                .build ();
    }
}
