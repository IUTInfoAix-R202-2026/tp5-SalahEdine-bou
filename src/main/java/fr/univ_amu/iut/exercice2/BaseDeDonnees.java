package fr.univ_amu.iut.exercice2;

import fr.univ_amu.iut.jdbc.DataAccessException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

/**
 * Exercice 2 : fournir une {@link DataSource} SQLite et initialiser le schéma.
 *
 * <p>À l'exercice 1, on ouvrait une {@link Connection} « à la main » avec le {@code DriverManager}.
 * En pratique, on travaille plutôt avec une {@link DataSource} : un objet qui sait fournir des
 * connexions. C'est l'abstraction que toutes les couches au-dessus (DAO, services) réclameront, et
 * qu'on pourra injecter (cf. capstone). Les DAO des exercices suivants reçoivent une {@code
 * DataSource} et ne connaissent jamais l'URL JDBC.
 *
 * <p>Cette classe expose aussi {@link #initialiser(DataSource)} qui crée les tables ({@code
 * schema.sql}) et insère les données du fil rouge ({@code seed.sql}). Le chargement des fichiers
 * SQL est fourni : ce n'est pas l'objet du TP.
 */
public class BaseDeDonnees {

  private BaseDeDonnees() {}

  /**
   * Crée une {@link DataSource} SQLite pointant vers le fichier donné, avec l'intégrité
   * référentielle (clés étrangères) activée.
   *
   * @param chemin chemin du fichier SQLite (ex : {@code "chauves_souris.db"})
   */
  public static DataSource surFichier(String chemin) {
    DataSource source = null;

    // TODO exercice 2 : créer et configurer la DataSource SQLite, et l'affecter à
    // `source`.
    //
    // 1. SQLiteConfig config = new SQLiteConfig();
    // config.enforceForeignKeys(true); // SQLite n'applique les FK que si on le
    // demande
    // 2. SQLiteDataSource sqlite = new SQLiteDataSource(config);
    // sqlite.setUrl("jdbc:sqlite:" + chemin);
    // 3. source = sqlite;
    SQLiteConfig config = new SQLiteConfig();
    config.enforceForeignKeys(true);
    SQLiteDataSource sqlite = new SQLiteDataSource(config);
    sqlite.setUrl("jdbc:sqlite:" + chemin);
    source = sqlite;

    return source;
  }

  /** Crée les tables (schema.sql) puis insère les données du fil rouge (seed.sql). Fourni. */
  public static void initialiser(DataSource source) {
    executerScript(source, "/db/schema.sql");
    executerScript(source, "/db/seed.sql");
  }

  private static void executerScript(DataSource source, String ressource) {
    String sql = lireRessource(ressource);
    try (Connection connexion = source.getConnection();
        Statement st = connexion.createStatement()) {
      for (String instruction : decouperInstructions(sql)) {
        st.execute(instruction);
      }
    } catch (SQLException e) {
      throw new DataAccessException("Échec de l'exécution du script " + ressource, e);
    }
  }

  private static String lireRessource(String chemin) {
    try (InputStream in = BaseDeDonnees.class.getResourceAsStream(chemin)) {
      if (in == null) {
        throw new IllegalStateException("Ressource introuvable : " + chemin);
      }
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Lecture impossible : " + chemin, e);
    }
  }

  /** Retire les commentaires en ligne et découpe le script en instructions sur les ';'. */
  private static String[] decouperInstructions(String sql) {
    StringBuilder sansCommentaires = new StringBuilder();
    for (String ligne : sql.split("\n")) {
      if (!ligne.strip().startsWith("--")) {
        sansCommentaires.append(ligne).append('\n');
      }
    }
    String[] brutes = sansCommentaires.toString().split(";");
    return java.util.Arrays.stream(brutes)
        .map(String::strip)
        .filter(s -> !s.isEmpty())
        .toArray(String[]::new);
  }
}
