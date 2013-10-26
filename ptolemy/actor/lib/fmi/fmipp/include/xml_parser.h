/* -------------------------------------------------------------------------
 * xml_parser.h
 * A parser for file modelVariables.xml of an FMU.
 * Supports "FMI for Model Exchange 1.0" and "FMI for Co-Simulation 1.0".
 *
 * Copyright 2010 QTronic GmbH. All rights reserved.
 *
 * The FmuSdk is licensed by the copyright holder under the BSD License
 * (http://www.opensource.org/licenses/bsd-license.html):
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY QTRONIC GMBH "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL QTRONIC GMBH BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * -------------------------------------------------------------------------*/

/* Comment by EW: I modified this file in order to allow implicit DLL linking under WIN32! */

#ifndef _FMIPP_XMLPARSER_H
#define _FMIPP_XMLPARSER_H


// define XML_STATIC before including expat.h
// to prevent error when linking with libexpatMT.lib
#define XML_STATIC
#include "expat.h"
#include "stack.h"

typedef unsigned int fmiValueReference;

#define XMLBUFSIZE 1024

#define fmiUndefinedValueReference (fmiValueReference)(-1)

#define SIZEOF_ELM 31
extern const char *elmNames[SIZEOF_ELM];

#define SIZEOF_ATT 47
extern const char *attNames[SIZEOF_ATT];

#define SIZEOF_ENU 13
extern const char *enuNames[SIZEOF_ENU];


/** Elements **/
typedef enum {
        elm_fmiModelDescription,elm_UnitDefinitions,elm_BaseUnit,elm_DisplayUnitDefinition,elm_TypeDefinitions,
        elm_Type,elm_RealType,elm_IntegerType,elm_BooleanType,elm_StringType,elm_EnumerationType,elm_Item,
        elm_DefaultExperiment,elm_VendorAnnotations,elm_Tool,elm_Annotation,elm_ModelVariables,elm_ScalarVariable,
        elm_DirectDependency,elm_Name,elm_Real,elm_Integer,elm_Boolean,elm_String,elm_Enumeration,
        elm_Implementation,elm_CoSimulation_StandAlone,elm_CoSimulation_Tool,elm_Model,elm_File,elm_Capabilities
} Elm;


/** Attributes **/
typedef enum {
        att_fmiVersion,att_displayUnit,att_gain,att_offset,att_unit,att_name,att_description,att_quantity,att_relativeQuantity,
        att_min,att_max,att_nominal,att_declaredType,att_start,att_fixed,att_startTime,att_stopTime,att_tolerance,att_value,
        att_valueReference,att_variability,att_causality,att_alias,att_modelName,att_modelIdentifier,att_guid,att_author,
        att_version,att_generationTool,att_generationDateAndTime,att_variableNamingConvention,att_numberOfContinuousStates,
        att_numberOfEventIndicators,att_input,
        att_canHandleVariableCommunicationStepSize,att_canHandleEvents,att_canRejectSteps,att_canInterpolateInputs,
        att_maxOutputDerivativeOrder,att_canRunAsynchronuously,att_canSignalEvents,att_canBeInstantiatedOnlyOncePerProcess,
        att_canNotUseMemoryManagementFunctions,att_entryPoint,att_manualStart,att_type
} Att;


/** Enumeration values **/
typedef enum {
        enu_flat,enu_structured,enu_constant,enu_parameter,enu_discrete,enu_continuous,
        enu_input,enu_output,enu_internal,enu_none,enu_noAlias,enu_alias,enu_negatedAlias
} Enu;


/** AST node for element
    DisplayUnitDefinition, RealType, IntegerType, BooleanType, StringType, DefaultExperiment,
    Item, Annotation, Name, Real, Integer, Boolean, String, Enumeration, Capabilities, File **/
typedef struct {
        Elm type;          // element type
        const char** attributes; // null or n attribute value strings
        int n;             // size of attributes, even number
} Element;


/** AST node for element that has a list of elements
    BaseUnit, EnumerationType, Tool, DirectDependency, Model **/
