import { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';

function Accueil() {
  const [articles, setArticles] = useState([]);
  const [categories, setCategories] = useState([]);
  const [categorieSelectionnee, setCategorieSelectionnee] = useState('');
  const [recherche, setRecherche] = useState('');
  const [rechercheActive, setRechercheActive] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [suggestionsArticles, setSuggestionsArticles] = useState([]);
  const [suggestionsCategories, setSuggestionsCategories] = useState([]);
  const [suggestionsVisibles, setSuggestionsVisibles] = useState(false);
  const debounceRef = useRef(null);
  const suggestionBoxRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    chargerCategories();
  }, []);

  useEffect(() => {
    chargerArticles();
  }, [page, categorieSelectionnee, rechercheActive]);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (suggestionBoxRef.current && !suggestionBoxRef.current.contains(e.target)) {
        setSuggestionsVisibles(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const chargerCategories = async () => {
    try {
      const response = await api.get('/categories');
      setCategories(response.data);
    } catch (err) {
      console.error('Erreur lors du chargement des catégories', err);
    }
  };

  const chargerArticles = async () => {
    try {
      let url;
      if (rechercheActive) {
        url = `/articles/recherche?motCle=${encodeURIComponent(rechercheActive)}&page=${page}&size=6`;
      } else if (categorieSelectionnee) {
        url = `/articles/categorie/${categorieSelectionnee}?page=${page}&size=6`;
      } else {
        url = `/articles?page=${page}&size=6`;
      }
      const response = await api.get(url);
      setArticles(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (err) {
      console.error('Erreur lors du chargement des articles', err);
    }
  };

  const handleSaisie = (valeur) => {
    setRecherche(valeur);

    if (debounceRef.current) clearTimeout(debounceRef.current);

    const motCle = valeur.trim();
    if (motCle.length < 1) {
      setSuggestionsArticles([]);
      setSuggestionsCategories([]);
      setSuggestionsVisibles(false);
      return;
    }

    debounceRef.current = setTimeout(async () => {
      try {
        const [resArticles, resCategories] = await Promise.all([
          api.get(`/articles/recherche?motCle=${encodeURIComponent(motCle)}&page=0&size=5`),
          api.get('/categories'),
        ]);

        setSuggestionsArticles(resArticles.data.content);
        setSuggestionsCategories(
          resCategories.data.filter((c) =>
            c.nom.toLowerCase().includes(motCle.toLowerCase())
          )
        );
        setSuggestionsVisibles(true);
      } catch (err) {
        console.error('Erreur lors de la recherche de suggestions', err);
      }
    }, 250);
  };

  const handleRechercheSubmit = (e) => {
    e.preventDefault();
    setSuggestionsVisibles(false);
    setCategorieSelectionnee('');
    setRechercheActive(recherche.trim());
    setPage(0);
  };

  const handleSuggestionArticle = (article) => {
    setSuggestionsVisibles(false);
    setRecherche('');
    navigate(`/articles/${article.id}`);
  };

  const handleSuggestionCategorie = (cat) => {
    setSuggestionsVisibles(false);
    setRecherche('');
    setRechercheActive('');
    handleCategorieClick(cat.id);
  };

  const handleCategorieClick = (id) => {
    setCategorieSelectionnee(id);
    setRechercheActive('');
    setRecherche('');
    setPage(0);
  };

  const handleEffacerRecherche = () => {
    setRecherche('');
    setRechercheActive('');
    setPage(0);
  };

  const aDesSuggestions = suggestionsArticles.length > 0 || suggestionsCategories.length > 0;
  const afficherUne = page === 0 && !categorieSelectionnee && !rechercheActive && articles.length > 0;
  const articleUne = articles[0];

  return (
    <div className="page-accueil">
      <h1>Dernières actualités</h1>

      <div className="barre-recherche-wrapper" ref={suggestionBoxRef}>
        <form className="barre-recherche" onSubmit={handleRechercheSubmit} autoComplete="off">
          <input
            type="text"
            placeholder="Rechercher un article ou une catégorie..."
            value={recherche}
            onChange={(e) => handleSaisie(e.target.value)}
            onFocus={() => { if (aDesSuggestions) setSuggestionsVisibles(true); }}
          />
          <button type="submit">Rechercher</button>
          {rechercheActive && (
            <button type="button" className="btn-effacer" onClick={handleEffacerRecherche}>
              Effacer
            </button>
          )}
        </form>

        {suggestionsVisibles && aDesSuggestions && (
          <div className="suggestions-dropdown">
            {suggestionsCategories.length > 0 && (
              <div className="suggestions-groupe">
                <span className="suggestions-titre">Catégories</span>
                {suggestionsCategories.map((cat) => (
                  <button
                    key={`cat-${cat.id}`}
                    className="suggestion-item suggestion-categorie"
                    onClick={() => handleSuggestionCategorie(cat)}
                  >
                    <span className="suggestion-icone">▸</span> {cat.nom}
                  </button>
                ))}
              </div>
            )}
            {suggestionsArticles.length > 0 && (
              <div className="suggestions-groupe">
                <span className="suggestions-titre">Articles</span>
                {suggestionsArticles.map((article) => (
                  <button
                    key={`art-${article.id}`}
                    className="suggestion-item suggestion-article"
                    onClick={() => handleSuggestionArticle(article)}
                  >
                    <span className="suggestion-icone">✎</span>
                    <span>
                      {article.titre}
                      <span className="suggestion-categorie-tag">{article.categorie?.nom}</span>
                    </span>
                  </button>
                ))}
              </div>
            )}
          </div>
        )}
      </div>

      {!rechercheActive && (
        <div className="filtre-categories">
          <button
            className={categorieSelectionnee === '' ? 'filtre-actif' : ''}
            onClick={() => handleCategorieClick('')}
          >
            Toutes
          </button>
          {categories.map((cat) => (
            <button
              key={cat.id}
              className={categorieSelectionnee === cat.id ? 'filtre-actif' : ''}
              onClick={() => handleCategorieClick(cat.id)}
            >
              {cat.nom}
            </button>
          ))}
        </div>
      )}

      {rechercheActive && (
        <p className="resultats-recherche-info">
          Résultats pour « {rechercheActive} »
        </p>
      )}

      {afficherUne && (
        <Link to={`/articles/${articleUne.id}`} className="article-une">
          <span className="une-label">À la une</span>
          <span className="article-categorie">{articleUne.categorie?.nom}</span>
          <h2>{articleUne.titre}</h2>
          <p>{articleUne.resume}</p>
        </Link>
      )}

      <div className="articles-liste">
        {(afficherUne ? articles.slice(1) : articles).map((article) => (
          <Link to={`/articles/${article.id}`} key={article.id} className="article-carte">
            {article.imageUrl ? (
              <img
                src={`http://localhost:8080${article.imageUrl}`}
                alt={article.titre}
                className="article-carte-image"
              />
            ) : (
              <span className="article-carte-cercle" />
            )}
            <div className="article-carte-texte">
              <span className="article-categorie">{article.categorie?.nom}</span>
              <h2>{article.titre}</h2>
              <p>{article.resume}</p>
            </div>
          </Link>
        ))}
      </div>

      {articles.length === 0 && (
        <p className="aucun-article">Aucun article ne correspond à votre recherche.</p>
      )}

      <div className="pagination">
        <button disabled={page === 0} onClick={() => setPage(page - 1)}>
          Précédent
        </button>
        <span>Page {page + 1} sur {totalPages || 1}</span>
        <button disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)}>
          Suivant
        </button>
      </div>
    </div>
  );
}

export default Accueil;