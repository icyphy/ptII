<?xml version="1.0"?>
<!-- 	This file deals with the local variables associated with Hybrid Automata (HA).

	There are local parameters and local variables in syntax of HSIF. However,
	the usage of local variables sometimes is more like use of local parameter.

	This file checks the role of the local variables. If the local variables are
	controlled by the integrators, they are regarded as local variables in Ptolemy.
	Otherwise, for example, the local variables are used only by the expressions
	to be integrated, they are regarded as local parameters.

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xalan="http://xml.apache.org/xslt" version="1.0">

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

    <!-- General Copy copies everything -->
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

            <xsl:apply-templates select="IntegerVariable[@kind='Input']|IntegerVariable[@kind='Output']"/>
            <xsl:apply-templates select="RealVariable[@kind='Input']|RealVariable[@kind='Output']"/>
            <xsl:apply-templates select="BooleanVariable[@kind='Input']|BooleanVariable[@kind='Output']"/>
            <xsl:apply-templates select="IntegerParameter|RealParameter|BooleanParameter"/>

            <xsl:for-each select="IntegerVariable[@kind='Controlled']|IntegerVariable[@kind='Observable']">
                <xsl:variable name="id" select="@_id"/>
                <xsl:variable name="counts"  select="count(..//DiffEquation/VarRef[@var=$id])"/>

                <xsl:if test="$counts=0">
                    <xsl:comment> This variable is actually a parameter. </xsl:comment>
                    <xsl:element name="IntegerParameter">
                        <xsl:attribute name="_id" ><xsl:value-of select="$id"/></xsl:attribute>
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
                <xsl:variable name="id" select="@_id"/>
                <xsl:variable name="counts"  select="count(..//DiffEquation/VarRef[@var=$id])"/>
                <xsl:if test="$counts=0">
                    <xsl:comment> This variable is actually a parameter. </xsl:comment>
                    <xsl:element name="RealParameter">
                        <xsl:attribute name="_id" ><xsl:value-of select="$id"/></xsl:attribute>
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
                <xsl:variable name="id" select="@_id"/>
                <xsl:variable name="counts"  select="count(..//DiffEquation/VarRef[@var=$id])"/>

                <xsl:if test="$counts=0">
                    <xsl:comment> This variable is actually a parameter. </xsl:comment>
                    <xsl:element name="BooleanParameter">
                        <xsl:attribute name="_id" ><xsl:value-of select="$id"/></xsl:attribute>
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

            <xsl:apply-templates select="Transition|Location|triggerInput|triggerOutput"/>

        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>


