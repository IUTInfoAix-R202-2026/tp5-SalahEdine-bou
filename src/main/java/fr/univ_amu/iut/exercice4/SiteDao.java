package fr.univ_amu.iut.exercice4;

import fr.univ_amu.iut.jdbc.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

/**
 * DAO de l'entité {@link Site} : CRUD complet (Create, Read, Update, Delete) sur la table {@code
 * site}.
 *
 * <p>L'exercice 3 vous a fait écrire les <b>lectures</b>. Ici, le cœur du travail concerne les
 * <b>écritures</b> : {@code insert}, {@code update}, {@code delete}. Une écriture s'exécute avec
 * {@link PreparedStatement#executeUpdate()} (et non {@code executeQuery}), qui renvoie le nombre de
 * lignes affectées.
 *
 * <p>Les lectures ({@code findAll}, {@code getByNumeroCarre}) et le mapping sont fournis : ils
 * suivent le modèle de l'exercice 3.
 */
public class SiteDao {

  private final DataSource source;

  public SiteDao(DataSource source) {
    this.source = source;
  }

  // ---------- Lectures (fournies, sur le modèle de l'exercice 3) ----------

  public List<Site> findAll() {
    List<Site> sites = new ArrayList<>();
    String sql =
        "SELECT numero_carre, nom_convivial, protocole, commentaire, date_creation"
            + " FROM site ORDER BY numero_carre";
    try (Connection connexion = source.getConnection();
        PreparedStatement ps = connexion.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        sites.add(depuis(rs));
      }
    } catch (SQLException e) {
      throw new DataAccessException("Impossible de lire les sites", e);
    }
    return sites;
  }

  public Optional<Site> getByNumeroCarre(String numeroCarre) {
    String sql =
        "SELECT numero_carre, nom_convivial, protocole, commentaire, date_creation"
            + " FROM site WHERE numero_carre = ?";
    try (Connection connexion = source.getConnection();
        PreparedStatement ps = connexion.prepareStatement(sql)) {
      ps.setString(1, numeroCarre);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? Optional.of(depuis(rs)) : Optional.empty();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Impossible de lire le site " + numeroCarre, e);
    }
  }

  // ---------- Écritures (à compléter) ----------

  /** Insère un nouveau site. */
  public void insert(Site site) {
    String sql =
        "INSERT INTO site (numero_carre, nom_convivial, protocole, commentaire, date_creation)"
            + " VALUES (?, ?, ?, ?, ?)";

    // TODO exercice 4 : insérer le site.
    //
    // - ouvrir une connexion + préparer la requête ;
    // - lier les 5 paramètres dans l'ordre des colonnes (setString) ;
    // - exécuter avec executeUpdate() ;
    // - envelopper toute SQLException dans une DataAccessException.
    try (Connection connection = source.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql)) {

      ps.setString(1, site.numeroCarre());
      ps.setString(2, site.nomConvivial());
      ps.setString(3, site.protocole());
      ps.setString(4, site.commentaire());
      ps.setString(5, site.dateCreation());

      ps.executeUpdate();

    } catch (SQLException e) {
      throw new DataAccessException("Impossible d'", e);
    }
  }

  /** Met à jour les champs d'un site existant (identifié par son numéro de carré). */
  public void update(Site site) {
    String sql =
        "UPDATE site SET nom_convivial = ?, protocole = ?, commentaire = ?, date_creation = ?"
            + " WHERE numero_carre = ?";

    // TODO exercice 4 : mettre à jour le site (mêmes étapes, executeUpdate).
    // Attention à l'ordre des paramètres : le numero_carre est le DERNIER (clause
    // WHERE).

    try (Connection connection = source.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql)) {

      ps.setString(5, site.numeroCarre());
      ps.setString(1, site.nomConvivial());
      ps.setString(2, site.protocole());
      ps.setString(3, site.commentaire());
      ps.setString(4, site.dateCreation());

      ps.executeUpdate();
    } catch (SQLException e) {
      throw new DataAccessException("Impossible d'", e);
    }
  }

  /** Supprime le site identifié par son numéro de carré. */
  public void delete(String numeroCarre) {
    String sql = "DELETE FROM site WHERE numero_carre = ?";

    // TODO exercice 4 : supprimer le site (PreparedStatement + executeUpdate).
    try (Connection connection = source.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, numeroCarre);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new DataAccessException("Impossible supprimer le site " + numeroCarre, e);
    }
  }

  private static Site depuis(ResultSet rs) throws SQLException {
    return new Site(
        rs.getString("numero_carre"),
        rs.getString("nom_convivial"),
        rs.getString("protocole"),
        rs.getString("commentaire"),
        rs.getString("date_creation"));
  }
}
