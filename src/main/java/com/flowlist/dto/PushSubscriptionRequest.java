package com.flowlist.dto;

public class PushSubscriptionRequest {

    private String endpoint;
    private String p256dh;
    private String auth;
    private boolean dueSoon = true;
    private boolean overdueAlerts = true;
    private boolean dailyDigest = true;

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getP256dh() { return p256dh; }
    public void setP256dh(String p256dh) { this.p256dh = p256dh; }

    public String getAuth() { return auth; }
    public void setAuth(String auth) { this.auth = auth; }

    public boolean isDueSoon() { return dueSoon; }
    public void setDueSoon(boolean dueSoon) { this.dueSoon = dueSoon; }

    public boolean isOverdueAlerts() { return overdueAlerts; }
    public void setOverdueAlerts(boolean overdueAlerts) { this.overdueAlerts = overdueAlerts; }

    public boolean isDailyDigest() { return dailyDigest; }
    public void setDailyDigest(boolean dailyDigest) { this.dailyDigest = dailyDigest; }
}
