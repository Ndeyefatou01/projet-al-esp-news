package esp.sn.projet_al_backend;

import esp.sn.projet_al_backend.entity.Utilisateur;
import esp.sn.projet_al_backend.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        System.out.println("###### DataInitializer EXECUTE ######");
        if (utilisateurRepository.findByLogin("admin").isEmpty()) {
            Utilisateur admin = new Utilisateur();
            admin.setLogin("admin");
            admin.setMotDePasse(passwordEncoder.encode("admin123"));
            admin.setNom("Mbow");
            admin.setPrenom("Fatima");
            admin.setEmail("admin@projet-al.sn");
            admin.setRole(Utilisateur.Role.ADMIN);
            utilisateurRepository.save(admin);
            System.out.println("###### Admin créé avec succès !");
        } else {
            System.out.println("###### Un admin existe déjà.");
        }
    }
}