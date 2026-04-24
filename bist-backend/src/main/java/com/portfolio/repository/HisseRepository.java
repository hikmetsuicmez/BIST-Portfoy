package com.portfolio.repository;

import com.portfolio.entity.Hisse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HisseRepository extends JpaRepository<Hisse, Long> {
    Optional<Hisse> findBySembol(String sembol);
    List<Hisse> findByAktifTrue();
    boolean existsBySembol(String sembol);
}
