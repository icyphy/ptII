<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Created by Christine Avanessians -->

<xsl:param name="URLparam">1</xsl:param>

<xsl:template match="/">
  <xsl:text>
  </xsl:text>
  <demo name="{entity/@name}" URL="{$URLparam}">
      <xsl:text>
      </xsl:text>
      <xsl:apply-templates/>
  <xsl:text>
  </xsl:text>
  </demo> 
</xsl:template> 

<xsl:template match="entity">
    <xsl:for-each select="./entity"> 
      <entity name="{@name}" class="{@class}">
      </entity>
      <xsl:text>
      </xsl:text>
    </xsl:for-each>  
</xsl:template>

</xsl:stylesheet>
