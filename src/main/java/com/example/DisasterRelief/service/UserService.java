package com.example.DisasterRelief.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class UserService{
    public Map<String, String> getInfo(String username){
        Map<String, String> resultMap = new HashMap<>();
        return resultMap;
    }
}