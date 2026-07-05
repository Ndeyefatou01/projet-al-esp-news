package esp.sn.projet_al_backend.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "categorie")
public class CategorieAvecArticlesDTO {
    private Long id;
    private String nom;
    private String description;

    @JacksonXmlElementWrapper(localName = "articles")
    @JacksonXmlProperty(localName = "article")
    private List<ArticleDTO> articles;
}