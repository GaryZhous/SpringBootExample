package com.example.DisasterRelief.service;

import com.example.DisasterRelief.Entity.Subscription;
import com.example.DisasterRelief.repository.SubscriptionRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DataBaseService {

    private final SubscriptionRepository subscriptionRepository;

    public DataBaseService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Cacheable("subscriptions")
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    @CacheEvict(value = "subscriptions", allEntries = true)
    @Transactional
    public void saveSubscription(Subscription subscription) {
        subscriptionRepository.save(subscription);
    }

    @CacheEvict(value = "subscriptions", allEntries = true)
    @Transactional
    public boolean deleteSubscription(String email) {
        int deleted = subscriptionRepository.deleteByEmail(email);
        return deleted > 0;
    }
}
