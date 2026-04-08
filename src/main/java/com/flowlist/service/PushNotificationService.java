package com.flowlist.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowlist.entity.PushSubscription;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

@Service
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    @Value("${vapid.public.key}")
    private String vapidPublicKey;

    @Value("${vapid.private.key}")
    private String vapidPrivateKey;

    @Value("${vapid.subject}")
    private String vapidSubject;

    private PushService pushService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        try {
            pushService = new PushService(vapidPublicKey, vapidPrivateKey, vapidSubject);
        } catch (Exception e) {
            log.error("Failed to initialize PushService: {}", e.getMessage());
        }
    }

    public String getVapidPublicKey() {
        return vapidPublicKey;
    }

    public void sendNotification(PushSubscription sub, String title, String body, String url, String type) {
        if (pushService == null) return;
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("title", title);
            payload.put("body", body);
            payload.put("url", url != null ? url : "/");
            payload.put("type", type);
            payload.put("icon", "/icon-192.png");
            payload.put("badge", "/badge-72.png");

            String json = objectMapper.writeValueAsString(payload);

            Notification notification = new Notification(
                sub.getEndpoint(),
                sub.getP256dh(),
                sub.getAuth(),
                json.getBytes()
            );

            pushService.send(notification);
        } catch (Exception e) {
            log.warn("Failed to send push to {}: {}", sub.getEndpoint(), e.getMessage());
        }
    }
}
