<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/class">
	<entity name="{@name}" class="ptolemy.actor.lib.js.JavaScript">       
        <property name="script"  class="ptolemy.kernel.util.StringAttribute">
				<xsl:attribute name="value">
					<xsl:variable name="function" select="."/>
					<xsl:value-of select="substring-before(substring-after($function, 'function fire() {'), '}&#10;&#10;  &#10;')"/>
            </xsl:attribute>
		<property name="style" class="ptolemy.actor.gui.style.HiddenStyle">
            </property>
        </property>	
			<property name="_location" class="ptolemy.kernel.util.Location" value="[37.5, 150.0]">
        </property>
			
		<xsl:for-each select="property">
		    <property name="{@name}" class="ptolemy.actor.parameters.PortParameter" value="&quot;{@value}&quot;">
                <property name="_hideName" class="ptolemy.kernel.util.SingletonAttribute">
                </property>
                <property name="_icon" class="ptolemy.vergil.icon.ValueIcon">
                </property>
                <property name="_iconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
						<configure>
							<svg>
								<polyline points="-15,-15, -3,-5, -16,5" style="stroke:black"></polyline>
								<polygon points="-22,-1, -22,4, -10,-5, -22,-14, -22,-9, -30,-9, -30, -1" style="fill:lightGray"></polygon>
							</svg>
						</configure>
				</property>	
			    <property name="_smallIconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
                <configure>
                    <svg>
						<text x="20" style="font-size:14; font-family:SansSerif; fill:black" y="20">-P-</text>
					</svg>
				</configure>
                </property>
                <property name="_editorFactory" class="ptolemy.vergil.toolbox.VisibleParameterEditorFactory">
                </property>
                <property name="_location" class="ptolemy.kernel.util.Location" value="{{160, 115}}">
                </property>
			</property>
				
			<port name="{@name}" class="ptolemy.actor.parameters.ParameterPort">
                <property name="input"/>
                <property name="defaultValue" class="ptolemy.data.expr.Parameter">
                </property>
                <property name="_location" class="ptolemy.kernel.util.Location" value="{{140.0, 110.0}}">
                </property>
                <property name="_type" class="ptolemy.actor.TypeAttribute" value="string">
                </property>
                <property name="_showName" class="ptolemy.data.expr.SingletonParameter" value="true">
                </property>
                <property name="_hide" class="ptolemy.data.expr.SingletonParameter" value="true">
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