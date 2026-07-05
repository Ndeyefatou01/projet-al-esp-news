import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function NavBar() {
  const { user, logout, isAuthenticated, isEditeur, isAdmin } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const today = new Date().toLocaleDateString('fr-FR', {
    weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
  });

  return (
    <header className="site-header">
      <div className="topbar">
        <span className="topbar-date">{today}</span>
        <div className="topbar-account">
          {isAuthenticated ? (
            <>
              <span className="topbar-user">{user.prenom} {user.nom} · {user.role}</span>
              <button onClick={handleLogout} className="btn-logout">Déconnexion</button>
            </>
          ) : (
            <Link to="/connexion" className="btn-login">Connexion</Link>
          )}
        </div>
      </div>

      <div className="navbar-brand">
        <Link to="/">ESP News</Link>
      </div>

      <nav className="navbar">
        <div className="navbar-links">
          <Link to="/">Accueil</Link>

          {isEditeur && (
            <>
              <Link to="/editeur/dashboard">Mes statistiques</Link>
              <Link to="/admin/articles">Gérer les articles</Link>
              <Link to="/admin/categories">Gérer les catégories</Link>
            </>
          )}

          {isAdmin && (
            <>
              <Link to="/admin/dashboard">Tableau de bord</Link>
              <Link to="/admin/utilisateurs">Gérer les utilisateurs</Link>
              <Link to="/admin/tokens">Jetons API</Link>
            </>
          )}
        </div>
      </nav>
    </header>
  );
}

export default NavBar;