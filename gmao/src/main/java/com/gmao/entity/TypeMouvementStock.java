package com.gmao.entity;

/**
 * Nature d'un {@link MouvementStock}.
 */
public enum TypeMouvementStock {
    /** Ajout de pièces dans un véhicule (réapprovisionnement depuis le dépôt). */
    APPROVISIONNEMENT,
    /** Retrait suite à l'utilisation d'une pièce lors d'une intervention. */
    CONSOMMATION,
    /** Réception de pièces transférées depuis un autre technicien. */
    TRANSFERT_ENTRANT,
    /** Envoi de pièces vers un autre technicien. */
    TRANSFERT_SORTANT,
    /** Correction manuelle (inventaire physique différent du stock théorique). */
    AJUSTEMENT
}
