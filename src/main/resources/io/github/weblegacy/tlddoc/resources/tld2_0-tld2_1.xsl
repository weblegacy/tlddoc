<?xml version="1.0" encoding="UTF-8"?>

<!--
  - <license>
  - Copyright (c) 2003-2004, Sun Microsystems, Inc.
  - Copyright (c) 2022-2024, Web-Legacy
  - All rights reserved.
  -
  - Redistribution and use in source and binary forms, with or without
  - modification, are permitted provided that the following conditions are met:
  -
  -     * Redistributions of source code must retain the above copyright
  -       notice, this list of conditions and the following disclaimer.
  -     * Redistributions in binary form must reproduce the above copyright
  -       notice, this list of conditions and the following disclaimer in the
  -       documentation and/or other materials provided with the distribution.
  -     * Neither the name of Sun Microsystems, Inc. nor the names of its
  -       contributors may be used to endorse or promote products derived from
  -       this software without specific prior written permission.
  -
  - THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  - ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  - WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  - DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  - ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  - (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  - LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  - ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  - (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  - SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  - </license>
-->

<!--

  Identity transformation (changing from the J2EE 2.0 namespace
  to the Java EE 2.1 namespace), added for flexibility.

  1. Change the <taglib> element to read as follows:
     <taglib xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
         http://java.sun.com/xml/ns/javaee/web-jsptaglibrary_2_1.xsd">

  Author: Mark Roth

-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:j2ee="http://java.sun.com/xml/ns/j2ee"
                xmlns="http://java.sun.com/xml/ns/javaee">
    <xsl:output method="xml" indent="yes" />

    <xsl:template match="/j2ee:taglib">
        <xsl:element name="taglib">
            <xsl:attribute name="xsi:schemaLocation"
                           namespace="http://www.w3.org/2001/XMLSchema-instance">http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-jsptaglibrary_2_1.xsd</xsl:attribute>
            <xsl:attribute name="version">2.1</xsl:attribute>

            <xsl:apply-templates select="*" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="j2ee:*">
        <xsl:element name="{local-name()}">
            <xsl:copy-of select="@*" />
            <xsl:apply-templates />
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>