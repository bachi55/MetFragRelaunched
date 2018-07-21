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

    private String base_candidate_property_query;

	public LocalSQLitePubChemDatabase(Settings settings) {
		super(settings);

        this.INCHIKEY_COLUMN_NAME = (String) settings.get(VariableNames.LOCAL_DATABASE_INCHIKEY_COLUMN_NAME);
        this.XLOGP3_COLUMN_NAME = (String) settings.get(VariableNames.LOCAL_DATABASE_XLOGP3_COLUMN_NAME);

        // Prefix of the query used to get canidate properties from the table
        // Usage: prefix + CONDITION, e.g. prefix + where cid = 100;
        this.base_candidate_property_query = "SELECT "
                + INCHI_COLUMN_NAME + ","
                + INCHIKEY1_COLUMN_NAME + ","
                + INCHIKEY2_COLUMN_NAME + ","
                + FORMULA_COLUMN_NAME + ","
                + MASS_COLUMN_NAME  + ","
                + INCHIKEY_COLUMN_NAME + ","
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
		String query = base_candidate_property_query + " where " + CID_COLUMN_NAME + " =\"" + identifier + "\";";

		logger.trace(query);

		ResultSet rs = this.submitQuery(query);
		if(rs == null) return null;

		Vector<String> inChIKeys1 = new Vector<String>();
		Vector<String> inChIKeys2 = new Vector<String>();
		Vector<String> formulas = new Vector<String>();
		Vector<String> inchis = new Vector<String>();
		Vector<String> masses = new Vector<String>();
		Vector<String> inchikeys = new Vector<String>();
		Vector<String> xlogp3s = new Vector<String>();
		
		try {
			while(rs.next()) {
				inchis.add(rs.getString(INCHI_COLUMN_NAME));
				inChIKeys1.add(rs.getString(INCHIKEY1_COLUMN_NAME));
				inChIKeys2.add(rs.getString(INCHIKEY2_COLUMN_NAME));
				masses.add(rs.getString(MASS_COLUMN_NAME));
				formulas.add(rs.getString(FORMULA_COLUMN_NAME));
				inchikeys.add(rs.getString(INCHIKEY_COLUMN_NAME));
				xlogp3s.add(rs.getString(XLOGP3_COLUMN_NAME));
			}
			rs.close();
			this.statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		ICandidate candidate = new TopDownPrecursorCandidate(inchis.get(0), identifier);

		candidate.setProperty(VariableNames.INCHI_KEY_1_NAME, inChIKeys1.get(0));
		candidate.setProperty(VariableNames.INCHI_KEY_2_NAME, inChIKeys2.get(0));
		candidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, formulas.get(0));
		candidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, masses.get(0));
		candidate.setProperty(VariableNames.INCHI_KEY_NAME, inchikeys.get(0));
		candidate.setProperty(VariableNames.PUBCHEM_XLOGP_NAME, xlogp3s.get(0));
		
		return candidate;
	}

	public CandidateList getCandidateByIdentifier(Vector<String> identifiers) {
		if(identifiers.size() == 0) return new CandidateList();

		String query = base_candidate_property_query + " where " + CID_COLUMN_NAME + " =\"" + identifiers.get(0) + "\"";
		for(String cid : identifiers)
			query += " or " + CID_COLUMN_NAME + " = \"" + cid + "\"";
		query += ";";

		logger.trace(query);

		ResultSet rs = this.submitQuery(query);
		if(rs == null) return new CandidateList();

		CandidateList candidates = new CandidateList();
		try {
			while(rs.next()) {
				String inchi = rs.getString(INCHI_COLUMN_NAME);
				ICandidate candidate = new TopDownPrecursorCandidate(inchi, rs.getString(CID_COLUMN_NAME));
				
				candidate.setProperty(VariableNames.INCHI_KEY_1_NAME, rs.getString(INCHIKEY1_COLUMN_NAME));
				candidate.setProperty(VariableNames.INCHI_KEY_2_NAME, rs.getString(INCHIKEY2_COLUMN_NAME));
				candidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, rs.getString(FORMULA_COLUMN_NAME));
				candidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, rs.getString(MASS_COLUMN_NAME));
				candidate.setProperty(VariableNames.INCHI_KEY_NAME, rs.getString(INCHIKEY_COLUMN_NAME));
				candidate.setProperty(VariableNames.PUBCHEM_XLOGP_NAME, rs.getString(XLOGP3_COLUMN_NAME));
				
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
	 * 
	 * @param query
	 * @return
	 */
	private ResultSet submitQuery(String query) {
		ResultSet rs = null; 
		try {
			this.databaseConnection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_NAME, this.db_user, this.db_password);
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
