import 'dart:convert';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:http/http.dart' as http;

import '../config/api_config.dart';
import '../models/utilisateur.dart';

/// ⚠️ flutter_secure_storage gère déjà le web nativement (WebCrypto) —
/// pas besoin de dart:html en plus. Cet import casse la compilation dès
/// que tu essaies de builder pour mobile/desktop (dart:html n'existe que
/// sur la cible web), donc je l'ai retiré.
class AuthService {
  final _storage = const FlutterSecureStorage();
  static const String _tokenKey = 'jwt_token';

  Future<void> _writeToken(String token) async {
    await _storage.write(key: _tokenKey, value: token);
  }

  Future<String?> getToken() async {
    return await _storage.read(key: _tokenKey);
  }

  Future<void> logout() async {
    await _storage.delete(key: _tokenKey);
  }

  Future<String?> login(String email, String motDePasse) async {
    final http.Response response;

    // Ce try/catch ne couvre QUE les erreurs réseau réelles (pas de
    // connexion, mauvaise IP...) — pas le cas d'un vrai 401 renvoyé par
    // le serveur, qui est traité séparément ci-dessous.
    try {
      response = await http.post(
        Uri.parse('${ApiConfig.baseUrl}/auth/login'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'email': email, 'motDePasse': motDePasse}),
      );
    } catch (e) {
      throw Exception(
        'Impossible de joindre le serveur. Vérifie la connexion et l\'URL backend.',
      );
    }

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      final token = data['token'];
      await _writeToken(token);
      return token;
    }

    String message = 'Email ou mot de passe incorrect';
    try {
      final error = jsonDecode(response.body);
      message = error['message'] ?? message;
    } catch (_) {
      // Corps de réponse non-JSON (ex: page d'erreur HTML) : on garde le
      // message par défaut plutôt que de planter.
    }
    throw Exception(message);
  }

  Future<Utilisateur?> getUtilisateurCourant() async {
    final token = await getToken();
    if (token == null) return null;

    final response = await http.get(
      Uri.parse('${ApiConfig.baseUrl}/auth/me'),
      headers: {'Authorization': 'Bearer $token'},
    );

    if (response.statusCode == 200) {
      return Utilisateur.fromJson(jsonDecode(response.body));
    }
    return null;
  }
}
