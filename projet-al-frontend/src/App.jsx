import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import NavBar from './components/NavBar';
import Accueil from './pages/Accueil';
import Connexion from './pages/Connexion';
import AdminArticles from './pages/AdminArticles';
import AdminCategories from './pages/AdminCategories';
import AdminUtilisateurs from './pages/AdminUtilisateurs';
import AdminTokens from './pages/AdminTokens';
import AdminDashboard from './pages/AdminDashboard';
import EditeurDashboard from './pages/EditeurDashboard';
import DetailArticle from './pages/DetailArticle';
import './App.css';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <NavBar />
        <Routes>
          <Route path="/" element={<Accueil />} />
          <Route path="/connexion" element={<Connexion />} />
          <Route path="/articles/:id" element={<DetailArticle />} />
          
          <Route path="/editeur/dashboard" element={
            <RouteProtegee roleRequis="EDITEUR"><EditeurDashboard /></RouteProtegee>
          } />
          <Route path="/admin/articles" element={
            <RouteProtegee roleRequis="EDITEUR"><AdminArticles /></RouteProtegee>
          } />
          <Route path="/admin/categories" element={
            <RouteProtegee roleRequis="EDITEUR"><AdminCategories /></RouteProtegee>
          } />

          <Route path="/admin/dashboard" element={
            <RouteProtegee roleRequis="ADMIN"><AdminDashboard /></RouteProtegee>
          } />
          <Route path="/admin/utilisateurs" element={
            <RouteProtegee roleRequis="ADMIN"><AdminUtilisateurs /></RouteProtegee>
          } />
          <Route path="/admin/tokens" element={
            <RouteProtegee roleRequis="ADMIN"><AdminTokens /></RouteProtegee>
          } />

        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;