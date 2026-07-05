import { useState, useEffect } from 'react';
import api from '../services/api';

function AdminCategories() {
  const [categories, setCategories] = useState([]);
  const [nom, setNom] = useState('');
  const [description, setDescription] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [erreur, setErreur] = useState('');

  useEffect(() => {
    chargerCategories();
  }, []);

  const chargerCategories = async () => {
    const response = await api.get('/categories');
    setCategories(response.data);
  };

  const resetForm = () => {
    setNom('');
    setDescription('');
    setEditingId(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErreur('');
    try {
      if (editingId) {
        await api.put(`/categories/${editingId}`, { nom, description });
      } else {
        await api.post('/categories', { nom, description });
      }
      resetForm();
      chargerCategories();
    } catch (err) {
      setErreur("Erreur lors de l'enregistrement (nom peut-être déjà utilisé)");
    }
  };

  const handleEdit = (categorie) => {
    setEditingId(categorie.id);
    setNom(categorie.nom);
    setDescription(categorie.description || '');
  };

  const handleDelete = async (id) => {
    if (window.confirm('Supprimer cette catégorie ?')) {
      await api.delete(`/categories/${id}`);
      chargerCategories();
    }
  };

  return (
    <div className="page-admin-categories">
      <h1>Gestion des catégories</h1>

      <form onSubmit={handleSubmit} className="form-categorie">
        <h2>{editingId ? 'Modifier la catégorie' : 'Nouvelle catégorie'}</h2>

        <input
          type="text"
          placeholder="Nom"
          value={nom}
          onChange={(e) => setNom(e.target.value)}
          required
        />
        <textarea
          placeholder="Description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />

        {erreur && <p className="erreur">{erreur}</p>}

        <div className="form-actions">
          <button type="submit">{editingId ? 'Enregistrer' : 'Créer'}</button>
          {editingId && (
            <button type="button" onClick={resetForm}>Annuler</button>
          )}
        </div>
      </form>

      <h2>Catégories existantes</h2>
      <div className="liste-admin-categories">
        {categories.map((cat) => (
          <div key={cat.id} className="admin-categorie-item">
            <strong>{cat.nom}</strong>
            <div className="admin-actions">
              <button onClick={() => handleEdit(cat)}>Modifier</button>
              <button onClick={() => handleDelete(cat.id)}>Supprimer</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default AdminCategories;