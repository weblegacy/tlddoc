/*
 * <license>
 * Copyright (c) 2003-2004, Sun Microsystems, Inc.
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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * ROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </license>
 */

package com.sun.tlddoc;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Main entry point for TLDDoc.  Allows commandline access.
 *
 * @author Mark Roth
 */
public class TLDDoc {
    private static final String USAGE =
        "Tag Library Documentation Generator " + Constants.VERSION +
        " by Mark Roth, Sun Microsystems, Inc.\n" +
        "Usage: tlddoc [options] taglib1 [taglib2 [taglib3 ...]]\n" +
        "Options:\n" +
        "  -help                  Displays this help message\n" +
        "  -xslt <directory>      Use the XSLT files in the given directory\n" +
        "                         instead of the defaults.\n" +
        "  -d <directory>         Destination directory for output files\n" +
        "                         (defaults to new dir called 'out')\n" +
        "  -doctitle <html-code>  Include title for the TLD index (first) " +
            "page\n" +
        "  -windowtitle <text>    Browser window title\n" +
        "  -q                     Quiet Mode\n" +
        "taglib{1,2,3,...}:\n" +
        "  * If the path is a file that ends in .tld, process an\n" +
        "    individual TLD file.\n" +
        "  * If the path is a file that ends in .jar, process a\n" +
        "    tag library JAR file.\n" +
        "  * If the path is a file that ends in .war, process all\n" +
        "    tag libraries in this web application.\n" +
        "  * If the path is a directory that includes /WEB-INF/tags\n" +
        "    process the implicit tag library for the given directory\n" +
        "    of tag files.\n" +
        "  * If the path is a directory containing a WEB-INF subdirectory,\n" +
        "    process all tag libraries in this web application.\n" +
        "  * Otherwise, error.";

    public static void main( String[] args ) {
        TLDDocGenerator generator = new TLDDocGenerator();

        Iterator<String> iter = Arrays.asList( args ).iterator();
        boolean atLeastOneTLD = false;

        try {
            while( iter.hasNext() ) {
                String arg = iter.next();

                switch (arg) {
                    case "-xslt":
                        arg = iter.next();
                        generator.setXSLTDirectory( new File( arg ) );
                        break;
                    case "-d":
                        arg = iter.next();
                        generator.setOutputDirectory( new File( arg ) );
                        break;
                    case "-help":
                        usage( null );
                        break;
                    case "-q":
                        generator.setQuiet( true );
                        break;
                    case "-doctitle":
                        arg = iter.next();
                        generator.setDocTitle( arg );
                        break;
                    case "-windowtitle":
                        arg = iter.next();
                        generator.setWindowTitle( arg );
                        break;
                    case "-webapp":
                        arg = iter.next();
                        File dir = new File( arg );
                        if( dir.exists() ) {
                            atLeastOneTLD = true;
                            generator.addWebApp( dir );
                        }
                        else {
                            usage( "Web app not found: " + arg );
                        }
                        break;
                    case "-jar":
                        arg = iter.next();
                        File jar = new File( arg );
                        if( jar.exists() ) {
                            atLeastOneTLD = true;
                            generator.addJAR( jar );
                        }
                        else {
                            usage( "JAR not found: " + arg );
                        }
                        break;
                    case "-tagdir":
                        arg = iter.next();
                        File tagdir = new File( arg );
                        if( tagdir.exists() ) {
                            atLeastOneTLD = true;
                            generator.addTagDir( tagdir );
                        }
                        else {
                            usage( "Tag Directory not found: " + arg );
                        }
                        break;
                    default:
                        File f = new File( arg );
                        if( f.exists() ) {
                                if( f.getName().toLowerCase().endsWith( ".tld" ) ) {
                                    // If the path is a file that ends in .tld,
                                    // process an individual TLD file.
                                    generator.addTLD( f );
                                    atLeastOneTLD = true;
                                }
                                else if( f.getName().toLowerCase().endsWith( ".jar" ) )
                                {
                                    // If the path is a file that ends in .jar,
                                    // process a tag library JAR file.
                                    generator.addJAR( f );
                                    atLeastOneTLD = true;
                                }
                                else if( f.getName().toLowerCase().endsWith( ".war" ) )
                                {
                                    // If the path is a file that ends in .war,
                                    // process all tag libraries in this web app.
                                    generator.addWAR( f );
                                    atLeastOneTLD = true;
                                }
                                else if( f.isDirectory() ) {
                                    // If the path is a directory that includes
                                    // /WEB-INF/tags process the implicit tag library
                                    // for the given directory of tag files.
                                    if( f.getAbsolutePath().replace(
                                        File.separatorChar, '/' ).contains(
                                        "/WEB-INF/tags" ) )
                                    {
                                        generator.addTagDir( f );
                                        atLeastOneTLD = true;
                                    }
                                    else {
                                        boolean foundWebInf = false;
                                        final File files[] = f.listFiles();
                                        if (files != null) {
                                            for( File file : files ) {
                                                if( file.getName().toUpperCase().
                                                    equals( "WEB-INF" ) &&
                                                    file.isDirectory() )
                                                {
                                                    foundWebInf = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if( foundWebInf ) {
                                            generator.addWebApp( f );
                                            atLeastOneTLD = true;
                                        }
                                        else {
                                            usage( "Cannot determine tag library " +
                                                "type for " + f.getAbsolutePath() );
                                        }
                                    }
                                }
                                else {
                                    usage( "Cannot determine tag library " +
                                        "type for " + f.getAbsolutePath() );
                                }
                            }
                            else {
                                usage( "File/directory not found: " + arg );
                            }
                            break;
                        }
            }
            if( !atLeastOneTLD ) {
                usage( "Please specify at least one TLD file." );
            }
        }
        catch( NoSuchElementException e ) {
            usage( "Invalid Syntax." );
        }

        try {
            generator.generate();
        }
        catch( GeneratorException e ) {
            e.printStackTrace( System.err );
            System.exit( 1 );
        }
    }

    private static void usage( String message ) {
        if( message != null ) System.out.println( "Error: " + message );
        System.out.println( USAGE );
        System.exit( 0 );
    }
}