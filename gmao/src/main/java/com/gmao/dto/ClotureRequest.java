package com.gmao.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO reçu depuis l'application mobile lors de la clôture d'une intervention.
 */
@Data
public class ClotureRequest {
    private List<Boolean> checklist;
    private String compteRendu;
    private String signatureBase64;
    private String photoBase64;
}
