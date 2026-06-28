package com.internship.report.service;

import com.internship.report.entity.Internship;
import com.internship.report.entity.Notification;
import com.internship.report.entity.User;
import com.internship.report.repository.InternshipRepository;
import com.internship.report.repository.LocationRecordRepository;
import com.internship.report.repository.NotificationRepository;
import com.internship.report.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTaskService {

    private final InternshipRepository internshipRepository;
    private final LocationRecordRepository locationRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 每天早上9点提醒未打卡的学生
     */
    @Scheduled(cron = "0 0 9 * * MON-FRI")
    @Transactional
    public void remindUncheckedStudents() {
        log.info("[定时任务] 开始提醒未打卡学生");
        List<User> students = userRepository.findByRole(User.Role.STUDENT);
        LocalDate today = LocalDate.now();

        for (User student : students) {
            // 检查今天是否已打卡
            boolean hasCheckedIn = locationRepository.existsByStudentIdAndRecordTimeBetween(
                    student.getId(),
                    today.atStartOfDay(),
                    today.plusDays(1).atStartOfDay()
            );

            if (!hasCheckedIn) {
                // 发送提醒通知
                Notification notification = new Notification();
                notification.setRecipient(student);
                notification.setTitle("打卡提醒");
                notification.setContent("您好，今天是" + today + "，您还没有进行打卡签到，请及时完成。");
                notification.setType(Notification.NotificationType.CHECKIN_REMIND);
                notification.setIsRead(false);
                notificationRepository.save(notification);
                log.info("[定时任务] 已发送打卡提醒给学生: {}", student.getRealName());
            }
        }
    }

    /**
     * 每天凌晨2点自动归档已完成的实习
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void autoArchiveCompletedInternships() {
        log.info("[定时任务] 开始归档已完成实习");
        List<Internship> ongoingInternships = internshipRepository.findByStatus(Internship.Status.ONGOING);
        LocalDate today = LocalDate.now();

        for (Internship internship : ongoingInternships) {
            if (internship.getEndDate() != null && internship.getEndDate().isBefore(today)) {
                internship.setStatus(Internship.Status.COMPLETED);
                internshipRepository.save(internship);

                // 通知学生
                Notification notification = new Notification();
                notification.setRecipient(internship.getStudent());
                notification.setTitle("实习已自动归档");
                notification.setContent("您的实习（" + internship.getCompanyName() + " - " + internship.getPosition() + "）已自动标记为完成。");
                notification.setType(Notification.NotificationType.SYSTEM_NOTICE);
                notification.setIsRead(false);
                notificationRepository.save(notification);
                log.info("[定时任务] 已归档实习: {}", internship.getId());
            }
        }
    }

    /**
     * 每周五下午5点发送周报提醒
     */
    @Scheduled(cron = "0 0 17 * * FRI")
    @Transactional
    public void remindWeeklyReport() {
        log.info("[定时任务] 开始发送周报提醒");
        List<User> students = userRepository.findByRole(User.Role.STUDENT);

        for (User student : students) {
            Notification notification = new Notification();
            notification.setRecipient(student);
            notification.setTitle("周报提醒");
            notification.setContent("本周即将结束，请记得提交您的周报。");
            notification.setType(Notification.NotificationType.DEADLINE_WARNING);
            notification.setIsRead(false);
            notificationRepository.save(notification);
        }
    }
}
