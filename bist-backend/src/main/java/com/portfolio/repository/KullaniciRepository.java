package com.portfolio.repository;

import com.portfolio.entity.Kullanici;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KullaniciRepository extends JpaRepository<Kullanici, Long> {
    Optional<Kullanici> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Kullanici> findByAktifTrue();
}
