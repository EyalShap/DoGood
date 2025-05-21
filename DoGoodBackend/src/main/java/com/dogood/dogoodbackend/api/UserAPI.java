package com.dogood.dogoodbackend.api;

import com.dogood.dogoodbackend.api.userrequests.*;
import com.dogood.dogoodbackend.domain.users.User;
import com.dogood.dogoodbackend.domain.users.notificiations.Notification;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.HourApprovalRequest;
import com.dogood.dogoodbackend.service.Response;
import com.dogood.dogoodbackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static com.dogood.dogoodbackend.utils.GetToken.getToken;

@RestController
@CrossOrigin
@RequestMapping("/api/users")
public class UserAPI {
    @Autowired
    UserService userService; //this is also singleton

    @PostMapping("/login")
    public Response<String> login(@RequestBody LoginRequest body) {
        return userService.login(body.getUsername(), body.getPassword());
    }

    @PostMapping("/logout")
    public Response<String> logout(HttpServletRequest request) {
        return userService.logout(getToken(request));
    }

    @PostMapping("/register")
    public Response<String> register(@RequestBody RegisterRequest body) {
        return userService.register(body.getUsername(), body.getPassword(), body.getName(), body.getEmail(), body.getPhone(), body.getBirthDate(), body.getProfilePicUrl());
    }
    // VERIFICATION START
    @PostMapping("/verify-email")
    public Response<String> verifyEmail(@RequestBody VerifyEmailRequest body) {
        return userService.verifyEmail(body);
    }
    // VERIFICATION END

    // FORGOT_PASSWORD START

    @PostMapping("/forgot-password")
    public Response<String> forgotPassword(@RequestBody ForgotPasswordRequest body) {
        return userService.forgotPassword(body);
    }

    @PostMapping("/verify-reset-password")
    public Response<String> verifyResetPassword(@RequestBody VerifyResetPasswordRequest body) {
        // No token needed for this endpoint as user is not logged in
        return userService.verifyPasswordResetCode(body);
    }

    @PostMapping("/reset-password")
    public Response<String> resetPassword(@RequestBody ResetPasswordRequest body) {
        // No token needed for this endpoint
        // The security relies on the short-lived, validated code passed in the body
        return userService.resetPassword(body);
    }
// FORGOT_PASSWORD END
// UPDATE-EMAIL-VERIFICATION START
@PostMapping("/request-update-code")
public Response<String> requestUpdateCode(HttpServletRequest httpRequest, @RequestBody RequestEmailUpdateVerificationRequest body) {
    String token = getToken(httpRequest);
    return userService.requestEmailUpdateVerification(token, body);
}

    @PostMapping("/verify-update-code")
    public Response<String> verifyUpdateCode(HttpServletRequest httpRequest, @RequestBody VerifyEmailUpdateCodeRequest body) {
        String token = getToken(httpRequest);
        return userService.verifyEmailUpdateCode(token, body);
    }
    // UPDATE-EMAIL-VERIFICATION END



    // PASSWORD-CHANGE-NO-EMAIL START
    @PostMapping("/change-password")
    public Response<String> changePassword(HttpServletRequest httpRequest, @RequestBody ChangePasswordRequest body) {
        String token = getToken(httpRequest);
        return userService.changePassword(token, body);
    }
    // PASSWORD-CHANGE-NO-EMAIL END

    // RESEND VERIFICATION START
    @PostMapping("/resend-verification-code")
    public Response<String> resendVerificationCode(@RequestBody ResendVerificationCodeRequest body) {
        // This endpoint is typically called when the user is not logged in (e.g. during registration or forgot password)
        // Thus, it does not use getToken(httpRequest) for user identification.
        // The username is taken directly from the request body.
        return userService.handleResendVerificationCode(body);
    }
    // RESEND VERIFICATION END

    @GetMapping("/isAdmin")
    public Response<Boolean> isAdmin(@RequestParam String username) {
        return userService.isAdmin(username);
    }

    @GetMapping("/getUserByUsername")
    public Response<User> getUserByUsername(@RequestParam String username) {
        return userService.getUserByUsername(username);
    }

    @GetMapping("/getUserByToken")
    public Response<User> getUserByToken(HttpServletRequest request) {
        return userService.getUserByToken(getToken(request));
    }

    @PatchMapping("/updateUserFields")
    public Response<String> updateUserFields(@RequestParam String username, @RequestBody UpdateUserRequest body, HttpServletRequest request){
        String token = getToken(request);
        return userService.updateUserFields(token, username, body.getPassword(), body.getEmails(), body.getName(), body.getPhone());
    }

