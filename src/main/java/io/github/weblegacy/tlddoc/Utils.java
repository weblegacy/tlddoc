/*
 * <license>
 * Copyright (c) 2003-2004, Sun Microsystems, Inc.
 * Copyright (c) 2022-2025, Web-Legacy
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
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Various utils used by TldDoc.
 *
 * @author ste-gr
 */
public final class Utils {

    /**
     * Private constructor as this is a utility class.
     */
    private Utils() {
    }

    /**
     * The name separator as a string from the default-file-system.
     */
    public static final String DEFAULT_SEPARATOR = FileSystems.getDefault().getSeparator();

    /**
     * Tests if the path is the WEB-INF directory.
     *
     * @param p the path
     *
     * @return {@code true} when it is the WEB-INF directory
     */
    public static boolean isWebInf(final Path p) {
        final Path fn = p.getFileName();
        return fn != null && Files.isDirectory(p) && "WEB-INF".equalsIgnoreCase(fn.toString());
    }

    /**
     * Tests if the path contains the directory-part.
     *
     * @param p path to test
     * @param s the directory-part
     *
     * @return {@code true} when the path contains the directory-part
     */
    public static boolean pathContains(final Path p, final String s) {
        return pathString(p).contains(s);
    }

    /**
     * Retuns the neutral string of the path (path-name-separator is always '/').
     *
     * @param p path to test
     *
     * @return the neutral string of the path
     */
    public static String pathString(final Path p) {
        final Path absolutePath = p.toAbsolutePath();
        final String separator = absolutePath.getFileSystem().getSeparator();

        String path = absolutePath.toString();
        if (!"/".equals(separator)) {
            path = path.replace(separator, "/");
        }

        return path;
    }

    /**
     * Returns from the file the file-name-part in lower-case.
     *
     * @param file the file
     *
     * @return file-name-part in lower-case
     */
    public static String getLowerFileName(final Path file) {
        final Path fn = file.getFileName();
        return fn == null ? "" : fn.toString().toLowerCase(Locale.ROOT);
    }

    /**
     * Checks if the file is a tag-file (ends with {@code .tag} or {@code .tagx}).
     *
     * @param file to check
     *
     * @return {@code true} if tag-file
     */
    public static boolean isTag(final Path file) {
        if (Files.isDirectory(file)) {
            return false;
        }

        final String lowerFileName = getLowerFileName(file);
        return lowerFileName.endsWith(".tag") || lowerFileName.endsWith(".tagx");
    }

    /**
     * Checks if the file is a tag-file (ends with {@code .tag} or {@code .tagx}).
     *
     * @param file to check
     *
     * @return {@code true} if tag-file
     */
    public static boolean isTag(final String file) {
        final String lowerFileName = file.toLowerCase(Locale.ROOT);
        return lowerFileName.endsWith(".tag") || lowerFileName.endsWith(".tagx");
    }

    /**
     * Checks if the file is a tld-file (ends with {@code .tld}).
     *
     * @param file to check
     *
     * @return {@code true} if tld-file
     */
    public static boolean isTld(final Path file) {
        return getLowerFileName(file).endsWith(".tld");
    }

    /**
     * Checks if the file is a tld-file (ends with {@code .tld}).
     *
     * @param file to check
     *
     * @return {@code true} if tld-file
     */
    public static boolean isTld(final String file) {
        return file.toLowerCase(Locale.ROOT).endsWith(".tld");
    }

    /**
     * Checks if the file is a jar-file (ends with {@code .jar}).
     *
     * @param file to check
     *
     * @return {@code true} if jar-file
     */
    public static boolean isJar(final Path file) {
        return getLowerFileName(file).endsWith(".jar");
    }

    /**
     * Checks if the file is a jar-file (ends with {@code .jar}).
     *
     * @param file to check
     *
     * @return {@code true} if jar-file
     */
    public static boolean isJar(final String file) {
        return file.toLowerCase(Locale.ROOT).endsWith(".jar");
    }

    /**
     * Process all filtered files under the given directory, recursively.
     *
     * @param path    The path to search (recursively).
     * @param filter  call process when filter is {@code true}
     * @param process process filtered files
     *
     * @throws IOException if an I/O error has occurred
     */
    public static void processFiles(final Path path, final Predicate<Path> filter,
            final Consumer<Path> process) throws IOException {

        if (Files.isDirectory(path)) {
            processFiles0(path, filter, process);
        }
    }

    /**
     * Process all filtered files under the given directory, recursively.
     *
     * @param path    The path to search (recursively).
     * @param filter  call process when filter is {@code true}
     * @param process process filtered files
     *
     * @throws IOException if an I/O error has occurred
     */
    private static void processFiles0(final Path path, final Predicate<Path> filter,
            final Consumer<Path> process) throws IOException {

        try (DirectoryStream<Path> files = Files.newDirectoryStream(path)) {
            for (Path file : files) {
                if (Files.isDirectory(file)) {
                    processFiles0(file, filter, process);
                } else if (filter.test(file)) {
                    process.accept(file);
                }
            }
        }
    }

    /**
     * Process all directories under the given directory, recursively.
     *
     * @param path    The path to search (recursively).
     * @param process process directory
     *
     * @throws IOException if an I/O error has occurred
     */
    public static void processDirs(final Path path, final Consumer<Path> process)
            throws IOException {

        if (Files.isDirectory(path)) {
            processDirs0(path, process);
        }
    }

    /**
     * Process all directories under the given directory, recursively.
     *
     * @param path    The path to search (recursively).
     * @param process process directory
     *
     * @throws IOException if an I/O error has occurred
     */
    private static void processDirs0(final Path path, final Consumer<Path> process)
            throws IOException {

        process.accept(path);

        try (DirectoryStream<Path> files = Files.newDirectoryStream(path, Files::isDirectory)) {
            for (Path file : files) {
                processDirs0(file, process);
            }
        }
    }

    /**
     * Start from the dir and backtrack, using the path as a relative path.
     * <p>
     * For example:</p>
     * <ul>
     * <li>dir: /home/mroth/test/sample/WEB-INF/tags/mytags</li>
     * <li>path: /WEB-INF/tags/mytags/tag1.tag</li>
     * <li>returns: /home/mroth/test/sample/WEB-INF/tags/mytags/tag1.tag</li>
     * </ul>
     *
     * @param dir  the start-directory
     * @param path the path to backtrack
     *
     * @return resolved path as {@link InputStream} or {@code null} when not found
     *
     * @throws IOException if an I/O error has occurred
     */
    public static InputStream backtrackPath(Path dir, String path) throws IOException {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        Path look = null;
        while (dir != null && !Files.exists(look = dir.resolve(path))) {
            dir = dir.getParent();
        }

        return look != null && Files.exists(look) ? Files.newInputStream(look) : null;
    }
}
