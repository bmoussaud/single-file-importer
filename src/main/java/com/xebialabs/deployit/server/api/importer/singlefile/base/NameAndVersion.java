/*
 * @(#)Filenames.java     1 Aug 2011
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
package com.xebialabs.deployit.server.api.importer.singlefile.base;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

public class NameAndVersion {
    private static final NameVersionParser PARSER = new NameVersionParser();
    
    public final String name;
    public final String version;

    private NameAndVersion(String name, String version) {
        this.name = name;
        this.version = version;
    }
    
    public static NameAndVersion from(String filename, String defaultVersion) {
        return PARSER.parse(filename, defaultVersion);
    }
    
    public static NameAndVersion from(String filename, String defaultVersion, 
            String nameVersionRegex) {
        return new NameVersionParser(nameVersionRegex).parse(filename, defaultVersion);
    }
    
    public static class NameVersionParser {
        // <name>.<ext> or <name>-<version>.<ext>
        private static final Pattern DEFAULT_NAME_VERSION_PATTERN = 
            Pattern.compile("([A-Za-z0-9]+)(?:-([^\\.]+))?\\.[A-Za-z0-9]+");
        
        private final Pattern nameVersionPattern;
        
        public NameVersionParser() {
            this(DEFAULT_NAME_VERSION_PATTERN);
        }
        
        public NameVersionParser(@Nonnull String nameVersionRegex) {
            this(compile(nameVersionRegex));
        }
        
        private static Pattern compile(String nameVersionRegex) {
            Pattern nameVersionPattern = Pattern.compile(nameVersionRegex);
            checkArgument(nameVersionPattern.matcher("").groupCount() > 0, 
                    "Name/version regular expression must contain at least one matchine group");
            return nameVersionPattern;
        }
        
        private NameVersionParser(@Nonnull Pattern nameVersionPattern) {
            this.nameVersionPattern = nameVersionPattern;
        }
        
        public @Nonnull NameAndVersion parse(@Nonnull String filename, 
                @Nonnull String defaultVersion) {
            Matcher nameAndVersion = nameVersionPattern.matcher(filename);
            if (!nameAndVersion.matches()) {
                return new NameAndVersion(filename, defaultVersion);
            }

            return new NameAndVersion(nameAndVersion.group(1), 
                    ((nameAndVersion.groupCount() > 1) && (nameAndVersion.group(2) != null)) 
                     ? nameAndVersion.group(2) 
                     : defaultVersion); 
        }
    }
}
