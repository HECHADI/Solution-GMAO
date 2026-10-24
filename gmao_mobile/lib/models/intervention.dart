enum InterventionStatus { aFaire, enCours, realise }

enum InterventionType { preventif, curatif }

class Intervention {
  final int id;
  final String titre;
  final String clientNom;
  final String siteNom;
  final DateTime? dateDebut;
  final int? dureeEstimee;
  final InterventionStatus statut;
  final InterventionType type;

  Intervention({
    required this.id,
    required this.titre,
    required this.clientNom,
    required this.siteNom,
    this.dateDebut,
    this.dureeEstimee,
    required this.statut,
    required this.type,
  });

  factory Intervention.fromJson(Map<String, dynamic> json) {
    // 🟢 1. Récupération sécurisée du titre / description (remplacement si NULL)
    final String titreExtrait =
        json['titre'] ??
        json['description'] ??
        json['libelle'] ??
        'Intervention #${json['id'] ?? 0}';

    // 🟢 2. Récupération sécurisée du client
    final String clientExtrait =
        json['clientNom'] ??
        (json['client'] != null ? json['client']['nom'] : null) ??
        'Client non spécifié';

    // 🟢 3. Récupération sécurisée du site / équipement / chantier
    final String siteExtrait =
        json['siteNom'] ??
        (json['chantier'] != null ? json['chantier']['nom'] : null) ??
        (json['equipement'] != null ? json['equipement']['nom'] : null) ??
        'Site principal';

    return Intervention(
      id: json['id'] ?? 0,
      titre: titreExtrait,
      clientNom: clientExtrait,
      siteNom: siteExtrait,
      dateDebut: json['dateDebut'] != null
          ? DateTime.tryParse(json['dateDebut'].toString())
          : null,
      dureeEstimee: json['dureeEstimee'] ?? json['dureeHeures'],
      statut: _parseStatus(json['statut']?.toString()),
      type: json['type']?.toString().toUpperCase() == 'PREVENTIF'
          ? InterventionType.preventif
          : InterventionType.curatif,
    );
  }

  static InterventionStatus _parseStatus(String? statusStr) {
    switch (statusStr?.toUpperCase()) {
      case 'EN_COURS':
        return InterventionStatus.enCours;
      case 'REALISE':
      case 'TERMINE':
        return InterventionStatus.realise;
      case 'A_FAIRE':
      default:
        return InterventionStatus.aFaire;
    }
  }
}
