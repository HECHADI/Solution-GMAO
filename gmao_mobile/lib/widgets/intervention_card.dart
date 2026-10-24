import 'package:flutter/material.dart';
import '../models/intervention.dart';

class InterventionCard extends StatelessWidget {
  final Intervention intervention;
  final VoidCallback onTap;

  const InterventionCard({
    super.key,
    required this.intervention,
    required this.onTap,
  });

  String _formatTime(DateTime? date) {
    if (date == null) return '--:--';
    return '${date.hour.toString().padLeft(2, '0')}:${date.minute.toString().padLeft(2, '0')}';
  }

  Color _getStatusColor() {
    switch (intervention.statut) {
      case InterventionStatus.aFaire:
        return Colors.blue;
      case InterventionStatus.enCours:
        return Colors.orange;
      case InterventionStatus.realise:
        return Colors.green;
    }
  }

  String _getStatusText() {
    switch (intervention.statut) {
      case InterventionStatus.aFaire:
        return 'À FAIRE';
      case InterventionStatus.enCours:
        return 'EN COURS';
      case InterventionStatus.realise:
        return 'RÉALISÉ';
    }
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 2,
      margin: const EdgeInsets.only(bottom: 16),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Ligne 1 : Heure et Statut
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Row(
                    children: [
                      const Icon(
                        Icons.access_time,
                        size: 18,
                        color: Colors.grey,
                      ),
                      const SizedBox(width: 6),
                      Text(
                        _formatTime(intervention.dateDebut),
                        style: const TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      if (intervention.dureeEstimee != null)
                        Text(
                          ' (${intervention.dureeEstimee} min)',
                          style: const TextStyle(
                            color: Colors.grey,
                            fontSize: 14,
                          ),
                        ),
                    ],
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 10,
                      vertical: 4,
                    ),
                    decoration: BoxDecoration(
                      color: _getStatusColor().withValues(alpha: 0.15),
                      borderRadius: BorderRadius.circular(20),
                      border: Border.all(
                        color: _getStatusColor().withValues(alpha: 0.5),
                      ),
                    ),
                    child: Text(
                      _getStatusText(),
                      style: TextStyle(
                        color: _getStatusColor(),
                        fontWeight: FontWeight.bold,
                        fontSize: 12,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),

              // Ligne 2 : Titre de l'intervention
              Text(
                intervention.titre,
                style: const TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.w600,
                ),
              ),
              const SizedBox(height: 12),

              // Ligne 3 : Client et Site
              Row(
                children: [
                  const Icon(Icons.business, size: 16, color: Colors.indigo),
                  const SizedBox(width: 6),
                  Expanded(
                    child: Text(
                      intervention.clientNom,
                      style: const TextStyle(
                        fontSize: 14,
                        color: Colors.black87,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 6),
              Row(
                children: [
                  const Icon(
                    Icons.location_on,
                    size: 16,
                    color: Colors.redAccent,
                  ),
                  const SizedBox(width: 6),
                  Expanded(
                    child: Text(
                      intervention.siteNom,
                      style: const TextStyle(
                        fontSize: 14,
                        color: Colors.black54,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
