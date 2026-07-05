package esp.sn.projet_al_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "articles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Article {

    public enum Statut {
        BROUILLON,
        PUBLIE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String resume;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String contenu;

    @Column(name = "date_publication")
    private LocalDateTime datePublication;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.BROUILLON;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    @ManyToOne
    @JoinColumn(name = "auteur_id", nullable = false)
    private Utilisateur auteur;

    @PrePersist
    protected void onCreate() {
        this.datePublication = LocalDateTime.now();
    }
}