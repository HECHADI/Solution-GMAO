package com.gmao.service.repository;

import com.gmao.entity.Chantier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChantierRepository extends JpaRepository<Chantier, Integer> {

    List<Chantier> findBySiteId(Integer siteId);
}
