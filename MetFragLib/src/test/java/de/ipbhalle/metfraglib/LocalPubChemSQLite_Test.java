package de.ipbhalle.metfraglib;

import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.SettingsChecker;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.process.CombinedMetFragProcess;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.apache.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.DoubleBuffer;

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
            bwriter.write("LocalPubChemDatabase = " + pubchemDBFilePath);
            bwriter.newLine();
            bwriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        this.finalParameterFilePath = tempFile.getAbsolutePath();
    }

    @Test
    public void test_query_by_monomass() {
        // TODO: Implement by comparison with the online pubchem db class.
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

        // Check that all 4 candidates could be retrieved (requires that they are all available in the
        // sandbox db!) CIDS used here: 1,3,9,245
        String[] cids = (String[]) settings.get(VariableNames.PRECURSOR_DATABASE_IDS_NAME);

        String[] inchis = new String[cids.length];
        inchis[0] = "InChI=1S/C9H17NO4/c1-7(11)14-8(5-9(12)13)6-10(2,3)4/h8H,5-6H2,1-4H3";
        inchis[1] = "InChI=1S/C7H8O4/c8-5-3-1-2-4(6(5)9)7(10)11/h1-3,5-6,8-9H,(H,10,11)";
        inchis[2] = "InChI=1S/C6H13O9P/c7-1-2(8)4(10)6(5(11)3(1)9)15-16(12,13)14/h1-11H,(H2,12,13,14)";
        inchis[3] = "InChI=1S/C14H25NO11/c1-4(18)15-7-9(20)12(6(3-17)24-13(7)23)26-14-11(22)10(21)8(19)5(2-16)25-14/h5-14,16-17,19-23H,2-3H2,1H3,(H,15,18)";

        Double[] monoisotopic_weights = new Double[cids.length];
        monoisotopic_weights[0] = 203.116;
        monoisotopic_weights[1] = 156.042;
        monoisotopic_weights[2] = 260.03;
        monoisotopic_weights[3] = 383.143;

        String[] inchikey1s = new String[cids.length];
        inchikey1s[0] = "RDHQFKQIGNGIED";
        inchikey1s[1] = "INCSWYKICIYAHB";
        inchikey1s[2] = "INAPMGSXUVUWAF";
        inchikey1s[3] = "KFEUJDWYNGMDBV";

        Double[] xlogp3s = new Double[cids.length];
        xlogp3s[0] = 0.4;
        xlogp3s[1] = -0.3;
        xlogp3s[2] = -4.8;
        xlogp3s[3] = -4.7;

        assertEquals(candidateList.getNumberElements(), cids.length);
        for (int i = 0; i < candidateList.getNumberElements(); i++) {
            assertEquals(cids[i], candidateList.getElement(i).getIdentifier());
            assertEquals(inchis[i], candidateList.getElement(i).getInChI());

            Double tmp = Double.parseDouble((String) candidateList.getElement(i).getProperty(
                    VariableNames.MONOISOTOPIC_MASS_NAME));
            assertEquals(monoisotopic_weights[i], tmp);

            assertEquals(inchikey1s[i], candidateList.getElement(i).getProperty(VariableNames.INCHI_KEY_1_NAME));

            tmp = Double.parseDouble((String) candidateList.getElement(i).getProperty(
                    VariableNames.PUBCHEM_XLOGP_NAME));
            assertEquals(xlogp3s[i], tmp);
        }
    }

}
