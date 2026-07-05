import { useState, useEffect } from 'react';
import api from '../services/api';

function AdminUtilisateurs() {
  const [utilisateurs, setUtilisateurs] = useState([]);
  const [login, setLogin] = useState('');
  const [motDePasse, setMotDePasse] = useState('');
  const [nom, setNom] = useState('');
  const [prenom, setPrenom] = useState('');
  const [email, setEmail] = useState('');
  const [role, setRole] = useState('EDITEUR');
  const [editingId, setEditingId] = useState(null);
  const [erreur, setErreur] = useState('');

  useEffect(() => {
    chargerUtilisateurs();
  }, []);

  const chargerUtilisateurs = async () => {
    const response = await api.get('/utilisateurs');
    setUtilisateurs(response.data);
  };

  const resetForm = () => {
    setLogin('');
    setMotDePasse('');
    setNom('');
    setPrenom('');
    setEmail('');
    setRole('EDITEUR');
    setEditingId(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErreur('');
    try {
      const payload = { login, nom, prenom, email, role };
      if (motDePasse) payload.motDePasse = motDePasse;

      if (editingId) {
        await api.put(`/utilisateurs/${editingId}`, payload);
      } else {
        payload.motDePasse = motDePasse;
        await api.post('/utilisateurs', payload);
      }
      resetForm();
      chargerUtilisateurs();
    } catch (err) {
      setErreur("Erreur lors de l'enregistrement (login/email peut-être déjà utilisé)");
    }
  };

  const handleEdit = (u) => {
    setEditingId(u.id);
    setLogin(u.login);
    setMotDePasse('');
    setNom(u.nom);
    setPrenom(u.prenom);
    setEmail(u.email);
    setRole(u.role);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Supprimer cet utilisateur ?')) {
      await api.delete(`/utilisateurs/${id}`);
      chargerUtilisateurs();
    }
  };

  return (
    <div className="page-admin-utilisateurs">
      <h1>Gestion des utilisateurs</h1>

      <form onSubmit={handleSubmit} className="form-utilisateur">
        <h2>{editingId ? "Modifier l'utilisateur" : 'Nouvel utilisateur'}</h2>

        <input type="text" placeholder="Login" value={login} onChange={(e) => setLogin(e.target.value)} required />
        <input
          type="password"
          placeholder={editingId ? 'Nouveau mot de passe (laisser vide pour ne pas changer)' : 'Mot de passe'}
          value={motDePasse}
          onChange={(e) => setMotDePasse(e.target.value)}
          required={!editingId}
        />
        <input type="text" placeholder="Nom" value={nom} onChange={(e) => setNom(e.target.value)} required />
        <input type="text" placeholder="Prénom" value={prenom} onChange={(e) => setPrenom(e.target.value)} required />
        <input type="email" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} required />

        <select value={role} onChange={(e) => setRole(e.target.value)}>
          <option value="EDITEUR">Éditeur</option>
          <option value="ADMIN">Admin</option>
        </select>

        {erreur && <p className="erreur">{erreur}</p>}

        <div className="form-actions">
          <button type="submit">{editingId ? 'Enregistrer' : 'Créer'}</button>
          {editingId && <button type="button" onClick={resetForm}>Annuler</button>}
        </div>
      </form>

      <h2>Utilisateurs existants</h2>
      <div className="liste-admin-utilisateurs">
        {utilisateurs.map((u) => (
          <div key={u.id} className="admin-utilisateur-item">
            <div>
              <strong>{u.prenom} {u.nom}</strong> ({u.login}) — <span className="role-tag">{u.role}</span>
            </div>
            <div className="admin-actions">
              <button onClick={() => handleEdit(u)}>Modifier</button>
              <button onClick={() => handleDelete(u.id)}>Supprimer</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default AdminUtilisateurs;