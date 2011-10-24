/*
 * @(#)SingleFileImporterTest.java     22 Oct 2011
 *
 * Copyright © 2010 Andrew Phillips.
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package com.xebialabs.deployit.server.api.importer.singlefile;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.xebialabs.deployit.plugin.api.boot.PluginBooter;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.udm.Deployable;
import com.xebialabs.deployit.plugin.jee.artifact.Ear;
import com.xebialabs.deployit.server.api.importer.ImportSource;
import com.xebialabs.deployit.server.api.importer.ImportingContext;
import com.xebialabs.deployit.server.api.importer.PackageInfo;
import com.xebialabs.overthere.local.LocalFile;

/**
 * Unit tests for the {@link SingleFileImporter}
 */
public class SingleFileImporterTest {
    private static final ImportingContext STUB_IMPORT_CTX = new ImportingContext() {
        @Override
        public <T> T getAttribute(String name) {
            throw new UnsupportedOperationException("TODO Auto-generated method stub");
        }

        @Override
        public <T> void setAttribute(String name, T value) {
            throw new UnsupportedOperationException("TODO Auto-generated method stub");
        }
    };
    
    private static class FilesWithVeImporter extends SingleFileImporter {

        private FilesWithVeImporter() {
            super(Type.valueOf(Ear.class));
        }

        @Override
        protected boolean isSupportedFile(File file) {
            return file.getName().contains("ve");
        }
    }
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    private final SingleFileImporter importer = new FilesWithVeImporter();
    private FileSource earSource;
    
    @BeforeClass
    public static void boot() {
        PluginBooter.bootWithoutGlobalContext();
    }
    
    @Before
    public void populateImportDir() throws IOException {
        earSource = new FileSource(tempFolder.newFile("name-version.ear"));
        tempFolder.newFile("name.war");
    }
    
    @Test
    public void listsSupportedFiles() {
        assertEquals(ImmutableList.of("name-version.ear"), 
                importer.list(tempFolder.getRoot()));
    }
    
    @Test
    public void handlesSupportedFiles() {
        assertTrue(format("Expected importer to handle '%s'", earSource.file),
                importer.canHandle(earSource));
    }
    
    @Test
    public void usesFilenameForPackageInfo() {
        PackageInfo packageInfo = importer.preparePackage(earSource, STUB_IMPORT_CTX);
        assertEquals("name", packageInfo.getApplicationName());
        assertEquals("version", packageInfo.getApplicationVersion());
    }
    
    @Test
    public void addsFileToImportedPackage() throws IOException {
        PackageInfo packageInfo = new PackageInfo(earSource);
        packageInfo.setApplicationName("name");
        packageInfo.setApplicationVersion("version");
        List<Deployable> deployables = importer
            .importEntities(packageInfo, STUB_IMPORT_CTX).getDeployables();
        assertEquals(1, deployables.size());
        assertTrue(format("Expected instance of %s", Ear.class),
                deployables.get(0) instanceof Ear);
        Ear ear = (Ear) deployables.get(0);
        assertTrue("Expected the files to contain the same bytes",
                Files.equal(earSource.getFile(), ((LocalFile) ear.getFile()).getFile()));
        assertEquals("Applications/name/version/name", ear.getId());
        assertEquals("name", ear.getName());
    }
    
    private static class FileSource implements ImportSource {
        private final File file;
        
        private FileSource(File file) {
            this.file = file;
        }
        
        @Override
        public File getFile() {
            return file;
        }

        @Override
        public void cleanUp() {}
    }
}
