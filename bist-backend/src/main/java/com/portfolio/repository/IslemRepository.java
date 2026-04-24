package com.portfolio.repository;

import com.portfolio.entity.PortfoyIslem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IslemRepository extends JpaRepository<PortfoyIslem, Long> {

    List<PortfoyIslem> findByHisseIdOrderByTarihDesc(Long hisseId);

    List<PortfoyIslem> findAllByOrderByTarihDesc();

    @Query("SELECT i FROM PortfoyIslem i WHERE i.tarih BETWEEN :baslangic AND :bitis ORDER BY i.tarih DESC")
    List<PortfoyIslem> findByTarihAraligi(@Param("baslangic") LocalDate baslangic,
                                           @Param("bitis") LocalDate bitis);
}
