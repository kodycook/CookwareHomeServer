package com.cookware.home.MediaManagerServer;

import com.cookware.home.MediaManagerServer.Tools.CsvTools;
import junit.framework.TestCase;

import java.io.File;

/**
 * Created by Kody on 17/10/2017.
 */
public class CsvToolsTest extends TestCase {
    public void testCreateFile() throws Exception {
        final String csvFile = "C:/Users/maste/Software/MediaManagerServer/test.csv";
        final File file = new File(csvFile);

        assert(!file.exists());
        new CsvTools().createFile(csvFile);
        assert(file.exists());

        file.delete();
        assert(!file.exists());
    }

    public void testWriteAndReadWithCsv() throws Exception {
        final String csvFile = "C:/Users/maste/Software/MediaManagerServer/test.csv";
        final String[] row1 = {"R1C1","R1C2","R1C3"};
        final String[] row2 = {"R2C1","R2C2","R2C3"};
        final String[] row3 = {"R3C1","R3C2","R3C3"};
        final String[][] table = {row1, row2, row3};
        final File file = new File(csvFile);
        final CsvTools csvTools = new CsvTools();

        csvTools.createFile(csvFile);
        csvTools.writeStringArrayToCsv(csvFile, table);

        final String[][] result = csvTools.getStringArrayFromCsv(csvFile);

        file.delete();
    }
}