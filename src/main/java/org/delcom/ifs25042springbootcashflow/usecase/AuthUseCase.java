package org.delcom.ifs25042springbootcashflow.usecase;

import org.delcom.ifs25042springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25042springbootcashflow.domain.entity.Role;
import org.delcom.ifs25042springbootcashflow.domain.entity.User;
import org.delcom.ifs25042springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25042springbootcashflow.domain.repository.IUserRepository;

/**
 * Use case autentikasi: registrasi, login, dan seed akun root admin.
 */
public class AuthUseCase {

    private final IUserRepository userRepository;

    public AuthUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String username, String password, String fullName) throws ValidationException {
        validateCredentials(username, password, fullName);
        if (userRepository.findByUsername(username.trim()).isPresent()) {
            throw new ValidationException("Username sudah dipakai");
        }
        return userRepository.saveNew(
                username.trim(),
                PasswordHasher.hash(password),
                fullName.trim(),
                Role.USER
        );
    }

    public User login(String username, String password) throws ValidationException {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username tidak boleh kosong");
        }
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Password tidak boleh kosong");
        }
        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new ValidationException("Username atau password salah"));
        if (!PasswordHasher.matches(password, user.getPasswordHash())) {
            throw new ValidationException("Username atau password salah");
        }
        return user;
    }

    /**
     * Memastikan akun admin default dari konfigurasi .env tersedia.
     */
    public User ensureRootAdmin(String rootUsername, String rootPassword) throws ValidationException {
        if (rootUsername == null || rootUsername.trim().isEmpty()) {
            throw new ValidationException("ROOT_USERNAME tidak boleh kosong");
        }
        if (rootPassword == null || rootPassword.isEmpty()) {
            throw new ValidationException("ROOT_PASSWORD tidak boleh kosong");
        }
        return userRepository.findByUsername(rootUsername.trim())
                .orElseGet(() -> userRepository.saveNew(
                        rootUsername.trim(),
                        PasswordHasher.hash(rootPassword),
                        "Administrator",
                        Role.ADMIN
                ));
    }

    private void validateCredentials(String username, String password, String fullName)
            throws ValidationException {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username tidak boleh kosong");
        }
        if (password == null || password.length() < 4) {
            throw new ValidationException("Password minimal 4 karakter");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Nama lengkap tidak boleh kosong");
        }
    }
}
