package esp.sn.projet_al_backend.controller;

import esp.sn.projet_al_backend.entity.Utilisateur;
import esp.sn.projet_al_backend.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @GetMapping
    public ResponseEntity<List<Utilisateur>> getAll() {
        return ResponseEntity.ok(utilisateurService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Utilisateur> getById(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Utilisateur> create(@Valid @RequestBody Utilisateur utilisateur) {
        return ResponseEntity.ok(utilisateurService.create(utilisateur));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Utilisateur> update(@PathVariable Long id, @Valid @RequestBody Utilisateur utilisateur) {
        return ResponseEntity.ok(utilisateurService.update(id, utilisateur));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        utilisateurService.delete(id);
        return ResponseEntity.noContent().build();
    }
}