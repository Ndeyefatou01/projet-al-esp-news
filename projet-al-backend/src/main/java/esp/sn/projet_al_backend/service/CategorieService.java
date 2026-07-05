package esp.sn.projet_al_backend.service;

import esp.sn.projet_al_backend.entity.Categorie;
import esp.sn.projet_al_backend.repository.CategorieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategorieService {

    private final CategorieRepository categorieRepository;

    public List<Categorie> findAll() {
        return categorieRepository.findAll();
    }

    public Categorie findById(Long id) {
        return categorieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'id : " + id));
    }

    public Categorie create(Categorie categorie) {
        if (categorieRepository.existsByNom(categorie.getNom())) {
            throw new RuntimeException("Une catégorie avec ce nom existe déjà");
        }
        return categorieRepository.save(categorie);
    }

    public Categorie update(Long id, Categorie categorieDetails) {
        Categorie categorie = findById(id);
        categorie.setNom(categorieDetails.getNom());
        categorie.setDescription(categorieDetails.getDescription());
        return categorieRepository.save(categorie);
    }

    public void delete(Long id) {
        Categorie categorie = findById(id);
        categorieRepository.delete(categorie);
    }
}