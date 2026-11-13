package org.delcom.ifs25042springbootcashflow.adapter.repository;

import org.delcom.ifs25042springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25042springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25042springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;

/**
 * Implementasi repository transaksi menggunakan basis generic {@link InMemoryRepository}.
 * Dipakai pada unit test; produksi memakai {@link JdbcTransactionRepository}.
 */
public class TransactionRepository extends InMemoryRepository<Transaction, Integer> implements ITransactionRepository {

    private int idCounter = 0;

    public TransactionRepository() {
        super(Transaction::getId);
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type) {
        Transaction transaction = new Transaction(nextId(), userId, description, amount, type);
        return save(transaction);
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return findBy(tx -> tx.getUserId() == userId);
    }

    private int nextId() {
        return ++idCounter;
    }
}
