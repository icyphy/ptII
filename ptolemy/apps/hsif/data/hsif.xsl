<!-- Questions: Global variables + Observable variables
                Deterministic semantics
                Channel implementations
                update actions entry/exit actions update the inputs or outputs
                events triggers?
     Export to HSIF
-->


<xsl:transform
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 version="1.0"
>

<xsl:output doctype-system="http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd"
	    doctype-public="-//UC Berkeley//DTD MoML 1//EN"/>
<xsl:output method="xml" indent="yes"/>

<!-- features of the XSLT 2.0 language -->
<xsl:decimal-format name="comma" decimal-separator="," grouping-separator="."/>

<!-- index every node -->
<xsl:key name="nid" match="*" use="@_id"/>

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
    <xsl:element name="entity">
        
        <!-- director -->
        <xsl:call-template name="composite">
            <xsl:with-param name="name" select="@name"/>
            <xsl:with-param name="class" select="'ptolemy.actor.TypedCompositeActor'"/>
            <xsl:with-param name="type" select="'CT'"/>
        </xsl:call-template>

        <!-- Modal Models -->
        <xsl:apply-templates select="HybridAutomaton">
        </xsl:apply-templates>

        <!-- parameters -->
        <xsl:for-each select="IntegerParameter|RealParameter|BooleanParameter|Parameter">
            <xsl:call-template name="parameter">
            </xsl:call-template>
        </xsl:for-each>

        <!-- I/O ports -->
        <xsl:for-each select="IntegerVariable">
            <xsl:call-template name="variable">
                <xsl:with-param name="portType" select="'ptolemy.actor.TypedIOPort'"/>
                <xsl:with-param name="dataType" select="'int'"/>
                <xsl:with-param name="environment" select = "'DNHA'"/>
            </xsl:call-template>
        </xsl:for-each>

        <xsl:for-each select="RealVariable">
            <xsl:call-template name="variable">
                <xsl:with-param name="portType" select="'ptolemy.actor.TypedIOPort'"/>
                <xsl:with-param name="dataType" select="'double'"/>
                <xsl:with-param name="environment" select = "'DNHA'"/>
            </xsl:call-template>
        </xsl:for-each>

        <xsl:for-each select="BooleanVariable">
            <xsl:call-template name="variable">
                <xsl:with-param name="portType" select="'ptolemy.actor.TypedIOPort'"/>
                <xsl:with-param name="dataType" select="'boolean'"/>
                <xsl:with-param name="environment" select = "'DNHA'"/>
            </xsl:call-template>
        </xsl:for-each>

        <!-- Make and link the relations based on I/O ports -->
        <xsl:for-each select="IntegerVariable|RealVariable|BooleanVariable">
            <xsl:element name="relation">
                <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
                <xsl:attribute name="class">ptolemy.actor.TypedIORelation</xsl:attribute>
            </xsl:element>
            <xsl:element name="link">
                <xsl:attribute name="port"><xsl:value-of select="@name"/></xsl:attribute>
                <xsl:attribute name="relation"><xsl:value-of select="@name"/></xsl:attribute>
            </xsl:element>
        </xsl:for-each>

        <!-- Make the relations based on I/O ports block diagram of network of Hybrid Automata-->
        <xsl:for-each select="HybridAutomaton">
            <xsl:variable name="prefix"><xsl:value-of select="@name"/></xsl:variable>
            <xsl:for-each select="IntegerVariable|RealVariable|BooleanVariable|triggerInput|triggerOutput">
                <xsl:element name="relation">
                    <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
                    <xsl:attribute name="class">ptolemy.actor.TypedIORelation</xsl:attribute>
                </xsl:element>
            </xsl:for-each>
        </xsl:for-each>

        <!-- Link the relations and the I/O ports of block diagram of network of Hybrid Automata -->
        <xsl:for-each select="HybridAutomaton">
            <xsl:variable name="prefix"><xsl:value-of select="@name"/></xsl:variable>
            <xsl:for-each select="IntegerVariable|RealVariable|BooleanVariable|triggerInput|triggerOutput">
                <xsl:element name="link">
                    <xsl:attribute name="port"><xsl:value-of select="concat($prefix, '.', @name)"/></xsl:attribute>
                    <xsl:attribute name="relation"><xsl:value-of select="@name"/></xsl:attribute>
                </xsl:element>              
            </xsl:for-each>
        </xsl:for-each>

    </xsl:element>
</xsl:template>   

<!-- ========================================================== -->
<!-- HybridAutomaton -->
<!-- ========================================================== -->

