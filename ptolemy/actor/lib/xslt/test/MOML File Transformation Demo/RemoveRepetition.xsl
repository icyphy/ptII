<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Created by Christine Avanessians -->

<xsl:template match="/">
  <xsl:variable name="unique-classes"
	select="/demo/entity[not(@class=preceding-sibling::entity/@class)]"/>   
	<xsl:text>
    </xsl:text>
    <xsl:for-each select="$unique-classes">
       <xsl:text>
       </xsl:text>
       <entity name="{@name}" class="{@class}" demoName="{//demo/@name}" demoURL="{//demo/@URL}">
       </entity>
    </xsl:for-each> 
    <xsl:text>
    </xsl:text>   
</xsl:template> 

</xsl:stylesheet>