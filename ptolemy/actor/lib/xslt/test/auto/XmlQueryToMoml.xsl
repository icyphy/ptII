<!--
    this stylesheet transform a discoved actor advertisement to a moml change request.

    author: yang
-->
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.1">
    <xsl:output method="xml"/>

    <xsl:template match = "Actors">
        <xsl:for-each select="Actor">

           <entity name="{name}" class="{class}"/>
        </xsl:for-each>
     </xsl:template>

</xsl:stylesheet>
