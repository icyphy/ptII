// Copyright (c) Vanderbilt University, 2000-2001
// ALL RIGHTS RESERVED
// Vanderbilt University disclaims all warranties with regard to this
// software, including all implied warranties of merchantability
// and fitness.  In no event shall Vanderbilt University be liable for
// any special, indirect or consequential damages or any damages
// whatsoever resulting from loss of use, data or profits, whether
// in an action of contract, negligence or other tortious action,
// arising out of or in connection with the use or performance of
// this software.
//
//    file: mdl.g
//  author: Chuck Thomason
//    date: 2/13/2001
//
//	This grammar file for ANTLR 2.7.0 generates a parser for Matlab Simulink (.mdl) files.
//	The parser builds up an output structure conformant to mdl.dtd using UDM.
//	The entry point method is void MDLParser::start().
//
// CHANGES:
//		- VIZA changed namespace qualifier of 'Object' from 'mdl' to 'Udm' on 07/12/04
//

header {
#pragma warning(disable:4101)
#pragma warning(disable:4786)
#include "UdmDom.h"
#include "Uml.h"
#include "UmlExt.h"

#include "mdl.h"
}

options {
language="Cpp";
}

// Parser
class MDLParser extends Parser;

options {
k=2;
}

{
// global stuff goes here
Udm::Object parentObj;	
mdl::parameter currParam;
}

start [mdl::Simulink rootSim]
 : {
	// initialization code
	parentObj = rootSim;
   }
   (	model (sf)?
   |	library (sf)?
   |    matdata (sf)?
   )*
   {
	parentObj = &Udm::_null;
	currParam = &Udm::_null;
   }
 ;

matdata
 : MATDATA LBRACE
	(	(IDENT	STRING_LITERAL_NORMAL)
	|	(IDENT	MEGA_STRING)
	|	(IDENT	VECTOR)
	|	(IDENT	IDENT)
    |   datarecord
	)*
   RBRACE
;

datarecord
 : DATARECORD LBRACE
	(	(IDENT	STRING_LITERAL_NORMAL)
	|	(IDENT	MEGA_STRING)
	|	(IDENT	VECTOR)
	|	(IDENT	IDENT)
    |   datarecord
	)*
   RBRACE
;

model
 : MODEL LBRACE		{  mdl::Model mod = mdl::Model::Create(parentObj); Udm::Object myParent = parentObj; parentObj = mod; }
	(	(paramName paramValue)
	|	blockdefaults
	| 	blockparameterdefaults
	|	annotationdefaults
	|	linedefaults
	|	system
	)*

   RBRACE	 { parentObj = myParent; }
 ;

library
 : LIBRARY LBRACE	{ mdl::Library lib = mdl::Library::Create(parentObj); Udm::Object myParent = parentObj; parentObj = lib; }
	(	(paramName paramValue)
	|	blockdefaults
	| 	blockparameterdefaults
	|	annotationdefaults
	|	linedefaults
	|	system
	)*

   RBRACE	{ parentObj = myParent; }
 ;

blockdefaults
 : BLOCKDEFAULTS LBRACE	{ mdl::BlockDefaults bdef = mdl::BlockDefaults::Create(parentObj); Udm::Object myParent = parentObj; parentObj = bdef; }
	(paramName paramValue)*

   RBRACE	{ parentObj = myParent; }
 ;

annotationdefaults
 : ANNOTATIONDEFAULTS LBRACE	 { mdl::AnnotationDefaults adef = mdl::AnnotationDefaults::Create(parentObj); Udm::Object myParent = parentObj; parentObj = adef; }
	(paramName paramValue)*

   RBRACE	{ parentObj = myParent; }
 ;

linedefaults
 : LINEDEFAULTS LBRACE	 { mdl::LineDefaults ldef = mdl::LineDefaults::Create(parentObj); Udm::Object myParent = parentObj; parentObj = ldef; }
	(paramName paramValue)*

   RBRACE	{ parentObj = myParent; }
 ;

system
 : SYSTEM LBRACE	{ mdl::System sys = mdl::System::Create(parentObj); Udm::Object myParent = parentObj; parentObj = sys; }
	(	(paramName paramValue)
	|	block
	|	line
	|	annotation
	)*

   RBRACE	{ parentObj = myParent; }
 ;

blockparameterdefaults
 : BLOCKPARAMETERDEFAULTS LBRACE 
     ( blockparameterdefault )*
   RBRACE
; 

blockparameterdefault
 : BLOCK LBRACE	{ mdl::BlockParameterDefault blk = mdl::BlockParameterDefault::Create(parentObj); Udm::Object myParent = parentObj; parentObj = blk; }
	( (paramName paramValue) )*
   RBRACE	{ parentObj = myParent; }
 ;

