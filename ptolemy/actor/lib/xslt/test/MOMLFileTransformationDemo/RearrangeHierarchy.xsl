<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Created by Christine Avanessians -->

<xsl:template match="/">
  <xsl:variable name="unique-classes"
	select="/entities/entity[not(@class=preceding-sibling::entity/@class)]/@class"/>   
	<xsl:text>
    </xsl:text>
    <entities>
    <xsl:for-each select="$unique-classes">
       <xsl:text>
       </xsl:text>
       <entity name="{../@name}" class="{.}">
         <xsl:for-each select="//entity[@class=current()]">
       	  <xsl:text>
    	  </xsl:text>
       	  <demo name="{@demoName}" URL="{@demoURL}">
       	  </demo>
       	  </xsl:for-each>
       <xsl:text>
       </xsl:text>	
       </entity>
    </xsl:for-each> 
    <xsl:text>
    </xsl:text>
    </entities>   
</xsl:template> 

</xsl:stylesheet>