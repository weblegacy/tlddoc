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
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Tag library that gets its information from a TLD file in a JAR.
 *
 * @author mroth
 */
public class JarTldFileTagLibrary extends TagLibrary {

    /**
     * The JAR containing the TLD file.
     */
    private final Path jar;

    /**
     * The JAR-file itself.
     */
    private JarFile jarFile = null;

    /**
     * The name of the JarEntry containing the TLD file.
     */
    private final String tldPath;

    /**
     * Creates a new instance of {@link JarTldFileTagLibrary}.
     *
     * @param jar     JAR containing the TLD file
     * @param tldPath name of the {@code JarEntry} containing the TLD file
     */
    public JarTldFileTagLibrary(Path jar, String tldPath) {
        this.jar = jar;
        this.tldPath = tldPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPathDescription() {
        return jar.toAbsolutePath().toString() + "!" + tldPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getResource(String path) throws IOException {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        return getInputStream(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Document getTldDocument(DocumentBuilder documentBuilder)
            throws IOException, SAXException, TransformerException {

        try (InputStream in = getInputStream(this.tldPath)) {
            if (documentBuilder != null && in != null) {
                return documentBuilder.parse(in);
            }
        }

        return null;
    }

    /**
     * Returns an input stream for reading the contents of the specified JAR-file entry.
     *
     * @param path the path to the resource
     *
     * @return an input stream for reading the contents of the specified JAR-file entry
     *
     * @throws IOException if an I/O error has occurred
     */
    private InputStream getInputStream(String path) throws IOException {
        if (jarFile == null) {
            jarFile = new JarFile(jar.toFile());
        }

        final JarEntry jarEntry = jarFile.getJarEntry(path);
        return jarEntry == null ? null : jarFile.getInputStream(jarEntry);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        if (jarFile == null) {
            return;
        }

        try {
            jarFile.close();
        } finally {
            jarFile = null;
        }
    }
}
