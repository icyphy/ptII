#include "TransLabelStringParser.h"
#include <algorithm>
#include <iostream>

///////////////////////////////////////////////////////////////////////////////
//
void breakExpression( std::string& expr, std::string& token, std::string::size_type from, std::string::size_type len)
{
	token= expr.substr( from, len);
	expr.erase( from, len);
}

///////////////////////////////////////////////////////////////////////////////
// Trim leading and terminating white space characters.
void trim( std::string& toTrim)
{
	std::string::iterator i;
	for( i= toTrim.begin(); isspace( *i); ++i) {}
	toTrim.erase( toTrim.begin(), i);
	std::string::reverse_iterator ri;
	for( ri= toTrim.rbegin(); isspace( *ri); ++ri) {}
	toTrim.erase( ri.base(), toTrim.rbegin().base());
}

///////////////////////////////////////////////////////////////////////////////
// Erase all occurences of 'ch' from 'toFilter'
template <class T>
void filter( T& toFilter, typename T::value_type ch) 
{
	T::iterator itErase= std::remove( toFilter.begin(), toFilter.end(), ch);
	toFilter.erase( itErase, toFilter.end());
}

///////////////////////////////////////////////////////////////////////////////
// Matlab Stateflow Transition label string parser
bool parseTransitionLabel( const std::string& transLabelExpr, std::string& event, std::string& condition, std::string& conditionAction, std::string& action, bool trim)
{
	std::string expr= transLabelExpr;
	const std::string CONDITION_START_MARKER= "[";
	const std::string CONDITION_END_MARKER= "]";
	const std::string CONDITION_ACTION_START_MARKER= "{";
	const std::string CONDITION_ACTION_END_MARKER= "}";
	const std::string ACTION_START_MARKER= "/";
	// start parsing
	std::string::size_type findPos= std::string::npos;
	std::string::size_type findEndPos= std::string::npos;
	findPos= expr.find( ACTION_START_MARKER);
	if ( std::string::npos != findPos) {
		// action found.
		breakExpression( expr, action, findPos, expr.size()- findPos);
	}
	findPos= expr.find( CONDITION_ACTION_START_MARKER);
	if ( std::string::npos != findPos) {
		// find end marker
		findEndPos= expr.find( CONDITION_ACTION_END_MARKER);
		if ( std::string::npos == findEndPos) {
			// invalid expression
			return false;
		}
		breakExpression( expr, conditionAction, findPos, findEndPos- findPos+1);
	}
	findPos= expr.find( CONDITION_START_MARKER);
	if ( std::string::npos != findPos) {
		// find end marker
		findEndPos= expr.find( CONDITION_END_MARKER);
		if ( std::string::npos == findEndPos) {
			// invalid expression
			return false;
		}
		breakExpression( expr, condition, findPos, findEndPos- findPos+ 1);
	}
	// event is the rest
	event= expr;
	// trim markers and white space characters 
	if ( trim) {
		::trim( event);
		::trim( condition); filter( condition, '['); filter( condition, ']');
		::trim( conditionAction); filter( conditionAction, '{'); filter( conditionAction, '}');
		::trim( action); filter( action, '/');
	}
	return true;
}

///////////////////////////////////////////////////////////////////////////////
// tester
void testParser()
{
	const std::string EVENT= "event";
	const std::string CONDITION= "condition";
	const std::string CONDITION_ACTION= "condition_action";
	const std::string ACTION= "transition_action";

	const std::string tests[] = { 
	// 4 tokens
							"event[condition]{condition_action}/transition_action",
	// 3 tokens
							"[condition]{condition_action}/transition_action",
							"event{condition_action}/transition_action",
							"event[condition]/transition_action",
							"event[condition]{condition_action}",
	// 2 tokens
							"event[condition]",
							"event{condition_action}",
							"event/transition_action",
							"[condition]{condition_action}",
							"[condition]/transition_action",
							"{condition_action}/transition_action",
	// 1 tokens
							"event",
							"[condition]",
							"{condition_action}",
							"/transition_action",
	// white space characters
							" event[condition]{condition_action}/transition_action",
							"event [condition]{condition_action}/transition_action",
							"event[condition] {condition_action}/transition_action",
							"event[condition]{condition_action} /transition_action",
							"event[condition]{condition_action}/transition_action ",
							"\tevent[condition]{condition_action}/transition_action",
							"event\t[condition]{condition_action}/transition_action",
							"event[condition]\t{condition_action}/transition_action",
							"event[condition]{condition_action}\t/transition_action",
							"event[condition]{condition_action}/transition_action\t",
	// invalid expressions
							"event[condition{condition_action}/transition_action",
							"event[condition]{condition_action/transition_action"
	};
	int tests_size= 27;
	for( int i=0; i< 27; ++i) {
		std::string event, condition, conditionAction, action;
		bool success= parseTransitionLabel( tests[ i], event, condition, conditionAction, action);
		if ( !success || 
			( !event.empty() && (event!= EVENT)) || 
			( !condition.empty() && (condition != CONDITION)) || 
			( !conditionAction.empty() && (conditionAction != CONDITION_ACTION)) || 
			( !action.empty() && (action != ACTION))
			) {
			std::cout << "Parsing error:\t'" << tests[ i] << "'\n\t\'" << event << "'\t\'" << condition << "'\t\'" << conditionAction << "'\t\'" << action << "\'\n";
		}
	}
}