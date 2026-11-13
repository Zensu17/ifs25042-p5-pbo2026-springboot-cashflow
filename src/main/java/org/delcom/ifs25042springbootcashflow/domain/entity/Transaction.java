package org.delcom.ifs25042springbootcashflow.domain.entity;

import java.time.LocalDateTime;

/**
 * Representasi entitas domain Transaksi Keuangan (Cashflow).
 * Tidak bergantung pada cara data disimpan ataupun cara data ditampilkan.
 */
public class Transaction {

    /** Identifier unik transaksi. */
    private final int id;

    /** Pemilik transaksi (FK ke users.id). */
    private final int userId;

    /** Keterangan transaksi. */
    private String description;

    /** Jumlah nominal uang dalam Rupiah (selalu positif). */
    private long amount;

    /** Tipe transaksi: INCOME atau EXPENSE. */
    private TransactionType type;

    /** Path relatif bukti gambar transaksi (opsional). */
    private String proofPath;

    /** Waktu transaksi dicatat. */
    private final LocalDateTime createdAt;

    /** Membuat transaksi baru tanpa bukti gambar (waktu saat ini). */
    public Transaction(int id, int userId, String description, long amount, TransactionType type) {
        this(id, userId, description, amount, type, null, LocalDateTime.now());
    }

    /** Membuat transaksi baru dengan bukti gambar (waktu saat ini). */
    public Transaction(int id, int userId, String description, long amount, TransactionType type, String proofPath) {
        this(id, userId, description, amount, type, proofPath, LocalDateTime.now());
    }

    /** Membuat transaksi lengkap dengan waktu pencatatan yang sudah ditentukan. */
    public Transaction(
            int id,
            int userId,
            String description,
            long amount,
            TransactionType type,
            String proofPath,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.description = description == null ? "" : description;
        this.amount = amount;
        this.type = type == null ? TransactionType.INCOME : type;
        this.proofPath = proofPath;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public long getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public String getProofPath() {
        return proofPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isIncome() {
        return type == TransactionType.INCOME;
    }

    public boolean isExpense() {
        return type == TransactionType.EXPENSE;
    }

    /** Nilai bertanda untuk perhitungan saldo (+amount jika INCOME, -amount jika EXPENSE). */
    public long signedAmount() {
        return isIncome() ? amount : -amount;
    }

    public void changeDescription(String description) {
        this.description = description == null ? "" : description;
    }

    public void changeAmount(long amount) {
        this.amount = amount;
    }

    public void changeType(TransactionType type) {
        if (type != null) {
            this.type = type;
        }
    }

    public void changeProofPath(String proofPath) {
        this.proofPath = proofPath;
    }
}
