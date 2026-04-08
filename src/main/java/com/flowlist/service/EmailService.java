package com.flowlist.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${app.mail.from:Drift <onboarding@resend.dev>}")
    private String fromAddress;

    @Value("${app.base-url}")
    private String baseUrl;

    private final RestClient restClient = RestClient.create("https://api.resend.com");

    public void sendPasswordReset(String toEmail, String token) {
        String resetLink = baseUrl + "?reset=" + token;

        String html = """
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:0;background:#f5f5f5;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;">
              <div style="max-width:480px;margin:40px auto;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.08);">
                <!-- Header gradient -->
                <div style="height:6px;background:linear-gradient(90deg,#405DE6,#833AB4,#C13584,#E1306C,#FD1D1D,#FCAF45);"></div>
                <div style="padding:40px 32px;">
                  <!-- Logo -->
                  <div style="text-align:center;margin-bottom:28px;">
                    <div style="display:inline-block;width:52px;height:52px;border-radius:14px;background:linear-gradient(45deg,#405DE6,#833AB4,#C13584,#E1306C,#FD1D1D,#FCAF45);line-height:52px;text-align:center;">
                      <span style="color:#fff;font-size:24px;font-weight:700;vertical-align:middle;">D</span>
                    </div>
                    <h1 style="font-size:20px;font-weight:700;color:#000000;margin:12px 0 0;letter-spacing:-0.02em;">Drift</h1>
                  </div>
                  <!-- Body -->
                  <h2 style="font-size:18px;font-weight:600;color:#000000;margin:0 0 10px;text-align:center;">Reset your password</h2>
                  <p style="font-size:14px;color:#737373;margin:0 0 28px;text-align:center;line-height:1.6;">
                    Someone requested a password reset for your Drift account.<br>Click the button below to set a new one.
                  </p>
                  <!-- CTA -->
                  <a href="%s"
                     style="display:block;text-align:center;padding:14px 24px;border-radius:10px;background:linear-gradient(45deg,#405DE6,#833AB4,#C13584,#E1306C,#FD1D1D,#FCAF45);color:#ffffff;font-size:15px;font-weight:600;text-decoration:none;margin-bottom:20px;">
                    Reset Password
                  </a>
                  <!-- Fallback link -->
                  <p style="font-size:12px;color:#8E8E8E;text-align:center;margin:0 0 6px;">
                    Or copy this link into your browser:
                  </p>
                  <p style="font-size:11px;color:#0095F6;text-align:center;word-break:break-all;margin:0 0 24px;">%s</p>
                  <!-- Notes -->
                  <p style="font-size:12px;color:#8E8E8E;text-align:center;margin:0 0 4px;">⏱ This link expires in <strong>1 hour</strong> and can only be used once.</p>
                  <p style="font-size:12px;color:#8E8E8E;text-align:center;margin:0;">If you didn't request this, you can safely ignore this email.</p>
                  <hr style="border:none;border-top:1px solid #EFEFEF;margin:24px 0 16px;" />
                  <p style="font-size:11px;color:#C7C7C7;text-align:center;margin:0;">Drift · Stay in the flow.</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(resetLink, resetLink);

        Map<String, Object> payload = Map.of(
                "from",    fromAddress,
                "to",      List.of(toEmail),
                "subject", "Reset your Drift password",
                "html",    html
        );

        restClient.post()
                .uri("/emails")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }
}
