<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
		    version="1.0">

   	<!-- **************************************************************************** -->   	
   	<!-- This XSL stylesheet will show how to copy a project -->
   	<!-- **************************************************************************** -->
   	
	<!-- The following line indicates that the result is a xml output file -->
	<xsl:output method="xml"/>
	   
	<xsl:template match="/">
		<xsl:copy-of  select="@*|node()">
			<xsl:apply-templates />
		</xsl:copy-of>
	</xsl:template>	
</xsl:stylesheet>