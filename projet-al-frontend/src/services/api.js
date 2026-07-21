import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_URL,
});

// Ajoute automatiquement le token JWT à chaque requête si connecté
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const estAppelDeConnexion = error.config?.url?.includes('/auth/login');

    if (error.response?.status === 401 && !estAppelDeConnexion) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      if (window.location.pathname !== '/connexion') {
        window.location.href = '/connexion';
      }
    }

    return Promise.reject(error);
  }
);

export default api;