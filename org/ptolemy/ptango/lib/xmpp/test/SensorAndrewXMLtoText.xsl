<!--
    This stylesheet extracts the value of the "typedValue" element 
    from each "transducerValue" element with a parent "data" element
    where the transducerValue element's name attribute equals the given nameParameter
    
    Sample data:
    <data><transducerValue id='1581' rawValue='241' typedValue='24.100000' name='Thermometer Digital' timestamp='2013-09-21T08:04:08.514642-0500'/>
    	  <transducerValue id='1582' rawValue='512' typedValue='26.032501' name='Thermometer Analog' timestamp='2013-09-21T08:04:08.514642-0500'/>
    	  <transducerValue id='1583' rawValue='18' typedValue='18.000000' name='RSSI' timestamp='2013-09-21T08:04:08.514642-0500'/>
    	  <transducerValue id='1584' rawValue='0' typedValue='0.000000' name='Ping' timestamp='2013-09-21T08:04:08.514642-0500'/>
    	  <transducerValue id='1585' rawValue='0' typedValue='0.000000' name='Motion Sensor' timestamp='2013-09-21T08:04:08.514642-0500'/>
    	  <transducerValue id='1586' rawValue='25' typedValue='25.000000' name='Microphone' timestamp='2013-09-21T08:04:08.514642-0500'/>
    	  <transducerValue id='1587' rawValue='1019' typedValue='3.454544' name='Light Meter' timestamp='2013-09-21T08:04:08.514642-0500'/>
    	  <transducerValue id='1588' rawValue='89' typedValue='89.000000' name='Humidity Sensor' timestamp='2013-09-21T08:04:08.514642-0500'/>
    	  <transducerValue id='1589' rawValue='267' typedValue='2.670000' name='Battery Level' timestamp='2013-09-21T08:04:08.514642-0500'/>
    	  <transducerValue id='1590' rawValue='97178' typedValue='97178.000000' name='Barometer' timestamp='2013-09-21T08:04:08.514642-0500'/>
    	  <transducerValue id='1591' rawValue='557' typedValue='-0.198181' name='Accelerometer Z' timestamp='2013-09-21T08:04:08.514642-0500'/>
    	  <transducerValue id='1592' rawValue='621' typedValue='9.810000' name='Accelerometer Y' timestamp='2013-09-21T08:04:08.514642-0500'/>
    	  <transducerValue id='1593' rawValue='494' typedValue='-0.154084' name='Accelerometer X' timestamp='2013-09-21T08:04:08.514642-0500'/>
    </data>
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.1">
  
  <xsl:param name="nameParameter"> </xsl:param>
  
  <xsl:output method="text"/>
  <xsl:template match="data">
  <!-- Blank spaces would be reproduced in the output, so write this all on one line: -->
      <xsl:for-each select="transducerValue[@name=$nameParameter]">{<xsl:value-of select="@name"/> = <xsl:value-of select="@typedValue"/>} </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>