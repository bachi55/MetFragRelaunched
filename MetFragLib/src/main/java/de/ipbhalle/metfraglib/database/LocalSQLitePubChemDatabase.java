package de.ipbhalle.metfraglib.database;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Vector;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.candidate.TopDownPrecursorCandidate;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Class implementing the support for a local PubChem database stored using SQLite.
 *
 * This class adds to more column names:
 *
 * - InChIKey
 * - XLogP3
 *
 * Those might be specific to PubChem databases.
 */
public class LocalSQLitePubChemDatabase extends AbstractLocalDatabase {

    protected String INCHIKEY_COLUMN_NAME;
    protected String XLOGP3_COLUMN_NAME;
    protected String IUPAC_NAME_COLUMN_NAME;

    private String base_candidate_property_query;

	public LocalSQLitePubChemDatabase(Settings settings) {
		super(settings);

        // We use the pubchem related variables here
        this.DATABASE_NAME = (String) settings.get(VariableNames.LOCAL_PUBCHEM_DATABASE_NAME);
        this.TABLE_NAME	= (String) settings.get(VariableNames.LOCAL_PUBCHEM_DATABASE_COMPOUND_TABLE_NAME);
        this.MASS_COLUMN_NAME =	(String) settings.get(VariableNames.LOCAL_PUBCHEM_DATABASE_MASS_COLUMN_NAME);
        this.FORMULA_COLUMN_NAME = (String) settings.get(VariableNames.LOCAL_PUBCHEM_DATABASE_FORMULA_COLUMN_NAME);
        this.INCHI_COLUMN_NAME = (String) settings.get(VariableNames.LOCAL_PUBCHEM_DATABASE_INCHI_COLUMN_NAME);
        this.CID_COLUMN_NAME = (String) settings.get(VariableNames.LOCAL_PUBCHEM_DATABASE_CID_COLUMN_NAME);
        this.SMILES_COLUMN_NAME = (String) settings.get(VariableNames.LOCAL_PUBCHEM_DATABASE_SMILES_COLUMN_NAME);
        this.COMPOUND_NAME_COLUMN_NAME = (String) settings.get(VariableNames.LOCAL_DATABASE_COMPOUND_NAME_COLUMN_NAME);

		// Introduce new fields
        this.INCHIKEY_COLUMN_NAME = (String) settings.get(VariableNames.LOCAL_PUBCHEM_DATABASE_INCHIKEY_COLUMN_NAME);
        this.XLOGP3_COLUMN_NAME = (String) settings.get(VariableNames.LOCAL_PUBCHEM_DATABASE_XLOGP3_COLUMN_NAME);
        this.IUPAC_NAME_COLUMN_NAME = (String) settings.get(VariableNames.LOCAL_PUBCHEM_DATABASE_COMPOUND_NAME_COLUMN_NAME);

        // Prefix of the query used to get candidate properties from the table
        // Usage: prefix + CONDITION, e.g. prefix + where cid = 100;
        this.base_candidate_property_query = "SELECT "
                + CID_COLUMN_NAME + ","
                + INCHI_COLUMN_NAME + ","
                + FORMULA_COLUMN_NAME + ","
                + MASS_COLUMN_NAME  + ","
                + INCHIKEY_COLUMN_NAME + ","
                + SMILES_COLUMN_NAME + ","
                + IUPAC_NAME_COLUMN_NAME + ","
                + XLOGP3_COLUMN_NAME  + " from " + TABLE_NAME;
    }

