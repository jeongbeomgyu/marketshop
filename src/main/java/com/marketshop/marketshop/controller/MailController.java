package com.marketshop.marketshop.controller;

import com.marketshop.marketshop.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

// 이메일 인증을 위한 컨트롤러
@RestController
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    @PostMapping("/mail")
    public ResponseEntity<String> MailSend(@RequestParam("mail") String mail) {
        try {
            int number = mailService.sendMail(mail);
            String num = String.valueOf(number);
            return ResponseEntity.ok(num);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending mail");
        }
    }
}
