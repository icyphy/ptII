<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/class">
	<entity name="{@name}" class="org.terraswarm.kernel.AccessorOne">       
        <property name="script" class="ptolemy.kernel.util.StringAttribute">
			<xsl:attribute name="value">
				<xsl:value-of select="script"/>
            </xsl:attribute>
		<property name="style" class="ptolemy.actor.gui.style.HiddenStyle">
            </property>
        </property>	
			<property name="_location" class="ptolemy.kernel.util.Location" value="[37.5, 150.0]">
        </property>
			
		<xsl:for-each select="input">
		    <property name="{@name}" class="ptolemy.actor.parameters.PortParameter" value="&quot;{@value}&quot;">
			</property>
				
			<port name="{@name}" class="ptolemy.actor.parameters.ParameterPort">
                <property name="_type" class="ptolemy.actor.TypeAttribute">
                	<xsl:attribute name="value">
                		<xsl:variable name="portType">
                			<xsl:choose>
  								<xsl:when test="@type='number'">
  									<xsl:value-of select="'double'"/>
 	 							</xsl:when>
 	 							<xsl:when test="@type='string'">
  									<xsl:value-of select="'string'"/>
 	 							</xsl:when>
  								<xsl:otherwise>
    								<xsl:value-of select="'general'"/>
  								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<xsl:value-of select="$portType"/>
					</xsl:attribute>
        		</property>
			</port>
		</xsl:for-each>
			
		<xsl:for-each select="output">
			<port name="{@name}" class="ptolemy.actor.TypedIOPort">
                <property name="output"/>
                <property name="_type" class="ptolemy.actor.TypeAttribute">
                	<xsl:attribute name="value">
                		<xsl:variable name="portType">
                			<xsl:choose>
  								<xsl:when test="@type='number'">
  									<xsl:value-of select="'double'"/>
 	 							</xsl:when>
 	 							<xsl:when test="@type='string'">
  									<xsl:value-of select="'string'"/>
 	 							</xsl:when>
  								<xsl:otherwise>
    								<xsl:value-of select="'general'"/>
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