block
 : BLOCK LBRACE	{ mdl::Block blk = mdl::Block::Create(parentObj); Udm::Object myParent = parentObj; parentObj = blk; }
	(
		port
	|	list_rule
	|	system
    |   linkdata
	|	(paramName paramValue)
    |   STRING_LITERAL_NORMAL
	)*

   RBRACE	{ parentObj = myParent; }
 ;

linkdata
 : LINKDATA LBRACE
	(	(IDENT	STRING_LITERAL_NORMAL)
	|	(IDENT	MEGA_STRING)
	|	(IDENT	VECTOR)
	|	(IDENT	IDENT)
    |   dialogparameters
	)*
   RBRACE
;

dialogparameters
 : DIALOGPARAMETERS LBRACE
	(	(IDENT	STRING_LITERAL_NORMAL)
	|	(IDENT	MEGA_STRING)
	|	(IDENT	VECTOR)
	|	(IDENT	IDENT)
	)*
   RBRACE
;

list_rule
 : LIST LBRACE	{ mdl::List lst = mdl::List::Create(parentObj); Udm::Object myParent = parentObj; parentObj = lst; }
	(paramName paramValue)*

   RBRACE	{ parentObj = myParent; }
 ;

port
 : PORT LBRACE	 { mdl::Port prt = mdl::Port::Create(parentObj); Udm::Object myParent = parentObj; parentObj = prt; }
	(paramName paramValue)*

  RBRACE	{ parentObj = myParent; }
 ;

line
 : LINE LBRACE	{ mdl::Line ln = mdl::Line::Create(parentObj); Udm::Object myParent = parentObj; parentObj = ln; }
	(
		(paramName paramValue)
	|	branch
	)*

   RBRACE	{ parentObj = myParent; }
 ;

branch
 : BRANCH LBRACE	{ mdl::Branch br = mdl::Branch::Create(parentObj); Udm::Object myParent = parentObj; parentObj = br;}
	(
		(paramName paramValue)
	|	branch
	)*

   RBRACE	{ parentObj = myParent; }
 ;

annotation
 : ANNOTATION LBRACE	{ mdl::Annotation ann = mdl::Annotation::Create(parentObj); Udm::Object myParent = parentObj; parentObj = ann; }
	(paramName paramValue)*

   RBRACE	 { parentObj = myParent; }
 ;

sf
 : STATEFLOW LBRACE	{ mdl::Stateflow statef = mdl::Stateflow::Create(parentObj); Udm::Object myParent = parentObj; parentObj = statef; }
 (	machine
 |	chart
 |	state
 |	transition
 |	data
 |	instance
 |	target
 |	junction
 |	event
 )*
   RBRACE	{ parentObj = myParent; }
 ;

machine
 : MACHINE LBRACE	{ mdl::machine mach = mdl::machine::Create(parentObj); Udm::Object myParent = parentObj; parentObj = mach; }
	(	(paramName paramValue)
	|	debug
	)*

   RBRACE	{ parentObj = myParent; }
 ;

debug
 : DEBUG LBRACE	 { mdl::debug dbg = mdl::debug::Create(parentObj); Udm::Object myParent = parentObj; parentObj = dbg; }
		(paramName paramValue)*

   RBRACE	{ parentObj = myParent; }
 ;

chart
 : CHART LBRACE	{ mdl::chart cht = mdl::chart::Create(parentObj); Udm::Object myParent = parentObj; parentObj = cht; }
	(	(paramName paramValue)
	|	stateFontS
	|	transitionFontS
	|	subviewS
	)*
   RBRACE	{ parentObj = myParent; }
 ;

stateFontS
 : STATEFONTS LBRACE
    (    (IDENT	STRING_LITERAL_NORMAL)
	|	(IDENT	MEGA_STRING)
	|	(IDENT	VECTOR)
	|	(IDENT	IDENT)
	)*

   RBRACE
 ;

transitionFontS
 : TRANSITIONFONTS LBRACE
	(	(IDENT	STRING_LITERAL_NORMAL)
	|	(IDENT	MEGA_STRING)
	|	(IDENT	VECTOR)
	|	(IDENT	IDENT)
	)*

   RBRACE
 ;


subviewS
 : SUBVIEWS LBRACE { mdl::subviewS sv = mdl::subviewS::Create(parentObj); Udm::Object myParent = parentObj; parentObj = sv; }
		(paramName paramValue)*

   RBRACE	{ parentObj = myParent; }
 ;

state
 : STATE LBRACE	{ mdl::state st = mdl::state::Create(parentObj); Udm::Object myParent = parentObj; parentObj = st; }
	(	(paramName paramValue)
	|	subviewS
	)*

   RBRACE	{ parentObj = myParent; }
 ;

transition
 : TRANSITION LBRACE	{ mdl::transition trans = mdl::transition::Create(parentObj); Udm::Object myParent = parentObj; parentObj = trans; }
	(	(paramName paramValue)
	|	src
	|	dst
	|	slide
	)*

   RBRACE	{ parentObj = myParent; }
 ;

