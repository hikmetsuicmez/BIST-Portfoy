package com.portfolio.repository;

import com.portfolio.entity.Temmettu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TemmettuRepository extends JpaRepository<Temmettu, Long> {

    List<Temmettu> findByKullaniciIdOrderByOdemeTarihiDesc(Long kullaniciId);

    List<Temmettu> findByKullaniciIdAndHisseId(Long kullaniciId, Long hisseId);

    List<Temmettu> findByKullaniciIdAndYil(Long kullaniciId, Integer yil);

    boolean existsByHisseIdAndKullaniciIdAndYil(Long hisseId, Long kullaniciId, Integer yil);

    @Query("SELECT COALESCE(SUM(t.toplamNet), 0) FROM Temmettu t WHERE t.kullanici.id = :kullaniciId")
    BigDecimal toplamNetTemmettu(@Param("kullaniciId") Long kullaniciId);

    @Query("SELECT COALESCE(SUM(t.toplamNet), 0) FROM Temmettu t WHERE t.kullanici.id = :kullaniciId AND t.yil = :yil")
    BigDecimal toplamNetTemmettuByYil(@Param("kullaniciId") Long kullaniciId, @Param("yil") Integer yil);
}
