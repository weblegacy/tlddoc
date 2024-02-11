/*
 * <license>
 * Copyright (c) 2003-2004, Sun Microsystems, Inc.
 * Copyright (c) 2022-2024, Web-Legacy
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </license>
 */

package com.sun.tlddoc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Implicit Tag Library for a directory of tag files that is encapsulated in a WAR file.
 *
 * @author mroth
 */
public class WarTagDirImplicitTagLibrary extends WarJarTagLibrary {

    /**
     * Creates a new instance of {@link WarTagDirImplicitTagLibrary}.
     *
     * @param war WAR file that contains this tag library
     * @param dir directory containing the tag files
     */
    public WarTagDirImplicitTagLibrary(Path war, String dir) {
        super(war, dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Document getTldDocument(DocumentBuilder documentBuilder) throws IOException,
            SAXException, TransformerFactoryConfigurationError, TransformerException {

        Document result = documentBuilder.newDocument();

        // Determine path from root of web application:
        String path = getEntry();
        if (!path.endsWith("/")) {
            path += "/";
        }

        Element taglibElement = TagDirImplicitTagLibrary.createRootTaglibNode(result,
                '/' + path);

        // According to the JSP 2.0 specification:
        // A <tag-file> element is considered to exist for each tag file in
        // this directory, with the following sub-elements:
        //    - The <name> for each is the filename of the tag file,
        //      without the .tag extension.
        //    - The <path> for each is the path of the tag file, relative
        //      to the root of the web application.
        ensureOpen();
        Enumeration<JarEntry> entries = getWarJarFile().entries();
        while (entries.hasMoreElements()) {
            JarEntry warEntry = entries.nextElement();
            String entryName = warEntry.getName();
            if (!warEntry.isDirectory() && entryName.startsWith(path)) {
                String relativeName = entryName.replace(Utils.DEFAULT_SEPARATOR, "/");
                relativeName = relativeName.substring(path.length());
                if (relativeName.indexOf('/') == -1 && Utils.isTag(relativeName)) {
                    // We're not in a subdirectory and file and ends with .tag or .tagx.
                    String tagName = relativeName.substring(0, relativeName.lastIndexOf('.'));
                    String tagPath = "/" + entryName;

                    TagDirImplicitTagLibrary.createTagEntry(result, tagName, tagPath,
                            taglibElement);
                }
            }
        }

        return TagDirImplicitTagLibrary.recreateDocument(documentBuilder, result);
    }
}
