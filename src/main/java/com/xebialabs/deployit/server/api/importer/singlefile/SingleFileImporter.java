/*
 * @(#)EarImporter.java     19 Oct 2011
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.xebialabs.deployit.plugin.api.reflect.DescriptorRegistry.getDescriptor;
import static java.lang.String.format;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.udm.Deployable;
import com.xebialabs.deployit.plugin.api.udm.base.BaseDeployableFileArtifact;
import com.xebialabs.deployit.plugin.api.util.Predicates;
import com.xebialabs.deployit.server.api.importer.ImportSource;
import com.xebialabs.deployit.server.api.importer.ImportedPackage;
import com.xebialabs.deployit.server.api.importer.ImportingContext;
import com.xebialabs.deployit.server.api.importer.ListableImporter;
import com.xebialabs.deployit.server.api.importer.PackageInfo;
import com.xebialabs.deployit.server.api.importer.singlefile.base.NameAndVersion;
import com.xebialabs.deployit.server.api.importer.singlefile.base.NameAndVersion.NameVersionParser;
import com.xebialabs.overthere.local.LocalFile;

public abstract class SingleFileImporter implements ListableImporter {
    private static final String DEFAULT_APP_VERSION = "1.0";
    private static final NameVersionParser NAME_VERSION_PARSER = new NameVersionParser();

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleFileImporter.class);
    
    protected final Type type;
    
    protected SingleFileImporter(Type type) {
        checkArgument(isBaseDeployableFileType(type), "'%s' must be a subtype of %s", 
                type, BaseDeployableFileArtifact.class);
        this.type = type; 
    }
    
    private static boolean isBaseDeployableFileType(Type type) {
        return Predicates.subtypeOf(Type.valueOf(BaseDeployableFileArtifact.class)).apply(type);
    }
    
    @Override
    public List<String> list(File directory) {
        ImmutableList<String> supportedFiles = copyOf(directory.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return isSupportedFile(new File(dir, name));
                    }
                }));
        LOGGER.debug("Found supported files in package directory: {}", supportedFiles);
        return supportedFiles;
    }
    
    protected abstract boolean isSupportedFile(File file);

    @Override
    public boolean canHandle(ImportSource source) {
        return isSupportedFile(source.getFile());
    }
    
    @Override
    public PackageInfo preparePackage(ImportSource source, ImportingContext context) {
        PackageMetadata packageMetadata = getPackageMetadata(source.getFile());
        PackageInfo packageInfo = new PackageInfo(source);
        packageInfo.setApplicationName(packageMetadata.appName);
        packageInfo.setApplicationVersion(packageMetadata.appVersion);
        return packageInfo;
    }
    
    // override me!
    protected PackageMetadata getPackageMetadata(File file) {
        NameAndVersion nameAndVersion = 
            NAME_VERSION_PARSER.parse(file.getName(), DEFAULT_APP_VERSION);
        return new PackageMetadata(nameAndVersion.name, nameAndVersion.version);
    }
    
    public static class PackageMetadata {
        public final String appName;
        public final String appVersion;
        
        public PackageMetadata(String appName, String appVersion) {
            this.appName = appName;
            this.appVersion = appVersion;
        }
    }
    
    @Override
    public ImportedPackage importEntities(PackageInfo packageInfo, ImportingContext context) {
        ImportedPackage importedPackage = new ImportedPackage(packageInfo);
        for (Deployable deployable : getDeployables(importedPackage)) {
            LOGGER.debug("Adding deployable '{}' to package '{}'", deployable, packageInfo);
            importedPackage.addDeployable(deployable);
        }
        return importedPackage;
    }
    
    // override me!
    protected Set<Deployable> getDeployables(ImportedPackage importedPackage) {
        File importedFile = importedPackage.getPackageInfo().getSource().getFile();
        BaseDeployableFileArtifact fileArtifact = 
            getDescriptor(getDeployableType(importedFile)).newInstance();
        fileArtifact.setId(format("%s/%s", importedPackage.getDeploymentPackage().getId(),
                importedPackage.getApplication().getName()));
        fileArtifact.setFile(LocalFile.valueOf(importedFile));
        LOGGER.debug("Created file artifact with ID '{}'", fileArtifact.getId());
        return ImmutableSet.<Deployable>of(fileArtifact);
    }
    
    // override me!
    protected Type getDeployableType(File file) {
        return type;
    }
    
    @Override
    public void cleanUp(PackageInfo packageInfo, ImportingContext context) {
        // nothing to do
    }
}
