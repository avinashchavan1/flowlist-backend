package com.flowlist.repository;

import com.flowlist.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    List<PushSubscription> findByUserId(Long userId);

    Optional<PushSubscription> findByEndpoint(String endpoint);

    @Transactional
    void deleteByEndpoint(String endpoint);

    @Query("SELECT s FROM PushSubscription s WHERE s.dueSoon = true")
    List<PushSubscription> findDueSoonSubscribers();

    @Query("SELECT s FROM PushSubscription s WHERE s.overdueAlerts = true")
    List<PushSubscription> findOverdueSubscribers();

    @Query("SELECT s FROM PushSubscription s WHERE s.dailyDigest = true")
    List<PushSubscription> findDailyDigestSubscribers();
}
