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
            
            <xsl:apply-templates select="*"/>

        </xsl:copy>
    </xsl:template>

    <!-- General Copy -->
    <xsl:template match="*">
        <xsl:copy>
            <xsl:for-each select="@*">
                <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
            </xsl:for-each>
            <xsl:apply-templates select="*"/>
        </xsl:copy>
    </xsl:template>

    <!-- Hybrid Automaton -->
    <xsl:template match="HybridAutomaton">
        <xsl:copy>
            <xsl:for-each select="@*">
                <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
            </xsl:for-each>

            <xsl:for-each select="IntegerVariable|RealVariable|BooleanVariable">
                <xsl:variable name="name" select="@name"/>
                <xsl:if test="not(preceding-sibling::IntegerVariable[@name=$name]|preceding-sibling::RealVariable[@name=$name]|preceding-sibling::BooleanVariable[@name=$name])">
                    <xsl:copy>
                        <xsl:for-each select="@*">
                            <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
                        </xsl:for-each>
                        <xsl:apply-templates select="*"/>
                    </xsl:copy>
                </xsl:if>
            </xsl:for-each>

            <xsl:for-each select="IntegerParameter|RealParameter|BooleanParameter">
                <xsl:variable name="name" select="@name"/>
                <xsl:if test="not(preceding-sibling::IntegerParameter[@name=$name]|preceding-sibling::RealParameter[@name=$name]|preceding-sibling::BooleanParameter[@name=$name])">
                    <xsl:copy>
                        <xsl:for-each select="@*">
                            <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
                        </xsl:for-each>
                        <xsl:apply-templates select="*"/>
                    </xsl:copy>
                </xsl:if>
            </xsl:for-each>

            <xsl:apply-templates select="DiscreteState|Transition"/>

        </xsl:copy>
    </xsl:template>

</xsl:transform>	
