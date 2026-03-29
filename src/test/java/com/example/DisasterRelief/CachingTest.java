package com.example.DisasterRelief;

import com.example.DisasterRelief.Entity.Subscription;
import com.example.DisasterRelief.service.DataBaseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CachingTest {

    @Autowired
    private DataBaseService dataBaseService;

    @Autowired
    private CacheManager cacheManager;

    @AfterEach
    void evictAllCaches() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
        File dataFile = new File("data.json");
        if (dataFile.exists() && !dataFile.delete()) {
            dataFile.deleteOnExit();
        }
    }

    @Test
    void getAllSubscriptionsIsCached() {
        dataBaseService.getAllSubscriptions();

        assertNotNull(cacheManager.getCache("subscriptions").get(SimpleKey.EMPTY),
                "subscriptions cache should be populated after first call");
    }

    @Test
    void saveSubscriptionEvictsCache() {
        dataBaseService.getAllSubscriptions();
        assertNotNull(cacheManager.getCache("subscriptions").get(SimpleKey.EMPTY));

        dataBaseService.saveSubscription(new Subscription("Bob", "bob@example.com", "456 Ave"));

        assertNull(cacheManager.getCache("subscriptions").get(SimpleKey.EMPTY),
                "subscriptions cache should be evicted after save");
    }

    @Test
    void deleteSubscriptionEvictsCache() {
        dataBaseService.saveSubscription(new Subscription("Alice", "alice@example.com", "123 St"));
        dataBaseService.getAllSubscriptions();
        assertNotNull(cacheManager.getCache("subscriptions").get(SimpleKey.EMPTY));

        dataBaseService.deleteSubscription("alice@example.com");

        assertNull(cacheManager.getCache("subscriptions").get(SimpleKey.EMPTY),
                "subscriptions cache should be evicted after delete");
    }

    @Test
    void cachedResultIsReturnedOnSubsequentCalls() {
        dataBaseService.saveSubscription(new Subscription("Carol", "carol@example.com", "789 Blvd"));

        var first = dataBaseService.getAllSubscriptions();
        var second = dataBaseService.getAllSubscriptions();

        assertSame(first, second, "second call should return the same cached list instance");
    }
}