src
 : SRC LBRACE	{ mdl::src source = mdl::src::Create(parentObj); Udm::Object myParent = parentObj; parentObj = source; }
	(	(paramName paramValue)*
	)

   RBRACE	{ parentObj = myParent; }
 ;

dst
 : DST LBRACE	{ mdl::dst dest = mdl::dst::Create(parentObj); Udm::Object myParent = parentObj; parentObj = dest; }
	(	(paramName paramValue)*
	)

   RBRACE	{ parentObj = myParent; }
 ;

slide
 : SLIDE LBRACE	{ mdl::slide sld = mdl::slide::Create(parentObj); Udm::Object myParent = parentObj; parentObj = sld; }
	(paramName paramValue)*

   RBRACE	{ parentObj = myParent; }
 ;

data
 : DATA LBRACE	{ mdl::data datum = mdl::data::Create(parentObj); Udm::Object myParent = parentObj; parentObj = datum; }
	(	(paramName paramValue)
	|	range
	|	array
	|	props
    |   fixptType
	)*

   RBRACE	{ parentObj = myParent; }
 ;

fixptType
 : FIXPTTYPE LBRACE
	(	(IDENT	STRING_LITERAL_NORMAL)
	|	(IDENT	MEGA_STRING)
	|	(IDENT	VECTOR)
	|	(IDENT	IDENT)
	)*

   RBRACE
 ;

props
 : PROPS LBRACE	{ mdl::props prp = mdl::props::Create(parentObj); Udm::Object myParent = parentObj; parentObj = prp; }
	(	(paramName paramValue)
	|	range
	|	array
	)*

   RBRACE	{ parentObj = myParent; }
 ;

range
 : RANGE LBRACE	{ mdl::range rng = mdl::range::Create(parentObj); Udm::Object myParent = parentObj; parentObj = rng; }
	(	(paramName paramValue)*
	)

   RBRACE	{ parentObj = myParent; }
 ;

array
 : ARRAY LBRACE	{ mdl::array arr = mdl::array::Create(parentObj); Udm::Object myParent = parentObj; parentObj = arr; }
	(	(paramName paramValue)*
	)

   RBRACE	{ parentObj = myParent; }
 ;

instance
 : INSTANCE LBRACE	{ mdl::instance inst = mdl::instance::Create(parentObj); Udm::Object myParent = parentObj; parentObj = inst; }
	(	(paramName paramValue)
	)*

   RBRACE	{ parentObj = myParent; }
 ;

target
 : TARGET LBRACE	{ mdl::target tgt = mdl::target::Create(parentObj); Udm::Object myParent = parentObj; parentObj = tgt; }
	(	(paramName paramValue)
	)*

   RBRACE	{ parentObj = myParent; }
 ;

junction
 : JUNCTION LBRACE	{ mdl::junction junct = mdl::junction::Create(parentObj); Udm::Object myParent = parentObj; parentObj = junct; }
	(	(paramName paramValue)
	)*

   RBRACE	{ parentObj = myParent; }
 ;

event
 : EVENT LBRACE	{ mdl::event ev = mdl::event::Create(parentObj); Udm::Object myParent = parentObj; parentObj = ev; }
	(	(paramName paramValue)
	)*

   RBRACE	{ parentObj = myParent; }
 ;

paramName
 :
	{ mdl::parameter param = mdl::parameter::Create(parentObj); string nm; currParam = param; }
	(
		pn:IDENT	 { param.name() = pn->getText(); }
	|	nm=keyword_var	 { param.name() = nm; }
	)

 ;

paramValue
 :
	{ string theVal; }
	(
	  s:STRING_LITERAL_NORMAL { currParam.value() = s->getText(); }
	| ms:MEGA_STRING { currParam.value() =  ms->getText(); }
	| v:VECTOR { currParam.value() = v->getText(); }
	| id:IDENT { currParam.value() = id->getText(); }
	| theVal=keyword_var { currParam.value() = theVal; }
	)
	{ currParam = &Udm::_null; }
 ;

