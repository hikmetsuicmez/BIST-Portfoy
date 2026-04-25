package com.portfolio.repository;

import com.portfolio.entity.PortfoyOzetGunluk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OzetGunlukRepository extends JpaRepository<PortfoyOzetGunluk, Long> {

    Optional<PortfoyOzetGunluk> findByTarih(LocalDate tarih);

    Optional<PortfoyOzetGunluk> findByTarihAndKullaniciId(LocalDate tarih, Long kullaniciId);

    List<PortfoyOzetGunluk> findTop30ByOrderByTarihDesc();

    List<PortfoyOzetGunluk> findTop30ByKullaniciIdOrderByTarihDesc(Long kullaniciId);

    boolean existsByTarih(LocalDate tarih);

    boolean existsByTarihAndKullaniciId(LocalDate tarih, Long kullaniciId);

    @Query("SELECT o FROM PortfoyOzetGunluk o WHERE o.kullanici.id = :kullaniciId AND o.tarih BETWEEN :baslangic AND :bitis ORDER BY o.tarih ASC")
    List<PortfoyOzetGunluk> findByKullaniciIdAndTarihAraligi(@Param("kullaniciId") Long kullaniciId,
                                                               @Param("baslangic") LocalDate baslangic,
                                                               @Param("bitis") LocalDate bitis);

    @Query("SELECT o FROM PortfoyOzetGunluk o WHERE o.tarih BETWEEN :baslangic AND :bitis ORDER BY o.tarih ASC")
    List<PortfoyOzetGunluk> findByTarihAraligi(@Param("baslangic") LocalDate baslangic,
                                                @Param("bitis") LocalDate bitis);
}
