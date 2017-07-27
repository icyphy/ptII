#ifndef PRISE_PARAMETERS_PARSER_H
#define PRISE_PARAMETERS_PARSER_H
#include "tinyxml2.h"
#include <string>
#include <vector>

// XML file copied to the bin directory
#define DEFAULT_XML_PATH "prise_parameters.xml"

using namespace tinyxml2;
using std::string;
using std::vector;


/////////////////////////////////
// TINY XML 2 SPECIFIC METHODS //
/////////////////////////////////



/**
 * find the next sibling (including the current node or not) having a given name
 */
XMLNode* getNextSibling(XMLNode* initialNode, string name, bool inclusive);

/**
 * find the first child having a given name
 */
XMLNode* getFirstChild(XMLNode* node, string name);

/**
 * find a federate child of the current node having the given id
 */
XMLNode* getFirstFederateChild(XMLNode* node, string id);

/**
 * find the text of a child having the given name
 */
const char* getFirstChildText(XMLNode* node, string name);

/**
 * find the text of a child having the given name and cast it into integer
 */
int getFirstChildInt(XMLNode* node, string name);

/**
 * find the first federation node in a tree given by its root
 */
XMLNode* getFirstFederation(XMLNode* root);

/**
 * find the first federate having a given name in a tree given by its root
 */
XMLNode* getFirstFederate(XMLNode* root, string id);



////////////////////////////////
// PARSER-INDEPENDENT METHODS //
////////////////////////////////



/**
 * find the text in the xml three where path contains the name of each node in descendant order
 */
const char* getFirstText(string url, vector<string> path);

/**
 * cast the result of previous function to int
 */
int getFirstInt(string url, vector<string> path);

/**
 * cast the result of previous previous function to int
 */
float getFirstFloat(string url, vector<string> path);

/**
 * find the text of a child having the given name and cast it into float number
 */
float getFirstChildFloat(XMLNode* node, string name);

/**
 * find the name of the first federation found
 */
const char* getFederationName(string file_url);

/**
 * find the federation file of the first federation found
 */
const char* getFedFile(string file_url);

/**
 * find the federate name
 */
const char* getFederateName(string file_url, string federate_node);

#endif
