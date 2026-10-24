package com.gmao.service.repository;

import com.gmao.entity.StockVehicule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockVehiculeRepository extends JpaRepository<StockVehicule, Integer> {

    List<StockVehicule> findByTechnicienId(Integer technicienId);

    Optional<StockVehicule> findByTechnicienIdAndPieceId(Integer technicienId, Integer pieceId);
}
