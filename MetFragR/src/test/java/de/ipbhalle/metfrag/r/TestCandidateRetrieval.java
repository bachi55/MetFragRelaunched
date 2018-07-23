package de.ipbhalle.metfrag.r;

import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.SettingsChecker;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.io.File;

public class TestCandidateRetrieval {
    private Logger logger;
    private String paramFilePath_by_identifier;
    private String paramFilePath_by_identifier_with_filter;
    private String[] cids = {"9", "49533", "49560", "49558", "49719", "3"};
    private String[] cids_with_filter = {"9", "49533", "49719", "3"};

    private MetFragGlobalSettings load_settings(String paramFilePath) {
        File parameterFile = new File(paramFilePath);

        MetFragGlobalSettings settings = null;
        try {
            settings = MetFragGlobalSettings.readSettings(parameterFile, logger);
        }
        catch(Exception e) {
            logger.error("Error reading parameter file " + parameterFile);
            System.exit(1);
        }

        SettingsChecker settingsChecker = new SettingsChecker();
        if(!settingsChecker.check(settings,
                false,
                true,
                false,
                true,
                false,
                false)) return null;

        return(settings);
    }

    @Before
    public void setUp() {
        this.logger = Logger.getLogger(this.getClass());
        this.paramFilePath_by_identifier = ClassLoader.getSystemResource(
                "example_settings_run_candidate_retrieval.txt").getPath();
        this.paramFilePath_by_identifier_with_filter = ClassLoader.getSystemResource(
                "example_settings_run_candidate_retrieval_prefilters.txt").getPath();
    }

    @Test
    public void test_candidate_retrieval_without_filters() {
        MetFragGlobalSettings settings = load_settings(this.paramFilePath_by_identifier);

        CandidateList candidates = MetfRag.runCandidateRetrieval(settings);

        assertEquals(cids.length, candidates.getNumberElements());

        for (int i = 0; i < candidates.getNumberElements(); i++) {
            assertEquals(this.cids[i], candidates.getElement(i).getIdentifier());
        }
    }

    @Test
    public void test_candidate_retrieval_with_filters() {
        MetFragGlobalSettings settings = load_settings(this.paramFilePath_by_identifier_with_filter);

        CandidateList candidates = MetfRag.runCandidateRetrieval(settings);

        assertEquals(cids_with_filter.length, candidates.getNumberElements());

        for (int i = 0; i < candidates.getNumberElements(); i++) {
            assertEquals(this.cids_with_filter[i], candidates.getElement(i).getIdentifier());
        }
    }
}
