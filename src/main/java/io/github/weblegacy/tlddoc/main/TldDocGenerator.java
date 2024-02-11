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

import io.github.weblegacy.tlddoc.Constants;
import io.github.weblegacy.tlddoc.JarTldFileTagLibrary;
import io.github.weblegacy.tlddoc.TagDirImplicitTagLibrary;
import io.github.weblegacy.tlddoc.TldFileTagLibrary;
import io.github.weblegacy.tlddoc.Utils;
import io.github.weblegacy.tlddoc.WarJarTldFileTagLibrary;
import io.github.weblegacy.tlddoc.WarTagDirImplicitTagLibrary;
import io.github.weblegacy.tlddoc.tagfileparser.Attribute;
import io.github.weblegacy.tlddoc.tagfileparser.Directive;
import io.github.weblegacy.tlddoc.tagfileparser.javacc.ParseException;
import io.github.weblegacy.tlddoc.tagfileparser.javacc.TagFile;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * TldDoc Generator. Takes a set of TLD files and generates a set of javadoc-style HTML pages that
 * describe the various components of the input tag libraries.
 *
 * @author Mark Roth
 */
public class TldDocGenerator {

    /**
     * The set of tag libraries we are parsing. Each element is a TagLibrary instance.
     */
    private final ArrayList<TagLibrary> tagLibraries = new ArrayList<>();

    /**
     * The directory containing the stylesheets, or null if the default stylesheets are to be used.
     */
    private Path xsltDirectory = null;

    /**
     * The output directory for generated files.
     */
    private Path outputDirectory = Paths.get("out");

    /**
     * The browser window title for the documentation.
     */
    private String windowTitle = Constants.DEFAULT_WINDOW_TITLE;

    /**
     * The title for the TLD index (first) page.
     */
    private String docTitle = Constants.DEFAULT_DOC_TITLE;

    /**
     * {@code True} if no {@code stdout} is to be produced during generation.
     */
    private boolean quiet;

    /**
     * The summary TLD document, used as input into XSLT.
     */
    private Document summaryTld;

    /**
     * Path to tlddoc resources.
     */
    private static final String RESOURCE_PATH = "/io/github/weblegacy/tlddoc/resources";

    /**
     * Helps uniquely generate substitute prefixes in the case of missing or duplicate short-names.
     */
    private int substitutePrefix = 1;

    /**
     * Creates a new TldDocGenerator.
     */
    public TldDocGenerator() {
    }

    /**
     * Adds the given Tag Library to the list of Tag Libraries to generate documentation for.
     *
     * @param tagLibrary The tag library to add.
     */
    public void addTagLibrary(TagLibrary tagLibrary) {
        tagLibraries.add(tagLibrary);
    }

    /**
     * Adds the given individual TLD file.
     *
     * @param tld The TLD file to add
     */
    public void addTld(Path tld) {
        addTagLibrary(new TldFileTagLibrary(tld));
    }

    /**
     * Adds all the tag libraries found in the given web application.
     *
     * @param path The path to the root of the web application.
     */
    public void addWebApp(Path path) {
        try {
            Path webinf = path.endsWith("WEB-INF") ? path : path.resolve("WEB-INF");

            // Scan all subdirectories of /WEB-INF/ for .tld files
            addWebAppTldsIn(webinf);

            // Add all JAR files in /WEB-INF/lib that might potentially
            // contain TLDs.
            addWebAppJarsIn(webinf.resolve("lib"));

            // Add all implicit tag libraries in /WEB-INF/tags
            addWebAppTagDirsIn(webinf.resolve("tags"));

        } catch (IOException e) {
            println("WARNING: Could not access one or more entries in " + path.toAbsolutePath()
                    + ".  Skipping Web-App.  Reason: " + e.getMessage());
        }
    }

    /**
     * Adds all TLD files under the given directory, recursively.
     *
     * @param path The path to search (recursively) for TLDs in.
     *
     * @throws IOException if an I/O error has occurred
     */
    private void addWebAppTldsIn(Path path) throws IOException {
        Utils.processFiles(path, Utils::isTld, this::addTld);
    }

