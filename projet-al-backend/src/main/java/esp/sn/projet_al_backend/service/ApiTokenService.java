package esp.sn.projet_al_backend.service;

import esp.sn.projet_al_backend.entity.ApiToken;
import esp.sn.projet_al_backend.entity.Utilisateur;
import esp.sn.projet_al_backend.repository.ApiTokenRepository;
import esp.sn.projet_al_backend.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiTokenService {

    private final ApiTokenRepository apiTokenRepository;
    private final UtilisateurRepository utilisateurRepository;

    public List<ApiToken> findAll() {
        return apiTokenRepository.findAll();
    }

    public ApiToken create(String description, String loginAdmin) {
        Utilisateur admin = utilisateurRepository.findByLogin(loginAdmin)
                .orElseThrow(() -> new RuntimeException("Administrateur non trouvé"));

        ApiToken apiToken = new ApiToken();
        apiToken.setDescription(description);
        apiToken.setCreePar(admin);
        apiToken.setActif(true);

        return apiTokenRepository.save(apiToken);
    }

    public void revoke(Long id) {
        ApiToken apiToken = apiTokenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jeton non trouvé avec l'id : " + id));
        apiToken.setActif(false);
        apiTokenRepository.save(apiToken);
    }

    public void delete(Long id) {
        ApiToken apiToken = apiTokenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jeton non trouvé avec l'id : " + id));
        apiTokenRepository.delete(apiToken);
    }

    public boolean isValid(String token) {
        return apiTokenRepository.findByTokenAndActifTrue(token).isPresent();
    }
}