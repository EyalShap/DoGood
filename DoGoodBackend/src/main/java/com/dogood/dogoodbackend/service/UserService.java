package com.dogood.dogoodbackend.service;

import com.dogood.dogoodbackend.domain.users.User;
import com.dogood.dogoodbackend.domain.users.UsersFacade;
import com.dogood.dogoodbackend.domain.users.auth.AuthFacade;
import com.dogood.dogoodbackend.domain.users.notificiations.Notification;
import com.dogood.dogoodbackend.domain.users.notificiations.NotificationSystem;
import com.dogood.dogoodbackend.domain.users.notificiations.PushNotificationSender;
import com.dogood.dogoodbackend.domain.volunteerings.scheduling.HourApprovalRequest;
import com.dogood.dogoodbackend.socket.NotificationSocketSender;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.dogood.dogoodbackend.api.userrequests.VerifyEmailRequest;
import com.dogood.dogoodbackend.api.userrequests.RequestEmailUpdateVerificationRequest;
import com.dogood.dogoodbackend.api.userrequests.VerifyEmailUpdateCodeRequest;
import com.dogood.dogoodbackend.api.userrequests.ForgotPasswordRequest;
import com.dogood.dogoodbackend.api.userrequests.VerifyResetPasswordRequest;
import com.dogood.dogoodbackend.api.userrequests.ResetPasswordRequest;
import com.dogood.dogoodbackend.api.userrequests.ChangePasswordRequest;
import com.dogood.dogoodbackend.api.userrequests.ResendVerificationCodeRequest;


