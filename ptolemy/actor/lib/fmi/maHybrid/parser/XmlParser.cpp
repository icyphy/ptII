/*
 * Copyright QTronic GmbH. All rights reserved.
 */

/* See $PTII/ptolemy/actor/lib/fmi/ma2/fmusdk-license.htm for the complete FMUSDK License. */

/* ---------------------------------------------------------------------------*
 * XmlParser.cpp
 * Parser implementation for xml model description file of a FMI 2.0 model.
 * The result of parsing is a ModelDescription object that can be queried to
 * get necessary information. The parsing is based on libxml2.lib.
 *
 * Author: Adrian Tirea
 * ---------------------------------------------------------------------------*/

#include "XmlParser.h"
#include "XmlParserException.h"
#include "XmlElement.h"

#include <cstring> // strdup()

#ifdef STANDALONE_XML_PARSER
// ## is special to gcc, see http://gcc.gnu.org/onlinedocs/cpp/Variadic-Macros.html
#define logThis(n, ...) printf(__VA_ARGS__);printf("\n")
#else

extern "C" {
#include "GlobalIncludes.h"
#include "logging.h" // logThis
} // closing brace for extern "C"
#endif // STANDALONE_XML_PARSER

const char *XmlParser::elmNames[SIZEOF_ELM] = { 
    "fmiModelDescription", "ModelExchange", "CoSimulation", "SourceFiles", "File",
    "UnitDefinitions", "Unit", "BaseUnit", "DisplayUnit", "TypeDefinitions",
    "SimpleType", "Real", "Integer", "Boolean", "String",
    "Enumeration", "Item", "LogCategories", "Category", "DefaultExperiment",
    "VendorAnnotations", "Tool", "ModelVariables", "ScalarVariable", "Annotations",
    "ModelStructure", "Outputs", "Derivatives", "DiscreteStates", "InitialUnknowns",
    "Unknown"
};

const char *XmlParser::attNames[SIZEOF_ATT] = {
    "fmiVersion", "modelName", "guid", "description", "author",
    "version", "copyright", "license", "generationTool", "generationDateAndTime",
    "variableNamingConvention", "numberOfEventIndicators", "name", "kg", "m",
    "s", "A", "K", "mol", "cd",
    "rad", "factor", "offset", "quantity", "unit",
    "displayUnit", "relativeQuantity", "min", "max", "nominal",
    "unbounded", "value", "startTime", "stopTime", "tolerance",
    "stepSize", "valueReference", "causality", "variability", "initial",
    "previous", "canHandleMultipleSetPerTimeInstant", "declaredType", "start", "derivative",
    "reinit", "index", "dependencies", "dependenciesKind", "modelIdentifier",
    "needsExecutionTool", "completedIntegratorStepNotNeeded", "canBeInstantiatedOnlyOncePerProcess", "canNotUseMemoryManagementFunctions", "canGetAndSetFMUstate",
    "canSerializeFMUstate", "providesDirectionalDerivative","canHandleVariableCommunicationStepSize", "canInterpolateInputs", "maxOutputDerivativeOrder",
    "canRunAsynchronuously",
    "canGetMaxStepSize",              // Attribute for getMaxStepSize() as proposed in the EMSOFT paper of EECS Berkeley
    "handleIntegerTime",              // Attribute for Hybrid CoSimulation
    // not conform with the FMI 2.0 doc. // Todo : remove then used models become mature.
    // FIXME: Watch out! These values are not added to the attribute enums in the header files. Values have to be added BEFORE this block!
    "xmlns:xsi",                       // Dymola examples from 2014 FD01
    "providesDirectionalDerivatives",  // Dymola examples from 2014 FD01
    "canHandleEvents"                  // Dymola examples from 2014 FD01

};

const char *XmlParser::enuNames[SIZEOF_ENU] = {
    "flat", "structured", "dependent", "constant", "fixed",
    "tunable", "discrete", "parameter", "calculatedParameter", "input",
    "output", "local", "independent", "continuous", "exact",
    "approx", "calculated"
};

XmlParser::XmlParser(char *xmlPath) {
    this->xmlPath = strdup(xmlPath);
    xmlReader = xmlReaderForFile(xmlPath, NULL, 0);
}

