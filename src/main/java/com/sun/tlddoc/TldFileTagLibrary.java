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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Tag library represented by a single standalone TLD file.
 *
 * @author mroth
 */
public class TldFileTagLibrary extends TagLibrary {

    /**
     * The location of the TLD file for this tag library.
     */
    private final Path tldFile;

    /**
     * Creates a new instance of {@link TldFileTagLibrary}.
     *
     * @param tldFile location of the TLD file for this tag library
     */
    public TldFileTagLibrary(Path tldFile) {
        this.tldFile = tldFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPathDescription() {
        return tldFile.toAbsolutePath().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Document getTldDocument(DocumentBuilder documentBuilder) throws
            IOException, SAXException, TransformerException {

        try (InputStream in = Files.newInputStream(tldFile)) {
            InputSource source = new InputSource(in);
            return documentBuilder.parse(source);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getResource(String path) throws IOException {
        InputStream result = null;

        // This is a bit of a guess, since we don't know where the TLD is.
        // Start from the directory containing the TLD, and backtrack,
        // using the path as a relative path.
        //   For example:
        //      TLD:  /home/mroth/test/sample/WEB-INF/tld/test.tld
        //      path: /WEB-INF/tags/tag1.tag
        Path dir = tldFile.getParent();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        Path look = null;
        while (dir != null && !Files.exists(look = dir.resolve(path))) {
            dir = dir.getParent();
        }

        if (look != null && Files.exists(look)) {
            // Found it (or something pretty close to it anyway)
            result = Files.newInputStream(look);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        // Nothing to do
    }
}