    @PatchMapping("/updateUserSkills")
    public Response<String> updateUserSkills(@RequestParam String username, @RequestBody List<String> body, HttpServletRequest request){
        String token = getToken(request);
        return userService.updateUserSkills(token, username, body);
    }

    @PatchMapping("/updateUserPreferences")
    public Response<String> updateUserPreferences(@RequestParam String username, @RequestBody List<String> body, HttpServletRequest request){
        String token = getToken(request);
        return userService.updateUserPreferences(token, username, body);
    }

    @GetMapping("/getUserApprovedHours")
    public Response<List<HourApprovalRequest>> getUserApprovedHours(@RequestParam String username, HttpServletRequest request) {
        String token = getToken(request);
        return userService.getApprovedHours(token, username);
    }

    @GetMapping("/leaderboard")
    public Response<Map<String, Double>> leaderboard(@RequestParam String username, HttpServletRequest request) {
        String token = getToken(request);
        return userService.leaderboard(token, username);
    }

    @PutMapping("/setLeaderboard")
    public Response<Boolean> setLeaderboard(@RequestParam String username, @RequestParam boolean leaderboard, HttpServletRequest request) {
        String token = getToken(request);
        return userService.setLeaderboard(token, username, leaderboard);
    }

    @PutMapping("/setNotifyRecommendation")
    public Response<Boolean> setNotifyRecommendation(@RequestParam String username, @RequestParam boolean notify, HttpServletRequest request) {
        String token = getToken(request);
        return userService.setNotifyRecommendation(token, username, notify);
    }

    @PutMapping("/setRemindActivity")
    public Response<Boolean> setRemindActivity(@RequestParam String username, @RequestParam boolean remind, HttpServletRequest request) {
        String token = getToken(request);
        return userService.setRemindActivity(token, username, remind);
    }

    @PatchMapping("/banUser")
    public Response<Boolean> banUser(@RequestParam String actor, @RequestParam String username, HttpServletRequest request){
        String token = getToken(request);
        return userService.banUser(token, actor, username);
    }

    @GetMapping("/getAllUserEmails")
    public Response<List<String>> getAllUserEmails(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);
        return userService.getAllUserEmails(token, actor);
    }

    @GetMapping("/getUserNotifications")
    public Response<List<Notification>> getUserNotifications(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);
        return userService.getUserNotifications(token, actor);
    }

    @PatchMapping("/readNewUserNotifications")
    public Response<List<Notification>> readNewUserNotifications(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);
        return userService.readNewUserNotifications(token, actor);
    }

    @GetMapping("/getNewUserNotificationsAmount")
    public Response<Integer> getNewUserNotificationsAmount(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);
        return userService.getNewUserNotificationsAmount(token, actor);
    }

    @PutMapping("/uploadCV")
    public Response<Boolean> uploadCV(@RequestParam String username, @RequestParam MultipartFile cvPdf, HttpServletRequest request) {
        String token = getToken(request);
        return userService.uploadCV(token, username, cvPdf);
    }

    @PutMapping("/removeCV")
    public Response<Boolean> removeCV(@RequestParam String username, HttpServletRequest request) {
        String token = getToken(request);
        return userService.uploadCV(token, username, null);
    }

    @GetMapping("/getCV")
    public ResponseEntity<byte[]> getCV(@RequestParam String actor, HttpServletRequest request) {
        String token = getToken(request);
        Response<byte[]> res = userService.getCV(token, actor);

        if (res.getError()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res.getErrorString().getBytes());
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .body(res.getData());
    }

    @PatchMapping("/generateSkillsAndPreferences")
    public Response<Boolean> generateSkillsAndPreferences(@RequestParam String username, HttpServletRequest request) {
        String token = getToken(request);
        return userService.generateSkillsAndPreferences(token, username);
    }

    @PatchMapping("/updateProfilePicture")
    public Response<String> updateProfilePicture(@RequestParam String username, @RequestBody Map<String, String> body, HttpServletRequest request) {
        String token = getToken(request);
        String profilePicUrl = body.get("profilePicUrl");
        return userService.updateProfilePicture(token, username, profilePicUrl);
    }

    @PostMapping("/registerFcmToken")
    public Response<String> registerFcmToken(@RequestParam String username, @RequestBody String fcmToken, HttpServletRequest request) {
        String token = getToken(request);
        return userService.registerFcmToken(token, username, fcmToken.replace("\"",""));
    }

    @PatchMapping("/removeFcmToken")
    public Response<String> removeFcmToken(@RequestParam String username, @RequestBody String fcmToken, HttpServletRequest request) {
        String token = getToken(request);
        return userService.removeFcmToken(token, username, fcmToken.replace("\"",""));
    }
}
