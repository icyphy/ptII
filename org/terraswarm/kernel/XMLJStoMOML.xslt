<?xml version="1.0" encoding="UTF-8"?>
<!-- XSL transformer to convert level one accessors into MoML for Ptolemy II -->
<!-- Authors: Patricia Derler and Edward A. Lee. -->
<xsl:stylesheet
  version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/class">
	<entity name="{@name}" class="org.terraswarm.kernel.AccessorOne">       
        <property name="script" class="ptolemy.kernel.util.StringAttribute">
        	<!-- Convert the body of the <script>...</script> element into the value of the "value" attribute. -->
			<xsl:attribute name="value">
				<xsl:value-of select="script"/>
            </xsl:attribute>
			<property name="style" class="ptolemy.actor.gui.style.HiddenStyle">
            </property>
        </property>
        <!-- Put at an arbitrary location in Vergil. -->
		<property name="_location" class="ptolemy.kernel.util.Location" value="[37.5, 150.0]">
        </property>
			
		<!-- Create a PortParameter for each input. -->
		<!-- NOTE: We ignore the type, if any, and infer the type from the value. -->
		<xsl:for-each select="input">
		    <property name="{@name}" class="ptolemy.actor.parameters.PortParameter">
		    	<!-- The default value needs quotation marks if the type is a string. -->
		    	<!-- FIXME: Put in string mode. -->
			    <xsl:variable name="defaultValue">
                	<xsl:choose>
						<xsl:when test="@type='string'">&quot;<xsl:value-of select="@value"/>&quot;</xsl:when>
  						<xsl:otherwise><xsl:value-of select="@value"/></xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
                <xsl:attribute name="value">
					<xsl:value-of select="$defaultValue"/>
				</xsl:attribute>
			</property>
		</xsl:for-each>
			
		<!-- Create a Port for each output. -->
		<xsl:for-each select="output">
			<port name="{@name}" class="ptolemy.actor.TypedIOPort">
                <property name="output"/>
                <property name="_type" class="ptolemy.actor.TypeAttribute">
                	<xsl:attribute name="value">
                		<xsl:variable name="portType">
                			<xsl:choose>
  								<xsl:when test="@type='number'">
  									<!-- JavaScript number is a double. -->
  									<xsl:value-of select="'double'"/>
 	 							</xsl:when>
  								<xsl:otherwise>
  									<!-- NOTE: Assume that other than 'number', accessor types are -->
  									<!-- specified identically to Ptolemy types. -->
    								<xsl:value-of select="@type"/>
  								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<xsl:value-of select="$portType"/>
					</xsl:attribute>
        		</property>
            </port>
		</xsl:for-each>	

	</entity>
  </xsl:template>
</xsl:stylesheet>