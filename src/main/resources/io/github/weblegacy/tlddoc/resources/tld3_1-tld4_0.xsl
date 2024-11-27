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

  Identity transformation (changing from the Jakarta EE 3.1 namespace
  to the Jakarta EE 4.0 namespace), added for flexibility.

  1. Change the <taglib> element to read as follows:
     <taglib xmlns="https://jakarta.ee/xml/ns/jakartaee""
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
         https://jakarta.ee/xml/ns/jakartaee/web-jsptaglibrary_4_0.xsd">

  Author: Stefan Graff

-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:jakartaee="https://jakarta.ee/xml/ns/jakartaee"
    xmlns="https://jakarta.ee/xml/ns/jakartaee">
    <xsl:output method="xml" indent="yes" />

    <xsl:template match="/jakartaee:taglib">
        <xsl:element name="taglib">
            <xsl:attribute name="xsi:schemaLocation"
                namespace="http://www.w3.org/2001/XMLSchema-instance">https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-jsptaglibrary_4_0.xsd</xsl:attribute>
            <xsl:attribute name="version">4.0</xsl:attribute>
            <xsl:apply-templates select="*" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="jakartaee:*">
        <xsl:element name="{local-name()}">
            <xsl:copy-of select="@*" />
            <xsl:apply-templates />
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>