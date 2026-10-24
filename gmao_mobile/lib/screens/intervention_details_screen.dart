import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:signature/signature.dart';
import '../models/intervention.dart';
import '../models/cloture_data.dart';
import '../services/api_service.dart';

class InterventionDetailsScreen extends StatefulWidget {
  final Intervention intervention;

  const InterventionDetailsScreen({super.key, required this.intervention});

  @override
  State<InterventionDetailsScreen> createState() =>
      _InterventionDetailsScreenState();
}

class _InterventionDetailsScreenState extends State<InterventionDetailsScreen> {
  final ApiService _apiService = ApiService();
  bool _isSubmitting = false;

  // 1. Checklist
  final List<Map<String, dynamic>> _checklist = [
    {'task': 'Mise en sécurité (EPI, Consignation)', 'done': false},
    {'task': 'Diagnostic visuel de l\'équipement', 'done': false},
    {'task': 'Intervention technique selon consigne', 'done': false},
    {'task': 'Nettoyage de la zone de travail', 'done': false},
  ];

  // 2. Compte Rendu Textuel
  final TextEditingController _reportController = TextEditingController();

  // 3. Photo
  final ImagePicker _picker = ImagePicker();
  XFile? _photo;

  // 4. Signature
  final SignatureController _signatureController = SignatureController(
    penStrokeWidth: 3,
    penColor: Colors.black,
    exportBackgroundColor: Colors.grey[200]!,
  );

  @override
  void dispose() {
    _reportController.dispose();
    _signatureController.dispose();
    super.dispose();
  }

  Future<void> _takePhoto() async {
    // Permet de prendre une photo via la caméra
    final XFile? image = await _picker.pickImage(source: ImageSource.camera);
    if (image != null) {
      setState(() {
        _photo = image;
      });
    }
  }

