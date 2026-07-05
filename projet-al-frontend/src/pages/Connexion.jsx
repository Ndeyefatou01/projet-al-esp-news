import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function Connexion() {
  const [login, setLogin] = useState('');
  const [motDePasse, setMotDePasse] = useState('');
  const [erreur, setErreur] = useState('');
  const { login: seConnecter } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErreur('');
    try {
      await seConnecter(login, motDePasse);
      navigate('/');
    } catch (err) {
      setErreur('Login ou mot de passe incorrect');
    }
  };

  return (
    <div className="page-connexion">
      <h1>Connexion</h1>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Login</label>
          <input
            type="text"
            value={login}
            onChange={(e) => setLogin(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label>Mot de passe</label>
          <input
            type="password"
            value={motDePasse}
            onChange={(e) => setMotDePasse(e.target.value)}
            required
          />
        </div>
        {erreur && <p className="erreur">{erreur}</p>}
        <button type="submit">Se connecter</button>
      </form>
    </div>
  );
}

export default Connexion;