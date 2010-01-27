<?xml version="1.0"?>
    
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.1">

    <!-- ********************************* -->
    <xsl:output method="xml" encoding="ISO-8859-1" indent="yes"/>
    <xsl:output doctype-public="-//UC Berkeley//DTD MoML 1//EN"
        doctype-system="D:\PtolemyII\ptII\ptolemy\moml\MoML_1.dtd"/>
    <xsl:strip-space elements="*"/>

    <!-- ***************************************************** -->
    <!-- *** buildLoop                                     *** -->
    <!-- ***************************************************** -->
    <xsl:template name="buildLoop">
        <xsl:param name="nbLoop"/>
        <xsl:param name="string" select="''"/>

        <xsl:if test="$nbLoop > 1">
            <xsl:call-template name="buildLoop">
                <xsl:with-param name="nbLoop" select="number($nbLoop)-1"/>
                <xsl:with-param name="string" select="concat($string,'0;')"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="$nbLoop = 1">
            <xsl:call-template name="buildLoop">
                <xsl:with-param name="nbLoop" select="number($nbLoop)-1"/>
                <xsl:with-param name="string" select="concat($string,'0')"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="$nbLoop = 0">
            <xsl:value-of select="$string"/>
        </xsl:if>
    </xsl:template>

    <!-- ***************************************************** -->
    <!-- *** string_replace                                          *** -->
    <!-- ***************************************************** -->
    <xsl:template name="string_replace">
        <xsl:param name="string"/>
        <xsl:param name="search"/>
        <xsl:param name="replace"/>
        <xsl:choose>
            <xsl:when test="contains($string, $search)">
                <xsl:variable name="rest">
                    <xsl:call-template name="string_replace">
                        <xsl:with-param name="string"
                            select="substring-after
                            ($string, $search)"/>
                        <xsl:with-param name="search" select="$search"/>
                        <xsl:with-param name="replace" select="$replace"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:if test="$rest != ''">
                    <xsl:value-of
                        select="concat(substring-before($string, $search), $replace, $rest)"/>
                </xsl:if>
                <xsl:if test="$rest = ''">
                    <xsl:value-of select="substring-before($string, $search)"/>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$string"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ***************************************************** -->
    <!-- *** node                                          *** -->
    <!-- ***************************************************** -->
    <xsl:template name="node">
        <xsl:copy-of select="." xml:space="preserve"/>
    </xsl:template>

    <!-- ***************************************************** -->
    <!-- *** repetition                                    *** -->
    <!-- ***************************************************** -->
    <xsl:template name="repetition">
        <xsl:param name="name"/>
        <xsl:element name="property">
            <xsl:attribute name="name">
                <xsl:value-of select="$name"/>
            </xsl:attribute>
            <xsl:attribute name="class">
                <xsl:text>ptolemy.data.expr.Parameter</xsl:text>
            </xsl:attribute>

            <xsl:attribute name="value">
                <xsl:text>{</xsl:text>
                <xsl:call-template name="string_replace">
                    <xsl:with-param name="string" select="@value"/>
                    <xsl:with-param name="search" select="';'"/>
                    <xsl:with-param name="replace" select="','"/>
                </xsl:call-template>
                <xsl:text>}</xsl:text>
            </xsl:attribute>

        </xsl:element>
    </xsl:template>

    <!-- ***************************************************** -->
    <!-- *** task                                          *** -->
    <!-- ***************************************************** -->
    <xsl:template name="task">
        <xsl:element name="entity">
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <xsl:attribute name="class">
                <xsl:text>ptolemy.domains.pthales.lib.PthalesGenericActor</xsl:text>
            </xsl:attribute>

            <!-- Emplacement -->
            <xsl:for-each select="./property[@name='_location']">
                <xsl:call-template name="node"/>
            </xsl:for-each>

            <!-- Repetitions -->
            <xsl:for-each select="./property[@name='internalLoops']">
                <xsl:call-template name="repetition">
                    <xsl:with-param name="name" select="'internalRepetitions'"/>
                </xsl:call-template>
            </xsl:for-each>
            <xsl:for-each select="./property[@name='LOOP']">
                <xsl:call-template name="repetition">
                    <xsl:with-param name="name" select="'repetitions'"/>
                </xsl:call-template>
            </xsl:for-each>
            
            <!-- function -->
            <xsl:element name="property">
                <xsl:attribute name="name">
                    <xsl:text>function</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="class">
                    <xsl:text>ptolemy.data.expr.StringParameter</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="value">
                    <xsl:value-of select="substring-before(./property[@name='ET']/@value,';')"/>
                </xsl:attribute>
            </xsl:element>

            <!-- arguments -->
            <xsl:element name="property">
                <xsl:attribute name="name">
                    <xsl:text>arguments</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="class">
                    <xsl:text>ptolemy.data.expr.StringParameter</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="value">
                    <xsl:value-of select="./property[@name='arguments']/@value"/>
                </xsl:attribute>
            </xsl:element>
            
            <!-- ports  -->
            <xsl:variable name="loops" select="property[@name='LOOP']/@value"/>
            <xsl:for-each select="port[@class='modeling.application.TaskPort']">
                <xsl:call-template name="port">
                    <xsl:with-param name="loops" select="$loops"/>
                </xsl:call-template>
            </xsl:for-each>

        </xsl:element>
    </xsl:template>

    <!-- ***************************************************** -->
    <!-- *** port                                          *** -->
    <!-- ***************************************************** -->
    <xsl:template name="port">
        <xsl:param name="loops"/>

        <xsl:element name="port">
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <xsl:attribute name="class">
                <xsl:text>ptolemy.domains.pthales.lib.PthalesIOPort</xsl:text>
            </xsl:attribute>

            <!-- propriétés ptolemy -->
            <xsl:for-each select="*[substring-before(@class,'.')='ptolemy' or not(@class)]">
                <xsl:call-template name="node"/>
            </xsl:for-each>

            <!-- output ports-->
            <xsl:if test="property[@name='output']">
                <xsl:variable name="name" select="property[@name='LINKEDARRAY']/@value"/>
                <xsl:variable name="array"
                    select="//entity[@class='modeling.application.Array'][@name=$name]"/>

                <xsl:call-template name="portInfo">
                    <xsl:with-param name="array" select="$array"/>
                    <xsl:with-param name="inout" select="property[@name='INOUT']"/>
                    <xsl:with-param name="loops" select="$loops"/>
                </xsl:call-template>
            </xsl:if>
            <!-- input ports-->
            <xsl:if test="property[@name='input']">
                <!-- Retrouver le port output producteur de l'input -->
                <xsl:variable name="namePort"
                    select="concat(ancestor::entity[@class='modeling.application.Computation']/@name,'.',@name)"/>
                <xsl:variable name="nameRelation" select="//link[@port=$namePort]/@relation"/>
                <xsl:variable name="nameLink"
                    select="//link[@port!=$namePort][@relation=$nameRelation]/@port"/>
                <xsl:variable name="nameTask" select="substring-before($nameLink,'.')"/>
                <xsl:variable name="nameTaskport" select="substring-after($nameLink,'.')"/>
                <xsl:variable name="nameArray"
                    select="//entity[@class='modeling.application.Computation'][@name=$nameTask]/port[@name=$nameTaskport]/property[@name='LINKEDARRAY']/@value"/>
                <xsl:variable name="array"
                    select="//entity[@class='modeling.application.Array'][@name=$nameArray]"/>

                <xsl:call-template name="portInfo">
                    <xsl:with-param name="array" select="$array"/>
                    <xsl:with-param name="inout" select="property[@name='INOUT']"/>
                    <xsl:with-param name="loops" select="$loops"/>
                </xsl:call-template>

            </xsl:if>

        </xsl:element>
    </xsl:template>


    <!-- ***************************************************** -->
    <!-- *** portInfo                                      *** -->
    <!-- ***************************************************** -->
    <xsl:template name="portInfo">
        <xsl:param name="array"/>
        <xsl:param name="inout"/>
        <xsl:param name="loops"/>


        <!-- Data Type -->
        <xsl:element name="property">
            <xsl:attribute name="name">
                <xsl:text>dataType</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="class">
                <xsl:text>ptolemy.data.expr.StringParameter</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="value">
                <xsl:value-of select="substring-before($array/property[@name='ARRAY']/@value,';')"/>
            </xsl:attribute>
        </xsl:element>

        <!-- Data Type Size -->
        <xsl:element name="property">
            <xsl:variable name="type"
                select="substring-before($array/property[@name='ARRAY']/@value,';')"/>
            <xsl:attribute name="name">
                <xsl:text>dataTypeSize</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="class">
                <xsl:text>ptolemy.data.expr.Parameter</xsl:text>
            </xsl:attribute>
            <xsl:if test="$array/property[@name='TYPESIZE']">
                <xsl:variable name="ref" select="$array/property[@name='TYPESIZE']/@value"/>
                <xsl:attribute name="value">
                    <xsl:value-of select="substring-before($ref,';')"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="not($array/property[@name='TYPESIZE'])">
                <xsl:variable name="ref"
                    select="//entity[@class='modeling.application.Array'][substring-before(property[@name='ARRAY']/@value,';')=$type]/property[@name='TYPESIZE']/@value"/>
                <xsl:attribute name="value">
                    <xsl:value-of select="substring-before(string($ref[1]),';')"/>
                </xsl:attribute>
            </xsl:if>
        </xsl:element>

        <!-- Dimension Name-->
        <xsl:element name="property">
            <xsl:attribute name="name">
                <xsl:text>dimensionNames</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="class">
                <xsl:text>ptolemy.data.expr.StringParameter</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="value">
                <xsl:variable name="vars">
                    <xsl:call-template name="string_replace">
                        <xsl:with-param name="string"
                            select="substring-after($array/property[@name='ARRAY']/@value,';')"/>
                        <xsl:with-param name="search" select="';'"/>
                        <xsl:with-param name="replace" select="','"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:value-of select="$vars"/>
            </xsl:attribute>
        </xsl:element>

        <!-- Array information -->
        <xsl:call-template name="arrayInfo">
            <xsl:with-param name="array" select="$array"/>
            <xsl:with-param name="inout" select="$inout"/>
            <xsl:with-param name="loop" select="$loops"/>
        </xsl:call-template>

    </xsl:template>

    <!-- ***************************************************** -->
    <!-- *** arrayInfo                                     *** -->
    <!-- ***************************************************** -->
    <xsl:template name="arrayInfo">
        <xsl:param name="array"/>
        <xsl:param name="inout"/>
        <xsl:param name="loop"/>

        <xsl:variable name="nbDim" select="number(substring-before(string($inout/@value),';'))"/>
        <xsl:variable name="vals" select="tokenize(string($inout/@value),';')"/>
        <xsl:variable name="dims"
            select="tokenize(substring-after($array/property[@name='ARRAY']/@value,';'),';')"/>
        <xsl:variable name="loops" select="tokenize(string($loop),';')"/>
        <xsl:variable name="nbLoops" select="count($loops)-1"/>

        <!--  /////////////////////////////////////////      -->
        <!-- calcul base -->
        <xsl:variable name="base">
            <xsl:call-template name="iterateArray">
                <xsl:with-param name="nbDim" select="$nbDim"/>
                <xsl:with-param name="vals" select="$vals"/>
                <xsl:with-param name="dims" select="$dims"/>
                <!-- l'origine n'a qu'une seule itération-->
                <xsl:with-param name="loops" select="$loops[1]"/>
                <xsl:with-param name="jump" select="1"/>
                <!-- formalisme differnet pour la base -->
                <xsl:with-param name="isBase" select="'true'"/>
            </xsl:call-template>
        </xsl:variable>

        <!--  /////////////////////////////////////////      -->
        <!-- affichage base -->
        <xsl:element name="property">
            <xsl:attribute name="name">
                <xsl:text>base</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="class">
                <xsl:text>ptolemy.data.expr.Parameter</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="value">
                <xsl:text>[</xsl:text>
                <xsl:value-of select="substring($base,1,string-length($base)-1)"/>
                <xsl:text>]</xsl:text>
            </xsl:attribute>
        </xsl:element>

        <!--  /////////////////////////////////////////      -->
        <!-- calcul pattern -->
        <xsl:variable name="jump" select="2 + $nbDim + $nbLoops*($nbDim+1)"/>
        <xsl:variable name="nbPatterns"
            select="number(( count($vals)- number($jump +1) ) div number($nbDim+1))"/>
        <xsl:variable name="patterns">
            <xsl:call-template name="buildLoop">
                <xsl:with-param name="string" select="''"/>
                <xsl:with-param name="nbLoop" select="$nbPatterns"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="pattern">
            <xsl:call-template name="iterateArray">
                <xsl:with-param name="nbDim" select="$nbDim"/>
                <xsl:with-param name="vals" select="$vals"/>
                <xsl:with-param name="dims" select="$dims"/>
                <xsl:with-param name="loops" select="tokenize($patterns,';')"/>
                <xsl:with-param name="jump" select="$jump"/>
                <!-- formalisme differnet pour le pattern -->
                <xsl:with-param name="isPattern" select="'true'"/>
            </xsl:call-template>
        </xsl:variable>

        <!--  /////////////////////////////////////////      -->
        <!-- affichage pattern -->
        <xsl:element name="property">
            <xsl:attribute name="name">
                <xsl:text>pattern</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="class">
                <xsl:text>ptolemy.data.expr.Parameter</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="value">
                <xsl:text>[</xsl:text>
                <xsl:if test="$pattern != ''">
                    <xsl:value-of select="substring($pattern,1,string-length($pattern)-1)"/>
                </xsl:if>
                <xsl:if test="$pattern = ''">
                    <xsl:value-of select="$dims[1]"/>
                    <xsl:text>={1,1}</xsl:text>
                </xsl:if>
                <xsl:text>]</xsl:text>
            </xsl:attribute>
        </xsl:element>

        <!--  /////////////////////////////////////////      -->
        <!-- calcul tilings -->
        <xsl:variable name="tiling">
            <xsl:variable name="tilings">
                <xsl:call-template name="buildLoop">
                    <xsl:with-param name="string" select="''"/>
                    <xsl:with-param name="nbLoop" select="$nbLoops"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:call-template name="iterateArray">
                <xsl:with-param name="nbDim" select="$nbDim"/>
                <xsl:with-param name="vals" select="$vals"/>
                <xsl:with-param name="dims" select="$dims"/>
                <xsl:with-param name="loops" select="tokenize($tilings,';')"/>
                <xsl:with-param name="jump" select="2 + $nbDim"/>
                <!-- formalisme differnet pour le tiling -->
                <xsl:with-param name="isTiling" select="'true'"/>
            </xsl:call-template>
        </xsl:variable>

        <!--  /////////////////////////////////////////      -->
        <!-- affichage tilings -->
        <xsl:element name="property">
            <xsl:attribute name="name">
                <xsl:text>tiling</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="class">
                <xsl:text>ptolemy.data.expr.Parameter</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="value">
                <xsl:text>[</xsl:text>
                <xsl:if test="$tiling != ''">
                    <xsl:value-of select="substring($tiling,1,string-length($tiling)-1)"/>
                </xsl:if>
                <xsl:if test="$tiling = ''">
                    <xsl:value-of select="$dims[1]"/>
                    <xsl:text>=0</xsl:text>
                </xsl:if>
                <xsl:text>]</xsl:text>
            </xsl:attribute>
        </xsl:element>
        <!--  /////////////////////////////////////////      -->

    </xsl:template>

    <!-- ***************************************************** -->
    <!-- *** iterateArray                                     *** -->
    <!-- ***************************************************** -->
    <xsl:template name="iterateArray">
        <xsl:param name="nbDim"/>
        <xsl:param name="vals"/>
        <xsl:param name="loops"/>
        <xsl:param name="dims"/>
        <xsl:param name="jump"/>
        <xsl:param name="isBase" select="'false'"/>
        <xsl:param name="isPattern" select="'false'"/>
        <xsl:param name="isTiling" select="'false'"/>

        <!-- parcours des boucles -->
        <xsl:for-each select="$loops">
            <xsl:variable name="posLoop" select="position()"/>

            <!-- boucle "vide" non traitée -->
            <xsl:if test="$loops[$posLoop] != ''">

                <xsl:variable name="coef" select="$vals[1+$jump+(number($posLoop)-1)*($nbDim+1)]"/>

                <xsl:variable name="output">

                    <!-- parcours des dimensions pour chaque boucle -->
                    <xsl:for-each select="$dims">
                        <xsl:variable name="posDim" select="position()"/>

                        <!-- dimension "vide" non traitée -->
                        <xsl:if test="$dims[$posDim] != ''">
                            <xsl:if
                                test="($vals[1+$jump+$posDim+(number($posLoop)-1)*($nbDim+1)] != '0' or $isBase='true') and $vals[1+$jump+$posDim+(number($posLoop)-1)*($nbDim+1)] != ''">

                                <xsl:value-of select="$dims[$posDim]"/>
                                <xsl:text>=</xsl:text>

                                <!-- affichage x*y -->
                                <xsl:if test="$isPattern='false'">
                                    <xsl:value-of
                                        select="number($vals[1+$jump+$posDim+(number($posLoop)-1)*($nbDim+1)])*number($coef)"/>
                                    <xsl:if test="$posLoop != last()">
                                        <xsl:text>,</xsl:text>
                                    </xsl:if>
                                </xsl:if>
                                <!-- affichage x.y -->
                                <xsl:if test="$isPattern='true'">
                                    <xsl:text>{</xsl:text>
                                    <xsl:value-of select="number($coef)"/>
                                    <xsl:text>,</xsl:text>
                                    <xsl:value-of
                                        select="number($vals[1+$jump+$posDim+(number($posLoop)-1)*($nbDim+1)])"/>
                                    <xsl:text>}</xsl:text>
                                    <xsl:if test="$posLoop != last()">
                                        <xsl:text>,</xsl:text>
                                    </xsl:if>
                                </xsl:if>
                            </xsl:if>
                        </xsl:if>

                    </xsl:for-each>
                </xsl:variable>
                <xsl:if test="$output='' and $isTiling='true'">
                    <xsl:text>empty</xsl:text>
                    <xsl:value-of select="$posLoop"/>
                    <xsl:text>=0,</xsl:text>
                </xsl:if>
                <xsl:if test="not($output='') or $isTiling='false'">
                    <xsl:value-of select="$output"/>
                </xsl:if>

            </xsl:if>
        </xsl:for-each>

    </xsl:template>

    <!-- ***************************************************** -->
    <!-- *** relation                                      *** -->
    <!-- ***************************************************** -->
    <xsl:template name="relation">
        <xsl:element name="relation">
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <xsl:attribute name="class">
                <xsl:text>ptolemy.actor.TypedIORelation</xsl:text>
            </xsl:attribute>
            <xsl:text>&#10;   </xsl:text>

            <!-- Emplacement -->
            <xsl:for-each select="./*[substring-before(@class,'.')='ptolemy']">
                <xsl:call-template name="node"/>
            </xsl:for-each>

        </xsl:element>
    </xsl:template>

    <!-- ***************************************************** -->
    <!-- *** entity                                        *** -->
    <!-- ***************************************************** -->
    <!-- Debut du fichier -->
    <xsl:template match="entity" mode="start">
        <xsl:element name="entity">
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <xsl:attribute name="class">
                <xsl:text>ptolemy.actor.TypedCompositeActor</xsl:text>
            </xsl:attribute>
            <xsl:text>&#10;   </xsl:text>

            <!-- Director : PThales forcément -->
            <xsl:element name="property">
                <xsl:attribute name="name">
                    <xsl:text>PThales Director</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="class">
                    <xsl:text>ptolemy.domains.pthales.kernel.PthalesDirector</xsl:text>
                </xsl:attribute>
                <xsl:element name="property">
                    <xsl:attribute name="name">
                        <xsl:text>_location</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="class">
                        <xsl:text>ptolemy.kernel.util.Location</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="value">
                        <xsl:text>[100.0, 100.0]</xsl:text>
                    </xsl:attribute>
                </xsl:element>
                <xsl:element name="property">
                    <xsl:attribute name="name">
                        <xsl:text>iterations</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="class">
                        <xsl:text>ptolemy.data.expr.Parameter</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="value">
                        <xsl:text>1</xsl:text>
                    </xsl:attribute>
                </xsl:element>
            </xsl:element>

            <!-- propriétés de l'application -->
            <xsl:for-each
                select="./property[substring-before(@class,'.')='ptolemy'][@class!='ptolemy.kernel.attributes.VersionAttribute']">
                <xsl:call-template name="node"/>
            </xsl:for-each>

            <!-- taches de l'application -->
            <xsl:for-each select="./entity[@class='modeling.application.Computation']">
                <xsl:call-template name="task"/>
            </xsl:for-each>

            <!-- relations de l'application -->
            <xsl:for-each select="relation">
                <xsl:call-template name="relation"/>
            </xsl:for-each>

            <!-- liens de l'application -->
            <xsl:for-each select="link">
                <xsl:call-template name="node"/>
            </xsl:for-each>

        </xsl:element>
    </xsl:template>

    <!-- ***************************************************** -->
    <!-- *** root                                          *** -->
    <!-- ***************************************************** -->
    <!-- Parsing de la racine -->
    <xsl:template match="/">
        <xsl:apply-templates mode="start"/>
    </xsl:template>

</xsl:stylesheet>
