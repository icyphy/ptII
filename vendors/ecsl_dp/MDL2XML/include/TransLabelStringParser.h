#ifndef TRANSLABELSTRINGPARSER_H
#define TRANSLABELSTRINGPARSER_H

#include <string>

///////////////////////////////////////////////////////////////////////////////
// Matlab Stateflow Transition label string parser
// Returns true if parsing was successful, false otherwise.
// Transition labels have this general format:
//		event [condition]{condition_action}/transition_action
// If 'trim' is true, the tokens are trimmed.
//	(no leading and terminating white space characters, no start and end markers)
extern bool parseTransitionLabel( const std::string& transLabelExpr, std::string& event, std::string& condition, std::string& conditionAction, std::string& action, bool trim= true);

///////////////////////////////////////////////////////////////////////////////
// Trim leading and terminating white space characters.
extern void trim( std::string& toTrim);

#endif //TRANSLABELSTRINGPARSER_H
