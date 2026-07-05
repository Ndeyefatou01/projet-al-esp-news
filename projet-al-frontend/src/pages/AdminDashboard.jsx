import { useState, useEffect } from 'react';
import api from '../services/api';

function AdminDashboard() {
  const [stats, setStats] = useState({
    totalArticles: 0,
    totalCategories: 0,
    totalUtilisateurs: 0,
    totalTokensActifs: 0,
    totalTokensRevoques: 0,
    categoriesDetail: [],
    auteursDetail: [],
  });
  const [chargement, setChargement] = useState(true);

  useEffect(() => {
    chargerStatistiques();
  }, []);

  const chargerStatistiques = async () => {
    try {
      const [articlesRes, categoriesRes, utilisateursRes, tokensRes] = await Promise.all([
        api.get('/articles?page=0&size=1'),
        api.get('/categories'),
        api.get('/utilisateurs'),
        api.get('/tokens'),
      ]);

      const categories = categoriesRes.data;
      const utilisateurs = utilisateursRes.data;

      const categoriesDetail = await Promise.all(
        categories.map(async (cat) => {
          const res = await api.get(`/articles/categorie/${cat.id}?page=0&size=1`);
          return { nom: cat.nom, total: res.data.totalElements };
        })
      );

      const auteursDetail = await Promise.all(
        utilisateurs.map(async (u) => {
          const res = await api.get(`/articles/auteur/${u.login}?page=0&size=1`);
          return {
            nom: `${u.prenom} ${u.nom}`,
            role: u.role,
            total: res.data.totalElements,
          };
        })
      );

      const tokens = tokensRes.data;

      setStats({
        totalArticles: articlesRes.data.totalElements,
        totalCategories: categories.length,
        totalUtilisateurs: utilisateurs.length,
        totalTokensActifs: tokens.filter((t) => t.actif).length,
        totalTokensRevoques: tokens.filter((t) => !t.actif).length,
        categoriesDetail,
        auteursDetail: auteursDetail.filter((a) => a.total > 0),
      });
    } catch (err) {
      console.error('Erreur lors du chargement des statistiques', err);
    } finally {
      setChargement(false);
    }
  };

  if (chargement) {
    return <div className="page-admin-dashboard"><p>Chargement des statistiques...</p></div>;
  }

  const maxArticles = Math.max(1, ...stats.categoriesDetail.map((c) => c.total));
  const maxAuteurArticles = Math.max(1, ...stats.auteursDetail.map((a) => a.total));

  return (
    <div className="page-admin-dashboard">
      <h1>Tableau de bord</h1>

      <div className="stats-grille">
        <div className="stat-carte">
          <span className="stat-nombre">{stats.totalArticles}</span>
          <span className="stat-label">Articles publiés</span>
        </div>
        <div className="stat-carte">
          <span className="stat-nombre">{stats.totalCategories}</span>
          <span className="stat-label">Catégories</span>
        </div>
        <div className="stat-carte">
          <span className="stat-nombre">{stats.totalUtilisateurs}</span>
          <span className="stat-label">Utilisateurs</span>
        </div>
        <div className="stat-carte">
          <span className="stat-nombre">{stats.totalTokensActifs}</span>
          <span className="stat-label">Jetons actifs</span>
        </div>
      </div>

      <h2>Répartition des articles par catégorie</h2>
      <div className="repartition-categories">
        {stats.categoriesDetail.map((cat) => (
          <div key={cat.nom} className="repartition-ligne">
            <span className="repartition-nom">{cat.nom}</span>
            <div className="repartition-barre-fond">
              <div
                className="repartition-barre"
                style={{ width: `${(cat.total / maxArticles) * 100}%` }}
              />
            </div>
            <span className="repartition-valeur">{cat.total}</span>
          </div>
        ))}
        {stats.categoriesDetail.length === 0 && (
          <p className="aucun-article">Aucune catégorie pour le moment.</p>
        )}
      </div>

      <h2>Articles par rédacteur</h2>
      <div className="repartition-categories">
        {stats.auteursDetail.map((auteur) => (
          <div key={auteur.nom} className="repartition-ligne">
            <span className="repartition-nom">
              {auteur.nom} <span className="role-tag">{auteur.role}</span>
            </span>
            <div className="repartition-barre-fond">
              <div
                className="repartition-barre"
                style={{ width: `${(auteur.total / maxAuteurArticles) * 100}%` }}
              />
            </div>
            <span className="repartition-valeur">{auteur.total}</span>
          </div>
        ))}
        {stats.auteursDetail.length === 0 && (
          <p className="aucun-article">Aucun article rédigé pour le moment.</p>
        )}
      </div>

      <h2>Jetons API</h2>
      <div className="stats-grille stats-grille-secondaire">
        <div className="stat-carte stat-carte-mini">
          <span className="stat-nombre">{stats.totalTokensActifs}</span>
          <span className="stat-label">Actifs</span>
        </div>
        <div className="stat-carte stat-carte-mini">
          <span className="stat-nombre">{stats.totalTokensRevoques}</span>
          <span className="stat-label">Révoqués</span>
        </div>
      </div>
    </div>
  );
}

export default AdminDashboard;