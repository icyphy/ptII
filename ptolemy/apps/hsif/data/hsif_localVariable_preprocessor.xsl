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
            
            <xsl:comment>Hybrid Automaton</xsl:comment>
            <xsl:apply-templates select="HybridAutomaton"/>

            <xsl:comment>General Copy</xsl:comment>
            <xsl:apply-templates select="Channel" mode="general"/>

            <xsl:comment>Variables</xsl:comment>
            <xsl:apply-templates select="IntegerVariable|RealVariable|BooleanVariable" mode="general"/>
    
            <xsl:comment>Parameters</xsl:comment>
            <xsl:apply-templates select="IntegerParameter|RealParameter|BooleanParameter" mode="general"/>
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

    <!-- Hybrid Automaton -->
    <xsl:template match="HybridAutomaton">
        <xsl:variable name="HAID" select="@name"/>
        <xsl:copy>
            
            <xsl:for-each select="@*">
                <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
            </xsl:for-each>

            <xsl:apply-templates select="IntegerVariable[@kind='Input']|IntegerVariable[@kind='Output']" mode="general"/>
            <xsl:apply-templates select="RealVariable[@kind='Input']|RealVariable[@kind='Output']" mode="general"/>
            <xsl:apply-templates select="BooleanVariable[@kind='Input']|BooleanVariable[@kind='Output']" mode="general"/>
            <xsl:apply-templates select="IntegerParameter|RealParameter|BooleanParameter" mode="general"/>

            <xsl:for-each select="IntegerVariable[@kind='Controlled']|IntegerVariable[@kind='Observable']">
                <xsl:variable name="name" select="@name"/>
                <xsl:variable name="counts"  select="count(..//DiffEquation/Var[@name=$name])"/>
               
                <xsl:if test="$counts=0">
                    <xsl:comment> This variable is actually a parameter. </xsl:comment>
                    <xsl:element name="IntegerParameter">
                        <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
                        <xsl:attribute name="value"><xsl:value-of select="0"/></xsl:attribute>
                    </xsl:element>
                </xsl:if>
                <xsl:if test="$counts!=0">
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
            
            <xsl:for-each select="RealVariable[@kind='Controlled']|RealVariable[@kind='Observable']">
                <xsl:variable name="name" select="@name"/>
                <xsl:variable name="counts"  select="count(..//DiffEquation/Var[@name=$name])"/>
                <AAAA/>
                <xsl:value-of select="$counts"/>
                <BBBB/>
                <xsl:if test="$counts=0">
                    <xsl:comment> This variable is actually a parameter. </xsl:comment>
                    <xsl:element name="RealParameter">
                        <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
                        <xsl:attribute name="value"><xsl:value-of select="0.0"/></xsl:attribute>
                    </xsl:element>
                </xsl:if>
                <xsl:if test="$counts!=0">
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

            <xsl:for-each select="BooleanVariable[@kind='Controlled']|BooleanVariable[@kind='Observable']">
                <xsl:variable name="name" select="@name"/>
                <xsl:variable name="counts"  select="count(..//DiffEquation/Var[@name=$name])"/>
               
                <xsl:if test="$counts=0">
                    <xsl:comment> This variable is actually a parameter. </xsl:comment>
                    <xsl:element name="BooleanParameter">
                        <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
                        <xsl:attribute name="value"><xsl:value-of select="false"/></xsl:attribute>
                    </xsl:element>
                </xsl:if>
                <xsl:if test="$counts!=0">
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
            
            <xsl:apply-templates select="DiscreteState|Transition" mode="general"/>

        </xsl:copy>
    </xsl:template>

</xsl:transform>	
