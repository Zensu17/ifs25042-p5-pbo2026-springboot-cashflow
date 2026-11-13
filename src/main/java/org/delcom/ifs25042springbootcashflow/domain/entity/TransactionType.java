package org.delcom.ifs25042springbootcashflow.domain.entity;

/**
 * Tipe transaksi keuangan dalam aplikasi Cashflow.
 */
public enum TransactionType {
    INCOME("Pemasukan"),
    EXPENSE("Pengeluaran");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