XmlParser::~XmlParser() {
    free(xmlPath);
    xmlCleanupParser(); // FIXME: Should solve "still reachable"s in valgrind, but it doesn't
}

ModelDescription *XmlParser::parse() {
    ModelDescription *md = NULL;
    if (xmlReader != NULL) {
        try {
            int ret = xmlTextReaderRead(xmlReader);
            // I expect that first element is fmiModelDescription.
            if (ret == 1) {
                if (0 != strcmp((char *)xmlTextReaderConstLocalName(xmlReader), elmNames[elm_fmiModelDescription])) {
                    throw XmlParserException("Expected '%s' element. Found instead: '%s'.",
                        elmNames[elm_fmiModelDescription],
                        xmlTextReaderConstLocalName(xmlReader));
                }

                md = new ModelDescription;
                md->type = elm_fmiModelDescription;
                parseElementAttributes((Element *)md);
                parseChildElements(md);
            } else {
                throw XmlParserException("Syntax error parsing xml file '%s'", xmlPath);
            }
        } catch (XmlParserException& e) {
            logThis(ERROR_ERROR, e.what());
            md = NULL;
        } catch (std::bad_alloc& ) {
            logThis(ERROR_FATAL, "Out of memory");
            md = NULL;
        }
        xmlFreeTextReader(xmlReader);
    } else {
        logThis(ERROR_ERROR, "Unable to open '%s'", xmlPath);
    }

    return validate(md);
}

void XmlParser::parseElementAttributes(Element *element) {
    while (xmlTextReaderMoveToNextAttribute(xmlReader)) {
        xmlChar *name = xmlTextReaderName(xmlReader);
        xmlChar *value = xmlTextReaderValue(xmlReader);
        XmlParser::Att key = checkAttribute((char *)name);
        char *theValue = value ? strdup((char *)value) : NULL;
        element->attributes.insert(std::pair<XmlParser::Att, char *>(key, theValue));
        xmlFree(name);
        xmlFree(value);
    }
}

void XmlParser::parseChildElements(Element *el) {
    int elementIsEmpty = xmlTextReaderIsEmptyElement(xmlReader);
    if (elementIsEmpty == -1) {
        throw XmlParserException("Error parsing xml file '%s'", xmlPath);
    } else if (elementIsEmpty == 1) {
        return;
    }

    int ret = xmlTextReaderRead(xmlReader);
    while (ret == 1  && xmlTextReaderNodeType(xmlReader) != XML_READER_TYPE_END_ELEMENT) {
        if (xmlTextReaderNodeType(xmlReader) == XML_READER_TYPE_ELEMENT) {
            const char *localName = (char *)xmlTextReaderConstLocalName(xmlReader);
            int depthBefore = xmlTextReaderDepth(xmlReader);
            int isEmptyElement = xmlTextReaderIsEmptyElement(xmlReader);
            el->handleElement(this, localName, isEmptyElement);
            if (!isEmptyElement) {
                int depthAfter = xmlTextReaderDepth(xmlReader);
                if (depthBefore != depthAfter) {
                    throw XmlParserException("Parser error. Depth wrong after parsing sub-tree for %s.", localName);
                }
            }
        }
        ret = xmlTextReaderRead(xmlReader);
    }
    if (ret == -1) {
        throw XmlParserException("Error parsing xml file '%s'", xmlPath);
    }
}

void XmlParser::parseEndElement() {
    int ret = xmlTextReaderRead(xmlReader);
    while (ret == 1  && xmlTextReaderNodeType(xmlReader) != XML_READER_TYPE_END_ELEMENT) {
        ret = xmlTextReaderRead(xmlReader);
    }
    if (ret == -1) {
        throw XmlParserException("Error parsing xml file '%s'", xmlPath);
    }
}

void XmlParser::parseSkipChildElement() {
    int ret = xmlTextReaderNext(xmlReader);
    if (ret == -1) {
        throw XmlParserException("Error parsing xml file '%s'", xmlPath);
    }
}

/* -------------------------------------------------------------------------* 
 * Helper functions to check validity of xml.
 * -------------------------------------------------------------------------*/

// Returns the index of name in the array.
// Throw exception if name not found (invalid).
static int checkName(const char *name, const char *kind, const char *array[], int n) {
    for (int i = 0; i < n; i++) {
        if (!strcmp(name, array[i])) {
            return i;
        }
    }
    throw (XmlParserException("Illegal %s %s", kind, name));
}

