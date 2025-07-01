package services;

import models.User;
import repositories.UserRepository;

public class AuthService {
    private static AuthService instance;
    private final UserRepository userRepository;
    private User currentUser;

    // Private constructor to prevent direct instantiation
    private AuthService() {
        this.userRepository = new UserRepository();
    }

    // Get the singleton instance
    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public boolean login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            System.out.println("User logged in: " + currentUser.getUsername()); // Debug log
            return true;
        }
        return false;
    }

    public boolean register(String username, String password, String email, String phone, boolean isAdmin) {
        User user = new User(0, username, password, email, phone, isAdmin, null);
        return userRepository.create(user);
    }

    public User getCurrentUser() {
        System.out.println("Getting current user: " + (currentUser != null ? currentUser.getUsername() : "null")); // Debug log
        return currentUser;
    }

    public void logout() {
        System.out.println("User logged out: " + (currentUser != null ? currentUser.getUsername() : "null")); // Debug log
        currentUser = null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
}