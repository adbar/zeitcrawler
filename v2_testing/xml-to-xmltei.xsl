<!--	This is part of the Zeitcrawler (http://code.google.com/p/zeitcrawler/).
	Copyright (C) Adrien Barbaresi 2014.
	This is free software, released under the GNU GPL v3 license (http://www.gnu.org/licenses/gpl.html).

	WORK IN PROGRESS ! This is an experimental software component.
-->

<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.tei-c.org/ns/1.0" version="1.0">
<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

<!--<xsl:strip-space elements="classCode"/>-->
<!-- normalize-space() -->


    <!-- Main document structure and more or less directly transferred elements -->

    <xsl:template match="/">

    <TEI xmlns="http://www.tei-c.org/ns/1.0">

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
                    <respStmt>
                      <resp/>
                      <orgName>Berlin-Brandenburgische Akademie der Wissenschaften</orgName>
                    </respStmt>
                </titleStmt>

                <publicationStmt>
                  <publisher>Berlin-Brandenburgische Akademie der Wissenschaften</publisher>
                </publicationStmt>

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
                            <editor>
                                <persName>
                                    <xsl:value-of select="//last_modified_by"/>
                                </persName>
                            </editor>
                        </titleStmt>

                        <publicationStmt>
                            <date type="publication">
                                <xsl:value-of select="//date"/> 
                            </date>
                            <idno type="URL">
                                <xsl:value-of select="//source"/> 
                            </idno>
                            <idno type="UUID">
                                <xsl:value-of select="//uuid"/> 
                            </idno>
                        </publicationStmt>

                        <seriesStmt>
                            <title>DIE ZEIT</title>
                            <xsl:apply-templates select="/root/head/year"/>
                            <xsl:apply-templates select="/root/head/volume"/>
                            <xsl:apply-templates select="/root/head/ressort"/>
                            <xsl:apply-templates select="/root/head/subressort"/>
                        </seriesStmt>

                    </biblFull>
                </sourceDesc>
            </fileDesc>

            <profileDesc>
                <textClass>
                    <!-- Metatype -->
                    <xsl:apply-templates select="/root"/>

                    <!-- Keywords -->
                    <keywords>
                        <xsl:for-each select="/root/head/keyword">
                            <term type="keyword">
                                <xsl:value-of select="."/>
                            </term>
                        </xsl:for-each>

                        <xsl:apply-templates select="/root/head/tag"/>

                    </keywords>
                </textClass>
            </profileDesc>



        </teiHeader>
        <text>
            <body>
                <xsl:apply-templates select="//body"/>
            </body>
        </text>

    </TEI>
    </xsl:template>


    <!-- Everything that has to be transformed noticeably -->

    <!-- Header -->

    <xsl:template match="/root/head/year | /root/head/volume | /root/head/ressort | /root/head/subressort">
        <biblScope unit="{local-name()}">
            <xsl:apply-templates select="node()"/>
        </biblScope>
    </xsl:template>

    <xsl:template match="/root">
        <xsl:choose>
            <xsl:when test="/root/metatype">
                <classCode scheme=""><xsl:value-of select="/root/metatype"/></classCode>
            </xsl:when>
            <xsl:otherwise>
                <classCode scheme="">missing</classCode>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="/root/head/tag">
        <term type="keyword">
            <measure quantity="{@score}"/>
            <xsl:apply-templates select="node()"/>
        </term>
    </xsl:template>


    <!-- Body and actual text -->

    <xsl:template match="body">
        <xsl:apply-templates select="@* | node()"/>
    </xsl:template>

    <xsl:template match="title">
        <floatingText type="title">
            <body>
                <p><xsl:apply-templates select="@* | node()"/></p>
            </body>
        </floatingText>
    </xsl:template>

    <xsl:template match="intertitle">
        <floatingText type="intertitle">
            <body>
                <p><xsl:apply-templates select="@* | node()"/></p>
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
            <xsl:apply-templates select="@* | node()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="a">
        <ref target="{@href}">
            <xsl:apply-templates select="node()"/>
        </ref>
    </xsl:template>


</xsl:stylesheet>
