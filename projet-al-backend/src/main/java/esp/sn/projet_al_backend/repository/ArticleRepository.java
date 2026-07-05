package esp.sn.projet_al_backend.repository;

import esp.sn.projet_al_backend.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    Page<Article> findAllByOrderByDatePublicationDesc(Pageable pageable);

    Page<Article> findByCategorieIdOrderByDatePublicationDesc(Long categorieId, Pageable pageable);

    Page<Article> findByTitreContainingIgnoreCaseOrderByDatePublicationDesc(String motCle, Pageable pageable);

    Page<Article> findByAuteurLoginOrderByDatePublicationDesc(String login, Pageable pageable);

    Page<Article> findByAuteurLoginAndCategorieIdOrderByDatePublicationDesc(String login, Long categorieId, Pageable pageable);

    Page<Article> findByStatutOrderByDatePublicationDesc(Article.Statut statut, Pageable pageable);

    Page<Article> findByStatutAndCategorieIdOrderByDatePublicationDesc(Article.Statut statut, Long categorieId, Pageable pageable);

    Page<Article> findByStatutAndTitreContainingIgnoreCaseOrderByDatePublicationDesc(Article.Statut statut, String motCle, Pageable pageable);
}