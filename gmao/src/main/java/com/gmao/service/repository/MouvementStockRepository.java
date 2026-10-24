package com.gmao.service.repository;

import com.gmao.entity.MouvementStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MouvementStockRepository extends JpaRepository<MouvementStock, Integer> {

    List<MouvementStock> findByPieceIdOrderByCreatedAtDesc(Integer pieceId);

    List<MouvementStock> findByTechnicienIdOrderByCreatedAtDesc(Integer technicienId);

    List<MouvementStock> findAllByOrderByCreatedAtDesc();
}
