"""
Tests unitaires de soap_client.py.

Aucun backend réel n'est nécessaire : urllib.request.urlopen est simulé
(unittest.mock) avec des réponses SOAP construites à la main, cohérentes
avec le XSD utilisateurs.xsd du backend.

Lancer avec :  python3 -m unittest test_soap_client.py -v
"""

import io
import unittest
import urllib.error
from unittest.mock import patch

import soap_client


def _reponse_xml(corps):
    xml = (
        '<?xml version="1.0" encoding="UTF-8"?>'
        '<S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/">'
        f"<S:Body>{corps}</S:Body>"
        "</S:Envelope>"
    )
    return xml.encode("utf-8")


class FakeHTTPResponse:
    """Simule l'objet retourné par urlopen (utilisable avec 'with ... as').”"""

    def __init__(self, contenu):
        self._contenu = contenu

    def read(self):
        return self._contenu

    def __enter__(self):
        return self

    def __exit__(self, *args):
        return False


class TestAuthentifier(unittest.TestCase):

    @patch("soap_client.urllib.request.urlopen")
    def test_authentification_reussie_admin(self, mock_urlopen):
        corps = (
            '<ns2:authentifierResponse xmlns:ns2="http://projet-al.esp.sn/soap/utilisateurs">'
            "<ns2:succes>true</ns2:succes>"
            "<ns2:estAdmin>true</ns2:estAdmin>"
            "<ns2:message>Authentification réussie</ns2:message>"
            "</ns2:authentifierResponse>"
        )
        mock_urlopen.return_value = FakeHTTPResponse(_reponse_xml(corps))

        succes, est_admin, message = soap_client.authentifier("admin", "admin123")

        self.assertTrue(succes)
        self.assertTrue(est_admin)
        self.assertEqual(message, "Authentification réussie")

    @patch("soap_client.urllib.request.urlopen")
    def test_authentification_refusee(self, mock_urlopen):
        corps = (
            '<ns2:authentifierResponse xmlns:ns2="http://projet-al.esp.sn/soap/utilisateurs">'
            "<ns2:succes>false</ns2:succes>"
            "<ns2:estAdmin>false</ns2:estAdmin>"
            "<ns2:message>Login ou mot de passe incorrect</ns2:message>"
            "</ns2:authentifierResponse>"
        )
        mock_urlopen.return_value = FakeHTTPResponse(_reponse_xml(corps))

        succes, est_admin, message = soap_client.authentifier("admin", "mauvais")

        self.assertFalse(succes)
        self.assertFalse(est_admin)

    @patch("soap_client.urllib.request.urlopen")
    def test_backend_injoignable(self, mock_urlopen):
        mock_urlopen.side_effect = urllib.error.URLError("Connection refused")

        with self.assertRaises(soap_client.ConnexionImpossible):
            soap_client.authentifier("admin", "admin123")


class TestListerUtilisateurs(unittest.TestCase):

    @patch("soap_client.urllib.request.urlopen")
    def test_liste_deux_utilisateurs(self, mock_urlopen):
        corps = (
            '<ns2:listerUtilisateursResponse xmlns:ns2="http://projet-al.esp.sn/soap/utilisateurs">'
            "<ns2:succes>true</ns2:succes>"
            "<ns2:message>2 utilisateur(s)</ns2:message>"
            "<ns2:utilisateur><ns2:id>1</ns2:id><ns2:login>admin</ns2:login>"
            "<ns2:nom>Mbow</ns2:nom><ns2:prenom>Fatima</ns2:prenom>"
            "<ns2:email>admin@projet-al.sn</ns2:email><ns2:role>ADMIN</ns2:role></ns2:utilisateur>"
            "<ns2:utilisateur><ns2:id>2</ns2:id><ns2:login>Awa</ns2:login>"
            "<ns2:nom>Ndiaye</ns2:nom><ns2:prenom>Awa</ns2:prenom>"
            "<ns2:email>awa.ndiaye@projet-al.sn</ns2:email><ns2:role>EDITEUR</ns2:role></ns2:utilisateur>"
            "</ns2:listerUtilisateursResponse>"
        )
        mock_urlopen.return_value = FakeHTTPResponse(_reponse_xml(corps))

        succes, message, utilisateurs = soap_client.lister_utilisateurs("un-jeton")

        self.assertTrue(succes)
        self.assertEqual(len(utilisateurs), 2)
        self.assertEqual(utilisateurs[0]["login"], "admin")
        self.assertEqual(utilisateurs[1]["role"], "EDITEUR")

    @patch("soap_client.urllib.request.urlopen")
    def test_jeton_invalide(self, mock_urlopen):
        corps = (
            '<ns2:listerUtilisateursResponse xmlns:ns2="http://projet-al.esp.sn/soap/utilisateurs">'
            "<ns2:succes>false</ns2:succes>"
            "<ns2:message>Jeton API invalide ou expiré</ns2:message>"
            "</ns2:listerUtilisateursResponse>"
        )
        mock_urlopen.return_value = FakeHTTPResponse(_reponse_xml(corps))

        succes, message, utilisateurs = soap_client.lister_utilisateurs("jeton-invalide")

        self.assertFalse(succes)
        self.assertEqual(utilisateurs, [])


