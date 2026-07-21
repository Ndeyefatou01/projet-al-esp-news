"""
Application cliente ESP News — gestion des utilisateurs via le service web SOAP.

Correspond au 3e livrable du sujet : "Créer une application Java (ou Python)
permettant de gérer les utilisateurs. Quand l'application est lancée, elle
demande à l'utilisateur son login et son mot de passe et invoque ainsi le
service web d'authentification pour voir si l'utilisateur a les droits
d'administration [...]. Le cas échéant, l'application devra fournir un accès
complet aux fonctionnalités de gestion des utilisateurs [...] en utilisant
les services web adéquats."

Prérequis : le backend Spring Boot doit être démarré (cf README du dépôt).

Lancer avec :  python3 main.py
"""

import getpass
import sys

import soap_client


def demander_champ_obligatoire(libelle):
    while True:
        valeur = input(f"{libelle} : ").strip()
        if valeur:
            return valeur
        print("  -> ce champ est obligatoire.")


def demander_mot_de_passe_obligatoire(libelle="Mot de passe"):
    while True:
        valeur = getpass.getpass(f"{libelle} : ")
        if valeur:
            return valeur
        print("  -> ce champ est obligatoire.")


def demander_role():
    while True:
        role = input("Rôle (EDITEUR/ADMIN) : ").strip().upper()
        if role in ("EDITEUR", "ADMIN"):
            return role
        print("  -> rôle invalide, saisir EDITEUR ou ADMIN.")


def se_connecter():
    print("=== ESP News — Application cliente de gestion des utilisateurs ===")
    print(f"Service SOAP : {soap_client.SOAP_ENDPOINT}\n")

    login = input("Login : ").strip()
    mot_de_passe = getpass.getpass("Mot de passe : ")

    succes, est_admin, message = soap_client.authentifier(login, mot_de_passe)

    if not succes:
        print(f"\nAuthentification refusée : {message}")
        return None

    if not est_admin:
        print(f"\nAuthentification réussie ({message}), mais ce compte n'est pas administrateur.")
        print("Seuls les administrateurs peuvent gérer les utilisateurs depuis cette application.")
        return None

    print(f"\nAuthentification réussie : {message}")
    print(f"Bienvenue {login}, droits administrateur confirmés.\n")

    jeton_api = getpass.getpass(
        "Jeton API (généré au préalable par un administrateur depuis /admin/tokens sur le site) : "
    ).strip()
    while not jeton_api:
        print("  -> le jeton API est obligatoire pour accéder au service de gestion des utilisateurs.")
        jeton_api = getpass.getpass("Jeton API : ").strip()

    return jeton_api


def action_lister(jeton_api):
    succes, message, utilisateurs = soap_client.lister_utilisateurs(jeton_api)
    if not succes:
        print(f"\nÉchec : {message}")
        return

    if not utilisateurs:
        print("\nAucun utilisateur.")
        return

    print(f"\n{'ID':<5}{'Login':<16}{'Nom':<16}{'Prénom':<16}{'Email':<30}{'Rôle':<10}")
    print("-" * 93)
    for u in utilisateurs:
        print(f"{u['id']:<5}{u['login']:<16}{u['nom']:<16}{u['prenom']:<16}{u['email']:<30}{u['role']:<10}")


def action_ajouter(jeton_api):
    print("\n-- Nouvel utilisateur --")
    utilisateur = {
        "login": demander_champ_obligatoire("Login"),
        "motDePasse": demander_mot_de_passe_obligatoire(),
        "nom": demander_champ_obligatoire("Nom"),
        "prenom": demander_champ_obligatoire("Prénom"),
        "email": demander_champ_obligatoire("Email"),
        "role": demander_role(),
    }
    succes, message, cree = soap_client.ajouter_utilisateur(jeton_api, utilisateur)
    print(f"\n{'Succès' if succes else 'Échec'} : {message}")
    if succes and cree:
        print(f"  -> nouvel id : {cree['id']}")


def action_modifier(jeton_api):
    print("\n-- Modifier un utilisateur --")
    print("(Astuce : utilisez d'abord l'option 'Lister' pour retrouver l'id et le login actuels.)")
    id_utilisateur = demander_champ_obligatoire("Id de l'utilisateur à modifier")
    login_actuel = demander_champ_obligatoire("Login actuel (le login n'est pas modifiable via ce service)")

    print("Nouvelles valeurs :")
    utilisateur = {
        "login": login_actuel,
        "motDePasse": getpass.getpass("Nouveau mot de passe (laisser vide pour ne pas le changer) : "),
        "nom": demander_champ_obligatoire("Nom"),
        "prenom": demander_champ_obligatoire("Prénom"),
        "email": demander_champ_obligatoire("Email"),
        "role": demander_role(),
    }
    succes, message = soap_client.modifier_utilisateur(jeton_api, id_utilisateur, utilisateur)
    print(f"\n{'Succès' if succes else 'Échec'} : {message}")


def action_supprimer(jeton_api):
    print("\n-- Supprimer un utilisateur --")
    id_utilisateur = demander_champ_obligatoire("Id de l'utilisateur à supprimer")
    confirmation = input(f"Confirmer la suppression de l'utilisateur {id_utilisateur} ? (o/N) : ").strip().lower()
    if confirmation != "o":
        print("Annulé.")
        return

    succes, message = soap_client.supprimer_utilisateur(jeton_api, id_utilisateur)
    print(f"\n{'Succès' if succes else 'Échec'} : {message}")


def menu(jeton_api):
    actions = {
        "1": action_lister,
        "2": action_ajouter,
        "3": action_modifier,
        "4": action_supprimer,
    }

    while True:
        print("\n--- Gestion des utilisateurs ---")
        print("1. Lister les utilisateurs")
        print("2. Ajouter un utilisateur")
        print("3. Modifier un utilisateur")
        print("4. Supprimer un utilisateur")
        print("5. Quitter")
        choix = input("Choix : ").strip()

        if choix == "5":
            print("Au revoir.")
            return

        action = actions.get(choix)
        if action is None:
            print("Choix invalide.")
            continue

        try:
            action(jeton_api)
        except soap_client.ConnexionImpossible as erreur:
            print(f"\n[Erreur de connexion] {erreur}")
        except soap_client.ReponseInattendue as erreur:
            print(f"\n[Erreur] {erreur}")


def main():
    try:
        jeton_api = se_connecter()
    except soap_client.ConnexionImpossible as erreur:
        print(f"\n[Erreur de connexion] {erreur}")
        sys.exit(1)
    except soap_client.ReponseInattendue as erreur:
        print(f"\n[Erreur] {erreur}")
        sys.exit(1)
    except (KeyboardInterrupt, EOFError):
        print("\nInterrompu.")
        sys.exit(0)

    if jeton_api is None:
        sys.exit(0)

    try:
        menu(jeton_api)
    except (KeyboardInterrupt, EOFError):
        print("\nInterrompu.")


if __name__ == "__main__":
    main()
