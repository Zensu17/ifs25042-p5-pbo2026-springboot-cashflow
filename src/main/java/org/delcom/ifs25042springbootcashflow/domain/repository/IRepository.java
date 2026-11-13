package org.delcom.ifs25042springbootcashflow.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Kontrak Generic Repository Port untuk operasi CRUD dan pencarian.
 *
 * @param <T>  tipe entitas domain
 * @param <ID> tipe data identifier entitas
 */
public interface IRepository<T, ID> {

    /** Mengambil semua data entitas. */
    List<T> findAll();

    /** Mencari satu entitas berdasarkan ID. */
    Optional<T> findById(ID id);

    /** Menyimpan entitas baru. */
    T save(T entity);

    /** Menghapus entitas berdasarkan ID. Mengembalikan true jika berhasil dihapus. */
    boolean deleteById(ID id);

    /** Menyimpan perubahan entitas yang sudah ada. */
    void update(T entity);

    /** Mencari daftar entitas yang memenuhi predikat kriteria tertentu. */
    List<T> findBy(Predicate<T> predicate);
}
