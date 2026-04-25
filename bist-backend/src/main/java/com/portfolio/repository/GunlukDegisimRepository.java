package com.portfolio.repository;

import com.portfolio.entity.PortfoyGunlukDegisim;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GunlukDegisimRepository extends JpaRepository<PortfoyGunlukDegisim, Long> {

    List<PortfoyGunlukDegisim> findByTarihOrderByHisseAsc(LocalDate tarih);

    Optional<PortfoyGunlukDegisim> findByTarihAndHisseId(LocalDate tarih, Long hisseId);

    List<PortfoyGunlukDegisim> findByTarihAndKullaniciId(LocalDate tarih, Long kullaniciId);

    @Query("SELECT g FROM PortfoyGunlukDegisim g WHERE g.hisse.id = :hisseId ORDER BY g.tarih DESC")
    List<PortfoyGunlukDegisim> findByHisseIdOrderByTarihDesc(@Param("hisseId") Long hisseId, Pageable pageable);

    boolean existsByTarihAndHisseId(LocalDate tarih, Long hisseId);
}
