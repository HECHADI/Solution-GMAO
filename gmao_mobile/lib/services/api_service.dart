import 'dart:convert';
import 'package:http/http.dart' as http;
import '../config/api_config.dart';
import '../models/intervention.dart';
import '../models/cloture_data.dart';
import 'auth_service.dart';

class ApiService {
  final _authService = AuthService();

  Future<Map<String, String>> _headers() async {
    final token = await _authService.getToken();
    return {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      if (token != null) 'Authorization': 'Bearer $token',
    };
  }

  Future<List<Intervention>> fetchInterventionsDuJour() async {
    final user = await _authService.getUtilisateurCourant();
    if (user == null) {
      throw Exception('Utilisateur non connecté');
    }

    final headers = await _headers();

    // 🟢 Tentative sur l'endpoint principal par ID
    var response = await http.get(
      Uri.parse('${ApiConfig.baseUrl}/interventions/technicien/${user.id}'),
      headers: headers,
    );

    // Fallback automatique si l'API utilise /mes-interventions
    if (response.statusCode == 404) {
      response = await http.get(
        Uri.parse('${ApiConfig.baseUrl}/interventions/mes-interventions'),
        headers: headers,
      );
    }

    if (response.statusCode != 200) {
      throw Exception('Erreur HTTP ${response.statusCode} : ${response.body}');
    }

    final List<dynamic> data = jsonDecode(utf8.decode(response.bodyBytes));

    // 🟢 On retourne TOUTES les interventions associées pour le test (pas de masquage strict par date)
    return data.map((json) => Intervention.fromJson(json)).toList();
  }

  Future<void> cloturerIntervention(
    int interventionId,
    ClotureData data,
  ) async {
    final body = {
      'compteRendu': data.compteRendu,
      if (data.signatureBytes != null)
        'signatureBase64': base64Encode(data.signatureBytes!),
    };

    final headers = await _headers();
    final response = await http.post(
      Uri.parse('${ApiConfig.baseUrl}/interventions/$interventionId/cloturer'),
      headers: headers,
      body: jsonEncode(body),
    );

    if (response.statusCode != 200) {
      throw Exception('Erreur ${response.statusCode} lors de la clôture');
    }
  }
}
