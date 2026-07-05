import { useState, useEffect } from 'react';
import api from '../services/api';

function AdminTokens() {
  const [tokens, setTokens] = useState([]);
  const [description, setDescription] = useState('');

  useEffect(() => {
    chargerTokens();
  }, []);

  const chargerTokens = async () => {
    const response = await api.get('/tokens');
    setTokens(response.data);
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    await api.post('/tokens', { description });
    setDescription('');
    chargerTokens();
  };

  const handleRevoke = async (id) => {
    await api.put(`/tokens/${id}/revoke`);
    chargerTokens();
  };

  const handleDelete = async (id) => {
    if (window.confirm('Supprimer définitivement ce jeton ?')) {
      await api.delete(`/tokens/${id}`);
      chargerTokens();
    }
  };

  return (
    <div className="page-admin-tokens">
      <h1>Gestion des jetons API</h1>
      <p className="tokens-info">
        Ces jetons permettent aux applications externes (ex: application client) d'accéder au service SOAP.
      </p>

      <form onSubmit={handleCreate} className="form-token">
        <input
          type="text"
          placeholder="Description du jeton"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          required
        />
        <button type="submit">Générer un nouveau jeton</button>
      </form>

      <div className="liste-tokens">
        {tokens.map((t) => (
          <div key={t.id} className={`token-item ${!t.actif ? 'token-inactif' : ''}`}>
            <div>
              <code className="token-valeur">{t.token}</code>
              <p className="token-description">{t.description}</p>
              <span className={`token-statut ${t.actif ? 'actif' : 'inactif'}`}>
                {t.actif ? 'Actif' : 'Révoqué'}
              </span>
            </div>
            <div className="admin-actions">
              {t.actif && (
                <button onClick={() => handleRevoke(t.id)}>Révoquer</button>
              )}
              <button onClick={() => handleDelete(t.id)}>Supprimer</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default AdminTokens;