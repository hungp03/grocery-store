package com.store.grocery.service.impl;

import com.store.grocery.domain.Role;
import com.store.grocery.domain.User;
import com.store.grocery.domain.UserToken;
import com.store.grocery.dto.request.user.UpdatePasswordRequest;
import com.store.grocery.dto.request.user.UpdateUserRequest;
import com.store.grocery.dto.request.user.UpdateUserStatusRequest;
import com.store.grocery.dto.request.user.UserRegisterRequest;
import com.store.grocery.dto.response.PaginationResponse;
import com.store.grocery.dto.response.user.CreateUserResponse;
import com.store.grocery.dto.response.user.DeviceResponse;
import com.store.grocery.dto.response.user.UpdateUserResponse;
import com.store.grocery.dto.response.user.UserResponse;
import com.store.grocery.mapper.UserMapper;
import com.store.grocery.repository.UserRepository;
import com.store.grocery.service.EmailService;
import com.store.grocery.service.OTPService;
import com.store.grocery.service.UserService;
import com.store.grocery.service.UserTokenService;
import com.store.grocery.util.SecurityUtil;
import com.store.grocery.util.enums.OTPType;
import com.store.grocery.util.exception.AuthException;
import com.store.grocery.util.exception.DuplicateResourceException;
import com.store.grocery.util.exception.ResourceInvalidException;
import com.store.grocery.util.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserTokenService userTokenService;
    private final EmailService emailService;
    private final OTPService otpService;

    @Override
    public CreateUserResponse create(UserRegisterRequest user) {
        log.info("Creating new user with email: {}", user.getEmail());
        if (this.isExistedEmail(user.getEmail())) {
            log.error("Email already exists: {}", user.getEmail());
            throw new DuplicateResourceException("Email " + user.getEmail() + " đã tồn tại");
        }
        User u = User.builder()
                .email(user.getEmail())
                .name(user.getName())
                .password(passwordEncoder.encode(user.getPassword()))
                .status(true)
                .role(new Role(2L)).build();
        User savedUser = this.userRepository.save(u);
        log.info("Successfully created user with ID: {}", savedUser.getId());
        return userMapper.toCreateUserResponse(savedUser);
    }

    @Override
    public boolean isExistedEmail(String email) {
        log.debug("Checking if email exists: {}", email);
        return this.userRepository.existsByEmail(email);
    }

    private boolean isExistedId(long id) {
        log.debug("Checking if user ID exists: {}", id);
        return this.userRepository.existsById(id);
    }

    @Override
    public void delete(long id) {
        log.info("Attempting to delete user with ID: {}", id);
        boolean check = this.isExistedId(id);
        if (!check) {
            log.error("User not found with ID: {}", id);
            throw new UserNotFoundException("Người dùng với id " + id + " không tồn tại");
        }
        this.userRepository.deleteById(id);
        log.info("Successfully deleted user with ID: {}", id);
    }

    @Override
    public User findById(long id) {
        log.debug("Fetching user by ID: {}", id);
        return this.userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User không tồn tại"));

    }

    @Override
    public UserResponse getUserById(long id) {
        return userMapper.toUserResponse(findById(id));
    }

    @Override
    public PaginationResponse fetchAllUser(Specification<User> specification, Pageable pageable) {
        log.info("Fetching all users with pagination");
        Page<User> userPage = this.userRepository.findAll(pageable);
        PaginationResponse paginationResponse = PaginationResponse.from(userPage, pageable);
        // remove sensitive data like password...
        List<UserResponse> listUser = userPage.getContent()
                .stream().map(userMapper::toUserResponse)
                .collect(Collectors.toList());

        paginationResponse.setResult(listUser);
        log.debug("Found {} users", userPage.getTotalElements());
        return paginationResponse;
    }

    @Override
    public void updateStatus(Long id, UpdateUserStatusRequest req) {
        log.info("Updating status for user ID: {}", id);
        User currentUser = findById(id);
        if (currentUser != null) {
            currentUser.setStatus(req.getStatus());
            this.userRepository.save(currentUser);
            log.info("Successfully updated status for user ID: {}", id);
        }
    }

    @Override
    public User getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        User u = this.userRepository.findByEmail(username);
        if (u == null) {
            log.error("User not found with username: {}", username);
            throw new UserNotFoundException("User không tồn tại");
        }
        return u;
    }

    @Override
    public void updatePassword(String email, String newPassword) {
        log.info("Resetting password for user with email: {}", email);
        User user = getUserByUsername(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        this.userRepository.save(user);
        log.info("Successfully reset password for user with email: {}", email);
    }

    @Override
    public void checkAccountBanned(User user) {
        log.debug("Checking if account is banned for user ID: {}", user.getId());
        if (!user.isStatus()) {
            log.error("Account is banned for user ID: {}", user.getId());
            throw new AuthException("Tài khoản của bạn đã bị vô hiệu hóa.");
        }
    }

    @Override
    public UpdateUserResponse updateUser(UpdateUserRequest request) {
        log.info("Updating user profile");
        long uid = SecurityUtil.getUserId();
        User currentUserDB = findById(uid);

        User updatedUser = currentUserDB.toBuilder()
                .name(request.getName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .avatarUrl(request.getAvatarUrl() != null ? request.getAvatarUrl() : currentUserDB.getAvatarUrl())  // Cập nhật avatarUrl nếu có
                .build();

        userRepository.save(updatedUser);
        log.info("Successfully updated user profile for user ID: {}", updatedUser.getId());
        return userMapper.toUpdateUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void changePassword(UpdatePasswordRequest dto) {
        log.info("Attempting to change password for user");
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            log.error("Password confirmation does not match");
            throw new ResourceInvalidException("Mật khẩu xác nhận không trùng khớp");
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = this.getUserByUsername(username);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            log.error("Current password is incorrect for user ID: {}", user.getId());
            throw new ResourceInvalidException("Mật khẩu cũ không đúng");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        log.info("Successfully changed password for user ID: {}", user.getId());
    }

    @Override
    public List<DeviceResponse> getLoggedInDevices(String deviceHash) {
        log.info("Getting logged in devices for current user");
        long userId = SecurityUtil.getUserId();
        List<UserToken> userTokens = this.userTokenService.findDevicesByUser(userId);
        return userTokens.stream()
                .map(token -> new DeviceResponse(token.getDeviceInfo(), token.getCreatedAt(), token.getDeviceHash(), token.getDeviceHash().equals(deviceHash)))
                .toList();
    }

    @Override
    public void requestDeactiveAccount() {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        log.info("Requesting account deactivation - Email: {}", email);
        if (!isExistedEmail(email)) {
            throw new UserNotFoundException("Email " + email + " không tồn tại");
        }
        String otp = otpService.generateOTP();
        otpService.storeOTP(otp, email, OTPType.DEACTIVE_ACCOUNT);
        this.emailService.sendEmailFromTemplateSync(email, "Deactive Account", "deactiveAccount", email, otp);
    }

    @Override
    @Transactional
    public void verifyOTPAndDisableAccount(String inputOtp) {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        log.info("Verifying OTP for account deactivation - Email: {}", email);
        boolean validOTP = otpService.verifyOTP(email, inputOtp, OTPType.DEACTIVE_ACCOUNT);
        if (!validOTP) {
            log.warn("Failed OTP verification for email: {}. Invalid or expired OTP.", email);
            throw new ResourceInvalidException("OTP không hợp lệ hoặc đã hết hạn");
        }
        otpService.deleteOtpByEmailAndType(email, OTPType.DEACTIVE_ACCOUNT);
        User u = getUserByUsername(email);
        u.setStatus(false);
        userRepository.save(u);
        log.info("Account {} has been deactivated", email);
    }
}

