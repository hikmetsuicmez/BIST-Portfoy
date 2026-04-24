package com.portfolio.repository;

import com.portfolio.entity.PortfoyPozisyon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PozisyonRepository extends JpaRepository<PortfoyPozisyon, Long> {

    Optional<PortfoyPozisyon> findByHisseId(Long hisseId);

    List<PortfoyPozisyon> findByToplamLotGreaterThan(BigDecimal minLot);
}
