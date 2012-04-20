<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="fo">
    <xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="yes"/>
    <xsl:param name="versionParam" select="'1.0'"/>

    <xsl:template match="class">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="simpleA4" page-height="21cm" page-width="29.7cm" margin-top="2cm" margin-bottom="2cm" margin-left="2cm" margin-right="2cm">
                    <fo:region-body/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="simpleA4">
                <fo:flow flow-name="xsl-region-body">
                    <fo:block font-size="16pt" font-weight="bold" space-after="5mm"> <xsl:value-of select="table"/>
                    </fo:block>
                    <fo:block font-size="10pt">
                        <fo:table table-layout="fixed" width="100%" border-collapse="separate">
                            <xsl:apply-templates select="column"/>

                            <fo:table-header>
                                <xsl:apply-templates select="header"/>
                            </fo:table-header>

                            <fo:table-body>
                                <xsl:choose>
                                    <xsl:when test="string(rows)">
                                        <xsl:apply-templates select="rows"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <fo:table-row>
                                            <fo:table-cell>
                                                <fo:block>
                                                </fo:block>
                                            </fo:table-cell>
                                        </fo:table-row>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>

    <xsl:template match="header">
        <fo:table-cell>
            <fo:block font-weight="bold">
                <xsl:value-of select="nameColumn"/>
            </fo:block>
        </fo:table-cell>
    </xsl:template>

    <xsl:template match="rows">
        <fo:table-row>
            <xsl:apply-templates select="row"/>
        </fo:table-row>
    </xsl:template>

    <xsl:template match="row">
        <fo:table-cell>
            <fo:block>
                <xsl:value-of select="value"/>
            </fo:block>
        </fo:table-cell>
    </xsl:template>

    <xsl:template match="column">
        <xsl:variable name="columnWidth">
            <xsl:value-of select="width"/>
        </xsl:variable>
        <fo:table-column column-width="{$columnWidth}"/>
    </xsl:template>


</xsl:stylesheet>
