import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../services/api';

function DetailArticle() {
  const { id } = useParams();
  const [article, setArticle] = useState(null);
  const [erreur, setErreur] = useState('');

  useEffect(() => {
    chargerArticle();
  }, [id]);

  const chargerArticle = async () => {
    try {
      const response = await api.get(`/articles/${id}`);
      setArticle(response.data);
    } catch (err) {
      setErreur("Cet article n'existe pas ou plus.");
    }
  };

  if (erreur) {
    return (
      <div className="page-detail-article">
        <p className="erreur">{erreur}</p>
        <Link to="/">Retour à l'accueil</Link>
      </div>
    );
  }

  if (!article) {
    return <div className="page-detail-article">Chargement...</div>;
  }

  const date = new Date(article.datePublication).toLocaleDateString('fr-FR', {
    year: 'numeric', month: 'long', day: 'numeric'
  });

  return (
    <div className="page-detail-article">
      <Link to="/" className="lien-retour">← Retour à l'accueil</Link>
      <span className="article-categorie">{article.categorie?.nom}</span>
      <h1>{article.titre}</h1>
      <p className="article-meta">
        Publié le {date} {article.auteur && `par ${article.auteur.prenom} ${article.auteur.nom}`}
      </p>
      {article.imageUrl && (
        <img
          src={`http://localhost:8080${article.imageUrl}`}
          alt={article.titre}
          className="article-detail-image"
        />
      )}
      <p className="article-resume">{article.resume}</p>
      <div className="article-contenu">{article.contenu}</div>
    </div>
  );
}

export default DetailArticle;