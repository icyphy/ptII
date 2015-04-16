<?xml version="1.0" encoding="UTF-8"?>
<!-- XSL transformer to convert level one accessors into MoML for Ptolemy II -->
<!-- Authors: Patricia Derler and Edward A. Lee. -->
<xsl:stylesheet
  version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/class">
	<entity name="{@name}" class="org.terraswarm.accessor.jjs.JSAccessor">
		<!-- Convert the script into a value for the script parameter. -->    
        <property name="script">
        	<!-- Convert the body of the <script>...</script> element into the value of the "value" attribute. -->
			<xsl:attribute name="value">
				<xsl:value-of select="script"/>
            </xsl:attribute>
        </property>

		<!-- Convert the documentation into a value for the script parameter. -->    
        <property name="documentation" class="ptolemy.vergil.basic.DocAttribute">
        	<property name="description" class="ptolemy.kernel.util.StringAttribute">
        		<xsl:attribute name="value">
        			<xsl:value-of select="documentation"/>
            	</xsl:attribute>
            </property>
            <!-- Get documentation for each input. -->
            <xsl:for-each select="input">
            	<xsl:choose>
					<!-- If there is a value, then make a PortParameter. Otherwise, make a port. -->
					<xsl:when test="@value">
            	    	<property name="{@name} (port-parameter)" class="ptolemy.kernel.util.StringAttribute" value="{@description}"/>
					</xsl:when>
					<xsl:otherwise>
            	    	<property name="{@name} (port)" class="ptolemy.kernel.util.StringAttribute" value="{@description}"/>
					</xsl:otherwise>
				</xsl:choose>
            </xsl:for-each>
            <!-- Get documentation for each output. -->
            <xsl:for-each select="output">
		    	<property name="{@name} (port)" class="ptolemy.kernel.util.StringAttribute" value="{@description}">
            	</property>
            </xsl:for-each>
            <!-- Ensure that the error output port has documentation. -->
            <property name="error (port)" class="ptolemy.kernel.util.StringAttribute" value="The error message if an error occurs. If this port is not connected and an error occurs, then an exception is thrown instead."/>
            <!-- Get author information. -->
            <property name="author" class="ptolemy.kernel.util.StringAttribute">
        		<xsl:attribute name="value">
        			<xsl:value-of select="author"/>
            	</xsl:attribute>
            </property>
            <!-- Get version information. -->
            <property name="version" class="ptolemy.kernel.util.StringAttribute">
        		<xsl:attribute name="value">
        			<xsl:value-of select="version"/>
            	</xsl:attribute>
            </property>
        </property>

		<!-- Create a PortParameter for each input. -->
		<!-- NOTE: We ignore the type, if any, and infer the type from the value. -->
		<xsl:for-each select="input">
			<xsl:choose>
				<!-- If there is a value, then make a PortParameter. Otherwise, make a port. -->
				<xsl:when test="@value">
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
				</xsl:when>
				<xsl:otherwise>
				    <port name="{@name}" class="ptolemy.actor.TypedIOPort">
		                <property name="input"/>
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
				</xsl:otherwise>
			</xsl:choose>
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
  								<xsl:when test="@type">
  									<!-- A type is given. -->
  									<!-- NOTE: Assume that other than 'number', accessor types are -->
  									<!-- specified identically to Ptolemy types. -->
    								<xsl:value-of select="@type"/>
 	 							</xsl:when>
  								<xsl:otherwise>
  									<!-- No type is given. Default to general. -->
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