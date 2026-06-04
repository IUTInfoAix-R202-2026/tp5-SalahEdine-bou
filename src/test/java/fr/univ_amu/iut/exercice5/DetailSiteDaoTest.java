package fr.univ_amu.iut.exercice5;

import static org.assertj.core.api.Assertions.assertThat;

import fr.univ_amu.iut.exercice2.BaseDeDonnees;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test de l'exercice 5 : lecture des relations (points d'un site) et jointure 3 tables (espèces
 * observées sur un site), sur la base du fil rouge.
 */
class DetailSiteDaoTest {

  @TempDir Path dossier;
  private DataSource source;
  private DetailSiteDao dao;

  @BeforeEach
  void preparer() {
    source = BaseDeDonnees.surFichier(dossier.resolve("test.db").toString());
    BaseDeDonnees.initialiser(source);
    dao = new DetailSiteDao(source);
  }

  // @Disabled("Retire cette annotation pour activer le test")
  @Test
  void la_jointure_remonte_les_especes_observees_sur_le_site() {
    assertThat(dao.findEspecesObserveesSurLeSite("640380"))
        .as("observation -> passage -> taxon, sans doublon, triées")
        .containsExactly("Noctule de Leisler", "Pipistrelle commune");
  }

  // @Disabled("Retire cette annotation pour activer le test")
  @Test
  void la_jointure_dedoublonne_les_especes() throws SQLException {
    // Une 2e observation du MEME taxon (Pippip) sur le passage 1 deja seede.
    try (Connection connexion = source.getConnection();
        Statement st = connexion.createStatement()) {
      st.execute(
          "INSERT INTO observation"
              + " (passage_id, temps_debut, temps_fin, frequence_mediane, code_taxon, probabilite)"
              + " VALUES (1, 5.0, 5.5, 45000, 'Pippip', 0.88)");
    }

    assertThat(dao.findEspecesObserveesSurLeSite("640380"))
        .as("avec DISTINCT, une espèce vue plusieurs fois n'apparaît qu'une fois")
        .containsExactly("Noctule de Leisler", "Pipistrelle commune");
  }

  // @Disabled("Retire cette annotation pour activer le test")
  @Test
  void un_site_sans_observation_renvoie_une_liste_vide() {
    assertThat(dao.findEspecesObserveesSurLeSite("000000")).isEmpty();
  }
}
