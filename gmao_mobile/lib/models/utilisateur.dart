enum RoleUtilisateur { manager, technicien }

class Utilisateur {
  final int id;
  final String nom;
  final String email;
  final RoleUtilisateur role;

  Utilisateur({
    required this.id,
    required this.nom,
    required this.email,
    required this.role,
  });

  factory Utilisateur.fromJson(Map<String, dynamic> json) {
    return Utilisateur(
      id: json['id'],
      nom: json['nom'],
      email: json['email'],
      role: json['role'] == 'MANAGER' ? RoleUtilisateur.manager : RoleUtilisateur.technicien,
    );
  }
}
