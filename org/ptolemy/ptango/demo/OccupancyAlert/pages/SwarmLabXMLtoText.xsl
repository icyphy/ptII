<!--
    This stylesheet extracts the value of the "typedValue" element 
    from each "transducerValue" element with a parent "data" element
    where the transducerValue element's name attribute equals the given nameParameter
    
    Sample data:
	<data>
		<transducerValue id='1473' rawValue='88616' typedValue='88616.000000' name='Uptime Counter' timestamp='2013-11-01T19:18:45.206139-0500'/>
		<transducerValue id='1474' rawValue='3214' typedValue='3.876319' name='True Power Meter' timestamp='2013-11-01T19:18:45.206139-0500'/>
		<transducerValue id='1475' rawValue='1' typedValue='1.000000' name='Socket State' timestamp='2013-11-01T19:18:45.206139-0500'/>
		<transducerValue id='1476' rawValue='40' typedValue='40.000000' name='RSSI' timestamp='2013-11-01T19:18:45.206139-0500'/>
		<transducerValue id='1477' rawValue='254' typedValue='123.652786' name='RMS Voltmeter' timestamp='2013-11-01T19:18:45.206139-0500'/>
		<transducerValue id='1478' rawValue='48' typedValue='0.123622' name='RMS Ammeter' timestamp='2013-11-01T19:18:45.206139-0500'/>
		<transducerValue id='1479' rawValue='0' typedValue='0.253582' name='Power Factor Meter' timestamp='2013-11-01T19:18:45.206139-0500'/>
		<transducerValue id='1480' rawValue='0' typedValue='0.000000' name='Ping' timestamp='2013-11-01T19:18:45.206139-0500'/>
		<transducerValue id='1481' rawValue='60' typedValue='60.000000' name='Frequency Meter' timestamp='2013-11-01T19:18:45.206139-0500'/>
		<transducerValue id='1482' rawValue='0' typedValue='0.000000' name='Energy Meter' timestamp='2013-11-01T19:18:45.206139-0500'/>
		<transducerValue id='1483' rawValue='12192' typedValue='15.286241' name='Apparent Power Meter' timestamp='2013-11-01T19:18:45.206139-0500'/>
	</data>
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.1">
  
  <xsl:param name="nameParameter"> </xsl:param>
  
  <xsl:output method="text"/>
  <xsl:template match="data">
  <!-- Blank spaces would be reproduced in the output, so write this all on one line: -->
      <xsl:for-each select="transducerValue[@name=$nameParameter]"><xsl:value-of select="@typedValue"/></xsl:for-each>
  </xsl:template>

</xsl:stylesheet>