//### This file created by BYACC 1.8(/Java extension  0.92)
//### Java capabilities added 7 Jan 97, Bob Jamison
//### Updated : 27 Nov 97  -- Bob Jamison, Joe Nieten
//###           01 Jan 98  -- Bob Jamison -- fixed generic semantic constructor
//###           01 Jun 99  -- Bob Jamison -- added Runnable support
//### Please send bug reports to rjamison@lincom-asg.com
//### static char yysccsid[] = "@(#)yaccpar	1.8 (Berkeley) 01/20/90";
//###
//### This version of BYACC/Java has been modified by Jeff Tsay with the
//###  following changes:
//### 1) Arrays of shorts and the array of rules have been moved to
//###    external tables because of the size limitation of .java files.
//### 2) New instances of parserval are created for the return value of
//###    each rule, so that assignments to the fields of parserval do not
//###    corrupt previous data.
//### Bob Jamison's version 0.93 fixes nil definitions, but the source
//### code was unavailable.



//#line 120 "jparser.y"
package ptolemy.lang.java;

import java.util.Iterator;
import java.util.LinkedList;
import java.io.IOException;
import java.io.FileInputStream;

import ptolemy.lang.*;

//#line 30 "parser.java"
import java.io.FileReader;
import java.io.StreamTokenizer;
import java.io.LineNumberReader;




