import 'package:flutter/material.dart';
import 'services/auth_service.dart';
import 'screens/login_screen.dart';
import 'screens/dashboard_screen.dart';

void main() {
  runApp(const GmaoApp());
}

class GmaoApp extends StatelessWidget {
  const GmaoApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'GMAO Technicien',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        primarySwatch: Colors.indigo,
        useMaterial3: true,
        scaffoldBackgroundColor: Colors.grey[100],
      ),
      home: const AuthGate(),
    );
  }
}

/// Vérifie s'il existe une session valide (token stocké) au démarrage,
/// avant d'afficher l'écran de connexion ou directement le dashboard.
class AuthGate extends StatefulWidget {
  const AuthGate({super.key});

  @override
  State<AuthGate> createState() => _AuthGateState();
}

class _AuthGateState extends State<AuthGate> {
  final _authService = AuthService();
  bool _verificationEnCours = true;
  bool _estConnecte = false;

  @override
  void initState() {
    super.initState();
    _verifierSession();
  }

  Future<void> _verifierSession() async {
    try {
      final token = await _authService.getToken();
      if (token != null) {
        final user = await _authService.getUtilisateurCourant();
        if (mounted) {
          setState(() {
            _estConnecte = user != null;
            _verificationEnCours = false;
          });
          return;
        }
      }
    } catch (_) {}

    if (!mounted) return;
    setState(() {
      _estConnecte = false;
      _verificationEnCours = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (_verificationEnCours) {
      return const Scaffold(
        backgroundColor: Colors.white,
        body: Center(child: CircularProgressIndicator(color: Colors.indigo)),
      );
    }
    return _estConnecte ? const DashboardScreen() : const LoginScreen();
  }
}
