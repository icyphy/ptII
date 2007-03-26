<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="/">
	<entities>
	<xsl:text>
    </xsl:text>
   <xsl:for-each select="entities/demo/entity/@class">
    <entity name="{../@name}" class="{.}">
    <xsl:text>y8g
    </xsl:text>
     </entity>
     </xsl:for-each>
     <xsl:text>
    </xsl:text>
    </entities>   
</xsl:template> 

</xsl:stylesheet>