  Future<void> _submitCloture() async {
    if (_signatureController.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Veuillez faire signer le client pour clôturer.'),
          backgroundColor: Colors.redAccent,
        ),
      );
      return;
    }

    setState(() => _isSubmitting = true);

    try {
      // Export de la signature en image PNG (Bytes)
      final signatureBytes = await _signatureController.toPngBytes();

      final data = ClotureData(
        checklist: _checklist.map((item) => item['done'] as bool).toList(),
        compteRendu: _reportController.text,
        photoPath: _photo?.path,
        signatureBytes: signatureBytes,
      );

      // Appel API
      await _apiService.cloturerIntervention(widget.intervention.id, data);

      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Intervention clôturée avec succès !'),
          backgroundColor: Colors.green,
        ),
      );
      Navigator.pop(context); // Retour au Dashboard
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Erreur lors de la clôture.'),
          backgroundColor: Colors.red,
        ),
      );
    } finally {
      if (mounted) setState(() => _isSubmitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[100],
      appBar: AppBar(
        title: const Text(
          "Détails de l'intervention",
          style: TextStyle(fontSize: 18),
        ),
        backgroundColor: Colors.indigo,
        foregroundColor: Colors.white,
      ),
      body: _isSubmitting
          ? const Center(child: CircularProgressIndicator(color: Colors.indigo))
          : SingleChildScrollView(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _buildHeaderInfo(),
                  const SizedBox(height: 20),
                  _buildSafetyAndHistory(),
                  const SizedBox(height: 20),
                  _buildChecklist(),
                  const SizedBox(height: 20),
                  _buildReportField(),
                  const SizedBox(height: 20),
                  _buildPhotoSection(),
                  const SizedBox(height: 20),
                  _buildSignatureSection(),
                  const SizedBox(height: 32),

                  // Bouton de clôture
                  SizedBox(
                    width: double.infinity,
                    height: 54,
                    child: ElevatedButton.icon(
                      onPressed: _submitCloture,
                      icon: const Icon(Icons.check_circle),
                      label: const Text(
                        'Clôturer l\'intervention',
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.green,
                        foregroundColor: Colors.white,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(height: 32),
                ],
              ),
            ),
    );
  }

  Widget _buildHeaderInfo() {
    return Card(
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(color: Colors.grey.shade300),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              widget.intervention.titre,
              style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            const Divider(height: 20),
            Row(
              children: [
                const Icon(Icons.business, size: 18, color: Colors.indigo),
                const SizedBox(width: 8),
                Text(
                  widget.intervention.clientNom,
                  style: const TextStyle(fontSize: 16),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                const Icon(
                  Icons.location_on,
                  size: 18,
                  color: Colors.redAccent,
                ),
                const SizedBox(width: 8),
                Text(
                  widget.intervention.siteNom,
                  style: const TextStyle(fontSize: 16),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSafetyAndHistory() {
    return Card(
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(color: Colors.grey.shade300),
      ),
      child: ExpansionTile(
        leading: const Icon(Icons.warning_amber_rounded, color: Colors.orange),
        title: const Text(
          "Consignes & Historique",
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        children: [
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: const [
                Text(
                  "⚠️ Consignes de sécurité :",
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                    color: Colors.red,
                  ),
                ),
                SizedBox(height: 4),
                Text(
                  "- Port des EPI obligatoire (Gants, Lunettes)\n- Consignation électrique requise avant intervention.",
                ),
                Divider(height: 24),
                Text(
                  "📜 Historique de l'équipement :",
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                    color: Colors.indigo,
                  ),
                ),
                SizedBox(height: 4),
                Text(
                  "- 12/05/2026 : Remplacement filtre\n- 04/01/2026 : Maintenance annuelle (RAS)",
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildChecklist() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          "Tâches à accomplir",
          style: TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.bold,
            color: Colors.indigo,
          ),
        ),
        const SizedBox(height: 8),
        Card(
          elevation: 0,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
            side: BorderSide(color: Colors.grey.shade300),
          ),
          child: Column(
            children: _checklist.asMap().entries.map((entry) {
              int idx = entry.key;
              Map<String, dynamic> item = entry.value;
              return CheckboxListTile(
                title: Text(
                  item['task'],
                  style: TextStyle(
                    decoration: item['done']
                        ? TextDecoration.lineThrough
                        : null,
                  ),
                ),
                value: item['done'],
                activeColor: Colors.green,
                onChanged: (bool? val) {
                  setState(() => _checklist[idx]['done'] = val!);
                },
              );
            }).toList(),
          ),
        ),
      ],
    );
  }

  Widget _buildReportField() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          "Compte Rendu",
          style: TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.bold,
            color: Colors.indigo,
          ),
        ),
        const SizedBox(height: 8),
        TextField(
          controller: _reportController,
          maxLines: 4,
          decoration: InputDecoration(
            hintText: "Décrivez l'intervention, les pièces changées...",
            filled: true,
            fillColor: Colors.white,
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: BorderSide(color: Colors.grey.shade300),
            ),
            enabledBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: BorderSide(color: Colors.grey.shade300),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildPhotoSection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          "Preuve visuelle (Photo)",
          style: TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.bold,
            color: Colors.indigo,
          ),
        ),
        const SizedBox(height: 8),
        Row(
          children: [
            ElevatedButton.icon(
              onPressed: _takePhoto,
              icon: const Icon(Icons.camera_alt),
              label: const Text("Prendre une photo"),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.white,
                foregroundColor: Colors.indigo,
              ),
            ),
            const SizedBox(width: 16),
            if (_photo != null)
              const Row(
                children: [
                  Icon(Icons.check_circle, color: Colors.green),
                  SizedBox(width: 4),
                  Text(
                    "Photo ajoutée",
                    style: TextStyle(
                      color: Colors.green,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
          ],
        ),
      ],
    );
  }

  Widget _buildSignatureSection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            const Text(
              "Signature du client",
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: Colors.indigo,
              ),
            ),
            TextButton(
              onPressed: () => _signatureController.clear(),
              child: const Text("Effacer", style: TextStyle(color: Colors.red)),
            ),
          ],
        ),
        const SizedBox(height: 8),
        ClipRRect(
          borderRadius: BorderRadius.circular(12),
          child: Container(
            decoration: BoxDecoration(
              border: Border.all(color: Colors.grey.shade300),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Signature(
              controller: _signatureController,
              height: 150,
              backgroundColor: Colors.grey.shade200,
            ),
          ),
        ),
      ],
    );
  }
}