<xsl:template match="HybridAutomaton">

    <xsl:element name="entity">

        <!-- director -->
        <xsl:call-template name="composite">
            <xsl:with-param name="name" select="@name"/>
            <xsl:with-param name="class" select="'ptolemy.vergil.fsm.modal.ModalModel'"/>
            <xsl:with-param name="type" select="'Modal'"/>
        </xsl:call-template>

        <!-- _tableau -->
        <xsl:element name="property">
            <xsl:attribute name="name">_tableauFactory</xsl:attribute>
            <xsl:attribute name="class">ptolemy.vergil.fsm.modal.ModalModel$ModalTableauFactory</xsl:attribute>
        </xsl:element>

        <!-- parameters -->
        <xsl:for-each select="IntegerParameter|RealParameter|BooleanParameter|Parameter">
            <xsl:call-template name="parameter"/>
        </xsl:for-each>

        <!-- I/O ports -->
        <xsl:for-each select="IntegerVariable">
            <xsl:call-template name="variable">
                <xsl:with-param name="portType" select="'ptolemy.vergil.fsm.modal.ModalPort'"/>
                <xsl:with-param name="dataType" select="'int'"/>
                <xsl:with-param name="environment" select = "'HA'"/>
            </xsl:call-template>
        </xsl:for-each>

        <xsl:for-each select="RealVariable">
            <xsl:call-template name="variable">
                <xsl:with-param name="portType" select="'ptolemy.vergil.fsm.modal.ModalPort'"/>
                <xsl:with-param name="dataType" select="'double'"/>
                <xsl:with-param name="environment" select = "'HA'"/>
            </xsl:call-template>
        </xsl:for-each>

        <xsl:for-each select="BooleanVariable">
            <xsl:call-template name="variable">
                <xsl:with-param name="portType" select="'ptolemy.vergil.fsm.modal.ModalPort'"/>
                <xsl:with-param name="dataType" select="'boolean'"/>
                <xsl:with-param name="environment" select = "'HA'"/>
            </xsl:call-template>
        </xsl:for-each>

        <!-- trigger I/O ports -->
        <xsl:for-each select="triggerInput|triggerOutput">
            <xsl:element name="port">
                <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
                <xsl:attribute name="class"><xsl:value-of select="'ptolemy.vergil.fsm.modal.ModalPort'"/></xsl:attribute>
                <xsl:element name="property">    
                    <xsl:attribute name="name"><xsl:value-of select="@type"/></xsl:attribute>
                </xsl:element>
                <xsl:element name="property">    
                    <xsl:attribute name="name"><xsl:value-of select="'signalType'"/></xsl:attribute>
                    <xsl:attribute name="class"><xsl:value-of select="'ptolemy.data.expr.Parameter'"/></xsl:attribute>
                    <xsl:attribute name="value"><xsl:value-of select="'&quot;DISCRETE&quot;'"/></xsl:attribute>
                </xsl:element>
                <xsl:element name="property">
                    <xsl:attribute name="name">_type</xsl:attribute>
                    <xsl:attribute name="class">ptolemy.actor.TypeAttribute</xsl:attribute>
                    <xsl:attribute name="value"><xsl:value-of select="'general'"/></xsl:attribute>
                </xsl:element>
            </xsl:element>
        </xsl:for-each>

        <!-- _Controller -->
        <xsl:element name="entity">
            <!-- attributes of entity -->
            <xsl:attribute name="name">_Controller</xsl:attribute>
            <xsl:attribute name="class">ptolemy.vergil.fsm.modal.ModalController</xsl:attribute>
            <xsl:call-template name="_Controller">
            </xsl:call-template>
         </xsl:element>

        <!-- Refinements from Discrete States -->
        <xsl:apply-templates select="DiscreteState" mode="refinement"/>

        <!-- link _Controller, Refinements -->
        <xsl:for-each select="IntegerVariable|RealVariable|BooleanVariable|triggerInput|triggerOutput">
            <xsl:call-template name="relation">
                <xsl:with-param name="variableName" select="@name"/>
            </xsl:call-template>
        </xsl:for-each>

    <!--
        The local constraints are embedded inside the Refinements.
        <xsl:apply-templates select="LocalConstraint">
        </xsl:apply-templates>
    -->
    </xsl:element>    

</xsl:template>

<!-- ========================================================== -->
<!-- Composite Entity -->
<!-- ========================================================== -->

<xsl:template name="composite">
    <xsl:param name="name" select="'Default Name'"/>
    <xsl:param name="class" select="'Default Class'"/>
    <xsl:param name="type" select="'Default Type'"/>
	<!-- attributes of entity -->
	<xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
	<xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>

    <xsl:choose>
        <xsl:when test="$type='CT'"><xsl:call-template name="CTDirector"/></xsl:when>
        <xsl:when test="$type='CTEmbedded'"><xsl:call-template name="CTEmbeddedDirector"/></xsl:when>
        <xsl:when test="$type='Modal'"><xsl:call-template name="ModalDirector"/></xsl:when>
        <xsl:when test="$type='PN'"><xsl:call-template name="PNDirector"/></xsl:when>
    </xsl:choose>
</xsl:template>

<!-- ========================================================== -->
<!-- Directors -->
<!-- ========================================================== -->

<!-- CT MixedDirector -->
<xsl:template name="CTDirector">
    <xsl:element name="property">
        <!-- attributes of entity -->
        <xsl:attribute name="name">CT Director</xsl:attribute>
        <xsl:attribute name="class">ptolemy.domains.ct.kernel.CTMixedSignalDirector</xsl:attribute>
    </xsl:element>
</xsl:template>

<!-- CT EmbeddedDirector -->
<xsl:template name="CTEmbeddedDirector">
    <xsl:element name="property">
        <!-- attributes of entity -->
        <xsl:attribute name="name">CT Embedded Director</xsl:attribute>
        <xsl:attribute name="class">ptolemy.domains.ct.kernel.CTEmbeddedDirector</xsl:attribute>
    </xsl:element>
</xsl:template>

<!-- Modal Director -->
<xsl:template name="ModalDirector">
    <xsl:element name="property">
        <!-- attributes of entity -->
        <xsl:attribute name="name">_Director</xsl:attribute>
        <xsl:attribute name="class">ptolemy.domains.fsm.kernel.HSDirector</xsl:attribute>
        <xsl:element name="property">
            <xsl:attribute name="name">controllerName</xsl:attribute>
            <xsl:attribute name="class">ptolemy.kernel.util.StringAttribute</xsl:attribute>
            <xsl:attribute name="value">_Controller</xsl:attribute>
        </xsl:element>
    </xsl:element>
