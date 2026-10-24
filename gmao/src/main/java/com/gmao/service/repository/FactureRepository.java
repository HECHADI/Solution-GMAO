package com.gmao.service.repository;

import com.gmao.entity.Facture;
import com.gmao.entity.StatutFacture;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FactureRepository extends JpaRepository<Facture, Integer> {

    List<Facture> findByClientId(Integer clientId);

    List<Facture> findByChantierId(Integer chantierId);

    List<Facture> findByStatut(StatutFacture statut);

    @EntityGraph(attributePaths = {"lignes", "lignes.intervention"})
    Optional<Facture> findWithLignesById(Integer id);

    @EntityGraph(attributePaths = {"lignes", "lignes.intervention"})
    List<Facture> findAllWithLignesBy();

    long countByNumeroStartingWith(String prefixe);
}
