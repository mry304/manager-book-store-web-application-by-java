package com.bittercode.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import jakarta.servlet.http.HttpSession;

import com.bittercode.constant.ResponseCode;
import com.bittercode.constant.db.UsersDBConstants;
import com.bittercode.model.StoreException;
import com.bittercode.model.User;
import com.bittercode.model.UserRole;
import com.bittercode.service.UserService;
import com.bittercode.util.DBUtil;

import java.util.logging.Level;
import java.util.logging.Logger;
public class UserServiceImpl implements UserService {
    private static final Logger LOGGER = Logger.getLogger(UserServiceImpl.class.getName());


    private static final String HASH_PREFIX = "pbkdf2_sha256$";
    private static final int PBKDF2_ITERATIONS = 120_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final String registerUserQuery = "INSERT INTO " + UsersDBConstants.TABLE_USERS + " ("
            + UsersDBConstants.COLUMN_USERNAME + ", "
            + UsersDBConstants.COLUMN_PASSWORD + ", "
            + UsersDBConstants.COLUMN_FIRSTNAME + ", "
            + UsersDBConstants.COLUMN_LASTNAME + ", "
            + UsersDBConstants.COLUMN_ADDRESS + ", "
            + UsersDBConstants.COLUMN_PHONE + ", "
            + UsersDBConstants.COLUMN_MAILID + ", "
            + UsersDBConstants.COLUMN_USERTYPE + ") VALUES(?,?,?,?,?,?,?,?)";

    private static final String loginUserQuery = "SELECT * FROM " + UsersDBConstants.TABLE_USERS + " WHERE ("
            + UsersDBConstants.COLUMN_USERNAME + "=? OR " + UsersDBConstants.COLUMN_MAILID + "=?) AND "
            + UsersDBConstants.COLUMN_USERTYPE + "=?";

    private static final String updatePasswordQuery = "UPDATE " + UsersDBConstants.TABLE_USERS + " SET "
            + UsersDBConstants.COLUMN_PASSWORD + "=? WHERE "
            + UsersDBConstants.COLUMN_USERNAME + "=?";

    @Override
    public User login(UserRole role, String email, String password, HttpSession session) throws StoreException {
        if (email == null || password == null) {
            return null;
        }
        User user = null;
        String userType = UserRole.SELLER.equals(role) ? "1" : "2";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(loginUserQuery)) {

            ps.setString(1, email);
            ps.setString(2, email);
            ps.setString(3, userType);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString(UsersDBConstants.COLUMN_PASSWORD);
                    if (!verifyPassword(password, storedPassword)) {
                        return null;
                    }

                    String username = rs.getString(UsersDBConstants.COLUMN_USERNAME);
                    if (storedPassword != null && !storedPassword.startsWith(HASH_PREFIX)) {
                        upgradeLegacyPasswordHash(con, username, password);
                    }

                    user = new User();
                    user.setUsername(username);
                    user.setFirstName(rs.getString(UsersDBConstants.COLUMN_FIRSTNAME));
                    user.setLastName(rs.getString(UsersDBConstants.COLUMN_LASTNAME));
                    user.setPhone(rs.getLong(UsersDBConstants.COLUMN_PHONE));
                    user.setEmailId(rs.getString(UsersDBConstants.COLUMN_MAILID));

                    if (session != null) {
                        session.setAttribute(role.toString(), user.getUsername());
                        session.setAttribute("userEmail", user.getEmailId());
                        session.setAttribute("user", user);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new StoreException("Lỗi đăng nhập");
        }
        return user;
    }

    @Override
    public boolean isLoggedIn(UserRole role, HttpSession session) {
        if (session == null) return false;
        if (role == null) role = UserRole.CUSTOMER;
        return session.getAttribute(role.toString()) != null;
    }

    @Override
    public boolean logout(HttpSession session) {
        if (session != null) {
            session.removeAttribute(UserRole.CUSTOMER.toString());
            session.removeAttribute(UserRole.SELLER.toString());
            session.invalidate();
        }
        return true;
    }

    @Override
    public String register(UserRole role, User user) throws StoreException {
        String responseMessage = ResponseCode.FAILURE.name();
        int userType = UserRole.SELLER.equals(role) ? 1 : 2;

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(registerUserQuery)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, hashPassword(user.getPassword()));
            ps.setString(3, user.getFirstName());
            ps.setString(4, user.getLastName());
            ps.setString(5, user.getAddress());
            ps.setString(6, user.getPhone() != null ? String.valueOf(user.getPhone()) : "0");
            ps.setString(7, user.getEmailId());
            ps.setInt(8, userType);

            int k = ps.executeUpdate();
            if (k > 0) {
                responseMessage = ResponseCode.SUCCESS.name();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            String msg = e.getMessage();
            if (msg != null && (msg.contains("Duplicate") || msg.contains("PRIMARY"))) {
                responseMessage = "User already registered with this username or email !!";
            } else {
                responseMessage = ResponseCode.FAILURE.name() + " : " + msg;
            }
        }
        return responseMessage;
    }

    private void upgradeLegacyPasswordHash(Connection con, String username, String rawPassword) {
        try (PreparedStatement ps = con.prepareStatement(updatePasswordQuery)) {
            ps.setString(1, hashPassword(rawPassword));
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.warning("[UserService] Failed to upgrade legacy password hash: " + e.getMessage());
        }
    }

    private String hashPassword(String password) {
        byte[] salt = new byte[SALT_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt, PBKDF2_ITERATIONS);
        return HASH_PREFIX + PBKDF2_ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    private boolean verifyPassword(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (storedPassword.startsWith(HASH_PREFIX)) {
            String[] parts = storedPassword.split("\\$");
            if (parts.length != 4) {
                return false;
            }
            try {
                int iterations = Integer.parseInt(parts[1]);
                byte[] salt = Base64.getDecoder().decode(parts[2]);
                byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
                byte[] actualHash = pbkdf2(rawPassword.toCharArray(), salt, iterations);
                return MessageDigest.isEqual(expectedHash, actualHash);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return MessageDigest.isEqual(
                rawPassword.getBytes(StandardCharsets.UTF_8),
                storedPassword.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, HASH_BITS);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2WithHmacSHA256 unavailable", e);
        }
    }
}