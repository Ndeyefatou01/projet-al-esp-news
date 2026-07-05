package esp.sn.projet_al_backend.repository;

import esp.sn.projet_al_backend.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByLogin(String login);

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);
}