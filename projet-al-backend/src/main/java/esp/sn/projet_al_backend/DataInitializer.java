package esp.sn.projet_al_backend;

import esp.sn.projet_al_backend.entity.Article;
import esp.sn.projet_al_backend.entity.Categorie;
import esp.sn.projet_al_backend.entity.Utilisateur;
import esp.sn.projet_al_backend.repository.ArticleRepository;
import esp.sn.projet_al_backend.repository.CategorieRepository;
import esp.sn.projet_al_backend.entity.Utilisateur;
import esp.sn.projet_al_backend.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Peuple la base avec les comptes de démonstration annoncés dans le README ainsi que
 * quelques catégories/articles d'exemple, pour que le site ne soit pas vide juste après
 * un premier clonage + démarrage 
 */

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final CategorieRepository categorieRepository;
    private final ArticleRepository articleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
         Utilisateur admin = creerUtilisateurSiAbsent("admin", "admin123", "Mbow", "Fatima",
                "admin@projet-al.sn", Utilisateur.Role.ADMIN);
        creerUtilisateurSiAbsent("Amadou01", "admin", "Fall", "Amadou",
                "amadou.fall@projet-al.sn", Utilisateur.Role.ADMIN);
        Utilisateur awa = creerUtilisateurSiAbsent("Awa", "awambow", "Ndiaye", "Awa",
                "awa.ndiaye@projet-al.sn", Utilisateur.Role.EDITEUR);
        creerUtilisateurSiAbsent("AMY", "admin", "Diouf", "Amy",
                "amy.diouf@projet-al.sn", Utilisateur.Role.EDITEUR);

        Categorie actualitesEsp = creerCategorieSiAbsente("Actualités ESP",
                "La vie de l'École Supérieure Polytechnique : événements, annonces, vie associative.");
        Categorie technologie = creerCategorieSiAbsente("Technologie",
                "Actualités liées à l'informatique, aux sciences et à l'innovation.");

        creerArticleSiAbsent(
                "Bienvenue sur ESP News",
                "La nouvelle plateforme d'actualités de l'ESP est en ligne.",
                "ESP News centralise désormais les actualités de l'école : annonces, "
                        + "événements et vie associative, avec un espace dédié pour les "
                        + "éditeurs et les administrateurs.",
                actualitesEsp, admin, Article.Statut.PUBLIE);

        creerArticleSiAbsent(
                "Projet d'architecture logicielle : lancement de la promotion",
                "Coup d'envoi du projet de groupe en architecture logicielle.",
                "Les étudiants de DIC2/MASTER1/DIT2 démarrent leur projet de groupe : "
                        + "site d'actualité, services web SOAP/REST et application cliente.",
                technologie, awa, Article.Statut.PUBLIE);

        creerArticleSiAbsent(
                "Brouillon de démonstration",
                "Cet article n'est volontairement pas publié.",
                "Sert à vérifier que le workflow brouillon/publié fonctionne bien : "
                        + "un visiteur ne doit jamais voir cet article, seuls les "
                        + "éditeurs/admins le peuvent.",
                technologie, awa, Article.Statut.BROUILLON);
    }

    private Utilisateur creerUtilisateurSiAbsent(String login, String motDePasse, String nom, String prenom,
                                                  String email, Utilisateur.Role role) {
        return utilisateurRepository.findByLogin(login).orElseGet(() -> {
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setLogin(login);
            utilisateur.setMotDePasse(passwordEncoder.encode(motDePasse));
            utilisateur.setNom(nom);
            utilisateur.setPrenom(prenom);
            utilisateur.setEmail(email);
            utilisateur.setRole(role);
            return utilisateurRepository.save(utilisateur);
        });
    }

    private Categorie creerCategorieSiAbsente(String nom, String description) {
        return categorieRepository.findByNom(nom).orElseGet(() -> {
            Categorie categorie = new Categorie();
            categorie.setNom(nom);
            categorie.setDescription(description);
            return categorieRepository.save(categorie);
        });
    }

    private void creerArticleSiAbsent(String titre, String resume, String contenu,
                                       Categorie categorie, Utilisateur auteur, Article.Statut statut) {
        boolean existeDeja = articleRepository.findAllByOrderByDatePublicationDesc(
                        org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .anyMatch(a -> a.getTitre().equals(titre));
        if (existeDeja) {
            return;
        }

        Article article = new Article();
        article.setTitre(titre);
        article.setResume(resume);
        article.setContenu(contenu);
        article.setCategorie(categorie);
        article.setAuteur(auteur);
        article.setStatut(statut);
        articleRepository.save(article);
    }
}