</xsl:template>

<!-- PN Director -->
<xsl:template name="PNDirector">
    <xsl:element name="property">
        <!-- attributes of entity -->
        <xsl:attribute name="name">PN Director</xsl:attribute>
        <xsl:attribute name="class">ptolemy.domains.pn.kernel.PNDirector</xsl:attribute>
    </xsl:element>
</xsl:template>

<!-- ========================================================== -->
<!-- Controller in Modal Model incluidng States and Transitions -->
<!-- ========================================================== -->

<!-- Controller for Modal Model -->
<xsl:template name="_Controller">
    <xsl:element name="property">
        <!-- attributes of property -->
        <xsl:attribute name="name">initialStateName</xsl:attribute>
        <xsl:attribute name="class">ptolemy.kernel.util.StringAttribute</xsl:attribute> 
        <xsl:attribute name="value">    
           <xsl:for-each select="DiscreteState">
               <xsl:if test="@initialState='true'">
                    <xsl:value-of select="@name"/>
               </xsl:if>
           </xsl:for-each>
         </xsl:attribute>
    </xsl:element>

    <!-- I/O ports (RefinementPort in _Controller) -->
    <xsl:for-each select="IntegerVariable">
        <xsl:call-template name="variable">
            <xsl:with-param name="portType" select="'ptolemy.vergil.fsm.modal.RefinementPort'"/>
            <xsl:with-param name="dataType" select="'int'"/>
            <xsl:with-param name="environment" select = "'controller'"/>
        </xsl:call-template>
    </xsl:for-each>

    <xsl:for-each select="RealVariable">
        <xsl:call-template name="variable">
            <xsl:with-param name="portType" select="'ptolemy.vergil.fsm.modal.RefinementPort'"/>
            <xsl:with-param name="dataType" select="'double'"/>
            <xsl:with-param name="environment" select = "'controller'"/>
        </xsl:call-template>
    </xsl:for-each>

    <xsl:for-each select="BooleanVariable">
        <xsl:call-template name="variable">
            <xsl:with-param name="portType" select="'ptolemy.vergil.fsm.modal.RefinementPort'"/>
            <xsl:with-param name="dataType" select="'boolean'"/>
            <xsl:with-param name="environment" select = "'controller'"/>
        </xsl:call-template>
    </xsl:for-each>

    <!-- trigger I/O ports -->
    <xsl:for-each select="triggerInput|triggerOutput">
        <xsl:element name="port">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:attribute name="class"><xsl:value-of select="'ptolemy.vergil.fsm.modal.RefinementPort'"/></xsl:attribute>
            <xsl:element name="property">  
                <xsl:attribute name="name">
                    <xsl:value-of select="@type"/>
                </xsl:attribute>
            </xsl:element>
            <xsl:element name="property">  
                <xsl:variable name="type" select="@type"/>
                <xsl:attribute name="name">
                    <xsl:if test="$type='output'"><xsl:value-of select="'input'"/></xsl:if>
                </xsl:attribute>
            </xsl:element>
            <xsl:element name="property">
                <xsl:attribute name="name">_type</xsl:attribute>
                <xsl:attribute name="class">ptolemy.actor.TypeAttribute</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="'general'"/></xsl:attribute>
            </xsl:element>
        </xsl:element>
    </xsl:for-each>

    <!-- Discrete States -->
    <xsl:apply-templates select="DiscreteState" mode="state"/>

    <!-- Guard Expressions and Set Expressions -->
    <xsl:apply-templates select="Transition" mode="info"/>

    <!-- Transitions -->
    <xsl:apply-templates select="Transition" mode="link"/>

</xsl:template>

<!-- States of FSM from Discrete States -->
<xsl:template match="DiscreteState" mode="state">
    <xsl:element name="entity">
        <!-- attributes of entity -->
        <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
        <xsl:attribute name="class">ptolemy.domains.fsm.kernel.State</xsl:attribute>
        <xsl:element name="property">
            <xsl:attribute name="name">refinementName</xsl:attribute>
            <xsl:attribute name="class">ptolemy.kernel.util.StringAttribute</xsl:attribute>
            <xsl:attribute name="value"><xsl:value-of select="@name"/></xsl:attribute>
        </xsl:element>
        <xsl:element name="port">
            <xsl:attribute name="name">incomingPort</xsl:attribute>
            <xsl:attribute name="class">ptolemy.kernel.ComponentPort</xsl:attribute>
        </xsl:element>
        <xsl:element name="port">
            <xsl:attribute name="name">outgoingPort</xsl:attribute>
            <xsl:attribute name="class">ptolemy.kernel.ComponentPort</xsl:attribute>
        </xsl:element>
    </xsl:element>
</xsl:template>

