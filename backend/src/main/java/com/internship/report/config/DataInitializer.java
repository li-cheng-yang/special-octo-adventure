package com.internship.report.config;

import com.internship.report.entity.User;
import com.internship.report.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("初始化测试数据...");
            
            // 创建管理员
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRealName("系统管理员");
            admin.setEmail("admin@example.com");
            admin.setPhone("13800000000");
            admin.setRole(User.Role.ADMIN);
            admin.setStatus(User.Status.ACTIVE);
            userRepository.save(admin);
            
            // 创建导师
            User teacher1 = new User();
            teacher1.setUsername("teacher1");
            teacher1.setPassword(passwordEncoder.encode("teacher123"));
            teacher1.setRealName("张教授");
            teacher1.setEmail("zhang@example.com");
            teacher1.setPhone("13800000001");
            teacher1.setRole(User.Role.TEACHER);
            teacher1.setDepartment("计算机学院");
            teacher1.setStatus(User.Status.ACTIVE);
            userRepository.save(teacher1);
            
            User teacher2 = new User();
            teacher2.setUsername("teacher2");
            teacher2.setPassword(passwordEncoder.encode("teacher123"));
            teacher2.setRealName("李教授");
            teacher2.setEmail("li@example.com");
            teacher2.setPhone("13800000002");
            teacher2.setRole(User.Role.TEACHER);
            teacher2.setDepartment("软件学院");
            teacher2.setStatus(User.Status.ACTIVE);
            userRepository.save(teacher2);
            
            // 创建学生
            for (int i = 1; i <= 10; i++) {
                User student = new User();
                student.setUsername("student" + i);
                student.setPassword(passwordEncoder.encode("student123"));
                student.setRealName("学生" + i);
                student.setEmail("student" + i + "@example.com");
                student.setPhone("1390000000" + i);
                student.setRole(User.Role.STUDENT);
                student.setStudentNo("2021000" + i);
                student.setDepartment("计算机学院");
                student.setMajor("软件工程");
                student.setClassName("软件2101班");
                student.setStatus(User.Status.ACTIVE);
                userRepository.save(student);
            }
            
            log.info("测试数据初始化完成！");
            log.info("管理员账号: admin / admin123");
            log.info("导师账号: teacher1 / teacher123");
            log.info("学生账号: student1 / student123");
        }
    }
}
