/*
 * <license>
 * Copyright (c) 2003-2004, Sun Microsystems, Inc.
 * Copyright (c) 2022-2026, Web-Legacy
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Tag library that gets its information from a TLD file in a JAR that's packaged inside a WAR.
 *
 * @author mroth
 */
public class WarJarTldFileTagLibrary extends WarJarTagLibrary {

    /**
     * The JAR containing the TLD file.
     */
    private final String warEntryName;

    /**
     * Creates a new instance of {@link WarJarTldFileTagLibrary}.
     *
     * @param war          WAR containing the JAR
     * @param warEntryName JAR containing the TLD file
     * @param tldPath      name of the {@code JarEntry} containing the TLD file
     */
    public WarJarTldFileTagLibrary(Path war, String warEntryName, String tldPath) {
        super(war, tldPath);
        this.warEntryName = warEntryName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPathDescription() {
        return getWarJar().toAbsolutePath().toString() + "!" + warEntryName + "!" + getEntry();
    }

    /**
     * Returns an input stream for reading the contents of the specified JAR-file entry from the
     * JAR-file in the WAR-file.
     *
     * @param path the path to the resource
     *
     * @return an input stream for reading the contents of the specified JAR-file entry
     *
     * @throws IOException if an I/O error has occurred
     */
    @Override
    protected InputStream getInputStream(String path) throws IOException {
        final JarInputStream in = new JarInputStream(super.getInputStream(warEntryName));

        // in is now the input stream to the JAR in the WAR.
        JarEntry jarEntry;
        while ((jarEntry = in.getNextJarEntry()) != null) {
            if (jarEntry.getName().equals(path)) {
                return in;
            }
        }

        return null;
    }
}
