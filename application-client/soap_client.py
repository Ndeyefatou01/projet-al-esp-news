"""
Client SOAP pour le service web de gestion des utilisateurs (ESP News).

Ce module ne contient AUCUNE interaction utilisateur (pas de input()/print()) :
il expose uniquement des fonctions pures qui construisent les enveloppes SOAP,
appellent le service, et retournent des structures Python simples (dict/tuples).
Toute l'interface console vit dans main.py. Cette séparation permet de tester
soap_client.py sans avoir à simuler une saisie clavier.

N'utilise que la bibliothèque standard (urllib, xml, ...) : aucune dépendance
externe à installer.
"""

import urllib.request
import urllib.error
import xml.etree.ElementTree as ET
from xml.sax.saxutils import escape

# À adapter si le backend tourne sur une autre adresse/port.
BASE_URL = "http://localhost:8080"
SOAP_ENDPOINT = f"{BASE_URL}/ws/soap/utilisateurs"

NS_UTI = "http://projet-al.esp.sn/soap/utilisateurs"


class ConnexionImpossible(Exception):
    """Le service SOAP n'a pas pu être joint (backend arrêté, mauvaise URL, etc.)."""


class ReponseInattendue(Exception):
    """La réponse du service SOAP n'a pas la forme attendue (SOAP Fault, XML invalide...)."""


# ---------------------------------------------------------------------------
# Construction des enveloppes SOAP
# ---------------------------------------------------------------------------

def _e(valeur):
    """Échappe une valeur pour une insertion sûre dans du XML."""
    return escape("" if valeur is None else str(valeur))


def _enveloppe(corps_xml):
    return (
        '<?xml version="1.0" encoding="UTF-8"?>'
        '<soapenv:Envelope '
        'xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" '
        f'xmlns:uti="{NS_UTI}">'
        f"<soapenv:Body>{corps_xml}</soapenv:Body>"
        "</soapenv:Envelope>"
    )


def _corps_authentifier(login, mot_de_passe):
    return (
        "<uti:authentifierRequest>"
        f"<uti:login>{_e(login)}</uti:login>"
        f"<uti:motDePasse>{_e(mot_de_passe)}</uti:motDePasse>"
        "</uti:authentifierRequest>"
    )


def _corps_lister(jeton_api):
    return (
        "<uti:listerUtilisateursRequest>"
        f"<uti:jetonApi>{_e(jeton_api)}</uti:jetonApi>"
        "</uti:listerUtilisateursRequest>"
    )


def _corps_utilisateur(utilisateur, inclure_mot_de_passe_vide=False):
    """Construit le complexType <uti:utilisateur> commun à ajouter/modifier.

    L'ordre des balises doit respecter l'ordre déclaré dans le XSD
    (login, motDePasse, nom, prenom, email, role) : elementFormDefault="qualified"
    impose ce même ordre à la désérialisation JAXB côté serveur.
    """
    champs = f"<uti:login>{_e(utilisateur['login'])}</uti:login>"

    mot_de_passe = utilisateur.get("motDePasse") or ""
    if mot_de_passe or inclure_mot_de_passe_vide:
        champs += f"<uti:motDePasse>{_e(mot_de_passe)}</uti:motDePasse>"

    champs += (
        f"<uti:nom>{_e(utilisateur['nom'])}</uti:nom>"
        f"<uti:prenom>{_e(utilisateur['prenom'])}</uti:prenom>"
        f"<uti:email>{_e(utilisateur['email'])}</uti:email>"
        f"<uti:role>{_e(utilisateur['role'])}</uti:role>"
    )
    return f"<uti:utilisateur>{champs}</uti:utilisateur>"


def _corps_ajouter(jeton_api, utilisateur):
    return (
        "<uti:ajouterUtilisateurRequest>"
        f"<uti:jetonApi>{_e(jeton_api)}</uti:jetonApi>"
        f"{_corps_utilisateur(utilisateur)}"
        "</uti:ajouterUtilisateurRequest>"
    )


def _corps_modifier(jeton_api, id_utilisateur, utilisateur):
    return (
        "<uti:modifierUtilisateurRequest>"
        f"<uti:jetonApi>{_e(jeton_api)}</uti:jetonApi>"
        f"<uti:id>{_e(id_utilisateur)}</uti:id>"
        f"{_corps_utilisateur(utilisateur)}"
        "</uti:modifierUtilisateurRequest>"
    )


def _corps_supprimer(jeton_api, id_utilisateur):
    return (
        "<uti:supprimerUtilisateurRequest>"
        f"<uti:jetonApi>{_e(jeton_api)}</uti:jetonApi>"
        f"<uti:id>{_e(id_utilisateur)}</uti:id>"
        "</uti:supprimerUtilisateurRequest>"
    )


# ---------------------------------------------------------------------------
# Appel HTTP + parsing des réponses
# ---------------------------------------------------------------------------

