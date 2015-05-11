/*
 * Copyright QTronic GmbH. All rights reserved.
 */

/* See $PTII/ptolemy/actor/lib/fmi/ma2/fmusdk-license.htm for the complete FMUSDK License. */

/* ---------------------------------------------------------------------------*
 * XmlParser.h
 * Parser for xml model description file of a FMI 2.0 model.
 * The result of parsing is a ModelDescription object that can be queried to
 * get necessary information. The parsing is based on libxml2.lib.
 *
 * Author: Adrian Tirea
 * ---------------------------------------------------------------------------*/

#ifndef XML_PARSER_H
#define XML_PARSER_H

#include <libxml/xmlreader.h>

#ifdef _MSC_VER
#pragma comment(lib, "libxml2.lib")
#pragma comment(lib, "wsock32.lib")
#endif

// same as in FMI 2.0 specification
typedef unsigned int fmi2ValueReference;

class Element;
class ModelDescription;

class XmlParser {
public:
    //Elements
    static const int SIZEOF_ELM = 31;
    static const char *elmNames[SIZEOF_ELM];
    /*static const*/ enum Elm {
        elm_BAD_DEFINED = -1,
        elm_fmiModelDescription, elm_ModelExchange, elm_CoSimulation, elm_SourceFiles, elm_File,
        elm_UnitDefinitions, elm_Unit, elm_BaseUnit, elm_DisplayUnit, elm_TypeDefinitions,
        elm_SimpleType, elm_Real, elm_Integer, elm_Boolean, elm_String,
        elm_Enumeration, elm_Item, elm_LogCategories, elm_Category, elm_DefaultExperiment,
        elm_VendorAnnotations, elm_Tool, elm_ModelVariables, elm_ScalarVariable, elm_Annotations,
        elm_ModelStructure, elm_Outputs, elm_Derivatives, elm_DiscreteStates, elm_InitialUnknowns,
        elm_Unknown
    };

    // Attributes
    static const int SIZEOF_ATT = 66; //65 //61;
    static const char *attNames[SIZEOF_ATT];
    /*static const*/ enum Att {
        att_BAD_DEFINED = -1,
        att_fmiVersion, att_modelName, att_guid, att_description, att_author,
        att_version, att_copyright, att_license, att_generationTool, att_generationDateAndTime,
        att_variableNamingConvention, att_numberOfEventIndicators, att_name, att_kg, att_m,
        att_s, att_A, att_K, att_mol, att_cd,
        att_rad, att_factor, att_offset, att_quantity, att_unit,
        att_displayUnit, att_relativeQuantity, att_min, att_max, att_nominal,
        att_unbounded, att_value, att_startTime, att_stopTime, att_tolerance,
        att_stepSize, att_valueReference, att_causality, att_variability, att_initial,
        att_previous, att_canHandleMultipleSetPerTimeInstant, att_declaredType, att_start, att_derivative,
        att_reinit, att_index, att_dependencies, att_dependenciesKind, att_modelIdentifier,
        att_needsExecutionTool, att_completedIntegratorStepNotNeeded, att_canBeInstantiatedOnlyOncePerProcess, att_canNotUseMemoryManagementFunctions, att_canGetAndSetFMUstate,
        att_canSerializeFMUstate, att_providesDirectionalDerivative, att_canHandleVariableCommunicationStepSize, att_canInterpolateInputs, att_maxOutputDerivativeOrder,
        att_canRunAsynchronuously,
        // additional attribute for getMaxStepSize() as proposed in the EMSOFT paper
        att_canGetMaxStepSize,
        // additional attributes for Hybrid CoSimulation
        att_handleIntegerTime,
        att_xmlnsXsi, att_providesDirectionalDerivatives, att_canHandleEvents
    };

    // Enumeration values
    static const int SIZEOF_ENU = 17;
    static const char *enuNames[SIZEOF_ENU];
    /*static*/ enum Enu {
        enu_BAD_DEFINED = -1,
        enu_flat, enu_structured, enu_dependent, enu_constant, enu_fixed,
        enu_tunable, enu_discrete, enu_parameter, enu_calculatedParameter, enu_input,
        enu_output, enu_local, enu_independent, enu_continuous, enu_exact,
        enu_approx, enu_calculated
    };

    // Possible results when retrieving an attribute value from an element
    enum ValueStatus {
        valueMissing,
        valueDefined,
        valueIllegal
    };
private:
    char *xmlPath;
    xmlTextReaderPtr xmlReader;

public:
    // return the type of this element. Int value match the index in elmNames.
    // throw XmlParserException if element is invalid.
    static XmlParser::Elm checkElement(const char* elm);
    // return the type of this attribute. Int value match the index in attNames.
    // throw XmlParserException if attribute is invalid.
    static XmlParser::Att checkAttribute(const char* att);
    // return the type of this enum value. Int value match the index in enuNames.
    // throw XmlParserException if enu is invalid.
    static XmlParser::Enu checkEnumValue(const char* enu);

    XmlParser(char *xmlPath);
    ~XmlParser();
    // return NULL on errors. Caller must free the result if not NULL.
    ModelDescription *parse();

    // throw XmlParserException if attribute invalid.
    void parseElementAttributes(Element *element);
    void parseChildElements(Element *el);
    void parseEndElement();
    void parseSkipChildElement();

private:
    // check some properties of model description (i.e. each variable has valueReference, ...)
    // if valid return the input model description, else return NULL.
    ModelDescription *validate(ModelDescription *md);
};

#endif // XML_PARSER_H
