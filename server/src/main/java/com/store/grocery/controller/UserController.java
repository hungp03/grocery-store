package com.store.grocery.controller;

import com.store.grocery.domain.User;
import com.store.grocery.dto.request.user.UpdatePasswordRequest;
import com.store.grocery.dto.request.user.UpdateUserRequest;
import com.store.grocery.dto.request.user.UserDisableOTPRequest;
import com.store.grocery.dto.request.user.UpdateUserStatusRequest;
import com.store.grocery.dto.response.PaginationResponse;
import com.store.grocery.dto.response.user.DeviceResponse;
import com.store.grocery.dto.response.user.UpdateUserResponse;
import com.store.grocery.dto.response.user.UserResponse;
import com.store.grocery.service.UserService;
import com.store.grocery.util.annotation.ApiMessage;
import com.store.grocery.util.exception.ResourceInvalidException;
import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Hidden
    @DeleteMapping("users/{id}")
    @ApiMessage("Delete user")
    // Not use
    public ResponseEntity<Void> deleteUser(@PathVariable("id") long id){
        this.userService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("users/{id}")
    @ApiMessage("Get user by id")
    public ResponseEntity<UserResponse> getUser(@PathVariable("id") long id) {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.getUserById(id));
    }

    @GetMapping("users")
    @ApiMessage("Fetch users")
    public ResponseEntity<PaginationResponse> fetchAllUser(@Filter Specification<User> spec, Pageable pageable) {
        return ResponseEntity.ok(this.userService.fetchAllUser(spec, pageable));
    }

    @PatchMapping("users/password")
    @ApiMessage("Change password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody UpdatePasswordRequest dto){
        this.userService.changePassword(dto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("users/{id}/status")
    @ApiMessage("Update user status")
    public ResponseEntity<Void> updateUserStatus(@PathVariable("id") long id, @Valid @RequestBody UpdateUserStatusRequest user) {
        this.userService.updateStatus(id, user);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "users/account")
    @ApiMessage("Update user information")
    public ResponseEntity<UpdateUserResponse> updateUser(
            @RequestBody UpdateUserRequest request){
        return ResponseEntity.ok(this.userService.updateUser(request));
    }

    @PostMapping("deactivate/request")
    @ApiMessage("Request deactivate account")
    public ResponseEntity<Void> requestDisableOTP() {
        userService.requestDeactiveAccount();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deactivate/confirm")
    @ApiMessage("Confirm deactivate account")
    public ResponseEntity<Void> confirmDisableAccount(
            @Valid @RequestBody UserDisableOTPRequest otpCode) {
        userService.verifyOTPAndDisableAccount(otpCode.getOtpCode());
        return ResponseEntity.ok().build();
    }
}