import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    private UsersFacade usersFacade;
    private AuthFacade authFacade;
    private NotificationSystem notificationSystem;

    @Autowired
    public UserService(FacadeManager facadeManager, NotificationSocketSender socketSender, PushNotificationSender pushNotificationSender){
        this.usersFacade = facadeManager.getUsersFacade();
        this.authFacade = facadeManager.getAuthFacade();
        this.notificationSystem = facadeManager.getNotificationSystem();
        this.notificationSystem.setSender(socketSender);
        this.notificationSystem.setPushNotificationSender(pushNotificationSender);
        pushNotificationSender.setUsersFacade(usersFacade);
        //this.usersFacade.registerAdmin("admin","password","admin","admin@gmail.com","052-0520520", new Date());
    }

    private void checkToken(String token, String username){
        if(!authFacade.getNameFromToken(token).equals(username)){
            throw new IllegalArgumentException("Invalid token");
        }
        if(usersFacade.isBanned(username)) {
            throw new IllegalArgumentException("Banned user.");
        }
    }

    public Response<String> login(String username, String password) {
        try{
            return Response.createResponse(usersFacade.login(username, password), null);
        }catch (Exception e){
            return Response.createResponse(null, e.getMessage());
        }
    }

    public Response<String> logout(String token) {
        try{
            usersFacade.logout(token);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> register(String username, String password, String name, String email, String phone, Date birthDate, String profilePicUrl) {
        try{
            usersFacade.register(username, password, name, email, phone, birthDate, profilePicUrl);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }
    // VERIFICATION START
    public Response<String> verifyEmail(VerifyEmailRequest request) {
        try {
            // No token check here as user is not logged in yet.
            // Verification is based on username and code.
            String message = usersFacade.verifyEmail(request.getUsername(), request.getCode());
            if ("Email verified successfully.".equals(message) || "Email already verified.".equals(message)) {
                return Response.createResponse(message, null);
            } else {
                // For other messages like "Invalid code", treat as error string in Response
                return Response.createResponse(null, message);
            }
        } catch (Exception e) {
            return Response.createResponse(null, e.getMessage());
        }
    }
    // VERIFICATION END
    // PASSWORD-CHANGE-NO-EMAIL START
    public Response<String> changePassword(String token, ChangePasswordRequest request) {
        try {
            String usernameFromToken = authFacade.getNameFromToken(token);
            // The facade will ensure usernameFromToken matches request.getUsername()
            String message = usersFacade.changePassword(
                    usernameFromToken,
                    request.getUsername(),
                    request.getOldPassword(),
                    request.getNewPassword()
            );
            return Response.createResponse(message, null);
        } catch (Exception e) {
            return Response.createResponse(null, e.getMessage());
        }
    }
    // PASSWORD-CHANGE-NO-EMAIL END

    // UPDATE-EMAIL-VERIFICATION START
    public Response<String> requestEmailUpdateVerification(String token, RequestEmailUpdateVerificationRequest request) {
        try {
            String actorUsername = authFacade.getNameFromToken(token);
            // The facade method will also check if actorUsername matches the owner of request.getEmail()
            String message = usersFacade.requestEmailUpdateVerification(request.getEmail(), actorUsername);
            return Response.createResponse(message, null);
        } catch (Exception e) {
            return Response.createResponse(null, e.getMessage());
        }
    }

    public Response<String> verifyEmailUpdateCode(String token, VerifyEmailUpdateCodeRequest request) {
        try {
            String actorUsername = authFacade.getNameFromToken(token);
            // The facade method will also check if actorUsername matches the owner of request.getEmail()
            String message = usersFacade.verifyEmailUpdateCode(request.getEmail(), request.getCode(), actorUsername);
            if (message.startsWith("Code verified successfully")) { // Check prefix for success
                return Response.createResponse(message, null);
            } else {
                return Response.createResponse(null, message); // Error messages
            }
        } catch (Exception e) {
            return Response.createResponse(null, e.getMessage());
        }
    }
    // UPDATE-EMAIL-VERIFICATION END

    // RESEND VERIFICATION START
    public Response<String> handleResendVerificationCode(ResendVerificationCodeRequest request) {
        try {
            String message = usersFacade.resendVerificationCode(request.getUsername());
            if ("A new verification code has been sent to your email address.".equals(message) ||
                    "Email already verified.".equals(message)) {
                return Response.createResponse(message, null);
            } else {
                // Should not happen based on facade logic, but as a fallback
                return Response.createResponse(null, message);
            }
        } catch (IllegalArgumentException e) { // Catch specific exceptions like UserNotFound
            return Response.createResponse(null, e.getMessage());
        } catch (IllegalStateException e) { // Catch specific exceptions like UserHasNoEmail
            return Response.createResponse(null, e.getMessage());
        } catch (Exception e) { // Generic catch for other unexpected errors
            return Response.createResponse(null, "An unexpected error occurred while resending the verification code.");
        }
    }
    // RESEND VERIFICATION END

    // FORGOT_PASSWORD START
    public Response<String> forgotPassword(ForgotPasswordRequest request) {
        try {
            // Facade method now takes username.
            // The facade will handle not throwing errors for non-existent users to prevent enumeration.
            usersFacade.forgotPassword(request.getUsername());
            // Always return a generic success message to the client, regardless of whether the user/email existed.
            return Response.createResponse("If your username is registered, a password reset code has been sent to your email.", null);
        } catch (Exception e) {
            // Log internal errors, but still return a generic message to client.
            System.err.println("Error during forgotPassword process for username " + request.getUsername() + ": " + e.getMessage());
            return Response.createResponse("If your username is registered, a password reset code has been sent to your email. If you encounter issues, please contact support.", null);
        }
    }

    public Response<String> verifyPasswordResetCode(VerifyResetPasswordRequest request) {
        try {
            boolean isValid = usersFacade.verifyPasswordResetCode(request.getUsername(), request.getCode());
            if (isValid) {
                return Response.createResponse("Verification code is valid.", null);
            } else {
                return Response.createResponse(null, "Invalid or expired verification code.");
            }
        } catch (Exception e) {
            System.err.println("Error during verifyPasswordResetCode: " + e.getMessage());
            return Response.createResponse(null, "An error occurred during verification.");
        }
    }

    public Response<String> resetPassword(ResetPasswordRequest request) {
        try {
            // No token check here, as this flow is for users who can't log in.
            // Security is handled by the one-time code.
            String resultMessage = usersFacade.resetPassword(request.getUsername(), request.getNewPassword(), request.getCode());
            if ("Password reset successfully.".equals(resultMessage)) {
                return Response.createResponse(resultMessage, null);
            } else {
                return Response.createResponse(null, resultMessage); // e.g., "Invalid code", "User not found"
            }
        } catch (Exception e) {
            System.err.println("Error during resetPassword: " + e.getMessage());
            return Response.createResponse(null, "An error occurred while resetting the password.");
        }
    }
// FORGOT_PASSWORD END

    public Response<String> updateProfilePicture(String token, String username, String profilePicUrl) {
        try {
            checkToken(token, username);
            usersFacade.updateProfilePicture(username, profilePicUrl);
            return Response.createOK();
        } catch (Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> isAdmin(String username) {
        try{
            return Response.createResponse(usersFacade.isAdmin(username));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<User> getUserByUsername(String username) {
        try{
            return Response.createResponse(usersFacade.getUser(username));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<User> getUserByToken(String token) {
        try{
            return Response.createResponse(usersFacade.getUserByToken(token));
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateUserFields(String token, String username, String password, List<String> emails, String name, String phone){
        try{
            checkToken(token, username);
            usersFacade.updateUserFields(username, password, emails, name, phone);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateUserSkills(String token, String username, List<String> skills){
        try{
            checkToken(token, username);
            usersFacade.updateUserSkills(username, skills);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> updateUserPreferences(String token, String username, List<String> categories){
        try{
            checkToken(token, username);
            usersFacade.updateUserPreferences(username, categories);
            return Response.createOK();
        }catch (Exception e){
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<HourApprovalRequest>> getApprovedHours(String token, String username) {
        try {
            checkToken(token, username);
            List<HourApprovalRequest> result = usersFacade.getApprovedHours(username);
            return Response.createResponse(result);
        } catch(Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Map<String, Double>> leaderboard(String token, String username) {
        try {
            checkToken(token, username);
            Map<String, Double> result = usersFacade.leaderboard();
            return Response.createResponse(result);
        } catch(Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> setLeaderboard(String token, String username, boolean leaderboard) {
        try {
            checkToken(token, username);
            usersFacade.setLeaderboard(username, leaderboard);
            return Response.createResponse(true);
        } catch(Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> setNotifyRecommendation(String token, String username, boolean notify) {
        try {
            checkToken(token, username);
            usersFacade.setNotifyRecommendation(username, notify);
            return Response.createResponse(true);
        } catch(Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> setRemindActivity(String token, String username, boolean remind) {
        try {
            checkToken(token, username);
            usersFacade.setRemindActivity(username, remind);
            return Response.createResponse(true);
        } catch(Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> banUser(String token, String actor, String username) {
        try {
            checkToken(token, actor);
            usersFacade.banUser(username, actor);
            return Response.createResponse(true);
        } catch(Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<String>> getAllUserEmails(String token, String actor) {
        try {
            checkToken(token, actor);
            List<String> res = usersFacade.getAllUserEmails();
            return Response.createResponse(res);
        } catch(Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<Notification>> getUserNotifications(String token, String actor) {
        try {
            checkToken(token, actor);
//            System.out.println("TESTING GET USER NOTIFICATIONS");
//            System.out.println(actor);
            return Response.createResponse(notificationSystem.getUserNotifications(actor));
        } catch(Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> uploadCV(String token, String actor, MultipartFile cvPdf) {
        try {
            checkToken(token, actor);
            usersFacade.uploadCV(actor, cvPdf);
            return Response.createResponse(true);
        } catch(Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<List<Notification>> readNewUserNotifications(String token, String actor) {
        try {
            checkToken(token, actor);
//            System.out.println("TESTING READ NEW NOTIFICATIONS");
//            System.out.println(actor);
            return Response.createResponse(notificationSystem.readNewUserNotifications(actor));
        } catch(Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<byte[]> getCV(String token, String actor) {
        try {
            checkToken(token, actor);
            byte[] res = usersFacade.getCV(actor);
            return Response.createResponse(res);
        } catch(Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Integer> getNewUserNotificationsAmount(String token, String actor) {
        try {
            checkToken(token, actor);
//            System.out.println("TESTING GET NEW USER NOTIFICATIONS AMOUNT");
//            System.out.println(actor);
            return Response.createResponse(notificationSystem.getNewUserNotificationsAmount(actor));
        } catch(Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<Boolean> generateSkillsAndPreferences(String token, String actor) {
        try {
            checkToken(token, actor);
            usersFacade.generateSkillsAndPreferences(actor);
            return Response.createResponse(true);
        } catch(Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> registerFcmToken(String token, String username, String fcmToken){
        try {
            checkToken(token, username);
            usersFacade.registerFcmToken(username, fcmToken);
            return Response.createOK();
        } catch(Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }

    public Response<String> removeFcmToken(String token, String username, String fcmToken){
        try {
            checkToken(token, username);
            usersFacade.removeFcmToken(username, fcmToken);
            return Response.createOK();
        } catch(Exception e) {
            return Response.createResponse(e.getMessage());
        }
    }
}
