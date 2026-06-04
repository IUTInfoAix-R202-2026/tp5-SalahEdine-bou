package fr.univ_amu.iut.exercice1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Exercice 1 : premier contact avec JDBC. Le jalon de cet exercice est simple mais essentiel :
 * <b>se connecter à une base et lire une table</b>.
 *
 * <p>On travaille sur une base SQLite <b>en mémoire</b> ({@code jdbc:sqlite::memory:}) : aucune
 * installation, aucun fichier, la base vit le temps de la connexion. On y crée une unique table
 * {@code taxon} (les espèces de chauves-souris du fil rouge VigieChiro), puis on la relit.
 *
 * <p>Tout accès JDBC suit les mêmes 5 étapes (les étapes 1 et 2 sont automatiques depuis JDBC 4.0)
 * :
 *
 * <ol>
 *   <li>charger le pilote (automatique) ;
 *   <li>ouvrir une {@link Connection} ;
 *   <li>créer une instruction ({@link Statement}) ;
 *   <li>exécuter et parcourir le {@link ResultSet} ;
 *   <li>libérer les ressources (le try-with-resources s'en charge).
 * </ol>
 */
public class ExempleJDBC {

  /** URL JDBC d'une base SQLite en mémoire (jetable, parfaite pour débuter et pour les tests). */
  public static final String URL_MEMOIRE = "jdbc:sqlite::memory:";

  public static void main(String[] args) throws SQLException {
    // Étape 2 : ouvrir la connexion (try-with-resources => fermeture automatique,
    // étape 5).
    try (Connection connexion = DriverManager.getConnection(URL_MEMOIRE)) {
      creerEtRemplirTable(connexion);

      System.out.println("Taxons présents dans la base :");
      for (String ligne : lireTaxons(connexion)) {
        System.out.println("  " + ligne);
      }
    }
  }

  /**
   * Prépare la base : crée la table {@code taxon} et y insère les espèces du fil rouge. Fourni : ce
   * n'est pas l'objet de l'exercice (on s'en servira autrement dès l'exercice 2).
   */
  static void creerEtRemplirTable(Connection connexion) throws SQLException {
    try (Statement st = connexion.createStatement()) {
      st.execute("CREATE TABLE taxon (code TEXT PRIMARY KEY, nom_vernaculaire TEXT NOT NULL)");
      st.execute("INSERT INTO taxon VALUES ('Pippip', 'Pipistrelle commune')");
      st.execute("INSERT INTO taxon VALUES ('Nyclei', 'Noctule de Leisler')");
      st.execute("INSERT INTO taxon VALUES ('Tadten', 'Molosse de Cestoni')");
      st.execute("INSERT INTO taxon VALUES ('Rhihip', 'Petit rhinolophe')");
    }
  }

  /**
   * Lit tous les taxons et renvoie, pour chacun, une ligne {@code "code - nom"}.
   *
   * <p>C'est le cœur de l'exercice : exécuter un {@code SELECT} et parcourir le {@link ResultSet}.
   */
  static List<String> lireTaxons(Connection connexion) throws SQLException {
    List<String> lignes = new ArrayList<>();

    // TODO exercice 1 : lire la table taxon.
    //

    // 1. Créer une instruction : connexion.createStatement() (dans un
    // try-with-resources).
    // 2. Exécuter le SELECT : st.executeQuery("SELECT code, nom_vernaculaire FROM
    // taxon").
    // 3. Parcourir le ResultSet avec while (rs.next()) et, pour chaque ligne,
    // ajouter à `lignes`
    // la chaîne : rs.getString("code") + " - " + rs.getString("nom_vernaculaire").

    try (Statement statement = connexion.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT code, nom_vernaculaire FROM taxon")) {
      while (resultSet.next()) {
        lignes.add(resultSet.getString("code") + " - " + resultSet.getString("nom_vernaculaire"));
      }
    }

    return lignes;
  }
}
