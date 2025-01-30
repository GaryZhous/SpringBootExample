package com.example.DisasterRelief.service;

/*mockup database service using jackson databind*/
import com.example.DisasterRelief.Entity.Resources;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MockDatabaseService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final File file = new File("data.json");

    public List<Subscription> getAllSubscriptions() {
        try {
            if (!file.exists()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(file, new TypeReference<>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void saveSubscription(Subscription subscription) {
        List<Subscription> subscriptions = getAllSubscriptions();
        subscriptions.add(subscription);
        writeToFile(subscriptions);
    }

    public boolean deleteSubscription(String email) {
        List<Subscription> subscriptions = getAllSubscriptions();
        boolean removed = subscriptions.removeIf(sub -> sub.getEmail().equals(email));
        if (removed) {
            writeToFile(subscriptions);
        }
        return removed;
    }

    private void writeToFile(List<Subscription> subscriptions) {
        try {
            objectMapper.writeValue(file, subscriptions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
