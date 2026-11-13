package org.delcom.ifs25042springbootcashflow.adapter.repository;

import org.delcom.ifs25042springbootcashflow.domain.entity.Role;
import org.delcom.ifs25042springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25042springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25042springbootcashflow.domain.entity.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
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
        User owner = userRepository.saveNew("andi", "hash", "Andi", Role.USER);
        assertTrue(owner.getId() > 0);
        assertTrue(userRepository.findByUsername("ANDI").isPresent());

        Transaction t1 = transactionRepository.save(owner.getId(), "Gaji", 5_000_000L, TransactionType.INCOME);
        Transaction t2 = transactionRepository.save(new Transaction(0, owner.getId(), "Makan", 25_000L, TransactionType.EXPENSE));
        assertTrue(t1.getId() > 0);
        assertTrue(t2.getId() > 0);

        assertEquals(2, transactionRepository.findAll().size());
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

        List<Transaction> remaining = transactionRepository.findByUserId(owner.getId());
        assertEquals(1, remaining.size());
    }
}
