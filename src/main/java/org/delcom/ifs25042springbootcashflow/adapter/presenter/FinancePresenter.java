package org.delcom.ifs25042springbootcashflow.adapter.presenter;

import org.delcom.ifs25042springbootcashflow.domain.entity.Transaction;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Presenter yang memformat data dari use case menjadi teks siap tampil.
 * Memisahkan logika tampilan dari entity, use case, dan view.
 */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Memformat nominal uang ke format standar modul: Rp 1,000,000.00
     * Jika negatif (misal saldo minus), ditampilkan -Rp 1,000,000.00.
     */
    public String formatMoney(long amount) {
        if (amount < 0) {
            return String.format(Locale.US, "-Rp %,d.00", Math.abs(amount));
        }
        return String.format(Locale.US, "Rp %,d.00", amount);
    }

    /**
     * Memformat satu transaksi sesuai acuan modul:
     * [<id>] <keterangan> | Rp 1,000,000.00 | [<Pemasukan/Pengeluaran>]
     */
    public String format(Transaction transaction) {
        return String.format(
                "[%d] %s | %s | [%s]",
                transaction.getId(),
                transaction.getDescription(),
                formatMoney(transaction.getAmount()),
                transaction.getType().getLabel()
        );
    }

    /**
     * Memformat teks saldo sesuai acuan modul:
     * Saldo: Rp 4,975,000.00
     */
    public String formatBalance(long balance) {
        return "Saldo: " + formatMoney(balance);
    }

    /** Memformat tanggal pencatatan transaksi. */
    public String formatCreatedAt(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_TIME_FORMAT);
    }

    /**
     * Memformat daftar transaksi menjadi teks multi-baris beserta ringkasan saldo.
     */
    public String formatList(List<Transaction> transactions, String header, String emptyMessage) {
        StringBuilder builder = new StringBuilder(header);
        if (transactions.isEmpty()) {
            builder.append('\n').append(emptyMessage);
            return builder.toString();
        }
        String body = transactions.stream()
                .map(this::format)
                .collect(Collectors.joining("\n"));
        long balance = transactions.stream()
                .mapToLong(Transaction::signedAmount)
                .sum();
        builder.append('\n').append(body)
                .append('\n').append(formatBalance(balance));
        return builder.toString();
    }

    /** Memformat daftar semua transaksi. */
    public String showTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi:", "- Data transaksi belum tersedia!");
    }

    /** Memformat hasil pencarian berdasarkan kata kunci. */
    public String showSearchResults(List<Transaction> transactions, String keyword) {
        return formatList(transactions, "Hasil Pencarian: \"" + keyword + "\"", "- Transaksi tidak ditemukan!");
    }

    /** Memformat daftar transaksi yang sudah diurutkan. */
    public String showSortedTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi (Terurut):", "- Data transaksi belum tersedia!");
    }

    /** Pesan sukses setelah menambah transaksi. */
    public String showAddSuccess(Transaction transaction) {
        return "Berhasil menambah transaksi: " + format(transaction);
    }

    /** Pesan sukses setelah menghapus transaksi. */
    public String showRemoveSuccess() {
        return "Berhasil menghapus transaksi.";
    }

    /** Pesan gagal saat menghapus transaksi. */
    public String showRemoveFailed(int id) {
        return String.format("[!] Gagal menghapus transaksi dengan ID: %d.", id);
    }

    /** Pesan sukses setelah mengubah transaksi. */
    public String showUpdateSuccess() {
        return "Berhasil mengubah transaksi.";
    }

    /** Pesan gagal saat mengubah transaksi. */
    public String showUpdateFailed(int id) {
        return String.format("[!] Gagal mengubah transaksi dengan ID: %d.", id);
    }

    /** Pesan saat ID tidak valid. */
    public String showInvalidId() {
        return "[!] ID tidak valid!";
    }

    /** Pesan validasi umum dari domain. */
    public String showValidationError(String message) {
        return "[!] " + message;
    }
}
