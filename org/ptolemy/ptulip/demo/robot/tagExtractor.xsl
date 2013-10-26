
<!-- Need to include namespace that <tulipcon> specified and give this 
     namespace an identifier, here, ns.  See:
  http://stackoverflow.com/questions/1730875/xslt-transform-xml-with-namespaces 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.1"
	xmlns:ns="http://tulip-control.sourceforge.net/ns/0" >
  
  <xsl:output method="xml" />
  
<!-- In <tulipcon> element, display contents of each <aut> element.
     Must prefix elements with our namespace identifier, ns 
 -->
  <xsl:template match="ns:tulipcon">
  	<xsl:for-each select="ns:aut"> 
  	
  	<!-- Use copy-of to display all content, including tags.  See:
  		http://forums.xkcd.com/viewtopic.php?f=11&t=1617
  		Have to wrap in <aut> </aut> tags for valid XML
  		(encoutered validation errors without)
  	-->
  		<aut>
  		<xsl:copy-of select="node()"/>
  		</aut>
  	</xsl:for-each>
  </xsl:template>

</xsl:stylesheet>