def appeler_soap(corps_xml, endpoint=SOAP_ENDPOINT, timeout=10):
    """Envoie l'enveloppe SOAP au service et retourne le corps brut de la réponse (bytes).

    Spring-WS route les requêtes par namespace/nom de l'élément racine du payload,
    pas par l'URL : n'importe quelle URL sous /ws/soap/* fonctionne, mais on utilise
    ici celle publiée dans le WSDL (.../ws/soap/utilisateurs.wsdl, sans le .wsdl).
    """
    donnees = _enveloppe(corps_xml).encode("utf-8")
    requete = urllib.request.Request(
        endpoint,
        data=donnees,
        headers={
            "Content-Type": "text/xml; charset=utf-8",
            "SOAPAction": '""',
        },
        method="POST",
    )
    try:
        with urllib.request.urlopen(requete, timeout=timeout) as reponse:
            return reponse.read()
    except urllib.error.HTTPError as erreur:
        # Un SOAP Fault (erreur de traitement) arrive souvent avec un code HTTP 500,
        # mais le corps contient un XML exploitable qu'on veut quand même lire.
        return erreur.read()
    except urllib.error.URLError as erreur:
        raise ConnexionImpossible(
            f"Impossible de joindre le service SOAP à {endpoint} ({erreur.reason}). "
            "Vérifiez que le backend Spring Boot est démarré."
        ) from erreur


def _nom_local(tag):
    return tag.split("}")[-1] if "}" in tag else tag


def _premier(element, nom_local):
    """Cherche le premier descendant (à n'importe quelle profondeur) portant ce nom local."""
    for e in element.iter():
        if _nom_local(e.tag) == nom_local:
            return e
    return None


def _tous(element, nom_local):
    return [e for e in element.iter() if _nom_local(e.tag) == nom_local]


def _texte_enfant(element, nom_local, defaut=""):
    """Cherche un enfant DIRECT (pas récursif) portant ce nom local et retourne son texte."""
    if element is not None:
        for enfant in element:
            if _nom_local(enfant.tag) == nom_local:
                return enfant.text if enfant.text is not None else defaut
    return defaut


def _parser_reponse(xml_bytes):
    try:
        racine = ET.fromstring(xml_bytes)
    except ET.ParseError as erreur:
        raise ReponseInattendue(f"Réponse du serveur illisible : {erreur}") from erreur

    fault = _premier(racine, "Fault")
    if fault is not None:
        faultstring = _texte_enfant(fault, "faultstring", "Erreur SOAP non précisée")
        raise ReponseInattendue(f"Le serveur a renvoyé une erreur SOAP : {faultstring}")

    return racine


def _utilisateur_depuis_element(element):
    return {
        "id": _texte_enfant(element, "id"),
        "login": _texte_enfant(element, "login"),
        "nom": _texte_enfant(element, "nom"),
        "prenom": _texte_enfant(element, "prenom"),
        "email": _texte_enfant(element, "email"),
        "role": _texte_enfant(element, "role"),
    }


# ---------------------------------------------------------------------------
# Opérations exposées (une fonction par opération SOAP du XSD)
# ---------------------------------------------------------------------------

def authentifier(login, mot_de_passe):
    """Retourne (succes: bool, est_admin: bool, message: str)."""
    racine = _parser_reponse(appeler_soap(_corps_authentifier(login, mot_de_passe)))
    reponse = _premier(racine, "authentifierResponse")
    succes = _texte_enfant(reponse, "succes") == "true"
    est_admin = _texte_enfant(reponse, "estAdmin") == "true"
    message = _texte_enfant(reponse, "message")
    return succes, est_admin, message


def lister_utilisateurs(jeton_api):
    """Retourne (succes: bool, message: str, utilisateurs: list[dict])."""
    racine = _parser_reponse(appeler_soap(_corps_lister(jeton_api)))
    reponse = _premier(racine, "listerUtilisateursResponse")
    succes = _texte_enfant(reponse, "succes") == "true"
    message = _texte_enfant(reponse, "message")
    utilisateurs = [_utilisateur_depuis_element(u) for u in _tous(reponse, "utilisateur")] if reponse is not None else []
    return succes, message, utilisateurs


def ajouter_utilisateur(jeton_api, utilisateur):
    """utilisateur: dict avec login, motDePasse, nom, prenom, email, role.
    Retourne (succes: bool, message: str, utilisateur_cree: dict | None).
    """
    racine = _parser_reponse(appeler_soap(_corps_ajouter(jeton_api, utilisateur)))
    reponse = _premier(racine, "ajouterUtilisateurResponse")
    succes = _texte_enfant(reponse, "succes") == "true"
    message = _texte_enfant(reponse, "message")
    element_utilisateur = _premier(reponse, "utilisateur") if reponse is not None else None
    utilisateur_cree = _utilisateur_depuis_element(element_utilisateur) if element_utilisateur is not None else None
    return succes, message, utilisateur_cree


def modifier_utilisateur(jeton_api, id_utilisateur, utilisateur):
    """Retourne (succes: bool, message: str).

    Note : le service ne permet pas de changer le login (voir UtilisateurEndpoint côté
    serveur), la valeur envoyée ici est structurellement requise par le XSD mais sera
    ignorée par le serveur pour ce champ précis.
    """
    racine = _parser_reponse(appeler_soap(_corps_modifier(jeton_api, id_utilisateur, utilisateur)))
    reponse = _premier(racine, "modifierUtilisateurResponse")
    succes = _texte_enfant(reponse, "succes") == "true"
    message = _texte_enfant(reponse, "message")
    return succes, message


def supprimer_utilisateur(jeton_api, id_utilisateur):
    """Retourne (succes: bool, message: str)."""
    racine = _parser_reponse(appeler_soap(_corps_supprimer(jeton_api, id_utilisateur)))
    reponse = _premier(racine, "supprimerUtilisateurResponse")
    succes = _texte_enfant(reponse, "succes") == "true"
    message = _texte_enfant(reponse, "message")
    return succes, message
