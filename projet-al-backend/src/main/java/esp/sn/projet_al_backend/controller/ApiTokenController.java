package esp.sn.projet_al_backend.controller;

import esp.sn.projet_al_backend.entity.ApiToken;
import esp.sn.projet_al_backend.service.ApiTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
public class ApiTokenController {

    private final ApiTokenService apiTokenService;

    @GetMapping
    public ResponseEntity<List<ApiToken>> getAll() {
        return ResponseEntity.ok(apiTokenService.findAll());
    }

    @PostMapping
    public ResponseEntity<ApiToken> create(@RequestBody Map<String, String> body, Authentication authentication) {
        String description = body.get("description");
        String loginAdmin = authentication.getName();
        return ResponseEntity.ok(apiTokenService.create(description, loginAdmin));
    }

    @PutMapping("/{id}/revoke")
    public ResponseEntity<Void> revoke(@PathVariable Long id) {
        apiTokenService.revoke(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        apiTokenService.delete(id);
        return ResponseEntity.noContent().build();
    }
}