	public Vector<String> getCandidateIdentifiers(double monoisotopicMass, double relativeMassDeviation) {
		double error = MathTools.calculateAbsoluteDeviation(monoisotopicMass, relativeMassDeviation);
		String query = "SELECT " + CID_COLUMN_NAME + " from " 
				+ TABLE_NAME + " where " + MASS_COLUMN_NAME 
				+ " between " + (monoisotopicMass - error)+" and "+(monoisotopicMass + error)+";";
		logger.trace(query);
		ResultSet rs = this.submitQuery(query);
		Vector<String> cids = new Vector<String>();
		if(rs == null) return cids;
		try {
			while(rs.next()) cids.add(rs.getString("cid"));
			rs.close();
			this.statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cids;
	}

	public Vector<String> getCandidateIdentifiers(String molecularFormula) {
		String query = "SELECT " + CID_COLUMN_NAME + " from " + TABLE_NAME 
				+ " where " + FORMULA_COLUMN_NAME + " = \"" + molecularFormula + "\";";
		logger.trace(query);
		ResultSet rs = this.submitQuery(query);
		Vector<String> cids = new Vector<String>();
		if(rs == null) return cids;
		try {
			while(rs.next()) cids.add(rs.getString(CID_COLUMN_NAME));
			rs.close();
			this.statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cids;
	}

	public Vector<String> getCandidateIdentifiers(Vector<String> identifiers) {
		if(identifiers.size() == 0) return new Vector<String>();
		String query = "SELECT " + CID_COLUMN_NAME + " from " + TABLE_NAME 
				+ " where " + CID_COLUMN_NAME + " =\"" + identifiers.get(0) + "\"";
		for(int i = 1; i < identifiers.size(); i++)
			query += "or " + CID_COLUMN_NAME + " =\"" + identifiers.get(i) + "\"";
		query += ";";
		logger.trace(query);
		ResultSet rs = this.submitQuery(query);
		Vector<String> cids = new Vector<String>();
		if(rs == null) return cids;
		try {
			while(rs.next()) cids.add(rs.getString(CID_COLUMN_NAME));
			rs.close();
			this.statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cids;
	}

	public ICandidate getCandidateByIdentifier(String identifier) {
		Vector<String> identifiers = new Vector<String>(1);
        identifiers.set(0, identifier);
		CandidateList candidates = this.getCandidateByIdentifier(identifiers);

		if(candidates.getNumberElements() == 0) {
		    return null;
        } else {
		    return candidates.getElement(0);
        }
	}

	public CandidateList getCandidateByIdentifier(Vector<String> identifiers) {
		if(identifiers.size() == 0) return new CandidateList();

        String query = base_candidate_property_query + " where " + CID_COLUMN_NAME + " in (\"" + identifiers.get(0) + "\"";
        for(int i = 1; i < identifiers.size(); i++)
            query += ",\"" + identifiers.get(i) + "\"";
        query += ");";
        
        logger.trace(query);

		ResultSet rs = this.submitQuery(query);
		if(rs == null) return new CandidateList();

		CandidateList candidates = new CandidateList();
		try {
			while(rs.next()) {
				ICandidate candidate = new TopDownPrecursorCandidate(
				        rs.getString(INCHI_COLUMN_NAME), rs.getString(CID_COLUMN_NAME));
                candidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, rs.getString(FORMULA_COLUMN_NAME));
                candidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, rs.getString(MASS_COLUMN_NAME));

                // Get inchikey parts from the inchi string. We do not need to store in the database
                String inchikey = rs.getString(INCHIKEY_COLUMN_NAME);
                if (inchikey != null) {
                    candidate.setProperty(VariableNames.INCHI_KEY_NAME, inchikey);

                    String[] inchikey_parts = inchikey.split("-");
                    candidate.setProperty(VariableNames.INCHI_KEY_1_NAME, inchikey_parts[0]);
                    candidate.setProperty(VariableNames.INCHI_KEY_2_NAME, inchikey_parts[1]);
                } else {
                    candidate.setProperty(VariableNames.INCHI_KEY_NAME, "NULL");
                    candidate.setProperty(VariableNames.INCHI_KEY_1_NAME, "NULL");
                    candidate.setProperty(VariableNames.INCHI_KEY_2_NAME, "NULL");
                }

				candidate.setProperty(VariableNames.PUBCHEM_XLOGP_NAME,
                        rs.getString(XLOGP3_COLUMN_NAME) != null ? rs.getString(XLOGP3_COLUMN_NAME) : "NULL");
				candidate.setProperty(VariableNames.SMILES_NAME,
                        rs.getString(SMILES_COLUMN_NAME) != null ? rs.getString(SMILES_COLUMN_NAME) : "NULL");
				candidate.setProperty(VariableNames.IUPAC_NAME_NAME,
                        rs.getString(IUPAC_NAME_COLUMN_NAME) != null ? rs.getString(IUPAC_NAME_COLUMN_NAME) : "NULL");
				
				candidates.addElement(candidate);
			}
			rs.close();
			this.statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return candidates;
	}
	
	/**
	 * Submit database query and retrieve results.
     *
	 * @param query, used to retrieve candidate information from the database
	 * @return ResultsSet
	 */
	private ResultSet submitQuery(String query) {
		ResultSet rs = null; 
		try {
			this.databaseConnection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_NAME,
                    this.db_user, this.db_password);
			this.statement = this.databaseConnection.createStatement();
			rs = this.statement.executeQuery(query);
		    SQLWarning warning = rs.getWarnings();
		    if(warning != null) logger.error("error code: " + warning.getErrorCode());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}

}
