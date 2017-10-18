package com.cookware.home.server.MediaManager;

import junit.framework.TestCase;

import java.io.File;

/**
 * Created by Kody on 17/10/2017.
 */
public class CsvManagerTest extends TestCase {
    public void testCreateFile() throws Exception {
        final String csvFile = "C:\\Users\\maste\\IdeaProjects\\CookwareHomeServer\\test.csv";
        final File file = new File(csvFile);

        assert(!file.exists());
        new CsvManager().createFile(csvFile);
        assert(file.exists());

        file.delete();
        assert(!file.exists());
    }

    public void testWriteAndReadWithCsv() throws Exception {
        final String csvFile = "C:\\Users\\maste\\IdeaProjects\\CookwareHomeServer\\test.csv";
        final String[] row1 = {"R1C1","R1C2","R1C3"};
        final String[] row2 = {"R2C1","R2C2","R2C3"};
        final String[] row3 = {"R3C1","R3C2","R3C3"};
        final String[][] table = {row1, row2, row3};
        final File file = new File(csvFile);
        final CsvManager csvManager = new CsvManager();

        csvManager.createFile(csvFile);
        csvManager.writeStringArrayToCsv(csvFile, table);

        final String[][] result = csvManager.getStringArrayFromCsv(csvFile);

        file.delete();
    }
}