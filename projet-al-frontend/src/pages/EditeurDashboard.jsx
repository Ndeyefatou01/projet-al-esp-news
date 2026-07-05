import { useState, useEffect } from 'react';
import api from '../services/api';

function EditeurDashboard() {
  const [stats, setStats] = useState({
    totalArticles: 0,
    totalCategories: 0,
    categoriesDetail: [],
  });
  const [chargement, setChargement] = useState(true);

  useEffect(() => {
    chargerStatistiques();
  }, []);

  const chargerStatistiques = async () => {
    try {
      const [articlesRes, categoriesRes] = await Promise.all([
        api.get('/articles/mes-articles?page=0&size=1'),
        api.get('/categories'),
      ]);

      const categories = categoriesRes.data;

      const categoriesDetail = await Promise.all(
        categories.map(async (cat) => {
          const res = await api.get(`/articles/mes-articles/categorie/${cat.id}?page=0&size=1`);
          return { nom: cat.nom, total: res.data.totalElements };
        })
      );

      setStats({
        totalArticles: articlesRes.data.totalElements,
        totalCategories: categories.length,
        categoriesDetail,
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

  return (
    <div className="page-admin-dashboard">
      <h1>Mes statistiques</h1>
      <p className="dashboard-sous-titre">Vos articles et leur répartition</p>

      <div className="stats-grille">
        <div className="stat-carte">
          <span className="stat-nombre">{stats.totalArticles}</span>
          <span className="stat-label">Mes articles publiés</span>
        </div>
        <div className="stat-carte">
          <span className="stat-nombre">{stats.totalCategories}</span>
          <span className="stat-label">Catégories</span>
        </div>
      </div>

      <h2>Répartition de mes articles par catégorie</h2>
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
    </div>
  );
}

export default EditeurDashboard;