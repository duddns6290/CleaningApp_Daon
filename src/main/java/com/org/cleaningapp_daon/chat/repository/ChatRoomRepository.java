package com.org.cleaningapp_daon.chat.repository;

import com.org.cleaningapp_daon.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByRequestId(Long requestId);

    List<ChatRoom> findByCustomerIdOrProviderIdOrderByLastMessageAtDesc(String customerId, String providerId);
}