typedef struct {
        Elm type;          // element type
        const char** attributes; // null or n attribute value strings
        int n;             // size of attributes, even number
        Element** list;    // null-terminated array of pointers to elements, not null
} ListElement;


/** AST node for element Type **/
typedef struct {
        Elm type;          // element type
        const char** attributes; // null or n attribute value strings
        int n;             // size of attributes, an even number
        Element* typeSpec; // one of RealType, IntegerType etc.
} Type;


/** AST node for element ScalarVariable **/
typedef struct {
        Elm type;          // element type
        const char** attributes; // null or n attribute value strings
        int n;             // size of attributes, even number
        Element* typeSpec; // one of Real, Integer, etc
        Element** directDependencies; // null or null-terminated list of Name
} ScalarVariable;


/** AST node for element CoSimulation_StandAlone and CoSimulation_Tool **/
typedef struct {
        Elm type; // one of elm_CoSimulation_StandAlone and elm_CoSimulation_Tool
        const char** attributes; // null or n attribute value strings
        int n;                   // size of attributes, even number
        Element* capabilities;   // a set of capability attributes
        ListElement* model;      // non-NULL to support tool coupling, NULL for standalone
} CoSimulation;


/** AST node for element ModelDescription **/
typedef struct {
        Elm type;          // element type
        const char** attributes; // null or n attribute value strings
        int n;             // size of attributes, even number
        ListElement** unitDefinitions;    // NULL or null-terminated list of BaseUnits
        Type**        typeDefinitions;    // NULL or null-terminated list of Types
        Element*      defaultExperiment;  // NULL or DefaultExperiment
        ListElement** vendorAnnotations;  // NULL or null-terminated list of Tools
        ScalarVariable** modelVariables;  // NULL or null-terminated list of ScalarVariable
        CoSimulation* cosimulation;       // NULL if this ModelDescription is for model exchange only
} ModelDescription;


/** types of AST nodes used to represent an element **/
typedef enum {
        astElement,
        astListElement,
        astType,
        astScalarVariable,
        astCoSimulation,
        astModelDescription
} AstNodeType;


/** Possible results when retrieving an attribute value from an element **/
typedef enum {
        valueMissing,
        valueDefined,
        valueIllegal
} ValueStatus;


/**
 * Public methods: Parsing and low-level AST access
 **/

ModelDescription* parse( const char* xmlPath );
const char* getString( void* element, Att a );
double getDouble     ( void* element, Att a, ValueStatus* vs );
int getInt           ( void* element, Att a, ValueStatus* vs );
unsigned int getUInt ( void* element, Att a, ValueStatus* vs );
char getBoolean      ( void* element, Att a, ValueStatus* vs );
Enu getEnumValue     ( void* element, Att a, ValueStatus* vs );
void freeElement     ( void* element );


/**
 * Convenience methods for AST access. To be used afer successful validation only.
 **/

const char* getModelIdentifier( ModelDescription* md );
int getNumberOfStates( ModelDescription* md );
int getNumberOfEventIndicators( ModelDescription* md );
const char* getName( void* element );
Enu getCausality( void* scalarVariable );
Enu getVariability( void* scalarVariable );
Enu getAlias( void* scalarVariable );
fmiValueReference getValueReference( void* scalarVariable );
ScalarVariable* getVariableByName( ModelDescription* md, const char* name );
ScalarVariable* getVariable( ModelDescription* md, fmiValueReference vr, Elm type );
Type* getDeclaredType( ModelDescription* md, const char* declaredType );
const char* getString2( ModelDescription* md, void* sv, Att a );
const char * getDescription( ModelDescription* md, ScalarVariable* sv );
const char * getVariableAttributeString( ModelDescription* md, fmiValueReference vr, Elm type, Att a );
double getVariableAttributeDouble( ModelDescription* md, fmiValueReference vr, Elm type, Att a, ValueStatus* vs );
double getNominal( ModelDescription* md, fmiValueReference vr );


#endif // _FMIPP_XMLPARSER_H
