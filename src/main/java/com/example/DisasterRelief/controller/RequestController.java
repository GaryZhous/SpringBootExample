package com.example.DisasterRelief.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RequestController {

    @GetMapping("/request")
    public String showRequestPage() {
        return "request";
    }
}