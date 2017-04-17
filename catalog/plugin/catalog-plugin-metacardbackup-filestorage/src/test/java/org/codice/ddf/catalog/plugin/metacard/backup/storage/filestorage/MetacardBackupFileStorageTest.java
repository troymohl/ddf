/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.ddf.catalog.plugin.metacard.backup.storage.filestorage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.codice.ddf.catalog.plugin.metacard.backup.storage.api.MetacardBackupException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MetacardBackupFileStorageTest {

    private static final String OUTPUT_DIRECTORY = "test-output/";

    private static final String TEST_ID = "TestId";

    private static final String TEST_SHORT_ID = "a";

    private static final byte[] TEST_BYTES = "Test String".getBytes();

    private MetacardBackupFileStorage fileStorageProvider = new MetacardBackupFileStorage();

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        fileStorageProvider.setOutputDirectory(OUTPUT_DIRECTORY);
    }

    @After
    public void cleanUp() throws Exception {
        fileStorageProvider.setOutputDirectory(OUTPUT_DIRECTORY);
        if (fileExists(TEST_ID)) {
            fileStorageProvider.deleteData(TEST_ID);
        }

        if (fileExists(TEST_SHORT_ID)) {
            fileStorageProvider.deleteData(TEST_SHORT_ID);
        }
    }

    @Test
    public void testOutputDirectory() {
        assertThat(fileStorageProvider.getOutputDirectory(), is(OUTPUT_DIRECTORY));
    }

    @Test
    public void testRefresh() {
        String newBackupDir = "target/temp";
        Map<String, Object> properties = new HashMap<>();
        properties.put("outputDirectory", newBackupDir);
        fileStorageProvider.refresh(properties);
        assertThat(fileStorageProvider.getOutputDirectory(), is(newBackupDir));
    }

    @Test
    public void testRefreshBadValues() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("outputDirectory", 2);
        assertThat(fileStorageProvider.getOutputDirectory(), is(OUTPUT_DIRECTORY));
    }

    @Test
    public void testRefreshEmptyStrings() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("outputDirectory", "");
        assertThat(fileStorageProvider.getOutputDirectory(), is(OUTPUT_DIRECTORY));
    }

    @Test(expected = MetacardBackupException.class)
    public void testStoreWithEmptyOutputDirectory() throws Exception {
        fileStorageProvider.setOutputDirectory("");
        fileStorageProvider.storeData(TEST_ID, TEST_BYTES);
    }

    @Test(expected = MetacardBackupException.class)
    public void testDeleteWithEmptyOutputDirectory() throws Exception {
        fileStorageProvider.setOutputDirectory("");
        fileStorageProvider.deleteData(TEST_ID);
    }

    @Test
    public void testPath() throws Exception {
        fileStorageProvider.setOutputDirectory(OUTPUT_DIRECTORY);
        Path path = fileStorageProvider.getMetacardDirectory(TEST_ID);
        assertThat(path.toString(), is(OUTPUT_DIRECTORY + "Tes/tId/TestId"));
    }

    @Test
    public void testStoreTwice() throws Exception {
        fileStorageProvider.setOutputDirectory(OUTPUT_DIRECTORY);
        fileStorageProvider.storeData(TEST_ID, TEST_BYTES);
        fileStorageProvider.storeData(TEST_ID, TEST_BYTES);
        assertFile(TEST_ID, true);
    }

    @Test
    public void testStoreAndDelete() throws Exception {
        fileStorageProvider.setOutputDirectory(OUTPUT_DIRECTORY);
        fileStorageProvider.storeData(TEST_ID, TEST_BYTES);
        assertFile(TEST_ID, true);

        fileStorageProvider.deleteData(TEST_ID);
        assertFile(TEST_ID, false);
    }

    @Test
    public void testStoreShortId() throws Exception {
        fileStorageProvider.setOutputDirectory(OUTPUT_DIRECTORY);
        fileStorageProvider.storeData(TEST_SHORT_ID, TEST_BYTES);
        assertFile(TEST_SHORT_ID, true);
    }

    @Test(expected = MetacardBackupException.class)
    public void testStoreNullId() throws Exception {
        fileStorageProvider.setOutputDirectory(OUTPUT_DIRECTORY);
        fileStorageProvider.storeData(null, TEST_BYTES);
    }

    @Test(expected = MetacardBackupException.class)
    public void testStoreNullData() throws Exception {
        fileStorageProvider.setOutputDirectory(OUTPUT_DIRECTORY);
        fileStorageProvider.storeData(TEST_ID, null);
    }

    private void assertFile(String id, boolean exists) {
        assertThat(fileExists(id), is(exists));
    }

    private boolean fileExists(String id) {
        Path path  = fileStorageProvider.getMetacardDirectory(id);
        return path.toFile().exists();
    }
}
