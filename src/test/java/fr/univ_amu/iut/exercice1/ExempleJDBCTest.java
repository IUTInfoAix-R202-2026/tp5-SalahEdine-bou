package fr.univ_amu.iut.exercice1;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Test de l'exercice 1 : on vérifie la lecture de la table {@code taxon} sur une base SQLite en
 * mémoire. Aucune interface graphique, aucun fichier : la base est créée puis lue dans la même
 * connexion.
 */
class ExempleJDBCTest {

  // @Disabled("Retire cette annotation pour activer le test")
  @Test
  void la_lecture_renvoie_les_quatre_taxons_du_fil_rouge() throws SQLException {
    try (Connection connexion = DriverManager.getConnection(ExempleJDBC.URL_MEMOIRE)) {
      ExempleJDBC.creerEtRemplirTable(connexion);

      List<String> lignes = ExempleJDBC.lireTaxons(connexion);

      assertThat(lignes)
          .as("la table contient les 4 taxons du fil rouge")
          .containsExactlyInAnyOrder(
              "Pippip - Pipistrelle commune",
              "Nyclei - Noctule de Leisler",
              "Tadten - Molosse de Cestoni",
              "Rhihip - Petit rhinolophe");
    }
  }

  // @Disabled("Retire cette annotation pour activer le test")
  @Test
  void chaque_ligne_a_le_format_code_tiret_nom() throws SQLException {
    try (Connection connexion = DriverManager.getConnection(ExempleJDBC.URL_MEMOIRE)) {
      ExempleJDBC.creerEtRemplirTable(connexion);

      List<String> lignes = ExempleJDBC.lireTaxons(connexion);

      assertThat(lignes).allSatisfy(ligne -> assertThat(ligne).contains(" - "));
    }
  }
}
