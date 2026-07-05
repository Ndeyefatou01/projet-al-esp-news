package esp.sn.projet_al_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "api_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "description")
    private String description;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    private boolean actif = true;

    @ManyToOne
    @JoinColumn(name = "cree_par_id", nullable = false)
    private Utilisateur creePar;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
        if (this.token == null) {
            this.token = UUID.randomUUID().toString();
        }
    }
}