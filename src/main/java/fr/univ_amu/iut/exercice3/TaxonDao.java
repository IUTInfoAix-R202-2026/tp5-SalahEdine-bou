package fr.univ_amu.iut.exercice3;

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
 * DAO (Data Access Object) de l'entité {@link Taxon} : encapsule l'accès aux données de la table
 * {@code taxon}. C'est le premier DAO du TP, en <b>lecture seule</b>.
 *
 * <p>Principe du pattern DAO : le code métier (ViewModel, services) ne voit jamais de SQL ; il
 * appelle des méthodes (<i>findAll</i>, <i>getByCode</i>) qui renvoient des objets du domaine. Le
 * DAO reçoit une {@link DataSource} : il ne connaît ni l'URL JDBC ni le SGBD.
 *
 * <p>On utilise systématiquement des {@link PreparedStatement} (avec des {@code ?}), jamais de
 * concaténation de chaînes : c'est la protection contre l'injection SQL.
 */
public class TaxonDao {

  private final DataSource source;

  public TaxonDao(DataSource source) {
    this.source = source;
  }

  /** Renvoie tous les taxons de la table, triés par code. */
  public List<Taxon> findAll() {
    List<Taxon> taxons = new ArrayList<>();
    String sql = "SELECT code, nom_latin, nom_vernaculaire FROM taxon ORDER BY code";

    // TODO exercice 3 : exécuter le SELECT et construire la liste des Taxon.
    //
    // - ouvrir une connexion (source.getConnection()) dans un try-with-resources ;
    // - préparer puis exécuter la requête (connexion.prepareStatement(sql),
    // ps.executeQuery()) ;
    // - pour chaque ligne, appeler depuis(rs) et l'ajouter à `taxons`.
    // - en cas de SQLException, lever une DataAccessException.
    try (Connection connection = source.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        taxons.add(depuis(rs));
      }
    } catch (SQLException e) {
      throw new DataAccessException("Impossible de lire les Taxons", e);
    }

    return taxons;
  }

  /** Cherche un taxon par son code ; renvoie {@link Optional#empty()} si absent. */
  public Optional<Taxon> getByCode(String code) {
    String sql = "SELECT code, nom_latin, nom_vernaculaire FROM taxon WHERE code = ?";
    Optional<Taxon> resultat = Optional.empty();

    // TODO exercice 3 : exécuter la requête paramétrée et affecter le taxon trouvé
    // à `resultat`.
    //
    // - préparer la requête, puis lier le paramètre `?` au code (méthode setString)
    // ;
    // - exécuter ; si le ResultSet contient une ligne, construire le Taxon avec
    // depuis(rs)
    // et l'envelopper dans un Optional ; sinon, laisser `resultat` vide.

    return resultat;
  }

  /** Construit un {@link Taxon} à partir de la ligne courante du {@link ResultSet}. */
  private static Taxon depuis(ResultSet rs) throws SQLException {
    return new Taxon(
        rs.getString("code"), rs.getString("nom_latin"), rs.getString("nom_vernaculaire"));
  }
}