XmlParser::Att XmlParser::checkAttribute(const char *att){
    return (XmlParser::Att)checkName(att, "attribute", XmlParser::attNames, XmlParser::SIZEOF_ATT);
}

XmlParser::Elm XmlParser::checkElement(const char *elm) {
    return (XmlParser::Elm)checkName(elm, "element", XmlParser::elmNames, XmlParser::SIZEOF_ELM);
}

XmlParser::Enu XmlParser::checkEnumValue(const char *enu) {
    return (XmlParser::Enu)checkName(enu, "enum value", XmlParser::enuNames, XmlParser::SIZEOF_ENU);
}

ModelDescription *XmlParser::validate(ModelDescription *md) {
    if (md == NULL) return NULL;
    int errors = 0;
    // check modelDescription required attributes
    if (!(md->getAttributeValue(XmlParser::att_fmiVersion)
        && md->getAttributeValue(XmlParser::att_modelName)
        && md->getAttributeValue(XmlParser::att_guid)
        )) {
            logThis(ERROR_ERROR, "Model description miss required attributes in file %s", xmlPath);
            return NULL;
    }

    if (!(md->coSimulation || md->modelExchange)) {
        logThis(ERROR_ERROR, "Model description must have a co-simulation or model exchange component in file %s",
            xmlPath);
            return NULL;
    }

    // check model variables
    for (std::vector<ScalarVariable *>::iterator it = md->modelVariables.begin(); it != md->modelVariables.end(); ++it) {
        const char *varName = (*it)->getAttributeValue(XmlParser::att_name);
        if (!varName) {
            logThis(ERROR_ERROR, "Scalar variable miss required %s attribute in modelDescription.xml",
                XmlParser::attNames[XmlParser::att_name]);
            errors++;
            continue;
        };
        XmlParser::ValueStatus vs;
        (*it)->getAttributeUInt(XmlParser::att_valueReference, &vs);
        if (vs == XmlParser::valueMissing) {
            logThis(ERROR_ERROR, "Scalar variable %s miss required %s attribute in modelDescription.xml",
                varName, XmlParser::attNames[XmlParser::att_valueReference]);
            errors++;
            continue;
        }
        if (vs == XmlParser::valueIllegal) {
            logThis(ERROR_ERROR, "Scalar variable %s has illegal format for %s attribute in modelDescription.xml",
                varName, XmlParser::attNames[XmlParser::att_valueReference]);
            errors++;
            continue;
        }
        if (!(*it)->typeSpec) {
            logThis(ERROR_ERROR, "Scalar variable %s miss type specification in modelDescription.xml %s",
                varName, XmlParser::attNames[XmlParser::att_valueReference]);
            errors++;
            continue;
        }
        if ((*it)->typeSpec->type == XmlParser::elm_Enumeration) {
            const char *typeName = (*it)->typeSpec->getAttributeValue(XmlParser::att_declaredType);
            if (!typeName) {
                logThis(ERROR_ERROR, "Scalar variable %s with enum type specification miss required %s attribute in modelDescription.xml",
                     varName, XmlParser::attNames[XmlParser::att_declaredType]);
                errors++;
                continue;
            }
            if (!md->getSimpleType(typeName)) {
                logThis(ERROR_ERROR, "Declared type %s of variable %s not found in modelDescription.xml",
                     typeName, varName);
                errors++;
                continue;
            }
        }
    }

    if (errors > 0) {
        logThis(ERROR_ERROR, "Found %d error in file %s", errors, xmlPath);
        return NULL;
    }
    return md;
}

//#define TEST
#ifdef TEST 
#ifndef STANDALONE_XML_PARSER
int main () { 
    //{ char c='c'; while(c!='g') scanf("%c", &c); } // to debug: wait for the g key

    XmlParser *parser = new XmlParser("c:\\_data\\fmi-fmu\\fmi-fmuVS\\fmi-fmuVS\\modelDescriptionDymola - ManuallyModifiedCopy.xml");
    ModelDescription *md = parser->parse();
    if (md) md->printElement(0);
    delete parser;
    if (md) delete md;

    dumpMemoryLeaks();
    return 0;
}
#endif // STANDALONE_XML_PARSER
#endif // TEST