    /**
     * Adds all TLD files under the given directory, recursively.
     *
     * @param war  The WAR file to search
     * @param path The path to search (recursively) for TLDs in.
     *
     * @throws IOException if an I/O error has occurred
     */
    private void addWarTldsIn(Path war, String path) throws IOException {
        try (JarFile warFile = new JarFile(war.toFile())) {
            final Enumeration<JarEntry> entries = warFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry jarEntry = entries.nextElement();
                final String entryName = jarEntry.getName();
                if (entryName.startsWith(path) && Utils.isTld(entryName)) {
                    addTagLibrary(new JarTldFileTagLibrary(war, jarEntry.getName()));
                }
            }
        }
    }

    /**
     * Adds all JAR files under the given directory, recursively.
     *
     * @param path The path to search (recursively) for JARs in.
     *
     * @throws IOException if an I/O error has occurred
     */
    private void addWebAppJarsIn(Path path) throws IOException {
        Utils.processFiles(path, Utils::isJar, this::addJar);
    }

    /**
     * Adds all JAR files under the given directory, recursively.
     *
     * @param war  The WAR file to search
     * @param path The path to search (recursively) for JARs in.
     *
     * @throws IOException if an I/O error has occurred
     */
    private void addWarJarsIn(Path war, String path) throws IOException {
        try (JarFile warFile = new JarFile(war.toFile())) {
            Enumeration<JarEntry> entries = warFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry warEntry = entries.nextElement();
                String entryName = warEntry.getName();
                if (entryName.startsWith(path) && Utils.isJar(entryName)) {
                    // Add all tag libraries found in the given JAR file that is
                    // inside this WAR file:
                    try (JarInputStream in = new JarInputStream(warFile.getInputStream(warEntry))) {
                        // Search for all TLD files in the JAR file

                        JarEntry jarEntry;
                        while ((jarEntry = in.getNextJarEntry()) != null) {
                            if (Utils.isTld(jarEntry.getName())) {
                                addTagLibrary(new WarJarTldFileTagLibrary(war,
                                        entryName, jarEntry.getName()));
                            }
                        }
                    } catch (IOException e) {
                        println("WARNING: Could not access one or more entries in "
                                + war.toAbsolutePath() + " entry " + entryName
                                + ".  Skipping JAR.  Reason: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Adds all implicit tag libraries under the given directory, recursively.
     *
     * @param path The path to search (recursively) for tag file directories in
     *
     * @throws IOException if an I/O error has occurred
     */
    private void addWebAppTagDirsIn(Path path) throws IOException {
        Utils.processDirs(path, this::addTagDir);
    }

    /**
     * Adds all implicit tag libraries under the given directory, recursively.
     *
     * @param war  The WAR file to search
     * @param path The path to search (recursively) for tag file directories in
     *
     * @throws IOException if an I/O error has occurred
     */
    private void addWarTagDirsIn(Path war, String path) throws IOException {
        try (JarFile warFile = new JarFile(war.toFile())) {
            Enumeration<JarEntry> entries = warFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith(path) && entry.isDirectory()) {
                    addTagLibrary(new WarTagDirImplicitTagLibrary(war, entry.getName()));
                }
            }
        }
    }

    /**
     * Adds all the tag libraries found in the given JAR.
     *
     * @param jar The JAR file to add.
     */
    public void addJar(Path jar) {
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            // Search for all TLD files in the JAR file
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (Utils.isTld(jarEntry.getName())) {
                    addTagLibrary(new JarTldFileTagLibrary(jar, jarEntry.getName()));
                }
            }
        } catch (IOException e) {
            println("WARNING: Could not access one or more entries in " + jar.toAbsolutePath()
                    + ".  Skipping JAR.  Reason: " + e.getMessage());
        }
    }

    /**
     * Adds all the tag libraries found in the given web application packaged as a WAR file.
     *
     * @param path The war containing the web application
     */
    public void addWar(Path path) {
        try {
            // Scan all subdirectories of /WEB-INF/ for .tld files
            addWarTldsIn(path, "WEB-INF/");

            // Add all JAR files in /WEB-INF/lib that might potentially
            // contain TLDs.
            addWarJarsIn(path, "WEB-INF/lib/");

            // Add all implicit tag libraries in /WEB-INF/tags
            addWarTagDirsIn(path, "WEB-INF/tags/");
        } catch (IOException e) {
            println("WARNING: Could not access one or more entries in " + path.toAbsolutePath()
                    + ".  Skipping WAR.  Reason: " + e.getMessage());
        }
    }

    /**
     * Adds the given directory of tag files.
     *
     * @param tagdir The tag directory to add
     */
    public void addTagDir(Path tagdir) {
        addTagLibrary(new TagDirImplicitTagLibrary(tagdir));
    }

    /**
     * Sets the directory from which to obtain the XSLT stylesheets. This allows the user to change
     * the look and feel of the generated output. If not specified, the default stylesheets are
     * used.
     *
     * @param dir The base directory for the stylesheets
     */
    public void setXsltDirectory(Path dir) {
        this.xsltDirectory = dir;
    }

    /**
     * Sets the output directory for generated files. If not specified, defaults to "."
     *
     * @param dir The base directory for generated files.
     */
    public void setOutputDirectory(Path dir) {
        this.outputDirectory = dir;
    }

    /**
     * Sets the browser window title for the documentation.
     *
     * @param title The browser window title
     */
    public void setWindowTitle(String title) {
        this.windowTitle = title;
    }

    /**
     * Sets the title for the TLD index (first) page.
     *
     * @param title The title for the TLD index
     */
    public void setDocTitle(String title) {
        this.docTitle = title;
    }

    /**
     * Sets quiet mode (produce no {@code stdout} during generation).
     *
     * @param quiet {@code True} if no output is to be produced, {@code false} otherwise.
     */
    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    /**
     * Returns {@code true} if the generator is in quiet mode or false if not.
     *
     * @return {@code True} if no output is to be produced, {@code false} otherwise.
     */
    public boolean isQuiet() {
        return quiet;
    }

    /**
     * Commences documentation generation.
     *
     * @throws GeneratorException any error during generation
     */
    public void generate() throws GeneratorException {
        try {
            Files.createDirectories(outputDirectory);

            copyStaticFiles();
            createTldSummaryDoc();
            generateOverview();
            generateTldDetail();
            outputSuccessMessage();
        } catch (IOException | SAXException | TransformerFactoryConfigurationError
                | FactoryConfigurationError | ParserConfigurationException
                | TransformerException e) {
            throw new GeneratorException(e);
        }
    }

    /////////////////////////////////////////////////////////////////////
    /**
     * Copies all static files to target directory.
     *
     * @throws IOException if an I/O error has occurred
     */
    private void copyStaticFiles() throws IOException {
        copyResourceToFile(outputDirectory.resolve("stylesheet.css"),
                RESOURCE_PATH + "/stylesheet.css");
    }

    /**
     * Creates a summary document, comprising all input TLDs. This document is later used as input
     * into XSLT to generate all non-static output pages. Stores the result as a DOM tree in the
     * summaryTLD attribute.
     *
     * @throws IOException                          if an I/O error has occurred
     * @throws SAXException                         If any parse errors occur.
     * @throws TransformerFactoryConfigurationError Thrown in case of {@linkplain
     * java.util.ServiceConfigurationError service configuration error} or if the implementation is
     *                                              not available or cannot be instantiated.
     * @throws TransformerConfigurationException    Thrown if there are errors when parsing the
     *                                              {@code Source} or it is not possible to create a
     *                                              {@code Transformer} instance.
     * @throws FactoryConfigurationError            in case of {@linkplain
     * java.util.ServiceConfigurationError service configuration error} or if the implementation is
     *                                              not available or cannot be instantiated.
     * @throws ParserConfigurationException         if a DocumentBuilder cannot be created which
     *                                              satisfies the configuration requested.
     * @throws TransformerException                 If an unrecoverable error occurs during the
     *                                              course of the transformation.
     * @throws GeneratorException                   taglib is not valid
     */
    private void createTldSummaryDoc() throws IOException, SAXException,
            TransformerFactoryConfigurationError, TransformerConfigurationException,
            FactoryConfigurationError, ParserConfigurationException, TransformerException,
            GeneratorException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        factory.setExpandEntityReferences(false);
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        documentBuilder.setEntityResolver((publicId, systemId)
                -> new InputSource(new CharArrayReader(new char[0]))
        );
        summaryTld = documentBuilder.newDocument();

        // Create root <tlds> element:
        Element rootElement = summaryTld.createElementNS(Constants.NS_JAKARTAEE, "tlds");
        summaryTld.appendChild(rootElement);
        // JDK 1.4 does not add xmlns for some reason - add it manually:
        rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns",
                Constants.NS_JAKARTAEE);

        // Create configuration element:
        Element configElement = summaryTld.createElementNS(Constants.NS_JAKARTAEE, "config");
        rootElement.appendChild(configElement);

        Element windowTitleElement = summaryTld.createElementNS(Constants.NS_JAKARTAEE,
                "window-title");
        windowTitleElement.appendChild(summaryTld.createTextNode(this.windowTitle));
        configElement.appendChild(windowTitleElement);

        Element docTitleElement = summaryTld.createElementNS(Constants.NS_JAKARTAEE, "doc-title");
        docTitleElement.appendChild(summaryTld.createTextNode(this.docTitle));
        configElement.appendChild(docTitleElement);

        // Append each <taglib> element from each TLD:
        println("Loading and translating " + tagLibraries.size()
                + " Tag Librar"
                + ((tagLibraries.size() == 1) ? "y" : "ies")
                + "...");
        for (final TagLibrary tagLibrary_ : tagLibraries) {
            // to AutoClose internal files at TagLibrary-Implementations
            try (TagLibrary tagLibrary = tagLibrary_) {
                Document doc = tagLibrary.getTldDocument(documentBuilder);

                // Convert document to JSP 3.0 TLD
                doc = upgradeTld(doc);

                // If this tag library has no tags, no validators,
                // and no functions, omit it
                final Element element = doc.getDocumentElement();
                int numTags = element == null ? 0
                        : element.getElementsByTagNameNS("*", "tag").getLength()
                        + element.getElementsByTagNameNS("*", "tag-file").getLength()
                        + element.getElementsByTagNameNS("*", "validator").getLength()
                        + element.getElementsByTagNameNS("*", "function").getLength();
                if (numTags > 0) {
                    // Populate the root element with extra information
                    populateTld(tagLibrary, doc);

                    Element taglibNode = (Element) summaryTld.importNode(
                            doc.getDocumentElement(), true);
                    if (!(taglibNode.getNamespaceURI().equals(Constants.NS_JAKARTAEE)
                            || taglibNode.getNamespaceURI().equals(Constants.NS_JAVAEE)
                            || taglibNode.getNamespaceURI().equals(Constants.NS_J2EE))) {
                        throw new GeneratorException("Error: "
                                + tagLibrary.getPathDescription()
                                + " does not have xmlns=\"" + Constants.NS_JAKARTAEE + "\"");
                    }
                    if (!taglibNode.getLocalName().equals("taglib")) {
                        throw new GeneratorException("Error: "
                                + tagLibrary.getPathDescription()
                                + " does not have <taglib> as root.");
                    }
                    rootElement.appendChild(taglibNode);
                }
            }
        }

        // If debug enabled, output the resulting document, as a test:
        if (Constants.DEBUG_INPUT_DOCUMENT) {
            Transformer transformer
                    = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(summaryTld),
                    new StreamResult(System.out));
        }
    }

    /**
     * Converts the given TLD to a JSP 3.0 TLD.
     *
     * @param doc the given TLD
     *
     * @return the converted to JSP 3.0 TLD
     *
     * @throws TransformerFactoryConfigurationError Thrown in case of {@linkplain
     * java.util.ServiceConfigurationError service configuration error} or if the implementation is
     *                                              not available or cannot be instantiated.
     * @throws TransformerConfigurationException    Thrown if there are errors when parsing the
     *                                              {@code Source} or it is not possible to create a
     *                                              {@code Transformer} instance.
     * @throws FactoryConfigurationError            in case of {@linkplain
     * java.util.ServiceConfigurationError service configuration error} or if the implementation is
     *                                              not available or cannot be instantiated.
     * @throws ParserConfigurationException         if a DocumentBuilder cannot be created which
     *                                              satisfies the configuration requested.
     * @throws TransformerException                 If an unrecoverable error occurs during the
     *                                              course of the transformation.
     */
    private Document upgradeTld(Document doc) throws
            TransformerFactoryConfigurationError, TransformerConfigurationException,
            FactoryConfigurationError, ParserConfigurationException, TransformerException {

        Element root = doc.getDocumentElement();

        // We use getElementsByTagName instead of getElementsByTagNameNS
        // here since JSP 1.1 TLDs have no namespace.
        if (root.getElementsByTagName("jspversion").getLength() > 0) {
            removeNameSpace(doc, root);

            // JSP 1.1 TLD - convert to JSP 1.2 TLD first.
            doc = convertTld(doc, RESOURCE_PATH + "/tld1_1-tld1_2.xsl");
            root = doc.getDocumentElement();
        }

        // We use getElementsByTagName instead of getElementsByTagNameNS
        // here since JSP 1.2 TLDs have no namespace.
        if (root.getElementsByTagName("jsp-version").getLength() > 0) {
            removeNameSpace(doc, root);

            // JSP 1.2 TLD - convert to JSP 2.0 TLD first
            doc = convertTld(doc, RESOURCE_PATH + "/tld1_2-tld2_0.xsl");
            root = doc.getDocumentElement();
        }

        if ("2.0".equals(root.getAttribute("version"))) {
            // JSP 2.0 TLD - convert to JSP 2.1 TLD first
            doc = convertTld(doc, RESOURCE_PATH + "/tld2_0-tld2_1.xsl");
            root = doc.getDocumentElement();
        }

        if ("2.1".equals(root.getAttribute("version"))) {
            // JSP 2.1 TLD - convert to JSP 3.0 TLD first
            doc = convertTld(doc, RESOURCE_PATH + "/tld2_1-tld3_0.xsl");
        }

        // Final conversion to remove unwanted elements
        doc = convertTld(doc, RESOURCE_PATH + "/tld3_0-tld3_0.xsl");

        // We should now have a JSP 3.0 TLD in doc.
        return doc;
    }

    /**
     * Deletes a possibly existing namespace from the root element.
     *
     * @param doc  the document
     * @param root the root-element
     */
    private static void removeNameSpace(final Document doc, final Element root) {
        final String ns = root.getNamespaceURI();
        if (!(ns == null || ns.isEmpty())) {
            removeNameSpace(doc, root, ns);
        }
    }

    /**
     * Deletes a specific namespace from all elements and attributes.
     *
     * @param doc  the document
     * @param node the current element
     * @param ns   the namespace to remove
     */
    private static void removeNameSpace(final Document doc, final Node node, final String ns) {
        if ((node.getNodeType() == Node.ELEMENT_NODE || node.getNodeType() == Node.ATTRIBUTE_NODE)
                && ns.equals(node.getNamespaceURI())) {
            doc.renameNode(node, null, node.getLocalName());
        }

        final NodeList list = node.getChildNodes();
        for (int i = 0;
                i < list.getLength();
                ++i) {
            removeNameSpace(doc, list.item(i), ns);
        }
    }

    /**
     * Converts the given TLD using the given stylesheet.
     *
     * @param doc        the given TLD
     * @param stylesheet the given stylesheet
     *
     * @return the converted TLD
     *
     * @throws TransformerFactoryConfigurationError Thrown in case of {@linkplain
     * java.util.ServiceConfigurationError service configuration error} or if the implementation is
     *                                              not available or cannot be instantiated.
     * @throws TransformerConfigurationException    Thrown if there are errors when parsing the
     *                                              {@code Source} or it is not possible to create a
     *                                              {@code Transformer} instance.
     * @throws FactoryConfigurationError            in case of {@linkplain
     * java.util.ServiceConfigurationError service configuration error} or if the implementation is
     *                                              not available or cannot be instantiated.
     * @throws ParserConfigurationException         if a DocumentBuilder cannot be created which
     *                                              satisfies the configuration requested.
     * @throws TransformerException                 If an unrecoverable error occurs during the
     *                                              course of the transformation.
     */
    private Document convertTld(Document doc, String stylesheet) throws
            TransformerFactoryConfigurationError, TransformerConfigurationException,
            FactoryConfigurationError, ParserConfigurationException, TransformerException {

        InputStream xsl = getResourceAsStream(stylesheet);
        Transformer transformer = TransformerFactory.newInstance().newTransformer(
                new StreamSource(xsl));
        Document result = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        transformer.transform(new DOMSource(doc), new DOMResult(result));
        return result;
    }

    /**
     * Populates the root element with any additional information needed before adding this to our
     * tree.
     *
     * @param tagLibrary The tag library being populated
     * @param doc        The TLD DOM to populate.
     */
    private void populateTld(TagLibrary tagLibrary, Document doc) {
        Element root = doc.getDocumentElement();

        checkOrAddShortName(tagLibrary, doc, root);
        checkOrAddAttributeType(doc, root);
        populateTagFileDetails(tagLibrary, doc, root);
    }

    /**
     * Find all tag-file elements and populate them with the actual parsed meta information, found
     * in the tag file's attributes.
     *
     * @param tagLibrary The tag library being populated
     * @param doc        The document we're populating
     * @param root       The root element of the TLD DOM being populated.
     */
    private void populateTagFileDetails(TagLibrary tagLibrary, Document doc, Element root) {
        NodeList tagFileNodes = root.getElementsByTagNameNS("*", "tag-file");
        for (int i = 0; i < tagFileNodes.getLength(); i++) {
            Element tagFileNode = (Element) tagFileNodes.item(i);
            String path = findElementValue(tagFileNode, "path");
            if (path == null) {
                println("WARNING: "
                        + tagLibrary.getPathDescription()
                        + " contains a tag-file element with no path.  Skipping.");
            } else {
                try (InputStream tagFileIn = tagLibrary.getResource(path)) {
                    if (tagFileIn == null) {
                        println("WARNING: Could not find tag file '"
                                + path + "' for tag library "
                                + tagLibrary.getPathDescription()
                                + ".  Data will be incomplete for this tag.");
                    } else {
                        println("Parsing tag file: " + path);
                        TagFile tagFile = TagFile.parse(tagFileIn);
                        for (Directive directive : tagFile.getDirectives()) {
                            String name = directive.getDirectiveName();
                            switch (name) {
                                case "tag":
                                    populateTagFileDetailsTagDirective(
                                            tagFileNode, doc, directive);
                                    break;
                                case "attribute":
                                    populateTagFileDetailsAttributeDirective(
                                            tagFileNode, doc, directive);
                                    break;
                                case "variable":
                                    populateTagFileDetailsVariableDirective(
                                            tagFileNode, doc, directive);
                                    break;
                                default:
                                    break;
                            }
                        }

                        populateTagFileDetailsTagDefaults(tagFileNode, doc,
                                path);
                    }
                } catch (IOException e) {
                    println("WARNING: Could not read tag file '"
                            + path + "' for tag library "
                            + tagLibrary.getPathDescription()
                            + ".  Data will be incomplete for this tag."
                            + "  Reason: " + e.getMessage());
                } catch (ParseException e) {
                    println("WARNING: Could not parse tag file '"
                            + path + "' for tag library "
                            + tagLibrary.getPathDescription()
                            + ".  Data will be incomplete for this tag."
                            + "  Reason: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Populates the given tag-file node with information from the given tag directive.
     *
     * @param tagFileNode The tag-file element
     * @param doc         The document we're populating
     * @param directive   The tag directive to process.
     */
    private void populateTagFileDetailsTagDirective(Element tagFileNode, Document doc,
            Directive directive) {

        for (Attribute attribute : directive.getAttributes()) {
            String name = attribute.getName();
            String value = attribute.getValue();
            Element element;
            if (name.equals("display-name")
                    || name.equals("body-content")
                    || name.equals("dynamic-attributes")
                    || name.equals("description")
                    || name.equals("example")) {
                element = doc.createElementNS(Constants.NS_JAKARTAEE, name);
                element.appendChild(doc.createTextNode(value));
                tagFileNode.appendChild(element);
            } else if (name.equals("small-icon")
                    || name.equals("large-icon")) {
                NodeList icons = tagFileNode.getElementsByTagNameNS("*", "icon");
                Element icon;
                if (icons.getLength() == 0) {
                    icon = doc.createElementNS(Constants.NS_JAKARTAEE, "icon");
                    tagFileNode.appendChild(icon);
                } else {
                    icon = (Element) icons.item(0);
                }
                element = doc.createElementNS(Constants.NS_JAKARTAEE, name);
                element.appendChild(doc.createTextNode(value));
                icon.appendChild(element);
            }
        }
    }

    /**
     * Populates the given tag-file node with default values, for those values not specified via tag
     * directives.
     *
     * @param tagFileNode The tag-file element
     * @param doc         The document we're populating
     * @param path        The path to the tag file
     */
    private void populateTagFileDetailsTagDefaults(Element tagFileNode, Document doc, String path) {
        String displayName = path.substring(
                path.lastIndexOf('/') + 1);
        displayName = displayName.substring(0,
                displayName.lastIndexOf('.'));
        populateDefault(doc, tagFileNode, "display-name", displayName);
        populateDefault(doc, tagFileNode, "body-content", "scriptless");
        populateDefault(doc, tagFileNode, "dynamic-attributes", "false");
    }

    /**
     * Searches for the value of the given element. If no value is found, a default value is
     * inserted.
     *
     * @param doc          The document we're populating
     * @param parent       The element to examine
     * @param tagName      The name of the element we're looking for
     * @param defaultValue The default value to insert, if not found.
     */
    private void populateDefault(Document doc, Element parent, String tagName,
            String defaultValue) {
        if (findElementValue(parent, tagName) == null) {
            Element element = doc.createElementNS(Constants.NS_JAKARTAEE, tagName);
            element.appendChild(doc.createTextNode(defaultValue));
            parent.appendChild(element);
        }
    }

    /**
     * Populates the given tag-file node with information from the given attribute directive.
     *
     * @param tagFileNode The tag-file element
     * @param doc         The document we're populating
     * @param directive   The attribute directive to process.
     */
    private void populateTagFileDetailsAttributeDirective(
            Element tagFileNode, Document doc, Directive directive) {
        Element attributeNode = doc.createElementNS(Constants.NS_JAKARTAEE,
                "attribute");
        tagFileNode.appendChild(attributeNode);
        String deferredValueType = null;
        String deferredMethodSignature = null;
        for (Attribute attribute : directive.getAttributes()) {
            String name = attribute.getName();
            String value = attribute.getValue();
            Element element;
            switch (name) {
                case "name":
                case "required":
                case "fragment":
                case "rtexprvalue":
                case "type":
                case "description":
                    element = doc.createElementNS(Constants.NS_JAKARTAEE, name);
                    element.appendChild(doc.createTextNode(value));
                    attributeNode.appendChild(element);
                    break;
                case "deferredValue":
                    if (deferredValueType == null) {
                        deferredValueType = "java.lang.Object";
                    }
                    break;
                case "deferredValueType":
                    deferredValueType = value;
                    break;
                case "deferredMethod":
                    if (deferredMethodSignature == null) {
                        deferredMethodSignature = "void methodname()";
                    }
                    break;
                case "deferredMethodSignature":
                    deferredMethodSignature = value;
                    break;
                default:
                    break;
            }
        }
        if (deferredValueType != null) {
            Element deferredValueElement
                    = doc.createElementNS(Constants.NS_JAKARTAEE, "deferred-value");
            attributeNode.appendChild(deferredValueElement);
            Element typeElement
                    = doc.createElementNS(Constants.NS_JAKARTAEE, "type");
            typeElement.appendChild(doc.createTextNode(deferredValueType));
            deferredValueElement.appendChild(typeElement);
        }
        if (deferredMethodSignature != null) {
            Element deferredMethodElement
                    = doc.createElementNS(Constants.NS_JAKARTAEE, "deferred-method");
            attributeNode.appendChild(deferredMethodElement);
            Element methodSignatureElement
                    = doc.createElementNS(Constants.NS_JAKARTAEE, "method-signature");
            methodSignatureElement.appendChild(
                    doc.createTextNode(deferredMethodSignature));
            deferredMethodElement.appendChild(methodSignatureElement);
        }
        populateDefault(doc, attributeNode, "required", "false");
        populateDefault(doc, attributeNode, "fragment", "false");
        populateDefault(doc, attributeNode, "rtexprvalue", "false");

        // Default is String if this is not a fragment attribute, or
        // javax.servlet.jsp.tagext.JspFragment if this is a fragment
        // attribute.
        String fragmentValue = findElementValue(attributeNode, "fragment");
        boolean fragment = !(fragmentValue == null
                || fragmentValue.equalsIgnoreCase("false"));
        populateDefault(doc, attributeNode, "type",
                fragment ? "javax.servlet.jsp.tagext.JspFragment"
                        : "java.lang.String");
    }

    /**
     * Populates the given tag-file node with information from the given variable directive.
     *
     * @param tagFileNode The tag-file element
     * @param doc         The document we're populating
     * @param directive   The variable directive to process.
     */
    private void populateTagFileDetailsVariableDirective(
            Element tagFileNode, Document doc, Directive directive) {
        Element variableNode = doc.createElementNS(Constants.NS_JAKARTAEE,
                "variable");
        tagFileNode.appendChild(variableNode);
        for (Attribute attribute : directive.getAttributes()) {
            String name = attribute.getName();
            String value = attribute.getValue();
            Element element;
            if (name.equals("name-given")
                    || name.equals("name-from-attribute")
                    || name.equals("variable-class")
                    || name.equals("declare")
                    || name.equals("scope")
                    || name.equals("description")) {
                element = doc.createElementNS(Constants.NS_JAKARTAEE, name);
                element.appendChild(doc.createTextNode(value));
                variableNode.appendChild(element);
            }
        }
        populateDefault(doc, variableNode, "variable-class",
                "java.lang.String");
        populateDefault(doc, variableNode, "declare", "true");
        populateDefault(doc, variableNode, "scope", "NESTED");
    }

    /**
     * Check to see if there's a short-name element. If not, the TLD is technically invalid, but
     * supply one anyway, and give a warning.
     *
     * @param tagLibrary The tag library being populated
     * @param doc        The TLD DOM to populate.
     * @param root       The root element of the TLD DOM being populated.
     */
    private void checkOrAddShortName(TagLibrary tagLibrary, Document doc,
            Element root) {
        if (root.getElementsByTagNameNS("*", "short-name").getLength() == 0) {
            String prefix = "prefix" + substitutePrefix;
            substitutePrefix++;
            Element shortName = doc.createElementNS(Constants.NS_JAKARTAEE,
                    "short-name");
            shortName.appendChild(doc.createTextNode(prefix));
            root.appendChild(shortName);
            println("WARNING: "
                    + tagLibrary.getPathDescription()
                    + " is missing a short-name element.  Using "
                    + prefix + ".");
        }
    }

    /**
     * Check for empty attribute types and fill in the correct value. This is more difficult for the
     * XSLT transform to do since the default is different depending on whether it is a fragment
     * attribute or not.
     *
     * @param doc  The TLD DOM to populate.
     * @param root The root element of the TLD DOM being populated.
     */
    private void checkOrAddAttributeType(Document doc, Element root) {
        NodeList tagNodes = root.getElementsByTagNameNS("*", "tag");
        for (int i = 0; i < tagNodes.getLength(); i++) {
            Element tagElement = (Element) tagNodes.item(i);
            NodeList attributeNodes
                    = tagElement.getElementsByTagNameNS("*", "attribute");
            for (int j = 0; j < attributeNodes.getLength(); j++) {
                Element attributeElement = (Element) attributeNodes.item(j);
                if (attributeElement.getElementsByTagNameNS("*",
                        "type").getLength() == 0) {
                    // No attribute type specified.
                    String defaultType = "java.lang.String";

                    // Check if there is a fragment element set to true:
                    String fragment = findElementValue(attributeElement,
                            "fragment");
                    if (fragment != null
                            && (fragment.trim().equalsIgnoreCase("true")
                            || fragment.trim().equalsIgnoreCase("yes"))) {
                        defaultType = "javax.servlet.jsp.tagext.JspFragment";
                    }

                    // Create <type> element and append to attribute
                    Element typeElement = doc.createElementNS(
                            Constants.NS_JAKARTAEE, "type");
                    typeElement.appendChild(
                            doc.createTextNode(defaultType));
                    attributeElement.appendChild(typeElement);
                }
            }
        }
    }

    /**
     * Generates all overview files, summarizing all TLDs.
     *
     * @throws TransformerFactoryConfigurationError Thrown in case of {@linkplain
     * java.util.ServiceConfigurationError service configuration error} or if the implementation is
     *                                              not available or cannot be instantiated.
     * @throws TransformerConfigurationException    Thrown if there are errors when parsing the
     *                                              {@code Source} or it is not possible to create a
     *                                              {@code Transformer} instance.
     * @throws TransformerException                 If an unrecoverable error occurs during the
     *                                              course of the transformation.
     */
    private void generateOverview() throws TransformerFactoryConfigurationError,
            TransformerConfigurationException, TransformerException {

        generatePage(outputDirectory.resolve("index.html"), RESOURCE_PATH + "/index.html.xsl");
        generatePage(outputDirectory.resolve("help-doc.html"),
                RESOURCE_PATH + "/help-doc.html.xsl");
        generatePage(outputDirectory.resolve("overview-frame.html"),
                RESOURCE_PATH + "/overview-frame.html.xsl");
        generatePage(outputDirectory.resolve("alltags-frame.html"),
                RESOURCE_PATH + "/alltags-frame.html.xsl");
        generatePage(outputDirectory.resolve("alltags-noframe.html"),
                RESOURCE_PATH + "/alltags-noframe.html.xsl");
        generatePage(outputDirectory.resolve("overview-summary.html"),
                RESOURCE_PATH + "/overview-summary.html.xsl");
    }

    /**
     * Generates all the detail folders for each TLD.
     *
     * @throws IOException          if an I/O error has occurred
     * @throws TransformerException If an unrecoverable error occurs during the course of the
     *                              transformation.
     * @throws GeneratorException   any error during generation
     */
    private void generateTldDetail() throws IOException, TransformerException, GeneratorException {
        ArrayList<String> shortNames = new ArrayList<>();
        Element root = summaryTld.getDocumentElement();
        NodeList taglibs = root.getElementsByTagNameNS("*", "taglib");
        int size = taglibs.getLength();
        for (int i = 0; i < size; i++) {
            Element taglib = (Element) taglibs.item(i);
            String shortName = findElementValue(taglib, "short-name");
            String displayName = findElementValue(taglib, "display-name");
            if (shortNames.contains(shortName)) {
                throw new GeneratorException("Two tag libraries exist with the same short-name '"
                        + shortName + "'.  This is not yet supported.");
            }
            String name = displayName;
            if (name == null) {
                name = shortName;
            }
            println("Generating docs for " + name + "...");
            shortNames.add(shortName);
            Path outDir = outputDirectory.resolve(shortName);
            Files.createDirectories(outDir);

            // Generate information for each TLD:
            generateTldDetail(outDir, shortName);

            // Generate information for each tag:
            NodeList tags = taglib.getElementsByTagNameNS("*", "tag");
            int numTags = tags.getLength();
            for (int j = 0; j < numTags; j++) {
                Element tag = (Element) tags.item(j);
                String tagName = findElementValue(tag, "name");
                generateTagDetail(outDir, shortName, tagName);
            }

            // Generate information for each tag-file:
            NodeList tagFiles = taglib.getElementsByTagNameNS("*", "tag-file");
            int numTagFiles = tagFiles.getLength();
            for (int j = 0; j < numTagFiles; j++) {
                Element tagFile = (Element) tagFiles.item(j);
                String tagFileName = findElementValue(tagFile, "name");
                generateTagDetail(outDir, shortName, tagFileName);
            }

            // Generate information for each function:
            NodeList functions = taglib.getElementsByTagNameNS("*", "function");
            int numFunctions = functions.getLength();
            for (int j = 0; j < numFunctions; j++) {
                Element function = (Element) functions.item(j);
                String functionName = findElementValue(function, "name");
                generateFunctionDetail(outDir, shortName, functionName);
            }
        }
    }

    /**
     * Generates the detail content for the tag library with the given short-name. Files will be
     * placed in outdir.
     *
     * @param outDir    the output directory for generated file
     * @param shortName the short-name of the tag library
     *
     * @throws IOException          if an I/O error has occurred
     * @throws TransformerException If an unrecoverable error occurs during the course of the
     *                              transformation.
     */
    private void generateTldDetail(Path outDir, String shortName) throws IOException,
            TransformerException {

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("tlddoc-shortName", shortName);

        generatePage(outDir.resolve("tld-frame.html"),
                RESOURCE_PATH + "/tld-frame.html.xsl", parameters);
        generatePage(outDir.resolve("tld-summary.html"),
                RESOURCE_PATH + "/tld-summary.html.xsl", parameters);
    }

    /**
     * Generates the detail content for the tag with the given name in the tag library with the
     * given short-name. Files will be placed in outdir.
     *
     * @param outDir    the output directory for generated file
     * @param shortName the short-name of the tag library
     * @param tagName   the tag-name of the tag library
     *
     * @throws IOException          if an I/O error has occurred
     * @throws TransformerException If an unrecoverable error occurs during the course of the
     *                              transformation.
     */
    private void generateTagDetail(Path outDir, String shortName, String tagName) throws
            IOException, TransformerException {

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("tlddoc-shortName", shortName);
        parameters.put("tlddoc-tagName", tagName);

        generatePage(outDir.resolve(tagName + ".html"),
                RESOURCE_PATH + "/tag.html.xsl", parameters);
    }

    /**
     * Generates the detail content for the function with the given name in the tag library with the
     * given short-name. Files will be placed in outdir.
     *
     * @param outDir       the output directory for generated file
     * @param shortName    the short-name of the tag library
     * @param functionName the function-name of the tag library
     *
     * @throws IOException          if an I/O error has occurred
     * @throws TransformerException If an unrecoverable error occurs during the course of the
     *                              transformation.
     */
    private void generateFunctionDetail(Path outDir, String shortName, String functionName) throws
            IOException, TransformerException {

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("tlddoc-shortName", shortName);
        parameters.put("tlddoc-functionName", functionName);

        generatePage(outDir.resolve(functionName + ".fn.html"),
                RESOURCE_PATH + "/function.html.xsl", parameters);
    }

    /**
     * Searches through the given element and returns the value of the body of the element. Returns
     * {@code null} if the element was not found.
     *
     * @param parent  the element to get the value
     * @param tagName the tag-name of the tag library
     *
     * @return the value of the body of the element
     */
    private String findElementValue(Element parent, String tagName) {
        String result = null;
        NodeList elements = parent.getElementsByTagNameNS("*", tagName);
        if (elements.getLength() >= 1) {
            Element child = (Element) elements.item(0);
            Node body = child.getFirstChild();
            if (body.getNodeType() == Node.TEXT_NODE) {
                result = body.getNodeValue();
            }
        }
        return result;
    }

    /**
     * Generates the given page dynamically, by running the summary document through the given XSLT
     * transform. Assumes no parameters.
     *
     * @param outFile  The target file
     * @param inputXsl The stylesheet to use for the transformation
     *
     * @throws TransformerFactoryConfigurationError Thrown in case of {@linkplain
     * java.util.ServiceConfigurationError service configuration error} or if the implementation is
     *                                              not available or cannot be instantiated.
     * @throws TransformerConfigurationException    Thrown if there are errors when parsing the
     *                                              {@code Source} or it is not possible to create a
     *                                              {@code Transformer} instance.
     * @throws TransformerException                 If an unrecoverable error occurs during the
     *                                              course of the transformation.
     */
    private void generatePage(Path outFile, String inputXsl) throws
            TransformerFactoryConfigurationError, TransformerConfigurationException,
            TransformerException {

        generatePage(outFile, inputXsl, null);
    }

    /**
     * Generates the given page dynamically, by running the summary document through the given XSLT
     * transform.
     *
     * @param outFile    The target file
     * @param inputXsl   The stylesheet to use for the transformation
     * @param parameters String key and Object value pairs to pass to the transformation.
     *
     * @throws TransformerFactoryConfigurationError Thrown in case of {@linkplain
     * java.util.ServiceConfigurationError service configuration error} or if the implementation is
     *                                              not available or cannot be instantiated.
     * @throws TransformerConfigurationException    Thrown if there are errors when parsing the
     *                                              {@code Source} or it is not possible to create a
     *                                              {@code Transformer} instance.
     * @throws TransformerException                 If an unrecoverable error occurs during the
     *                                              course of the transformation.
     */
    private void generatePage(Path outFile, String inputXsl, Map<String, String> parameters) throws
            TransformerFactoryConfigurationError, TransformerConfigurationException,
            TransformerException {

        InputStream xsl = getResourceAsStream(inputXsl);
        Transformer transformer = TransformerFactory.newInstance().newTransformer(
                new StreamSource(xsl));
        if (parameters != null) {
            for (Entry<String, String> entry : parameters.entrySet()) {
                transformer.setParameter(entry.getKey(), entry.getValue());
            }
        }
        transformer.transform(new DOMSource(summaryTld), new StreamResult(outFile.toFile()));
    }

    /**
     * Copy the given resource to the given output file. The classloader that loaded TldDocGenerator
     * will be used to find the resource. If xsltDirectory is not null, the files are copied from
     * that directory instead.
     *
     * @param outputFile The destination file
     * @param resource   The resource to copy, starting with '/'
     *
     * @throws IOException if an I/O error has occurred
     */
    private void copyResourceToFile(Path outputFile, String resource) throws IOException {
        try (InputStream in = getResourceAsStream(resource)) {
            Files.copy(in, outputFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Outputs the given message to {@code stdout}, only if {@code !quiet}.
     *
     * @param message the message to {@code stdout}
     */
    private void println(String message) {
        if (!quiet) {
            System.out.println(message);
        }
    }

    /**
     * If {@code xsltDirectory} is {@code null}, obtains an {@code InputStream} of the given
     * resource from {@code RESOURCE_PATH}, using the class loader that loaded
     * {@code TldDocGenerator}. Otherwise, finds the file in {@code xsltDirectory} and defaults to
     * the default stylesheet if it has not been overridden.
     *
     * @param resource must start with {@code RESOURCE_PATH}.
     *
     * @return An InputStream for the given resource.
     */
    private InputStream getResourceAsStream(String resource) {
        InputStream result = null;

        if (xsltDirectory != null) {
            Path resourceFile = xsltDirectory.resolve(
                    resource.substring(RESOURCE_PATH.length() + 1));
            try {
                result = Files.newInputStream(resourceFile);
            } catch (IOException e) {
                // result will be null and we'll default to default stylesheet
                println("XSLT-Directory not found, use default-stylesheet: " + e.getMessage());
            }
        }

        if (result == null) {
            result = TldDocGenerator.class.getResourceAsStream(resource);
        }

        return result;
    }

    /**
     * Displays a "success" message.
     */
    private void outputSuccessMessage() {
        println("\nDocumentation generated.");
    }
}
