<!--
    This stylesheet extracts the value of the "typedValue" element 
    from each "transducerValue" element with a parent "data" element
    where the transducerValue element's id attribute equals the given elementID
    
    Sample data:
    <data><transducerValue id='voltage' rawValue='164' typedValue='16.400000' timestamp='2013-03-04_11:52:29'/></data>
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.1">
  
  <xsl:param name="idParameter"> </xsl:param>
  
  <xsl:output method="text"/>
  <xsl:template match="data">
      <xsl:for-each select="transducerValue[@id=$idParameter]">
{<xsl:value-of select="@id"/> = <xsl:value-of select="@typedValue"/>} 
     </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>