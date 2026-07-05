package esp.sn.projet_al_backend.security;

import esp.sn.projet_al_backend.entity.Utilisateur;
import esp.sn.projet_al_backend.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + login));

        return new User(
                utilisateur.getLogin(),
                utilisateur.getMotDePasse(),
                List.of(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name()))
        );
    }
}