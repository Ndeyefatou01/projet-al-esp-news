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
          <Route path="/admin/dashboard" element={<AdminDashboard />} />
          <Route path="/editeur/dashboard" element={<EditeurDashboard />} />
          <Route path="/admin/articles" element={<AdminArticles />} />
          <Route path="/admin/categories" element={<AdminCategories />} />
          <Route path="/admin/utilisateurs" element={<AdminUtilisateurs />} />
          <Route path="/admin/tokens" element={<AdminTokens />} />
          <Route path="/articles/:id" element={<DetailArticle />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;