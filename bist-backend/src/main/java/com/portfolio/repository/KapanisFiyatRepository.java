package com.portfolio.repository;

import com.portfolio.entity.KapanisFiyat;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface KapanisFiyatRepository extends JpaRepository<KapanisFiyat, Long> {

    Optional<KapanisFiyat> findByHisseIdAndTarih(Long hisseId, LocalDate tarih);

    List<KapanisFiyat> findByTarihOrderByHisseAsc(LocalDate tarih);

    @Query("SELECT k FROM KapanisFiyat k WHERE k.hisse.id = :hisseId ORDER BY k.tarih DESC")
    List<KapanisFiyat> findByHisseIdOrderByTarihDesc(@Param("hisseId") Long hisseId, Pageable pageable);

    @Query("SELECT k FROM KapanisFiyat k WHERE k.hisse.id = :hisseId AND k.tarih < :tarih ORDER BY k.tarih DESC")
    List<KapanisFiyat> findPreviousByHisseId(@Param("hisseId") Long hisseId,
                                              @Param("tarih") LocalDate tarih,
                                              Pageable pageable);
}
