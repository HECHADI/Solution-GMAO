/// Configuration de connexion au backend Spring Boot.
///
/// ⚠️ IMPORTANT — l'URL à utiliser dépend de où tu testes l'app :
///
/// - Chrome / web (`flutter run -d chrome`) → 'http://localhost:8080'
///   ⚠️ nécessite que ton origine Chrome soit autorisée dans le CORS
///   backend (SecurityConfig.java) — vérifie que 'http://localhost:XXXXX'
///   (le port que Chrome affiche au lancement, souvent aléatoire pour
///   `flutter run -d chrome`) y figure, sinon ajoute-le.
///
/// - Émulateur Android      → 'http://10.0.2.2:8080'
///   (10.0.2.2 est un alias spécial de l'émulateur vers le localhost
///   de ta machine hôte — 'localhost' tout court ne fonctionnera PAS
///   depuis l'émulateur, il pointerait vers l'émulateur lui-même)
///
/// - Simulateur iOS         → 'http://localhost:8080' fonctionne directement
///
/// - Appareil physique      → l'adresse IP locale de ta machine sur le
///   réseau Wi-Fi (ex: 'http://192.168.1.42:8080'), trouvable via
///   `ipconfig` (Windows) ou `ifconfig`/`ip a` (Mac/Linux). Le téléphone
///   et l'ordinateur doivent être sur le même réseau Wi-Fi.
class ApiConfig {
  static const String baseUrl = 'http://localhost:8080/api';
}
