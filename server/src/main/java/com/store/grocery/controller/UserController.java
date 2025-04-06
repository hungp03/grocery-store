package com.store.grocery.controller;

import com.store.grocery.domain.User;
import com.store.grocery.dto.request.user.UpdatePasswordRequest;
import com.store.grocery.dto.request.user.UpdateUserRequest;
import com.store.grocery.dto.request.user.UserDisableOTPRequest;
import com.store.grocery.dto.request.user.UpdateUserStatusRequest;
import com.store.grocery.dto.response.PaginationResponse;
import com.store.grocery.dto.response.user.DeviceResponse;
import com.store.grocery.dto.response.user.UserResponse;
import com.store.grocery.service.UserService;
import com.store.grocery.util.annotation.ApiMessage;
import com.store.grocery.util.exception.ResourceInvalidException;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/v2")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("users/devices")
    @ApiMessage("Get logged in devices")
    public ResponseEntity<List<DeviceResponse>> getLoggedInDevices(@CookieValue(name = "device", defaultValue = "none") String deviceHash){
        return ResponseEntity.ok(this.userService.getLoggedInDevices(deviceHash));
    }

    @DeleteMapping("users/{id}")
    @ApiMessage("Delete user")
    // Not use
    public ResponseEntity<Void> deleteUser(@PathVariable("id") long id) throws ResourceInvalidException {
        this.userService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("users/{id}")
    @ApiMessage("Get user by id")
    public ResponseEntity<UserResponse> getUser(@PathVariable("id") long id) throws ResourceInvalidException {
        User currentUser = this.userService.getUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.convertToUserDTO(currentUser));
    }

    @GetMapping("users")
    @ApiMessage("Fetch users")
    public ResponseEntity<PaginationResponse> fetchAllUser(@Filter Specification<User> spec, Pageable pageable) {
        return ResponseEntity.ok(this.userService.fetchAllUser(spec, pageable));
    }

    @PutMapping("users/update-password")
    @ApiMessage("Change password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody UpdatePasswordRequest dto){
        this.userService.changePassword(dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("users/status")
    @ApiMessage("Update user status")
    public ResponseEntity<Void> updateUser(@RequestBody UpdateUserStatusRequest user) throws ResourceInvalidException {
        this.userService.updateStatus(user);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "users/account", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiMessage("Update user information")
    public ResponseEntity<Void> updateUser(
            @RequestPart("user") UpdateUserRequest request,
            @RequestPart(value = "avatarUrl", required = false) MultipartFile avatar) throws IOException {
        this.userService.updateUser(request, avatar);
        return ResponseEntity.ok().build();
    }

    @PostMapping("deactivate/account")
    @ApiMessage("Request deactivate account")
    public ResponseEntity<Void> requestDisableOTP() {
        userService.requestDeactiveAccount();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deactivate/confirm")
    @ApiMessage("Confirm deactivate account")
    public ResponseEntity<Void> confirmDisableAccount(
            @RequestBody UserDisableOTPRequest otpCode) {
        userService.verifyOTPAndDisableAccount(otpCode.getOtpCode());
        return ResponseEntity.ok().build();
    }
}
