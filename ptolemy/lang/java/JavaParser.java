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



//#line 152 "jparser.y"
/*
Java parser produced by BYACC/J, with input file jparser.y

Copyright (c) 1998-2000 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.lang.java;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.io.IOException;
import java.io.FileInputStream;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

//#line 64 "JavaParser.java"
import java.io.File;
import java.io.RandomAccessFile;
import java.io.FileReader;
import java.io.StreamTokenizer;
import java.io.LineNumberReader;




//#####################################################################
// class: JavaParser
// does : encapsulates yacc() parser functionality in a Java
//        class for quick code development
//#####################################################################
public class JavaParser
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
            short[] retval = new short[size];
            File binFile = new File(filename + ".bin");
            File tblFile = new File(filename + ".tbl");
            // try to read cached binary file first if it's newer than the text file
            // or if the text file does not exist
            if (binFile.exists() &&
                    (!tblFile.exists() || (binFile.lastModified() >= tblFile.lastModified()))) {
                FileInputStream fileIn = null;
                try {
                    RandomAccessFile rafIn = new RandomAccessFile(binFile, "r");
                    for (int i = 0; i < size; i++) {
                        retval[i] = rafIn.readShort();
                    }
                    rafIn.close();
                    return retval;
                } catch (IOException e) {
                    // just read the text table instead if an I/O error occurs
                }
            }
            FileReader fileReader = null;
            try {
                fileReader = new FileReader(tblFile);
            } catch (IOException e) {
                throw new RuntimeException("no tables for " + filename + " could be found");
            }
            StreamTokenizer tokenizer = new StreamTokenizer(fileReader);
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
            // write out the table in binary format for next time
            try {
                RandomAccessFile rafOut = new RandomAccessFile(binFile, "rw");
                for (int i = 0; i < size; i++) {
                    rafOut.writeShort(retval[i]);
                }
                rafOut.close();
            } catch (IOException e) {
                throw new RuntimeException("could not write binary table");
            }
            return retval;
        }
    static String[] read_string_table(String filename, int size)
        {
            FileReader fileReader = null;
            try {
                fileReader = new FileReader(filename);
            } catch (IOException e) {
                return null; // hide error if we delete this non-critical table
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
    //public class JavaParsersemantic is defined in JavaParserval.java


    String   yytext;//user variable to return contextual strings
    JavaParserval yyval; //used to return semantic vals from action routines
    JavaParserval yylval;//the 'lval' (result) I got from yylex()
    JavaParserval valstk[];
    int valptr;
    //###############################################################
    // methods: value stack push,pop,drop,peek.
    //###############################################################
    void val_init()
        {
            valstk=new JavaParserval[YYSTACKSIZE];
            yyval=new JavaParserval(0);
            yylval=new JavaParserval(0);
            valptr=-1;
        }
    void val_push(JavaParserval val)
        {
            if (valptr>=YYSTACKSIZE)
                return;
            valstk[++valptr]=val;
        }
    JavaParserval val_pop()
        {
            if (valptr<0)
                return new JavaParserval(-1);
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
    JavaParserval val_peek(int relative)
        {
            int ptr;
            ptr=valptr-relative;
            if (ptr<0)
                return new JavaParserval(-1);
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
    final static int NRULES=292;
    final static short yylhs[] = read_short_table("yylhs", NRULES - 2);
    final static short yylen[] = read_short_table("yylen", NRULES - 2);
    final static int NSTATES=538;
    final static short yydefred[] = read_short_table("yydefred", NSTATES);
    final static int YYDGOTOSIZE=107;
    final static short yydgoto[] = read_short_table("yydgoto", YYDGOTOSIZE);
    final static short yysindex[] = read_short_table("yysindex", NSTATES);
    final static short yyrindex[] = read_short_table("yyrindex", NSTATES);
    final static int YYGINDEXSIZE=107;
    final static short yygindex[] = read_short_table("yygindex", YYGINDEXSIZE);
    final static int YYTABLESIZE=5284;
    final static short yytable[] = read_short_table("yytable", YYTABLESIZE + 1);
    final static short yycheck[] = read_short_table("yycheck", YYTABLESIZE + 1);
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
    final static String YYRULE_FILENAME = "yyrule.tbl";
    final static String yyrule[] = read_string_table(YYRULE_FILENAME,
            NRULES - 2);
    //#line 1495 "jparser.y"

    public void init(String filename) throws IOException {
        _filename = filename;
        _lexer = new Yylex(new FileInputStream(_filename));
    }

    public int parse() { return yyparse(); }

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

    protected static final List cons(Object obj)
        {
            return cons(obj, new LinkedList());
        }

    protected static final List cons(Object obj, List list)
        {
            if ((obj != null) && (obj != AbsentTreeNode.instance)) {
                list.add(0, obj);
            }

            return list;
        }

    protected static final List append(List list, Object obj)
        {
            list.add(obj);
            return list;
        }


    protected static final List appendLists(List list1, List list2)
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

    protected String _filename = null;
    protected Yylex _lexer = null;
    //#line 2015 "JavaParser.java"
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
                        if (yyrule == null) {
                            debug("state "+yystate+", reducing "+yym+" by rule "+yyn+" yyrule[] is null, perhaps " + YYRULE_FILENAME + " does not exist");
                        } else {
                            debug("state "+yystate+", reducing "+yym+" by rule "+yyn+" ("+yyrule[yyn]+")");
                        }
                    if (yym>0) { //if count of rhs not 'nil'
                        try {
                            yyval = (JavaParserval) val_peek(yym-1).clone(); //get current semantic value
                        } catch (CloneNotSupportedException e) {
                            yyerror("Clone not supported");
                        }
                    } else {
                        yyval = new JavaParserval();
                    }
                    switch(yyn)
                        {
                            //########## USER-SUPPLIED ACTIONS ##########
                        case 1:
                            //#line 268 "jparser.y"
                            { _theAST = (CompileUnitNode) val_peek(0).obj; }
                            break;
                        case 2:
                            //#line 274 "jparser.y"
                            { yyval.obj = new IntLitNode(val_peek(0).sval); }
                            break;
                        case 3:
                            //#line 276 "jparser.y"
                            { yyval.obj = new LongLitNode(val_peek(0).sval); }
                            break;
                        case 4:
                            //#line 278 "jparser.y"
                            { yyval.obj = new FloatLitNode(val_peek(0).sval); }
                            break;
                        case 5:
                            //#line 280 "jparser.y"
                            { yyval.obj = new DoubleLitNode(val_peek(0).sval); }
                            break;
                        case 6:
                            //#line 282 "jparser.y"
                            { yyval.obj = new BoolLitNode("true"); }
                            break;
                        case 7:
                            //#line 284 "jparser.y"
                            { yyval.obj = new BoolLitNode("false"); }
                            break;
                        case 8:
                            //#line 286 "jparser.y"
                            { yyval.obj = new CharLitNode(val_peek(0).sval); }
                            break;
                        case 9:
                            //#line 288 "jparser.y"
                            { yyval.obj = new StringLitNode(val_peek(0).sval); }
                            break;
                        case 14:
                            //#line 309 "jparser.y"
                            { yyval.obj = BoolTypeNode.instance; }
                            break;
                        case 15:
                            //#line 311 "jparser.y"
                            { yyval.obj = CharTypeNode.instance; }
                            break;
                        case 16:
                            //#line 313 "jparser.y"
                            { yyval.obj = ByteTypeNode.instance; }
                            break;
                        case 17:
                            //#line 315 "jparser.y"
                            { yyval.obj = ShortTypeNode.instance; }
                            break;
                        case 18:
                            //#line 317 "jparser.y"
                            { yyval.obj = IntTypeNode.instance; }
                            break;
                        case 19:
                            //#line 319 "jparser.y"
                            { yyval.obj = FloatTypeNode.instance; }
                            break;
                        case 20:
                            //#line 321 "jparser.y"
                            { yyval.obj = LongTypeNode.instance; }
                            break;
                        case 21:
                            //#line 323 "jparser.y"
                            { yyval.obj = DoubleTypeNode.instance; }
                            break;
                        case 22:
                            //#line 331 "jparser.y"
                            { yyval.obj = new TypeNameNode((NameNode) val_peek(0).obj); }
                            break;
                        case 23:
                            //#line 336 "jparser.y"
                            { yyval.obj = new ArrayTypeNode((TypeNode) val_peek(1).obj); }
                            break;
                        case 24:
                            //#line 345 "jparser.y"
                            { yyval.obj = new CompileUnitNode((TreeNode) val_peek(2).obj, (List) val_peek(1).obj, (List) val_peek(0).obj);  }
                            break;
                        case 25:
                            //#line 350 "jparser.y"
                            { yyval.obj = val_peek(1).obj; }
                            break;
                        case 26:
                            //#line 352 "jparser.y"
                            { yyval.obj = AbsentTreeNode.instance; }
                            break;
                        case 27:
                            //#line 357 "jparser.y"
                            { yyval.obj = new LinkedList(); }
                            break;
                        case 28:
                            //#line 359 "jparser.y"
                            { yyval.obj = cons(val_peek(1).obj, (List) val_peek(0).obj); }
                            break;
                        case 29:
                            //#line 365 "jparser.y"
                            { yyval.obj = new LinkedList(); }
                            break;
                        case 30:
                            //#line 367 "jparser.y"
                            { yyval.obj = cons(val_peek(1).obj, (List) val_peek(0).obj); }
                            break;
                        case 31:
                            //#line 369 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 36:
                            //#line 387 "jparser.y"
                            { yyval.obj = new ImportNode((NameNode) val_peek(1).obj); }
                            break;
                        case 37:
                            //#line 392 "jparser.y"
                            { yyval.obj = new ImportOnDemandNode((NameNode) val_peek(3).obj); }
                            break;
                        case 38:
                            //#line 404 "jparser.y"
                            {
                                /* add a default constructor if none is found*/
                                NameNode name = (NameNode) val_peek(3).obj;
                                List body = (List) val_peek(0).obj;

                                Iterator bodyItr = body.iterator();

                                boolean constructorFound = false;

                                while (!constructorFound && bodyItr.hasNext()) {
                                    Object member = bodyItr.next();

                                    if (member instanceof ConstructorDeclNode) {
                                        constructorFound = true;
                                    }
                                }

                                if (!constructorFound) {
                                    body.add(new ConstructorDeclNode(Modifier.PUBLIC_MOD,
                                            new NameNode(name.getQualifier(), name.getIdent()),
                                            new LinkedList(),                /* params*/
                                            new LinkedList(),                /* throws */
                                            new BlockNode(new LinkedList()), /* body*/
                                            new SuperConstructorCallNode(new LinkedList())));
                                }

                                yyval.obj = new ClassDeclNode(val_peek(5).ival, name, (List) val_peek(1).obj,
                                        (List) body, (TreeNode) val_peek(2).obj);
                            }
                            break;
                        case 39:
                            //#line 445 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 40:
                            //#line 447 "jparser.y"
                            {
                                /* this will be fixed later by class resolution*/
                                yyval.obj = AbsentTreeNode.instance;
                            }
                            break;
                        case 41:
                            //#line 458 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 42:
                            //#line 460 "jparser.y"
                            { yyval.obj = new LinkedList(); }
                            break;
                        case 43:
                            //#line 469 "jparser.y"
                            {
                                yyval.obj = val_peek(1).obj; /* in the original, an ABSENT tree is added*/
                            }
                            break;
                        case 44:
                            //#line 475 "jparser.y"
                            { }
                            break;
                        case 45:
                            //#line 477 "jparser.y"
                            { yyval.obj = new LinkedList(); }
                            break;
                        case 47:
                            //#line 483 "jparser.y"
                            { yyval.obj = appendLists((List) val_peek(1).obj, (List) val_peek(0).obj); }
                            break;
                        case 49:
                            //#line 492 "jparser.y"
                            { yyval.obj = cons(val_peek(0).obj); }
                            break;
                        case 50:
                            //#line 494 "jparser.y"
                            { yyval.obj = cons(val_peek(0).obj); }
                            break;
                        case 51:
                            //#line 496 "jparser.y"
                            { yyval.obj = cons(val_peek(0).obj); }
                            break;
                        case 52:
                            //#line 499 "jparser.y"
                            { yyval.obj = cons(val_peek(0).obj); }
                            break;
                        case 53:
                            //#line 505 "jparser.y"
                            { yyval.obj = cons(val_peek(0).obj); }
                            break;
                        case 54:
                            //#line 507 "jparser.y"
                            { yyval.obj = cons(val_peek(1).obj); }
                            break;
                        case 55:
                            //#line 515 "jparser.y"
                            {
                                Modifier.checkFieldModifiers(val_peek(3).ival);
                                List result = new LinkedList();

                                List varDecls = (List) val_peek(1).obj;
                                Iterator itr = varDecls.iterator();

                                while (itr.hasNext()) {
                                    DeclaratorNode decl = (DeclaratorNode) itr.next();
                                    result = cons(new FieldDeclNode(val_peek(3).ival,
                                            TypeUtility.makeArrayType((TypeNode) val_peek(2).obj, decl.getDims()),
                                            decl.getName(), decl.getInitExpr()),
                                            result);
                                }

                                yyval.obj = result;
                            }
                            break;
                        case 56:
                            //#line 541 "jparser.y"
                            { }
                            break;
                        case 57:
                            //#line 543 "jparser.y"
                            { yyval.ival = Modifier.NO_MOD; }
                            break;
                        case 58:
                            //#line 547 "jparser.y"
                            { yyval.ival = val_peek(0).ival; }
                            break;
                        case 59:
                            //#line 549 "jparser.y"
                            {
                                yyval.ival = (val_peek(1).ival | val_peek(0).ival);
                                if ((val_peek(1).ival & val_peek(0).ival) != 0) {
                                    yyerror("repeated modifier");
                                }
                            }
                            break;
                        case 60:
                            //#line 560 "jparser.y"
                            { yyval.ival = Modifier.PUBLIC_MOD; }
                            break;
                        case 61:
                            //#line 562 "jparser.y"
                            { yyval.ival = Modifier.PROTECTED_MOD;  }
                            break;
                        case 62:
                            //#line 564 "jparser.y"
                            { yyval.ival = Modifier.PRIVATE_MOD;  }
                            break;
                        case 63:
                            //#line 567 "jparser.y"
                            { yyval.ival = Modifier.STATIC_MOD;  }
                            break;
                        case 64:
                            //#line 569 "jparser.y"
                            { yyval.ival = Modifier.FINAL_MOD;  }
                            break;
                        case 65:
                            //#line 572 "jparser.y"
                            { yyval.ival = Modifier.ABSTRACT_MOD;  }
                            break;
                        case 66:
                            //#line 574 "jparser.y"
                            { yyval.ival = Modifier.NATIVE_MOD;  }
                            break;
                        case 67:
                            //#line 576 "jparser.y"
                            { yyval.ival = Modifier.SYNCHRONIZED_MOD;  }
                            break;
                        case 68:
                            //#line 579 "jparser.y"
                            { yyval.ival = Modifier.TRANSIENT_MOD;  }
                            break;
                        case 69:
                            //#line 581 "jparser.y"
                            { yyval.ival = Modifier.VOLATILE_MOD;  }
                            break;
                        case 70:
                            //#line 583 "jparser.y"
                            { yyval.ival = Modifier.STRICTFP_MOD; }
                            break;
                        case 71:
                            //#line 594 "jparser.y"
                            { yyval.obj = cons(val_peek(0).obj); }
                            break;
                        case 72:
                            //#line 596 "jparser.y"
                            { yyval.obj = cons(val_peek(0).obj, (List) val_peek(2).obj); }
                            break;
                        case 73:
                            //#line 601 "jparser.y"
                            { yyval.obj = new DeclaratorNode(val_peek(0).ival, (NameNode) val_peek(1).obj, AbsentTreeNode.instance); }
                            break;
                        case 74:
                            //#line 603 "jparser.y"
                            { yyval.obj = new DeclaratorNode(val_peek(2).ival, (NameNode) val_peek(3).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 77:
                            //#line 619 "jparser.y"
                            {
                                Modifier.checkMethodModifiers(val_peek(8).ival);
                                yyval.obj = new MethodDeclNode(val_peek(8).ival, (NameNode) val_peek(6).obj, (List) val_peek(4).obj,
                                        (List) val_peek(1).obj, (TreeNode) val_peek(0).obj,
                                        TypeUtility.makeArrayType((TypeNode) val_peek(7).obj, val_peek(2).ival));
                            }
                            break;
                        case 78:
                            //#line 627 "jparser.y"
                            {
                                Modifier.checkMethodModifiers(val_peek(8).ival);
                                yyval.obj = new MethodDeclNode(val_peek(8).ival, (NameNode) val_peek(6).obj, (List) val_peek(4).obj,
                                        (List) val_peek(1).obj, (TreeNode) val_peek(0).obj,
                                        TypeUtility.makeArrayType((TypeNode) val_peek(7).obj, val_peek(2).ival));
                            }
                            break;
                        case 79:
                            //#line 637 "jparser.y"
                            { yyval.obj = VoidTypeNode.instance; }
                            break;
                        case 80:
                            //#line 645 "jparser.y"
                            { }
                            break;
                        case 81:
                            //#line 647 "jparser.y"
                            { yyval.obj = new LinkedList();  }
                            break;
                        case 82:
                            //#line 652 "jparser.y"
                            { yyval.obj = cons(val_peek(0).obj); }
                            break;
                        case 83:
                            //#line 654 "jparser.y"
                            { yyval.obj = cons(val_peek(2).obj, (List) val_peek(0).obj); }
                            break;
                        case 84:
                            //#line 659 "jparser.y"
                            {
                                Modifier.checkParameterModifiers(val_peek(3).ival);
                                yyval.obj = new ParameterNode(val_peek(3).ival, TypeUtility.makeArrayType((TypeNode) val_peek(2).obj, val_peek(0).ival),
                                        (NameNode) val_peek(1).obj);
                            }
                            break;
                        case 85:
                            //#line 669 "jparser.y"
                            { }
                            break;
                        case 86:
                            //#line 671 "jparser.y"
                            { yyval.obj = new LinkedList(); }
                            break;
                        case 87:
                            //#line 676 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 88:
                            //#line 681 "jparser.y"
                            { yyval.obj = cons(val_peek(0).obj); }
                            break;
                        case 89:
                            //#line 683 "jparser.y"
                            { yyval.obj = cons(val_peek(2).obj, (List) val_peek(0).obj); }
                            break;
                        case 91:
                            //#line 692 "jparser.y"
                            { yyval.obj = AbsentTreeNode.instance; }
                            break;
                        case 92:
                            //#line 701 "jparser.y"
                            {
                                Modifier.checkConstructorModifiers(val_peek(9).ival);
                                yyval.obj = new ConstructorDeclNode(val_peek(9).ival,
                                        new NameNode(AbsentTreeNode.instance, val_peek(8).sval), (List) val_peek(6).obj,
                                        (List) val_peek(4).obj, new BlockNode((List) val_peek(1).obj),
                                        (ConstructorCallNode) val_peek(2).obj);
                            }
                            break;
                        case 93:
                            //#line 710 "jparser.y"
                            {
                                Modifier.checkConstructorModifiers(val_peek(8).ival);
                                yyval.obj = new ConstructorDeclNode(val_peek(8).ival,
                                        new NameNode(AbsentTreeNode.instance, val_peek(7).sval), (List) val_peek(5).obj,
                                        (List) val_peek(3).obj, new BlockNode((List) val_peek(1).obj),
                                        new SuperConstructorCallNode(new LinkedList()));
                            }
                            break;
                        case 94:
                            //#line 725 "jparser.y"
                            { yyval.obj = new ThisConstructorCallNode((List) val_peek(2).obj); }
                            break;
                        case 95:
                            //#line 727 "jparser.y"
                            { yyval.obj = new SuperConstructorCallNode((List) val_peek(2).obj); }
                            break;
                        case 96:
                            //#line 735 "jparser.y"
                            { yyval.obj = new StaticInitNode((BlockNode) val_peek(0).obj); }
                            break;
                        case 97:
                            //#line 740 "jparser.y"
                            { yyval.obj = new InstanceInitNode((BlockNode) val_peek(0).obj); }
                            break;
                        case 98:
                            //#line 748 "jparser.y"
                            {
                                Modifier.checkInterfaceModifiers(val_peek(4).ival);
                                yyval.obj = new InterfaceDeclNode(val_peek(4).ival, (NameNode) val_peek(2).obj, (List) val_peek(1).obj, (List) val_peek(0).obj);
                            }
                            break;
                        case 99:
                            //#line 763 "jparser.y"
                            { }
                            break;
                        case 100:
                            //#line 765 "jparser.y"
                            { yyval.obj = new LinkedList(); }
                            break;
                        case 101:
                            //#line 770 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 102:
                            //#line 777 "jparser.y"
                            { yyval.obj = val_peek(1).obj; }
                            break;
                        case 103:
                            //#line 782 "jparser.y"
                            { yyval.obj = new LinkedList(); }
                            break;
                        case 104:
                            //#line 784 "jparser.y"
                            { yyval.obj = appendLists((List) val_peek(1).obj, (List) val_peek(0).obj); }
                            break;
                        case 106:
                            //#line 790 "jparser.y"
                            { yyval.obj = cons(val_peek(0).obj); }
                            break;
                        case 107:
                            //#line 792 "jparser.y"
                            { yyval.obj = cons(val_peek(0).obj); }
                            break;
                        case 108:
                            //#line 794 "jparser.y"
                            { yyval.obj = cons(val_peek(1).obj); }
                            break;
                        case 109:
                            //#line 799 "jparser.y"
                            {
                                int modifiers = val_peek(3).ival;
                                modifiers |= (Modifier.STATIC_MOD | Modifier.FINAL_MOD);

                                Modifier.checkConstantFieldModifiers(modifiers);
                                List varDecls = (List) val_peek(1).obj;
                                Iterator itr = varDecls.iterator();

                                List result = new LinkedList();

                                while (itr.hasNext()) {
                                    DeclaratorNode decl = (DeclaratorNode) itr.next();
                                    result = cons(new FieldDeclNode(modifiers,
                                            TypeUtility.makeArrayType((TypeNode) val_peek(2).obj, decl.getDims()),
                                            decl.getName(), decl.getInitExpr()), result);
                                }

                                yyval.obj = result;
                            }
                            break;
                        case 110:
                            //#line 823 "jparser.y"
                            {
                                Modifier.checkMethodSignatureModifiers(val_peek(8).ival);
                                yyval.obj = new MethodDeclNode(val_peek(8).ival, (NameNode) val_peek(6).obj,
                                        (List) val_peek(4).obj, (List) val_peek(1).obj,
                                        AbsentTreeNode.instance,
                                        TypeUtility.makeArrayType((TypeNode) val_peek(7).obj, val_peek(2).ival));
                            }
                            break;
                        case 111:
                            //#line 832 "jparser.y"
                            {
                                Modifier.checkMethodSignatureModifiers(val_peek(8).ival);
                                yyval.obj = new MethodDeclNode(val_peek(8).ival, (NameNode) val_peek(6).obj,
                                        (List) val_peek(4).obj, (List) val_peek(1).obj,
                                        AbsentTreeNode.instance,
                                        TypeUtility.makeArrayType((TypeNode) val_peek(7).obj, val_peek(2).ival));
                            }
                            break;
                        case 112:
                            //#line 847 "jparser.y"
                            { yyval.obj = new ArrayInitNode((List) val_peek(1).obj); }
                            break;
                        case 113:
                            //#line 849 "jparser.y"
                            { yyval.obj = new ArrayInitNode((List) val_peek(2).obj); }
                            break;
                        case 114:
                            //#line 851 "jparser.y"
                            { yyval.obj = new ArrayInitNode(new LinkedList()); }
                            break;
                        case 115:
                            //#line 857 "jparser.y"
                            { yyval.obj = cons(val_peek(0).obj); }
                            break;
                        case 116:
                            //#line 859 "jparser.y"
                            { yyval.obj = append((List) val_peek(2).obj, val_peek(0).obj); }
                            break;
                        case 119:
                            //#line 874 "jparser.y"
                            { yyval.obj = new BlockNode((List) val_peek(1).obj); }
                            break;
                        case 120:
                            //#line 878 "jparser.y"
                            { }
                            break;
                        case 121:
                            //#line 880 "jparser.y"
                            { yyval.obj = new LinkedList(); }
                            break;
                        case 122:
                            //#line 885 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 123:
                            //#line 887 "jparser.y"
                            { yyval.obj = appendLists((List) val_peek(1).obj, (List) val_peek(0).obj); }
                            break;
                        case 124:
                            //#line 892 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 125:
                            //#line 894 "jparser.y"
                            { yyval.obj = cons(val_peek(0).obj); }
                            break;
                        case 126:
                            //#line 896 "jparser.y"
                            { yyval.obj = cons(new UserTypeDeclStmtNode((UserTypeDeclNode) val_peek(0).obj)); }
                            break;
                        case 127:
                            //#line 904 "jparser.y"
                            {
                                Modifier.checkLocalVariableModifiers(val_peek(3).ival);

                                List varDecls = (List) val_peek(1).obj;
                                List result = new LinkedList();

                                Iterator itr = varDecls.iterator();

                                while (itr.hasNext()) {
                                    DeclaratorNode decl = (DeclaratorNode) itr.next();
                                    result = cons(new LocalVarDeclNode(val_peek(3).ival,
                                            TypeUtility.makeArrayType((TypeNode) val_peek(2).obj, decl.getDims()),
                                            decl.getName(), decl.getInitExpr()), result);
                                }
                                yyval.obj = result;
                            }
                            break;
                        case 128:
                            //#line 922 "jparser.y"
                            {
                                List varDecls = (List) val_peek(1).obj;
                                List result = new LinkedList();

                                Iterator itr = varDecls.iterator();

                                while (itr.hasNext()) {
                                    DeclaratorNode decl = (DeclaratorNode) itr.next();
                                    result = cons(new LocalVarDeclNode(Modifier.NO_MOD,
                                            TypeUtility.makeArrayType((TypeNode) val_peek(2).obj, decl.getDims()),
                                            decl.getName(), decl.getInitExpr()), result);
                                }
                                yyval.obj = result;
                            }
                            break;
                        case 129:
                            //#line 942 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 130:
                            //#line 944 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 131:
                            //#line 946 "jparser.y"
                            { yyval.obj = new ExprStmtNode((ExprNode) val_peek(1).obj); }
                            break;
                        case 132:
                            //#line 948 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 133:
                            //#line 950 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 134:
                            //#line 952 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 135:
                            //#line 954 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 136:
                            //#line 956 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 137:
                            //#line 963 "jparser.y"
                            { yyval.obj = new EmptyStmtNode(); }
                            break;
                        case 138:
                            //#line 971 "jparser.y"
                            { yyval.obj = new LabeledStmtNode((NameNode) val_peek(2).obj, (StatementNode) val_peek(0).obj); }
                            break;
                        case 139:
                            //#line 979 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 140:
                            //#line 981 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 141:
                            //#line 983 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 142:
                            //#line 985 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 143:
                            //#line 987 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 144:
                            //#line 989 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 145:
                            //#line 991 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 146:
                            //#line 999 "jparser.y"
                            { yyval.obj = new IfStmtNode((ExprNode) val_peek(2).obj, (StatementNode) val_peek(0).obj, AbsentTreeNode.instance); }
                            break;
                        case 147:
                            //#line 1001 "jparser.y"
                            { yyval.obj = new IfStmtNode((ExprNode) val_peek(4).obj, (StatementNode) val_peek(2).obj, (TreeNode) val_peek(0).obj); }
                            break;
                        case 148:
                            //#line 1003 "jparser.y"
                            { yyval.obj = new SwitchNode((ExprNode) val_peek(2).obj, (List) val_peek(0).obj); }
                            break;
                        case 149:
                            //#line 1008 "jparser.y"
                            { yyval.obj = val_peek(1).obj; }
                            break;
                        case 150:
                            //#line 1013 "jparser.y"
                            { yyval.obj = new LinkedList(); }
                            break;
                        case 151:
                            //#line 1015 "jparser.y"
                            {
                                yyval.obj = cons(new SwitchBranchNode((List) val_peek(2).obj, (List) val_peek(1).obj),
                                        (List) val_peek(0).obj);
                            }
                            break;
                        case 152:
                            //#line 1021 "jparser.y"
                            { yyval.obj = cons(new SwitchBranchNode((List) val_peek(0).obj, new LinkedList())); }
                            break;
                        case 153:
                            //#line 1026 "jparser.y"
                            { yyval.obj = cons(val_peek(0).obj); }
                            break;
                        case 154:
                            //#line 1028 "jparser.y"
                            { yyval.obj = cons(val_peek(1).obj, (List) val_peek(0).obj); }
                            break;
                        case 155:
                            //#line 1033 "jparser.y"
                            { yyval.obj = new CaseNode((TreeNode) val_peek(1).obj); }
                            break;
                        case 156:
                            //#line 1035 "jparser.y"
                            { yyval.obj = new CaseNode(AbsentTreeNode.instance); }
                            break;
                        case 157:
                            //#line 1042 "jparser.y"
                            { yyval.obj = new LoopNode(new EmptyStmtNode(), (ExprNode) val_peek(2).obj, (TreeNode) val_peek(0).obj); }
                            break;
                        case 158:
                            //#line 1044 "jparser.y"
                            { yyval.obj = new LoopNode((TreeNode) val_peek(5).obj, (ExprNode) val_peek(2).obj, new EmptyStmtNode()); }
                            break;
                        case 159:
                            //#line 1046 "jparser.y"
                            { yyval.obj = new ForNode((List) val_peek(5).obj, (ExprNode) val_peek(4).obj,
                                    (List) val_peek(2).obj, (StatementNode) val_peek(0).obj); }
                            break;
                        case 160:
                            //#line 1049 "jparser.y"
                            { yyval.obj = new ForNode((List) val_peek(4).obj, new BoolLitNode("true"), (List) val_peek(2).obj,
                                    (StatementNode) val_peek(0).obj); }
                            break;
                        case 161:
                            //#line 1055 "jparser.y"
                            { yyval.obj = val_peek(1).obj; }
                            break;
                        case 162:
                            //#line 1057 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 163:
                            //#line 1062 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 164:
                            //#line 1064 "jparser.y"
                            { yyval.obj = new LinkedList(); }
                            break;
                        case 165:
                            //#line 1069 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 166:
                            //#line 1071 "jparser.y"
                            { yyval.obj = new LinkedList(); }
                            break;
                        case 167:
                            //#line 1076 "jparser.y"
                            { yyval.obj = cons(val_peek(0).obj); }
                            break;
                        case 168:
                            //#line 1078 "jparser.y"
                            { yyval.obj = cons(val_peek(2).obj, (List) val_peek(0).obj); }
                            break;
                        case 169:
                            //#line 1086 "jparser.y"
                            { yyval.obj = new BreakNode((TreeNode) val_peek(1).obj); }
                            break;
                        case 170:
                            //#line 1088 "jparser.y"
                            { yyval.obj = new ContinueNode((TreeNode) val_peek(1).obj); }
                            break;
                        case 171:
                            //#line 1090 "jparser.y"
                            { yyval.obj = new ReturnNode((TreeNode) val_peek(1).obj); }
                            break;
                        case 172:
                            //#line 1092 "jparser.y"
                            { yyval.obj = new ThrowNode((ExprNode) val_peek(1).obj); }
                            break;
                        case 173:
                            //#line 1097 "jparser.y"
                            { }
                            break;
                        case 174:
                            //#line 1099 "jparser.y"
                            { yyval.obj = AbsentTreeNode.instance; }
                            break;
                        case 175:
                            //#line 1107 "jparser.y"
                            { yyval.obj = new SynchronizedNode((ExprNode) val_peek(2).obj, (TreeNode) val_peek(0).obj); }
                            break;
                        case 176:
                            //#line 1109 "jparser.y"
                            { yyval.obj = new TryNode((BlockNode) val_peek(1).obj, new LinkedList(), (TreeNode) val_peek(0).obj); }
                            break;
                        case 177:
                            //#line 1111 "jparser.y"
                            { yyval.obj = new TryNode((BlockNode) val_peek(1).obj, (List) val_peek(0).obj, AbsentTreeNode.instance); }
                            break;
                        case 178:
                            //#line 1113 "jparser.y"
                            { yyval.obj = new TryNode((BlockNode) val_peek(2).obj, (List) val_peek(1).obj, (TreeNode) val_peek(0).obj); }
                            break;
                        case 179:
                            //#line 1118 "jparser.y"
                            { yyval.obj = cons(val_peek(0).obj); }
                            break;
                        case 180:
                            //#line 1120 "jparser.y"
                            { yyval.obj = cons(val_peek(1).obj, (List) val_peek(0).obj); }
                            break;
                        case 181:
                            //#line 1125 "jparser.y"
                            { yyval.obj = new CatchNode((ParameterNode) val_peek(2).obj, (BlockNode) val_peek(0).obj); }
                            break;
                        case 182:
                            //#line 1130 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 183:
                            //#line 1141 "jparser.y"
                            { yyval.obj = new ObjectNode((NameNode) val_peek(0).obj); }
                            break;
                        case 185:
                            //#line 1144 "jparser.y"
                            { yyval.obj = new TypeClassAccessNode(new TypeNameNode((NameNode) val_peek(2).obj)); }
                            break;
                        case 186:
                            //#line 1146 "jparser.y"
                            { yyval.obj = new TypeClassAccessNode((TypeNode) val_peek(2).obj); }
                            break;
                        case 187:
                            //#line 1148 "jparser.y"
                            { yyval.obj = new TypeClassAccessNode((TypeNode) val_peek(2).obj); }
                            break;
                        case 188:
                            //#line 1150 "jparser.y"
                            { yyval.obj = new OuterThisAccessNode(new TypeNameNode((NameNode) val_peek(2).obj)); }
                            break;
                        case 189:
                            //#line 1152 "jparser.y"
                            { yyval.obj = new OuterSuperAccessNode(new TypeNameNode((NameNode) val_peek(2).obj)); }
                            break;
                        case 193:
                            //#line 1163 "jparser.y"
                            { yyval.obj = new NullPntrNode(); }
                            break;
                        case 194:
                            //#line 1165 "jparser.y"
                            { yyval.obj = new ThisNode(); }
                            break;
                        case 195:
                            //#line 1167 "jparser.y"
                            { yyval.obj = val_peek(1).obj; }
                            break;
                        case 196:
                            //#line 1169 "jparser.y"
                            { yyval.obj = new ObjectNode((NameNode) val_peek(1).obj); }
                            break;
                        case 198:
                            //#line 1172 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 200:
                            //#line 1176 "jparser.y"
                            { yyval.obj = new TypeClassAccessNode((TypeNode) val_peek(2).obj); }
                            break;
                        case 201:
                            //#line 1185 "jparser.y"
                            { yyval.obj = val_peek(0).obj; }
                            break;
                        case 203:
                            //#line 1191 "jparser.y"
                            { yyval.obj = new NameNode(AbsentTreeNode.instance, val_peek(0).sval); }
                            break;
                        case 204:
                            //#line 1196 "jparser.y"
                            { yyval.obj = new NameNode((NameNode) val_peek(2).obj, val_peek(0).sval); }
                            break;
                        case 205:
                            //#line 1203 "jparser.y"
                            { yyval.obj = new ArrayAccessNode(new ObjectNode((NameNode) val_peek(3).obj), (ExprNode) val_peek(1).obj); }
                            break;
                        case 206:
                            //#line 1205 "jparser.y"
                            { yyval.obj = new ArrayAccessNode((ExprNode) val_peek(3).obj, (ExprNode) val_peek(1).obj); }
                            break;
                        case 207:
                            //#line 1214 "jparser.y"
                            { yyval.obj = new ObjectFieldAccessNode((NameNode) val_peek(0).obj, (ExprNode) val_peek(2).obj); }
                            break;
                        case 208:
                            //#line 1216 "jparser.y"
                            { yyval.obj = new SuperFieldAccessNode((NameNode) val_peek(0).obj); }
                            break;
                        case 209:
                            //#line 1224 "jparser.y"
                            { yyval.obj = new MethodCallNode(new ObjectNode((NameNode) val_peek(3).obj), (List) val_peek(1).obj); }
                            break;
                        case 210:
                            //#line 1226 "jparser.y"
                            { yyval.obj = new MethodCallNode((FieldAccessNode) val_peek(3).obj, (List) val_peek(1).obj); }
                            break;
                        case 211:
                            //#line 1230 "jparser.y"
                            {  }
                            break;
                        case 212:
                            //#line 1232 "jparser.y"
                            { yyval.obj = new LinkedList(); }
                            break;
                        case 213:
                            //#line 1237 "jparser.y"
                            { yyval.obj = cons(val_peek(0).obj); }
                            break;
                        case 214:
                            //#line 1239 "jparser.y"
                            { yyval.obj = cons(val_peek(2).obj, (List) val_peek(0).obj); }
                            break;
                        case 215:
                            //#line 1247 "jparser.y"
                            { yyval.obj = new AllocateNode((TypeNameNode) val_peek(3).obj, (List) val_peek(1).obj, AbsentTreeNode.instance); }
                            break;
                        case 216:
                            //#line 1250 "jparser.y"
                            {
                                yyval.obj = new AllocateAnonymousClassNode((TypeNameNode) val_peek(4).obj,
                                        (List) val_peek(2).obj, (List) val_peek(0).obj, AbsentTreeNode.instance);
                            }
                            break;
                        case 217:
                            //#line 1255 "jparser.y"
                            {
                                yyval.obj = new AllocateArrayNode((TypeNode) val_peek(2).obj, (List) val_peek(1).obj, val_peek(0).ival,
                                        AbsentTreeNode.instance);
                            }
                            break;
                        case 218:
                            //#line 1261 "jparser.y"
                            {
                                yyval.obj = new AllocateArrayNode((TypeNode) val_peek(2).obj, new LinkedList(), val_peek(1).ival,
                                        (TreeNode) val_peek(0).obj);
                            }
                            break;
                        case 219:
                            //#line 1266 "jparser.y"
                            {
                                yyval.obj = new AllocateArrayNode((TypeNode) val_peek(2).obj, (List) val_peek(1).obj, val_peek(0).ival,
                                        AbsentTreeNode.instance);
                            }
                            break;
                        case 220:
                            //#line 1272 "jparser.y"
                            {
                                yyval.obj = new AllocateArrayNode((TypeNode) val_peek(2).obj, new LinkedList(), val_peek(1).ival,
                                        (TreeNode) val_peek(0).obj);
                            }
                            break;
                        case 221:
                            //#line 1278 "jparser.y"
                            {
                                yyval.obj = new AllocateNode(
                                        new TypeNameNode(new NameNode(AbsentTreeNode.instance, val_peek(3).sval)),
                                        (List) val_peek(1).obj, (ExprNode) val_peek(6).obj);
                            }
                            break;
                        case 222:
                            //#line 1285 "jparser.y"
                            {
                                yyval.obj = new AllocateAnonymousClassNode(
                                        new TypeNameNode(new NameNode(AbsentTreeNode.instance, val_peek(4).sval)),
                                        (List) val_peek(2).obj, (List) val_peek(0).obj, (ExprNode) val_peek(7).obj);
                            }
                            break;
                        case 223:
                            //#line 1293 "jparser.y"
                            {
                                yyval.obj = new AllocateNode(
                                        new TypeNameNode(new NameNode(AbsentTreeNode.instance, val_peek(3).sval)),
                                        (List) val_peek(1).obj, new ObjectNode((NameNode) val_peek(6).obj));
                            }
                            break;
                        case 224:
                            //#line 1300 "jparser.y"
                            {
                                yyval.obj = new AllocateAnonymousClassNode(
                                        new TypeNameNode(new NameNode(AbsentTreeNode.instance, val_peek(4).sval)),
                                        (List) val_peek(2).obj, (List) val_peek(0).obj, new ObjectNode((NameNode) val_peek(7).obj));
                            }
                            break;
                        case 225:
                            //#line 1309 "jparser.y"
                            { yyval.obj = cons(val_peek(0).obj); }
                            break;
                        case 226:
                            //#line 1311 "jparser.y"
                            { yyval.obj = cons(val_peek(1).obj, (List) val_peek(0).obj); }
                            break;
                        case 227:
                            //#line 1316 "jparser.y"
                            { yyval.obj = val_peek(1).obj; }
                            break;
                        case 228:
                            //#line 1320 "jparser.y"
                            { }
                            break;
                        case 229:
                            //#line 1322 "jparser.y"
                            { yyval.ival = 0; }
                            break;
                        case 230:
                            //#line 1327 "jparser.y"
                            { yyval.ival = 1; }
                            break;
                        case 231:
                            //#line 1329 "jparser.y"
                            { yyval.ival = val_peek(1).ival + 1; }
                            break;
                        case 235:
                            //#line 1343 "jparser.y"
                            { yyval.obj = new PostIncrNode((ExprNode) val_peek(1).obj); }
                            break;
                        case 236:
                            //#line 1348 "jparser.y"
                            { yyval.obj = new PostDecrNode((ExprNode) val_peek(1).obj); }
                            break;
                        case 239:
                            //#line 1358 "jparser.y"
                            { yyval.obj = new UnaryPlusNode((ExprNode) val_peek(0).obj); }
                            break;
                        case 240:
                            //#line 1360 "jparser.y"
                            { yyval.obj = new UnaryMinusNode((ExprNode) val_peek(0).obj); }
                            break;
                        case 242:
                            //#line 1366 "jparser.y"
                            { yyval.obj = new PreIncrNode((ExprNode) val_peek(0).obj); }
                            break;
                        case 243:
                            //#line 1371 "jparser.y"
                            { yyval.obj = new PreDecrNode((ExprNode) val_peek(0).obj); }
                            break;
                        case 245:
                            //#line 1377 "jparser.y"
                            { yyval.obj = new ComplementNode((ExprNode) val_peek(0).obj); }
                            break;
                        case 246:
                            //#line 1379 "jparser.y"
                            { yyval.obj = new NotNode((ExprNode) val_peek(0).obj); }
                            break;
                        case 248:
                            //#line 1385 "jparser.y"
                            { yyval.obj = new CastNode((TypeNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 249:
                            //#line 1387 "jparser.y"
                            { yyval.obj = new CastNode((TypeNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 250:
                            //#line 1389 "jparser.y"
                            { yyval.obj = new CastNode(new TypeNameNode((NameNode) val_peek(2).obj), (ExprNode) val_peek(0).obj); }
                            break;
                        case 251:
                            //#line 1400 "jparser.y"
                            { }
                            break;
                        case 252:
                            //#line 1402 "jparser.y"
                            { yyval.obj = AbsentTreeNode.instance; }
                            break;
                        case 254:
                            //#line 1408 "jparser.y"
                            { yyval.obj = new MultNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 255:
                            //#line 1410 "jparser.y"
                            { yyval.obj = new DivNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 256:
                            //#line 1412 "jparser.y"
                            { yyval.obj = new RemNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 257:
                            //#line 1414 "jparser.y"
                            { yyval.obj = new PlusNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 258:
                            //#line 1416 "jparser.y"
                            { yyval.obj = new MinusNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 259:
                            //#line 1418 "jparser.y"
                            { yyval.obj = new LeftShiftLogNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 260:
                            //#line 1420 "jparser.y"
                            { yyval.obj = new RightShiftLogNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 261:
                            //#line 1422 "jparser.y"
                            { yyval.obj = new RightShiftArithNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 262:
                            //#line 1424 "jparser.y"
                            { yyval.obj = new LTNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 263:
                            //#line 1426 "jparser.y"
                            { yyval.obj = new GTNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 264:
                            //#line 1428 "jparser.y"
                            { yyval.obj = new LENode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 265:
                            //#line 1430 "jparser.y"
                            { yyval.obj = new GENode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 266:
                            //#line 1432 "jparser.y"
                            { yyval.obj = new InstanceOfNode((ExprNode) val_peek(2).obj, (TypeNode) val_peek(0).obj); }
                            break;
                        case 267:
                            //#line 1434 "jparser.y"
                            { yyval.obj = new EQNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 268:
                            //#line 1436 "jparser.y"
                            { yyval.obj = new NENode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 269:
                            //#line 1438 "jparser.y"
                            { yyval.obj = new BitAndNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 270:
                            //#line 1440 "jparser.y"
                            { yyval.obj = new BitOrNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 271:
                            //#line 1442 "jparser.y"
                            { yyval.obj = new BitXorNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 272:
                            //#line 1444 "jparser.y"
                            { yyval.obj = new CandNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 273:
                            //#line 1446 "jparser.y"
                            { yyval.obj = new CorNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 274:
                            //#line 1448 "jparser.y"
                            { yyval.obj = new IfExprNode((ExprNode) val_peek(4).obj, (ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 276:
                            //#line 1457 "jparser.y"
                            { yyval.obj = new AssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 277:
                            //#line 1459 "jparser.y"
                            { yyval.obj = new MultAssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 278:
                            //#line 1461 "jparser.y"
                            { yyval.obj = new DivAssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 279:
                            //#line 1463 "jparser.y"
                            { yyval.obj = new RemAssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 280:
                            //#line 1465 "jparser.y"
                            { yyval.obj = new PlusAssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 281:
                            //#line 1467 "jparser.y"
                            { yyval.obj = new MinusAssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 282:
                            //#line 1469 "jparser.y"
                            { yyval.obj = new LeftShiftLogAssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 283:
                            //#line 1471 "jparser.y"
                            { yyval.obj = new RightShiftLogAssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 284:
                            //#line 1473 "jparser.y"
                            { yyval.obj = new RightShiftArithAssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 285:
                            //#line 1475 "jparser.y"
                            { yyval.obj = new BitAndAssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 286:
                            //#line 1477 "jparser.y"
                            { yyval.obj = new BitXorAssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                        case 287:
                            //#line 1479 "jparser.y"
                            { yyval.obj = new BitOrAssignNode((ExprNode) val_peek(2).obj, (ExprNode) val_peek(0).obj); }
                            break;
                            //#line 3358 "JavaParser.java"
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
