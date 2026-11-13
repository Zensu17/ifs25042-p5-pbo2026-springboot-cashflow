package org.delcom.ifs25042springbootcashflow.domain;

import org.delcom.ifs25042springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25042springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25042springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25042springbootcashflow.domain.entity.Role;
import org.delcom.ifs25042springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25042springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25042springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25042springbootcashflow.domain.entity.User;
import org.delcom.ifs25042springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25042springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25042springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25042springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25042springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25042springbootcashflow.usecase.UserUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class EntityAndUseCaseTest {

    @Test
    void passwordHasherAndExceptionsCovered() {
        String hash = PasswordHasher.hash("rahasia");
        assertEquals(64, hash.length());
        assertTrue(PasswordHasher.matches("rahasia", hash));
        assertFalse(PasswordHasher.matches("salah", hash));

        try (MockedStatic<MessageDigest> mocked = mockStatic(MessageDigest.class)) {
            mocked.when(() -> MessageDigest.getInstance("SHA-256"))
                    .thenThrow(new NoSuchAlgorithmException("missing"));
            assertThrows(IllegalStateException.class, PasswordHasher::messageDigest);
        }

        RuntimeException cause = new RuntimeException("akar");
        DomainException d1 = new DomainException("err");
        DomainException d2 = new DomainException("err", cause);
        assertEquals("err", d1.getMessage());
        assertSame(cause, d2.getCause());

        EntityNotFoundException enf = new EntityNotFoundException(99);
        assertEquals(99, enf.getId());
        assertTrue(enf.getMessage().contains("99"));

        ValidationException ve = new ValidationException("tidak valid");
        assertEquals("tidak valid", ve.getMessage());
    }

    @Test
    void transactionUserAndSortOptionCovered() {
        LocalDateTime fixedTime = LocalDateTime.of(2026, 3, 15, 10, 30);
        Transaction t1 = new Transaction(1, 10, "Gaji", 5_000_000L, TransactionType.INCOME);
        Transaction t2 = new Transaction(2, 10, "Makan", 25_000L, TransactionType.EXPENSE, "proofs/a.png");
        Transaction t3 = new Transaction(3, 10, null, 100_000L, null, null, fixedTime);

        assertEquals(1, t1.getId());
        assertEquals(10, t1.getUserId());
        assertTrue(t1.isIncome());
        assertFalse(t1.isExpense());
        assertEquals(5_000_000L, t1.signedAmount());

        assertTrue(t2.isExpense());
        assertFalse(t2.isIncome());
        assertEquals(-25_000L, t2.signedAmount());
        assertEquals("proofs/a.png", t2.getProofPath());

        assertEquals("", t3.getDescription());
        assertEquals(TransactionType.INCOME, t3.getType());
        assertEquals(fixedTime, t3.getCreatedAt());

        t1.changeDescription(null);
        assertEquals("", t1.getDescription());
        t1.changeDescription("Bonus");
        t1.changeAmount(1_500_000L);
        t1.changeType(null);
        assertEquals(TransactionType.INCOME, t1.getType());
        t1.changeType(TransactionType.EXPENSE);
        t1.changeProofPath("proofs/b.png");
        assertEquals("Bonus", t1.getDescription());
        assertEquals(1_500_000L, t1.getAmount());
        assertEquals(TransactionType.EXPENSE, t1.getType());
        assertEquals("proofs/b.png", t1.getProofPath());

        User admin = new User(1, "admin", "h", "Admin", Role.ADMIN, null);
        User regular = new User(2, "user", "h", "User", Role.USER, "p.png");
        assertTrue(admin.isAdmin());
        assertFalse(regular.isAdmin());
        regular.changeFullName("User Baru");
        regular.changePasswordHash("h2");
        regular.changePhotoPath("p2.png");
        assertEquals("User Baru", regular.getFullName());
        assertEquals("h2", regular.getPasswordHash());
        assertEquals("p2.png", regular.getPhotoPath());

        for (SortOption option : SortOption.values()) {
            List<Transaction> sorted = List.of(t1, t2).stream().sorted(option.comparator()).toList();
            assertEquals(2, sorted.size());
        }
    }

    @Test
    void financeUseCaseAndPresenterCovered() throws Exception {
        TransactionRepository repo = new TransactionRepository();
        FinanceUseCase useCase = new FinanceUseCase(repo);
        FinancePresenter presenter = new FinancePresenter();

        Transaction inc = useCase.addIncome(1, "Gaji Bulanan", 5_000_000L);
        Transaction exp = useCase.addExpense(1, "Kopi Pagi", 25_000L);
        assertEquals(5_000_000L, useCase.getTotalIncome(1));
        assertEquals(25_000L, useCase.getTotalExpense(1));
        assertEquals(4_975_000L, useCase.getBalance(1));
        assertEquals(2, useCase.getAllTransactions().size());
        assertEquals(1, useCase.searchTransactions(1, "kopi").size());
        assertEquals(2, useCase.sortTransactions(1, SortOption.AMOUNT_DESC).size());

        assertEquals("Rp 5,000,000.00", presenter.formatMoney(5_000_000L));
        assertEquals("-Rp 25,000.00", presenter.formatMoney(-25_000L));
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(4_975_000L));
        assertTrue(presenter.format(inc).contains("Rp 5,000,000.00"));
        assertTrue(presenter.showTransactions(List.of(inc, exp)).contains("Saldo: Rp 4,975,000.00"));
        assertTrue(presenter.showTransactions(List.of()).contains("belum tersedia"));
        assertTrue(presenter.showSearchResults(List.of(exp), "kopi").contains("Kopi Pagi"));
        assertTrue(presenter.showSortedTransactions(List.of(inc)).contains("Gaji Bulanan"));
        assertTrue(presenter.showAddSuccess(inc).contains("Berhasil"));
        assertNotNull(presenter.showRemoveSuccess());
        assertTrue(presenter.showRemoveFailed(9).contains("9"));
        assertNotNull(presenter.showUpdateSuccess());
        assertTrue(presenter.showUpdateFailed(9).contains("9"));
        assertNotNull(presenter.showInvalidId());
        assertTrue(presenter.showValidationError("err").contains("err"));

        useCase.updateTransaction(inc.getId(), "Gaji Pokok", 6_000_000L, TransactionType.INCOME, "proofs/slip.png", false);
        useCase.updateTransactionForUser(1, inc.getId(), null, null, null, null, true);
        assertNull(repo.findById(inc.getId()).orElseThrow().getProofPath());

        assertThrows(ValidationException.class, () -> useCase.addIncome(1, " ", 100));
        assertThrows(ValidationException.class, () -> useCase.addIncome(1, "Valid", 0));
        assertThrows(ValidationException.class, () -> useCase.addTransaction(1, "Valid", 100, null));
        assertThrows(ValidationException.class, () -> useCase.removeTransactionForUser(2, inc.getId()));
        assertThrows(EntityNotFoundException.class, () -> useCase.removeTransaction(999));

        useCase.removeTransactionForUser(1, exp.getId());
        assertEquals(1, useCase.getTransactionsByUser(1).size());
    }

    @Test
    void authAndUserUseCaseCovered() throws Exception {
        UserRepository userRepo = new UserRepository();
        AuthUseCase auth = new AuthUseCase(userRepo);
        UserUseCase users = new UserUseCase(userRepo);

        User admin = auth.ensureRootAdmin("admin", "admin");
        assertTrue(admin.isAdmin());
        assertSame(admin, auth.ensureRootAdmin("admin", "admin"));

        User registered = auth.register("budi", "1234", "Budi Santoso");
        assertEquals(Role.USER, registered.getRole());
        assertEquals(registered.getId(), auth.login("budi", "1234").getId());

        users.updateProfile(registered.getId(), "Budi Baru", "5678");
        users.updatePhoto(registered.getId(), "profiles/budi.png");
        assertEquals(2, users.listUsers().size());

        assertThrows(ValidationException.class, () -> auth.register("budi", "1234", "Dup"));
        assertThrows(ValidationException.class, () -> auth.login("budi", "salah"));
        assertThrows(EntityNotFoundException.class, () -> users.getUser(999));
    }

    private static void assertNotNull(Object value) {
        org.junit.jupiter.api.Assertions.assertNotNull(value);
    }
}
