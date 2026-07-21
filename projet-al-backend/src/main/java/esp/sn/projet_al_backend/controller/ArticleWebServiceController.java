package esp.sn.projet_al_backend.controller;

import esp.sn.projet_al_backend.dto.ArticleDTO;
import esp.sn.projet_al_backend.dto.CategorieAvecArticlesDTO;
import esp.sn.projet_al_backend.entity.Article;
import esp.sn.projet_al_backend.repository.ArticleRepository;
import esp.sn.projet_al_backend.repository.CategorieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ws/rest/articles")
@RequiredArgsConstructor
public class ArticleWebServiceController {

    private final ArticleRepository articleRepository;
    private final CategorieRepository categorieRepository;

    // 1. Récupérer la liste de tous les articles (XML ou JSON)
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<ArticleDTO>> getAllArticles() {
        List<ArticleDTO> articles = articleRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(articles);
    }

    // 2. Récupérer les articles regroupés par catégorie (XML ou JSON)
    @GetMapping(value = "/par-categorie", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<CategorieAvecArticlesDTO>> getArticlesParCategorie() {
        List<CategorieAvecArticlesDTO> result = categorieRepository.findAll().stream()
                .map(cat -> new CategorieAvecArticlesDTO(
                        cat.getId(),
                        cat.getNom(),
                        cat.getDescription(),
                        cat.getArticles() == null ? List.of() : cat.getArticles().stream().map(this::toDTO).toList()
                ))
                .toList();
        return ResponseEntity.ok(result);
    }

    // 3. Récupérer les articles d'une catégorie donnée (XML ou JSON)
    @GetMapping(value = "/categorie/{categorieId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<ArticleDTO>> getArticlesByCategorie(@PathVariable Long categorieId) {
        List<ArticleDTO> articles = articleRepository.findAll().stream()
                .filter(a -> a.getCategorie().getId().equals(categorieId))
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(articles);
    }

    private ArticleDTO toDTO(Article article) {
        return new ArticleDTO(
                article.getId(),
                article.getTitre(),
                article.getResume(),
                article.getContenu(),
                article.getDatePublication(),
                article.getCategorie().getNom(),
                article.getCategorie().getId()
        );
    }
}