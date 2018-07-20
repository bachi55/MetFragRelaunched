package de.ipbhalle.metfraglib.database;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;

import de.ipbhalle.metfraglib.settings.Settings;

public class LocalSQLiteDatabase extends LocalMySQLDatabase {

	public LocalSQLiteDatabase(Settings settings) {
		super(settings);
	}

	/**
	 *
	 * @param query
	 * @return
	 */
	@Override
	protected ResultSet submitQuery(String query) {
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
