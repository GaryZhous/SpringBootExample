package com.example.DisasterRelief.repository;

import com.example.DisasterRelief.Entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    int deleteByEmail(String email);
}