<!-- Transition information -->
<xsl:template match="Transition" mode="info">
    <xsl:variable name="nextStateID" select="dst_end"/>
    <xsl:variable name="nextState" select="key('nid',$nextStateID)/@name"/>
    <xsl:element name="relation">
        <!-- attributes of relation -->
        <xsl:attribute name="name"><xsl:value-of select="@_id"/></xsl:attribute>
        <xsl:attribute name="class">ptolemy.domains.fsm.kernel.Transition</xsl:attribute>
        <!-- attributes of guard Expression -->
        <xsl:element name="property">
            <xsl:attribute name="name">guardExpression</xsl:attribute>
            <xsl:attribute name="class">ptolemy.kernel.util.StringAttribute</xsl:attribute>
            <xsl:attribute name="value"><xsl:apply-templates select="Expr" mode="expr"/>
                <xsl:variable name="event" select="@trigger"/>
                <xsl:if test="$event!=''">
                    <xsl:variable name="trigger" select="key('nid', $event)/@name"/>
                    <xsl:value-of select="concat(' || ', $trigger, 'Input_isPresent')"/>
                </xsl:if>
            </xsl:attribute>
        </xsl:element>
        <!-- attributes of Output Action -->
        <xsl:element name="property">
            <xsl:attribute name="name">outputActions</xsl:attribute>
            <xsl:attribute name="class">ptolemy.domains.fsm.kernel.OutputActionsAttribute</xsl:attribute>
            <xsl:attribute name="value">
                <xsl:apply-templates select="SendAction"/>
            </xsl:attribute>
        </xsl:element>
        <!-- attributes of Set Action -->
        <xsl:element name="property">
            <xsl:attribute name="name">setActions</xsl:attribute>
            <xsl:attribute name="class">ptolemy.domains.fsm.kernel.CommitActionsAttribute</xsl:attribute>
            <xsl:attribute name="value">
                <xsl:apply-templates select="UpdateAction"/>
            </xsl:attribute>
        </xsl:element>
        <xsl:element name="property">
            <xsl:attribute name="name">reset</xsl:attribute>
            <xsl:attribute name="class">ptolemy.data.expr.Parameter</xsl:attribute>
            <xsl:attribute name="value">true</xsl:attribute>
        </xsl:element>
        <xsl:element name="property">
            <xsl:attribute name="name">preemptive</xsl:attribute>
            <xsl:attribute name="class">ptolemy.data.expr.Parameter</xsl:attribute>
            <xsl:attribute name="value">false</xsl:attribute>
        </xsl:element>
    </xsl:element>

</xsl:template>

<!-- Transitions link states -->
<xsl:template match="Transition" mode="link">
    <xsl:variable name="relationName"><xsl:value-of select="@_id"/></xsl:variable>
    <xsl:variable name="dst"><xsl:value-of select="@dst_end_"/></xsl:variable>
    <xsl:for-each select="../DiscreteState[@_id=$dst]">
        <xsl:element name="link">
            <xsl:variable name="stateName"><xsl:value-of select="@name"/></xsl:variable>
            <xsl:attribute name="port"><xsl:value-of select="concat($stateName, '.incomingPort')"/></xsl:attribute>
            <xsl:attribute name="relation"><xsl:value-of select="$relationName"/></xsl:attribute>
        </xsl:element>
    </xsl:for-each>
    <xsl:variable name="src"><xsl:value-of select="@src_end_"/></xsl:variable>
    <xsl:for-each select="../DiscreteState[@_id=$src]">
        <xsl:element name="link">
            <xsl:variable name="stateName"><xsl:value-of select="@name"/></xsl:variable>
            <xsl:attribute name="port"><xsl:value-of select="concat($stateName, '.outgoingPort')"/></xsl:attribute>
            <xsl:attribute name="relation"><xsl:value-of select="$relationName"/></xsl:attribute>
        </xsl:element>
    </xsl:for-each>
</xsl:template>

<!-- ========================================================== -->
<!-- Refinements -->
<!-- ========================================================== -->

