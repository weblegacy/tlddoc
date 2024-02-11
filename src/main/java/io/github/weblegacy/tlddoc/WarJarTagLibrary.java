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

package io.github.weblegacy.tlddoc;

import io.github.weblegacy.tlddoc.main.TagLibrary;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Base class for a WAR/JAR container-file.
 *
 * @author ste-gr
 */
public class WarJarTagLibrary implements TagLibrary {

    /**
     * The WAR/JAR container-file.
     */
    private final Path warJar;

    /**
     * The WAR/JAR-file itself.
     */
    private JarFile warJarFile = null;

    /**
     * The name of the jarEntry.
     */
    private final String entry;

    /**
     * Creates a new instance of {@link WarJarTagLibrary}.
     *
     * @param warJar WAR/JAR container-file
     * @param entry  name of the {@code JarEntry}
     */
    public WarJarTagLibrary(final Path warJar, final String entry) {
        this.warJar = warJar;
        this.entry = entry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPathDescription() {
        return warJar.toAbsolutePath().toString() + "!" + entry;
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
    public Document getTldDocument(final DocumentBuilder documentBuilder) throws IOException,
            SAXException, TransformerFactoryConfigurationError, TransformerException {

        try (InputStream in = getInputStream(entry)) {
            if (documentBuilder != null && in != null) {
                return documentBuilder.parse(in);
            }
        }

        return null;
    }

    /**
     * Returns an input stream for reading the contents of the specified WAR/JAR-file entry.
     *
     * @param path the path to the resource
     *
     * @return an input stream for reading the contents of the specified JAR-file entry
     *
     * @throws IOException if an I/O error has occurred
     */
    protected InputStream getInputStream(String path) throws IOException {
        ensureOpen();
        final JarEntry jarEntry = warJarFile.getJarEntry(path);
        return jarEntry == null ? null : warJarFile.getInputStream(jarEntry);
    }

    /**
     * Returns the WAR/JAR container-file.
     *
     * @return the WAR/JAR container-file
     */
    protected Path getWarJar() {
        return warJar;
    }

    /**
     * Returns the WAR/JAR-file itself.
     *
     * @return the WAR/JAR-file itself
     */
    protected JarFile getWarJarFile() {
        return warJarFile;
    }

    /**
     * Returns the name of the JarEntry.
     *
     * @return the name of the JarEntry
     */
    protected String getEntry() {
        return entry;
    }

    /**
     * Opens the WAR/JAR-file if it is not open yet.
     *
     * @throws IOException if an I/O error has occurred
     */
    protected void ensureOpen() throws IOException {
        if (warJarFile == null) {
            warJarFile = new JarFile(warJar.toFile());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        if (warJarFile == null) {
            return;
        }

        try {
            warJarFile.close();
        } finally {
            warJarFile = null;
        }
    }
}
