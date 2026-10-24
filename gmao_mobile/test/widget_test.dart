import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:gmao_mobile/main.dart';

void main() {
  testWidgets('Test d\'affichage initial de GMAO App', (
    WidgetTester tester,
  ) async {
    // Charge notre application GmaoApp
    await tester.pumpWidget(const GmaoApp());

    // Vérifie qu'un composant de chargement (CircularProgressIndicator) s'affiche
    // pendant la vérification de la session AuthGate
    expect(find.byType(CircularProgressIndicator), findsOneWidget);
  });
}
