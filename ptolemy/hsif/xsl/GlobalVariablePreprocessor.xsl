<?xml version="1.0"?>
<!-- 	This file deals with the global variables and channels associated with DNHA in HSIF.

	This file checks the role of each global variable in Hybrid Automata (HA) to decide
        if it works as input or output or both. Then the global variables are localized
	into the local variables in the HA based on their roles.

	Instead of using 'input', 'controlled', 'observable', the roles of ports have
	only two types: input and output.

	Also, the channels are localized as input or output ports into different HA
	according their roles in transitions.

	Two things need discussions:
	1. Several HA may have the same global variable as output.
	   Then, multi input ports may be necessary for the HA as receivers.
	2. If HA has a global variable as both input and output, it should be regarded as
	   output only.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- index every node via attribute _id -->
<xsl:key name="nid" match="*" use="@_id"/>

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
    <!-- root element 						    -->
    <!-- ========================================================== -->

    <xsl:template match="/">
        <xsl:comment><xsl:value-of select="$author"/> Generated at <xsl:value-of select="$now"/></xsl:comment>
        <xsl:apply-templates/>
    </xsl:template>

    <!-- ========================================================== -->
    <!-- DNHA element 						    -->
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

    <!-- General Copy copies everything -->
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
        <xsl:variable name="name" select="key('nid',@var)/@name"/>
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


            <xsl:for-each select="descendant::Action/VarRef|descendant::DiffEquation/VarRef">
                <xsl:variable name="name" select="key('nid',@var)/@name"/>
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

            <xsl:for-each select="descendant::Expr/descendant::VarRef">
                <xsl:variable name="name" select="key('nid',@var)/@name"/>
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

</xsl:stylesheet>



