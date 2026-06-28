package com.internship.report.service;

import com.internship.report.dto.NotificationDTO;
import com.internship.report.entity.Notification;
import com.internship.report.entity.User;
import com.internship.report.repository.NotificationRepository;
import com.internship.report.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public Notification createNotification(Long recipientId, String title, String content,
                                           Notification.NotificationType type, String relatedType, Long relatedId) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedType(relatedType);
        notification.setRelatedId(relatedId);
        notification.setIsRead(false);

        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotifications(Long recipientId, int page, int size) {
        Page<Notification> notifications = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(recipientId, PageRequest.of(page, size));
        return notifications.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getLatestNotifications(Long recipientId) {
        return notificationRepository.findTop10ByRecipientIdOrderByCreatedAtDesc(recipientId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long recipientId) {
        return notificationRepository.countUnreadByRecipientId(recipientId);
    }

    @Transactional
    public void markAllAsRead(Long recipientId) {
        notificationRepository.markAllAsReadByRecipientId(recipientId);
    }

    private NotificationDTO convertToDTO(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setContent(n.getContent());
        dto.setType(n.getType().name());
        dto.setRelatedType(n.getRelatedType());
        dto.setRelatedId(n.getRelatedId());
        dto.setIsRead(n.getIsRead());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}
