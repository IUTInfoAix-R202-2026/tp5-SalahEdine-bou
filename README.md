[![Open in Codespaces](https://classroom.github.com/assets/launch-codespace-2972f46106e565e64193e422d61a12cf1da4916b45550586e14ef0a7c637dd04.svg)](https://classroom.github.com/open-in-codespaces?assignment_repo_id=24075742)
# <img src=".github/assets/logo.png" alt="class logo" class="logo" width="120"/> R2.02 - Développement d'applications avec IHM

### IUT d'Aix-Marseille - Département Informatique Aix-en-Provence

* **Ressource :** [Syllabus R2.02](https://github.com/IUTInfoAix-R202/syllabus) (compétences, calendrier, évaluations, ressources détaillées)

* **Équipe pédagogique :**

  * [Sébastien Nedjar](mailto:sebastien.nedjar@univ-amu.fr) - responsable du module
  * [Frédéric Flouvat](mailto:frederic.flouvat@univ-amu.fr)
  * [Sophie Nabitz](mailto:sophie.nabitz@univ-avignon.fr)
  * [Samir Chtioui](mailto:samir.chtioui@gmail.com)

* **Besoin d'aide ?**
    * Consulter et/ou créer des [issues](https://github.com/IUTInfoAix-R202/tp5/issues)
    * [Email](mailto:sebastien.nedjar@univ-amu.fr) pour toute question


## TP5 - Persistance des données avec JDBC et SQLite

> 🎓 **Accepter le devoir TP5 sur GitHub Classroom** : 👉 **https://classroom.github.com/a/jMN6Xq9q**
>
> Cela crée votre dépôt personnel `IUTInfoAix-R202-2026/tp5-votreLogin`. La marche à suivre (Classroom puis Codespace) est commune à tous les TP : voir le [TP1](https://github.com/IUTInfoAix-R202/tp1#mise-en-place).

> 📚 **Cours associé** : [CM4 - MVVM, persistance et synthèse](https://iutinfoaix-r202.github.io/cours/cm4-mvvm-persistance.html) (Partie 4 - Persistance). Chaque exercice ci-dessous renvoie aux slides correspondants.

## Objectifs de la séance

### Ce que vous saurez faire à la fin de cette séance

Les exercices de ce TP suivent la **taxonomie de Bloom** (du plus simple au plus complexe).

| Niveau Bloom | Exercices | Vous serez capable de... | Compétence BUT |
|---|---|---|---|
| **Comprendre** | 1-2 | vous **connecter** à une base SQLite et **lire** une table (les 5 étapes JDBC, `Connection`, `ResultSet`, puis une `DataSource`) | AC12.02 |
| **Appliquer** | 3-4 | écrire un **DAO** : d'abord en lecture (`findAll`, `getByCode`), puis le **CRUD complet** (insert / update / delete) avec `PreparedStatement` | AC12.02 |
| **Analyser** | 5-6 | lire des **relations** par des **jointures** (`JOIN`), et garantir l'atomicité d'un import par une **transaction** (`commit` / `rollback`) | AC12.02 |
| **Créer** | 7 | brancher la persistance sur une **IHM MVVM** (capstone) : une `TableView` de sites **persistés en base** (réutilise le MVVM + Guice du TP4) | AC11.02, AC11.04, AC12.02 |

> **Important** : les exercices 1 à 6 sont des exercices de persistance « back-end », **sans interface graphique**. On les valide avec `./mvnw test`. Seul le capstone (exercice 7) a une fenêtre.

### Pourquoi cette démarche

- **Pourquoi JDBC ?** C'est l'API standard et la **fondation** de tout accès relationnel en Java : tous les outils de plus haut niveau (Hibernate, Spring Data, jOOQ...) s'appuient au final sur JDBC. La comprendre, c'est comprendre ce qui se passe vraiment quand une application parle à une base.
- **Pourquoi un DAO ?** Écrire du SQL au milieu d'un ViewModel mélangerait deux responsabilités. On **encapsule** l'accès aux données dans une classe dédiée par entité (le *Data Access Object*) : le reste de l'application appelle des méthodes métier et ne voit jamais de SQL.
- **Pourquoi SQLite ?** Pour une application de bureau mono-utilisateur (comme la SAÉ), SQLite est idéal : zéro installation (une dépendance Maven), la base est un simple fichier, et le mode mémoire rend les tests gratuits. C'est le SGBD le plus déployé au monde, pas un jouet.
- **Pourquoi des `PreparedStatement` ?** Pour la sécurité (protection contre l'**injection SQL**) et la lisibilité. **Jamais** de concaténation de paramètres dans une requête.

### Lien avec la SAÉ 2.01 (VigieChiro PR Companion)

Le schéma de ce TP est un **sous-ensemble du MCD de la SAÉ** : `taxon` (espèces), `site`, `point_ecoute`, `passage`, `observation`. Vous y manipulez les vraies données du fil rouge (PR 1925492, carré 640380, espèces `Pippip`, `Nyclei`...).

| TP5 | Rôle dans la SAÉ |
|---|---|
| Ex 1-2 - Connexion + DataSource | savoir ouvrir et alimenter la base de l'application |
| Ex 3-4 - DAO Taxon (lecture) puis Site (CRUD) | la brique de base de la couche de persistance |
| Ex 5-6 - Jointures + transactions | lire des données reliées, importer sans corrompre la base |
| **Ex 7 - Capstone** | la liste des sites, **persistée**, en MVVM + Guice : le pont entre le TP4 (IHM) et les données |

> Pour la SAÉ, la **couche DAO complète vous sera fournie**. Ce TP est donc **formatif** : son but est que vous sachiez **lire, comprendre, construire et étendre** une telle couche. Vous travaillez ici deux entités à fond (Taxon, Site) ; en SAÉ vous réutiliserez la couche fournie pour toutes les autres avec les mêmes réflexes.

---

> [!NOTE]
> **Mise en place, évaluation, commandes, workflow Git, assistance IA et dépannage : identiques aux TP précédents.** Pour ne pas dupliquer ce qui a déjà été présenté aux TP1 à TP4, reportez-vous au README du TP1 :
> [Mise en place (Classroom + Codespace)](https://github.com/IUTInfoAix-R202/tp1#mise-en-place) · [Comment vous êtes évalué·e](https://github.com/IUTInfoAix-R202/tp1#rendu-du-travail-et-évaluation) · [Commandes Maven](https://github.com/IUTInfoAix-R202/tp1#commandes-essentielles) · [Workflow Git par exercice](https://github.com/IUTInfoAix-R202/tp1#workflow-de-développement---un-cycle-par-exercice) · [Assistance IA (Copilot Chat)](https://github.com/IUTInfoAix-R202/tp1#assistance-ia) · [Dépannage](https://github.com/IUTInfoAix-R202/tp1#dépannage)

Le TP5 comporte **7 exercices principaux + 6 bonus**, à faire dans l'ordre. Chaque exercice vit dans son propre sous-paquet (code et tests en miroir).

---

> [!TIP]
> **Le réflexe persistance de tout le TP** : le SQL vit **uniquement** dans les classes DAO. Le reste de l'application (ViewModel, services) appelle des méthodes (`findAll`, `insert`...) qui renvoient ou prennent des objets du domaine. Et on utilise **toujours** des `PreparedStatement` avec des `?`, jamais de concaténation de chaînes.

---

## Le modèle de données VigieChiro (le MCD de la SAÉ)

Tout le TP manipule une base **dérivée du Modèle Conceptuel de Données de la SAÉ 2.01**. Le modèle complet de la SAÉ compte **16 entités** (de l'`Utilisateur` jusqu'au `Groupe taxonomique`). Ce TP n'en travaille qu'un **sous-ensemble de 5**, largement suffisant pour pratiquer la connexion, le DAO, les jointures et les transactions.

Vous travaillez **Taxon** et **Site de suivi** *à fond* (lecture **et** écriture) ; **Point d'écoute**, **Passage** et **Observation** servent de support aux **jointures** (exercice 5) et à l'**import transactionnel** (exercice 6).

<img alt="MCD focalise du TP5 (5 entites travaillees)" width="100%" src=".github/assets/mcd-focalise.svg"/>

- **Taxon** : une espèce (ou catégorie) de chauve-souris identifiable par Tadarida, repérée par un `code` de 6 caractères (ex. `Pippip`). Travaillé en lecture à l'exercice 3.
- **Site de suivi** : un carré géographique suivi par l'utilisateur, identifié par son `n° carré` de 6 chiffres (ex. `640380`). Travaillé en CRUD complet à l'exercice 4 et affiché dans le capstone (exercice 7).
- **Point d'écoute** : un emplacement précis à l'intérieur d'un site (ex. `Z1`).
- **Passage** : une campagne d'enregistrement sur un point d'écoute, pour une `année` et un `n° de passage` donnés.
- **Observation** : une ligne de résultat Tadarida (un cri détecté), classée - ou non - par un `Taxon`. Le lien vers le `Taxon` est facultatif (`0..1`) : une observation peut rester non identifiée.

> [!NOTE]
> **Simplification pédagogique.** Dans le MCD complet de la SAÉ, une `Observation` est rattachée à son `Passage` par une chaîne d'entités intermédiaires (`Résultats d'identification`, `Séquence d'écoute`). Ce TP **raccourcit** ce chemin : ici une observation pointe directement vers son passage. Le diagramme complet ci-dessous situe ce sous-ensemble dans l'ensemble du modèle.

<details>
<summary><strong>Voir le MCD complet de la SAÉ</strong> (16 entités) - déplier si besoin</summary>

Source : [brief de la SAÉ 2.01, modèle conceptuel](https://iutinfoaix-s201.github.io/brief/Analyse%20et%20conception/Mod%C3%A8le%20conceptuel/).

<img alt="MCD complet de la SAE 2.01 (16 entites ; 5 en vert = travaillees au TP5)" width="100%" src=".github/assets/mcd-complet.svg"/>

</details>

### Des entités du MCD aux objets métiers

Une application Java vit dans le **« monde objet »** (des classes, des instances) ; la base, dans le **« monde relationnel »** (des tables, des lignes). Les faire dialoguer suppose de **traduire** le modèle relationnel en un **modèle objet** : chaque **table** devient une **classe**, chaque **ligne** un **objet**, chaque **colonne** un **champ**. Le diagramme de classes ci-dessus *est* déjà ce modèle objet ; on crée donc **une classe par entité** - l'**objet métier** (`Taxon`, `Site`...) - qui sert de porteur de données entre la base et le reste de l'application.

Historiquement, une telle classe métier suivait les conventions **JavaBean** : un constructeur sans argument, des champs privés exposés par des accesseurs `getXxx`/`setXxx`, et la surcharge de `toString()`, `equals()` et `hashCode()` (parfois `Serializable` en prime). C'est correct, mais **verbeux**. Depuis Java 16, on écrit ces porteurs de données avec un **`record`** :

```java
public record Taxon(String code, String nomLatin, String nomVernaculaire) {}
```

Un `record` est **immuable** et génère pour vous le constructeur, les accesseurs (`code()`, `nomLatin()`... **sans** le préfixe `get`), ainsi que `equals()`, `hashCode()` et `toString()`. C'est exactement le rôle d'un objet métier (un *Data Transfer Object*), en une ligne. Les exercices 3 à 7 manipulent ces records (`Taxon`, `Site`, `PointEcoute`, `ObservationAImporter`) : les DAO les **produisent** en lecture et les **consomment** en écriture.

> [!NOTE]
> La correspondance n'est pas toujours « 1 classe = 1 table » (on peut regrouper ou découper). Mais dans ce TP, chaque entité travaillée a sa classe, au plus près des tables.

---

## Exercice 1 - Premier contact JDBC : se connecter et lire

### 📖 JDBC, le pont entre Java et le monde relationnel

Une application doit souvent **conserver** ses données au-delà de sa propre exécution : on dit qu'elle les **persiste**. Le réceptacle le plus répandu est la **base de données relationnelle**, où l'information est rangée dans des **tables** (en théorie : des *relations*) : des lignes (*tuples*) décrites par des colonnes (*attributs*). Interroger une table, c'est faire de l'**algèbre relationnelle** sans le dire : un `SELECT code, nom FROM taxon WHERE code = 'Pippip'` *projette* (choisit des colonnes) et *sélectionne* (filtre des lignes).

Côté Java, le dialogue avec ces bases passe par **JDBC** (*Java DataBase Connectivity*), l'API standard de la plateforme. Son intérêt tient à une idée : **une seule API, des pilotes interchangeables**. JDBC s'organise en trois couches - votre **code** appelle l'**API JDBC** (des interfaces : `Connection`, `Statement`, `ResultSet`...), le **DriverManager** choisit le bon **pilote**, et le pilote traduit vos appels en ordres compris par le SGBD cible. Conséquence : passer de SQLite à PostgreSQL ne change (en théorie) qu'une ligne d'URL et une dépendance, pas votre code.

Tout accès JDBC suit le **même scénario en 5 temps** : (1-2) ouvrir une `Connection`, (3) créer une instruction, (4) l'exécuter et parcourir le résultat, (5) tout refermer. C'est ce scénario, et lui seul, que vous allez exécuter dans cet exercice.

**Objectif** : le jalon fondateur du TP - **ouvrir une connexion à une base et lire une table**.

**Ce que vous allez découvrir** :
- Une base SQLite **en mémoire** (`jdbc:sqlite::memory:`) : jetable, sans fichier, parfaite pour débuter et pour les tests.
- La [`Connection`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/Connection.html) ouverte par [`DriverManager`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/DriverManager.html), et fermée **automatiquement** par le `try-with-resources`.
- Le [`Statement`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/Statement.html) et l'exécution d'un `SELECT` par `executeQuery`.
- Le [`ResultSet`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/ResultSet.html) et son parcours ligne à ligne avec `while (rs.next())`, puis la lecture d'une colonne par `rs.getString("...")`.

> 📚 **Cours** : [CM4 #48-51](https://iutinfoaix-r202.github.io/cours/cm4-mvvm-persistance.html#48) - l'architecture de JDBC, le scénario en 7 étapes, le canon d'un `SELECT` complet et l'ouverture d'une `Connection`.

**Le matériel fourni** dans le paquet `fr.univ_amu.iut.exercice1` :
- `ExempleJDBC.java` : la classe de l'exercice. `URL_MEMOIRE` et `creerEtRemplirTable(...)` (création + remplissage de la table `taxon`) sont **fournis complets** ; seule `lireTaxons(...)` est **à compléter** (`// TODO exercice 1`).
- `ExempleJDBCTest.java` : **2 tests JUnit** (sans fenêtre) à activer un par un.

### 📋 Mémo - le scénario JDBC en 5 temps

<img alt="Mémo - le scénario JDBC en 5 temps" src=".github/assets/memo-le-scenario-jdbc-en-5-temps.svg"/>

Le **patron** générique d'une lecture (à adapter, ce n'est pas la solution) :

```java
try (Statement st = connexion.createStatement();
     ResultSet rs = st.executeQuery("SELECT ... FROM ...")) {
  while (rs.next()) {
    // rs.getString("uneColonne"), rs.getInt("uneAutre"), ...
  }
} // st et rs fermés automatiquement, même en cas d'exception
```

**Travail à faire** : compléter `lireTaxons(Connection)`. Trois gestes (le détail est dans le `// TODO`).

1. **Créer l'instruction** : un `Statement` via `connexion.createStatement()`, dans un `try-with-resources`.
2. **Exécuter** le `SELECT code, nom_vernaculaire FROM taxon` avec `executeQuery`, qui renvoie un `ResultSet`.
3. **Parcourir** le `ResultSet` avec `while (rs.next())` et, pour chaque ligne, ajouter à la liste la chaîne `code + " - " + nom_vernaculaire` (avec `rs.getString`).

**Progression des tests**, à activer un par un :

| Test | Ce qui est vérifié | Geste |
|---|---|---|
| `la_lecture_renvoie_les_quatre_taxons_du_fil_rouge` | les 4 taxons sont lus | gestes 1+2+3 |
| `chaque_ligne_a_le_format_code_tiret_nom` | chaque ligne a le format `code - nom` | geste 3 |

> [!TIP]
> Cet exercice n'ouvre **aucune fenêtre** : on le valide avec `./mvnw test -Dtest=ExempleJDBCTest`. La base vit **en mémoire**, il n'y a donc rien à inspecter sur disque - ce sera différent dès l'exercice 2, où la base devient un vrai fichier.

---

## Exercice 2 - Une DataSource et un schéma

### 📖 Du DriverManager à la DataSource : programmer contre une abstraction

À l'exercice 1, on ouvrait une connexion « à la main » avec `DriverManager.getConnection(url)`. Cela fonctionne, mais cela **fige** dans le code l'URL, le pilote, les identifiants. En pratique, on préfère dépendre d'une **abstraction** : l'interface [`javax.sql.DataSource`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/javax/sql/DataSource.html), « un objet qui sait fournir des connexions ». Le code au-dessus (DAO, services) ne réclame plus qu'une `DataSource` et ignore tout du SGBD : c'est le **principe d'inversion des dépendances**, et c'est ce qui permettra (au capstone) d'**injecter** la base via Guice, ou (au bonus 9) de remplacer SQLite par un pool de connexions **sans toucher au code métier**.

Deux notions de base s'ajoutent ici. D'abord la distinction **schéma vs données** : créer les tables relève du **DDL** (`CREATE TABLE`, ici `schema.sql`), les peupler du **DML** (`INSERT`, ici `seed.sql`). Ensuite l'**intégrité référentielle** : une clé étrangère interdit d'insérer une ligne qui pointe vers une ligne inexistante (une observation rattachée à un taxon fantôme). Particularité de SQLite : pour rester rétro-compatible, il **n'applique PAS les clés étrangères par défaut** ; il faut l'activer explicitement (`enforceForeignKeys(true)`). On verra à l'exercice 6 que c'est précisément ce qui permet à une transaction de refuser un import incohérent.

**Objectif** : passer de la connexion « à la main » à une **`DataSource`**, et initialiser un vrai schéma.

**Ce que vous allez découvrir** :
- L'interface [`DataSource`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/javax/sql/DataSource.html) et son implémentation SQLite `org.sqlite.SQLiteDataSource`, configurée par `org.sqlite.SQLiteConfig`.
- L'activation explicite des **clés étrangères** SQLite (`enforceForeignKeys(true)`).
- Le chargement d'un schéma (`schema.sql`) et de données (`seed.sql`) : la distinction **DDL / DML**.
- Une base désormais dans un **fichier** (testée via `@TempDir`, nettoyé automatiquement), donc plusieurs connexions possibles sur la même base.

> 📚 **Cours** : [CM4 #60-61](https://iutinfoaix-r202.github.io/cours/cm4-mvvm-persistance.html#60) - SQLite comme base locale d'une application de bureau, et pourquoi ce choix convient à la SAÉ.

**Le matériel fourni** dans le paquet `fr.univ_amu.iut.exercice2` :
- `BaseDeDonnees.java` : `initialiser(...)` et toute la mécanique de chargement des scripts SQL sont **fournies complètes** ; seule `surFichier(String)` est **à compléter** (`// TODO exercice 2`).
- `src/main/resources/db/schema.sql` et `seed.sql` : le schéma et les données du fil rouge, **fournis** (lisez-les, ils décrivent le MCD).
- `BaseDeDonneesTest.java` : **3 tests JUnit** (base dans un fichier temporaire `@TempDir`).

### 📋 Mémo - la DataSource, une abstraction à deux faces

<img alt="Mémo - la DataSource, une abstraction à deux faces" src=".github/assets/memo-la-datasource-une-abstraction-a-deux-faces.svg"/>

Côté **consommateur** (tous les exercices suivants), on n'utilise une `DataSource` que comme ceci - jamais l'URL :

```java
try (Connection connexion = source.getConnection()) {
  // ... requêtes ...
}
```

Côté **fournisseur** (cet exercice), il s'agit de **construire** cette `DataSource` : on configure une `SQLiteConfig`, on en fait une `SQLiteDataSource`, on lui donne son URL `jdbc:sqlite:<chemin>`.

**Travail à faire** : compléter `surFichier(String chemin)`. Deux gestes (le détail est dans le `// TODO`).

1. **Activer l'intégrité référentielle** : créer une `SQLiteConfig` et lui demander `enforceForeignKeys(true)`.
2. **Construire la `DataSource`** : créer une `SQLiteDataSource(config)`, lui donner l'URL `"jdbc:sqlite:" + chemin`, puis l'affecter à `source`.

**Progression des tests**, à activer un par un :

| Test | Ce qui est vérifié | Geste |
|---|---|---|
| `la_base_initialisee_contient_les_quatre_taxons` | la `DataSource` permet de lire la base initialisée | geste 2 |
| `le_site_du_fil_rouge_est_present` | les données du fil rouge sont chargées | geste 2 |
| `les_cles_etrangeres_sont_actives` *(nouveau)* | une insertion violant une clé étrangère est **refusée** | geste 1 |

> [!TIP]
> Pour **inspecter** la base produite, ouvrez le fichier `.db` avec l'extension SQLite de VS Code (ou la commande `sqlite3 chemin.db ".tables"`). À partir d'ici, toutes les bases du TP sont de vrais fichiers.

---

## Exercice 3 - Premier DAO : lecture (TaxonDao)

### 📖 Le DAO

Écrire du JDBC directement au milieu du code métier a **deux effets néfastes** bien identifiés. D'abord une **pollution** : le SQL et la mécanique `Connection`/`ResultSet` noient la logique métier, le code devient illisible et le risque d'erreur grimpe. Ensuite une **dépendance forte au SGBD** : métier et accès aux données s'entremêlent, si bien que changer de base (SQLite → PostgreSQL) devient délicat, voire impossible.

La réponse est le patron **DAO** (*Data Access Object*) : une couche dédiée qui **encapsule tous les accès** à la source de données. Le reste de l'application ne parle qu'aux objets de cette couche (des **objets du domaine**, ici des `Taxon`) et ignore tout du stockage : le DAO est une **abstraction du modèle de données, indépendante de la solution de stockage**. On prévoit **au moins un DAO par entité** du MCD, nommé d'après elle (`TaxonDao`, `SiteDao`...).

Chaque DAO offre les quatre opérations de base de la persistance - le **CRUD** (*Create, Retrieve, Update, Delete*) - suivant une convention de nommage répandue :

- **`insert`** / **`update`** / **`delete`** : les écritures (exercice 4) ;
- **`find...`** : une lecture pouvant renvoyer **plusieurs** résultats (`findAll`) ;
- **`get...`** : une lecture renvoyant **exactement un** résultat, recherché par sa clé (`getByCode`) ;
- **`compute...`** : un calcul délégué au SGBD (un `COUNT`, une moyenne...).

Un DAO complet - ici le `SiteDao` de l'exercice 4 - expose donc ces opérations (le `TaxonDao` de cet exercice n'en couvre que la moitié *lecture*) :

<img alt="SiteDao : un DAO complet (findAll, getByNumeroCarre, insert, update, delete) qui mappe des Site et obtient ses connexions aupres d'une DataSource" width="520" src=".github/assets/dao-sitedao.svg"/>

Écrire et maintenir cette couche à la main est **fastidieux et répétitif** - c'est précisément pourquoi des outils comme Hibernate ou Spring Data en **génèrent** l'essentiel (vous le mesurerez au bonus 8 en factorisant la boucle `ResultSet`).

**La hiérarchie des DAO.** Tous les DAO partagent les mêmes opérations de base (`findAll`, `insert`, `update`, `delete`). Pour que le code client dépende d'une **abstraction** et non d'une implémentation précise, on extrait alors une **interface** commune que chaque DAO implémente : le client manipule un `Dao`, jamais directement un `...DaoJdbc`. On peut aller plus loin avec une *fabrique* (`AbstractFactory`) qui fournit le bon DAO sans que le client connaisse la technologie de stockage. Vous mettrez cette idée en pratique au **bonus 12** (l'interface `SitesDao` et ses deux implémentations, JDBC ou en mémoire).

<img alt="Hierarchie des DAO : une interface Dao implementee par TaxonDao et SiteDao (faits dans le TP) plus PointEcouteDao, PassageDao, ObservationDao (a venir)" width="100%" src=".github/assets/dao-hierarchie.svg"/>

*En bleu, les DAO écrits dans ce TP (`TaxonDao`, `SiteDao`) ; en gris, ceux qu'une couche complète ajouterait pour les autres entités du MCD - tous sur le même patron.*

Un DAO fait deux choses à chaque lecture : exécuter une requête, puis **transformer** chaque ligne du `ResultSet` en objet (le *mapping*, ici la méthode `depuis(rs)` fournie). Et il le fait avec un [`PreparedStatement`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/PreparedStatement.html), **jamais** par concaténation de chaînes. Pourquoi ? Parce qu'écrire `"... WHERE code = '" + code + "'"` ouvre la porte à l'**injection SQL** : si `code` vaut `' OR '1'='1`, la requête renvoie toute la table ; pire, un paramètre malicieux peut détruire des données. Le `?` d'un `PreparedStatement`, lié par `setString(1, code)`, est transmis **séparément** de la requête : il ne peut jamais être interprété comme du code SQL. Sécurité **et** lisibilité.

Dernier détail : une recherche par identifiant peut ne **rien** trouver. Plutôt que de renvoyer `null` (et risquer le `NullPointerException`), on renvoie un [`Optional<Taxon>`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/Optional.html) : « peut-être un taxon, peut-être rien », explicite et sûr.

**Objectif** : écrire le premier *Data Access Object*, en lecture seule, sur la table `taxon`.

**Ce que vous allez découvrir** :
- Le patron **DAO** : une classe par entité, qui reçoit une `DataSource` et expose des méthodes métier.
- Le [`PreparedStatement`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/PreparedStatement.html) et ses paramètres `?` liés par `setString` : la protection contre l'**injection SQL**.
- Le **mapping** `ResultSet` → objet du domaine (la méthode `depuis(rs)`, fournie).
- L'[`Optional<T>`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/Optional.html) pour modéliser « peut-être absent ».

> 📚 **Cours** : [CM4 #62](https://iutinfoaix-r202.github.io/cours/cm4-mvvm-persistance.html#62) (le patron DAO), [CM4 #52](https://iutinfoaix-r202.github.io/cours/cm4-mvvm-persistance.html#52) (`Statement` vs `PreparedStatement`) et [CM4 #54](https://iutinfoaix-r202.github.io/cours/cm4-mvvm-persistance.html#54) (parcourir un `ResultSet`).

**Le matériel fourni** dans le paquet `fr.univ_amu.iut.exercice3` :
- `Taxon.java` : le modèle (`record` immuable), **fourni complet**.
- `TaxonDao.java` : le constructeur (qui reçoit la `DataSource`) et le mapping `depuis(rs)` sont **fournis** ; `findAll()` et `getByCode(String)` sont **à compléter** (`// TODO exercice 3`).
- `TaxonDaoTest.java` : **3 tests JUnit** (base temporaire `@TempDir`, peuplée par le `seed.sql`).

### 📋 Mémo - le patron d'une lecture DAO

<img alt="Mémo - le patron d'une lecture DAO" src=".github/assets/memo-le-patron-d-une-lecture-dao.svg"/>

Le **patron** d'une lecture paramétrée (à adapter, ce n'est pas la solution) :

```java
String sql = "SELECT ... FROM ... WHERE colonne = ?";
try (Connection connexion = source.getConnection();
     PreparedStatement ps = connexion.prepareStatement(sql)) {
  ps.setString(1, valeur);                 // paramètre lié, jamais concaténé
  try (ResultSet rs = ps.executeQuery()) {
    while (rs.next()) { /* ... depuis(rs) ... */ }
  }
} catch (SQLException e) {
  throw new DataAccessException("message", e);
}
```

**Travail à faire** : compléter les deux lectures. Deux gestes (le détail est dans les `// TODO`).

1. **`findAll()`** : exécuter le `SELECT` (sans paramètre), parcourir le `ResultSet` et, pour chaque ligne, ajouter `depuis(rs)` à la liste. Toute `SQLException` devient une `DataAccessException`.
2. **`getByCode(String code)`** : lier le `?` au code (`setString`), exécuter ; si une ligne existe, renvoyer un `Optional` du `Taxon` construit par `depuis(rs)`, sinon un `Optional` vide.

**Progression des tests**, à activer un par un :

| Test | Ce qui est vérifié | Geste |
|---|---|---|
| `trouver_tous_renvoie_les_quatre_taxons_tries_par_code` | la lecture de toute la table, triée | geste 1 |
| `trouver_par_code_renvoie_le_taxon_attendu` | la requête paramétrée renvoie le bon taxon | geste 2 |
| `trouver_par_code_inconnu_renvoie_vide` | un code absent renvoie `Optional.empty()` | geste 2 |

> [!TIP]
> Comparez vos deux méthodes : `findAll` n'a **pas** de paramètre, `getByCode` en a un (le `?`). C'est tout l'apport du `PreparedStatement`. Validez avec `./mvnw test -Dtest=TaxonDaoTest`.

---

## Exercice 4 - CRUD complet (SiteDao)

### 📖 Les écritures, `executeUpdate` et le piège du `WHERE`

Lire ne suffit pas : une application doit aussi **créer**, **modifier** et **supprimer**. Avec les opérations de lecture, cela forme le **CRUD** (*Create, Read, Update, Delete*), le contrat de base de tout DAO. Côté JDBC, une écriture ne s'exécute pas avec `executeQuery` (qui renvoie un `ResultSet`) mais avec [`executeUpdate`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/PreparedStatement.html), qui renvoie un **entier** : le nombre de lignes affectées. Ce nombre est précieux (un `update` qui renvoie 0 signale que rien n'a été modifié).

Le piège classique des écritures, c'est la **clause `WHERE`**. Un `UPDATE site SET nom = ?` **sans** `WHERE` modifie **toutes** les lignes de la table ; un `DELETE FROM site` sans `WHERE` **vide** la table entière. Le `WHERE id = ?` qui cible une ligne précise n'est donc pas un détail : c'est ce qui distingue « modifier un site » de « écraser tous les sites ». Et attention à l'**ordre des paramètres** : dans `UPDATE site SET nom = ? WHERE numero_carre = ?`, les `?` se lisent **de gauche à droite**, donc le critère du `WHERE` est le **dernier** paramètre.

Enfin, on poursuit la convention de la couche de persistance : toute `SQLException` (vérifiée, technique) est **traduite** en `DataAccessException` (non vérifiée) pour ne pas polluer le code métier.

**Objectif** : compléter le DAO de l'entité `Site` avec les **écritures** : créer, modifier, supprimer.

**Ce que vous allez découvrir** :
- [`executeUpdate()`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/PreparedStatement.html) (pour `INSERT` / `UPDATE` / `DELETE`) et son retour : le nombre de lignes affectées.
- L'importance de la clause `WHERE` et l'**ordre des paramètres** (le critère du `WHERE` en dernier).
- La traduction `SQLException` → `DataAccessException` (déjà vue en lecture, ici sur des écritures).

> 📚 **Cours** : [CM4 #58](https://iutinfoaix-r202.github.io/cours/cm4-mvvm-persistance.html#58) (`INSERT` / `UPDATE` / `DELETE` avec `executeUpdate`) ; le patron DAO reste celui du [CM4 #62](https://iutinfoaix-r202.github.io/cours/cm4-mvvm-persistance.html#62).

**Le matériel fourni** dans le paquet `fr.univ_amu.iut.exercice4` :
- `Site.java` : le modèle (`record`), **fourni complet**.
- `SiteDao.java` : les **lectures** (`findAll`, `getByNumeroCarre`) et le mapping `depuis(rs)` sont **fournis** (sur le modèle de l'exercice 3) ; `insert`, `update`, `delete` sont **à compléter** (`// TODO exercice 4`).
- `SiteDaoTest.java` : **5 tests JUnit** (base temporaire `@TempDir`).

### 📋 Mémo - le patron d'une écriture DAO

```java
String sql = "UPDATE table SET col = ? WHERE critere = ?";  // le critère en DERNIER
try (Connection connexion = source.getConnection();
     PreparedStatement ps = connexion.prepareStatement(sql)) {
  ps.setString(1, ...);     // d'abord les colonnes du SET
  ps.setString(2, ...);     // puis le critère du WHERE
  ps.executeUpdate();       // pas executeQuery : renvoie le nombre de lignes touchées
} catch (SQLException e) {
  throw new DataAccessException("message", e);
}
```

**Travail à faire** : compléter les trois écritures. Trois gestes (le détail est dans les `// TODO`).

1. **`insert(Site site)`** : `INSERT`, lier les **5 paramètres** dans l'ordre des colonnes, `executeUpdate`.
2. **`update(Site site)`** : `UPDATE`, lier les 4 champs puis - **en dernier** - le `numero_carre` de la clause `WHERE`, `executeUpdate`.
3. **`delete(String numeroCarre)`** : `DELETE ... WHERE numero_carre = ?`, `executeUpdate`.

**Progression des tests**, à activer un par un :

| Test | Ce qui est vérifié | Geste |
|---|---|---|
| `inserer_ajoute_un_site_relisible` | l'insertion est persistée et relisible | geste 1 |
| `mettre_a_jour_modifie_les_champs_du_site` | l'`UPDATE` modifie bien le site visé | geste 2 |
| `mettre_a_jour_ne_modifie_que_le_site_vise` *(nouveau)* | l'`UPDATE` **ne touche pas** les autres sites (clause `WHERE`) | geste 2 |
| `supprimer_retire_le_site` | le `DELETE` retire le site visé | geste 3 |
| `supprimer_ne_retire_que_le_site_vise` *(nouveau)* | le `DELETE` **ne touche pas** les autres sites (clause `WHERE`) | geste 3 |

> [!TIP]
> Le test `..._ne_..._que_le_site_vise` est un garde-fou : si vous oubliez le `WHERE`, votre écriture touche toute la table et ce test devient rouge. Validez avec `./mvnw test -Dtest=SiteDaoTest`.

---

## Exercice 5 - Relations et jointures

### 📖 Les relations, et les jointures pour les lire

Une base relationnelle bien conçue **ne duplique pas** l'information : plutôt que de répéter le nom d'une espèce dans chaque observation, on le range **une fois** dans la table `taxon`, et chaque observation y **renvoie** par une **clé étrangère** (`code_taxon`). C'est la *normalisation* : moins de redondance, donc moins d'incohérences possibles. Le revers, c'est qu'une donnée « complète » est désormais **éparpillée** sur plusieurs tables. Pour la reconstituer, on **recolle** les tables avec une **jointure** (`JOIN ... ON`), qui apparie les lignes selon une condition (typiquement clé étrangère = clé primaire). En algèbre relationnelle, c'est l'opérateur de *jointure* ; en SQL, c'est `JOIN`.

Ici, savoir « quelles espèces ont été détectées sur un site » oblige à remonter une **chaîne** : une `observation` appartient à un `passage` (lui-même rattaché à un site), et classe un `taxon`. La requête enchaîne donc deux jointures : `observation → passage` (pour filtrer par site) et `observation → taxon` (pour récupérer le nom de l'espèce). Deux raffinements achèvent le travail : `DISTINCT` élimine les doublons (une même espèce détectée plusieurs fois ne doit apparaître qu'une fois) et `ORDER BY` trie le résultat.

**Objectif** : lire des données **reliées** entre plusieurs tables grâce aux **jointures** SQL.

**Ce que vous allez découvrir** :
- Les **clés étrangères** comme matérialisation d'une relation, et la **normalisation** (ne pas dupliquer).
- La **jointure** `JOIN ... ON` qui recolle plusieurs tables, ici sur **trois** tables.
- `DISTINCT` (dédoublonner) et `ORDER BY` (trier) dans un [`SELECT`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/ResultSet.html) parcouru ligne à ligne.

> 📚 **Cours** : [CM4 #50](https://iutinfoaix-r202.github.io/cours/cm4-mvvm-persistance.html#50) (le canon d'un `SELECT` complet) et [CM4 #54](https://iutinfoaix-r202.github.io/cours/cm4-mvvm-persistance.html#54) (parcourir le `ResultSet`). Le `JOIN` proprement dit relève de la ressource **bases de données** ; ce TP le pratique depuis Java.

**Le matériel fourni** dans le paquet `fr.univ_amu.iut.exercice5` :
- `PointEcoute.java` : le modèle (`record`), **fourni complet**.
- `DetailSiteDao.java` : `findPointsDuSite(...)` est **fourni** (une lecture simple, sur le modèle des exercices 3-4, à lire comme exemple) ; `findEspecesObserveesSurLeSite(String)` est **à compléter** (`// TODO exercice 5`, qui contient la requête `JOIN`).
- `DetailSiteDaoTest.java` : **3 tests JUnit** (base temporaire `@TempDir`).

### 📋 Mémo - la chaîne de jointures

<img alt="Mémo - la chaîne de jointures" src=".github/assets/memo-la-chaine-de-jointures.svg"/>

On part du site (le filtre `WHERE p.numero_carre = ?`), on remonte jusqu'au `taxon` pour lire le nom de l'espèce. `DISTINCT` garantit qu'une espèce détectée plusieurs fois n'apparaît **qu'une fois** ; `ORDER BY` trie. La requête complète est dans le `// TODO` (le SQL n'est pas le cœur de l'exercice, c'est l'**enchaînement des jointures** qui compte).

**Travail à faire** : compléter `findEspecesObserveesSurLeSite(String numeroCarre)`. Un geste.

1. Exécuter la **jointure 3 tables** (fournie dans le `// TODO`), lier le paramètre du site, parcourir le `ResultSet` et ajouter chaque `nom_vernaculaire` à la liste. Toute `SQLException` devient une `DataAccessException`.

**Progression des tests**, à activer un par un :

| Test | Ce qui est vérifié | Geste |
|---|---|---|
| `la_jointure_remonte_les_especes_observees_sur_le_site` | la jointure 3 tables renvoie les espèces, triées | geste 1 |
| `la_jointure_dedoublonne_les_especes` *(nouveau)* | `DISTINCT` : une espèce vue plusieurs fois n'apparaît qu'une fois | geste 1 |
| `un_site_sans_observation_renvoie_une_liste_vide` | un site sans observation renvoie une liste vide | geste 1 |

> [!TIP]
> Inspectez la base avec l'extension SQLite et essayez la requête à la main avant de la coder. Validez avec `./mvnw test -Dtest=DetailSiteDaoTest`.

---

## Exercice 6 - Transactions : tout ou rien

### 📖 La transaction, l'unité du « tout ou rien »

Importer un passage et ses observations, ce sont **plusieurs** écritures (un `INSERT` dans `passage`, puis un `INSERT` par observation). Que se passe-t-il si la troisième échoue ? Sans précaution, on se retrouve avec un passage **à moitié** importé : la base est dans un état **incohérent**. La parade est la **transaction** : un groupe d'opérations traité comme **une seule**, indivisible. On résume ses garanties par l'acronyme **ACID** - **A**tomicité (tout ou rien), **C**ohérence (la base passe d'un état valide à un autre), **I**solation (les transactions concurrentes ne se marchent pas dessus), **D**urabilité (une fois validé, c'est gravé).

En JDBC, une connexion est par défaut en mode **auto-commit** : chaque ordre est validé immédiatement, isolément. Pour grouper plusieurs ordres en une transaction, il faut donc **désactiver** ce mode (`setAutoCommit(false)`), exécuter toutes les écritures, puis **valider** explicitement (`commit()`). Si quoi que ce soit échoue, on **annule tout** (`rollback()`) : la base revient à l'état d'avant la transaction. C'est précisément l'intégrité référentielle de l'exercice 2 qui déclenche ici l'échec - insérer une observation au taxon inexistant viole une clé étrangère, lève une `SQLException`, et notre `rollback` annule **jusqu'au passage déjà inséré**.

Détail technique utile : la clé du passage est un entier **auto-incrémenté** par la base. On ne la connaît qu'**après** l'`INSERT` ; on la récupère avec `Statement.RETURN_GENERATED_KEYS` puis `getGeneratedKeys()`, pour pouvoir rattacher les observations à ce passage. (À noter : on ouvre la connexion **avant** le `try` et on la ferme dans un `finally`, afin de pouvoir appeler `rollback()` dans le `catch` - un `try-with-resources` la fermerait trop tôt.)

**Objectif** : garantir l'**atomicité** d'un import : si une partie échoue, **rien** n'est écrit.

**Ce que vous allez découvrir** :
- Les propriétés **ACID** et la notion de transaction.
- [`setAutoCommit(false)`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/Connection.html), `commit()`, `rollback()` sur la [`Connection`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/Connection.html).
- Les **clés générées** : `Statement.RETURN_GENERATED_KEYS` + `getGeneratedKeys()`.
- La gestion manuelle de la connexion (ouverte avant le `try`, fermée dans le `finally`) pour pouvoir faire `rollback`.

> 📚 **Cours** : [CM4 #59](https://iutinfoaix-r202.github.io/cours/cm4-mvvm-persistance.html#59) - les transactions, `commit` et `rollback`.

**Le matériel fourni** dans le paquet `fr.univ_amu.iut.exercice6` :
- `ObservationAImporter.java` : le modèle de transfert (`record`), **fourni complet**.
- `ImportPassageService.java` : `nombrePassages()` et les utilitaires de fermeture/annulation silencieuses sont **fournis** ; `importer(...)` est **à compléter** (`// TODO exercice 6`).
- `ImportPassageServiceTest.java` : **3 tests JUnit** (base temporaire `@TempDir`).

### 📋 Mémo - le patron d'une transaction

```java
Connection connexion = null;
try {
  connexion = source.getConnection();
  connexion.setAutoCommit(false);   // début de transaction
  // ... plusieurs écritures (dont la récupération de la clé générée) ...
  connexion.commit();               // tout a réussi -> on valide
} catch (SQLException e) {
  if (connexion != null) connexion.rollback();   // un échec -> on annule TOUT
  throw new DataAccessException("message", e);
} finally {
  if (connexion != null) connexion.close();       // toujours refermer
}
```

**Travail à faire** : compléter `importer(...)`. Quatre gestes (le détail est dans le `// TODO`).

1. Ouvrir la connexion (**avant** le `try`) et `setAutoCommit(false)`.
2. Insérer le passage avec `RETURN_GENERATED_KEYS`, puis récupérer l'`id` généré (`getGeneratedKeys`).
3. Insérer **chaque** observation avec ce `passageId`.
4. `commit()` à la fin ; dans le `catch`, `rollback()` puis lever une `DataAccessException` ; fermer la connexion dans le `finally`.

**Progression des tests**, à activer un par un :

| Test | Ce qui est vérifié | Geste |
|---|---|---|
| `un_import_valide_persiste_le_passage` | le passage est inséré, l'`id` généré est renvoyé | gestes 1+2+4 |
| `un_import_valide_persiste_les_observations` *(nouveau)* | les observations sont bien insérées et rattachées | geste 3 |
| `un_import_avec_un_taxon_inexistant_est_entierement_annule` | un échec annule **tout** (rollback, atomicité) | gestes 1+4 |

> [!TIP]
> Pour « voir » l'atomicité : commentez temporairement votre `rollback`, relancez le test d'échec, et constatez qu'un passage orphelin reste en base. Remettez le `rollback` : la base reste propre. Validez avec `./mvnw test -Dtest=ImportPassageServiceTest`.

---

## Exercice 7 - Capstone : mes sites de suivi, persistés (MVVM + Guice + SQLite)

### 📖 L'architecture en couches, assemblée par l'injection

Ce dernier exercice **réunit tout le module**. Une application bien construite s'organise en **couches** empilées, chacune ne parlant qu'à sa voisine : la **Vue** (FXML + contrôleur) affiche et capte les gestes ; le **ViewModel** (TP4) tient l'état de l'écran sous forme de propriétés observables et expose des commandes ; le **DAO** (TP5) traduit ces intentions en SQL ; la **DataSource** fournit les connexions ; **JDBC** parle à **SQLite**. Chaque couche ignore les détails de celle d'en dessous : le ViewModel ne sait pas qu'il y a du SQL, la Vue ne sait pas qu'il y a une base.

Reste à **assembler** ces couches. Plutôt que chaque classe fasse `new` de ses dépendances (ce qui les soude entre elles), on confie l'assemblage à un **conteneur d'injection** : **Guice** (TP4). Le `PersistanceModule` dit « voici comment fabriquer une `DataSource`, un `SiteDao` » ; Guice injecte alors le `SiteDao` dans le `SitesViewModel`, et le ViewModel dans le contrôleur. On peut ainsi remplacer la vraie base par une base de test sans toucher au reste - c'est l'objet du bonus 12.

Le bénéfice concret, lui, est tangible : comme la liste des sites vit dans une base, elle **survit à la fermeture de l'application**. C'est exactement ce qu'on attend d'un logiciel, et ce que vous construirez en SAÉ. L'écran applique au passage deux heuristiques de Nielsen : une **confirmation** avant suppression (prévention des erreurs, #5) et un **message clair** si la suppression est impossible (récupération après erreur, #9).

**Objectif** : brancher la persistance (TP5) sur une IHM MVVM (TP4), le tout câblé par Guice.

### Maquette à reproduire

Voici l'interface que vous devez construire :

![Maquette Mes sites de suivi (capstone TP4 ↔ TP5)](src/main/resources/assets/maquette_sites.svg)

**Le rendu final** (le capstone, alimenté par la base SQLite seedée à chaque lancement ; ici une ligne est sélectionnée — « Supprimer » devient rouge actif — et le formulaire est rempli — « Ajouter » devient vert actif) :

<img alt="Résultat attendu - Exercice 7 : l'écran VigieChiro avec la TableView des sites persistés (1re ligne sélectionnée), le résumé, le formulaire d'ajout (bouton Ajouter vert) et le bouton Supprimer (rouge)" src=".github/assets/apercu-ex7-sites.png" width="560"/>

**Ce que vous allez découvrir** :
- L'**architecture en couches** complète : Vue → ViewModel → DAO → DataSource → JDBC → SQLite.
- L'**injection** du `SiteDao` par Guice (`@Inject`, module `@Provides`), exactement comme au TP4.
- Une [`ObservableList<Site>`](https://openjfx.io/javadoc/25/javafx.base/javafx/collections/ObservableList.html) chargée depuis la base et reliée à une [`TableView`](https://openjfx.io/javadoc/25/javafx.controls/javafx/scene/control/TableView.html) : ajouter/supprimer **persiste** ET rafraîchit l'écran.
- Deux heuristiques de Nielsen : confirmation (#5) et message d'erreur clair (#9).

> 📚 **Cours** : [CM4 #64](https://iutinfoaix-r202.github.io/cours/cm4-mvvm-persistance.html#64) (l'architecture complète), [CM4 #35-39](https://iutinfoaix-r202.github.io/cours/cm4-mvvm-persistance.html#35) (Guice et son intégration FXML) et [CM4 #13-29](https://iutinfoaix-r202.github.io/cours/cm4-mvvm-persistance.html#13) (MVVM). Heuristiques : [CM4 #67](https://iutinfoaix-r202.github.io/cours/cm4-mvvm-persistance.html#67) (#5) et [CM4 #70](https://iutinfoaix-r202.github.io/cours/cm4-mvvm-persistance.html#70) (#9).

**Le matériel fourni** dans le paquet `fr.univ_amu.iut.exercice7` :
- `PersistanceModule.java`, `SitesApp.java` : le câblage Guice + bootstrap, **fournis complets**.
- `SitesView.fxml` : la vue, **fournie complète**.
- `SitesController.java` : les actions `surAjouter` / `surSupprimer` (avec confirmation et message d'erreur) sont **fournies** ; `initialize()` est **à compléter** (`// TODO exercice 7`).
- `SitesViewModel.java` : le constructeur (chargement + résumé) et les commandes `ajouterCommand` / `supprimerCommand` sont **à compléter**.
- `SitesViewModelTest.java` : **4 tests JUnit** (la logique, sans fenêtre). `SitesControllerTest.java` : **6 tests TestFX** (le câblage de la vue).

### 📋 Mémo - l'architecture en couches du capstone

<img alt="Mémo - l'architecture en couches du capstone" src=".github/assets/memo-l-architecture-en-couches-du-capstone.svg"/>

**Travail à faire** : compléter le ViewModel puis le contrôleur. Huit gestes (le détail est dans les `// TODO`).

*Dans `SitesViewModel` :*
1. **Constructeur** : remplir `sites` avec `dao.findAll()` et lier `resume` au nombre de sites (format dans le test).
2. **`ajouterCommand`** : persister (`dao.insert`) puis ajouter à `sites`.
3. **`supprimerCommand`** : supprimer en base (`dao.delete`) puis retirer de `sites`.

*Dans `SitesController.initialize()` :*
4. **Colonnes** : une *cell value factory* par colonne (numéro, nom, protocole).
5. **`setItems`** : donner à la `TableView` les `sitesProperty()` du ViewModel.
6. **Résumé** : lier `labelResume.textProperty()` à `resumeProperty()`.
7. **Protocoles** : remplir `choiceProtocole` avec les deux protocoles.
8. **Bouton Supprimer** : le désactiver tant qu'aucune ligne n'est sélectionnée (binding sur la sélection).

**Progression des tests**, à activer un par un :

*Tests du ViewModel (JUnit, sans fenêtre) :*

| Test | Ce qui est vérifié | Geste |
|---|---|---|
| `au_demarrage_les_sites_sont_charges_depuis_la_base` | la liste est chargée depuis la base | geste 1 |
| `le_resume_reflete_le_nombre_de_sites` | le résumé dérive du nombre de sites | geste 1 |
| `ajouter_persiste_le_site_et_l_ajoute_a_la_liste` | `ajouterCommand` persiste **et** met à jour la liste | geste 2 |
| `supprimer_retire_le_site_de_la_liste_et_de_la_base` | `supprimerCommand` supprime des deux côtés | geste 3 |

*Tests du contrôleur (TestFX, avec fenêtre) :*

| Test | Ce qui est vérifié | Geste |
|---|---|---|
| `le_tableau_affiche_les_sites_persistes` | la `TableView` reçoit les items du ViewModel | geste 5 |
| `les_colonnes_affichent_les_donnees_du_site` *(nouveau)* | les colonnes lisent les champs du `Site` | geste 4 |
| `le_resume_affiche_le_nombre_de_sites` *(nouveau)* | `labelResume` est relié au ViewModel | geste 6 |
| `la_liste_des_protocoles_propose_les_deux_valeurs` *(nouveau)* | `choiceProtocole` est peuplée | geste 7 |
| `le_bouton_supprimer_est_desactive_sans_selection_puis_actif` *(nouveau)* | le bouton suit la sélection | geste 8 |
| `ajouter_un_site_via_le_formulaire_ajoute_une_ligne` | le formulaire ajoute une ligne (vue + commande) | gestes 5+7+2 |

> [!TIP]
> Lancez l'application : `./mvnw javafx:run`, puis le bouton du capstone dans le lanceur, via [noVNC](https://github.com/IUTInfoAix-R202/tp1#voir-votre-fenêtre-avec-vnc) (port 6080). Ajoutez un site, fermez, relancez : il est **toujours là**. C'est ça, la persistance.

---

## Exercices bonus

Les bonus sont **facultatifs** et ne comptent pas dans l'autograding. Ils prolongent la couche de persistance : les deux premiers la rendent plus élégante (8) ou plus robuste (9), les quatre suivants préparent directement des gestes de la **SAÉ**.

### Bonus 8 - Un requêteur générique (`RowMapper`)

Dans les exercices 3 à 6, vous avez réécrit à chaque fois la même boucle `while (rs.next())`. C'est de la duplication : la mécanique JDBC (ouvrir, exécuter, parcourir, fermer) ne change pas, seule la façon de **lire une ligne** varie d'un DAO à l'autre. Ce bonus isole cette variation dans une **interface fonctionnelle** et écrit la mécanique **une seule fois**. C'est exactement l'idée du `JdbcTemplate` de Spring.

- **Objectif** : un requêteur générique capable d'exécuter n'importe quel `SELECT`.
- **À compléter** : la méthode générique `query(String sql, RowMapper<T> mapper)` de `RequeteurSql` - exécuter la requête et, pour chaque ligne, ajouter `mapper.mapper(rs)` au résultat.
- **Fourni** : `RowMapper<T>` (l'interface fonctionnelle `T mapper(ResultSet)`), le constructeur de `RequeteurSql`. **2 tests** : un mapping vers `String` (les codes), un vers `Integer` (un `COUNT`) - la **même** classe sert les deux, preuve de la généricité.

> 📚 **Cours** : [CM4 #62](https://iutinfoaix-r202.github.io/cours/cm4-mvvm-persistance.html#62) (le patron DAO, dont ceci est l'aboutissement).

#### 📋 Mémo - le `RowMapper` est un lambda

Une fois `query` écrit, le « comment lire une ligne » se passe en **lambda** à l'appel - la mécanique, elle, ne se réécrit plus :

```java
RequeteurSql requeteur = new RequeteurSql(source);

List<String> codes = requeteur.query(
    "SELECT code FROM taxon ORDER BY code",
    rs -> rs.getString("code"));               // RowMapper<String>

List<Integer> compte = requeteur.query(
    "SELECT COUNT(*) AS n FROM site",
    rs -> rs.getInt("n"));                      // RowMapper<Integer>
```

### Bonus 9 - Un pool de connexions (HikariCP)

Réponse à la question « et les pools de connexions ? ». Ouvrir une connexion coûte cher ; une application interactive en garde donc un **pool** prêt à l'emploi (emprunter une connexion est instantané, la « fermer » la rend au pool). Le point clef, et toute la leçon de ce bonus : `HikariDataSource` **est** une `DataSource`. Le `SiteDao` de l'exercice 4, écrit contre l'interface, fonctionne avec le pool **sans changer une ligne**. C'est le bénéfice d'avoir programmé contre l'abstraction (exercice 2).

- **Objectif** : fournir une `DataSource` **poolée** plutôt qu'une connexion neuve à chaque appel.
- **À compléter** : `poolSurFichier(String)` - configurer un `HikariConfig` (URL JDBC, taille max, `PRAGMA foreign_keys = ON` à l'initialisation de chaque connexion) et en faire un `HikariDataSource`.
- **Fourni** : la structure de la classe. **2 tests** : le `SiteDao` de l'exercice 4 tourne inchangé sur le pool, et la source produite est bien un `HikariDataSource`.

> 📚 **Cours** : [CM4 #63](https://iutinfoaix-r202.github.io/cours/cm4-mvvm-persistance.html#63) (le *connection pool*).

#### 📋 Mémo - on change l'implémentation, pas le code qui l'utilise

<img alt="Mémo - on change l'implémentation, pas le code qui l'utilise" src=".github/assets/memo-on-change-l-implementation-pas-le-code-qui.svg"/>

### Bonus 10 - Importer des observations depuis un CSV

Le geste central du parcours **P2** de la SAÉ : Tadarida produit un fichier CSV d'observations, qu'il faut lire et insérer en base. Vous parsez un CSV simplifié **à la main** (sans bibliothèque), pour bien voir ce qu'est « lire un fichier tabulaire ».

- **Objectif** : transformer un CSV d'observations en lignes insérées.
- **À compléter** : `importer(long passageId, String contenuCsv)` - découper en lignes (`split("\\R")`), sauter l'en-tête et les lignes vides, découper chaque ligne sur `;`, convertir (`Double.parseDouble` / `Integer.parseInt`), insérer (`executeUpdate`), compter.
- **Fourni** : la structure et la requête `INSERT`. **1 test** : 3 lignes de données insérées, en-tête bien ignoré.

> 📚 **Cours** : [CM4 #58](https://iutinfoaix-r202.github.io/cours/cm4-mvvm-persistance.html#58) (`executeUpdate`).

#### 📋 Mémo - du CSV aux colonnes de `observation`

```
temps_debut;temps_fin;frequence_mediane;code_taxon;probabilite   <- en-tête (ligne 0, ignorée)
0.4;1.2;45000;Pippip;0.92                                        <- une observation
```

| Cellule | `col[...]` | Conversion | Colonne `observation` |
|---|---|---|---|
| `0.4` | `col[0]` | `Double.parseDouble` | `temps_debut` |
| `1.2` | `col[1]` | `Double.parseDouble` | `temps_fin` |
| `45000` | `col[2]` | `Integer.parseInt` | `frequence_mediane` |
| `Pippip` | `col[3]` | (chaîne) | `code_taxon` |
| `0.92` | `col[4]` | `Double.parseDouble` | `probabilite` |

### Bonus 11 - Insertion par lot (`executeBatch`)

La SAÉ importe des **milliers** d'observations par nuit. Les insérer une par une (un `executeUpdate` par ligne, comme à l'exercice 6) provoque un aller-retour à chaque appel : c'est lent. Le mode **batch** accumule les ordres puis les envoie **d'un coup**. C'est le prolongement direct de l'exercice 6 : même transaction, mais `addBatch()` / [`executeBatch()`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/Statement.html) au lieu d'`executeUpdate()` répétés.

- **Objectif** : insérer un lot d'observations efficacement, dans une transaction.
- **À compléter** : `importerLot(long passageId, List<ObservationAImporter> observations)` - `setAutoCommit(false)`, `addBatch()` pour chaque observation, `executeBatch()`, `commit()` ; `rollback()` dans le `catch`.
- **Fourni** : la requête `INSERT`, les utilitaires de fermeture/annulation. Réutilise le record `ObservationAImporter` de l'exercice 6. **1 test** : un lot de 3 observations est bien inséré.

> 📚 **Cours** : [CM4 #59](https://iutinfoaix-r202.github.io/cours/cm4-mvvm-persistance.html#59) (les transactions).

#### 📋 Mémo - accumuler puis envoyer en un seul coup

<img alt="Mémo - accumuler puis envoyer en un seul coup" src=".github/assets/memo-accumuler-puis-envoyer-en-un-seul-coup.svg"/>

### Bonus 12 - Le DAO en interface (+ fausse implémentation pour les tests)

Jusqu'ici `SiteDao` était une classe concrète. En faire une **interface** (`SitesDao`) permet d'avoir deux implémentations interchangeables : une vraie en JDBC pour la production, et une **fausse en mémoire** pour les tests. On peut alors tester un ViewModel **sans base de données**, exactement comme au TP4 (dépendre d'une interface, injecter l'implémentation). C'est l'architecture testable que la SAÉ devrait adopter.

- **Objectif** : rendre la couche de persistance **substituable** par injection.
- **À compléter** : dans `DaoModule.configure()`, **lier** l'interface `SitesDao` à son implémentation JDBC (`bind(...).to(...)`).
- **Fourni** : l'interface `SitesDao`, les deux implémentations (`SitesDaoJdbc`, `SitesDaoEnMemoire`), le reste du module. **2 tests** : le faux dépôt en mémoire fonctionne sans base, et Guice fournit bien l'implémentation JDBC en « production ».

> 📚 **Cours** : [CM4 #35-39](https://iutinfoaix-r202.github.io/cours/cm4-mvvm-persistance.html#35) (Guice, `bind`, modules de test).

#### 📋 Mémo - une interface, deux implémentations

<img alt="SitesDao : interface (findAll, insert) implementee par SitesDaoJdbc (production) et SitesDaoEnMemoire (test) ; SitesViewModel depend de l'interface" width="480" src=".github/assets/dao-sitesdao-interface.svg"/>

### Bonus 13 - Initialisation idempotente du schéma

Le capstone repart d'une base **jetable** à chaque lancement (un fichier temporaire neuf). Une vraie application - la SAÉ - garde au contraire **une base persistante** entre deux lancements : il faut donc créer le schéma **une seule fois**, au premier démarrage. Sinon, au second lancement, `CREATE TABLE` échouerait (table déjà existante) et le `seed` dupliquerait les données. Une opération qu'on peut relancer sans effet de bord est dite **idempotente**.

- **Objectif** : initialiser le schéma **seulement si** la base est vide.
- **À compléter** : `initialiserSiNecessaire(DataSource)` - n'appeler `BaseDeDonnees.initialiser` que si la table `taxon` n'existe pas encore.
- **Fourni** : `tableExiste(...)`, qui interroge le catalogue SQLite (`sqlite_master`). **2 tests** : deux initialisations successives ne lèvent pas d'erreur **et** ne dupliquent pas les données (4 taxons, pas 8).

> 📚 **Cours** : [CM4 #60](https://iutinfoaix-r202.github.io/cours/cm4-mvvm-persistance.html#60) (SQLite comme base locale **persistante** de l'application).

#### 📋 Mémo - n'initialiser qu'une fois

<img alt="Mémo - n'initialiser qu'une fois" src=".github/assets/memo-n-initialiser-qu-une-fois.svg"/>

---

*IUT d'Aix-Marseille - Département Informatique - 2026*
