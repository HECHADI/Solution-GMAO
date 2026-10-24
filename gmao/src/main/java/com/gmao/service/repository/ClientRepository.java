package com.gmao.service.repository;

import com.gmao.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository Spring Data JPA pour l'entité {@link Client}.
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
    
    /**
     * Vérifie si un client existe avec cette adresse e-mail.
     */
    boolean existsByEmail(String email);
}