<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/class">
	<entity name="{@name}" class="ptolemy.actor.lib.js.JavaScript">       
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
                <property name="_type" class="ptolemy.actor.TypeAttribute" value="string">
                </property>
                <property name="_showName" class="ptolemy.data.expr.SingletonParameter" value="true">
                </property>
			</port>
		</xsl:for-each>
			
		<xsl:for-each select="output">
			<port name="{@name}" class="ptolemy.actor.TypedIOPort">
                <property name="output"/>
                <property name="_type" class="ptolemy.actor.TypeAttribute" value="general">
                </property>
                <property name="_showName" class="ptolemy.data.expr.SingletonParameter" value="true">
                </property>
            </port>
		</xsl:for-each>	

	</entity>
  </xsl:template>
</xsl:stylesheet>