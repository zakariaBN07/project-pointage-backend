package com.example.pointage_backend.repository;

import com.example.pointage_backend.model.Remarque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RemarqueRepository extends JpaRepository<Remarque, Long> {
    List<Remarque> findByReceiverRoleOrReceiverIdOrderByTimestampDesc(String receiverRole, Long receiverId);
    List<Remarque> findByReceiverIdOrderByTimestampDesc(Long receiverId);
    List<Remarque> findBySenderIdOrderByTimestampDesc(Long senderId);
    List<Remarque> findAllByOrderByTimestampDesc();
}
