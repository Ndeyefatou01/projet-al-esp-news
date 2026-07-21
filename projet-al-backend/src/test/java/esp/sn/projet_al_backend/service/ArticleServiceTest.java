package esp.sn.projet_al_backend.service;

import esp.sn.projet_al_backend.entity.Article;
import esp.sn.projet_al_backend.exception.ResourceNotFoundException;
import esp.sn.projet_al_backend.repository.ArticleRepository;
import esp.sn.projet_al_backend.repository.CategorieRepository;
import esp.sn.projet_al_backend.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Vérifie le correctif de sécurité sur la visibilité des articles : un article en
 * BROUILLON ne doit jamais être retourné à un appelant "visiteur" (findByIdVisiblePourVisiteur),
 * même s'il connaît son id, alors qu'un éditeur/admin (findById) doit pouvoir y accéder.
 */
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private CategorieRepository categorieRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;

    private ArticleService articleService;

    @BeforeEach
    void setUp() {
        articleService = new ArticleService(articleRepository, categorieRepository, utilisateurRepository);
    }

    @Test
    void findByIdVisiblePourVisiteur_refuseUnBrouillon() {
        Article brouillon = new Article();
        brouillon.setId(1L);
        brouillon.setStatut(Article.Statut.BROUILLON);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(brouillon));

        assertThatThrownBy(() -> articleService.findByIdVisiblePourVisiteur(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByIdVisiblePourVisiteur_autoriseUnArticlePublie() {
        Article publie = new Article();
        publie.setId(2L);
        publie.setStatut(Article.Statut.PUBLIE);
        when(articleRepository.findById(2L)).thenReturn(Optional.of(publie));

        Article resultat = articleService.findByIdVisiblePourVisiteur(2L);

        assertThat(resultat.getStatut()).isEqualTo(Article.Statut.PUBLIE);
    }

    @Test
    void findById_autoriseUnBrouillon_carReserveAuxEditeursEtAdmins() {
        Article brouillon = new Article();
        brouillon.setId(3L);
        brouillon.setStatut(Article.Statut.BROUILLON);
        when(articleRepository.findById(3L)).thenReturn(Optional.of(brouillon));

        Article resultat = articleService.findById(3L);

        assertThat(resultat.getStatut()).isEqualTo(Article.Statut.BROUILLON);
    }
}
