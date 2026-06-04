package fr.univ_amu.iut.exercice6;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.univ_amu.iut.exercice2.BaseDeDonnees;
import fr.univ_amu.iut.jdbc.DataAccessException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test de l'exercice 6 : on vérifie d'abord qu'un import valide est bien persisté, puis qu'un
 * import contenant une observation invalide (taxon inexistant) est <b>entièrement annulé</b>
 * (rollback).
 */
class ImportPassageServiceTest {

  @TempDir Path dossier;
  private DataSource source;
  private ImportPassageService service;

  @BeforeEach
  void preparer() {
    source = BaseDeDonnees.surFichier(dossier.resolve("test.db").toString());
    BaseDeDonnees.initialiser(source);
    service = new ImportPassageService(source);
  }

  @Disabled("Retire cette annotation pour activer le test")
  @Test
  void un_import_valide_persiste_le_passage() {
    int avant = service.nombrePassages();

    long id =
        service.importer(
            "640380",
            "Z1",
            3,
            2026,
            List.of(
                new ObservationAImporter(0.5, 1.0, 40000, "Pippip", 0.9),
                new ObservationAImporter(3.0, 3.6, 25000, "Tadten", 0.6)));

    assertThat(id).isPositive();
    assertThat(service.nombrePassages()).isEqualTo(avant + 1);
  }

  @Disabled("Retire cette annotation pour activer le test")
  @Test
  void un_import_valide_persiste_les_observations() throws SQLException {
    long id =
        service.importer(
            "640380",
            "Z1",
            5,
            2026,
            List.of(
                new ObservationAImporter(0.5, 1.0, 40000, "Pippip", 0.9),
                new ObservationAImporter(3.0, 3.6, 25000, "Tadten", 0.6)));

    try (Connection connexion = source.getConnection();
        PreparedStatement ps =
            connexion.prepareStatement("SELECT COUNT(*) FROM observation WHERE passage_id = ?")) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        assertThat(rs.getInt(1))
            .as("les 2 observations du passage doivent être persistées")
            .isEqualTo(2);
      }
    }
  }

  @Disabled("Retire cette annotation pour activer le test")
  @Test
  void un_import_avec_un_taxon_inexistant_est_entierement_annule() {
    int avant = service.nombrePassages();

    assertThatThrownBy(
            () ->
                service.importer(
                    "640380",
                    "Z1",
                    4,
                    2026,
                    List.of(
                        new ObservationAImporter(0.5, 1.0, 40000, "Pippip", 0.9),
                        new ObservationAImporter(2.0, 2.5, 30000, "XXXXXX", 0.5))))
        .isInstanceOf(DataAccessException.class);

    assertThat(service.nombrePassages())
        .as("le rollback doit avoir annulé l'insertion du passage")
        .isEqualTo(avant);
  }
}
