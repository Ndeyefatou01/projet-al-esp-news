package esp.sn.projet_al_backend.service;

import esp.sn.projet_al_backend.entity.Utilisateur;
import esp.sn.projet_al_backend.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Utilisateur> findAll() {
        return utilisateurRepository.findAll();
    }

    public Utilisateur findById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + id));
    }

    public Utilisateur create(Utilisateur utilisateur) {
        if (utilisateurRepository.existsByLogin(utilisateur.getLogin())) {
            throw new RuntimeException("Ce login est déjà utilisé");
        }
        if (utilisateurRepository.existsByEmail(utilisateur.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        return utilisateurRepository.save(utilisateur);
    }

    public Utilisateur update(Long id, Utilisateur utilisateurDetails) {
        Utilisateur utilisateur = findById(id);
        utilisateur.setNom(utilisateurDetails.getNom());
        utilisateur.setPrenom(utilisateurDetails.getPrenom());
        utilisateur.setEmail(utilisateurDetails.getEmail());
        utilisateur.setRole(utilisateurDetails.getRole());

        // Ne change le mot de passe que s'il est fourni
        if (utilisateurDetails.getMotDePasse() != null && !utilisateurDetails.getMotDePasse().isBlank()) {
            utilisateur.setMotDePasse(passwordEncoder.encode(utilisateurDetails.getMotDePasse()));
        }

        return utilisateurRepository.save(utilisateur);
    }

    public void delete(Long id) {
        Utilisateur utilisateur = findById(id);
        utilisateurRepository.delete(utilisateur);
    }
}