package com.flowlist.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowlist.entity.PushSubscription;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
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

    /**
     * Sends a push notification.
     * @return true if sent successfully, false if the subscription is invalid/expired
     */
    public boolean sendNotification(PushSubscription sub, String title, String body, String url, String type) {
        if (pushService == null) return false;
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

            HttpResponse response = pushService.send(notification);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 200 && statusCode < 300) {
                log.debug("Push sent to {} — status {}", sub.getEndpoint(), statusCode);
                return true;
            } else {
                log.warn("Push rejected for {} — FCM status {}: {}", sub.getEndpoint(), statusCode,
                    response.getStatusLine().getReasonPhrase());
                return false;
            }
        } catch (Exception e) {
            log.warn("Failed to send push to {} — {}", sub.getEndpoint(), e.getMessage());
            return false;
        }
    }
}
