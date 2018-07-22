package de.ipbhalle.metfraglib;

import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.SettingsChecker;
import de.ipbhalle.metfraglib.process.CombinedMetFragProcess;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.apache.log4j.Logger;
import java.io.File;
import java.io.IOException;

public class LocalPubChemSQLite_Test {
    private Logger logger;
    private String finalParameterFilePath;

    @Before
    public void setUp() {
        this.logger = Logger.getLogger(LocalPubChemSQLite_Test.class);
        java.io.File parameterFilePath = new java.io.File(ClassLoader.getSystemResource("local_sqlite_example_pubchemcid.txt").getFile());
        String peakListFilePath = ClassLoader.getSystemResource("peaklist_file_example_1.txt").getFile();
        String pubchemDBFilePath = ClassLoader.getSystemResource("pubchem.db").getFile();
        /*
         * read file parameters and add additional ones into a new temporary parameter file
         * - peaklist file location
         * - temp folder as result storage
         */
        String tempDir = System.getProperty("java.io.tmpdir");
        java.io.File tempFile = null;
        try {
            tempFile = java.io.File.createTempFile("temp-file-name", ".tmp");
            tempFile.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(tempFile == null) {
            throw new NullPointerException();
        }
        java.io.BufferedWriter bwriter;
        try {
            bwriter = new java.io.BufferedWriter(new java.io.FileWriter(tempFile));
            java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.FileReader(parameterFilePath));
            String line = "";
            while((line = breader.readLine()) != null) {
                line = line.trim();
                bwriter.write(line);
                bwriter.newLine();
            }
            breader.close();
            /*
             * add results store path
             */
            bwriter.write("ResultsPath = " + tempDir);
            bwriter.newLine();
            /*
             * add peaklist path
             */
            bwriter.write("PeakListPath = " + peakListFilePath);
            bwriter.newLine();

            /*
             * add pubchem database path
             */
            bwriter.write("LocalDatabase = " + pubchemDBFilePath);
            bwriter.newLine();
            bwriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        this.finalParameterFilePath = tempFile.getAbsolutePath();
    }

    @Test
    public void test_query_by_identifier() {
        File parameterFile = new File(this.finalParameterFilePath);

        MetFragGlobalSettings settings = null;
        try {
            settings = MetFragGlobalSettings.readSettings(parameterFile, logger);
        }
        catch(Exception e) {
            logger.error("Error reading parameter file " + parameterFile);
            System.exit(1);
        }

        SettingsChecker settingsChecker = new SettingsChecker();
        if(!settingsChecker.check(settings)) return;
        //init the MetFrag process
        CombinedMetFragProcess mp = new CombinedMetFragProcess(settings);

        //retrieve candidates from database
        try {
            boolean candidatesRetrieved = mp.retrieveCompounds();
            if(!candidatesRetrieved) throw new Exception();
        } catch (Exception e1) {
            e1.printStackTrace();
            logger.error("Error when retrieving compounds.");
            System.exit(2);
        }

        //fetch the scored candidate list
        CandidateList candidateList = mp.getCandidateList();

        assertEquals(candidateList.getNumberElements(), 3);
        // System.out.println(candidateList.getElement(0).getPropertyNames().toString());
    }

}
