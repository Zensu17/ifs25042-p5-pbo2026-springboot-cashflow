package org.delcom.ifs25042springbootcashflow.usecase;

import org.delcom.ifs25042springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25042springbootcashflow.domain.entity.User;
import org.delcom.ifs25042springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25042springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25042springbootcashflow.domain.repository.IUserRepository;

import java.util.List;

/**
 * Use case pengelolaan profil dan daftar pengguna.
 */
public class UserUseCase {

    private final IUserRepository userRepository;

    public UserUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public User getUser(int id) throws EntityNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
    }

    public User updateProfile(int userId, String fullName, String newPassword)
            throws EntityNotFoundException, ValidationException {
        User user = getUser(userId);
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Nama lengkap tidak boleh kosong");
        }
        user.changeFullName(fullName.trim());
        if (newPassword != null && !newPassword.isBlank()) {
            if (newPassword.length() < 4) {
                throw new ValidationException("Password minimal 4 karakter");
            }
            user.changePasswordHash(PasswordHasher.hash(newPassword));
        }
        userRepository.update(user);
        return user;
    }

    public User updatePhoto(int userId, String photoPath)
            throws EntityNotFoundException, ValidationException {
        if (photoPath == null || photoPath.isBlank()) {
            throw new ValidationException("Path foto tidak boleh kosong");
        }
        User user = getUser(userId);
        user.changePhotoPath(photoPath);
        userRepository.update(user);
        return user;
    }
}
