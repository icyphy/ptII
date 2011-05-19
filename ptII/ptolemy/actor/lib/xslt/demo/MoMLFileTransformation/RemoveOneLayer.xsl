<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Created by Christine Avanessians -->

<xsl:template match="/">
  <xsl:text>
  </xsl:text>
  <entities>
      <xsl:for-each select="entities/entityFromDemo/entity"> 
      <xsl:text>
      </xsl:text>
      <entity name="{@name}" class="{@class}" demoName="{@demoName}" demoURL="{@demoURL}">
      </entity>
      </xsl:for-each> 
  <xsl:text>
  </xsl:text> 
  </entities> 
</xsl:template> 

</xsl:stylesheet>
