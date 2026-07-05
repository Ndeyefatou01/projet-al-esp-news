package esp.sn.projet_al_backend.repository;

import esp.sn.projet_al_backend.entity.ApiToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiTokenRepository extends JpaRepository<ApiToken, Long> {

    Optional<ApiToken> findByTokenAndActifTrue(String token);
}