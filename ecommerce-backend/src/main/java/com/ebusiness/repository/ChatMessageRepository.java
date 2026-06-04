package com.ebusiness.repository;

import com.ebusiness.entity.ChatMessage;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

  @Query("SELECT m FROM ChatMessage m WHERE (m.senderId = :userId1 AND m.receiverId = :userId2) OR (m.senderId = :userId2 AND m.receiverId = :userId1) ORDER BY m.createdAt DESC")
  List<ChatMessage> findConversation(Long userId1, Long userId2);

  @Query("SELECT m FROM ChatMessage m WHERE (m.senderId = :userId1 AND m.receiverId = :userId2) OR (m.senderId = :userId2 AND m.receiverId = :userId1) ORDER BY m.createdAt DESC")
  Page<ChatMessage> findConversationPaged(Long userId1, Long userId2, Pageable pageable);

  @Query("SELECT DISTINCT CASE WHEN m.senderId = :userId THEN m.receiverId ELSE m.senderId END FROM ChatMessage m WHERE m.senderId = :userId OR m.receiverId = :userId")
  List<Long> findConversationPartners(Long userId);
}
