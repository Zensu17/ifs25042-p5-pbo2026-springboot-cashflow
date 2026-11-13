package org.delcom.ifs25042springbootcashflow.usecase;

import org.delcom.ifs25042springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25042springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25042springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25042springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25042springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25042springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;
import java.util.Optional;

/**
 * Use case yang menangani logika bisnis aplikasi Cashflow.
 * Tidak melakukan I/O UI — hanya memproses data dan melempar checked exception
 * domain jika operasi tidak valid atau data tidak ditemukan.
 */
public class FinanceUseCase {

    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** Mengambil semua transaksi milik satu pengguna. */
    public List<Transaction> getTransactionsByUser(int userId) {
        return transactionRepository.findByUserId(userId);
    }

    /** Mengambil semua transaksi yang tersedia (admin). */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /** Menambahkan transaksi pemasukan (INCOME) baru. */
    public Transaction addIncome(int userId, String description, long amount) throws ValidationException {
        return addTransaction(userId, description, amount, TransactionType.INCOME);
    }

    /** Menambahkan transaksi pengeluaran (EXPENSE) baru. */
    public Transaction addExpense(int userId, String description, long amount) throws ValidationException {
        return addTransaction(userId, description, amount, TransactionType.EXPENSE);
    }

    /** Menambahkan transaksi baru setelah validasi keterangan, jumlah, dan tipe. */
    public Transaction addTransaction(int userId, String description, long amount, TransactionType type)
            throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan transaksi tidak boleh kosong");
        }
        if (amount <= 0) {
            throw new ValidationException("Jumlah transaksi harus lebih dari 0");
        }
        if (type == null) {
            throw new ValidationException("Tipe transaksi tidak boleh kosong");
        }
        return transactionRepository.save(userId, description.trim(), amount, type);
    }

    /** Menghitung total pemasukan (INCOME) milik pengguna. */
    public long getTotalIncome(int userId) {
        return transactionRepository.findByUserId(userId).stream()
                .filter(Transaction::isIncome)
                .mapToLong(Transaction::getAmount)
                .sum();
    }

    /** Menghitung total pengeluaran (EXPENSE) milik pengguna. */
    public long getTotalExpense(int userId) {
        return transactionRepository.findByUserId(userId).stream()
                .filter(Transaction::isExpense)
                .mapToLong(Transaction::getAmount)
                .sum();
    }

    /** Menghitung saldo bersih pengguna (total pemasukan - total pengeluaran). */
    public long getBalance(int userId) {
        return transactionRepository.findByUserId(userId).stream()
                .mapToLong(Transaction::signedAmount)
                .sum();
    }

    /** Menghapus transaksi berdasarkan ID; melempar exception jika tidak ditemukan. */
    public void removeTransaction(int id) throws EntityNotFoundException {
        boolean deleted = transactionRepository.deleteById(id);
        if (!deleted) {
            throw new EntityNotFoundException(id);
        }
    }

    /** Menghapus transaksi milik pengguna tertentu. */
    public void removeTransactionForUser(int userId, int id)
            throws EntityNotFoundException, ValidationException {
        Transaction transaction = requireOwnedTransaction(userId, id);
        removeTransaction(transaction.getId());
    }

    /** Mengubah data transaksi berdasarkan ID. */
    public void updateTransaction(
            int id,
            String description,
            Long amount,
            TransactionType type,
            String proofPath,
            boolean clearProof
    ) throws EntityNotFoundException, ValidationException {
        Optional<Transaction> found = transactionRepository.findById(id);
        if (found.isEmpty()) {
            throw new EntityNotFoundException(id);
        }
        Transaction transaction = found.get();
        applyUpdates(transaction, description, amount, type, proofPath, clearProof);
        transactionRepository.update(transaction);
    }

    /** Mengubah data transaksi milik pengguna tertentu. */
    public void updateTransactionForUser(
            int userId,
            int id,
            String description,
            Long amount,
            TransactionType type,
            String proofPath,
            boolean clearProof
    ) throws EntityNotFoundException, ValidationException {
        Transaction transaction = requireOwnedTransaction(userId, id);
        applyUpdates(transaction, description, amount, type, proofPath, clearProof);
        transactionRepository.update(transaction);
    }

    /** Mencari transaksi pengguna yang keterangannya mengandung kata kunci (case-insensitive). */
    public List<Transaction> searchTransactions(int userId, String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return transactionRepository.findByUserId(userId).stream()
                .filter(tx -> tx.getDescription().toLowerCase().contains(lowerKeyword))
                .toList();
    }

    /** Mengurutkan transaksi pengguna sesuai kriteria {@link SortOption} yang dipilih. */
    public List<Transaction> sortTransactions(int userId, SortOption option) {
        return transactionRepository.findByUserId(userId).stream()
                .sorted(option.comparator())
                .toList();
    }

    private Transaction requireOwnedTransaction(int userId, int id)
            throws EntityNotFoundException, ValidationException {
        Optional<Transaction> found = transactionRepository.findById(id);
        if (found.isEmpty()) {
            throw new EntityNotFoundException(id);
        }
        Transaction transaction = found.get();
        if (transaction.getUserId() != userId) {
            throw new ValidationException("Transaksi tidak milik pengguna ini");
        }
        return transaction;
    }

    private void applyUpdates(
            Transaction transaction,
            String description,
            Long amount,
            TransactionType type,
            String proofPath,
            boolean clearProof
    ) throws ValidationException {
        if (description != null) {
            if (description.trim().isEmpty()) {
                throw new ValidationException("Keterangan transaksi tidak boleh kosong");
            }
            transaction.changeDescription(description.trim());
        }
        if (amount != null) {
            if (amount <= 0) {
                throw new ValidationException("Jumlah transaksi harus lebih dari 0");
            }
            transaction.changeAmount(amount);
        }
        if (type != null) {
            transaction.changeType(type);
        }
        if (clearProof) {
            transaction.changeProofPath(null);
        } else if (proofPath != null) {
            transaction.changeProofPath(proofPath);
        }
    }
}
