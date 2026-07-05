package esp.sn.projet_al_backend.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "article")
public class ArticleDTO {
    private Long id;
    private String titre;
    private String resume;
    private String contenu;
    private LocalDateTime datePublication;
    private String categorieNom;
    private Long categorieId;
}