class TestAjouterModifierSupprimer(unittest.TestCase):

    @patch("soap_client.urllib.request.urlopen")
    def test_ajouter_utilisateur(self, mock_urlopen):
        corps = (
            '<ns2:ajouterUtilisateurResponse xmlns:ns2="http://projet-al.esp.sn/soap/utilisateurs">'
            "<ns2:succes>true</ns2:succes>"
            "<ns2:message>Utilisateur créé</ns2:message>"
            "<ns2:utilisateur><ns2:id>5</ns2:id><ns2:login>nouveau</ns2:login>"
            "<ns2:nom>Test</ns2:nom><ns2:prenom>Nouveau</ns2:prenom>"
            "<ns2:email>nouveau@projet-al.sn</ns2:email><ns2:role>EDITEUR</ns2:role></ns2:utilisateur>"
            "</ns2:ajouterUtilisateurResponse>"
        )
        mock_urlopen.return_value = FakeHTTPResponse(_reponse_xml(corps))

        utilisateur = {
            "login": "nouveau", "motDePasse": "secret123", "nom": "Test",
            "prenom": "Nouveau", "email": "nouveau@projet-al.sn", "role": "EDITEUR",
        }
        succes, message, cree = soap_client.ajouter_utilisateur("un-jeton", utilisateur)

        self.assertTrue(succes)
        self.assertEqual(cree["id"], "5")

    @patch("soap_client.urllib.request.urlopen")
    def test_modifier_utilisateur(self, mock_urlopen):
        corps = (
            '<ns2:modifierUtilisateurResponse xmlns:ns2="http://projet-al.esp.sn/soap/utilisateurs">'
            "<ns2:succes>true</ns2:succes><ns2:message>Utilisateur modifié</ns2:message>"
            "</ns2:modifierUtilisateurResponse>"
        )
        mock_urlopen.return_value = FakeHTTPResponse(_reponse_xml(corps))

        utilisateur = {
            "login": "awa", "motDePasse": "", "nom": "Ndiaye",
            "prenom": "Awa", "email": "awa@projet-al.sn", "role": "EDITEUR",
        }
        succes, message = soap_client.modifier_utilisateur("un-jeton", "2", utilisateur)

        self.assertTrue(succes)

    @patch("soap_client.urllib.request.urlopen")
    def test_supprimer_utilisateur(self, mock_urlopen):
        corps = (
            '<ns2:supprimerUtilisateurResponse xmlns:ns2="http://projet-al.esp.sn/soap/utilisateurs">'
            "<ns2:succes>true</ns2:succes><ns2:message>Utilisateur supprimé</ns2:message>"
            "</ns2:supprimerUtilisateurResponse>"
        )
        mock_urlopen.return_value = FakeHTTPResponse(_reponse_xml(corps))

        succes, message = soap_client.supprimer_utilisateur("un-jeton", "2")

        self.assertTrue(succes)


class TestSoapFault(unittest.TestCase):

    @patch("soap_client.urllib.request.urlopen")
    def test_soap_fault_leve_reponse_inattendue(self, mock_urlopen):
        corps = (
            "<S:Fault>"
            "<faultcode>S:Server</faultcode>"
            "<faultstring>Erreur interne de traitement</faultstring>"
            "</S:Fault>"
        )
        mock_urlopen.return_value = FakeHTTPResponse(_reponse_xml(corps))

        with self.assertRaises(soap_client.ReponseInattendue):
            soap_client.authentifier("admin", "admin123")


# Construction des requêtes : vérifie que le XML envoyé est bien formé et complet,
# sans avoir besoin d'un serveur (on inspecte directement l'argument passé à urlopen).
class TestConstructionRequetes(unittest.TestCase):

    @patch("soap_client.urllib.request.urlopen")
    def test_requete_ajouter_contient_tous_les_champs(self, mock_urlopen):
        corps_reponse = (
            '<ns2:ajouterUtilisateurResponse xmlns:ns2="http://projet-al.esp.sn/soap/utilisateurs">'
            "<ns2:succes>true</ns2:succes><ns2:message>ok</ns2:message>"
            "</ns2:ajouterUtilisateurResponse>"
        )
        mock_urlopen.return_value = FakeHTTPResponse(_reponse_xml(corps_reponse))

        utilisateur = {
            "login": "test&<>", "motDePasse": "pwd", "nom": "N",
            "prenom": "P", "email": "e@e.sn", "role": "ADMIN",
        }
        soap_client.ajouter_utilisateur("jeton", utilisateur)

        requete_envoyee = mock_urlopen.call_args[0][0]
        corps_envoye = requete_envoyee.data.decode("utf-8")

        # Les caractères spéciaux doivent être échappés (XML valide)
        self.assertIn("test&amp;&lt;&gt;", corps_envoye)
        self.assertIn("<uti:jetonApi>jeton</uti:jetonApi>", corps_envoye)
        self.assertIn("<uti:role>ADMIN</uti:role>", corps_envoye)


if __name__ == "__main__":
    unittest.main()
