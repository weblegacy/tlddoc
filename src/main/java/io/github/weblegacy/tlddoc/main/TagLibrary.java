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

package io.github.weblegacy.tlddoc.main;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Base class for a tag library source. Different tag libraries will locate resources, such as tag
 * files, in different ways.
 *
 * @author mroth
 */
public interface TagLibrary extends Closeable {

    /**
     * Returns a String that the user would recognize as a location for this tag library.
     *
     * @return a String that the user would recognize as a location for this tag library
     */
    String getPathDescription();

    /**
     * Returns a Document of the effective tag library descriptor for this tag library. This might
     * come from a file or be implicitly generated.
     *
     * @param documentBuilder {@code DocumentBuilder} to obtain DOM Document for creating an
     *                        XML-document.
     *
     * @return created XML-Document
     *
     * @throws IOException                          if an I/O error has occurred
     * @throws SAXException                         If any parse errors occur.
     * @throws TransformerFactoryConfigurationError Thrown in case of {@linkplain
     * java.util.ServiceConfigurationError service configuration error} or if the implementation is
     *                                              not available or cannot be instantiated.
     * @throws TransformerException                 If an unrecoverable error occurs during the
     *                                              course of the transformation.
     */
    Document getTldDocument(DocumentBuilder documentBuilder) throws IOException,
            SAXException, TransformerFactoryConfigurationError, TransformerException;

    /**
     * Returns an input stream for the given resource, or {@code null} if the resource could not be
     * found.
     *
     * @param path the path to the resource
     *
     * @return the input stream for the given resource or {@code null} if the resource could not be
     *         found
     *
     * @throws IOException if an I/O error has occurred
     */
    InputStream getResource(String path) throws IOException;
}
