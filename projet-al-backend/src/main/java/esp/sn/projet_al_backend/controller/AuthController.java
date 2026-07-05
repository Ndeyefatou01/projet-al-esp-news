package esp.sn.projet_al_backend.controller;

import esp.sn.projet_al_backend.dto.LoginRequest;
import esp.sn.projet_al_backend.dto.LoginResponse;
import esp.sn.projet_al_backend.entity.Utilisateur;
import esp.sn.projet_al_backend.repository.UtilisateurRepository;
import esp.sn.projet_al_backend.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getLogin(), request.getMotDePasse())
        );

        Utilisateur utilisateur = utilisateurRepository.findByLogin(request.getLogin())
                .orElseThrow();

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(utilisateur.getLogin())
                .password(utilisateur.getMotDePasse())
                .roles(utilisateur.getRole().name())
                .build();

        String token = jwtService.generateToken(userDetails);

        LoginResponse response = new LoginResponse(
                token,
                utilisateur.getLogin(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getRole().name()
        );

        return ResponseEntity.ok(response);
    }
}