<!-- Discrete State as State in FSM-->
<xsl:template match="DiscreteState" mode="refinement">
    <xsl:element name="entity">
        <xsl:call-template name="composite">
            <xsl:with-param name="name" select="@name"/>
            <xsl:with-param name="class" select="'ptolemy.vergil.fsm.modal.Refinement'"/>
            <xsl:with-param name="type" select="'CTEmbedded'"/>
        </xsl:call-template>
        <xsl:element name="property">
            <xsl:attribute name="name">InitialState</xsl:attribute>
            <xsl:attribute name="class">ptolemy.kernel.util.Attribute</xsl:attribute>
        </xsl:element>
        <xsl:element name="property">
            <xsl:attribute name="name">EntryAction</xsl:attribute>
            <xsl:attribute name="class">ptolemy.kernel.util.Attribute</xsl:attribute>
        </xsl:element>
        <xsl:element name="property">
            <xsl:attribute name="name">ExitAction</xsl:attribute>
            <xsl:attribute name="class">ptolemy.kernel.util.Attribute</xsl:attribute>
        </xsl:element>

        <!-- I/O port (RefinementPort) -->
        <xsl:for-each select="../IntegerVariable">
            <xsl:call-template name="variable">
                <xsl:with-param name="portType" select="'ptolemy.vergil.fsm.modal.RefinementPort'"/>
                <xsl:with-param name="dataType" select="'int'"/>
                <xsl:with-param name="environment" select = "'FSM'"/>
            </xsl:call-template>
        </xsl:for-each>

        <xsl:for-each select="../RealVariable">
            <xsl:call-template name="variable">
                <xsl:with-param name="portType" select="'ptolemy.vergil.fsm.modal.RefinementPort'"/>
                <xsl:with-param name="dataType" select="'double'"/>
                <xsl:with-param name="environment" select = "'FSM'"/>
            </xsl:call-template>
        </xsl:for-each>

        <xsl:for-each select="../BooleanVariable">
            <xsl:call-template name="variable">
                <xsl:with-param name="portType" select="'ptolemy.vergil.fsm.modal.RefinementPort'"/>
                <xsl:with-param name="dataType" select="'boolean'"/>
                <xsl:with-param name="environment" select = "'FSM'"/>
            </xsl:call-template>
        </xsl:for-each>

        <!-- trigger I/O ports -->
        <xsl:for-each select="../triggerInput|../triggerOutput">
            <xsl:element name="port">
                <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
                <xsl:attribute name="class"><xsl:value-of select="'ptolemy.vergil.fsm.modal.RefinementPort'"/></xsl:attribute>
                <xsl:element name="property">    
                    <xsl:attribute name="name"><xsl:value-of select="@type"/></xsl:attribute>
                </xsl:element>
                <xsl:element name="property">    
                    <xsl:attribute name="name"><xsl:value-of select="'signalType'"/></xsl:attribute>
                    <xsl:attribute name="class"><xsl:value-of select="'ptolemy.data.expr.Parameter'"/></xsl:attribute>
                    <xsl:attribute name="value"><xsl:value-of select="'&quot;DISCRETE&quot;'"/></xsl:attribute>
                </xsl:element>
                <xsl:element name="property">
                    <xsl:attribute name="name">_type</xsl:attribute>
                    <xsl:attribute name="class">ptolemy.actor.TypeAttribute</xsl:attribute>
                    <xsl:attribute name="value"><xsl:value-of select="'general'"/></xsl:attribute>
                </xsl:element>
            </xsl:element>
        </xsl:for-each>

        <!-- Invariants -->
        <xsl:apply-templates select="Expr" mode="invariant"/>

        <!-- Differential Equations -->
        <xsl:for-each select="DiffEquation">
            <xsl:call-template name="DiffEquation"/>
        </xsl:for-each>

        <!-- Make and link the relations based on I/O ports -->
        <xsl:for-each select="../IntegerVariable|../RealVariable|../BooleanVariable|../triggerInput|../triggerOutput">
            <xsl:element name="relation">
                <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
                <xsl:attribute name="class">ptolemy.actor.TypedIORelation</xsl:attribute>
            </xsl:element>
            <xsl:element name="link">
                <xsl:attribute name="port"><xsl:value-of select="@name"/></xsl:attribute>
                <xsl:attribute name="relation"><xsl:value-of select="@name"/></xsl:attribute>
            </xsl:element>
        </xsl:for-each>

        <!-- Link the relations and the input ports of Invariants-->
        <xsl:for-each select="Expr">
            <xsl:variable name="temp"><xsl:value-of select="@name"/></xsl:variable>
            <xsl:variable name="name">
                <xsl:if test="$temp!=''"><xsl:value-of select="$temp"/></xsl:if>
                <xsl:if test="$temp=''">invariant</xsl:if>
            </xsl:variable>
            <xsl:for-each select="descendant::Var">
                <xsl:variable name="varName" select="@name"/>
                <xsl:variable name="counts" select="count(//DNHA/HybridAutomaton/IntegerVariable[@name=$varName]|//DNHA/HybridAutomaton/RealVariable[@name=$varName]|//DNHA/HybridAutomaton/BooleanVariable[@name=$varName])"/>
                <xsl:if test="$counts!=0">
                    <xsl:element name="link">
                        <xsl:attribute name="port"><xsl:value-of select="concat($name, '.', $varName)"/></xsl:attribute>
                        <xsl:attribute name="relation"><xsl:value-of select="$varName"/></xsl:attribute>
                    </xsl:element>              
                </xsl:if>
            </xsl:for-each>
        </xsl:for-each>

        <!-- Link the relations and the input ports of block diagram of Differential Equations -->
        <xsl:for-each select="DiffEquation">
            <xsl:variable name="prefix"><xsl:value-of select="Var/@name"/></xsl:variable>
            <xsl:for-each select="RExpr/descendant::Var">
                <xsl:variable name="varName" select="@name"/>
                <xsl:variable name="counts" select="count(//DNHA/HybridAutomaton/IntegerVariable[@name=$varName]|//DNHA/HybridAutomaton/RealVariable[@name=$varName]|//DNHA/HybridAutomaton/BooleanVariable[@name=$varName])"/>
                <xsl:if test="$counts!=0">
                    <xsl:element name="link">
                        <xsl:attribute name="port"><xsl:value-of select="concat($prefix, 'FlowEquation.', @name)"/></xsl:attribute>
                        <xsl:attribute name="relation"><xsl:value-of select="@name"/></xsl:attribute>
                    </xsl:element>              
                </xsl:if>
            </xsl:for-each>
        </xsl:for-each>

    </xsl:element>
</xsl:template>

<!-- ========================================================== -->
<!-- Link _Controller, Refinements via relations-->
<!-- ========================================================== -->

<xsl:template name="relation">
    <xsl:param name="variableName" select="'Default VariableName'"/>
    <xsl:element name="relation">
        <xsl:attribute name="name"><xsl:value-of select="concat($variableName, 'Relation')"/></xsl:attribute>
        <xsl:attribute name="class">ptolemy.actor.TypedIORelation</xsl:attribute>
    </xsl:element>
    <xsl:element name="link">
        <xsl:attribute name="port"><xsl:value-of select="$variableName"/></xsl:attribute>
        <xsl:attribute name="relation"><xsl:value-of select="concat($variableName, 'Relation')"/></xsl:attribute>
    </xsl:element>
    <xsl:element name="link">
        <xsl:attribute name="port"><xsl:value-of select="concat('_Controller.', $variableName)"/></xsl:attribute>
        <xsl:attribute name="relation"><xsl:value-of select="concat($variableName, 'Relation')"/></xsl:attribute>
    </xsl:element>
    <xsl:for-each select="../DiscreteState">
        <xsl:element name="link">
           <xsl:attribute name="port"><xsl:value-of select="concat(@name, '.', $variableName)"/></xsl:attribute>
            <xsl:attribute name="relation"><xsl:value-of select="concat($variableName, 'Relation')"/></xsl:attribute>
        </xsl:element>
    </xsl:for-each>
