package esp.sn.projet_al_backend.endpoint;

import esp.sn.projet_al_backend.entity.Utilisateur;
import esp.sn.projet_al_backend.repository.UtilisateurRepository;
import esp.sn.projet_al_backend.service.ApiTokenService;
import esp.sn.projet_al_backend.soap.generated.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.List;
import java.util.Optional;

@Endpoint
@RequiredArgsConstructor
public class UtilisateurEndpoint {

    private static final String NAMESPACE_URI = "http://projet-al.esp.sn/soap/utilisateurs";

    private final UtilisateurRepository utilisateurRepository;
    private final ApiTokenService apiTokenService;
    private final PasswordEncoder passwordEncoder;

    // 1. AUTHENTIFICATION
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "authentifierRequest")
    @ResponsePayload
    public AuthentifierResponse authentifier(@RequestPayload AuthentifierRequest request) {
        AuthentifierResponse response = new AuthentifierResponse();

        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByLogin(request.getLogin());

        if (utilisateurOpt.isEmpty() || !passwordEncoder.matches(request.getMotDePasse(), utilisateurOpt.get().getMotDePasse())) {
            response.setSucces(false);
            response.setEstAdmin(false);
            response.setMessage("Login ou mot de passe incorrect");
            return response;
        }

        Utilisateur utilisateur = utilisateurOpt.get();
        response.setSucces(true);
        response.setEstAdmin(utilisateur.getRole() == Utilisateur.Role.ADMIN);
        response.setMessage("Authentification réussie");
        return response;
    }

    // 2. LISTER LES UTILISATEURS
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "listerUtilisateursRequest")
    @ResponsePayload
    public ListerUtilisateursResponse lister(@RequestPayload ListerUtilisateursRequest request) {
        ListerUtilisateursResponse response = new ListerUtilisateursResponse();

        if (!apiTokenService.isValid(request.getJetonApi())) {
            response.setSucces(false);
            response.setMessage("Jeton API invalide");
            return response;
        }

        List<Utilisateur> utilisateurs = utilisateurRepository.findAll();
        for (Utilisateur u : utilisateurs) {
            response.getUtilisateur().add(toType(u));
        }

        response.setSucces(true);
        response.setMessage("Liste récupérée avec succès");
        return response;
    }

    // 3. AJOUTER UN UTILISATEUR
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "ajouterUtilisateurRequest")
    @ResponsePayload
    public AjouterUtilisateurResponse ajouter(@RequestPayload AjouterUtilisateurRequest request) {
        AjouterUtilisateurResponse response = new AjouterUtilisateurResponse();

        if (!apiTokenService.isValid(request.getJetonApi())) {
            response.setSucces(false);
            response.setMessage("Jeton API invalide");
            return response;
        }

        UtilisateurType ut = request.getUtilisateur();

        if (utilisateurRepository.existsByLogin(ut.getLogin())) {
            response.setSucces(false);
            response.setMessage("Ce login existe déjà");
            return response;
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setLogin(ut.getLogin());
        utilisateur.setMotDePasse(passwordEncoder.encode(ut.getMotDePasse()));
        utilisateur.setNom(ut.getNom());
        utilisateur.setPrenom(ut.getPrenom());
        utilisateur.setEmail(ut.getEmail());
        utilisateur.setRole(Utilisateur.Role.valueOf(ut.getRole()));

        Utilisateur saved = utilisateurRepository.save(utilisateur);

        response.setSucces(true);
        response.setMessage("Utilisateur créé avec succès");
        response.setUtilisateur(toType(saved));
        return response;
    }

    // 4. MODIFIER UN UTILISATEUR
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "modifierUtilisateurRequest")
    @ResponsePayload
    public ModifierUtilisateurResponse modifier(@RequestPayload ModifierUtilisateurRequest request) {
        ModifierUtilisateurResponse response = new ModifierUtilisateurResponse();

        if (!apiTokenService.isValid(request.getJetonApi())) {
            response.setSucces(false);
            response.setMessage("Jeton API invalide");
            return response;
        }

        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(request.getId());
        if (utilisateurOpt.isEmpty()) {
            response.setSucces(false);
            response.setMessage("Utilisateur non trouvé");
            return response;
        }

        Utilisateur utilisateur = utilisateurOpt.get();
        UtilisateurType ut = request.getUtilisateur();
        utilisateur.setNom(ut.getNom());
        utilisateur.setPrenom(ut.getPrenom());
        utilisateur.setEmail(ut.getEmail());
        utilisateur.setRole(Utilisateur.Role.valueOf(ut.getRole()));

        if (ut.getMotDePasse() != null && !ut.getMotDePasse().isBlank()) {
            utilisateur.setMotDePasse(passwordEncoder.encode(ut.getMotDePasse()));
        }

        utilisateurRepository.save(utilisateur);

        response.setSucces(true);
        response.setMessage("Utilisateur modifié avec succès");
        return response;
    }

    // 5. SUPPRIMER UN UTILISATEUR
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "supprimerUtilisateurRequest")
    @ResponsePayload
    public SupprimerUtilisateurResponse supprimer(@RequestPayload SupprimerUtilisateurRequest request) {
        SupprimerUtilisateurResponse response = new SupprimerUtilisateurResponse();

        if (!apiTokenService.isValid(request.getJetonApi())) {
            response.setSucces(false);
            response.setMessage("Jeton API invalide");
            return response;
        }

        if (!utilisateurRepository.existsById(request.getId())) {
            response.setSucces(false);
            response.setMessage("Utilisateur non trouvé");
            return response;
        }

        utilisateurRepository.deleteById(request.getId());
        response.setSucces(true);
        response.setMessage("Utilisateur supprimé avec succès");
        return response;
    }

    private UtilisateurType toType(Utilisateur u) {
        UtilisateurType ut = new UtilisateurType();
        ut.setId(u.getId());
        ut.setLogin(u.getLogin());      
        ut.setNom(u.getNom());
        ut.setPrenom(u.getPrenom());
        ut.setEmail(u.getEmail());
        ut.setRole(u.getRole().name());
        return ut;
    }
}