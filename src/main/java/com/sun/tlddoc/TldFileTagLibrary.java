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
import javax.xml.transform.TransformerFactoryConfigurationError;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Tag library represented by a single standalone TLD file.
 *
 * @author mroth
 */
public class TldFileTagLibrary implements TagLibrary {

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
    public Document getTldDocument(DocumentBuilder documentBuilder) throws IOException,
            SAXException, TransformerFactoryConfigurationError, TransformerException {

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
        return Utils.backtrackPath(tldFile.getParent(), path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        // Nothing to do
    }
}
