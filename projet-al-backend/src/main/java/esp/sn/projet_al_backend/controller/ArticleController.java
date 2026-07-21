package esp.sn.projet_al_backend.controller;

import esp.sn.projet_al_backend.entity.Article;
import esp.sn.projet_al_backend.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    // ---------- PUBLIC (page Accueil) ----------

    @GetMapping
    public ResponseEntity<Page<Article>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(articleService.findAllPublies(page, size));
    }

    @GetMapping("/categorie/{categorieId}")
    public ResponseEntity<Page<Article>> getByCategorie(
            @PathVariable Long categorieId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(articleService.findByCategoriePublies(categorieId, page, size));
    }

    @GetMapping("/recherche")
    public ResponseEntity<Page<Article>> rechercher(
            @RequestParam String motCle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(articleService.rechercherPublies(motCle, page, size));
    }

    // ---------- ADMIN/EDITEUR : tous statuts ----------

    @GetMapping("/mes-articles")
    public ResponseEntity<Page<Article>> getMesArticles(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String login = authentication.getName();
        return ResponseEntity.ok(articleService.findByAuteur(login, page, size));
    }

    @GetMapping("/mes-articles/categorie/{categorieId}")
    public ResponseEntity<Page<Article>> getMesArticlesByCategorie(
            @PathVariable Long categorieId,
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String login = authentication.getName();
        return ResponseEntity.ok(articleService.findByAuteurAndCategorie(login, categorieId, page, size));
    }

    @GetMapping("/auteur/{login}")
    public ResponseEntity<Page<Article>> getByAuteur(
            @PathVariable String login,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(articleService.findByAuteur(login, page, size));
    }

    @GetMapping("/admin/tous")
    public ResponseEntity<Page<Article>> getAllAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(articleService.findAll(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Article> getById(@PathVariable Long id, Authentication authentication) {
        boolean estEditeurOuAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EDITEUR") || a.getAuthority().equals("ROLE_ADMIN"));

        Article article = estEditeurOuAdmin
                ? articleService.findById(id)
                : articleService.findByIdVisiblePourVisiteur(id);

        return ResponseEntity.ok(article);
    }

    @PostMapping
    public ResponseEntity<Article> create(
            @Valid @RequestBody Article article,
            @RequestParam Long categorieId,
            Authentication authentication
    ) {
        String loginAuteur = authentication.getName();
        return ResponseEntity.ok(articleService.create(article, categorieId, loginAuteur));
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<Article> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok(articleService.uploaderImage(id, file));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Article> update(
            @PathVariable Long id,
            @Valid @RequestBody Article article,
            @RequestParam(required = false) Long categorieId
    ) {
        return ResponseEntity.ok(articleService.update(id, article, categorieId));
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<Article> changerStatut(
            @PathVariable Long id,
            @RequestParam Article.Statut statut
    ) {
        return ResponseEntity.ok(articleService.changerStatut(id, statut));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}