</xsl:template>

<!-- ========================================================== -->
<!-- Parameters -->
<!-- ========================================================== -->
<xsl:template name="parameter">
    <xsl:element name="property">
        <!-- attributes of property -->
        <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
        <xsl:attribute name="class">ptolemy.data.expr.Parameter</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="@value"/></xsl:attribute>
    </xsl:element>
</xsl:template>

<xsl:template match="RealParameter">
    <xsl:element name="property">
        <!-- attributes of property -->
        <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
        <xsl:attribute name="class">ptolemy.data.expr.Parameter</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="@value"/></xsl:attribute>
    </xsl:element>
</xsl:template>

<!-- ========================================================== -->
<!-- Variables -->
<!-- ========================================================== -->
<xsl:template name="variable">
    <xsl:param name="portType" select="'Default PortType'"/>
    <xsl:param name="environment" select = "'Default Environment'"/>
    <xsl:param name="dataType" select = "'Default DataType'"/>
    <xsl:choose>
        <!--xsl:when test="@kind='Controlled'"-->
        <xsl:when test="@kind='Output'">
            <xsl:element name="port">
                <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
                <xsl:attribute name="class"><xsl:value-of select="$portType"/></xsl:attribute>
                <xsl:if test="$environment='controller'">
                    <xsl:element name="property">    
                        <xsl:attribute name="name">input</xsl:attribute>
                    </xsl:element>
                </xsl:if>
                <xsl:element name="property">    
                    <xsl:attribute name="name">output</xsl:attribute>
                </xsl:element>
                <xsl:call-template name="value"/>
                <!--property name="_type" class="ptolemy.actor.TypeAttribute" value="double"-->
                <xsl:element name="property">
                    <xsl:attribute name="name">_type</xsl:attribute>
                    <xsl:attribute name="class">ptolemy.actor.TypeAttribute</xsl:attribute>
                    <xsl:attribute name="value"><xsl:value-of select="$dataType"/></xsl:attribute>
                </xsl:element>
            </xsl:element>
        </xsl:when>
        <xsl:when test="@kind='Input'">
            <xsl:element name="port">
                <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
                <xsl:attribute name="class"><xsl:value-of select="$portType"/></xsl:attribute>
                <xsl:element name="property">    
                    <xsl:attribute name="name">input</xsl:attribute>
                </xsl:element>
                <xsl:element name="property">
                    <xsl:attribute name="name">_type</xsl:attribute>
                    <xsl:attribute name="class">ptolemy.actor.TypeAttribute</xsl:attribute>
                    <xsl:attribute name="value"><xsl:value-of select="$dataType"/></xsl:attribute>
                </xsl:element>
                <xsl:call-template name="value"/>
            </xsl:element>
        </xsl:when>
    </xsl:choose>

</xsl:template>

<!-- ========================================================== -->
<!-- Values -->
<!-- ========================================================== -->
<xsl:template name="value">
    <xsl:choose>
        <xsl:when test="name()='IntegerVariable'">
            <xsl:element name="property">
                <xsl:attribute name="name">minValue</xsl:attribute>
                <xsl:attribute name="class">ptolemy.data.expr.Parameter</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="@minValue"/></xsl:attribute>
            </xsl:element>
            <xsl:element name="property">
                <xsl:attribute name="name">maxValue</xsl:attribute>
                <xsl:attribute name="class">ptolemy.data.expr.Parameter</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="@maxValue"/></xsl:attribute>
            </xsl:element>
            <xsl:element name="property">
                <xsl:attribute name="name">initialMinValue</xsl:attribute>
                <xsl:attribute name="class">ptolemy.data.expr.Parameter</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="@initialMinValue"/></xsl:attribute>
            </xsl:element>
            <xsl:element name="property">
                <xsl:attribute name="name">initialMaxValue</xsl:attribute>
                <xsl:attribute name="class">ptolemy.data.expr.Parameter</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="@initialMaxValue"/></xsl:attribute>
            </xsl:element>
        </xsl:when>
        <xsl:when test="name()='RealVariable'">
            <xsl:element name="property">
                <xsl:attribute name="name">minValue</xsl:attribute>
                <xsl:attribute name="class">ptolemy.data.expr.Parameter</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="@minValue"/></xsl:attribute>
            </xsl:element>
            <xsl:element name="property">
                <xsl:attribute name="name">maxValue</xsl:attribute>
                <xsl:attribute name="class">ptolemy.data.expr.Parameter</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="@maxValue"/></xsl:attribute>
            </xsl:element>
            <xsl:element name="property">
                <xsl:attribute name="name">initialMinValue</xsl:attribute>
                <xsl:attribute name="class">ptolemy.data.expr.Parameter</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="@initialMinValue"/></xsl:attribute>
            </xsl:element>
            <xsl:element name="property">
                <xsl:attribute name="name">initialMaxValue</xsl:attribute>
                <xsl:attribute name="class">ptolemy.data.expr.Parameter</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="@initialMaxValue"/></xsl:attribute>
            </xsl:element>
        </xsl:when>
        <xsl:when test="name()='BooleanVariable'">
            <xsl:element name="property">
                <xsl:attribute name="name">defaultValue</xsl:attribute>
                <xsl:attribute name="class">ptolemy.data.expr.Parameter</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="@defaultValue"/></xsl:attribute>
            </xsl:element>
        </xsl:when>
    </xsl:choose>
