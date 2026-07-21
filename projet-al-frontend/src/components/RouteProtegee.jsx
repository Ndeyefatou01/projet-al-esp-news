import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * Empêche l'accès à une page si l'utilisateur n'est pas connecté, ou n'a pas le rôle
 * requis.
*/

function RouteProtegee({ children, roleRequis }) {
  const { isAuthenticated, isEditeur, isAdmin } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/connexion" replace />;
  }

  if (roleRequis === 'ADMIN' && !isAdmin) {
    return <Navigate to="/" replace />;
  }

  if (roleRequis === 'EDITEUR' && !isEditeur) {
    return <Navigate to="/" replace />;
  }

  return children;
}

export default RouteProtegee;
