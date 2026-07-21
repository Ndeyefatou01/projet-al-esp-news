package esp.sn.projet_al_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 255, message = "Le titre ne doit pas dépasser 255 caractères")
    @Column(nullable = false)
    private String titre;

    @Size(max = 500, message = "Le résumé ne doit pas dépasser 500 caractères")
    @Column(columnDefinition = "TEXT")
    private String resume;

    @NotBlank(message = "Le contenu est obligatoire")
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
    
    // Un article publié est visible par n'importe quel visiteur
    // on n'expose donc que les infos de l'auteur nécessaires à l'affichage (nom/prénom/rôle),
    @ManyToOne
    @JoinColumn(name = "auteur_id", nullable = false)
    private Utilisateur auteur;
    @JsonIgnoreProperties({"email", "dateCreation"})

    @PrePersist
    protected void onCreate() {
        this.datePublication = LocalDateTime.now();
    }
}