<!--
  Fix the expression actor in moml files.  The expression parameter is now a string attribute.
@author: Steve Neuendorffer
@version: $Id$
-->

<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0">
	
  <xsl:output method="xml" standalone="no" doctype-public="-//UC Berkeley//DTD MoML 1//EN" doctype-system="http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd"/>

    <xsl:template match="entity[@class='ptolemy.actor.lib.Expression']/property[@name='expression'][@class='ptolemy.data.expr.Parameter']">
        <xsl:copy>
            <xsl:for-each select="@*">
                <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
            </xsl:for-each>
	    <xsl:attribute name="class">ptolemy.kernel.util.StringAttribute</xsl:attribute>
            <xsl:apply-templates select="node() | text()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*|text()|comment()|processing-instruction()">
        <xsl:copy>
            <xsl:for-each select="@*">
                <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
            </xsl:for-each>

            <xsl:apply-templates select="node() | text()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>

