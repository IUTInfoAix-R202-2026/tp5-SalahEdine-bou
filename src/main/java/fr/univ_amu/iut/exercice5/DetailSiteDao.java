package fr.univ_amu.iut.exercice5;

import fr.univ_amu.iut.jdbc.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/**
 * DAO de lecture des relations autour d'un site (exercice 5).
 *
 * <p>Les données réelles sont reliées : un {@code site} contient des {@code point_ecoute}, qui
 * portent des {@code passage}, qui produisent des {@code observation}, chacune classée par un
 * {@code taxon}. Lire ces relations en SQL se fait avec des <b>jointures</b> (clause {@code JOIN
 * ... ON}).
 *
 * <p>{@link #findPointsDuSite(String)} est fourni (lecture simple, sur le modèle des exercices
 * 3-4). Votre travail porte sur {@link #findEspecesObserveesSurLeSite(String)}, qui traverse
 * <b>trois</b> tables.
 */
public class DetailSiteDao {

  private final DataSource source;

  public DetailSiteDao(DataSource source) {
    this.source = source;
  }

  /** Points d'écoute d'un site (fourni). */
  public List<PointEcoute> findPointsDuSite(String numeroCarre) {
    List<PointEcoute> points = new ArrayList<>();
    String sql =
        "SELECT numero_carre, code, descriptif FROM point_ecoute WHERE numero_carre = ? ORDER BY code";
    try (Connection connexion = source.getConnection();
        PreparedStatement ps = connexion.prepareStatement(sql)) {
      ps.setString(1, numeroCarre);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          points.add(
              new PointEcoute(
                  rs.getString("numero_carre"), rs.getString("code"), rs.getString("descriptif")));
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Impossible de lire les points du site " + numeroCarre, e);
    }
    return points;
  }

  /**
   * Noms vernaculaires (distincts, triés) des espèces observées sur un site, en remontant la chaîne
   * observation -> passage -> taxon.
   */
  public List<String> findEspecesObserveesSurLeSite(String numeroCarre) {
    List<String> especes = new ArrayList<>();
    String sql =
        "SELECT DISTINCT t.nom_vernaculaire"
            + " FROM observation"
            + " o JOIN passage p ON o.passage_id = p.id JOIN taxon"
            + " t ON o.code_taxon = t.code "
            + " WHERE p.numero_carre = ?"
            + " ORDER BY t.nom_vernaculaire";
    try (Connection connection = source.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, numeroCarre);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          especes.add(rs.getString("nom_vernaculaire"));
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Impossible lire les espèces " + numeroCarre, e);
    }

    // TODO exercice 5 : écrire la jointure entre observation, passage et taxon.
    //
    // Objectif : pour un site donné, lister les noms vernaculaires des espèces
    // détectées,
    // sans doublon, triés.
    //
    // SELECT DISTINCT t.nom_vernaculaire
    // FROM observation o
    // JOIN passage p ON o.passage_id = p.id
    // JOIN taxon t ON o.code_taxon = t.code
    // WHERE p.numero_carre = ?
    // ORDER BY t.nom_vernaculaire
    //
    // Préparer la requête, positionner le paramètre, parcourir le ResultSet et
    // ajouter chaque
    // nom à `especes`. Envelopper toute SQLException dans une DataAccessException.

    return especes;
  }
}
