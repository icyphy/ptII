<!-- Questions: Global variables + Observable variables
                Deterministic semantics
                Channel implementations
                update actions entry/exit actions update the inputs or outputs
                events triggers?
     Export to HSIF .... NO...
-->


<xsl:transform
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 version="1.0"
>

    <xsl:output doctype-system="HSIF.dtd"/>
    <xsl:output method="xml" indent="yes"/>

    <!-- features of the XSLT 2.0 language -->
    <xsl:decimal-format name="comma" decimal-separator="," grouping-separator="."/>

    <!-- include necessary attributes here -->

    <!-- time function -->
    <xsl:variable name="now" xmlns:Date="/java.util.Date">
        <xsl:value-of select="Date:toString(Date:new())"/>
    </xsl:variable>

    <!-- configuration -->
    <xsl:param name="author">Ptolemy II</xsl:param>
    <xsl:preserve-space elements="*"/>

    <!-- ========================================================== -->
    <!-- root element -->
    <!-- ========================================================== -->

    <xsl:template match="/">
        <xsl:comment><xsl:value-of select="$author"/> Generated at <xsl:value-of select="$now"/></xsl:comment>
        <xsl:apply-templates/>
    </xsl:template>

    <!-- ========================================================== -->
    <!-- DNHA element -->
    <!-- ========================================================== -->

    <xsl:template match="DNHA">
        
        <xsl:copy>
            <xsl:for-each select="@*">
                <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
            </xsl:for-each>
            
            <xsl:comment>Variables</xsl:comment>
            <xsl:apply-templates select="IntegerVariable|RealVariable|BooleanVariable"/>
    
            <xsl:comment>Parameters</xsl:comment>
            <xsl:apply-templates select="IntegerParameter|RealParameter|BooleanParameter|Channel" mode="general"/>

            <xsl:comment>Hybrid Automaton</xsl:comment>
            <xsl:apply-templates select="HybridAutomaton"/>

        </xsl:copy>
    </xsl:template>

    <!-- General Copy -->
    <xsl:template match="*" mode="general">
        <xsl:copy>
            <xsl:for-each select="@*">
                <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
            </xsl:for-each>
            <xsl:apply-templates select="*" mode="general"/>
        </xsl:copy>
    </xsl:template>

    <!-- variables -->
    <xsl:template match="IntegerVariable|RealVariable|BooleanVariable">
        <xsl:variable name="name" select="@name"/>
        <xsl:choose>
            <xsl:when test="@kind='Controlled'">
                <xsl:comment>Controlled variables</xsl:comment>
                <xsl:copy>
                    <xsl:for-each select="@*">
                        <xsl:attribute name="{name()}">
                            <xsl:variable name="kind"><xsl:value-of select="."/></xsl:variable>
                            <xsl:choose>
                                <xsl:when test="$kind='Controlled'">Output</xsl:when>
                                <xsl:otherwise><xsl:value-of select="$kind"/></xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                    </xsl:for-each>
                </xsl:copy>
            </xsl:when>
            <xsl:when test="@kind='Input'">
                <xsl:comment>Input variables</xsl:comment>
                <xsl:copy>
                    <xsl:for-each select="@*">
                        <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
                    </xsl:for-each>
                </xsl:copy>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <!-- Hybrid Automaton -->
    <xsl:template match="HybridAutomaton">

        <xsl:variable name="HAID" select="@name"/>

        <xsl:copy>
            <xsl:for-each select="@*">
                <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
            </xsl:for-each>
    

            <xsl:for-each select="descendant::UpdateAction/Var|descendant::DiffEquation/Var">
                <xsl:variable name="name" select="@name"/>
                <xsl:for-each select="//DNHA/IntegerVariable|//DNHA/RealVariable|//DNHA/BooleanVariable">
                    <xsl:if test="@name=$name">
                        <xsl:copy>
                            <xsl:for-each select="@*">
                                <xsl:attribute name="{name()}">
                                    <xsl:variable name="kind"><xsl:value-of select="."/></xsl:variable>
                                    <xsl:choose>
                                        <xsl:when test="$kind='Observable'">Output</xsl:when>
                                        <xsl:when test="$kind='Controlled'">Output</xsl:when>
                                        <xsl:otherwise><xsl:value-of select="$kind"/></xsl:otherwise>
                                    </xsl:choose>
                                </xsl:attribute>
                            </xsl:for-each>
                        </xsl:copy>
                    </xsl:if>
                </xsl:for-each>
            </xsl:for-each>
            
            <xsl:for-each select="descendant::Expr/descendant::Var|descendant::Expr/descendant::Var">
                <xsl:variable name="name" select="@name"/>
                <xsl:for-each select="//DNHA/IntegerVariable|//DNHA/RealVariable|//DNHA/BooleanVariable">
                    <xsl:if test="@name=$name">
                        <xsl:copy>
                            <xsl:for-each select="@*">
                                <xsl:attribute name="{name()}">
                                    <xsl:variable name="kind"><xsl:value-of select="."/></xsl:variable>
                                    <xsl:choose>
                                        <xsl:when test="$kind='Observable'">Input</xsl:when>
                                        <xsl:when test="$kind='Controlled'">Input</xsl:when>
                                        <xsl:otherwise><xsl:value-of select="$kind"/></xsl:otherwise>
                                    </xsl:choose>
                                </xsl:attribute>
                            </xsl:for-each>
                        </xsl:copy>
                    </xsl:if>
                </xsl:for-each>
            </xsl:for-each>

            <xsl:for-each select="../Channel">
                <xsl:variable name="triggerId" select="@_id"/>
                <xsl:variable name="name" select="@name"/>    
                <xsl:variable name="triggerOutputs" select="count(//DNHA/HybridAutomaton[@name=$HAID]/descendant::SendAction[@dst=$triggerId])"/>
                <xsl:variable name="triggerInputs" select="count(//DNHA/HybridAutomaton[@name=$HAID]/descendant::Transition[@trigger=$triggerId])"/>
                <xsl:if test="$triggerOutputs!=0">
                    <triggerOutput type="output">
                        <xsl:attribute name="name"><xsl:value-of select="concat($name, 'Output')"/></xsl:attribute>
                    </triggerOutput>
                </xsl:if>                    
                <xsl:if test="$triggerInputs!=0">
                    <triggerInput type="input">
                        <xsl:attribute name="name"><xsl:value-of select="concat($name, 'Input')"/></xsl:attribute>
                    </triggerInput>
                </xsl:if>                    
            </xsl:for-each>
            
            <xsl:apply-templates select="*" mode="general"/>
        </xsl:copy>
    </xsl:template>

</xsl:transform>	
