<!--
  Add marker tags
@author: Christopher Hylands, contributor Joern Janneck
@version: $Id$
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:output method="xml" indent="yes"/>	

    <xsl:template match="Scripts/Script[@Type='URL']">
        <xsl:copy>
            <xsl:for-each select="@*">
                <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
            </xsl:for-each>
            <xsl:apply-templates select="node() | text()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/WMBasicEdit">
    	<xsl:copy>
        	<xsl:apply-templates select="@*|node()"/>

		<Markers>
			<xsl:apply-templates select="Markers/*"/>

	
			<xsl:for-each select="Scripts/*">
	            		<Marker Name="{@Command}" Time="{@Time}"/>
			</xsl:for-each>
		</Markers>
	</xsl:copy>
    </xsl:template>

   <xsl:template match="Markers"></xsl:template>

   <xsl:template match="*">
        <xsl:copy>
            <xsl:for-each select="@*">
                <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
            </xsl:for-each>
            <xsl:apply-templates select="*"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
