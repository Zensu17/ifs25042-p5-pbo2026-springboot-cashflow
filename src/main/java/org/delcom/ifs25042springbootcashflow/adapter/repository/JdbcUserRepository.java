package org.delcom.ifs25042springbootcashflow.adapter.repository;

import org.delcom.ifs25042springbootcashflow.domain.entity.Role;
import org.delcom.ifs25042springbootcashflow.domain.entity.User;
import org.delcom.ifs25042springbootcashflow.domain.repository.IUserRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Repository pengguna berbasis JDBC (PostgreSQL).
 */
public class JdbcUserRepository implements IUserRepository {

    private final DataSource dataSource;

    public JdbcUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT id, username, password_hash, full_name, role, photo_path FROM users ORDER BY id";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(mapRow(rs));
            }
            return users;
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal membaca daftar pengguna", ex);
        }
    }

    @Override
    public Optional<User> findById(Integer id) {
        String sql = "SELECT id, username, password_hash, full_name, role, photo_path FROM users WHERE id = ?";
        return queryOptional(sql, statement -> statement.setInt(1, id), "Gagal mencari pengguna");
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, full_name, role, photo_path FROM users WHERE LOWER(username) = LOWER(?)";
        return queryOptional(sql, statement -> statement.setString(1, username), "Gagal mencari username");
    }

    @Override
    public User save(User entity) {
        return saveNew(entity.getUsername(), entity.getPasswordHash(), entity.getFullName(), entity.getRole());
    }

    @Override
    public User saveNew(String username, String passwordHash, String fullName, Role role) {
        String sql = "INSERT INTO users (username, password_hash, full_name, role) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, fullName);
            statement.setString(4, role.name());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException("Gagal mendapatkan ID pengguna baru");
                }
                return new User(keys.getInt(1), username, passwordHash, fullName, role, null);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal menyimpan pengguna", ex);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal menghapus pengguna", ex);
        }
    }

    @Override
    public void update(User entity) {
        String sql = "UPDATE users SET password_hash = ?, full_name = ?, photo_path = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getPasswordHash());
            statement.setString(2, entity.getFullName());
            statement.setString(3, entity.getPhotoPath());
            statement.setInt(4, entity.getId());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Gagal mengubah pengguna", ex);
        }
    }

    @Override
    public List<User> findBy(Predicate<User> predicate) {
        return findAll().stream().filter(predicate).toList();
    }

    private Optional<User> queryOptional(String sql, SqlBinder binder, String errorMessage) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(sql);
            binder.bind(statement);
            rs = statement.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw new IllegalStateException(errorMessage, ex);
        } finally {
            closeQuietly(rs);
            closeQuietly(statement);
            closeQuietly(connection);
        }
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
            // best-effort close; primary SQLException already handled above
        }
    }

    @FunctionalInterface
    private interface SqlBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }

    private static User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("full_name"),
                Role.valueOf(rs.getString("role")),
                rs.getString("photo_path")
        );
    }
}
