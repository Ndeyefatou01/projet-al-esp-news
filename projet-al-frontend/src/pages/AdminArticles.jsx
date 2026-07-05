import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

function AdminArticles() {
  const { isAdmin } = useAuth();
  const [articles, setArticles] = useState([]);
  const [categories, setCategories] = useState([]);
  const [titre, setTitre] = useState('');
  const [resume, setResume] = useState('');
  const [contenu, setContenu] = useState('');
  const [categorieId, setCategorieId] = useState('');
  const [statut, setStatut] = useState('BROUILLON');
  const [imageFile, setImageFile] = useState(null);
  const [editingId, setEditingId] = useState(null);
  const [erreur, setErreur] = useState('');

  useEffect(() => {
    chargerArticles();
    chargerCategories();
  }, []);

  const chargerArticles = async () => {
    const response = await api.get('/articles/admin/tous?page=0&size=50');
    setArticles(response.data.content);
  };

  const chargerCategories = async () => {
    const response = await api.get('/categories');
    setCategories(response.data);
  };

  const resetForm = () => {
    setTitre('');
    setResume('');
    setContenu('');
    setCategorieId('');
    setStatut('BROUILLON');
    setImageFile(null);
    setEditingId(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErreur('');
    try {
      let articleId = editingId;

      if (editingId) {
        await api.put(`/articles/${editingId}?categorieId=${categorieId}`, {
          titre, resume, contenu
        });
        await api.patch(`/articles/${editingId}/statut?statut=${statut}`);
      } else {
        const response = await api.post(`/articles?categorieId=${categorieId}`, {
          titre, resume, contenu
        });
        articleId = response.data.id;
        if (statut === 'PUBLIE') {
          await api.patch(`/articles/${articleId}/statut?statut=PUBLIE`);
        }
      }

      if (imageFile) {
        const formData = new FormData();
        formData.append('file', imageFile);
        await api.post(`/articles/${articleId}/image`, formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
      }

      resetForm();
      chargerArticles();
    } catch (err) {
      setErreur("Erreur lors de l'enregistrement");
    }
  };

  const handleEdit = (article) => {
    setEditingId(article.id);
    setTitre(article.titre);
    setResume(article.resume);
    setContenu(article.contenu);
    setCategorieId(article.categorie.id);
    setStatut(article.statut || 'BROUILLON');
    setImageFile(null);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Supprimer cet article ?')) {
      await api.delete(`/articles/${id}`);
      chargerArticles();
    }
  };

  const handlePublier = async (article) => {
    await api.patch(`/articles/${article.id}/statut?statut=PUBLIE`);
    chargerArticles();
  };

  const handleRepasserBrouillon = async (article) => {
    await api.patch(`/articles/${article.id}/statut?statut=BROUILLON`);
    chargerArticles();
  };

  return (
    <div className="page-admin-articles">
      <h1>Gestion des articles</h1>

      <form onSubmit={handleSubmit} className="form-article">
        <h2>{editingId ? "Modifier l'article" : 'Nouvel article'}</h2>

        <input
          type="text"
          placeholder="Titre"
          value={titre}
          onChange={(e) => setTitre(e.target.value)}
          required
        />

        <select value={categorieId} onChange={(e) => setCategorieId(e.target.value)} required>
          <option value="">-- Choisir une catégorie --</option>
          {categories.map((cat) => (
            <option key={cat.id} value={cat.id}>{cat.nom}</option>
          ))}
        </select>

        <select value={statut} onChange={(e) => setStatut(e.target.value)}>
          <option value="BROUILLON">Brouillon</option>
          <option value="PUBLIE">Publié</option>
        </select>

        <textarea
          placeholder="Résumé"
          value={resume}
          onChange={(e) => setResume(e.target.value)}
          required
        />

        <textarea
          placeholder="Contenu complet"
          value={contenu}
          onChange={(e) => setContenu(e.target.value)}
          rows={8}
          required
        />

        <div className="form-group">
          <label>Image de l'article</label>
          <input
            type="file"
            accept="image/*"
            onChange={(e) => setImageFile(e.target.files[0])}
          />
        </div>

        {erreur && <p className="erreur">{erreur}</p>}

        <div className="form-actions">
          <button type="submit">{editingId ? 'Enregistrer' : 'Créer'}</button>
          {editingId && (
            <button type="button" onClick={resetForm}>Annuler</button>
          )}
        </div>
      </form>

      <h2>Articles existants</h2>
      <div className="liste-admin-articles">
        {articles.map((article) => (
          <div key={article.id} className="admin-article-item">
            <div>
              <strong>{article.titre}</strong>
              <span className="categorie-tag">{article.categorie?.nom}</span>
              <span className={`statut-tag statut-${article.statut?.toLowerCase()}`}>
                {article.statut === 'PUBLIE' ? 'Publié' : 'Brouillon'}
              </span>
              {isAdmin && article.auteur && (
                <span className="role-tag">{article.auteur.prenom} {article.auteur.nom}</span>
              )}
            </div>
            <div className="admin-actions">
              {article.statut !== 'PUBLIE' && (
                <button onClick={() => handlePublier(article)}>Publier</button>
              )}
              {article.statut === 'PUBLIE' && (
                <button onClick={() => handleRepasserBrouillon(article)}>Repasser en brouillon</button>
              )}
              <button onClick={() => handleEdit(article)}>Modifier</button>
              <button onClick={() => handleDelete(article.id)}>Supprimer</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default AdminArticles;