</xsl:template>

<!-- ========================================================== -->
<!-- Expressions, Invariants, DiffEquations, UpdateActions, SendActions -->
<!-- ========================================================== -->
<!-- Expression -->

<xsl:template match="Expr" mode="expr">
    <xsl:for-each select="LExpr">
        <xsl:variable name="index" select="position()"/>
        <xsl:apply-templates select="."/>
        <xsl:text> </xsl:text>
        <xsl:variable name="temp">
            <xsl:value-of disable-output-escaping="yes" select="../LOp[$index]/@logicOp"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$temp='and'"><xsl:text>&amp;</xsl:text></xsl:when>
            <xsl:when test="$temp='or'">||</xsl:when>
            <xsl:when test="$temp=' and '"><xsl:text>&amp;</xsl:text></xsl:when>
            <xsl:when test="$temp=' or '">||</xsl:when>
            <xsl:otherwise><xsl:value-of select="$temp"/></xsl:otherwise>
        </xsl:choose>
        <xsl:text> </xsl:text>
    </xsl:for-each>
</xsl:template>

<xsl:template match="LExpr">
    <xsl:for-each select="RExpr">
        <xsl:variable name="index" select="position()"/>
        <xsl:apply-templates select="."/>
        <xsl:value-of disable-output-escaping="yes" select="../ROp[$index]/@relOp"/>
    </xsl:for-each>
</xsl:template>

<xsl:template match="RExpr">
    <xsl:for-each select="AExpr">
        <xsl:variable name="index" select="position()"/>
        <xsl:apply-templates select="."/>
        <xsl:value-of disable-output-escaping="yes" select="../AOp[$index]/@addOp"/>
    </xsl:for-each>
</xsl:template>

<xsl:template match="AExpr">
    <xsl:for-each select="MExpr">
        <xsl:variable name="index" select="position()"/>
        <xsl:apply-templates select="."/>
        <xsl:value-of disable-output-escaping="yes" select="../MOp[$index]/@mulOp"/>
    </xsl:for-each>
</xsl:template>

<xsl:template match="MExpr">
    <xsl:apply-templates select="ExprCall"/>
    <xsl:for-each select="Par|Var|Const">
        <xsl:for-each select="@unOp">
            <xsl:variable name="temp"><xsl:value-of select="."/></xsl:variable>
            <xsl:choose>
                <xsl:when test="$temp!='NOP'"><xsl:value-of select="$temp"/></xsl:when>
            </xsl:choose>
        </xsl:for-each>
        <xsl:value-of select="@value|@name"/>
    </xsl:for-each>
</xsl:template>

<xsl:template match="ExprCall">
    <xsl:text>(</xsl:text>
    <xsl:apply-templates select="Expr" mode="expr"/>
    <xsl:text>)</xsl:text>
</xsl:template>

<!-- Invariants -->
<xsl:template match="Expr" mode="invariant">
    <xsl:element name="entity"> 
        <xsl:variable name="temp"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:attribute name="name">
            <xsl:if test="$temp!=''"><xsl:value-of select="$temp"/></xsl:if>
            <xsl:if test="$temp=''">invariant</xsl:if>
        </xsl:attribute>
        <xsl:attribute name="class">ptolemy.actor.lib.Assertion</xsl:attribute>
        <xsl:element name="property">
            <xsl:attribute name="name">assertion</xsl:attribute>
            <xsl:attribute name="class">ptolemy.data.expr.Parameter</xsl:attribute>
            <xsl:attribute name="value"><xsl:apply-templates select="." mode="expr"/></xsl:attribute>
        </xsl:element>

        <xsl:for-each select="descendant::Var">
            <xsl:variable name="varName" select="@name"/>
            <xsl:variable name="counts" select="count(//DNHA/HybridAutomaton/IntegerVariable[@name=$varName]|//DNHA/HybridAutomaton/RealVariable[@name=$varName]|//DNHA/HybridAutomaton/BooleanVariable[@name=$varName])"/>

            <xsl:if test="$counts!=0">
                <xsl:element name="port">
                    <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
                    <xsl:attribute name="class">ptolemy.actor.TypedIOPort</xsl:attribute>
                    <xsl:element name="property">
                        <xsl:attribute name="name">input</xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:if>
        </xsl:for-each>

        <!--xsl:for-each select="../../IntegerVariable|../../RealVariable|../../BooleanVariable">
            <xsl:element name="port">
                <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
                <xsl:attribute name="class">ptolemy.actor.TypedIOPort</xsl:attribute>
                <xsl:element name="property">
                    <xsl:attribute name="name">input</xsl:attribute>
                </xsl:element>
            </xsl:element>
        </xsl:for-each-->

    </xsl:element>
</xsl:template>

<!-- UpdateActions -->
<xsl:template match="UpdateAction">
    <xsl:for-each select="Var">
        <xsl:variable name="index" select="position()"/>
        <xsl:for-each select="@unOp|@name">
            <xsl:variable name="temp"><xsl:value-of select="."/></xsl:variable>
            <xsl:choose>
                <xsl:when test="$temp!='NOP'"><xsl:value-of select="$temp"/></xsl:when>
            </xsl:choose>
        </xsl:for-each>
        <xsl:value-of select="'='"/>
        <xsl:apply-templates select="../Expr[$index]" mode="expr"/>
        <xsl:value-of select="';'"/>
    </xsl:for-each>
