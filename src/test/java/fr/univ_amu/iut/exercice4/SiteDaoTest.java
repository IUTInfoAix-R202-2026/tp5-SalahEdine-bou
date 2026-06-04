package fr.univ_amu.iut.exercice4;

import static org.assertj.core.api.Assertions.assertThat;

import fr.univ_amu.iut.exercice2.BaseDeDonnees;
import java.nio.file.Path;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test de l'exercice 4 : CRUD complet sur une base SQLite temporaire. Chaque écriture est relue
 * pour vérifier qu'elle a bien été persistée.
 */
class SiteDaoTest {

  @TempDir Path dossier;
  private SiteDao dao;

  @BeforeEach
  void preparer() {
    DataSource source = BaseDeDonnees.surFichier(dossier.resolve("test.db").toString());
    BaseDeDonnees.initialiser(source);
    dao = new SiteDao(source);
  }

  // @Disabled("Retire cette annotation pour activer le test")
  @Test
  void inserer_ajoute_un_site_relisible() {
    Site nouveau = new Site("752204", "ZAC Nord", "PointFixeRecherche", null, "2026-05-01");

    dao.insert(nouveau);

    assertThat(dao.getByNumeroCarre("752204")).contains(nouveau);
    assertThat(dao.findAll()).extracting(Site::numeroCarre).contains("640380", "752204");
  }

  // @Disabled("Retire cette annotation pour activer le test")
  @Test
  void mettre_a_jour_modifie_les_champs_du_site() {
    dao.update(
        new Site("640380", "Étang rénové", "PointFixeRecherche", "Capteur déplacé", "2026-04-20"));

    Site relu = dao.getByNumeroCarre("640380").orElseThrow();
    assertThat(relu.nomConvivial()).isEqualTo("Étang rénové");
    assertThat(relu.protocole()).isEqualTo("PointFixeRecherche");
    assertThat(relu.commentaire()).isEqualTo("Capteur déplacé");
  }

  // @Disabled("Retire cette annotation pour activer le test")
  @Test
  void supprimer_retire_le_site() {
    dao.insert(new Site("123456", "À supprimer", "PointFixeStandard", null, "2026-05-02"));
    assertThat(dao.getByNumeroCarre("123456")).isPresent();

    dao.delete("123456");

    assertThat(dao.getByNumeroCarre("123456")).isEmpty();
  }

  // @Disabled("Retire cette annotation pour activer le test")
  @Test
  void mettre_a_jour_ne_modifie_que_le_site_vise() {
    dao.insert(
        new Site("333333", "Témoin", "PointFixeStandard", "ne doit pas bouger", "2026-05-03"));

    dao.update(
        new Site("640380", "Étang rénové", "PointFixeRecherche", "Capteur déplacé", "2026-04-20"));

    Site temoin = dao.getByNumeroCarre("333333").orElseThrow();
    assertThat(temoin.nomConvivial())
        .as("un UPDATE sans WHERE toucherait ce témoin")
        .isEqualTo("Témoin");
    assertThat(temoin.commentaire()).isEqualTo("ne doit pas bouger");
  }

  // @Disabled("Retire cette annotation pour activer le test")
  @Test
  void supprimer_ne_retire_que_le_site_vise() {
    dao.insert(new Site("111111", "Site A", "PointFixeStandard", null, "2026-05-03"));
    dao.insert(new Site("222222", "Site B", "PointFixeStandard", null, "2026-05-03"));

    dao.delete("111111");

    assertThat(dao.getByNumeroCarre("111111")).as("le site visé est supprimé").isEmpty();
    assertThat(dao.getByNumeroCarre("222222"))
        .as("un DELETE sans WHERE viderait la table")
        .isPresent();
    assertThat(dao.getByNumeroCarre("640380")).as("le site seedé doit survivre").isPresent();
  }
}
