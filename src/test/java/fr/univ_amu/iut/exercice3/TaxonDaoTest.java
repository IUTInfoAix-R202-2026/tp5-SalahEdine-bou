package fr.univ_amu.iut.exercice3;

import static org.assertj.core.api.Assertions.assertThat;

import fr.univ_amu.iut.exercice2.BaseDeDonnees;
import java.nio.file.Path;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test de l'exercice 3 : le DAO de lecture se vérifie sur une base SQLite temporaire peuplée avec
 * les données du fil rouge. Aucune interface graphique.
 */
class TaxonDaoTest {

  @TempDir Path dossier;
  private TaxonDao dao;

  @BeforeEach
  void preparer() {
    DataSource source = BaseDeDonnees.surFichier(dossier.resolve("test.db").toString());
    BaseDeDonnees.initialiser(source);
    dao = new TaxonDao(source);
  }

  // @Disabled("Retire cette annotation pour activer le test")
  @Test
  void trouver_tous_renvoie_les_quatre_taxons_tries_par_code() {
    assertThat(dao.findAll())
        .extracting(Taxon::code)
        .containsExactly("Nyclei", "Pippip", "Rhihip", "Tadten");
  }

  // @Disabled("Retire cette annotation pour activer le test")
  @Test
  void trouver_par_code_renvoie_le_taxon_attendu() {
    Optional<Taxon> taxon = dao.getByCode("Pippip");

    assertThat(taxon).isPresent();
    assertThat(taxon.get().nomVernaculaire()).isEqualTo("Pipistrelle commune");
    assertThat(taxon.get().nomLatin()).isEqualTo("Pipistrellus pipistrellus");
  }

  // @Disabled("Retire cette annotation pour activer le test")
  @Test
  void trouver_par_code_inconnu_renvoie_vide() {
    assertThat(dao.getByCode("Zzzzzz")).isEmpty();
  }
}
