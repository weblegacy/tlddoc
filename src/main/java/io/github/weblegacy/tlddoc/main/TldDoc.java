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

package io.github.weblegacy.tlddoc.main;

import io.github.weblegacy.tlddoc.Utils;
import io.github.weblegacy.tlddoc.Version;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * Main entry point for TldDoc. Allows commandline access.
 *
 * @author Mark Roth
 */
public class TldDoc {

    /**
     * Defines the Help-text for TldDoc.
     */
    private static final String USAGE
            = "Tag Library Documentation Generator " + Version.VERSION + " by\n"
            + " * Mark Roth, Sun Microsystems, Inc. and\n"
            + " * Stefan Graff, Web-Legacy\n"
            + "\n"
            + "Usage: tlddoc [options] taglib1 [taglib2 [taglib3 ...]]\n"
            + "Options:\n"
            + "  -help                  Displays this help message\n"
            + "  -xslt <directory>      Use the XSLT files in the given directory\n"
            + "                         instead of the defaults.\n"
            + "  -d <directory>         Destination directory for output files\n"
            + "                         (defaults to new dir called 'out')\n"
            + "  -doctitle <html-code>  Include title for the TLD index (first) page\n"
            + "  -windowtitle <text>    Browser window title\n"
            + "  -q                     Quiet Mode\n"
            + "\n"
            + "taglib{1,2,3,...}:\n"
            + "  * If the path is a file that ends in .tld, process an\n"
            + "    individual TLD file.\n"
            + "  * If the path is a file that ends in .jar, process a\n"
            + "    tag library JAR file.\n"
            + "  * If the path is a file that ends in .war, process all\n"
            + "    tag libraries in this web application.\n"
            + "  * If the path is a directory that includes /WEB-INF/tags\n"
            + "    process the implicit tag library for the given directory\n"
            + "    of tag files.\n"
            + "  * If the path is a directory containing a WEB-INF subdirectory,\n"
            + "    process all tag libraries in this web application.\n"
            + "  * Otherwise, error.";

    /**
     * Private constructor as this is the main class.
     */
    private TldDoc() {
    }

    /**
     * The main-entry-point for TldDoc.
     *
     * @param args Arguments from command line
     */
    public static void main(String[] args) {
        TldDocGenerator generator = new TldDocGenerator();

        try {
            initGenerator(args, generator);
            generator.generate();
        } catch (IOException | GeneratorException e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static void initGenerator(final String[] args, final TldDocGenerator generator)
            throws IOException {

        Iterator<String> iter = Arrays.asList(args).iterator();
        boolean atLeastOneTld = false;

        try {
            while (iter.hasNext()) {
                String arg = iter.next();

                switch (arg) {
                    case "-xslt":
                        arg = iter.next();
                        generator.setXsltDirectory(Paths.get(arg));
                        break;
                    case "-d":
                        arg = iter.next();
                        generator.setOutputDirectory(Paths.get(arg));
                        break;
                    case "-help":
                        usage(null);
                        break;
                    case "-q":
                        generator.setQuiet(true);
                        break;
                    case "-doctitle":
                        arg = iter.next();
                        generator.setDocTitle(arg);
                        break;
                    case "-windowtitle":
                        arg = iter.next();
                        generator.setWindowTitle(arg);
                        break;
                    case "-webapp":
                        atLeastOneTld |= addPath(iter, generator::addWebApp, "Web app");
                        break;
                    case "-jar":
                        atLeastOneTld |= addPath(iter, generator::addJar, "JAR");
                        break;
                    case "-tagdir":
                        atLeastOneTld |= addPath(iter, generator::addTagDir, "Tag Directory");
                        break;
                    default:
                        Path f = Paths.get(arg);
                        if (Files.exists(f)) {
                            String fn = Utils.getLowerFileName(f);
                            if (fn.endsWith(".tld")) {
                                // If the path is a file that ends in .tld,
                                // process an individual TLD file.
                                generator.addTld(f);
                                atLeastOneTld = true;
                            } else if (fn.endsWith(".jar")) {
                                // If the path is a file that ends in .jar,
                                // process a tag library JAR file.
                                generator.addJar(f);
                                atLeastOneTld = true;
                            } else if (fn.endsWith(".war")) {
                                // If the path is a file that ends in .war,
                                // process all tag libraries in this web app.
                                generator.addWar(f);
                                atLeastOneTld = true;
                            } else {
                                boolean foundWebInf = false;
                                if (Files.isDirectory(f)) {
                                    // If the path is a directory that includes
                                    // /WEB-INF/tags process the implicit tag library
                                    // for the given directory of tag files.
                                    if (Utils.pathContains(f, "/WEB-INF/tags")) {
                                        generator.addTagDir(f);
                                        foundWebInf = true;
                                    } else {
                                        if (f.endsWith("WEB-INF")) {
                                            foundWebInf = true;
                                        } else {
                                            try (DirectoryStream<Path> files = Files
                                                    .newDirectoryStream(f, Utils::isWebInf)) {

                                                foundWebInf = files.iterator().hasNext();
                                            }
                                        }

                                        if (foundWebInf) {
                                            generator.addWebApp(f);
                                        }
                                    }
                                }
                                if (foundWebInf) {
                                    atLeastOneTld = true;
                                } else {
                                    usage("Cannot determine tag library "
                                            + "type for " + f.toAbsolutePath());
                                }
                            }
                        } else {
                            usage("File/directory not found: " + arg);
                        }
                        break;
                }
            }
            if (!atLeastOneTld) {
                usage("Please specify at least one TLD file.");
            }
        } catch (NoSuchElementException e) {
            usage("Invalid Syntax.");
        }
    }

    private static boolean addPath(final Iterator<String> iter, final Consumer<Path> addDirFunction,
            final String type) throws IOException {

        final String arg = iter.next();
        final Path dir = Paths.get(arg);
        if (Files.exists(dir)) {
            addDirFunction.accept(dir);
            return true;
        }
        usage(type + " not found: " + arg);
        return false;
    }

    private static void usage(String message) {
        if (message != null) {
            System.out.println("Error: " + message);
        }
        System.out.println(USAGE);
        System.exit(0);
    }
}
