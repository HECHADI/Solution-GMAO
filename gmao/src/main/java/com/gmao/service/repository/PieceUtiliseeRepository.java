package com.gmao.service.repository; // 💡 Ajuste le package selon l'emplacement de tes autres repositories

import com.gmao.entity.PieceUtilisee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PieceUtiliseeRepository extends JpaRepository<PieceUtilisee, Integer> {
    // 🟢 Permet au service de facturation de retrouver tout ce qui a été consommé sur l'intervention
    List<PieceUtilisee> findByInterventionId(Integer interventionId);
}