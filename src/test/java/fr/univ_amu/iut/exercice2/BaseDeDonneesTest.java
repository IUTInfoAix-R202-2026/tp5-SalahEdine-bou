package fr.univ_amu.iut.exercice2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test de l'exercice 2 : on crée une base SQLite dans un fichier temporaire (nettoyé
 * automatiquement par {@code @TempDir}), on l'initialise, puis on vérifie qu'elle contient bien les
 * données du fil rouge.
 */
class BaseDeDonneesTest {

  @TempDir Path dossier;

  private DataSource baseInitialisee() {
    DataSource source = BaseDeDonnees.surFichier(dossier.resolve("test.db").toString());
    BaseDeDonnees.initialiser(source);
    return source;
  }

  // @Disabled("Retire cette annotation pour activer le test")
  @Test
  void la_base_initialisee_contient_les_quatre_taxons() throws SQLException {
    DataSource source = baseInitialisee();

    try (Connection connexion = source.getConnection();
        Statement st = connexion.createStatement();
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM taxon")) {
      rs.next();
      assertThat(rs.getInt(1)).isEqualTo(4);
    }
  }

  // @Disabled("Retire cette annotation pour activer le test")
  @Test
  void le_site_du_fil_rouge_est_present() throws SQLException {
    DataSource source = baseInitialisee();

    try (Connection connexion = source.getConnection();
        Statement st = connexion.createStatement();
        ResultSet rs =
            st.executeQuery("SELECT nom_convivial FROM site WHERE numero_carre = '640380'")) {
      rs.next();
      assertThat(rs.getString(1)).isEqualTo("Étang de la Tuilière");
    }
  }

  // @Disabled("Retire cette annotation pour activer le test")
  @Test
  void les_cles_etrangeres_sont_actives() throws SQLException {
    DataSource source = baseInitialisee();

    try (Connection connexion = source.getConnection();
        Statement st = connexion.createStatement()) {
      // observation.code_taxon est une clé étrangère vers taxon(code).
      // 'ZZZZZZ' n'existe pas : l'insertion doit être refusée SI les clés étrangères
      // sont activées.
      assertThatThrownBy(
              () ->
                  st.execute(
                      "INSERT INTO observation"
                          + " (passage_id, temps_debut, temps_fin, frequence_mediane, code_taxon,"
                          + " probabilite) VALUES (1, 0.0, 1.0, 40000, 'ZZZZZZ', 0.5)"))
          .as("avec enforceForeignKeys(true), un taxon inexistant doit être rejeté")
          .isInstanceOf(SQLException.class);
    }
  }
}
