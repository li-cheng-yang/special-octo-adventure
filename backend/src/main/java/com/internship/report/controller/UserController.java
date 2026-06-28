package com.internship.report.controller;

import com.internship.report.dto.*;
import com.internship.report.entity.User;
import com.internship.report.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(@AuthenticationPrincipal Long userId) {
        UserDTO user = userService.getCurrentUser(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> updateCurrentUser(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        UserDTO user = userService.updateUser(userId, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", user));
    }

    @PostMapping("/me/avatar")
    public ResponseEntity<ApiResponse<UserDTO>> uploadAvatar(
            @AuthenticationPrincipal Long userId,
            @RequestParam("file") MultipartFile file) {
        UserDTO user = userService.updateAvatar(userId, file);
        return ResponseEntity.ok(ApiResponse.success("头像上传成功", user));
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        User.Role roleEnum = role != null ? User.Role.valueOf(role.toUpperCase()) : User.Role.STUDENT;
        var userPage = userService.getUsers(roleEnum, keyword, page, size);

        PageResponse<UserDTO> response = new PageResponse<>(
                userPage.getContent(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.getNumber()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/teachers")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllTeachers() {
        List<UserDTO> teachers = userService.getAllTeachers();
        return ResponseEntity.ok(ApiResponse.success(teachers));
    }

    @GetMapping("/students/by-teacher/{teacherId}")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getStudentsByTeacher(@PathVariable Long teacherId) {
        List<UserDTO> students = userService.getStudentsByTeacher(teacherId);
        return ResponseEntity.ok(ApiResponse.success(students));
    }


    @GetMapping("/students/by-teacher/{teacherId}/department")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getStudentsByTeacherDepartment(@PathVariable Long teacherId) {
        List<UserDTO> students = userService.getStudentsByTeacherWithDepartment(teacherId);
        return ResponseEntity.ok(ApiResponse.success(students));
    }


    @GetMapping("/students/by-teacher/{teacherId}/search")
    public ResponseEntity<ApiResponse<List<UserDTO>>> searchStudents(
            @PathVariable Long teacherId,
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String className,
            @RequestParam(required = false, defaultValue = "false") boolean useDepartmentFilter) {
        List<UserDTO> students = userService.searchStudents(teacherId, studentNo, realName, major, className, useDepartmentFilter);
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam String status) {
        userService.updateUserStatus(userId, User.Status.valueOf(status.toUpperCase()));
        return ResponseEntity.ok(ApiResponse.success("状态更新成功", null));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserStats>> getUserStats() {
        UserStats stats = new UserStats(
                userService.countByRole(User.Role.STUDENT),
                userService.countByRole(User.Role.TEACHER),
                userService.countByRole(User.Role.ADMIN)
        );
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    public record PageResponse<T>(List<T> content, long totalElements, int totalPages, int currentPage) {}

    public record UserStats(long studentCount, long teacherCount, long adminCount) {}
}
