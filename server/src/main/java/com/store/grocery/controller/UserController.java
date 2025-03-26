package com.store.grocery.controller;

import com.store.grocery.domain.User;
import com.store.grocery.domain.request.user.UpdatePasswordDTO;
import com.store.grocery.domain.request.user.UpdateUserRequest;
import com.store.grocery.domain.request.user.UserStatusDTO;
import com.store.grocery.domain.response.PaginationDTO;
import com.store.grocery.domain.response.user.CreateUserDTO;
import com.store.grocery.domain.response.user.DeviceDTO;
import com.store.grocery.domain.response.user.ResLoginDTO;
import com.store.grocery.domain.response.user.UserDTO;
import com.store.grocery.service.UserService;
import com.store.grocery.util.annotation.ApiMessage;
import com.store.grocery.util.exception.ResourceInvalidException;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("users")
    @ApiMessage("Create new user")
    // Not use
    public ResponseEntity<CreateUserDTO> createNewUser(@Valid @RequestBody User user){
        User newUser = this.userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertToCreateDTO(newUser));
    }

    @GetMapping("users/devices")
    @ApiMessage("Get logged in devices")
    public ResponseEntity<List<DeviceDTO>> getLoggedInDevices(@CookieValue(name = "device", defaultValue = "none") String deviceHash){
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
    public ResponseEntity<UserDTO> getUser(@PathVariable("id") long id) throws ResourceInvalidException {
        User currentUser = this.userService.getUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.convertToUserDTO(currentUser));
    }

    @GetMapping("users")
    @ApiMessage("Fetch users")
    public ResponseEntity<PaginationDTO> fetchAllUser(@Filter Specification<User> spec, Pageable pageable) {
        return ResponseEntity.ok(this.userService.fetchAllUser(spec, pageable));
    }

    @PutMapping("users/update-password")
    @ApiMessage("Change password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody UpdatePasswordDTO dto){
        this.userService.changePassword(dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("users/status")
    @ApiMessage("Update user status")
    public ResponseEntity<Void> updateUser(@RequestBody UserStatusDTO user) throws ResourceInvalidException {
        this.userService.updateStatus(user);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "users/account", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiMessage("Update user information")
    public ResponseEntity<ResLoginDTO.UserGetAccount> updateUser(
            @RequestPart("user") UpdateUserRequest request,
            @RequestPart(value = "avatarUrl", required = false) MultipartFile avatar) throws IOException {

        return ResponseEntity.ok(this.userService.updateUser(request, avatar));
    }


}
