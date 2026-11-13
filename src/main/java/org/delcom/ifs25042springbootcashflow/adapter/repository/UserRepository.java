package org.delcom.ifs25042springbootcashflow.adapter.repository;

import org.delcom.ifs25042springbootcashflow.domain.entity.Role;
import org.delcom.ifs25042springbootcashflow.domain.entity.User;
import org.delcom.ifs25042springbootcashflow.domain.repository.IUserRepository;

import java.util.Optional;

/**
 * Implementasi in-memory untuk pengguna (unit test).
 */
public class UserRepository extends InMemoryRepository<User, Integer> implements IUserRepository {

    private int idCounter = 0;

    public UserRepository() {
        super(User::getId);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return findBy(user -> user.getUsername().equalsIgnoreCase(username)).stream().findFirst();
    }

    @Override
    public User saveNew(String username, String passwordHash, String fullName, Role role) {
        User user = new User(nextId(), username, passwordHash, fullName, role, null);
        return save(user);
    }

    @Override
    public void update(User entity) {
        // Objek mutable disimpan by-reference dalam memori.
    }

    private int nextId() {
        return ++idCounter;
    }
}
