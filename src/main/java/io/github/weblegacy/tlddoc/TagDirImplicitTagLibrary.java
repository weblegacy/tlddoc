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

import io.github.weblegacy.tlddoc.main.TagLibrary;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Implicit Tag Library for a directory of tag files.
 *
 * @author mroth
 */
public class TagDirImplicitTagLibrary implements TagLibrary {

    /**
     * The directory containing the tag files.
     */
    private final Path dir;

    /**
     * Creates a new instance of {@link TagDirImplicitTagLibrary}.
     *
     * @param dir directory containing the tag files
     */
    public TagDirImplicitTagLibrary(Path dir) {
        this.dir = dir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPathDescription() {
        return dir.toAbsolutePath().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getResource(String path) throws IOException {
        return Utils.backtrackPath(dir, path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Document getTldDocument(DocumentBuilder documentBuilder) throws IOException,
            SAXException, TransformerFactoryConfigurationError, TransformerException {

        Document result = documentBuilder.newDocument();

        // Determine path from root of web application (this is somewhat of
        // a guess):
        String path = Utils.pathString(dir);
        int index = path.indexOf("/WEB-INF/");
        if (index != -1) {
            path = path.substring(index);
        } else {
            path = "unknown";
        }
        if (!path.endsWith("/")) {
            path += "/";
        }

        // Create root taglib node:
        Element taglibElement = createRootTaglibNode(result, path);

        // According to the JSP 2.0 specification:
        // A <tag-file> element is considered to exist for each tag file in
        // this directory, with the following sub-elements:
        //    - The <name> for each is the filename of the tag file,
        //      without the .tag extension.
        //    - The <path> for each is the path of the tag file, relative
        //      to the root of the web application.
        final String p = path;
        try (DirectoryStream<Path> files = Files.newDirectoryStream(dir, Utils::isTag)) {
            for (Path file : files) {
                final Path fn = file.getFileName();
                if (fn == null) {
                    continue;
                }

                final String fileName = fn.toString();
                final String tagName = fileName.substring(0, fileName.lastIndexOf('.'));
                final String tagPath = p + fileName;

                createTagEntry(result, tagName, tagPath, taglibElement);
            }
        }

        return recreateDocument(documentBuilder, result);
    }

    /**
     * Creates an implicit tag library root node, with default values. Shared by
     * WarTagDirImplicitTagLibrary.
     *
     * @param result XML-document to add new tag-element
     * @param path   path to the TLD Files
     *
     * @return new created tag library root node
     */
    static Element createRootTaglibNode(Document result, String path) {
        Element taglibElement = result.createElementNS(Constants.NS_JAKARTAEE, "taglib");
        // JDK 1.4 does not add xmlns for some reason - add it manually:
        taglibElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
                "xmlns", Constants.NS_JAKARTAEE);
        taglibElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
                "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        taglibElement.setAttributeNS(
                "http://www.w3.org/2001/XMLSchema-instance",
                "xsi:schemaLocation",
                Constants.NS_JAKARTAEE
                + " https://jakarta.ee/xml/ns/jakartaee/web-jsptaglibrary_3_0.xsd");
        taglibElement.setAttribute("version", "3.0");
        result.appendChild(taglibElement);

        // Add <description>
        Element descriptionElement = result.createElement("description");
        descriptionElement.appendChild(result.createTextNode(
                "Implicit tag library for tag file directory " + path));
        taglibElement.appendChild(descriptionElement);

        // Add <tlib-version> of 1.0
        Element tlibVersionElement = result.createElement("tlib-version");
        tlibVersionElement.appendChild(result.createTextNode("1.0"));
        taglibElement.appendChild(tlibVersionElement);

        // According to the JSP 2.0 specification, <short-name> is derived
        // from the directory name. If the directory is /WEB-INF/tags/, the
        // short name is simply tags. Otherwise, the full directory path
        // (relative to the web application) is taken, minus the
        // /WEB-INF/tags/ prefix. Then, all / characters are replaced
        // with -, which yields the short name. Note that short names are
        // not guaranteed to be unique.
        String shortName;
        switch (path) {
            case "unknown":
                shortName = path;
                break;
            case "/WEB-INF/tags":
            case "/WEB-INF/tags/":
                shortName = "tags";
                break;
            default:
                shortName = path;
                if (shortName.startsWith("/WEB-INF/tags")) {
                    shortName = shortName.substring("/WEB-INF/tags".length());
                }
                if (shortName.startsWith("/")) {
                    shortName = shortName.substring(1);
                }
                if (shortName.endsWith("/")) {
                    shortName = shortName.substring(0, shortName.length() - 1);
                }
                shortName = shortName.replace('/', '-');
                break;
        }
        Element shortNameElement = result.createElement("short-name");
        shortNameElement.appendChild(result.createTextNode(shortName));
        taglibElement.appendChild(shortNameElement);

        Element uriElement = result.createElement("uri");
        uriElement.appendChild(result.createTextNode(path));
        taglibElement.appendChild(uriElement);

        return taglibElement;
    }

    /**
     * Creates an tag-entry into tag library root node. Shared by WarTagDirImplicitTagLibrary.
     *
     * @param result        XML-document to add new tag-element
     * @param tagName       name of the tag-entry
     * @param tagPath       path of the tag-entry
     * @param taglibElement tag library root node
     *
     * @throws DOMException if an XML error has occurred
     */
    static void createTagEntry(final Document result, final String tagName, final String tagPath,
            Element taglibElement) throws DOMException {

        final Element tagFileElement = result.createElement("tag-file");
        final Element nameElement = result.createElement("name");
        nameElement.appendChild(result.createTextNode(tagName));
        tagFileElement.appendChild(nameElement);

        final Element pathElement = result.createElement("path");
        pathElement.appendChild(result.createTextNode(tagPath));
        tagFileElement.appendChild(pathElement);
        taglibElement.appendChild(tagFileElement);
    }

    /**
     * Recreates the XML document. JDK 1.4 does not correctly import the node into the tree, so
     * simulate reading this entry from a file. There might be a better / more efficient way to do
     * this, but this works.
     *
     * @param documentBuilder {@code DocumentBuilder} to obtain DOM Document for creating an
     *                        XML-document.
     * @param result          XML-document to recreate
     *
     * @return the recreated XML-document
     *
     * @throws IOException                          if an I/O error has occurred
     * @throws SAXException                         If any parse errors occur.
     * @throws TransformerFactoryConfigurationError Thrown in case of {@linkplain
     * java.util.ServiceConfigurationError service configuration error} or if the implementation is
     *                                              not available or cannot be instantiated.
     * @throws TransformerException                 If an unrecoverable error occurs during the
     *                                              course of the transformation.
     */
    static Document recreateDocument(final DocumentBuilder documentBuilder, final Document result)
            throws IOException, TransformerFactoryConfigurationError, TransformerException,
            SAXException {

        final StringWriter buffer = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(result), new StreamResult(buffer));

        return documentBuilder.parse(new InputSource(new StringReader(buffer.toString())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        // Nothing to do
    }
}
