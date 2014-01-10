<!--	This is part of the Zeitcrawler (http://code.google.com/p/zeitcrawler/).
	Copyright (C) Adrien Barbaresi 2014.
	This is free software, released under the GNU GPL v3 license (http://www.gnu.org/licenses/gpl.html).

	WORK IN PROGRESS ! This is an experimental software component.
-->

<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.tei-c.org/ns/1.0" encoding="utf-8" version="1.0">
<xsl:output method="xml" encoding="utf-8" indent="yes"/>

<!--<xsl:strip-space elements="*"/>-->
<!-- normalize-space() -->


    <!-- Identity transform 
    <xsl:template match="@*|*|text()|processing-instruction()">
        <xsl:copy>
            <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
        </xsl:copy>
    </xsl:template>-->


    <xsl:template match="/">

    <TEI encoding="utf-8" xmlns="http://www.tei-c.org/ns/1.0">

        <teiHeader>

            <fileDesc>

                <titleStmt>
                    <title type="main">
                        <xsl:value-of select="//title"/>
                    </title>
                    <editor>
                        <persName>
                            <surname>Adrien</surname>
                            <forename>Barbaresi</forename>
                        </persName>
                    </editor>
                </titleStmt>
                <respStmt>
                    <orgName>Berlin-Brandenburgische Akademie der Wissenschaften</orgName>
                </respStmt>


                <sourceDesc>
                    <biblFull>
                        <titleStmt>
                            <xsl:if test="//supertitle">
                                <title type="supertitle">
                                    <xsl:value-of select="//supertitle"/>
                                </title>
                            </xsl:if>
                            
                            <title type="main">
                                <xsl:value-of select="//title"/>
                            </title>
                            <title type="subtitle">
                                <xsl:value-of select="//subtitle"/>
                            </title>
                            <author>
                                <persName>
                                    <xsl:value-of select="//author"/>
                                </persName>
                            </author>
                        </titleStmt>

                        <editionStmt>
                            <editor>
                                <persName>
                                    <xsl:value-of select="//last_modified_by"/>
                                </persName>
                            </editor>
                        </editionStmt>

                        <publicationStmt>
                            <date type="publication">
                                <xsl:value-of select="//date"/> 
                            </date>
                            <url type="web page URL">
                                <xsl:value-of select="//source"/> 
                            </url>
                            <idno type="ZEIT uuid">
                                <xsl:value-of select="//uuid"/> 
                            </idno>
                        </publicationStmt>

                        <seriesStmt>
                            <bibl>
                                <biblScope unit="year">
                                    <xsl:value-of select="//year"/>
                                </biblScope>
                                <biblScope unit="volume">
                                    <xsl:value-of select="//volume"/>
                                </biblScope>
                                <xsl:for-each select="/root/head/ressort">
                                    <term type="ressort">
                                        <xsl:value-of select="."/> <!--<xsl:value-of select="//ressort"/>-->
                                    </term>
                                </xsl:for-each>

                                <xsl:for-each select="/root/head/subressort">
                                    <term type="subressort">
                                        <xsl:value-of select="."/>
                                    </term>
                                </xsl:for-each>
                            </bibl>
                        </seriesStmt>

                    </biblFull>
                </sourceDesc>
            </fileDesc>

            <profileDesc>
                <textClass>
                    <!-- Metatype -->
                    <xsl:choose>
                        <xsl:when test="/root/metatype">
                            <classCode><xsl:value-of select="/root/metatype"/></classCode>
                        </xsl:when>
                        <xsl:otherwise>
                            <classCode>missing</classCode>
                        </xsl:otherwise>
                     </xsl:choose>
                     <!--   <xsl:if test="not(metatype)">
                            <kind>missing</kind>
                        </xsl:if>
                        <xsl:if test="/root/metatype">
                            <kind><xsl:value-of select="//metatype"/></kind>
                        </xsl:if>-->

                    <!-- Keywords -->
                    <keywords>
                        <xsl:for-each select="/root/head/keyword">
                            <term type="keyword">
                                <xsl:value-of select="."/> <!--<xsl:value-of select="//ressort"/>-->
                            </term>
                        </xsl:for-each>
                        <xsl:for-each select="/root/head/tag">
                            <term type="tag">
                                <xsl:value-of select="."/>
                            </term>
                        </xsl:for-each>
                        <!--<xsl:apply-templates select="/root/head/tag"/>
http://www.tei-c.org/release/doc/tei-p5-doc/de/html/ref-att.canonical.html
http://www.tei-c.org/release/doc/tei-p5-doc/de/html/ref-idno.html
http://www.tei-c.org/release/doc/tei-p5-doc/de/html/ref-textClass.html
                        -->
                    </keywords>
                </textClass>
            </profileDesc>



        </teiHeader>
        <text>
            <body>

        <!--<xsl:apply-templates select="body"/>-->
            <xsl:apply-templates select="//body"/>
            </body>
        </text>

    </TEI>



    </xsl:template>


    <!-- Header -->



    <!--<xsl:template match="/root/head/tag">
            <xsl:apply-templates select="node()|@*"/>
        <xsl:element name="tag">

        </xsl:element>
    </xsl:template>-->


    <!-- Body and actual text -->

    <xsl:template match="body">
        <xsl:apply-templates select="node()|@*"/>
    </xsl:template>


    <xsl:template match="title">
        <floatingText type="title">
            <body>
                <xsl:apply-templates select="@* | node()"/>
            </body>
        </floatingText>
    </xsl:template>

    <xsl:template match="intertitle">
        <floatingText type="intertitle">
            <body>
                <xsl:apply-templates select="@* | node()"/>
            </body>
        </floatingText>
    </xsl:template>

    <xsl:template match="p">
        <p>
            <xsl:apply-templates select="@* | node()"/>
        </p>
    </xsl:template>

    <xsl:template match="em | strong">
        <xsl:element name="hi">
            <!--<xsl:value-of select="."/>-->
            <xsl:apply-templates select="@* | node()"/>
        </xsl:element>
    </xsl:template>


</xsl:stylesheet>
