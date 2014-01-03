<!--	This is part of the Zeitcrawler (http://code.google.com/p/zeitcrawler/).
	Copyright (C) Adrien Barbaresi 2014.
	This is free software, released under the GNU GPL v3 license (http://www.gnu.org/licenses/gpl.html).

	WORK IN PROGRESS ! This is an experimental software component.
-->

<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" encoding="utf-8" indent="yes"/>
<!--<xsl:preserve-space elements="*"/>-->

<!-- recursive transform may be better... -->


<xsl:strip-space elements="floatingText body" />

    <xsl:template match="/">

    <TEI encoding="utf-8" xmlns="http://www.tei-c.org/ns/1.0">

        <teiHeader>
            <fileDesc>

                <titleStmt>
                    <title>
                        <xsl:value-of select="//date"/>
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
            </fileDesc>

            <sourceDesc>
                <titleStmt>
                    <title type="supertitle">
                        <xsl:value-of select="//supertitle"/> 
                    </title>
                    <title type="main">
                        <xsl:value-of select="//title"/> 
                    </title>
                    <title type="subtitle">
                        <xsl:value-of select="//subtitle"/> 
                    </title>

                    <editor>
                        <persName>
                            <xsl:value-of select="//last_modified_by"/>
                        </persName>
                    </editor>
                    <seriesStmt>
                        <year>
                            <xsl:value-of select="//year"/>
                        </year>
                        <volume>
                            <xsl:value-of select="//volume"/>
                        </volume>
                        <ressort>
                            <xsl:value-of select="//ressort"/>
                        </ressort>
                        <subressort>
                            <xsl:value-of select="//sub_ressort"/>
                        </subressort>
                    </seriesStmt>
                </titleStmt>

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
            </sourceDesc>

            <profileDesc>
                <textClass>
                    <kind>
                        <xsl:value-of select="//metatype"/> 
                    </kind>
                    <xsl:copy-of select="//tag"/>
                    <xsl:copy-of select="//keyword"/>
                </textClass>
            </profileDesc>

        </teiHeader>
<text>
            <body>

                <div type="article">
                    <xsl:for-each select="//body//*">
                        <xsl:choose>
                            <xsl:when test="name() = 'p'">
                                <p>
                                    <xsl:value-of select="."/>
                                </p>
                            </xsl:when>
                            <xsl:when test="name() = 'title'">
                                <floatingText type="title">
                                    <body>
                                        <xsl:value-of select="."/>
                                    </body>
                                </floatingText>
                            </xsl:when>
                            <xsl:when test="name() = 'intertitle'">
                                <floatingText type="intertitle">
                                    <body>
                                        <xsl:value-of select="."/>
                                    </body>
                                </floatingText>
                            </xsl:when>
                        </xsl:choose>
                    </xsl:for-each>
                </div>

            </body>

        </text>
    </TEI>
    </xsl:template>


</xsl:stylesheet>