//#####################################################################
// class: parser
// does : encapsulates yacc() parser functionality in a Java
//        class for quick code development
//#####################################################################
public class parser
{

boolean yydebug;        //do I want debug output?
int yynerrs;            //number of errors so far
int yyerrflag;          //was there an error?
int yychar;             //the current working character

//########## MESSAGES ##########
//###############################################################
// method: debug
//###############################################################
void debug(String msg)
{
  if (yydebug)
    System.out.println(msg);
}

//########## STATE STACK ##########
final static int YYSTACKSIZE = 500;  //maximum stack size
int statestk[],stateptr;             //state stack
//###############################################################
// methods: state stack push,pop,drop,peek
//###############################################################
void state_push(int state)
{
  if (stateptr>=YYSTACKSIZE)         //overflowed?
    return;
  statestk[++stateptr]=state;
}
int state_pop()
{
  if (stateptr<0)                    //underflowed?
    return -1;
  return statestk[stateptr--];
}
void state_drop(int cnt)
{
int ptr;
  ptr=stateptr-cnt;
  if (ptr<0)
    return;
  stateptr = ptr;
}
int state_peek(int relative)
{
int ptr;
  ptr=stateptr-relative;
  if (ptr<0)
    return -1;
  return statestk[ptr];
}
//###############################################################
// method: init_stacks : allocate and prepare stacks
//###############################################################
boolean init_stacks()
{
  statestk = new int[YYSTACKSIZE];
  stateptr = -1;
  val_init();
  return true;
}
//###############################################################
// method: dump_stacks : show n levels of the stacks
//###############################################################
void dump_stacks(int count)
{
int i;
  System.out.println("=index==state====value=     s:"+stateptr+"  v:"+valptr);
  for (i=0;i<count;i++)
    System.out.println(" "+i+"    "+statestk[i]+"      "+valstk[i]);
  System.out.println("======================");
}
static short[] read_short_table(String filename, int size)
{
  FileReader fileReader = null;
  try {
    fileReader = new FileReader(filename);
  } catch (IOException e) {
    throw new RuntimeException(filename + " not found");
  }
  StreamTokenizer tokenizer = new StreamTokenizer(fileReader);
  short[] retval = new short[size];
  for (int i = 0; i < size; i++) {
      try {
        tokenizer.nextToken();
      } catch (IOException e) {
        throw new RuntimeException(filename + "does not contain enough entries");
      }
      // this shouldn't happen if we didn't call parseNumbers() - BUG in JDK 1.2.1
      retval[i] = (short) tokenizer.nval;
  }
  try {
    fileReader.close();
  } catch (IOException e) {
    throw new RuntimeException(filename + " could not be closed");
  }
  return retval;
}
static String[] read_string_table(String filename, int size)
{
  FileReader fileReader = null;
  try {
    fileReader = new FileReader(filename);
  } catch (IOException e) {
    throw new RuntimeException(filename + " not found");
  }
  LineNumberReader lineReader = new LineNumberReader(fileReader);
  String[] retval = new String[size];
  for (int i = 0; i < size; i++) {
      try {
        retval[i] = lineReader.readLine();
      } catch (IOException e) {
        throw new RuntimeException(filename + "does not contain enough entries");
      }
  }
  try {
    fileReader.close();
  } catch (IOException e) {
    throw new RuntimeException(filename + " could not be closed");
  }
  return retval;
}


//########## SEMANTIC VALUES ##########
//public class parsersemantic is defined in parserval.java


String   yytext;//user variable to return contextual strings
parserval yyval; //used to return semantic vals from action routines
parserval yylval;//the 'lval' (result) I got from yylex()
parserval valstk[];
int valptr;
//###############################################################
// methods: value stack push,pop,drop,peek.
//###############################################################
void val_init()
{
  valstk=new parserval[YYSTACKSIZE];
  yyval=new parserval(0);
  yylval=new parserval(0);
  valptr=-1;
}
void val_push(parserval val)
{
  if (valptr>=YYSTACKSIZE)
    return;
  valstk[++valptr]=val;
}
parserval val_pop()
{
  if (valptr<0)
    return new parserval(-1);
  return valstk[valptr--];
}
void val_drop(int cnt)
{
int ptr;
  ptr=valptr-cnt;
  if (ptr<0)
    return;
  valptr = ptr;
}
parserval val_peek(int relative)
{
int ptr;
  ptr=valptr-relative;
  if (ptr<0)
    return new parserval(-1);
  return valstk[ptr];
}
//#### end semantic value section ####
public final static short ABSTRACT=257;
public final static short BOOLEAN=258;
public final static short BREAK=259;
public final static short BYTE=260;
public final static short CASE=261;
public final static short CATCH=262;
public final static short CHAR=263;
public final static short CLASS=264;
public final static short CONTINUE=265;
public final static short DEFAULT=266;
public final static short DO=267;
public final static short DOUBLE=268;
public final static short ELSE=269;
public final static short EXTENDS=270;
public final static short FINAL=271;
public final static short FINALLY=272;
public final static short FLOAT=273;
public final static short FOR=274;
public final static short IF=275;
public final static short IMPLEMENTS=276;
public final static short IMPORT=277;
public final static short INSTANCEOF=278;
public final static short INT=279;
public final static short INTERFACE=280;
public final static short LONG=281;
public final static short NATIVE=282;
public final static short NEW=283;
public final static short _NULL=284;
public final static short PACKAGE=285;
public final static short PRIVATE=286;
public final static short PROTECTED=287;
public final static short PUBLIC=288;
public final static short RETURN=289;
public final static short SHORT=290;
public final static short STATIC=291;
public final static short STRICTFP=292;
public final static short SUPER=293;
public final static short SWITCH=294;
public final static short SYNCHRONIZED=295;
public final static short THIS=296;
public final static short THROW=297;
public final static short THROWS=298;
public final static short TRANSIENT=299;
public final static short TRY=300;
public final static short VOID=301;
public final static short VOLATILE=302;
public final static short WHILE=303;
public final static short CONST=304;
public final static short GOTO=305;
public final static short TRUE=306;
public final static short FALSE=307;
public final static short IDENTIFIER=308;
public final static short INT_LITERAL=309;
public final static short LONG_LITERAL=310;
public final static short FLOAT_LITERAL=311;
public final static short DOUBLE_LITERAL=312;
public final static short CHARACTER_LITERAL=313;
public final static short STRING_LITERAL=314;
public final static short EMPTY_DIM=315;
public final static short CAND=316;
public final static short COR=317;
public final static short EQ=318;
public final static short NE=319;
public final static short LE=320;
public final static short GE=321;
public final static short LSHIFTL=322;
public final static short ASHIFTR=323;
public final static short LSHIFTR=324;
public final static short PLUS_ASG=325;
public final static short MINUS_ASG=326;
public final static short MULT_ASG=327;
public final static short DIV_ASG=328;
public final static short REM_ASG=329;
public final static short LSHIFTL_ASG=330;
public final static short ASHIFTR_ASG=331;
public final static short LSHIFTR_ASG=332;
public final static short AND_ASG=333;
public final static short XOR_ASG=334;
public final static short OR_ASG=335;
public final static short PLUSPLUS=336;
public final static short MINUSMINUS=337;
public final static short YYERRCODE=256;
final static int NRULES=293;
final static short yylhs[] = read_short_table("yylhs.tbl", NRULES - 2);
final static short yylen[] = read_short_table("yylen.tbl", NRULES - 2);
final static int NSTATES=543;
final static short yydefred[] = read_short_table("yydefred.tbl", NSTATES);
final static int YYDGOTOSIZE=107;
final static short yydgoto[] = read_short_table("yydgoto.tbl", YYDGOTOSIZE);
final static short yysindex[] = read_short_table("yysindex.tbl", NSTATES);
final static short yyrindex[] = read_short_table("yyrindex.tbl", NSTATES);
final static int YYGINDEXSIZE=107;
final static short yygindex[] = read_short_table("yygindex.tbl", YYGINDEXSIZE);
final static int YYTABLESIZE=5344;
final static short yytable[] = read_short_table("yytable.tbl", YYTABLESIZE + 1);
final static short yycheck[] = read_short_table("yycheck.tbl", YYTABLESIZE + 1);
final static short YYFINAL=2;
final static short YYMAXTOKEN=337;
final static String yyname[] = {
"end-of-file",null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,"'!'",null,null,null,"'%'","'&'",null,"'('","')'","'*'","'+'",
"','","'-'","'.'","'/'",null,null,null,null,null,null,null,null,null,null,"':'",
"';'","'<'","'='","'>'","'?'",null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,"'['",null,"']'","'^'",null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,"'{'","'|'","'}'","'~'",null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,"ABSTRACT","BOOLEAN","BREAK",
"BYTE","CASE","CATCH","CHAR","CLASS","CONTINUE","DEFAULT","DO","DOUBLE","ELSE",
"EXTENDS","FINAL","FINALLY","FLOAT","FOR","IF","IMPLEMENTS","IMPORT",
"INSTANCEOF","INT","INTERFACE","LONG","NATIVE","NEW","_NULL","PACKAGE",
"PRIVATE","PROTECTED","PUBLIC","RETURN","SHORT","STATIC","STRICTFP","SUPER",
"SWITCH","SYNCHRONIZED","THIS","THROW","THROWS","TRANSIENT","TRY","VOID",
"VOLATILE","WHILE","CONST","GOTO","TRUE","FALSE","IDENTIFIER","INT_LITERAL",
"LONG_LITERAL","FLOAT_LITERAL","DOUBLE_LITERAL","CHARACTER_LITERAL",
"STRING_LITERAL","EMPTY_DIM","CAND","COR","EQ","NE","LE","GE","LSHIFTL",
"ASHIFTR","LSHIFTR","PLUS_ASG","MINUS_ASG","MULT_ASG","DIV_ASG","REM_ASG",
"LSHIFTL_ASG","ASHIFTR_ASG","LSHIFTR_ASG","AND_ASG","XOR_ASG","OR_ASG",
"PLUSPLUS","MINUSMINUS",
};
final static String yyrule[] = read_string_table("yyrule.tbl", NRULES - 2);
//#line 1395 "jparser.y"

protected void init(String filename) throws IOException {
  _filename = filename;
  _lexer = new Yylex(new FileInputStream(_filename));
}

protected int yylex()
{
  int retval;

  try {
    retval = _lexer.yylex();

    yylval = _lexer.getParserVal();

  } catch (IOException e) {

    throw new RuntimeException("lexical error");
  }

  return retval;
}

protected static final LinkedList cons(Object obj)
{
  return cons(obj, new LinkedList());
}

protected static final LinkedList cons(Object obj, LinkedList list)
{
  if ((obj != null) && (obj != AbsentTreeNode.instance)) {
     list.addFirst(obj);
  }

  return list;
}

protected static final LinkedList append(LinkedList list, Object obj)
{
  list.addLast(obj);

  return list;
}


protected static final Object appendLists(LinkedList list1, LinkedList list2)
{
  list1.addAll(list2);

  return list1;
}

/** Place to put the finished AST. */
protected CompileUnitNode _theAST;

public CompileUnitNode getAST() { return _theAST; }

protected void yyerror(String msg)
{
  String errMsg = "parse error in " + _filename + ": " + msg;
  if (_lexer != null) {
     errMsg += " on line " + _lexer.lineNumber();
  }
  ApplicationUtility.error(errMsg);
}

/** An array type with given ELEMENTTYPE and DIMS dimensions.  When
 *  DIMS=0, equals ELEMENTTYPE.
 */
protected static TypeNode makeArrayType(TypeNode elementType, int dims)
{
  while (dims > 0) {
	   elementType = new ArrayTypeNode(elementType);
	   dims -= 1;
  }
  return elementType;
}

protected String _filename = null;
protected Yylex _lexer = null;
//#line 1980 "parser.java"
//###############################################################
// method: yylexdebug : check lexer state
//###############################################################
void yylexdebug(int state,int ch)
{
String s=null;
  if (ch < 0) ch=0;
  if (ch <= YYMAXTOKEN) //check index bounds
     s = yyname[ch];    //now get it
  if (s==null)
    s = "illegal-symbol";
  debug("state "+state+", reading "+ch+" ("+s+")");
}



//###############################################################
// method: yyparse : parse input and execute indicated items
//###############################################################
int yyparse()
{
int yyn;       //next next thing to do
int yym;       //
int yystate;   //current parsing state from state table
String yys;    //current token string
boolean doaction;
  init_stacks();
  yynerrs = 0;
  yyerrflag = 0;
  yychar = -1;          //impossible char forces a read
  yystate=0;            //initial state
  state_push(yystate);  //save it
  while (true) //until parsing is done, either correctly, or w/error
    {
    doaction=true;
    if (yydebug) debug("loop"); 
    //#### NEXT ACTION (from reduction table)
    for (yyn=yydefred[yystate];yyn==0;yyn=yydefred[yystate])
      {
      if (yydebug) debug("yyn:"+yyn+"  state:"+yystate+"  char:"+yychar);
      if (yychar < 0)      //we want a char?
        {
        yychar = yylex();  //get next token
        //#### ERROR CHECK ####
        if (yychar < 0)    //it it didn't work/error
          {
          yychar = 0;      //change it to default string (no -1!)
          if (yydebug)
            yylexdebug(yystate,yychar);
          }
        }//yychar<0
      yyn = yysindex[yystate];  //get amount to shift by (shift index)
      if ((yyn != 0) && (yyn += yychar) >= 0 &&
          yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
        {
        if (yydebug)
          debug("state "+yystate+", shifting to state "+yytable[yyn]+"");
        //#### NEXT STATE ####
        yystate = yytable[yyn];//we are in a new state
        state_push(yystate);   //save it
        val_push(yylval);      //push our lval as the input for next rule
        yychar = -1;           //since we have 'eaten' a token, say we need another
        if (yyerrflag > 0)     //have we recovered an error?
           --yyerrflag;        //give ourselves credit
        doaction=false;        //but don't process yet
        break;   //quit the yyn=0 loop
        }

    yyn = yyrindex[yystate];  //reduce
    if ((yyn !=0 ) && (yyn += yychar) >= 0 &&
            yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
      {   //we reduced!
      if (yydebug) debug("reduce");
      yyn = yytable[yyn];
      doaction=true; //get ready to execute
      break;         //drop down to actions
      }
    else //ERROR RECOVERY
      {
      if (yyerrflag==0)
        {
        yyerror("syntax error");
        yynerrs++;
        }
      if (yyerrflag < 3) //low error count?
        {
        yyerrflag = 3;
        while (true)   //do until break
          {
          if (stateptr<0)   //check for under & overflow here
            {
            yyerror("stack underflow. aborting...");  //note lower case 's'
            return 1;
            }
          yyn = yysindex[state_peek(0)];
          if ((yyn != 0) && (yyn += YYERRCODE) >= 0 &&
                    yyn <= YYTABLESIZE && yycheck[yyn] == YYERRCODE)
            {
            if (yydebug)
              debug("state "+state_peek(0)+", error recovery shifting to state "+yytable[yyn]+" ");
            yystate = yytable[yyn];
            state_push(yystate);
            val_push(yylval);
            doaction=false;
            break;
            }
          else
            {
            if (yydebug)
              debug("error recovery discarding state "+state_peek(0)+" ");
            if (stateptr<0)   //check for under & overflow here
              {
              yyerror("Stack underflow. aborting...");  //capital 'S'
              return 1;
              }
            state_pop();
            val_pop();
            }
          }
        }
      else            //discard this token
        {
        if (yychar == 0)
          return 1; //yyabort
        if (yydebug)
          {
          yys = null;
          if (yychar <= YYMAXTOKEN) yys = yyname[yychar];
          if (yys == null) yys = "illegal-symbol";
          debug("state "+yystate+", error recovery discards token "+yychar+" ("+yys+")");
          }
        yychar = -1;  //read another
        }
      }//end error recovery
    }//yyn=0 loop
    if (!doaction)   //any reason not to proceed?
      continue;      //skip action
    yym = yylen[yyn];          //get count of terminals on rhs
    if (yydebug)
      debug("state "+yystate+", reducing "+yym+" by rule "+yyn+" ("+yyrule[yyn]+")");
    if (yym>0) { //if count of rhs not 'nil'
       try {
         yyval = (parserval) val_peek(yym-1).clone(); //get current semantic value
       } catch (CloneNotSupportedException e) {
         yyerror("Clone not supported");
       }
    } else {
      yyval = new parserval();
    }
    switch(yyn)
      {
//########## USER-SUPPLIED ACTIONS ##########
case 1:
//#line 202 "jparser.y"
{ _theAST = (CompileUnitNode) val_peek(0).obj; }
break;
case 2:
//#line 208 "jparser.y"
{ yyval.obj = new IntLitNode(val_peek(0).sval); }
break;
case 3:
//#line 210 "jparser.y"
{ yyval.obj = new LongLitNode(val_peek(0).sval); }
break;
case 4:
//#line 212 "jparser.y"
{ yyval.obj = new FloatLitNode(val_peek(0).sval); }
break;
case 5:
//#line 214 "jparser.y"
{ yyval.obj = new DoubleLitNode(val_peek(0).sval); }
break;
case 6:
//#line 216 "jparser.y"
{ yyval.obj = new BoolLitNode("true"); }
break;
case 7:
//#line 218 "jparser.y"
{ yyval.obj = new BoolLitNode("false"); }
break;
case 8:
//#line 220 "jparser.y"
{ yyval.obj = new CharLitNode(val_peek(0).sval); }
break;
case 9:
//#line 222 "jparser.y"
{ yyval.obj = new StringLitNode(val_peek(0).sval); }
break;
case 14:
//#line 243 "jparser.y"
{ yyval.obj = BoolTypeNode.instance; }
break;
case 15:
//#line 245 "jparser.y"
{ yyval.obj = CharTypeNode.instance; }
break;
case 16:
//#line 247 "jparser.y"
{ yyval.obj = ByteTypeNode.instance; }
break;
case 17:
//#line 249 "jparser.y"
{ yyval.obj = ShortTypeNode.instance; }
break;
case 18:
//#line 251 "jparser.y"
{ yyval.obj = IntTypeNode.instance; }
break;
case 19:
//#line 253 "jparser.y"
{ yyval.obj = FloatTypeNode.instance; }
break;
case 20:
//#line 255 "jparser.y"
{ yyval.obj = LongTypeNode.instance; }
break;
case 21:
//#line 257 "jparser.y"
{ yyval.obj = DoubleTypeNode.instance; }
break;
case 22:
//#line 265 "jparser.y"
{ yyval.obj = new TypeNameNode((NameNode) val_peek(0).obj); }
break;
case 23:
//#line 270 "jparser.y"
{ yyval.obj = new ArrayTypeNode((TypeNode) val_peek(1).obj); }
break;
case 24:
//#line 279 "jparser.y"
{ yyval.obj = new CompileUnitNode((TreeNode) val_peek(2).obj, (LinkedList) val_peek(1).obj, (LinkedList) val_peek(0).obj);  }
break;
case 25:
//#line 284 "jparser.y"
{ yyval.obj = val_peek(1).obj; }
break;
case 26:
//#line 286 "jparser.y"
{ yyval.obj = AbsentTreeNode.instance; }
break;
case 27:
//#line 291 "jparser.y"
{ yyval.obj = new LinkedList(); }
break;
case 28:
//#line 293 "jparser.y"
{ yyval.obj = cons(val_peek(1).obj, (LinkedList) val_peek(0).obj); }
break;
case 29:
//#line 299 "jparser.y"
{ yyval.obj = new LinkedList(); }
break;
case 30:
//#line 301 "jparser.y"
{ yyval.obj = cons(val_peek(1).obj, (LinkedList) val_peek(0).obj); }
break;
case 31:
//#line 303 "jparser.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 36:
//#line 321 "jparser.y"
{ yyval.obj = new ImportNode((NameNode) val_peek(1).obj); }
break;
case 37:
//#line 326 "jparser.y"
{ yyval.obj = new ImportOnDemandNode((NameNode) val_peek(3).obj); }
break;
case 38:
//#line 338 "jparser.y"
{ yyval.obj = new ClassDeclNode(val_peek(5).ival, (NameNode) val_peek(3).obj, (LinkedList) val_peek(1).obj,
           (LinkedList) val_peek(0).obj, (TreeNode) val_peek(2).obj); }
break;
case 39:
//#line 353 "jparser.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 40:
//#line 355 "jparser.y"
{ yyval.obj = AbsentTreeNode.instance; }
break;
case 41:
//#line 363 "jparser.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 42:
//#line 365 "jparser.y"
{ yyval.obj = new LinkedList(); }
break;
case 43:
//#line 374 "jparser.y"
{
     yyval.obj = val_peek(1).obj; /* in the original, an ABSENT tree is added*/
   }
break;
case 44:
//#line 380 "jparser.y"
{ }
break;
case 45:
//#line 382 "jparser.y"
{ yyval.obj = new LinkedList(); }
break;
case 47:
//#line 388 "jparser.y"
{ yyval.obj = appendLists((LinkedList) val_peek(1).obj, (LinkedList) val_peek(0).obj); }
break;
case 49:
//#line 397 "jparser.y"
{ yyval.obj = cons(val_peek(0).obj); }
break;
case 50:
//#line 399 "jparser.y"
{ yyval.obj = cons(val_peek(0).obj); }
break;
case 51:
//#line 401 "jparser.y"
{ yyval.obj = cons(val_peek(0).obj); }
break;
case 52:
//#line 404 "jparser.y"
{ yyval.obj = cons(val_peek(0).obj); }
break;
case 53:
//#line 410 "jparser.y"
{ yyval.obj = cons(val_peek(0).obj); }
break;
case 54:
//#line 412 "jparser.y"
{ yyval.obj = cons(val_peek(1).obj); }
break;
case 55:
//#line 420 "jparser.y"
{
      Modifier.checkFieldModifiers(val_peek(3).ival);
	     LinkedList result = new LinkedList();

      LinkedList varDecls = (LinkedList) val_peek(1).obj;
      Iterator itr = varDecls.iterator();

	     while (itr.hasNext()) {
		     DeclaratorNode decl = (DeclaratorNode) itr.next();
		     result = cons(new FieldDeclNode(val_peek(3).ival,
						            makeArrayType((TypeNode) val_peek(2).obj, decl.getDims()),
						            decl.getName(), decl.getInitExpr()),
				               result);
		   }

      yyval.obj = result;
   }
break;
case 56:
//#line 446 "jparser.y"
{ }
break;
case 57:
//#line 448 "jparser.y"
{ yyval.ival = Modifier.NO_MOD; }
break;
case 58:
//#line 452 "jparser.y"
{ yyval.ival = val_peek(0).ival; }
break;
case 59:
//#line 454 "jparser.y"
{
     yyval.ival = (val_peek(1).ival | val_peek(0).ival);
		  if ((val_peek(1).ival & val_peek(0).ival) != 0) {
		     yyerror("repeated modifier");
     }
   }
break;
case 60:
//#line 465 "jparser.y"
{ yyval.ival = Modifier.PUBLIC_MOD; }
break;
case 61:
//#line 467 "jparser.y"
{ yyval.ival = Modifier.PROTECTED_MOD;  }
break;
case 62:
//#line 469 "jparser.y"
{ yyval.ival = Modifier.PRIVATE_MOD;  }
break;
case 63:
//#line 472 "jparser.y"
{ yyval.ival = Modifier.STATIC_MOD;  }
break;
case 64:
//#line 474 "jparser.y"
{ yyval.ival = Modifier.FINAL_MOD;  }
break;
case 65:
//#line 477 "jparser.y"
{ yyval.ival = Modifier.ABSTRACT_MOD;  }
break;
case 66:
//#line 479 "jparser.y"
{ yyval.ival = Modifier.NATIVE_MOD;  }
break;
case 67:
//#line 481 "jparser.y"
{ yyval.ival = Modifier.SYNCHRONIZED_MOD;  }
break;
case 68:
//#line 484 "jparser.y"
{ yyval.ival = Modifier.TRANSIENT_MOD;  }
break;
case 69:
//#line 486 "jparser.y"
{ yyval.ival = Modifier.VOLATILE_MOD;  }
break;
case 70:
//#line 488 "jparser.y"
{ yyval.ival = Modifier.STRICTFP_MOD; }
break;
case 71:
//#line 499 "jparser.y"
{ yyval.obj = cons(val_peek(0).obj); }
break;
case 72:
//#line 501 "jparser.y"
{ yyval.obj = cons(val_peek(0).obj, (LinkedList) val_peek(2).obj); }
break;
case 73:
//#line 506 "jparser.y"
{ yyval.obj = new DeclaratorNode(val_peek(0).ival, (NameNode) val_peek(1).obj, AbsentTreeNode.instance); }
break;
case 74:
//#line 508 "jparser.y"
{ yyval.obj = new DeclaratorNode(val_peek(2).ival, (NameNode) val_peek(3).obj, (ExprNode) val_peek(0).obj); }
break;
case 77:
//#line 524 "jparser.y"
{
     Modifier.checkMethodModifiers(val_peek(8).ival);
	    yyval.obj = new MethodDeclNode(val_peek(8).ival, (NameNode) val_peek(6).obj, (LinkedList) val_peek(4).obj,
                             (LinkedList) val_peek(1).obj, (TreeNode) val_peek(0).obj,
                             makeArrayType((TypeNode) val_peek(7).obj, val_peek(2).ival));
   }
break;
case 78:
//#line 532 "jparser.y"
{
     Modifier.checkMethodModifiers(val_peek(8).ival);
	    yyval.obj = new MethodDeclNode(val_peek(8).ival, (NameNode) val_peek(6).obj, (LinkedList) val_peek(4).obj,
                             (LinkedList) val_peek(1).obj, (TreeNode) val_peek(0).obj,
                             makeArrayType((TypeNode) val_peek(7).obj, val_peek(2).ival));
   }
break;
case 79:
//#line 542 "jparser.y"
{ yyval.obj = VoidTypeNode.instance; }
break;
case 80:
//#line 550 "jparser.y"
{ }
break;
case 81:
//#line 552 "jparser.y"
{ yyval.obj = new LinkedList();  }
break;
case 82:
//#line 557 "jparser.y"
{ yyval.obj = cons(val_peek(0).obj); }
break;
case 83:
//#line 559 "jparser.y"
{ yyval.obj = cons(val_peek(2).obj, (LinkedList) val_peek(0).obj); }
break;
case 84:
//#line 564 "jparser.y"
{
     Modifier.checkParameterModifiers(val_peek(3).ival); 
     yyval.obj = new ParameterNode(val_peek(3).ival, makeArrayType((TypeNode) val_peek(2).obj, val_peek(0).ival),
          (NameNode) val_peek(1).obj);
   }
break;
case 85:
//#line 575 "jparser.y"
{ }
break;
case 86:
//#line 577 "jparser.y"
{ yyval.obj = new LinkedList(); }
break;
case 87:
//#line 582 "jparser.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 88:
//#line 587 "jparser.y"
{ yyval.obj = cons(val_peek(0).obj); }
break;
case 89:
//#line 589 "jparser.y"
{ yyval.obj = cons(val_peek(2).obj, (LinkedList) val_peek(0).obj); }
break;
case 91:
//#line 598 "jparser.y"
{ yyval.obj = AbsentTreeNode.instance; }
break;
case 92:
//#line 607 "jparser.y"
{
      Modifier.checkConstructorModifiers(val_peek(9).ival);
	     yyval.obj = new ConstructorDeclNode(val_peek(9).ival,
            new NameNode(AbsentTreeNode.instance, val_peek(8).sval), (LinkedList) val_peek(6).obj,
            (LinkedList) val_peek(4).obj, new BlockNode((LinkedList) val_peek(1).obj),
            (ConstructorCallNode) val_peek(2).obj);
   }
break;
case 93:
//#line 616 "jparser.y"
{
     Modifier.checkConstructorModifiers(val_peek(8).ival);
	    yyval.obj = new ConstructorDeclNode(val_peek(8).ival,
           new NameNode(AbsentTreeNode.instance, val_peek(7).sval), (LinkedList) val_peek(5).obj,
           (LinkedList) val_peek(3).obj, new BlockNode((LinkedList) val_peek(1).obj),
           new SuperConstructorCallNode(new LinkedList()));
	  }
break;
case 94:
//#line 631 "jparser.y"
{ yyval.obj = new ThisConstructorCallNode((LinkedList) val_peek(2).obj); }
break;
case 95:
//#line 633 "jparser.y"
{ yyval.obj = new SuperConstructorCallNode((LinkedList) val_peek(2).obj); }
break;
case 96:
//#line 641 "jparser.y"
{ yyval.obj = new StaticInitNode((BlockNode) val_peek(0).obj); }
break;
case 97:
//#line 646 "jparser.y"
{ yyval.obj = new InstanceInitNode((BlockNode) val_peek(0).obj); }
break;
case 98:
//#line 654 "jparser.y"
{
     Modifier.checkInterfaceModifiers(val_peek(4).ival);
     yyval.obj = new InterfaceDeclNode(val_peek(4).ival, (NameNode) val_peek(2).obj, (LinkedList) val_peek(1).obj, (LinkedList) val_peek(0).obj);
   }
break;
case 99:
//#line 669 "jparser.y"
{ }
break;
case 100:
//#line 671 "jparser.y"
{ yyval.obj = new LinkedList(); }
break;
case 101:
//#line 676 "jparser.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 102:
//#line 683 "jparser.y"
{ yyval.obj = val_peek(1).obj; }
break;
case 103:
//#line 688 "jparser.y"
{ yyval.obj = new LinkedList(); }
break;
case 104:
//#line 690 "jparser.y"
{ yyval.obj = appendLists((LinkedList) val_peek(1).obj, (LinkedList) val_peek(0).obj); }
break;
case 106:
//#line 696 "jparser.y"
{ yyval.obj = cons(val_peek(0).obj); }
break;
case 107:
//#line 698 "jparser.y"
{ yyval.obj = cons(val_peek(0).obj); }
break;
case 108:
//#line 700 "jparser.y"
{ yyval.obj = cons(val_peek(1).obj); }
break;
case 109:
//#line 705 "jparser.y"
{
     int modifiers = val_peek(3).ival;
     modifiers |= (Modifier.STATIC_MOD | Modifier.FINAL_MOD);

     Modifier.checkConstantFieldModifiers(modifiers);
     LinkedList varDecls = (LinkedList) val_peek(1).obj;
     Iterator itr = varDecls.iterator();

	    LinkedList result = new LinkedList();

	    while (itr.hasNext()) {
		    DeclaratorNode decl = (DeclaratorNode) itr.next();
		    result = cons(new FieldDeclNode(modifiers,
                     makeArrayType((TypeNode) val_peek(2).obj, decl.getDims()),
						          decl.getName(), decl.getInitExpr()), result);
		  }

	    yyval.obj = result;
	  }
break;
case 110:
//#line 729 "jparser.y"
{
     Modifier.checkMethodSignatureModifiers(val_peek(8).ival);
	    yyval.obj = new MethodDeclNode(val_peek(8).ival | Modifier.ABSTRACT_MOD, (NameNode) val_peek(6).obj,
                             (LinkedList) val_peek(4).obj, (LinkedList) val_peek(1).obj,
                             AbsentTreeNode.instance,
                             makeArrayType((TypeNode) val_peek(7).obj, val_peek(2).ival));
   }
break;
case 111:
//#line 738 "jparser.y"
{
     Modifier.checkMethodSignatureModifiers(val_peek(8).ival);
	    yyval.obj = new MethodDeclNode(val_peek(8).ival | Modifier.ABSTRACT_MOD, (NameNode) val_peek(6).obj,
                             (LinkedList) val_peek(4).obj, (LinkedList) val_peek(1).obj,
                             AbsentTreeNode.instance,
                             makeArrayType((TypeNode) val_peek(7).obj, val_peek(2).ival));
   }
break;
case 112:
//#line 753 "jparser.y"
{ yyval.obj = new ArrayInitNode((LinkedList) val_peek(1).obj); }
break;
case 113:
//#line 755 "jparser.y"
{ yyval.obj = new ArrayInitNode((LinkedList) val_peek(2).obj); }
break;
case 114:
//#line 757 "jparser.y"
{ yyval.obj = new ArrayInitNode(new LinkedList()); }
break;
case 115:
//#line 763 "jparser.y"
{ yyval.obj = cons(val_peek(0).obj); }
break;
case 116:
//#line 765 "jparser.y"
{ yyval.obj = append((LinkedList) val_peek(2).obj, val_peek(0).obj); }
break;
case 119:
//#line 780 "jparser.y"
{ yyval.obj = new BlockNode((LinkedList) val_peek(1).obj); }
break;
case 120:
//#line 784 "jparser.y"
{ }
break;
case 121:
//#line 786 "jparser.y"
{ yyval.obj = new LinkedList(); }
break;
case 122:
//#line 791 "jparser.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 123:
//#line 793 "jparser.y"
{ yyval.obj = appendLists((LinkedList) val_peek(1).obj, (LinkedList) val_peek(0).obj); }
break;
case 124:
//#line 798 "jparser.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 125:
//#line 800 "jparser.y"
{ yyval.obj = cons(val_peek(0).obj); }
break;
case 126:
//#line 802 "jparser.y"
{ yyval.obj = cons(new UserTypeDeclStmtNode((UserTypeDeclNode) val_peek(0).obj)); }
break;
case 127:
//#line 810 "jparser.y"
{
     Modifier.checkLocalVariableModifiers(val_peek(3).ival);

     LinkedList varDecls = (LinkedList) val_peek(1).obj;
     LinkedList result = new LinkedList();

     Iterator itr = varDecls.iterator();

	    while (itr.hasNext()) {
		    DeclaratorNode decl = (DeclaratorNode) itr.next();
		    result = cons(new VarDeclNode(val_peek(3).ival,
                     makeArrayType((TypeNode) val_peek(2).obj, decl.getDims()),
                     decl.getName(), decl.getInitExpr()), result);
     }
     yyval.obj = result;
   }
break;
case 128:
//#line 828 "jparser.y"
{
     LinkedList varDecls = (LinkedList) val_peek(1).obj;
     LinkedList result = new LinkedList();

     Iterator itr = varDecls.iterator();

	    while (itr.hasNext()) {
		    DeclaratorNode decl = (DeclaratorNode) itr.next();
  	    result = cons(new VarDeclNode(Modifier.NO_MOD,
                     makeArrayType((TypeNode) val_peek(2).obj, decl.getDims()),
                     decl.getName(), decl.getInitExpr()), result);
     }
     yyval.obj = result;
   }
break;
case 131:
//#line 850 "jparser.y"
{ yyval.obj = new ExprStmtNode((ExprNode) val_peek(1).obj); }
break;
case 137:
//#line 862 "jparser.y"
{ yyval.obj = new EmptyStmtNode(); }
break;
case 138:
//#line 870 "jparser.y"
{ yyval.obj = new LabeledStmtNode((NameNode) val_peek(2).obj, (StatementNode) val_peek(0).obj); }
break;
case 139:
//#line 878 "jparser.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 140:
//#line 880 "jparser.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 141:
//#line 882 "jparser.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 142:
//#line 884 "jparser.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 143:
//#line 886 "jparser.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 144:
//#line 888 "jparser.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 145:
//#line 890 "jparser.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 146:
//#line 898 "jparser.y"
{ yyval.obj = new IfStmtNode((ExprNode) val_peek(2).obj, (StatementNode) val_peek(0).obj, AbsentTreeNode.instance); }
break;
case 147:
//#line 900 "jparser.y"
{ yyval.obj = new IfStmtNode((ExprNode) val_peek(4).obj, (StatementNode) val_peek(2).obj, (TreeNode) val_peek(0).obj); }
break;
case 148:
//#line 902 "jparser.y"
{ yyval.obj = new SwitchNode((ExprNode) val_peek(2).obj, (LinkedList) val_peek(0).obj); }
break;
case 149:
//#line 907 "jparser.y"
{ yyval.obj = val_peek(1).obj; }
break;
case 150:
//#line 912 "jparser.y"
{ yyval.obj = new LinkedList(); }
break;
case 151:
//#line 914 "jparser.y"
{
     yyval.obj = cons(new SwitchBranchNode((LinkedList) val_peek(2).obj, (LinkedList) val_peek(1).obj),
               (LinkedList) val_peek(0).obj);
   }
break;
case 152:
//#line 920 "jparser.y"
{ yyval.obj = cons(new SwitchBranchNode((LinkedList) val_peek(0).obj, new LinkedList())); }
break;
case 153:
//#line 925 "jparser.y"
{ yyval.obj = cons(val_peek(0).obj); }
break;
case 154:
//#line 927 "jparser.y"
{ yyval.obj = cons(val_peek(1).obj, (LinkedList) val_peek(0).obj); }
break;
case 155:
//#line 932 "jparser.y"
{ yyval.obj = new CaseNode((TreeNode) val_peek(1).obj); }
break;
case 156:
//#line 934 "jparser.y"
{ yyval.obj = new CaseNode(AbsentTreeNode.instance); }
break;
case 157:
//#line 941 "jparser.y"
{ yyval.obj = new LoopNode(new EmptyStmtNode(), (ExprNode) val_peek(2).obj, (TreeNode) val_peek(0).obj); }
break;
case 158:
//#line 943 "jparser.y"
{ yyval.obj = new LoopNode((TreeNode) val_peek(5).obj, (ExprNode) val_peek(2).obj, new EmptyStmtNode()); }
break;
case 159:
//#line 945 "jparser.y"
{ yyval.obj = new ForNode((LinkedList) val_peek(5).obj, (ExprNode) val_peek(4).obj,
      (LinkedList) val_peek(2).obj, (StatementNode) val_peek(0).obj); }
break;
case 160:
//#line 948 "jparser.y"
{ yyval.obj = new ForNode((LinkedList) val_peek(4).obj, new BoolLitNode("true"), (LinkedList) val_peek(2).obj,
      (StatementNode) val_peek(0).obj); }
break;
case 161:
//#line 954 "jparser.y"
{ yyval.obj = val_peek(1).obj; }
break;
case 162:
//#line 956 "jparser.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 163:
//#line 960 "jparser.y"
{ }
break;
case 164:
//#line 962 "jparser.y"
{ yyval.obj = new LinkedList(); }
break;
case 165:
//#line 966 "jparser.y"
{ }
break;
case 166:
//#line 968 "jparser.y"
{ yyval.obj = new LinkedList(); }
break;
case 167:
//#line 973 "jparser.y"
{ yyval.obj = cons(val_peek(0).obj); }
break;
case 168:
//#line 975 "jparser.y"
{ yyval.obj = cons(val_peek(2).obj, (LinkedList) val_peek(0).obj); }
break;
case 169:
//#line 983 "jparser.y"
{ yyval.obj = new BreakNode((TreeNode) val_peek(1).obj); }
break;
case 170:
//#line 985 "jparser.y"
{ yyval.obj = new ContinueNode((TreeNode) val_peek(1).obj); }
break;
case 171:
//#line 987 "jparser.y"
{ yyval.obj = new ReturnNode((TreeNode) val_peek(1).obj); }
break;
case 172:
//#line 989 "jparser.y"
{ yyval.obj = new ThrowNode((ExprNode) val_peek(1).obj); }
break;
case 173:
//#line 994 "jparser.y"
{ }
break;
case 174:
//#line 996 "jparser.y"
{ yyval.obj = AbsentTreeNode.instance; }
break;
case 175:
//#line 1004 "jparser.y"
{ yyval.obj = new SynchronizedNode((ExprNode) val_peek(2).obj, (TreeNode) val_peek(0).obj); }
break;
case 176:
//#line 1006 "jparser.y"
{ yyval.obj = new TryNode((BlockNode) val_peek(1).obj, new LinkedList(), (TreeNode) val_peek(0).obj); }
break;
case 177:
//#line 1008 "jparser.y"
{ yyval.obj = new TryNode((BlockNode) val_peek(1).obj, (LinkedList) val_peek(0).obj, AbsentTreeNode.instance); }
break;
case 178:
//#line 1010 "jparser.y"
{ yyval.obj = new TryNode((BlockNode) val_peek(2).obj, (LinkedList) val_peek(1).obj, (TreeNode) val_peek(0).obj); }
break;
case 179:
//#line 1015 "jparser.y"
{ yyval.obj = cons(val_peek(0).obj); }
break;
case 180:
//#line 1017 "jparser.y"
{ yyval.obj = cons(val_peek(1).obj, (LinkedList) val_peek(0).obj); }
break;
case 181:
//#line 1022 "jparser.y"
{ yyval.obj = new CatchNode((ParameterNode) val_peek(2).obj, (BlockNode) val_peek(0).obj); }
break;
case 182:
//#line 1027 "jparser.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 183:
//#line 1038 "jparser.y"
{ yyval.obj = new ObjectNode((NameNode) val_peek(0).obj); }
break;
case 185:
//#line 1041 "jparser.y"
{ yyval.obj = new TypeClassAccessNode(new TypeNameNode((NameNode) val_peek(2).obj)); }
break;
case 186:
//#line 1043 "jparser.y"
{ yyval.obj = new TypeClassAccessNode((TypeNode) val_peek(2).obj); }
break;
case 187:
//#line 1045 "jparser.y"
{ yyval.obj = new TypeClassAccessNode((TypeNode) val_peek(2).obj); }
break;
case 188:
//#line 1047 "jparser.y"
{ yyval.obj = new OuterThisAccessNode(new TypeNameNode((NameNode) val_peek(2).obj)); }
break;
case 189:
//#line 1049 "jparser.y"
{ yyval.obj = new OuterSuperAccessNode(new TypeNameNode((NameNode) val_peek(2).obj)); }
break;
case 193:
//#line 1060 "jparser.y"
{ yyval.obj = new NullPntrNode(); }
break;
case 194:
//#line 1062 "jparser.y"
{ yyval.obj = new ThisNode(); }
break;
case 195:
//#line 1064 "jparser.y"
{ yyval.obj = val_peek(1).obj; }
break;
case 196:
//#line 1066 "jparser.y"
{ yyval.obj = new ObjectNode((NameNode) val_peek(1).obj); }
break;
case 198:
//#line 1069 "jparser.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 200:
//#line 1073 "jparser.y"
{ yyval.obj = new TypeClassAccessNode((TypeNode) val_peek(2).obj); }
break;
case 201:
//#line 1082 "jparser.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 203:
//#line 1088 "jparser.y"
{ yyval.obj = new NameNode(AbsentTreeNode.instance, val_peek(0).sval); }
break;
case 204:
//#line 1093 "jparser.y"
{ yyval.obj = new NameNode((NameNode) val_peek(2).obj, val_peek(0).sval); }
break;
case 205:
//#line 1100 "jparser.y"
{ yyval.obj = new ArrayAccessNode(new ObjectNode((NameNode) val_peek(3).obj), (ExprNode) val_peek(1).obj); }
break;
case 206:
//#line 1102 "jparser.y"
{ yyval.obj = new ArrayAccessNode((ExprNode) val_peek(3).obj, (ExprNode) val_peek(1).obj); }
break;
case 207:
//#line 1111 "jparser.y"
{ yyval.obj = new ObjectFieldAccessNode((TreeNode) val_peek(2).obj, (NameNode) val_peek(0).obj); }
break;
case 208:
//#line 1113 "jparser.y"
{ yyval.obj = new SuperFieldAccessNode((NameNode) val_peek(0).obj); }
break;
case 209:
//#line 1121 "jparser.y"
{ yyval.obj = new MethodCallNode((NameNode) val_peek(3).obj, (LinkedList) val_peek(1).obj); }
break;
case 210:
//#line 1123 "jparser.y"
{ yyval.obj = new MethodCallNode((TreeNode) val_peek(3).obj, (LinkedList) val_peek(1).obj); }
break;
case 211:
//#line 1126 "jparser.y"
{ yyval.obj = new MethodCallNode(new NameNode((NameNode) val_peek(5).obj, val_peek(3).sval), (LinkedList) val_peek(1).obj); }
break;
case 212:
//#line 1130 "jparser.y"
{  }
break;
case 213:
//#line 1132 "jparser.y"
{ yyval.obj = new LinkedList(); }
break;
case 214:
//#line 1137 "jparser.y"
{ yyval.obj = cons(val_peek(0).obj); }
break;
case 215:
//#line 1139 "jparser.y"
{ yyval.obj = cons(val_peek(2).obj, (LinkedList) val_peek(0).obj); }
break;
case 216:
//#line 1147 "jparser.y"
{ yyval.obj = new AllocateNode((TypeNameNode) val_peek(3).obj, (LinkedList) val_peek(1).obj, new ThisNode()); }
break;
case 217:
//#line 1150 "jparser.y"
{
     yyval.obj = new AllocateAnonymousClassNode((TypeNameNode) val_peek(4).obj,
               (LinkedList) val_peek(2).obj, (LinkedList) val_peek(0).obj, new ThisNode());
   }
break;
case 218:
//#line 1155 "jparser.y"
{
     yyval.obj = new AllocateArrayNode((TypeNode) val_peek(2).obj, (LinkedList) val_peek(1).obj, val_peek(0).ival,
           AbsentTreeNode.instance);
   }
break;
case 219:
//#line 1161 "jparser.y"
{
     yyval.obj = new AllocateArrayNode((TypeNode) val_peek(2).obj, new LinkedList(), val_peek(1).ival,
          (TreeNode) val_peek(0).obj);
   }
break;
case 220:
//#line 1166 "jparser.y"
{
     yyval.obj = new AllocateArrayNode((TypeNode) val_peek(2).obj, (LinkedList) val_peek(1).obj, val_peek(0).ival,
           AbsentTreeNode.instance);
   }
break;
case 221:
//#line 1172 "jparser.y"
{
     yyval.obj = new AllocateArrayNode((TypeNode) val_peek(2).obj, new LinkedList(), val_peek(1).ival,
           (TreeNode) val_peek(0).obj);
   }
break;
case 222:
//#line 1178 "jparser.y"
{
     yyval.obj = new AllocateNode(
           new TypeNameNode(new NameNode(AbsentTreeNode.instance, val_peek(3).sval)),
           (LinkedList) val_peek(1).obj, (ExprNode) val_peek(6).obj);
   }
break;
case 223:
//#line 1185 "jparser.y"
{
     yyval.obj = new AllocateAnonymousClassNode(
           new TypeNameNode(new NameNode(AbsentTreeNode.instance, val_peek(4).sval)),
           (LinkedList) val_peek(2).obj, (LinkedList) val_peek(0).obj, (ExprNode) val_peek(7).obj);
   }
break;
case 224:
//#line 1193 "jparser.y"
{
     yyval.obj = new AllocateNode(
           new TypeNameNode(new NameNode(AbsentTreeNode.instance, val_peek(3).sval)),
           (LinkedList) val_peek(1).obj, new ObjectNode((NameNode) val_peek(6).obj));
   }
break;
case 225:
//#line 1200 "jparser.y"
{
     yyval.obj = new AllocateAnonymousClassNode(
           new TypeNameNode(new NameNode(AbsentTreeNode.instance, val_peek(4).sval)),
           (LinkedList) val_peek(2).obj, (LinkedList) val_peek(0).obj, new ObjectNode((NameNode) val_peek(7).obj));
   }
break;
case 226:
//#line 1209 "jparser.y"
{ yyval.obj = cons(val_peek(0).obj); }
break;
case 227:
//#line 1211 "jparser.y"
{ yyval.obj = cons(val_peek(1).obj, (LinkedList) val_peek(0).obj); }
break;
case 228:
//#line 1216 "jparser.y"
{ yyval.obj = val_peek(1).obj; }
break;
case 229:
//#line 1220 "jparser.y"
{ }
break;
case 230:
//#line 1222 "jparser.y"
{ yyval.ival = 0; }
break;
case 231:
//#line 1227 "jparser.y"
{ yyval.ival = 1; }
break;
case 232:
//#line 1229 "jparser.y"
{ yyval.ival = val_peek(1).ival + 1; }
break;
case 236:
//#line 1243 "jparser.y"
{ yyval.obj = new PostIncrNode((ExprNode) val_peek(1).obj); }
break;
case 237:
//#line 1248 "jparser.y"
{ yyval.obj = new PostDecrNode((ExprNode) val_peek(1).obj); }
break;
case 240:
//#line 1258 "jparser.y"
{ yyval.obj = new UnaryPlusNode((ExprNode) val_peek(0).obj); }
break;
case 241:
//#line 1260 "jparser.y"
{ yyval.obj = new UnaryMinusNode((ExprNode) val_peek(0).obj); }
break;
case 243:
//#line 1266 "jparser.y"
{ yyval.obj = new PreIncrNode((ExprNode) val_peek(0).obj); }
break;
case 244:
//#line 1271 "jparser.y"
{ yyval.obj = new PreDecrNode((ExprNode) val_peek(0).obj); }
break;
case 246:
//#line 1277 "jparser.y"
{ yyval.obj = new ComplementNode((ExprNode) val_peek(0).obj); }
break;
case 247:
//#line 1279 "jparser.y"
{ yyval.obj = new NotNode((ExprNode) val_peek(0).obj); }
break;
case 249:
//#line 1285 "jparser.y"
{ yyval.obj = new CastNode((TypeNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 250:
//#line 1287 "jparser.y"
{ yyval.obj = new CastNode((TypeNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 251:
//#line 1289 "jparser.y"
{ yyval.obj = new CastNode(new TypeNameNode((NameNode) val_peek(2).obj), (ExprNode) val_peek(0).obj); }
break;
case 252:
//#line 1300 "jparser.y"
{ }
break;
case 253:
//#line 1302 "jparser.y"
{ yyval.obj = AbsentTreeNode.instance; }
break;
case 255:
//#line 1308 "jparser.y"
{ yyval.obj = new MultNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 256:
//#line 1310 "jparser.y"
{ yyval.obj = new DivNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 257:
//#line 1312 "jparser.y"
{ yyval.obj = new RemNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 258:
//#line 1314 "jparser.y"
{ yyval.obj = new PlusNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 259:
//#line 1316 "jparser.y"
{ yyval.obj = new MinusNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 260:
//#line 1318 "jparser.y"
{ yyval.obj = new LeftShiftLogNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 261:
//#line 1320 "jparser.y"
{ yyval.obj = new RightShiftLogNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 262:
//#line 1322 "jparser.y"
{ yyval.obj = new RightShiftArithNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 263:
//#line 1324 "jparser.y"
{ yyval.obj = new LTNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 264:
//#line 1326 "jparser.y"
{ yyval.obj = new GTNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 265:
//#line 1328 "jparser.y"
{ yyval.obj = new LENode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 266:
//#line 1330 "jparser.y"
{ yyval.obj = new GENode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 267:
//#line 1332 "jparser.y"
{ yyval.obj = new InstanceOfNode((ExprNode) val_peek(2).obj, (TypeNode) val_peek(0).obj); }
break;
case 268:
//#line 1334 "jparser.y"
{ yyval.obj = new EQNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 269:
//#line 1336 "jparser.y"
{ yyval.obj = new NENode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 270:
//#line 1338 "jparser.y"
{ yyval.obj = new BitAndNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 271:
//#line 1340 "jparser.y"
{ yyval.obj = new BitOrNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 272:
//#line 1342 "jparser.y"
{ yyval.obj = new BitXorNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 273:
//#line 1344 "jparser.y"
{ yyval.obj = new CandNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 274:
//#line 1346 "jparser.y"
{ yyval.obj = new CorNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 275:
//#line 1348 "jparser.y"
{ yyval.obj = new IfExprNode((ExprNode) val_peek(4).obj, (ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 277:
//#line 1357 "jparser.y"
{ yyval.obj = new AssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 278:
//#line 1359 "jparser.y"
{ yyval.obj = new MultAssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 279:
//#line 1361 "jparser.y"
{ yyval.obj = new DivAssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 280:
//#line 1363 "jparser.y"
{ yyval.obj = new RemAssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 281:
//#line 1365 "jparser.y"
{ yyval.obj = new PlusAssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 282:
//#line 1367 "jparser.y"
{ yyval.obj = new MinusAssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 283:
//#line 1369 "jparser.y"
{ yyval.obj = new LeftShiftLogAssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 284:
//#line 1371 "jparser.y"
{ yyval.obj = new RightShiftLogAssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 285:
//#line 1373 "jparser.y"
{ yyval.obj = new RightShiftArithAssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 286:
//#line 1375 "jparser.y"
{ yyval.obj = new BitAndAssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 287:
//#line 1377 "jparser.y"
{ yyval.obj = new BitXorAssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
case 288:
//#line 1379 "jparser.y"
{ yyval.obj = new BitOrAssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
break;
//#line 3269 "parser.java"
//########## END OF USER-SUPPLIED ACTIONS ##########
    }//switch
    //#### Now let's reduce... ####
    if (yydebug) debug("reduce");
    state_drop(yym);             //we just reduced yylen states
    yystate = state_peek(0);     //get new state
    val_drop(yym);               //corresponding value drop
    yym = yylhs[yyn];            //select next TERMINAL(on lhs)
    if (yystate == 0 && yym == 0)//done? 'rest' state and at first TERMINAL
      {
      debug("After reduction, shifting from state 0 to state "+YYFINAL+"");
      yystate = YYFINAL;         //explicitly say we're done
      state_push(YYFINAL);       //and save it
      val_push(yyval);           //also save the semantic value of parsing
      if (yychar < 0)            //we want another character?
        {
        yychar = yylex();        //get next character
        if (yychar<0) yychar=0;  //clean, if necessary
        if (yydebug)
          yylexdebug(yystate,yychar);
        }
      if (yychar == 0)          //Good exit (if lex returns 0 ;-)
         break;                 //quit the loop--all DONE
      }//if yystate
    else                        //else not done yet
      {                         //get next state and push, for next yydefred[]
      yyn = yygindex[yym];      //find out where to go
      if ((yyn != 0) && (yyn += yystate) >= 0 &&
            yyn <= YYTABLESIZE && yycheck[yyn] == yystate)
        yystate = yytable[yyn]; //get new state
      else
        yystate = yydgoto[yym]; //else go to new defred
      debug("after reduction, shifting from state "+state_peek(0)+" to state "+yystate+"");
      state_push(yystate);     //going again, so push state & val...
      val_push(yyval);         //for next action
      }
    }//main loop
  return 0;//yyaccept!!
}
//## end of method parse() ######################################



}
//################### END OF CLASS yaccpar ######################
