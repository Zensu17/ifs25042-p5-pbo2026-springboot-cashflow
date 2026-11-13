package org.delcom.ifs25042springbootcashflow.domain.entity;

import java.util.Comparator;

/**
 * Kriteria pengurutan transaksi keuangan.
 * Setiap opsi membawa comparator-nya sendiri sehingga logika pengurutan
 * terpusat di domain.
 */
public enum SortOption {

    /** Urutkan jumlah dari terkecil ke terbesar. */
    AMOUNT_ASC(Comparator.comparingLong(Transaction::getAmount)),

    /** Urutkan jumlah dari terbesar ke terkecil. */
    AMOUNT_DESC(Comparator.comparingLong(Transaction::getAmount).reversed()),

    /** Tampilkan transaksi Pemasukan (INCOME) terlebih dahulu. */
    INCOME_FIRST(Comparator.comparing(Transaction::isIncome).reversed()),

    /** Tampilkan transaksi Pengeluaran (EXPENSE) terlebih dahulu. */
    EXPENSE_FIRST(Comparator.comparing(Transaction::isIncome)),

    /** Urutkan keterangan dari A ke Z (case-insensitive). */
    DESCRIPTION_ASC(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER)),

    /** Urutkan keterangan dari Z ke A (case-insensitive). */
    DESCRIPTION_DESC(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER).reversed());

    private final Comparator<Transaction> comparator;

    SortOption(Comparator<Transaction> comparator) {
        this.comparator = comparator;
    }

    /** Mengembalikan comparator yang sesuai dengan opsi ini. */
    public Comparator<Transaction> comparator() {
        return comparator;
    }
}
