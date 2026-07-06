# ESP News

Plateforme d'actualités développée dans le cadre d'un projet académique à l'École Supérieure Polytechnique (ESP/UCAD, Dakar). L'application permet la publication et la gestion d'articles avec un système de rôles (Administrateur / Éditeur), un workflow éditorial (brouillon/publié), et une API SOAP sécurisée par jetons.

## Stack technique

### Frontend
- React (Vite)
- React Router
- Axios

### Backend
- Java 17 / Spring Boot
- Spring Security (JWT)
- Spring Data JPA (Hibernate)
- Spring Web Services (SOAP)
- MySQL

## Fonctionnalités principales

- Gestion des articles (création, modification, suppression, upload d'image)
- Statuts d'article : **Brouillon** / **Publié**
- Gestion des catégories
- Gestion des utilisateurs et des rôles (ADMIN / EDITEUR)
- Génération et révocation de jetons API pour l'accès au service SOAP
- Service web SOAP pour la gestion des utilisateurs (authentification, CRUD)
- Tableau de bord avec statistiques (articles par catégorie, par auteur)
- Recherche d'articles avec suggestions
- Filtrage par catégorie
- Pagination

## Prérequis

Avant de lancer le projet, assure-toi d'avoir installé :

| Outil | Version recommandée |
|---|---|
| Java (JDK) | 17 ou supérieur |
| Maven | fourni via le wrapper `mvnw` (aucune installation requise) |
| Node.js | 18 ou supérieur |
| npm | fourni avec Node.js |
| MySQL | 8.x (via WAMP, XAMPP, ou installation standalone) |

## Installation et lancement

### 1. Base de données

1. Démarre ton serveur MySQL (WAMP, XAMPP, ou service MySQL local).
2. Crée une base de données nommée `projet_al` :
   ```sql
   CREATE DATABASE projet_al;
   ```
3. Aucune autre action n'est requise : les tables sont créées automatiquement au démarrage du backend grâce à `spring.jpa.hibernate.ddl-auto=update`.

### 2. Configuration du backend

Dans `projet-al-backend/src/main/resources/application.properties`, vérifie/adapte les identifiants de connexion à ta base MySQL si besoin :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/projet_al?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=
```

Par défaut, la configuration suppose un utilisateur MySQL `root` sans mot de passe (config par défaut de WAMP/XAMPP). Adapte selon ton environnement.

### 3. Lancer le backend

Depuis le dossier `projet-al-backend` :

```bash
cd projet-al-backend
.\mvnw.cmd clean spring-boot:run
```

`clean` supprime les anciens fichiers compilés avant de relancer — utile si tu rencontres des erreurs de build ou après avoir modifié des dépendances.

Le backend démarre sur **http://localhost:8080**.

Attends de voir dans le terminal :
```
Started ProjetAlBackendApplication
```

### 4. Lancer le frontend

Depuis le dossier `projet-al-frontend`, dans un terminal séparé :

```bash
npm install
cd C:\Users\HP\Downloads\projet-al-backend\projet-al-frontend
npm run dev
```

Le frontend démarre sur **http://localhost:5173**.

### 5. Accéder à l'application

Ouvre ton navigateur sur : **http://localhost:5173**

## Comptes de test

| Rôle | Login | Mot de passe |
|---|---|---|
| Administrateur | admin | admin123 |
| Administrateur | Amadou01 | admin |
| Éditeur | Awa | awambow |
| Éditeur | AMY | admin |

## Structure du projet

```
projet-al-esp-news/
├── projet-al-backend/    # API REST + service SOAP (Spring Boot)
└── projet-al-frontend/   # Interface utilisateur (React)
```

## Service SOAP

Le service SOAP de gestion des utilisateurs est accessible à l'adresse :

**http://localhost:8080/ws/soap/utilisateurs.wsdl**

Les opérations disponibles (`authentifier`, `listerUtilisateurs`, `ajouterUtilisateur`, `modifierUtilisateur`, `supprimerUtilisateur`) nécessitent un **jeton API valide**, généré depuis l'interface d'administration (`/admin/tokens`), à l'exception de l'authentification.

## Auteurs

Projet réalisé par Ndeye Fatou Mbow et son équipe à savoir Amadou Aboubackrim Fall et Ndeye Yacine Diallo , dans le cadre du cursus à l'École Supérieure Polytechnique (ESP/UCAD).
