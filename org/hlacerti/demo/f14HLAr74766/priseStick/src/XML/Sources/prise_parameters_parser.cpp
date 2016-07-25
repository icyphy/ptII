#include "prise_parameters_parser.hpp"

#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <iostream>
#include <fstream>
#define FEDERATION_TAG "federation"
#define FEDERATION_NAME_TAG "federationName"
#define FEDFILE_TAG "fedFile"
#define FEDERATION_TAG "federation"
#define FEDERATE_TAG "federate"
#define DEBUG false


using namespace tinyxml2;
using std::cout;
using std::endl;


/////////////////////////////////
// TINY XML 2 SPECIFIC METHODS //
/////////////////////////////////



/**
 * find the next sibling (including the current node or not) having a given name
 */
XMLNode* getNextSibling(XMLNode* initialNode, string name, bool inclusive) {

	XMLNode* node = initialNode;

	// if initial node is not included, skip it
	if(!inclusive)
		node = node->NextSibling();

	// find the node name
	const char* value = node->Value();

	// while the node name is not the given one
	while(strcmp(value, name.c_str())) {

		// find next sibling
		node = node->NextSibling();

		// if there is no other sibling, return NULL
		if(node==NULL)
			return NULL;

		// else update node name
		value = node->Value();
	}
	// return the found node
	return node;
}

/**
 * find the first child having a given name
 */
XMLNode* getFirstChild(XMLNode* node, string name) {
	// find the potential first child
	if(node == NULL)
		return NULL;	
	XMLNode* child = node->FirstChild();
	// if the node has no child, return NULL
	if(child==NULL)
		return NULL;
	// find the first sibling (including that child) having the given name
	return getNextSibling(child, name.c_str(), true);
}

/**
 * find a federate child of the current node having the given id
 */
XMLNode* getFirstFederateChild(XMLNode* node, string id) {
	
	XMLNode* child = getFirstChild(node, (char*) FEDERATE_TAG);

	// get federate id
	const char* value = child->ToElement()->Attribute("id");

	// while the federate id is not found or not the given one
	while(value==NULL || strcmp(value, id.c_str())) {

		// find next federate
		child = getNextSibling(child, (char*) FEDERATE_TAG, false);

		// if there is no other federate, return NULL
		if(child==NULL)
			return NULL;

		// else update federate id
		value = child->ToElement()->Attribute("id");
	}

	// return the found federate
	return child;
}

/**
 * find the text of a child having the given name
 */
const char* getFirstChildText(XMLNode* node, string name) {

	XMLNode* child = getFirstChild(node, name);
	if(child==NULL)
		return NULL;

	return child->ToElement()->GetText();
}

/**
 * find the text of a child having the given name and cast it into integer
 */
int getFirstChildInt(XMLNode* node, string name) {

	const char* text = getFirstChildText(node, name);

	return atoi(text);
}

/**
 * find the text of a child having the given name and cast it into float number
 */
float getFirstChildFloat(XMLNode* node, string name) {

	const char* text = getFirstChildText(node, name);

	return atof(text);
}

/**
 * find the first federation node in a tree given by its root
 */
XMLNode* getFirstFederation(XMLNode* root) {

	// nodes to be browsed 
	XMLNode* federation;

	// find federation node (can be root)
	federation = getNextSibling(root, (char*) FEDERATION_TAG, true);
    if (DEBUG)
	    printf("federation found: %s\n", federation->Value()); //federation

	return federation;
}

/**
 * find the first federate having a given name in a tree given by its root
 */
XMLNode* getFirstFederate(XMLNode* root, string id) {

	// nodes to be browsed
	XMLNode* federation;
	XMLNode* federate;

	// find federation node (can be root)
	federation = getFirstFederation(root);
	federate = getFirstFederateChild(federation, id);
    if (DEBUG)
	    printf("federate found: %s\n", federate->ToElement()->Attribute("id")); //federate

	return federate;
}



////////////////////////////////
// PARSER-INDEPENDENT METHODS //
////////////////////////////////



/**
 * find the text in the xml three where path contains the name of each node in descendant order
 */
const char* getFirstText(string file_url, vector<string> path){
	XMLDocument xml_parameters;
	xml_parameters.LoadFile(file_url.c_str());

  XMLNode* root = xml_parameters.FirstChild();
	XMLNode* current_node = getFirstFederation(root);
	for(int i = 0 ; i < path.size(); i++){
		current_node = getFirstChild(current_node, path[i]);
	}
	return current_node->ToElement()->GetText();
}

/**
 * cast the result of previous previous function to int
 */
int getFirstInt(string url, vector<string> path){
	return atoi(getFirstText(url, path));
}

/**
 * cast the result of previous previous previous function to float
 */
float getFirstFloat(string url, vector<string> path){
	return atof(getFirstText(url, path));
}

/**
 * find the name of the first federation found
 */
const char* getFederationName(string file_url) {
	vector<string> ret;
	ret.push_back("federationName");
	return getFirstText(file_url, ret);
}

/**
 * find the federation file of the first federation found
 */
const char* getFedFile(string file_url) {
	vector<string> ret;
	ret.push_back("fedFile");
	return getFirstText(file_url, ret);
}

/**
 * find the federate name
 */
const char* getFederateName(string file_url, string federate_node) {
	vector<string> federate_name;
	federate_name.push_back(federate_node);
	federate_name.push_back("federateName");
	return getFirstText(file_url,federate_name);
}

////////////////////
// ONLY FOR DEBUG //
////////////////////



int mainTest( int argc, char** argv )
{
	XMLDocument xml_parameters;
	xml_parameters.LoadFile( "../prise_parameters.xml" );

  XMLNode* root = xml_parameters.FirstChild();
	XMLNode* federation = getFirstFederation(root);
    const char* text = federation->Value();
    if (DEBUG)
        printf("federation found: %s\n", federation->Value()); //federation
    return 0;
}

