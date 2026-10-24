import 'package:flutter/material.dart';
import '../models/intervention.dart';
import '../services/api_service.dart';
import '../services/auth_service.dart';
import '../widgets/intervention_card.dart';
import 'login_screen.dart';
import 'intervention_details_screen.dart';

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key});

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  final ApiService _apiService = ApiService();

  List<Intervention> _interventions = [];
  bool _isLoading = true;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _chargerInterventions();
  }

  Future<void> _chargerInterventions() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final data = await _apiService.fetchInterventionsDuJour();
      setState(() {
        _interventions = data;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        // ⚠️ TEMPORAIRE pour diagnostic — remets le message générique une
        // fois le problème identifié et corrigé.
        _errorMessage = "Erreur : $e";
        _isLoading = false;
      });
    }
  }

  Future<void> _seDeconnecter() async {
    await AuthService().logout();
    if (!mounted) return;
    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (_) => const LoginScreen()),
      (route) => false,
    );
  }

  @override
  Widget build(BuildContext context) {
    // Calcul de la date du jour
    final today = DateTime.now();
    final dateDuJourStr =
        "${today.day.toString().padLeft(2, '0')}/${today.month.toString().padLeft(2, '0')}/${today.year}";

    return Scaffold(
      backgroundColor: Colors.grey[100],
      appBar: AppBar(
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Mes Interventions',
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
            Text(
              "Aujourd'hui, $dateDuJourStr",
              style: const TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.normal,
              ),
            ),
          ],
        ),
        backgroundColor: Colors.indigo,
        foregroundColor: Colors.white,
        elevation: 0,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _chargerInterventions,
          ),
          IconButton(
            icon: const Icon(Icons.logout),
            tooltip: 'Déconnexion',
            onPressed: _seDeconnecter,
          ),
        ],
      ),
      body: _buildBody(),
    );
  }

  Widget _buildBody() {
    if (_isLoading) {
      return const Center(
        child: CircularProgressIndicator(color: Colors.indigo),
      );
    }

    if (_errorMessage != null) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.error_outline, size: 48, color: Colors.redAccent),
            const SizedBox(height: 16),
            Text(_errorMessage!, style: const TextStyle(fontSize: 16)),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: _chargerInterventions,
              child: const Text('Réessayer'),
            ),
          ],
        ),
      );
    }

    if (_interventions.isEmpty) {
      return const Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.check_circle_outline, size: 64, color: Colors.green),
            SizedBox(height: 16),
            Text(
              "Aucune intervention prévue aujourd'hui.",
              style: TextStyle(fontSize: 16, color: Colors.grey),
            ),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: _chargerInterventions,
      color: Colors.indigo,
      child: ListView.builder(
        padding: const EdgeInsets.all(16.0),
        itemCount: _interventions.length,
        itemBuilder: (context, index) {
          final intervention = _interventions[index];
          return InterventionCard(
            intervention: intervention,
            onTap: () async {
              await Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (_) =>
                      InterventionDetailsScreen(intervention: intervention),
                ),
              );
              // Au retour (après clôture ou annulation), on rafraîchit la liste
              _chargerInterventions();
            },
          );
        },
      ),
    );
  }
}
