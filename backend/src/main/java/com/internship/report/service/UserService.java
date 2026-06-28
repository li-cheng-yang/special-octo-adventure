package com.internship.report.service;

import com.internship.report.dto.*;
import com.internship.report.entity.User;
import com.internship.report.exception.BusinessException;
import com.internship.report.repository.UserRepository;
import com.internship.report.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Value("${file.upload-dir:/tmp/uploads}")
    private String uploadDir;

    @Transactional
    public JwtResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        if (user.getStatus() == User.Status.INACTIVE) {
            throw new BusinessException("账号已被禁用");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole().name());

        return new JwtResponse(token, user.getId(), user.getUsername(), user.getRealName(), user.getRole().name());
    }

    @Transactional
    public JwtResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        User.Role role;
        try {
            role = User.Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的角色类型");
        }

        if (role == User.Role.STUDENT && request.getStudentNo() == null) {
            throw new BusinessException("学生必须填写学号");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(role);
        user.setStudentNo(request.getStudentNo());
        user.setDepartment(request.getDepartment());
        user.setMajor(request.getMajor());
        user.setClassName(request.getClassName());
        user.setStatus(User.Status.ACTIVE);

        user = userRepository.save(user);

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole().name());

        return new JwtResponse(token, user.getId(), user.getUsername(), user.getRealName(), user.getRole().name());
    }

    public UserDTO getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return convertToDTO(user);
    }

    @Transactional
    public UserDTO updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (request.getRealName() != null) user.setRealName(request.getRealName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getDepartment() != null) user.setDepartment(request.getDepartment());
        if (request.getMajor() != null) user.setMajor(request.getMajor());
        if (request.getClassName() != null) user.setClassName(request.getClassName());

        user = userRepository.save(user);
        return convertToDTO(user);
    }

    @Transactional
    public UserDTO updateAvatar(Long userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("只能上传图片文件");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        try {
            Path uploadPath = Paths.get(uploadDir, "avatars");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String storedName = "avatar_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

            Path filePath = uploadPath.resolve(storedName);
            file.transferTo(filePath.toFile());

            // 删除旧头像
            if (user.getAvatar() != null) {
                try {
                    Path oldPath = Paths.get(uploadDir).resolve(user.getAvatar().replace("/api/files/avatar/", ""));
                    Files.deleteIfExists(oldPath);
                } catch (IOException ignored) {}
            }

            user.setAvatar("/api/files/avatar/" + storedName);
            user = userRepository.save(user);
            return convertToDTO(user);

        } catch (IOException e) {
            throw new BusinessException("头像上传失败: " + e.getMessage());
        }
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public Page<UserDTO> getUsers(User.Role role, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());

        Page<User> userPage;
        if (keyword != null && !keyword.isEmpty()) {
            userPage = userRepository.searchByRoleAndKeyword(role, keyword, pageable);
        } else {
            userPage = userRepository.findByRole(role, pageable);
        }

        return userPage.map(this::convertToDTO);
    }

    public List<UserDTO> getStudentsByTeacher(Long teacherId) {
        List<User> students = userRepository.findStudentsByTeacherId(teacherId);
        return students.stream()
                .filter(u -> u.getRole() == User.Role.STUDENT)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    public List<UserDTO> getStudentsByTeacherWithDepartment(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new BusinessException("导师不存在"));

        if (teacher.getDepartment() == null || teacher.getDepartment().isEmpty()) {
            // 导师没有设置院系，返回所有学生
            return getStudentsByTeacher(teacherId);
        }

        List<User> students = userRepository.findStudentsByTeacherIdAndDepartment(
                teacherId, teacher.getDepartment());
        return students.stream()
                .filter(u -> u.getRole() == User.Role.STUDENT)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    public List<UserDTO> searchStudents(Long teacherId, String studentNo, String realName,
                                         String major, String className, boolean useDepartmentFilter) {
        List<User> students;

        if (useDepartmentFilter) {
            User teacher = userRepository.findById(teacherId)
                    .orElseThrow(() -> new BusinessException("导师不存在"));
            String department = (teacher.getDepartment() != null) ? teacher.getDepartment() : null;

            if (department == null || department.isEmpty()) {
                students = userRepository.searchStudentsByTeacherId(teacherId, studentNo, realName, major, className);
            } else {
                students = userRepository.searchStudentsByTeacherIdAndDepartment(
                        teacherId, department, studentNo, realName, major, className);
            }
        } else {
            students = userRepository.searchStudentsByTeacherId(teacherId, studentNo, realName, major, className);
        }

        return students.stream()
                .filter(u -> u.getRole() == User.Role.STUDENT)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getAllTeachers() {
        List<User> teachers = userRepository.findByRoleAndStatus(User.Role.TEACHER, User.Status.ACTIVE);
        return teachers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateUserStatus(Long userId, User.Status status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setStatus(status);
        userRepository.save(user);
    }

    public long countByRole(User.Role role) {
        return userRepository.countByRole(role);
    }

    private UserDTO convertToDTO(User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .studentNo(user.getStudentNo())
                .department(user.getDepartment())
                .major(user.getMajor())
                .className(user.getClassName())
                .avatar(user.getAvatar())
                .status(user.getStatus().name())
                .createTime(user.getCreateTime() != null ? user.getCreateTime().format(formatter) : null)
                .build();
    }
}
