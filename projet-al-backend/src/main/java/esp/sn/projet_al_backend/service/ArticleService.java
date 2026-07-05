package esp.sn.projet_al_backend.service;

import esp.sn.projet_al_backend.entity.Article;
import esp.sn.projet_al_backend.entity.Categorie;
import esp.sn.projet_al_backend.entity.Utilisateur;
import esp.sn.projet_al_backend.repository.ArticleRepository;
import esp.sn.projet_al_backend.repository.CategorieRepository;
import esp.sn.projet_al_backend.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CategorieRepository categorieRepository;
    private final UtilisateurRepository utilisateurRepository;

    // ---------- PUBLIC (page Accueil) : uniquement les articles PUBLIE ----------

    public Page<Article> findAllPublies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return articleRepository.findByStatutOrderByDatePublicationDesc(Article.Statut.PUBLIE, pageable);
    }

    public Page<Article> findByCategoriePublies(Long categorieId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return articleRepository.findByStatutAndCategorieIdOrderByDatePublicationDesc(Article.Statut.PUBLIE, categorieId, pageable);
    }

    public Page<Article> rechercherPublies(String motCle, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return articleRepository.findByStatutAndTitreContainingIgnoreCaseOrderByDatePublicationDesc(Article.Statut.PUBLIE, motCle, pageable);
    }

    // ---------- ADMIN/EDITEUR : tous les statuts ----------

    public Page<Article> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return articleRepository.findAllByOrderByDatePublicationDesc(pageable);
    }

    public Page<Article> findByCategorie(Long categorieId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return articleRepository.findByCategorieIdOrderByDatePublicationDesc(categorieId, pageable);
    }

    public Page<Article> rechercher(String motCle, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return articleRepository.findByTitreContainingIgnoreCaseOrderByDatePublicationDesc(motCle, pageable);
    }

    public Page<Article> findByAuteur(String login, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return articleRepository.findByAuteurLoginOrderByDatePublicationDesc(login, pageable);
    }

    public Page<Article> findByAuteurAndCategorie(String login, Long categorieId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return articleRepository.findByAuteurLoginAndCategorieIdOrderByDatePublicationDesc(login, categorieId, pageable);
    }

    public Article findById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé avec l'id : " + id));
    }

    public Article create(Article article, Long categorieId, String loginAuteur) {
        Categorie categorie = categorieRepository.findById(categorieId)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'id : " + categorieId));
        Utilisateur auteur = utilisateurRepository.findByLogin(loginAuteur)
                .orElseThrow(() -> new RuntimeException("Auteur non trouvé"));

        article.setCategorie(categorie);
        article.setAuteur(auteur);
        if (article.getStatut() == null) {
            article.setStatut(Article.Statut.BROUILLON);
        }
        return articleRepository.save(article);
    }

    public Article update(Long id, Article articleDetails, Long categorieId) {
        Article article = findById(id);
        article.setTitre(articleDetails.getTitre());
        article.setResume(articleDetails.getResume());
        article.setContenu(articleDetails.getContenu());

        if (categorieId != null) {
            Categorie categorie = categorieRepository.findById(categorieId)
                    .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'id : " + categorieId));
            article.setCategorie(categorie);
        }

        return articleRepository.save(article);
    }

    public Article changerStatut(Long id, Article.Statut nouveauStatut) {
        Article article = findById(id);
        article.setStatut(nouveauStatut);
        return articleRepository.save(article);
    }

    public Article uploaderImage(Long id, MultipartFile file) throws IOException {
        Article article = findById(id);

        String dossierUploads = "uploads/articles/";
        File dossier = new File(dossierUploads);
        if (!dossier.exists()) {
            dossier.mkdirs();
        }

        String nomFichier = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path chemin = Paths.get(dossierUploads + nomFichier);
        Files.write(chemin, file.getBytes());

        article.setImageUrl("/uploads/articles/" + nomFichier);
        return articleRepository.save(article);
    }

    public void delete(Long id) {
        Article article = findById(id);
        articleRepository.delete(article);
    }
}