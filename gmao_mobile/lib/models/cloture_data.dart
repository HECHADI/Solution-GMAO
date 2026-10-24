import 'dart:typed_data';

/// Modèle contenant les données de clôture de l'intervention
class ClotureData {
  final List<bool> checklist;
  final String compteRendu;
  final String? photoPath; 
  final Uint8List? signatureBytes;

  ClotureData({
    required this.checklist,
    required this.compteRendu,
    this.photoPath,
    this.signatureBytes,
  });
}
