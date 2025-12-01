# 🏗️ Architecture et Relations entre les Fichiers - Pawfect Match

## 📋 Table des Matières
1. [Vue d'Ensemble de l'Architecture](#vue-densemble)
2. [Structure des Fichiers](#structure-des-fichiers)
3. [Flux de Navigation](#flux-de-navigation)
4. [Flux de Données](#flux-de-données)
5. [Relations entre Composants](#relations-entre-composants)
6. [Technologies Utilisées](#technologies-utilisées)

---

## 🎯 Vue d'Ensemble de l'Architecture {#vue-densemble}

L'application **Pawfect Match** suit une architecture **MVC (Model-View-Controller)** simplifiée :

- **Model** : Classes `Pet.java` et `Owner.java` (données)
- **View** : Fichiers XML de layout dans `res/layout/`
- **Controller** : Les Activities (MainActivity, PetListActivity, etc.)

---

## 📁 Structure des Fichiers {#structure-des-fichiers}

### 1. **AndroidManifest.xml** - Le Cœur de Configuration
**Rôle** : Déclare tous les composants de l'application

```xml
- Déclare les permissions (INTERNET, READ_MEDIA_IMAGES)
- Enregistre toutes les Activities
- Définit l'Activity principale (LAUNCHER)
- Configure les relations parent-enfant entre Activities
```

**Relations** :
- Référence toutes les Activities
- Utilise `@string/app_name` pour le nom de l'app
- Utilise `@mipmap/ic_launcher` pour l'icône

---

### 2. **Modèles de Données (Package `model/`)**

#### **Pet.java**
```java
- Représente un animal de compagnie
- Implémente Serializable (pour passer entre Activities)
- Contient : id, name, description, type, age, race, owner, photoUrls
- Constructeur vide requis par Firestore
```

**Relations** :
- Utilisé par : `PetListActivity`, `AddPetActivity`, `PetDetailActivity`, `PetAdapter`
- Stocké dans Firestore collection "pets"
- Contient une référence à `Owner`

#### **Owner.java**
```java
- Représente le propriétaire d'un animal
- Implémente Serializable
- Contient : name, email, phone
```

**Relations** :
- Inclus dans `Pet` (composition)
- Utilisé pour afficher les coordonnées du propriétaire

---

### 3. **Activities (Écrans de l'Application)**

#### **MainActivity.java** - Point d'Entrée
**Rôle** : Écran principal après authentification

**Fonctionnalités** :
- Vérifie l'authentification Firebase
- Affiche les boutons "Add Pet" et "View Pets"
- Gère la recherche
- Menu de déconnexion

**Relations** :
- Utilise `R.layout.activity_main` (interface)
- Utilise `R.menu.main_menu` (menu toolbar)
- Navigue vers : `SignInActivity`, `AddPetActivity`, `PetListActivity`
- Utilise `FirebaseAuth` pour l'authentification

**Flux** :
```
Démarrage App → MainActivity
  ↓ (si non connecté)
SignInActivity
  ↓ (après connexion)
MainActivity
```

---

#### **SignInActivity.java** - Connexion
**Rôle** : Authentification utilisateur

**Relations** :
- Utilise `R.layout.activity_sign_in`
- Utilise `FirebaseAuth` pour signIn
- Navigue vers `SignUpActivity` (création compte)
- Navigue vers `MainActivity` (après connexion)

---

#### **SignUpActivity.java** - Inscription
**Rôle** : Création de compte

**Relations** :
- Utilise `R.layout.activity_sign_up`
- Utilise `FirebaseAuth` pour createUser
- Navigue vers `MainActivity` après inscription

---

#### **PetListActivity.java** - Liste des Animaux
**Rôle** : Affiche tous les animaux avec filtres et recherche

**Fonctionnalités** :
- Charge les pets depuis Firestore
- Filtre par type (All, Dog, Cat)
- Recherche par nom/description/race
- Gère les clics (voir détails, éditer, supprimer)

**Relations** :
- Utilise `R.layout.activity_pet_list`
- Utilise `PetAdapter` pour afficher la liste
- Utilise `FirebaseFirestore` pour charger les données
- Navigue vers `PetDetailActivity` (clic sur pet)
- Navigue vers `AddPetActivity` (mode édition)

**Flux de Données** :
```
FirebaseFirestore.collection("pets")
  ↓ (get())
List<Pet>
  ↓ (filtrer/rechercher)
filteredPets
  ↓ (adapter)
RecyclerView (affichage)
```

---

#### **PetDetailActivity.java** - Détails d'un Animal
**Rôle** : Affiche les détails complets d'un animal

**Fonctionnalités** :
- Affiche l'image, les infos du pet, et les coordonnées du propriétaire
- Email et téléphone cliquables (ouvre app email/dialer)

**Relations** :
- Utilise `R.layout.activity_pet_detail`
- Reçoit `Pet` via Intent (Serializable)
- Utilise `Glide` pour charger l'image

**Flux** :
```
PetListActivity (clic sur pet)
  ↓ (Intent avec Pet)
PetDetailActivity
```

---

#### **AddPetActivity.java** - Ajouter/Modifier un Animal
**Rôle** : Formulaire pour créer ou modifier un pet

**Fonctionnalités** :
- Mode création : nouveau pet
- Mode édition : modifie un pet existant
- Upload d'image via ImageKit
- Validation des champs
- Sauvegarde dans Firestore

**Relations** :
- Utilise `R.layout.activity_add_pet`
- Utilise `ImageKitHelper` pour upload images
- Utilise `FirebaseFirestore` pour sauvegarder
- Utilise `Glide` pour afficher l'image

**Flux de Sauvegarde** :
```
Formulaire rempli
  ↓
ImageKitHelper.uploadImage() (si image)
  ↓
URL de l'image
  ↓
Créer objet Pet + Owner
  ↓
FirebaseFirestore.collection("pets").add() ou .set()
  ↓
Retour à PetListActivity
```

---

### 4. **Adapter (Package `adapter/`)**

#### **PetAdapter.java** - Adaptateur RecyclerView
**Rôle** : Lie les données `Pet` à l'affichage dans la liste

**Fonctionnalités** :
- Crée les ViewHolders pour chaque item
- Affiche les données du pet dans `item_pet_card.xml`
- Charge les images avec Glide
- Gère les clics (pet, edit, delete)

**Relations** :
- Utilise `R.layout.item_pet_card` (layout de chaque item)
- Implémente interface `OnPetClickListener`
- Utilise `Glide` pour les images
- Communique avec `PetListActivity` via callbacks

**Pattern** :
```
RecyclerView
  ↓
PetAdapter
  ↓
PetViewHolder (pour chaque item)
  ↓
item_pet_card.xml (affichage)
```

---

### 5. **Utilitaires (Package `util/`)**

#### **ImageKitHelper.java** - Gestion des Images
**Rôle** : Upload d'images vers ImageKit

**Fonctionnalités** :
- Convertit URI en File
- Upload via API REST ImageKit
- Utilise OkHttp pour les requêtes HTTP
- Retourne l'URL de l'image uploadée

**Relations** :
- Utilisé par `AddPetActivity`
- Utilise `OkHttpClient` (bibliothèque OkHttp)
- Appelle l'API ImageKit

**Flux** :
```
URI de l'image (galerie)
  ↓
ImageKitHelper.uploadImage()
  ↓
OkHttp → ImageKit API
  ↓
URL de l'image (retour)
  ↓
Stockée dans Pet.photoUrls
```

---

## 🔄 Flux de Navigation {#flux-de-navigation}

```
┌─────────────────┐
│  App Launch     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  MainActivity   │◄──┐
│  (Vérifie Auth) │   │ (retour après logout)
└────────┬────────┘   │
         │             │
    ┌────┴────┐        │
    │         │        │
    ▼         ▼        │
┌─────────┐ ┌─────────┐│
│SignIn   │ │SignUp   ││
└────┬────┘ └────┬────┘│
     │           │     │
     └─────┬─────┘     │
           │           │
           ▼           │
    ┌──────────────┐   │
    │ MainActivity │───┘
    └──────┬───────┘
           │
      ┌────┴────┐
      │         │
      ▼         ▼
┌──────────┐ ┌──────────┐
│AddPet    │ │PetList   │
│Activity  │ │Activity  │
└────┬─────┘ └────┬──────┘
     │            │
     │            ▼
     │      ┌──────────────┐
     │      │PetDetail     │
     │      │Activity      │
     │      └──────────────┘
     │
     └──────────┐
                │
                ▼
         ┌──────────────┐
         │PetList       │
         │Activity      │
         │(refresh)     │
         └──────────────┘
```

---

## 💾 Flux de Données {#flux-de-données}

### **Création d'un Pet**

```
1. AddPetActivity
   ↓
2. Utilisateur remplit formulaire
   ↓
3. Sélectionne image → ImageKitHelper.uploadImage()
   ↓
4. ImageKit retourne URL
   ↓
5. Création objet Pet + Owner
   ↓
6. FirebaseFirestore.collection("pets").add(pet)
   ↓
7. Firestore sauvegarde
   ↓
8. Retour à PetListActivity
   ↓
9. PetListActivity.loadPets() (recharge)
```

### **Affichage des Pets**

```
1. PetListActivity.loadPets()
   ↓
2. FirebaseFirestore.collection("pets").get()
   ↓
3. Conversion documents → List<Pet>
   ↓
4. Filtrage/Recherche → filteredPets
   ↓
5. PetAdapter.setData(filteredPets)
   ↓
6. RecyclerView affiche items
   ↓
7. Pour chaque item : Glide charge l'image depuis URL
```

### **Clic sur un Pet**

```
1. Utilisateur clique sur item dans RecyclerView
   ↓
2. PetAdapter.onPetClick() → listener.onPetClick(pet)
   ↓
3. PetListActivity.onPetClick(Pet pet)
   ↓
4. Intent avec Pet (Serializable)
   ↓
5. PetDetailActivity reçoit Pet
   ↓
6. Affiche toutes les infos + image (Glide)
```

---

## 🔗 Relations entre Composants {#relations-entre-composants}

### **1. Activities ↔ Layouts**
Chaque Activity utilise un layout XML :
- `MainActivity` → `activity_main.xml`
- `PetListActivity` → `activity_pet_list.xml`
- `AddPetActivity` → `activity_add_pet.xml`
- `PetDetailActivity` → `activity_pet_detail.xml`
- `SignInActivity` → `activity_sign_in.xml`
- `SignUpActivity` → `activity_sign_up.xml`

### **2. Adapter ↔ Layout**
- `PetAdapter` → `item_pet_card.xml` (layout de chaque item)

### **3. Activities ↔ Models**
- Toutes les Activities utilisent `Pet` et `Owner`
- Les données sont passées via Intent (Serializable)

### **4. Activities ↔ Firebase**
- `MainActivity`, `SignInActivity`, `SignUpActivity` → `FirebaseAuth`
- `PetListActivity`, `AddPetActivity` → `FirebaseFirestore`

### **5. Activities ↔ Utils**
- `AddPetActivity` → `ImageKitHelper` (upload images)

### **6. Adapter ↔ Activities**
- `PetAdapter` implémente interface `OnPetClickListener`
- `PetListActivity` implémente cette interface
- Communication via callbacks

### **7. Resources (Ressources)**
- `strings.xml` : Tous les textes (utilisés via `R.string.xxx`)
- `colors.xml` : Couleurs (utilisées via `@color/xxx`)
- `themes.xml` : Thèmes Material Design
- `menu/main_menu.xml` : Menu toolbar (utilisé par MainActivity)

---

## 🛠️ Technologies Utilisées {#technologies-utilisées}

### **1. Firebase**
- **Firebase Auth** : Authentification email/password
- **Firebase Firestore** : Base de données NoSQL
  - Collection "pets" : stocke tous les animaux
  - Structure : Document ID → Pet object

### **2. ImageKit**
- Service cloud pour stockage d'images
- API REST pour upload
- Retourne URLs publiques

### **3. Glide**
- Bibliothèque pour charger/afficher images
- Utilisée dans : `PetAdapter`, `AddPetActivity`, `PetDetailActivity`
- Charge depuis URLs (ImageKit)

### **4. OkHttp**
- Bibliothèque HTTP pour requêtes réseau
- Utilisée par `ImageKitHelper` pour upload

### **5. Material Design**
- Composants Material (Buttons, TextFields, Cards, Chips)
- Thème personnalisé avec couleurs logo

---

## 📊 Schéma de l'Architecture Complète

```
┌─────────────────────────────────────────────────────────┐
│                    ANDROID APP                          │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌──────────────┐      ┌──────────────┐               │
│  │  Activities  │◄─────►│   Layouts    │               │
│  │  (Controller) │       │    (View)    │               │
│  └──────┬───────┘      └──────────────┘               │
│         │                                               │
│         ▼                                               │
│  ┌──────────────┐      ┌──────────────┐               │
│  │   Adapters   │◄─────►│   Models     │               │
│  │  (PetAdapter)│       │ (Pet, Owner) │               │
│  └──────┬───────┘      └──────────────┘               │
│         │                                               │
│         ▼                                               │
│  ┌──────────────┐      ┌──────────────┐               │
│  │   Utils      │      │  Resources    │               │
│  │(ImageKitHelper)│    │(strings,colors)│              │
│  └──────┬───────┘      └──────────────┘               │
│         │                                               │
└─────────┼───────────────────────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────┐
│                  SERVICES EXTERNES                      │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌──────────────┐      ┌──────────────┐               │
│  │   Firebase    │      │   ImageKit   │               │
│  │  (Auth + DB)  │      │  (Images)    │               │
│  └──────────────┘      └──────────────┘               │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

## 🎓 Points Clés pour la Soutenance

### **1. Architecture MVC**
- **Model** : Pet, Owner (données)
- **View** : Layouts XML
- **Controller** : Activities

### **2. Pattern Adapter**
- `PetAdapter` adapte les données `Pet` pour `RecyclerView`
- Pattern Observer pour les clics

### **3. Communication entre Composants**
- **Intent** : Passage de données entre Activities
- **Serializable** : Pet et Owner peuvent être passés via Intent
- **Callbacks** : Interface `OnPetClickListener` pour communication Adapter ↔ Activity

### **4. Gestion des Données**
- **Firestore** : Base de données cloud (NoSQL)
- **ImageKit** : Stockage d'images
- **Glide** : Chargement d'images depuis URLs

### **5. Cycle de Vie Android**
- `onCreate()` : Initialisation
- `onStart()` : Vérification auth
- `onResume()` : Rechargement données

### **6. Flux Asynchrone**
- Firebase : Callbacks asynchrones
- ImageKit : Upload asynchrone avec callbacks
- Glide : Chargement images asynchrone

---

## 🔍 Questions Probables du Professeur

### **Q1 : Comment les données circulent entre les écrans ?**
**R** : Via `Intent` avec objets `Serializable` (Pet, Owner). Par exemple, `PetListActivity` envoie un `Pet` à `PetDetailActivity` via Intent.

### **Q2 : Comment fonctionne l'upload d'images ?**
**R** : `ImageKitHelper` utilise OkHttp pour envoyer l'image à l'API ImageKit. L'API retourne une URL publique qui est stockée dans Firestore avec les données du pet.

### **Q3 : Comment la liste se met à jour après ajout/modification ?**
**R** : `PetListActivity.onResume()` recharge les données depuis Firestore. Quand on revient de `AddPetActivity`, la liste se rafraîchit automatiquement.

### **Q4 : Pourquoi utiliser un Adapter ?**
**R** : `RecyclerView` nécessite un Adapter pour convertir les données (`List<Pet>`) en vues affichables. L'Adapter gère aussi le recyclage des vues pour performance.

### **Q5 : Comment fonctionne l'authentification ?**
**R** : `FirebaseAuth` gère l'authentification. `MainActivity` vérifie si l'utilisateur est connecté au démarrage. Si non, redirection vers `SignInActivity`.

---

## ✅ Conclusion

L'application suit une architecture claire avec séparation des responsabilités :
- **Modèles** : Structure des données
- **Vues** : Interfaces utilisateur (XML)
- **Contrôleurs** : Logique métier (Activities)
- **Utilitaires** : Fonctions réutilisables (ImageKitHelper)
- **Adapters** : Liaison données-vues

Tous les composants communiquent via des mécanismes Android standards (Intent, Callbacks, Firebase) pour créer une application fonctionnelle et maintenable.