</xsl:template>

<!-- SendActions -->
<xsl:template match="SendAction">
    <xsl:variable name="dst" select="@dst"/>
    <xsl:variable name="trigger" select="key('nid', $dst)/@name"/>
    <xsl:value-of select="concat($trigger, 'Output', '=1')"/>
</xsl:template>

<!-- DiffEquation -->
<xsl:template name="DiffEquation">
    <!-- integrator(s) -->
    <xsl:for-each select="Var">
        <xsl:element name="entity">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:attribute name="class">ptolemy.domains.ct.lib.Integrator</xsl:attribute>
            <xsl:element name="port">
                <xsl:attribute name="name">output</xsl:attribute>
                <xsl:attribute name="class">ptolemy.actor.TypedIOPort</xsl:attribute>
                <xsl:element name="property">
                    <xsl:attribute name="name">output</xsl:attribute>
                </xsl:element>
            </xsl:element>
            <xsl:element name="port">
                <xsl:attribute name="name">input</xsl:attribute>
                <xsl:attribute name="class">ptolemy.actor.TypedIOPort</xsl:attribute>
                <xsl:element name="property">
                    <xsl:attribute name="name">input</xsl:attribute>
                </xsl:element>
            </xsl:element>
        </xsl:element>
    </xsl:for-each>

    <!-- flowEquation(s) -->
    <xsl:for-each select="RExpr">
        <xsl:element name="entity">
            <xsl:variable name="integrator"><xsl:value-of select="../Var/@name"/></xsl:variable>
            <xsl:attribute name="name"><xsl:value-of select="concat($integrator, 'FlowEquation')"/></xsl:attribute>
            <xsl:attribute name="class">ptolemy.actor.lib.Expression</xsl:attribute>
            <xsl:element name="property">
                <xsl:attribute name="name">expression</xsl:attribute>
                <!--xsl:attribute name="class">ptolemy.data.expr.Parameter</xsl:attribute-->
                <xsl:attribute name="value"><xsl:apply-templates select="."/></xsl:attribute>
            </xsl:element>
            <xsl:element name="port">
                <xsl:attribute name="name">output</xsl:attribute>
                <xsl:attribute name="class">ptolemy.actor.TypedIOPort</xsl:attribute>
                <xsl:element name="property">
                    <xsl:attribute name="name">output</xsl:attribute>
                </xsl:element>
                <xsl:element name="property">
                    <xsl:attribute name="name">_type</xsl:attribute>
                    <xsl:attribute name="class">ptolemy.actor.TypeAttribute</xsl:attribute>
                    <xsl:attribute name="value"><xsl:value-of select="'double'"/></xsl:attribute>
                </xsl:element>
            </xsl:element>
    
            <xsl:for-each select="descendant::Var">
                <xsl:variable name="varName" select="@name"/>
                <xsl:variable name="counts" select="count(//DNHA/HybridAutomaton/IntegerVariable[@name=$varName]|//DNHA/HybridAutomaton/RealVariable[@name=$varName]|//DNHA/HybridAutomaton/BooleanVariable[@name=$varName])"/>
                <xsl:if test="$counts!=0">
                    <xsl:element name="port">
                        <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
                        <xsl:attribute name="class">ptolemy.actor.TypedIOPort</xsl:attribute>
                        <xsl:element name="property">
                            <xsl:attribute name="name">input</xsl:attribute>
                        </xsl:element>
                    </xsl:element>
                </xsl:if>
            </xsl:for-each>

        </xsl:element>
    </xsl:for-each>

    <xsl:for-each select="Var">
        <xsl:variable name="varName"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:element name="relation">
            <xsl:attribute name="name"><xsl:value-of select="$varName"/></xsl:attribute>
            <xsl:attribute name="class">ptolemy.actor.TypedIORelation</xsl:attribute>
        </xsl:element>
        <xsl:element name="relation">
            <xsl:attribute name="name"><xsl:value-of select="concat($varName, 'relationBetweenIntegratorAndExpression')"/></xsl:attribute>
            <xsl:attribute name="class">ptolemy.actor.TypedIORelation</xsl:attribute>
        </xsl:element>
        <xsl:element name="link">
            <xsl:attribute name="port"><xsl:value-of select="concat($varName, '.input')"/></xsl:attribute>
            <xsl:attribute name="relation"><xsl:value-of select="concat($varName, 'relationBetweenIntegratorAndExpression')"/></xsl:attribute>
        </xsl:element>
        <xsl:element name="link">
            <xsl:attribute name="port"><xsl:value-of select="concat($varName, 'FlowEquation', '.output')"/></xsl:attribute>
            <xsl:attribute name="relation"><xsl:value-of select="concat($varName, 'relationBetweenIntegratorAndExpression')"/></xsl:attribute>
        </xsl:element>
        <xsl:element name="link">
            <xsl:attribute name="port"><xsl:value-of select="$varName"/></xsl:attribute>
            <xsl:attribute name="relation"><xsl:value-of select="$varName"/></xsl:attribute>
        </xsl:element>
        <xsl:element name="link">
            <xsl:attribute name="port"><xsl:value-of select="concat($varName, '.output')"/></xsl:attribute>
            <xsl:attribute name="relation"><xsl:value-of select="$varName"/></xsl:attribute>
        </xsl:element>
    </xsl:for-each>

</xsl:template>

</xsl:transform>	
