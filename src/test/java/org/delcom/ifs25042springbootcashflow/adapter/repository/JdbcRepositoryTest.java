package org.delcom.ifs25042springbootcashflow.adapter.repository;

import org.delcom.ifs25042springbootcashflow.domain.entity.Role;
import org.delcom.ifs25042springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25042springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25042springbootcashflow.domain.entity.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcRepositoryTest {

    private JdbcUserRepository userRepository;
    private JdbcTransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        DataSource dataSource = createDataSource();
        SchemaInitializer.initialize(dataSource);
        userRepository = new JdbcUserRepository(dataSource);
        transactionRepository = new JdbcTransactionRepository(dataSource);
    }

    private DataSource createDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:jdbc_cf_" + System.nanoTime() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    @Test
    void userAndTransactionCrud() {
        assertTrue(userRepository.findAll().isEmpty());
        User owner = userRepository.saveNew("andi", "hash", "Andi", Role.USER);
        assertTrue(owner.getId() > 0);
        assertTrue(userRepository.findByUsername("ANDI").isPresent());
        assertTrue(userRepository.findByUsername("missing").isEmpty());
        assertEquals(1, userRepository.findAll().size());
        assertEquals(owner.getId(), userRepository.findById(owner.getId()).orElseThrow().getId());
        assertTrue(userRepository.findById(999).isEmpty());
        assertEquals(1, userRepository.findBy(user -> user.getUsername().equals("andi")).size());

        owner.changeFullName("Andi Baru");
        owner.changePasswordHash("hash-baru");
        owner.changePhotoPath("profiles/andi.png");
        userRepository.update(owner);
        User updatedOwner = userRepository.findById(owner.getId()).orElseThrow();
        assertEquals("Andi Baru", updatedOwner.getFullName());
        assertEquals("hash-baru", updatedOwner.getPasswordHash());
        assertEquals("profiles/andi.png", updatedOwner.getPhotoPath());
        User savedCopy = userRepository.save(new User(0, "budi", "hash", "Budi", Role.USER, null));
        assertTrue(savedCopy.getId() > owner.getId());

        Transaction t1 = transactionRepository.save(owner.getId(), "Gaji", 5_000_000L, TransactionType.INCOME);
        Transaction t2 = transactionRepository.save(new Transaction(0, owner.getId(), "Makan", 25_000L, TransactionType.EXPENSE));
        transactionRepository.save(savedCopy.getId(), "Orang lain", 1_000L, TransactionType.INCOME);
        assertTrue(t1.getId() > 0);
        assertTrue(t2.getId() > 0);

        assertEquals(3, transactionRepository.findAll().size());
        assertEquals(2, transactionRepository.findByUserId(owner.getId()).size());
        assertTrue(transactionRepository.findById(t1.getId()).isPresent());
        assertTrue(transactionRepository.findById(999).isEmpty());
        assertEquals(1, transactionRepository.findBy(tx -> tx.getDescription().equals("Gaji")).size());

        t1.changeDescription("Gaji Pokok");
        t1.changeAmount(5_500_000L);
        t1.changeProofPath("proofs/slip.png");
        transactionRepository.update(t1);

        Transaction updated = transactionRepository.findById(t1.getId()).orElseThrow();
        assertEquals("Gaji Pokok", updated.getDescription());
        assertEquals(5_500_000L, updated.getAmount());
        assertEquals("proofs/slip.png", updated.getProofPath());

        assertTrue(transactionRepository.deleteById(t2.getId()));
        assertFalse(transactionRepository.deleteById(t2.getId()));
        assertEquals(1, transactionRepository.findBy(tx -> tx.getUserId() == owner.getId()).size());

        List<Transaction> remaining = transactionRepository.findByUserId(owner.getId());
        assertEquals(1, remaining.size());

        assertTrue(transactionRepository.deleteById(t1.getId()));
        assertTrue(userRepository.deleteById(owner.getId()));
        assertFalse(userRepository.deleteById(owner.getId()));
    }

    @Test
    void schemaInitializerReportsConnectionFailure() throws SQLException {
        DataSource brokenDataSource = Mockito.mock(DataSource.class);
        Mockito.when(brokenDataSource.getConnection()).thenThrow(new SQLException("offline"));

        IllegalStateException error = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> SchemaInitializer.initialize(brokenDataSource));

        assertTrue(error.getMessage().contains("Gagal menginisialisasi schema database"));
    }
}
