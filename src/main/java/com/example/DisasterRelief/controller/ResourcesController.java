package com.example.DisasterRelief.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.DisasterRelief.service.EmailService;

import jakarta.mail.MessagingException;

@RestController
@RequestMapping("/api")
public class ResourcesController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send-request")
    public ResponseEntity<Map<String, String>> handleRequest(@RequestBody Map<String, Object> requestData) throws MessagingException {
        String htmlContent = "<html>" +
        "<body style='margin:0; padding:0; font-family: Arial, sans-serif; background: url(\"https://publichealth.jhu.edu/sites/default/files/styles/article_feature_retina/public/2024-07/flood-cat.jpg\") no-repeat center center fixed; background-size: cover;'>" +

        "<div style='max-width: 600px; margin: 20px auto; background: rgba(255, 255, 255, 0.9); padding: 20px; border-radius: 10px; box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);'>" +

        "<h2 style='color: blue; text-align: center;'>Urgent Disaster Relief Request</h2>" +
        "<p style='font-size: 16px; color: #333;'>Dear Local Authorities,</p>" +
        "<p style='font-size: 16px; color: #333;'>We are reaching out to formally request disaster relief resources for affected individuals in our community at location 25 Grenville St. Below is the list of immediate necessities:</p>" +
        
        "<ul style='font-size: 16px; color: #555;'>" +
        "<li><b>Towels:</b> " + 98 + " units</li>" +
        "<li><b>Instant Noodles:</b> " + 27 + " packs</li>" +
        "<li><b>Tissue Paper:</b> " + 11 + " rolls</li>" +
        "<li><b>Water:</b> " + 55 + " bottles</li>" +
        "</ul>" +

        "<p style='font-size: 16px; color: #333;'>Your prompt assistance in providing these essential supplies will be crucial in supporting those in urgent need.</p>" +
        
        "<div style='text-align: center; margin-top: 20px;'>" +
        "<a href='http://100.66.67.100:8088/exportRequest' style='background: blue; color: white; padding: 10px 20px; font-size: 16px; text-decoration: none; border-radius: 5px; display: inline-block;'>Download Excel Report</a>" +
        "</div>" +

        "<p style='font-size: 16px; color: #333; text-align: center; margin-top: 20px;'>Thank you for your immediate attention to this matter.</p>" +
        "</div>" +
        "</body>" +
        "</html>";
        emailService.sendEmail("sihan.zhou@mail.utoronto.ca", "hello", htmlContent);
        return ResponseEntity.ok(Map.of("message", "Request received successfully!"));
    }
}
