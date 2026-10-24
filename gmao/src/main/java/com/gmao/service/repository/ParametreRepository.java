package com.gmao.service.repository;

import com.gmao.entity.ParametreApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParametreRepository extends JpaRepository<ParametreApplication, Integer> {
    Optional<ParametreApplication> findByCle(String cle);
}
