package com.example.DisasterRelief.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import com.example.DisasterRelief.service.EmailService;

import jakarta.mail.MessagingException;

@RestController
@RequestMapping("/api")
public class ResourcesController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send-request")
    public ResponseEntity<Map<String, String>> handleRequest(@RequestBody Map<String, Object> requestData) throws MessagingException {
        String name = HtmlUtils.htmlEscape((String) requestData.getOrDefault("name", "Unknown"));
        String address = HtmlUtils.htmlEscape((String) requestData.getOrDefault("address", "Unknown"));
        int towel = safeParseInt(requestData.get("towel"));
        int instantNoodles = safeParseInt(requestData.get("instantNoodles"));
        int tissuePaper = safeParseInt(requestData.get("tissuePaper"));
        int water = safeParseInt(requestData.get("water"));

        String htmlContent = "<html>" +
        "<body style='margin:0; padding:0; font-family: Arial, sans-serif;'>" +

        "<div style='max-width: 600px; margin: 20px auto; background: rgba(255, 255, 255, 0.9); padding: 20px; border-radius: 10px; box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);'>" +

        "<h2 style='color: blue; text-align: center;'>Urgent Disaster Relief Request</h2>" +
        "<p style='font-size: 16px; color: #333;'>Dear Local Authorities,</p>" +
        "<p style='font-size: 16px; color: #333;'>We are reaching out to formally request disaster relief resources for affected individuals in our community. " +
        "Requestor: <b>" + name + "</b> at location: <b>" + address + "</b>. Below is the list of immediate necessities:</p>" +

        "<ul style='font-size: 16px; color: #555;'>" +
        "<li><b>Towels:</b> " + towel + " units</li>" +
        "<li><b>Instant Noodles:</b> " + instantNoodles + " packs</li>" +
        "<li><b>Tissue Paper:</b> " + tissuePaper + " rolls</li>" +
        "<li><b>Water:</b> " + water + " bottles</li>" +
        "</ul>" +

        "<p style='font-size: 16px; color: #333;'>Your prompt assistance in providing these essential supplies will be crucial in supporting those in urgent need.</p>" +

        "<p style='font-size: 16px; color: #333; text-align: center; margin-top: 20px;'>Thank you for your immediate attention to this matter.</p>" +
        "</div>" +
        "</body>" +
        "</html>";
        emailService.sendEmail("sihan.zhou@mail.utoronto.ca", "Disaster Relief Request from " + name, htmlContent);
        return ResponseEntity.ok(Map.of("message", "Request received successfully!"));
    }

    private int safeParseInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