keyword_var returns [string theVal]
 :
	  PORT { theVal = "Port"; }
	| CHART { theVal = "chart"; }
	| MACHINE { theVal = "machine"; }
	| STATE { theVal = "state"; }
    | MATDATA { theVal = "MatData"; }
    | DATARECORD { theVal = "DataRecord"; }
	| MODEL { theVal = "Model"; }
	| LIBRARY { theVal = "Library"; }
	| BLOCKDEFAULTS { theVal = "BlockDefaults"; }
	| BLOCKPARAMETERDEFAULTS { theVal = "BlockParameterDefaults"; }
	| ANNOTATIONDEFAULTS { theVal = "AnnotationDefaults"; }
	| LINEDEFAULTS { theVal = "LineDefaults"; }
	| SYSTEM { theVal = "System"; }
	| BLOCK { theVal = "Block"; }
	| LIST { theVal = "List"; }
	| LINE { theVal = "Line"; }
	| BRANCH { theVal = "Branch"; }
	| ANNOTATION { theVal = "Annotation"; }
	| STATEFLOW { theVal = "Stateflow"; }
	| DEBUG { theVal = "Debug"; }
	| SUBVIEWS { theVal = "subviewS"; }
	| TRANSITION { theVal = "transition"; }
	| SRC { theVal = "src"; }
	| DST { theVal = "dst"; }
	| SLIDE { theVal = "slide"; }
	| DATA { theVal = "data"; }
	| PROPS { theVal = "props"; }
	| RANGE { theVal = "range"; }
	| ARRAY { theVal = "array"; }
	| INSTANCE { theVal = "instance"; }
	| TARGET { theVal = "target"; }
	| JUNCTION {theVal = "junction"; }
	| EVENT { theVal = "event"; }
 ;
// end Parser

// Scanner
class MDLLexer extends Lexer;

options {
       charVocabulary = '\0'..'\377';
       testLiterals=false;    // don't automatically test for literals
}

tokens {
PORT="Port";
CHART="chart";
MACHINE="machine";
STATE="state";
MATDATA="MatData";
DATARECORD="DataRecord";
MODEL="Model";
LIBRARY="Library";
BLOCKDEFAULTS="BlockDefaults";
BLOCKPARAMETERDEFAULTS="BlockParameterDefaults";
ANNOTATIONDEFAULTS="AnnotationDefaults";
LINEDEFAULTS="LineDefaults";
SYSTEM="System";
BLOCK="Block";
LINKDATA="LinkData";
DIALOGPARAMETERS="DialogParameters";
LIST="List";
LINE="Line";
BRANCH="Branch";
ANNOTATION="Annotation";
STATEFLOW="Stateflow";
DEBUG="debug";
STATEFONTS="stateFontS";
TRANSITIONFONTS="transitionFontS";
SUBVIEWS="subviewS";
TRANSITION="transition";
SRC="src";
DST="dst";
SLIDE="slide";
DATA="data";
FIXPTTYPE="fixptType";
PROPS="props";
RANGE="range";
ARRAY="array";
INSTANCE="instance";
TARGET="target";
JUNCTION="junction";
EVENT="event";
//INF_SYMB="Inf"
}
protected COMMA		: ','	;
protected SEMI		: ';'	;
LBRACE			: '{'	;
RBRACE			: '}'	;
protected LBRACKET	: '['	;
protected RBRACKET	: ']'	;

// string literals

protected STRING_LITERAL_NORMAL
 : '"'! (ESC|~('"'|'\\'|'\n'|'\r'))* '"'!
 ;

protected // not a token; only invoked by another rule.
ESC
 :    '\\'
  (    'n'
  |    'r'
  |    't'
  |    'b'
  |    'f'
  |    '"'
  |    '\''
  |    '\\'
  |    ('u')+
        HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
  )
 ;

protected HEX_DIGIT
 :
	( ('0'..'9') | ('a'..'f') | ('A'..'F') )
 ;

protected MEGA_STRING
 :
	STRING_LITERAL_NORMAL (NEWLN!)+ ('\t'! | ' '!)* STRING_LITERAL {newline();}

 ;

STRING_LITERAL
 :
	(STRING_LITERAL_NORMAL (NEWLN)+ ('\t' | ' ')* '"') => MEGA_STRING	{ _ttype = MEGA_STRING; }
	| STRING_LITERAL_NORMAL				{ _ttype = STRING_LITERAL_NORMAL; }
 ;

VECTOR
 :
	LBRACKET ( IDENT ( (COMMA | SEMI | WS) (WS!)* IDENT)* )? RBRACKET
 ;

// Whitespace -- ignored
WS
 : ( ' '
   | '\t'
   | '\f'

   // handle newlines
   | NEWLN
     { newline(); }
   )

   // now the overall whitespace action -- skip it!
   { _ttype = ANTLR_USE_NAMESPACE(antlr)Token::SKIP; }
 ;

protected NEWLN
 : (//	options { generateAmbigWarnings=false; } :
//	"\r\n"
//   |	('\n' | '\r')
//   |	'\r'
	'\n'
//     |  "\r\n"
     |  '\r'
   )
 ;
COMMENT
 : "#" (~('\n'|'\r'))* 	{ _ttype = ANTLR_USE_NAMESPACE(antlr)Token::SKIP; }
 ;

IDENT
  options {testLiterals=true;}
// : ('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'-')*
 : ('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'+'|'-'|'*'|'/'|'.'|'!'|'<'|'>'|'=')+
 ;
