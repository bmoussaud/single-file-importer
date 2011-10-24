/*
 * @(#)NameVersionParserTest.java     22 Oct 2011
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.xebialabs.deployit.server.api.importer.singlefile.base.NameAndVersion.NameVersionParser;

/**
 * Unit tests for the {@link NameVersionParser}
 */
public class NameVersionParserTest {

    @Test
    public void extractsNameAndVersion() {
        NameAndVersion nameAndVersion = 
            new NameVersionParser().parse("name-version.ext", "");
        assertEquals("name", nameAndVersion.name);
        assertEquals("version", nameAndVersion.version);
    }
    
    @Test
    public void extractsNameIfNoVersion() {
        NameAndVersion nameAndVersion = 
            new NameVersionParser().parse("name.ext", "1.1");
        assertEquals("name", nameAndVersion.name);
        assertEquals("1.1", nameAndVersion.version);
    }
    
    @Test
    public void fallsBackToDefaultVersion() {
        // name does not match
        NameAndVersion nameAndVersion = 
            new NameVersionParser().parse("no match.ext", "1.1");
        assertEquals("no match.ext", nameAndVersion.name);
        assertEquals("1.1", nameAndVersion.version);        
    }
   
    @Test(expected = IllegalArgumentException.class)
    public void requiresOneMatchingGroup() {
        // no matching groups
        new NameVersionParser(".+");        
    }

    @Test
    public void usesProvidedNameVersionRegex() {
        // <word> <word>
        NameAndVersion nameAndVersion = 
            new NameVersionParser("(\\w+) (\\w+)").parse("now match", "");
        assertEquals("now", nameAndVersion.name);
        assertEquals("match", nameAndVersion.version);        
    }
    
    @Test
    public void handlesRegexWithOneMatchingGroup() {
        NameAndVersion nameAndVersion = 
            new NameVersionParser("(\\w+)").parse("name", "1.1");
        assertEquals("name", nameAndVersion.name);
        assertEquals("1.1", nameAndVersion.version);        
    }
}
