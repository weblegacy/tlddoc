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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Implicit Tag Library for a directory of tag files.
 *
 * @author  mroth
 */
public class TagDirImplicitTagLibrary
    extends TagLibrary
{
    /**
     * The directory containing the tag files
     */
    final private File dir;

    /**
     * Creates a new instance of {@link TagDirImplicitTagLibrary}
     *
     * @param dir directory containing the tag files
     */
    public TagDirImplicitTagLibrary( File dir ) {
        this.dir = dir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPathDescription() {
        return dir.getAbsolutePath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getResource(String path)
        throws IOException
    {
        InputStream result = null;

        // Start from the tag directory and backtrack,
        // using the path as a relative path.
        //   For example:
        //      TLD:  /home/mroth/test/sample/WEB-INF/tags/mytags
        //      path: /WEB-INF/tags/mytags/tag1.tag

        File dir_ = this.dir;
        if( path.startsWith( "/" ) ) {
            path = path.substring( 1 );
        }
        File look = null;
        while( dir_ != null && !(look = new File( dir_, path )).exists() ) {
            dir_ = dir_.getParentFile();
        }

        if( look != null && look.exists() ) {
            // Found it:
            result = new FileInputStream( look );
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Document getTLDDocument(DocumentBuilder documentBuilder)
        throws IOException, SAXException, TransformerException
    {
        Document result = documentBuilder.newDocument();

        // Determine path from root of web application (this is somewhat of
        // a guess):
        String path = dir.getAbsolutePath().replace( File.separatorChar, '/' );
        int index = path.indexOf( "/WEB-INF/" );
        if( index != -1 ) {
            path = path.substring( index );
        }
        else {
            path = "unknown";
        }
        if( !path.endsWith( "/" ) ) path += "/";

        // Create root taglib node:
        Element taglibElement = createRootTaglibNode( result, path );

        // According to the JSP 2.0 specification:
        // A <tag-file> element is considered to exist for each tag file in
        // this directory, with the following sub-elements:
        //    - The <name> for each is the filename of the tag file,
        //      without the .tag extension.
        //    - The <path> for each is the path of the tag file, relative
        //      to the root of the web application.
        File[] files = this.dir.listFiles();
        if( files != null ) {
            for( File file : files ) {
                final String fileName = file.getName();
                final String fileNameLower = fileName.toLowerCase();
                if( !file.isDirectory() &&
                    ( fileNameLower.endsWith( ".tag" ) ||
                      fileNameLower.endsWith( ".tagx" ) ) )
                {
                    String tagName = fileName.substring( 0,
                        fileName.lastIndexOf( '.' ) );
                    String tagPath = path + fileName;

                    Element tagFileElement = result.createElement( "tag-file" );
                    Element nameElement = result.createElement( "name" );
                    nameElement.appendChild( result.createTextNode( tagName ) );
                    tagFileElement.appendChild( nameElement );
                    Element pathElement = result.createElement( "path" );
                    pathElement.appendChild( result.createTextNode( tagPath ) );
                    tagFileElement.appendChild( pathElement );
                    taglibElement.appendChild( tagFileElement );
                }
            }
        }

        // Output implicit tag library, as a test.
        StringWriter buffer = new StringWriter();
        Transformer transformer =
            TransformerFactory.newInstance().newTransformer();
        transformer.transform( new DOMSource( result ),
            new StreamResult( buffer ) );
        result = documentBuilder.parse( new InputSource( new StringReader(
            buffer.toString() ) ) );

        return result;

    }

    /**
     * Creates an implicit tag library root node, with default values.
     * Shared by WARTagDirImplicitTagLibrary.
     *
     * @param result XML-document to add new tag-element
     * @param path path to the TLD Files
     *
     * @return new created tag library root node
     */
    protected static Element createRootTaglibNode( Document result,
        String path )
    {
        Element taglibElement = result.createElementNS(
            Constants.NS_JAVAEE, "taglib" );
        // JDK 1.4 does not add xmlns for some reason - add it manually:
        taglibElement.setAttributeNS( "http://www.w3.org/2000/xmlns/",
            "xmlns", Constants.NS_JAVAEE );
        taglibElement.setAttributeNS( "http://www.w3.org/2000/xmlns/",
            "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance" );
        taglibElement.setAttributeNS(
            "http://www.w3.org/2001/XMLSchema-instance",
            "xsi:schemaLocation",
            Constants.NS_JAVAEE +
            " http://java.sun.com/xml/ns/javaee/web-jsptaglibrary_2_1.xsd" );
        taglibElement.setAttribute( "version", "2.1" );
        result.appendChild( taglibElement );

        // Add <description>
        Element descriptionElement = result.createElement( "description" );
        descriptionElement.appendChild( result.createTextNode(
            "Implicit tag library for tag file directory " + path ) );
        taglibElement.appendChild( descriptionElement );

        // Add <tlib-version> of 1.0
        Element tlibVersionElement = result.createElement( "tlib-version" );
        tlibVersionElement.appendChild( result.createTextNode( "1.0" ) );
        taglibElement.appendChild( tlibVersionElement );

        // According to the JSP 2.0 specification, <short-name> is derived
        // from the directory name.  If the directory is /WEB-INF/tags/, the
        // short name is simply tags.  Otherwise, the full directory path
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
                if( shortName.startsWith( "/WEB-INF/tags" ) ) {
                    shortName = shortName.substring( "/WEB-INF/tags".length() );
                }
                if( shortName.startsWith( "/" ) ) {
                    shortName = shortName.substring( 1 );
                }
                if( shortName.endsWith( "/" ) ) {
                    shortName = shortName.substring( 0, shortName.length() - 1 );
                }
                shortName = shortName.replace( File.separatorChar, '/' );
                shortName = shortName.replace( '/', '-' );
                break;
        }
        Element shortNameElement = result.createElement( "short-name" );
        shortNameElement.appendChild( result.createTextNode( shortName ) );
        taglibElement.appendChild( shortNameElement );

        Element uriElement = result.createElement( "uri" );
        uriElement.appendChild( result.createTextNode( path ) );
        taglibElement.appendChild( uriElement );

        return taglibElement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        // Nothing to do
    }

}