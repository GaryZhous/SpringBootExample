package com.example.DisasterRelief.service;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class IdempotencyService {

    private final Cache idempotencyCache;

    public IdempotencyService(CacheManager cacheManager) {
        this.idempotencyCache = cacheManager.getCache("idempotency");
    }

    public Optional<Map<String, String>> findByKey(String idempotencyKey) {
        Cache.ValueWrapper wrapper = idempotencyCache.get(idempotencyKey);
        if (wrapper != null) {
            @SuppressWarnings("unchecked")
            Map<String, String> response = (Map<String, String>) wrapper.get();
            return Optional.ofNullable(response);
        }
        return Optional.empty();
    }

    public void store(String idempotencyKey, Map<String, String> response) {
        idempotencyCache.put(idempotencyKey, response);
    }
}
