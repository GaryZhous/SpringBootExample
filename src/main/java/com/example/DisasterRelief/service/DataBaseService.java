package com.example.DisasterRelief.service;

/*mockup database service using jackson databind*/
import com.example.DisasterRelief.Entity.Subscription;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataBaseService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final File file = new File("data.json");

    @Cacheable("subscriptions")
    public List<Subscription> getAllSubscriptions() {
        return readFromFile();
    }

    @CacheEvict(value = "subscriptions", allEntries = true)
    public void saveSubscription(Subscription subscription) {
        List<Subscription> subscriptions = readFromFile();
        subscriptions.add(subscription);
        writeToFile(subscriptions);
    }

    @CacheEvict(value = "subscriptions", allEntries = true)
    public boolean deleteSubscription(String email) {
        List<Subscription> subscriptions = readFromFile();
        boolean removed = subscriptions.removeIf(sub -> sub.getEmail().equals(email));
        if (removed) {
            writeToFile(subscriptions);
        }
        return removed;
    }

    private List<Subscription> readFromFile() {
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

    private void writeToFile(List<Subscription> subscriptions) {
        try {
            objectMapper.writeValue(file, subscriptions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
