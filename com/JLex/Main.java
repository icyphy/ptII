/**************************************************************
  JLex: A Lexical Analyzer Generator for Java(TM)
  Written by Elliot Berk. Copyright 1996.
  Contact at ejberk@princeton.edu.

  Version 1.2.4, 7/24/99, [C. Scott Ananian]
   Correct the parsing of '-' in character classes, closing active
     bug #1.  Behaviour follows egrep: leading and trailing dashes in
     a character class lose their special meaning, so [-+] and [+-] do
     what you would expect them to.
   New %ignorecase directive for generating case-insensitive lexers by
     expanding matched character classes in a unicode-friendly way.
   Handle unmatched braces in quoted strings or comments within
     action code blocks.
   Fixed input lexer to allow whitespace in character classes, closing
     active bug #9.  Whitespace in quotes had been previously fixed.
   Made Yylex.YYEOF and %yyeof work like the manual says they should.

  Version 1.2.3, 6/26/97, [Raimondas Lencevicius]
   Fixed the yy_nxt[][] assignment that has generated huge code
   exceeding 64K method size limit. Now the assignment
   is handled by unpacking a string encoding of integer array.
   To achieve that, added
   "private int [][] unpackFromString(int size1, int size2, String st)"
   function and coded the yy_nxt[][] values into a string
   by printing integers into a string and representing
   integer sequences as "value:length" pairs.
   Improvement: generated .java file reduced 2 times, .class file
     reduced 6 times for sample grammar. No 64K errors.
   Possible negatives: Some editors and OSs may not be able to handle
     the huge one-line generated string. String unpacking may be slower
     than direct array initialization.

  Version 1.2.2, 10/24/97, [Martin Dirichs]
  Notes:
    Changed yy_instream to yy_reader of type BufferedReader. This reflects
     the improvements in the JDK 1.1 concerning InputStreams. As a
     consequence, changed yy_buffer from byte[] to char[].
     The lexer can now be initialized with either an InputStream
     or a Reader. A third, private constructor is called by the other
     two to execute user specified constructor code.

  Version 1.2.1, 9/15/97 [A. Appel]
   Fixed bugs 6 (character codes > 127) and 10 (deprecated String constructor).

  Version 1.2, 5/5/97, [Elliot Berk]
  Notes:
    Simply changed the name from JavaLex to JLex.  No other changes.

  Version 1.1.5, 2/25/97, [Elliot Berk]
  Notes:
    Simple optimization to the creation of the source files.
     Added a BufferedOutputStream in the creation of the DataOutputStream
     field m_outstream of the class CLexGen.  This helps performance by
     doing some buffering, and was suggested by Max Hailperin,
     Associate Professor of Computer Science, Gustavus Adolphus College.

  Version 1.1.4, 12/12/96, [Elliot Berk]
  Notes:
    Added %public directive to make generated class public.

  Version 1.1.3, 12/11/96, [Elliot Berk]
  Notes:
    Converted assertion failure on invalid character class
     when a dash '-' is not preceded with a start-of-range character.
     Converted this into parse error E_DASH.

  Version 1.1.2, October 30, 1996 [Elliot Berk]
    Fixed BitSet bugs by installing a BitSet class of my own,
     called JavaLexBitSet.  Fixed support for '\r', non-UNIX
     sequences.  Added try/catch block around lexer generation
     in main routine to moderate error information presented
     to user.  Fixed macro expansion, so that macros following
     quotes are expanded correctly in regular expressions.
     Fixed dynamic reallocation of accept action buffers.

  Version 1.1.1, September 3, 1996 [Andrew Appel]
    Made the class "Main" instead of "JavaLex",
     improved the installation instructions to reflect this.

  Version 1.1, August 15, 1996  [Andrew Appel]
    Made yychar, yyline, yytext global to the lexer so that
     auxiliary functions can access them.

/***************************************************************
  Package Declaration
  **************************************************************/
// package JLex;
package com.JLex; // ctsay


/***************************************************************
  Imported Packages
  **************************************************************/
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

/******************************
  Questions:
  2) How should I use the Java package system
  to make my tool more modularized and
  coherent?

  Unimplemented:
  !) Fix BitSet issues -- expand only when necessary.
  2) Repeated accept rules.
  6) Clean up the CAlloc class and use buffered
  allocation.
  7) Reading and writing in Unicode.
  Use DataInputStream and DataOutputStream,
  along with writeUTF???
  8) Fix or at least annotate ^x bug.
  9) Add to spec about extending character set.
  10) making sure newlines are handled correctly
S  and consistly with the m_unix flag
  11) m_verbose -- what should be done with it?
  12) turn lexical analyzer into a coherent
  Java package
  13) turn lexical analyzer generator into a
  coherent Java package
  16) pretty up generated code
  17) make it possible to have white space in
  regular expressions
  18) clean up all of the class files the lexer
  generator produces when it is compiled,
  and reduce this number in some way.
  24) character format to and from file: writeup
  and implementation
  25) Debug by testing all arcane regular expression cases.
  26) Look for and fix all UNDONE comments below.
  27) Fix package system.
  28) Clean up unnecessary classes.
  *****************************/

/***************************************************************
  Class: CSpec
 **************************************************************/
class CSpec
{
    /***************************************************************
                                                                    Member Variables
    **************************************************************/

  /* Lexical States. */
    Hashtable m_states; /* Hashtable taking state indices (Integer)
                           to state name (String). */

  /* Regular Expression Macros. */
    Hashtable m_macros; /* Hashtable taking macro name (String)
                           to corresponding char buffer that
                           holds macro definition. */

  /* NFA Machine. */
    CNfa m_nfa_start; /* Start state of NFA machine. */
    Vector m_nfa_states; /* Vector of states, with index
                                 corresponding to label. */

    Vector m_state_rules[]; /* An array of Vectors of Integers.
                               The ith Vector represents the lexical state
                               with index i.  The contents of the ith
                               Vector are the indices of the NFA start
                               states that can be matched while in
                               the ith lexical state. */


    int m_state_dtrans[];

    /* DFA Machine. */
    Vector m_dfa_states; /* Vector of states, with index
                            corresponding to label. */
    Hashtable m_dfa_sets; /* Hashtable taking set of NFA states
                             to corresponding DFA state,
                             if the latter exists. */

    /* Accept States and Corresponding Anchors. */
    Vector m_accept_vector;
    int m_anchor_array[];

  /* Transition Table. */
    Vector m_dtrans_vector;
    int m_dtrans_ncols;
    int m_row_map[];
    int m_col_map[];

  /* Regular expression token variables. */
    int m_current_token;
    char m_lexeme;
    boolean m_in_quote;
    boolean m_in_ccl;

  /* Verbose execution flag. */
    boolean m_verbose;

    /* JLex directives flags. */
    boolean m_integer_type;
    boolean m_intwrap_type;
    boolean m_yyeof;
    boolean m_count_chars;
    boolean m_count_lines;
    boolean m_cup_compatible;
    boolean m_unix;
    boolean m_public;
    boolean m_ignorecase;

    char m_init_code[];
    int m_init_read;

    char m_init_throw_code[];
    int m_init_throw_read;

    char m_class_code[];
    int m_class_read;

    char m_eof_code[];
    int m_eof_read;

    char m_eof_value_code[];
    int m_eof_value_read;

    char m_eof_throw_code[];
    int m_eof_throw_read;

    char m_yylex_throw_code[];
    int m_yylex_throw_read;

    /* Class, function, type names. */
    char m_class_name[] = {
        'Y', 'y', 'l',
        'e', 'x'
    };
    char m_implements_name[] = {};
    char m_function_name[] = {
        'y', 'y', 'l',
        'e', 'x'
    };
    char m_type_name[] = {
        'Y', 'y', 't',
        'o', 'k', 'e',
        'n'
    };

    /* Lexical Generator. */
    private CLexGen m_lexGen;

    /***************************************************************
    Constants
    ***********************************************************/
    static final int NONE = 0;
    static final int START = 1;
    static final int END = 2;

    /***************************************************************
    Function: CSpec
    Description: Constructor.
    **************************************************************/
    CSpec
    (
            CLexGen lexGen
            )
    {
        m_lexGen = lexGen;

        /* Initialize regular expression token variables. */
        m_current_token = CLexGen.EOS;
        m_lexeme = '\0';
        m_in_quote = false;
        m_in_ccl = false;

        /* Initialize hashtable for lexer states. */
        m_states = new Hashtable();
        m_states.put(new String("YYINITIAL"),new Integer(m_states.size()));

        /* Initialize hashtable for lexical macros. */
        m_macros = new Hashtable();

        /* Initialize variables for lexer options. */
        m_integer_type = false;
        m_intwrap_type = false;
        m_count_lines = false;
        m_count_chars = false;
        m_cup_compatible = false;
        m_unix = true;
        m_public = false;
        m_yyeof = false;
        m_ignorecase = false;

        /* Initialize variables for JLex runtime options. */
        m_verbose = true;

        m_nfa_start = null;
        m_nfa_states = new Vector();

        m_dfa_states = new Vector();
        m_dfa_sets = new Hashtable();

        m_dtrans_vector = new Vector();
        m_dtrans_ncols = CUtility.MAX_SEVEN_BIT + 1;
        m_row_map = null;
        m_col_map = null;

        m_accept_vector = null;
        m_anchor_array = null;

        m_init_code = null;
        m_init_read = 0;

        m_init_throw_code = null;
        m_init_throw_read = 0;

        m_yylex_throw_code = null;
        m_yylex_throw_read = 0;

        m_class_code = null;
        m_class_read = 0;

        m_eof_code = null;
        m_eof_read = 0;

        m_eof_value_code = null;
        m_eof_value_read = 0;

        m_eof_throw_code = null;
        m_eof_throw_read = 0;

        m_state_dtrans = null;

        m_state_rules = null;
    }
}

/***************************************************************
  Class: CEmit
  **************************************************************/
class CEmit
{
    /***************************************************************
                                                                    Member Variables
    **************************************************************/
    private CSpec m_spec;
    private DataOutputStream m_outstream;

  /***************************************************************
    Constants: Anchor Types
    **************************************************************/
    // private final int START = 1;
    // private final int END = 2;
    // private final int NONE = 4;

    /***************************************************************
    Constants
    **************************************************************/
    //private final boolean EDBG = true;
    private final boolean NOT_EDBG = false;

  /***************************************************************
    Function: CEmit
    Description: Constructor.
    **************************************************************/
    CEmit
    (
     )
    {
        reset();
    }

    /***************************************************************
    Function: reset
    Description: Clears member variables.
    **************************************************************/
    private void reset
    (
     )
    {
        m_spec = null;
        m_outstream = null;
    }

    /***************************************************************
    Function: set
    Description: Initializes member variables.
    **************************************************************/
    private void set
    (
            CSpec spec,
            OutputStream outstream
            )
    {
        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != spec);
                CUtility.cuassert(null != outstream);
            }

        m_spec = spec;
        m_outstream = new DataOutputStream(outstream);
    }

    /***************************************************************
    Function: emit_imports
    Description: Emits import packages at top of
    generated source file.
    **************************************************************/
  /*void emit_imports
    (
     CSpec spec,
     OutputStream outstream
     )
      throws java.io.IOException
        {
          set(spec,outstream);

          if (CUtility.DEBUG)
            {
              CUtility.cuassert(null != m_spec);
              CUtility.cuassert(null != m_outstream);
            }*/

          /*m_outstream.writeBytes("import java.lang.String;\n");
          m_outstream.writeBytes("import java.lang.System;\n");
          m_outstream.writeBytes("import java.io.BufferedReader;\n");
          m_outstream.writeBytes("import java.io.InputStream;\n");*/
        /*
          reset();
        }*/

  /***************************************************************
    Function: print_details
    Description: Debugging output.
    **************************************************************/
    private void print_details
    (
     )
    {
        int i;
        int j;
        int next;
        int state;
        CDTrans dtrans;
        CAccept accept;
        boolean tr;

        System.out.println("---------------------- Transition Table "
                + "----------------------");

        for (i = 0; i < m_spec.m_row_map.length; ++i)
            {
                System.out.print("State " + i);

                accept = (CAccept) m_spec.m_accept_vector.elementAt(i);
                if (null == accept)
                    {
                        System.out.println(" [nonaccepting]");
                    }
                else
                    {
                        System.out.println(" [accepting, line "
                                + accept.m_line_number
                                + " <"
                                + (new java.lang.String(accept.m_action,0,
                                        accept.m_action_read))
                                + ">]");
                    }
                dtrans = (CDTrans) m_spec.m_dtrans_vector.elementAt(m_spec.m_row_map[i]);

                tr = false;
                state = dtrans.m_dtrans[m_spec.m_col_map[0]];
                if (CDTrans.F != state)
                    {
                        tr = true;
                        System.out.print("\tgoto " + state + " on [" + ((char) 0));
                    }
                for (j = 1; j < m_spec.m_dtrans_ncols; ++j)
                    {
                        next = dtrans.m_dtrans[m_spec.m_col_map[j]];
                        if (state == next)
                            {
                                if (CDTrans.F != state)
                                    {
                                        System.out.print((char) j);
                                    }
                            }
                        else
                            {
                                state = next;
                                if (tr)
                                    {
                                        System.out.println("]");
                                        tr = false;
                                    }
                                if (CDTrans.F != state)
                                    {
                                        tr = true;
                                        System.out.print("\tgoto " + state + " on [" + ((char) j));
                                    }
                            }
                    }
                if (tr)
                    {
                        System.out.println("]");
                    }
            }

        System.out.println("---------------------- Transition Table "
                + "----------------------");
    }

    /***************************************************************
    Function: emit
    Description: High-level access function to module.
    **************************************************************/
    void emit
    (
            CSpec spec,
            OutputStream outstream
            )
            throws java.io.IOException
    {
        set(spec,outstream);

        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != m_spec);
                CUtility.cuassert(null != m_outstream);
            }

        if (CUtility.OLD_DEBUG) {
            print_details();
        }

        emit_header();
        emit_construct();
        emit_helpers();
        emit_driver();
        emit_footer();

        reset();
    }

    /***************************************************************
    Function: emit_construct
    Description: Emits constructor, member variables,
    and constants.
    **************************************************************/
    private void emit_construct
    (
     )
            throws java.io.IOException
    {
        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != m_spec);
                CUtility.cuassert(null != m_outstream);
            }

        /* Constants */
        m_outstream.writeBytes("\tprivate final int YY_BUFFER_SIZE = 512;\n");

        m_outstream.writeBytes("\tprivate final int YY_F = -1;\n");
        m_outstream.writeBytes("\tprivate final int YY_NO_STATE = -1;\n");

        m_outstream.writeBytes("\tprivate final int YY_NOT_ACCEPT = 0;\n");
        m_outstream.writeBytes("\tprivate final int YY_START = 1;\n");
        m_outstream.writeBytes("\tprivate final int YY_END = 2;\n");
        m_outstream.writeBytes("\tprivate final int YY_NO_ANCHOR = 4;\n");

        m_outstream.writeBytes("\tprivate final char YY_EOF = \'\\uFFFF\';\n");
        if (m_spec.m_integer_type || true == m_spec.m_yyeof)
            m_outstream.writeBytes("\tpublic final int YYEOF = -1;\n");

        /* User specified class code. */
        if (null != m_spec.m_class_code)
            {
                m_outstream.writeBytes(new String(m_spec.m_class_code,0,
                        m_spec.m_class_read));
            }

        /* Member Variables */
        m_outstream.writeBytes("\tprivate java.io.BufferedReader yy_reader;\n");
        m_outstream.writeBytes("\tprivate int yy_buffer_index;\n");
        m_outstream.writeBytes("\tprivate int yy_buffer_read;\n");
        m_outstream.writeBytes("\tprivate int yy_buffer_start;\n");
        m_outstream.writeBytes("\tprivate int yy_buffer_end;\n");
        m_outstream.writeBytes("\tprivate char yy_buffer[];\n");
        if (m_spec.m_count_chars)
            {
                m_outstream.writeBytes("\tprivate int yychar;\n");
            }
        if (m_spec.m_count_lines)
            {
                m_outstream.writeBytes("\tprivate int yyline;\n");
            }
        m_outstream.writeBytes("\tprivate int yy_lexical_state;\n");
        /*if (m_spec.m_count_lines || true == m_spec.m_count_chars)
          {
          m_outstream.writeBytes("\tprivate int yy_buffer_prev_start;\n");
          }*/
        m_outstream.writeBytes("\n");


        /* Function: first constructor (Reader) */
        m_outstream.writeBytes("\t");
        if (true == m_spec.m_public) {
            m_outstream.writeBytes("public ");
        }
        m_outstream.writeBytes(new String(m_spec.m_class_name));
        m_outstream.writeBytes(" (java.io.Reader reader)");

        if (null != m_spec.m_init_throw_code)
            {
                m_outstream.writeBytes("\n");
                m_outstream.writeBytes("\t\tthrows ");
                m_outstream.writeBytes(new String(m_spec.m_init_throw_code,0,
                        m_spec.m_init_throw_read));
                m_outstream.writeBytes("\n\t\t{\n");
            }
        else
            {
                m_outstream.writeBytes(" {\n");
            }

        m_outstream.writeBytes("\t\tthis ();\n");
        m_outstream.writeBytes("\t\tif (null == reader) {\n");
        m_outstream.writeBytes("\t\t\tthrow (new Error(\"Error: Bad input "
                + "stream initializer.\"));\n");
        m_outstream.writeBytes("\t\t}\n");
        m_outstream.writeBytes("\t\tyy_reader = new java.io.BufferedReader(reader);\n");
        m_outstream.writeBytes("\t}\n\n");


        /* Function: second constructor (InputStream) */
        m_outstream.writeBytes("\t");
        if (true == m_spec.m_public) {
            m_outstream.writeBytes("public ");
        }
        m_outstream.writeBytes(new String(m_spec.m_class_name));
        m_outstream.writeBytes(" (java.io.InputStream instream)");

        if (null != m_spec.m_init_throw_code)
            {
                m_outstream.writeBytes("\n");
                m_outstream.writeBytes("\t\tthrows ");
                m_outstream.writeBytes(new String(m_spec.m_init_throw_code,0,
                        m_spec.m_init_throw_read));
                m_outstream.writeBytes("\n\t\t{\n");
            }
        else
            {
                m_outstream.writeBytes(" {\n");
            }

        m_outstream.writeBytes("\t\tthis ();\n");
        m_outstream.writeBytes("\t\tif (null == instream) {\n");
        m_outstream.writeBytes("\t\t\tthrow (new Error(\"Error: Bad input "
                + "stream initializer.\"));\n");
        m_outstream.writeBytes("\t\t}\n");
        m_outstream.writeBytes("\t\tyy_reader = new java.io.BufferedReader(new java.io.InputStreamReader(instream));\n");
        m_outstream.writeBytes("\t}\n\n");


        /* Function: third, private constructor - only for internal use */
        m_outstream.writeBytes("\tprivate ");
        m_outstream.writeBytes(new String(m_spec.m_class_name));
        m_outstream.writeBytes(" ()");

        if (null != m_spec.m_init_throw_code)
            {
                m_outstream.writeBytes("\n");
                m_outstream.writeBytes("\t\tthrows ");
                m_outstream.writeBytes(new String(m_spec.m_init_throw_code,0,
                        m_spec.m_init_throw_read));
                m_outstream.writeBytes("\n\t\t{\n");
            }
        else
            {
                m_outstream.writeBytes(" {\n");
            }

        m_outstream.writeBytes("\t\tyy_buffer = new char[YY_BUFFER_SIZE];\n");
        m_outstream.writeBytes("\t\tyy_buffer_read = 0;\n");
        m_outstream.writeBytes("\t\tyy_buffer_index = 0;\n");
        m_outstream.writeBytes("\t\tyy_buffer_start = 0;\n");
        m_outstream.writeBytes("\t\tyy_buffer_end = 0;\n");
        if (m_spec.m_count_chars)
            {
                m_outstream.writeBytes("\t\tyychar = 0;\n");
            }
        if (m_spec.m_count_lines)
            {
                m_outstream.writeBytes("\t\tyyline = 0;\n");
            }
        m_outstream.writeBytes("\t\tyy_lexical_state = YYINITIAL;\n");
        /*if (m_spec.m_count_lines || true == m_spec.m_count_chars)
          {
          m_outstream.writeBytes("\t\tyy_buffer_prev_start = 0;\n");
          }*/

        /* User specified constructor code. */
        if (null != m_spec.m_init_code)
            {
                m_outstream.writeBytes(new String(m_spec.m_init_code,0,
                        m_spec.m_init_read));
            }

        m_outstream.writeBytes("\t}\n\n");

    }

    /***************************************************************
    Function: emit_states
    Description: Emits constants that serve as lexical states,
    including YYINITIAL.
    **************************************************************/
    private void emit_states
    (
     )
            throws java.io.IOException
    {
        Enumeration states;
        String state;
        int index;

        states = m_spec.m_states.keys();
        /*index = 0;*/
        while (states.hasMoreElements())
            {
                state = (String) states.nextElement();

                if (CUtility.DEBUG)
                    {
                        CUtility.cuassert(null != state);
                    }

                m_outstream.writeBytes("\tprivate final int "
                        + state
                        + " = "
                        + (m_spec.m_states.get(state)).toString()
                        + ";\n");
                /*++index;*/
            }

        m_outstream.writeBytes("\tprivate final int yy_state_dtrans[] = {\n");
        for (index = 0; index < m_spec.m_state_dtrans.length; ++index)
            {
                m_outstream.writeBytes("\t\t" + m_spec.m_state_dtrans[index]);
                if (index < m_spec.m_state_dtrans.length - 1)
                    {
                        m_outstream.writeBytes(",\n");
                    }
                else
                    {
                        m_outstream.writeBytes("\n");
                    }
            }
        m_outstream.writeBytes("\t};\n");
    }

    /***************************************************************
    Function: emit_helpers
    Description: Emits helper functions, particularly
    error handling and input buffering.
    **************************************************************/
    private void emit_helpers
    (
     )
            throws java.io.IOException
    {
        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != m_spec);
                CUtility.cuassert(null != m_outstream);
            }

        /* Function: yy_do_eof */
        m_outstream.writeBytes("\tprivate boolean yy_eof_done = false;\n");
        if (null != m_spec.m_eof_code)
            {
                m_outstream.writeBytes("\tprivate void yy_do_eof ()");

                if (null != m_spec.m_eof_throw_code)
                    {
                        m_outstream.writeBytes("\n");
                        m_outstream.writeBytes("\t\tthrows ");
                        m_outstream.writeBytes(new String(m_spec.m_eof_throw_code,0,
                                m_spec.m_eof_throw_read));
                        m_outstream.writeBytes("\n\t\t{\n");
                    }
                else
                    {
                        m_outstream.writeBytes(" {\n");
                    }

                m_outstream.writeBytes("\t\tif (false == yy_eof_done) {\n");
                m_outstream.writeBytes(new String(m_spec.m_eof_code,0,
                        m_spec.m_eof_read));
                m_outstream.writeBytes("\t\t}\n");
                m_outstream.writeBytes("\t\tyy_eof_done = true;\n");
                m_outstream.writeBytes("\t}\n");
            }

        emit_states();

        /* Function: yybegin */
        m_outstream.writeBytes("\tprivate void yybegin (int state) {\n");
        m_outstream.writeBytes("\t\tyy_lexical_state = state;\n");
        m_outstream.writeBytes("\t}\n");

        /* Function: yy_initial_dtrans */
        /*m_outstream.writeBytes("\tprivate int yy_initial_dtrans (int state) {\n");
          m_outstream.writeBytes("\t\treturn yy_state_dtrans[state];\n");
          m_outstream.writeBytes("\t}\n");*/

        /* Function: yy_advance */
        m_outstream.writeBytes("\tprivate char yy_advance ()\n");
        m_outstream.writeBytes("\t\tthrows java.io.IOException {\n");
        /*m_outstream.writeBytes("\t\t{\n");*/
        m_outstream.writeBytes("\t\tint next_read;\n");
        m_outstream.writeBytes("\t\tint i;\n");
        m_outstream.writeBytes("\t\tint j;\n");
        m_outstream.writeBytes("\n");

        m_outstream.writeBytes("\t\tif (yy_buffer_index < yy_buffer_read) {\n");
        m_outstream.writeBytes("\t\t\treturn yy_buffer[yy_buffer_index++];\n");
        /*m_outstream.writeBytes("\t\t\t++yy_buffer_index;\n");*/
        m_outstream.writeBytes("\t\t}\n");
        m_outstream.writeBytes("\n");

        m_outstream.writeBytes("\t\tif (0 != yy_buffer_start) {\n");
        m_outstream.writeBytes("\t\t\ti = yy_buffer_start;\n");
        m_outstream.writeBytes("\t\t\tj = 0;\n");
        m_outstream.writeBytes("\t\t\twhile (i < yy_buffer_read) {\n");
        m_outstream.writeBytes("\t\t\t\tyy_buffer[j] = yy_buffer[i];\n");
        m_outstream.writeBytes("\t\t\t\t++i;\n");
        m_outstream.writeBytes("\t\t\t\t++j;\n");
        m_outstream.writeBytes("\t\t\t}\n");
        m_outstream.writeBytes("\t\t\tyy_buffer_end = yy_buffer_end - yy_buffer_start;\n");
        m_outstream.writeBytes("\t\t\tyy_buffer_start = 0;\n");
        m_outstream.writeBytes("\t\t\tyy_buffer_read = j;\n");
        m_outstream.writeBytes("\t\t\tyy_buffer_index = j;\n");
        m_outstream.writeBytes("\t\t\tnext_read = yy_reader.read(yy_buffer,\n");
        m_outstream.writeBytes("\t\t\t\t\tyy_buffer_read,\n");
        m_outstream.writeBytes("\t\t\t\t\tyy_buffer.length - yy_buffer_read);\n");
        m_outstream.writeBytes("\t\t\tif (-1 == next_read) {\n");
        m_outstream.writeBytes("\t\t\t\treturn YY_EOF;\n");
        m_outstream.writeBytes("\t\t\t}\n");
        m_outstream.writeBytes("\t\t\tyy_buffer_read = yy_buffer_read + next_read;\n");
        m_outstream.writeBytes("\t\t}\n");
        m_outstream.writeBytes("\n");

        m_outstream.writeBytes("\t\twhile (yy_buffer_index >= yy_buffer_read) {\n");
        m_outstream.writeBytes("\t\t\tif (yy_buffer_index >= yy_buffer.length) {\n");
        m_outstream.writeBytes("\t\t\t\tyy_buffer = yy_double(yy_buffer);\n");
        m_outstream.writeBytes("\t\t\t}\n");
        m_outstream.writeBytes("\t\t\tnext_read = yy_reader.read(yy_buffer,\n");
        m_outstream.writeBytes("\t\t\t\t\tyy_buffer_read,\n");
        m_outstream.writeBytes("\t\t\t\t\tyy_buffer.length - yy_buffer_read);\n");
        m_outstream.writeBytes("\t\t\tif (-1 == next_read) {\n");
        m_outstream.writeBytes("\t\t\t\treturn YY_EOF;\n");
        m_outstream.writeBytes("\t\t\t}\n");
        m_outstream.writeBytes("\t\t\tyy_buffer_read = yy_buffer_read + next_read;\n");
        m_outstream.writeBytes("\t\t}\n");

        m_outstream.writeBytes("\t\treturn yy_buffer[yy_buffer_index++];\n");
        m_outstream.writeBytes("\t}\n");

        /* Function: yy_move_start */
        m_outstream.writeBytes("\tprivate void yy_move_start () {\n");
        if (m_spec.m_count_lines)
            {
                if (m_spec.m_unix)
                    {
                        m_outstream.writeBytes("\t\tif ((byte) '\\n' "
                                + "== yy_buffer[yy_buffer_start]) {\n");
                    }
                else
                    {
                        m_outstream.writeBytes("\t\tif ((byte) '\\n' "
                                + "== yy_buffer[yy_buffer_start]\n");
                        m_outstream.writeBytes("\t\t\t|| (byte) '\\r' "
                                + "== yy_buffer[yy_buffer_start]) {\n");
                    }
                m_outstream.writeBytes("\t\t\t++yyline;\n");
                m_outstream.writeBytes("\t\t}\n");
            }
        if (m_spec.m_count_chars)
            {
                m_outstream.writeBytes("\t\t++yychar;\n");
            }
        m_outstream.writeBytes("\t\t++yy_buffer_start;\n");
        m_outstream.writeBytes("\t}\n");

        /* Function: yy_pushback */
        m_outstream.writeBytes("\tprivate void yy_pushback () {\n");
        m_outstream.writeBytes("\t\t--yy_buffer_end;\n");
        m_outstream.writeBytes("\t}\n");

        /* Function: yy_mark_start */
        m_outstream.writeBytes("\tprivate void yy_mark_start () {\n");
        if (m_spec.m_count_lines || true == m_spec.m_count_chars)
            {
                if (m_spec.m_count_lines)
                    {
                        m_outstream.writeBytes("\t\tint i;\n");
                        m_outstream.writeBytes("\t\tfor (i = yy_buffer_start; "
                                + "i < yy_buffer_index; ++i) {\n");
                        if (m_spec.m_unix)
                            {
                                m_outstream.writeBytes("\t\t\tif ((byte) '\\n' == yy_buffer[i]) {\n");
                            }
                        else
                            {
                                m_outstream.writeBytes("\t\t\tif ((byte) '\\n' == yy_buffer[i] || "
                                        + "(byte) '\\r' == yy_buffer[i]) {\n");
                            }
                        m_outstream.writeBytes("\t\t\t\t++yyline;\n");
                        m_outstream.writeBytes("\t\t\t}\n");
                        m_outstream.writeBytes("\t\t}\n");
                    }
                if (m_spec.m_count_chars)
                    {
                        m_outstream.writeBytes("\t\tyychar = yychar\n");
                        m_outstream.writeBytes("\t\t\t+ yy_buffer_index - yy_buffer_start;\n");
                    }
            }
        m_outstream.writeBytes("\t\tyy_buffer_start = yy_buffer_index;\n");
        m_outstream.writeBytes("\t}\n");

        /* Function: yy_mark_end */
        m_outstream.writeBytes("\tprivate void yy_mark_end () {\n");
        m_outstream.writeBytes("\t\tyy_buffer_end = yy_buffer_index;\n");
        m_outstream.writeBytes("\t}\n");

        /* Function: yy_to_mark */
        m_outstream.writeBytes("\tprivate void yy_to_mark () {\n");
        m_outstream.writeBytes("\t\tyy_buffer_index = yy_buffer_end;\n");
        m_outstream.writeBytes("\t}\n");

        /* Function: yytext */
        m_outstream.writeBytes("\tprivate java.lang.String yytext () {\n");
        m_outstream.writeBytes("\t\treturn (new java.lang.String(yy_buffer,\n");
        m_outstream.writeBytes("\t\t\tyy_buffer_start,\n");
        m_outstream.writeBytes("\t\t\tyy_buffer_end - yy_buffer_start));\n");
        m_outstream.writeBytes("\t}\n");

        /* Function: yylength */
        m_outstream.writeBytes("\tprivate int yylength () {\n");
        m_outstream.writeBytes("\t\treturn yy_buffer_end - yy_buffer_start;\n");
        m_outstream.writeBytes("\t}\n");

        /* Function: yy_double */
        m_outstream.writeBytes("\tprivate char[] yy_double (char buf[]) {\n");
        m_outstream.writeBytes("\t\tint i;\n\t\tchar newbuf[];\n");
        m_outstream.writeBytes("\t\tnewbuf = new char[2*buf.length];\n");
        m_outstream.writeBytes("\t\tfor (i = 0; i < buf.length; ++i) {\n");
        m_outstream.writeBytes("\t\t\tnewbuf[i] = buf[i];\n");
        m_outstream.writeBytes("\t\t}\n");
        m_outstream.writeBytes("\t\treturn newbuf;\n");
        m_outstream.writeBytes("\t}\n");

        /* Function: yy_error */
        m_outstream.writeBytes("\tprivate final int YY_E_INTERNAL = 0;\n");
        m_outstream.writeBytes("\tprivate final int YY_E_MATCH = 1;\n");
        m_outstream.writeBytes("\tprivate java.lang.String yy_error_string[] = {\n");
        m_outstream.writeBytes("\t\t\"Error: Internal error.\\n\",\n");
        m_outstream.writeBytes("\t\t\"Error: Unmatched input.\\n\"\n");
        m_outstream.writeBytes("\t};\n");
        m_outstream.writeBytes("\tprivate void yy_error (int code,boolean fatal) {\n");
        m_outstream.writeBytes("\t\tjava.lang.System.out.print(yy_error_string[code]);\n");
        m_outstream.writeBytes("\t\tjava.lang.System.out.flush();\n");
        m_outstream.writeBytes("\t\tif (fatal) {\n");
        m_outstream.writeBytes("\t\t\tthrow new Error(\"Fatal Error.\\n\");\n");
        m_outstream.writeBytes("\t\t}\n");
        m_outstream.writeBytes("\t}\n");

        /* Function: yy_next */
        /*m_outstream.writeBytes("\tprivate int yy_next (int current,char lookahead) {\n");
          m_outstream.writeBytes("\t\treturn yy_nxt[yy_rmap[current]][yy_cmap[lookahead]];\n");
          m_outstream.writeBytes("\t}\n");*/

        /* Function: yy_accept */
        /*m_outstream.writeBytes("\tprivate int yy_accept (int current) {\n");
          m_outstream.writeBytes("\t\treturn yy_acpt[current];\n");
          m_outstream.writeBytes("\t}\n");*/


        // Function: private int [][] unpackFromString(int size1, int size2, String st)
        // Added 6/24/98 Raimondas Lencevicius
        // May be made more efficient by replacing String operations
        // Assumes correctly formed input String. Performs no error checking
        m_outstream.writeBytes("private int [][] unpackFromString(int size1, int size2, String st)\n");
        m_outstream.writeBytes("    {\n");
        m_outstream.writeBytes("      int colonIndex = -1;\n");
        m_outstream.writeBytes("      String lengthString;\n");
        m_outstream.writeBytes("      int sequenceLength = 0;\n");
        m_outstream.writeBytes("      int sequenceInteger = 0;\n");

        m_outstream.writeBytes("      int commaIndex;\n");
        m_outstream.writeBytes("      String workString;\n");

        m_outstream.writeBytes("      int res[][] = new int[size1][size2];\n");
        m_outstream.writeBytes("      for (int i= 0; i < size1; i++)\n");
        m_outstream.writeBytes("        for (int j= 0; j < size2; j++)\n");
        m_outstream.writeBytes("          {\n");
        m_outstream.writeBytes("            if (sequenceLength == 0) \n");
        m_outstream.writeBytes("              {        \n");
        m_outstream.writeBytes("                commaIndex = st.indexOf(',');\n");
        m_outstream.writeBytes("                if (commaIndex == -1)\n");
        m_outstream.writeBytes("                  workString = st;\n");
        m_outstream.writeBytes("                else\n");
        m_outstream.writeBytes("                  workString = st.substring(0, commaIndex);\n");
        m_outstream.writeBytes("                st = st.substring(commaIndex+1);\n");
        m_outstream.writeBytes("                colonIndex = workString.indexOf(':');\n");
        m_outstream.writeBytes("                if (colonIndex == -1)\n");
        m_outstream.writeBytes("                  {\n");
        m_outstream.writeBytes("                    res[i][j] = Integer.parseInt(workString);\n");
        m_outstream.writeBytes("                  }\n");
        m_outstream.writeBytes("                else \n");
        m_outstream.writeBytes("                  {\n");
        m_outstream.writeBytes("                    lengthString = workString.substring(colonIndex+1);  \n");
        m_outstream.writeBytes("                    sequenceLength = Integer.parseInt(lengthString);\n");
        m_outstream.writeBytes("                    workString = workString.substring(0,colonIndex);\n");
        m_outstream.writeBytes("                    sequenceInteger = Integer.parseInt(workString);\n");
        m_outstream.writeBytes("                    res[i][j] = sequenceInteger;\n");
        m_outstream.writeBytes("                    sequenceLength--;\n");
        m_outstream.writeBytes("                  }\n");
        m_outstream.writeBytes("              }\n");
        m_outstream.writeBytes("            else \n");
        m_outstream.writeBytes("              {\n");
        m_outstream.writeBytes("                res[i][j] = sequenceInteger;\n");
        m_outstream.writeBytes("                sequenceLength--;\n");
        m_outstream.writeBytes("              }\n");
        m_outstream.writeBytes("          }\n");
        m_outstream.writeBytes("      return res;\n");
        m_outstream.writeBytes("    }\n");



    }

    /***************************************************************
    Function: emit_header
    Description: Emits class header.
    **************************************************************/
    private void emit_header
    (
     )
            throws java.io.IOException
    {
        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != m_spec);
                CUtility.cuassert(null != m_outstream);
            }

        m_outstream.writeBytes("\n\n");
        if (true == m_spec.m_public) {
            m_outstream.writeBytes("public ");
        }
        m_outstream.writeBytes("class ");
        m_outstream.writeBytes(new String(m_spec.m_class_name,0,
                m_spec.m_class_name.length));
        if (m_spec.m_implements_name.length > 0) {
            m_outstream.writeBytes(" implements ");
            m_outstream.writeBytes(new String(m_spec.m_implements_name,0,
                    m_spec.m_implements_name.length));
        }
        m_outstream.writeBytes(" {\n");
    }

    /***************************************************************
    Function: emit_table
    Description: Emits transition table.
    **************************************************************/
    private void emit_table
    (
     )
            throws java.io.IOException
    {
        int i;
        int elem;
        int size;
        CDTrans dtrans;
        boolean is_start;
        boolean is_end;
        CAccept accept;

        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != m_spec);
                CUtility.cuassert(null != m_outstream);
            }

        m_outstream.writeBytes("\tprivate int yy_acpt[] = {\n");
        size = m_spec.m_accept_vector.size();
        for (elem = 0; elem < size; ++elem)
            {
                accept = (CAccept) m_spec.m_accept_vector.elementAt(elem);

                if (null != accept)
                    {
                        is_start = (0 != (m_spec.m_anchor_array[elem] & CSpec.START));
                        is_end = (0 != (m_spec.m_anchor_array[elem] & CSpec.END));

                        if (is_start && true == is_end)
                            {
                                m_outstream.writeBytes("\t\tYY_START | YY_END");
                            }
                        else if (is_start)
                            {
                                m_outstream.writeBytes("\t\tYY_START");
                            }
                        else if (is_end)
                            {
                                m_outstream.writeBytes("\t\tYY_END");
                            }
                        else
                            {
                                m_outstream.writeBytes("\t\tYY_NO_ANCHOR");
                            }
                    }
                else
                    {
                        m_outstream.writeBytes("\t\tYY_NOT_ACCEPT");
                    }

                if (elem < size - 1)
                    {
                        m_outstream.writeBytes(",");
                    }

                m_outstream.writeBytes("\n");
            }
        m_outstream.writeBytes("\t};\n");

        m_outstream.writeBytes("\tprivate int yy_cmap[] = {\n\t\t");
        for (i = 0; i < m_spec.m_col_map.length; ++i)
            {
                m_outstream.writeBytes((new Integer(m_spec.m_col_map[i])).toString());

                if (i < m_spec.m_col_map.length - 1)
                    {
                        m_outstream.writeBytes(",");
                    }

                if (0 == ((i + 1) % 8))
                    {
                        m_outstream.writeBytes("\n\t\t");
                    }
                else
                    {
                        m_outstream.writeBytes(" ");
                    }
            }
        m_outstream.writeBytes("\n\t};\n");

        m_outstream.writeBytes("\tprivate int yy_rmap[] = {\n\t\t");
        for (i = 0; i < m_spec.m_row_map.length; ++i)
            {
                m_outstream.writeBytes((new Integer(m_spec.m_row_map[i])).toString());

                if (i < m_spec.m_row_map.length - 1)
                    {
                        m_outstream.writeBytes(",");
                    }

                if (0 == ((i + 1) % 8))
                    {
                        m_outstream.writeBytes("\n\t\t");
                    }
                else
                    {
                        m_outstream.writeBytes(" ");
                    }
            }
        m_outstream.writeBytes("\n\t};\n");

        // 6/24/98 Raimondas Lencevicius
        // Code to the method end modified
        /* yy_nxt[][] is assigned result of
           "int [][] unpackFromString(int size1, int size2, String st)".
           yy_nxt[][] values are coded into a string
           by printing integers and representing
           integer sequences as "value:length" pairs. */

        m_outstream.writeBytes
            ("\tprivate int yy_nxt[][] = unpackFromString(");
        size = m_spec.m_dtrans_vector.size();
        int sequenceLength = 0; // RL - length of the number sequence
        boolean sequenceStarted = false; // RL - has number sequence started?
        int previousInt = -20; // RL - Bogus -20 state.

        // RL - Output matrix size
        m_outstream.writeBytes((new Integer(size)).toString());
        m_outstream.writeBytes(",");
        m_outstream.writeBytes((new Integer(m_spec.m_dtrans_ncols)).toString());
        m_outstream.writeBytes(",");
        m_outstream.writeBytes("\n\"");

        //  RL - Output matrix
        for (elem = 0; elem < size; ++elem)
            {

                dtrans = (CDTrans) m_spec.m_dtrans_vector.elementAt(elem);

                for (i = 0; i < m_spec.m_dtrans_ncols; ++i)
                    {
                        int writeInt = dtrans.m_dtrans[i];
                        if (writeInt == previousInt) // RL - sequence?
                            {
                                if (sequenceStarted)
                                    {
                                        sequenceLength++;
                                    }
                                else
                                    {
                                        m_outstream.writeBytes((new Integer(writeInt)).toString());
                                        m_outstream.writeBytes(":");
                                        sequenceLength = 2;
                                        sequenceStarted = true;
                                    }
                            }
                        else // RL - no sequence or end sequence
                            {
                                if (sequenceStarted)
                                    {
                                        m_outstream.writeBytes((new Integer(sequenceLength)).toString());
                                        m_outstream.writeBytes(",");
                                        sequenceLength = 0;
                                        sequenceStarted = false;
                                    }
                                else
                                    {
                                        if (previousInt != -20)
                                            {
                                                m_outstream.writeBytes((new Integer(previousInt)).toString());
                                                m_outstream.writeBytes(",");
                                            }
                                    }
                            }
                        previousInt = writeInt;
                    }
            }
        if (sequenceStarted)
            {
                m_outstream.writeBytes((new Integer(sequenceLength)).toString());
            }
        else
            {
                m_outstream.writeBytes((new Integer(previousInt)).toString());
            }
        m_outstream.writeBytes("\");\n");
    }

    /***************************************************************
    Function: emit_driver
    Description:
    **************************************************************/
    private void emit_driver
    (
     )
            throws java.io.IOException
    {
        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != m_spec);
                CUtility.cuassert(null != m_outstream);
            }

        emit_table();

        if (m_spec.m_integer_type)
            {
                m_outstream.writeBytes("\tpublic int ");
                m_outstream.writeBytes(new String(m_spec.m_function_name));
                m_outstream.writeBytes(" ()\n");
            }
        else if (m_spec.m_intwrap_type)
            {
                m_outstream.writeBytes("\tpublic java.lang.Integer ");
                m_outstream.writeBytes(new String(m_spec.m_function_name));
                m_outstream.writeBytes(" ()\n");
            }
        else
            {
                m_outstream.writeBytes("\tpublic ");
                m_outstream.writeBytes(new String(m_spec.m_type_name));
                m_outstream.writeBytes(" ");
                m_outstream.writeBytes(new String(m_spec.m_function_name));
                m_outstream.writeBytes(" ()\n");
            }

        /*m_outstream.writeBytes("\t\tthrows java.io.IOException {\n");*/
        m_outstream.writeBytes("\t\tthrows java.io.IOException");
        if (null != m_spec.m_yylex_throw_code)
            {
                m_outstream.writeBytes(", ");
                m_outstream.writeBytes(new String(m_spec.m_yylex_throw_code,0,
                        m_spec.m_yylex_throw_read));
                m_outstream.writeBytes("\n\t\t{\n");
            }
        else
            {
                m_outstream.writeBytes(" {\n");
            }

        m_outstream.writeBytes("\t\tchar yy_lookahead;\n");
        m_outstream.writeBytes("\t\tint yy_anchor = YY_NO_ANCHOR;\n");
        /*m_outstream.writeBytes("\t\tint yy_state "
          + "= yy_initial_dtrans(yy_lexical_state);\n");*/
        m_outstream.writeBytes("\t\tint yy_state "
                + "= yy_state_dtrans[yy_lexical_state];\n");
        m_outstream.writeBytes("\t\tint yy_next_state = YY_NO_STATE;\n");
        /*m_outstream.writeBytes("\t\tint yy_prev_stave = YY_NO_STATE;\n");*/
        m_outstream.writeBytes("\t\tint yy_last_accept_state = YY_NO_STATE;\n");
        m_outstream.writeBytes("\t\tboolean yy_initial = true;\n");
        m_outstream.writeBytes("\t\tint yy_this_accept;\n");
        m_outstream.writeBytes("\n");

        m_outstream.writeBytes("\t\tyy_mark_start();\n");
        /*m_outstream.writeBytes("\t\tyy_this_accept = yy_accept(yy_state);\n");*/
        m_outstream.writeBytes("\t\tyy_this_accept = yy_acpt[yy_state];\n");
        m_outstream.writeBytes("\t\tif (YY_NOT_ACCEPT != yy_this_accept) {\n");
        m_outstream.writeBytes("\t\t\tyy_last_accept_state = yy_state;\n");
        m_outstream.writeBytes("\t\t\tyy_mark_end();\n");
        m_outstream.writeBytes("\t\t}\n");

        if (NOT_EDBG)
            {
                m_outstream.writeBytes("\t\tjava.lang.System.out.println(\"Begin\");\n");
            }

        m_outstream.writeBytes("\t\twhile (true) {\n");

        m_outstream.writeBytes("\t\t\tyy_lookahead = yy_advance();\n");
        m_outstream.writeBytes("\t\t\tyy_next_state = YY_F;\n");
        m_outstream.writeBytes("\t\t\tif (YY_EOF != yy_lookahead) {\n");
        /*m_outstream.writeBytes("\t\t\t\tyy_next_state = "
          + "yy_next(yy_state,yy_lookahead);\n");*/
        m_outstream.writeBytes("\t\t\t\tyy_next_state = "
                + "yy_nxt[yy_rmap[yy_state]][yy_cmap[yy_lookahead]];\n");

        m_outstream.writeBytes("\t\t\t}\n");

        if (NOT_EDBG)
            {
                m_outstream.writeBytes("java.lang.System.out.println(\"Current state: \""
                        + " + yy_state\n");
                m_outstream.writeBytes("+ \"\tCurrent input: \"\n");
                m_outstream.writeBytes(" + ((char) yy_lookahead));\n");
            }
        if (NOT_EDBG)
            {
                m_outstream.writeBytes("\t\t\tjava.lang.System.out.println(\"State = \""
                        + "+ yy_state);\n");
                m_outstream.writeBytes("\t\t\tjava.lang.System.out.println(\"Accepting status = \""
                        + "+ yy_this_accept);\n");
                m_outstream.writeBytes("\t\t\tjava.lang.System.out.println(\"Last accepting state = \""
                        + "+ yy_last_accept_state);\n");
                m_outstream.writeBytes("\t\t\tjava.lang.System.out.println(\"Next state = \""
                        + "+ yy_next_state);\n");
                m_outstream.writeBytes("\t\t\tjava.lang.System.out.println(\"Lookahead input = \""
                        + "+ ((char) yy_lookahead));\n");
            }

        m_outstream.writeBytes("\t\t\tif (YY_F != yy_next_state) {\n");
        m_outstream.writeBytes("\t\t\t\tyy_state = yy_next_state;\n");
        m_outstream.writeBytes("\t\t\t\tyy_initial = false;\n");
        /*m_outstream.writeBytes("\t\t\t\tyy_this_accept = yy_accept(yy_state);\n");*/
        m_outstream.writeBytes("\t\t\t\tyy_this_accept = yy_acpt[yy_state];\n");
        m_outstream.writeBytes("\t\t\t\tif (YY_NOT_ACCEPT != yy_this_accept) {\n");
        m_outstream.writeBytes("\t\t\t\t\tyy_last_accept_state = yy_state;\n");
        m_outstream.writeBytes("\t\t\t\t\tyy_mark_end();\n");
        m_outstream.writeBytes("\t\t\t\t}\n");
        /*m_outstream.writeBytes("\t\t\t\tyy_prev_state = yy_state;\n");*/
        /*m_outstream.writeBytes("\t\t\t\tyy_state = yy_next_state;\n");*/
        m_outstream.writeBytes("\t\t\t}\n");

        m_outstream.writeBytes("\t\t\telse {\n");
        m_outstream.writeBytes("\t\t\t\tif (YY_EOF == yy_lookahead "
                + "&& true == yy_initial) {\n");
        if (null != m_spec.m_eof_code)
            {
                m_outstream.writeBytes("\t\t\t\t\tyy_do_eof();\n");
            }

        if (m_spec.m_integer_type || true == m_spec.m_yyeof)
            {
                m_outstream.writeBytes("\t\t\t\t\treturn YYEOF;\n");
            }
        else if (null != m_spec.m_eof_value_code)
            {
                m_outstream.writeBytes(new String(m_spec.m_eof_value_code,0,
                        m_spec.m_eof_value_read));
            }
        else
            {
                m_outstream.writeBytes("\t\t\t\t\treturn null;\n");
            }

        m_outstream.writeBytes("\t\t\t\t}\n");

        m_outstream.writeBytes("\t\t\t\telse if (YY_NO_STATE == yy_last_accept_state) {\n");


        /*m_outstream.writeBytes("\t\t\t\t\tyy_error(YY_E_MATCH,false);\n");
          m_outstream.writeBytes("\t\t\t\t\tyy_initial = true;\n");
          m_outstream.writeBytes("\t\t\t\t\tyy_state "
          + "= yy_state_dtrans[yy_lexical_state];\n");
          m_outstream.writeBytes("\t\t\t\t\tyy_next_state = YY_NO_STATE;\n");*/
        /*m_outstream.writeBytes("\t\t\t\t\tyy_prev_state = YY_NO_STATE;\n");*/
        /*m_outstream.writeBytes("\t\t\t\t\tyy_last_accept_state = YY_NO_STATE;\n");
          m_outstream.writeBytes("\t\t\t\t\tyy_mark_start();\n");*/
        /*m_outstream.writeBytes("\t\t\t\t\tyy_this_accept = yy_accept(yy_state);\n");*/
        /*m_outstream.writeBytes("\t\t\t\t\tyy_this_accept = yy_acpt[yy_state];\n");
          m_outstream.writeBytes("\t\t\t\t\tif (YY_NOT_ACCEPT != yy_this_accept) {\n");
          m_outstream.writeBytes("\t\t\t\t\t\tyy_last_accept_state = yy_state;\n");
          m_outstream.writeBytes("\t\t\t\t\t}\n");*/

        m_outstream.writeBytes("\t\t\t\t\tthrow (new Error(\"Lexical Error: Unmatched Input.\"));\n");
        m_outstream.writeBytes("\t\t\t\t}\n");

        m_outstream.writeBytes("\t\t\t\telse {\n");
        m_outstream.writeBytes("\t\t\t\t\tyy_to_mark();\n");

        m_outstream.writeBytes("\t\t\t\t\tyy_anchor = yy_acpt[yy_last_accept_state];\n");
        /*m_outstream.writeBytes("\t\t\t\t\tyy_anchor "
          + "= yy_accept(yy_last_accept_state);\n");*/
        m_outstream.writeBytes("\t\t\t\t\tif (0 != (YY_END & yy_anchor)) {\n");
        m_outstream.writeBytes("\t\t\t\t\t\tyy_pushback();\n");
        m_outstream.writeBytes("\t\t\t\t\t}\n");
        m_outstream.writeBytes("\t\t\t\t\tif (0 != (YY_START & yy_anchor)) {\n");
        m_outstream.writeBytes("\t\t\t\t\t\tyy_move_start();\n");
        m_outstream.writeBytes("\t\t\t\t\t}\n");


        m_outstream.writeBytes("\t\t\t\t\tswitch (yy_last_accept_state) {\n");

        emit_actions("\t\t\t\t\t");

        m_outstream.writeBytes("\t\t\t\t\tdefault:\n");
        m_outstream.writeBytes("\t\t\t\t\t\tyy_error(YY_E_INTERNAL,false);\n");
        /*m_outstream.writeBytes("\t\t\t\t\t\treturn null;\n");*/
        m_outstream.writeBytes("\t\t\t\t\tcase -1:\n");
        m_outstream.writeBytes("\t\t\t\t\t}\n");

        m_outstream.writeBytes("\t\t\t\t\tyy_initial = true;\n");
        m_outstream.writeBytes("\t\t\t\t\tyy_state "
                + "= yy_state_dtrans[yy_lexical_state];\n");
        m_outstream.writeBytes("\t\t\t\t\tyy_next_state = YY_NO_STATE;\n");
        /*m_outstream.writeBytes("\t\t\t\t\tyy_prev_state = YY_NO_STATE;\n");*/
        m_outstream.writeBytes("\t\t\t\t\tyy_last_accept_state = YY_NO_STATE;\n");

        m_outstream.writeBytes("\t\t\t\t\tyy_mark_start();\n");

        /*m_outstream.writeBytes("\t\t\t\t\tyy_this_accept = yy_accept(yy_state);\n");*/
        m_outstream.writeBytes("\t\t\t\t\tyy_this_accept = yy_acpt[yy_state];\n");
        m_outstream.writeBytes("\t\t\t\t\tif (YY_NOT_ACCEPT != yy_this_accept) {\n");
        m_outstream.writeBytes("\t\t\t\t\t\tyy_last_accept_state = yy_state;\n");
        m_outstream.writeBytes("\t\t\t\t\t}\n");

        m_outstream.writeBytes("\t\t\t\t}\n");
        m_outstream.writeBytes("\t\t\t}\n");
        m_outstream.writeBytes("\t\t}\n");
        m_outstream.writeBytes("\t}\n");

        /*m_outstream.writeBytes("\t\t\t\t\n");
          m_outstream.writeBytes("\t\t\t\n");
          m_outstream.writeBytes("\t\t\t\n");
          m_outstream.writeBytes("\t\t\t\n");
          m_outstream.writeBytes("\t\t\t\n");
          m_outstream.writeBytes("\t\t}\n");*/
    }

    /***************************************************************
    Function: emit_actions
    Description:
    **************************************************************/
    private void emit_actions
    (
            String tabs
            )
            throws java.io.IOException
    {
        int elem;
        int size;
        int bogus_index;
        CAccept accept;

        if (CUtility.DEBUG)
            {
                CUtility.cuassert(m_spec.m_accept_vector.size()
                        == m_spec.m_anchor_array.length);
            }

        bogus_index = -2;
        size = m_spec.m_accept_vector.size();
        for (elem = 0; elem < size; ++elem)
            {
                accept = (CAccept) m_spec.m_accept_vector.elementAt(elem);
                if (null != accept)
                    {
                        m_outstream.writeBytes(tabs + "case " + elem
                                /*+ (new Integer(elem)).toString()*/
                                + ":\n");
                        m_outstream.writeBytes(tabs + "\t");
                        m_outstream.writeBytes(new String(accept.m_action,0,
                                accept.m_action_read));
                        m_outstream.writeBytes("\n");
                        m_outstream.writeBytes(tabs + "case " + bogus_index + ":\n");
                        m_outstream.writeBytes(tabs + "\tbreak;\n");
                        --bogus_index;
                    }
            }
    }

    /***************************************************************
    Function: emit_footer
    Description:
    **************************************************************/
    private void emit_footer
    (
     )
            throws java.io.IOException
    {
        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != m_spec);
                CUtility.cuassert(null != m_outstream);
            }

        m_outstream.writeBytes("}\n");
    }
}

/***************************************************************
  Class: CBunch
  **************************************************************/
class CBunch
{
    /***************************************************************
                                                                    Member Variables
    **************************************************************/
    Vector m_nfa_set; /* Vector of CNfa states in dfa state. */
    JavaLexBitSet m_nfa_bit; /* BitSet representation of CNfa labels. */
    CAccept m_accept; /* Accepting actions, or null if nonaccepting state. */
    int m_anchor; /* Anchors on regular expression. */
    int m_accept_index; /* CNfa index corresponding to accepting actions. */

    /***************************************************************
    Function: CBunch
    Description: Constructor.
    **************************************************************/
    CBunch
    (
     )
    {
        m_nfa_set = null;
        m_nfa_bit = null;
        m_accept = null;
        m_anchor = CSpec.NONE;
        m_accept_index = -1;
    }
}

/***************************************************************
  Class: CMakeNfa
  **************************************************************/
class CMakeNfa
{
    /***************************************************************
                                                                    Member Variables
    **************************************************************/
    private CSpec m_spec;
    private CLexGen m_lexGen;
    private CInput m_input;

    /***************************************************************
    Function: CMakeNfa
    Description: Constructor.
    **************************************************************/
    CMakeNfa
    (
     )
    {
        reset();
    }

    /***************************************************************
    Function: reset
    Description: Resets CMakeNfa member variables.
    **************************************************************/
    private void reset
    (
     )
    {
        m_input = null;
        m_lexGen = null;
        m_spec = null;
    }

    /***************************************************************
    Function: set
    Description: Sets CMakeNfa member variables.
    **************************************************************/
    private void set
    (
            CLexGen lexGen,
            CSpec spec,
            CInput input
            )
    {
        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != input);
                CUtility.cuassert(null != lexGen);
                CUtility.cuassert(null != spec);
            }

        m_input = input;
        m_lexGen = lexGen;
        m_spec = spec;
    }

    /***************************************************************
    Function: thompson
    Description: High level access function to module.
    Deposits result in input CSpec.
    **************************************************************/
    void thompson
    (
            CLexGen lexGen,
            CSpec spec,
            CInput input
            )
            throws java.io.IOException
    {
        int i;
        CNfa elem;
        int size;

        /* Set member variables. */
        reset();
        set(lexGen,spec,input);

        size = m_spec.m_states.size();
        m_spec.m_state_rules = new Vector[size];
        for (i = 0; i < size; ++i)
            {
                m_spec.m_state_rules[i] = new Vector();
            }

        /* Initialize current token variable
           and create nfa. */
        /*m_spec.m_current_token = CLexGen.EOS;
          m_lexGen.advance();*/

        m_spec.m_nfa_start = machine();

        /* Set labels in created nfa machine. */
        size = m_spec.m_nfa_states.size();
        for (i = 0; i < size; ++i)
            {
                elem = (CNfa) m_spec.m_nfa_states.elementAt(i);
                elem.m_label = i;
            }

        /* Debugging output. */
        if (CUtility.DO_DEBUG)
            {
                m_lexGen.print_nfa();
            }

        if (m_spec.m_verbose)
            {
                System.out.println("NFA comprised of "
                        + (m_spec.m_nfa_states.size() + 1)
                        + " states.");
            }

        reset();
    }

    /***************************************************************
    Function: discardCNfa
    Description:
    **************************************************************/
    private void discardCNfa
    (
            CNfa nfa
            )
    {
        m_spec.m_nfa_states.removeElement(nfa);
    }

    /***************************************************************
    Function: processStates
    Description:
    **************************************************************/
    private void processStates
    (
            JavaLexBitSet states,
            CNfa current
            )
    {
        int size;
        int i;

        size = m_spec.m_states.size();
        for (i = 0; i <  size; ++i)
            {
                if (states.get(i))
                    {
                        m_spec.m_state_rules[i].addElement(current);
                    }
            }
    }

    /***************************************************************
    Function: machine
    Description: Recursive descent regular expression parser.
    **************************************************************/
    private CNfa machine
    (
     )
            throws java.io.IOException
    {
        CNfa start;
        CNfa p;
        JavaLexBitSet states;

        if (CUtility.DESCENT_DEBUG)
            {
                CUtility.enter("machine",m_spec.m_lexeme,m_spec.m_current_token);
            }

        states = m_lexGen.getStates();

        /* Begin: Added for states. */
        m_spec.m_current_token = CLexGen.EOS;
        m_lexGen.advance();
        /* End: Added for states. */

        start = CAlloc.newCNfa(m_spec);
        p = start;
        p.m_next = rule();

        processStates(states,p.m_next);

        while (CLexGen.END_OF_INPUT != m_spec.m_current_token)
            {
                /* Make state changes HERE. */
                states = m_lexGen.getStates();

                /* Begin: Added for states. */
                m_lexGen.advance();
                if (CLexGen.END_OF_INPUT == m_spec.m_current_token)
                    {
                        break;
                    }
                /* End: Added for states. */

                p.m_next2 = CAlloc.newCNfa(m_spec);
                p = p.m_next2;
                p.m_next = rule();

                processStates(states,p.m_next);
            }

        if (CUtility.DESCENT_DEBUG)
            {
                CUtility.leave("machine",m_spec.m_lexeme,m_spec.m_current_token);
            }

        return start;
    }

    /***************************************************************
    Function: rule
    Description: Recursive descent regular expression parser.
    **************************************************************/
    private CNfa rule
    (
     )
            throws java.io.IOException
    {
        CNfaPair pair;
        CNfa start = null;
        CNfa end = null;
        int anchor = CSpec.NONE;

        if (CUtility.DESCENT_DEBUG)
            {
                CUtility.enter("rule",m_spec.m_lexeme,m_spec.m_current_token);
            }

        pair = CAlloc.newCNfaPair();

        if (CLexGen.AT_BOL == m_spec.m_current_token)
            {
                start = CAlloc.newCNfa(m_spec);
                start.m_edge = '\n';
                anchor = anchor | CSpec.START;
                m_lexGen.advance();

                expr(pair);
                start.m_next = pair.m_start;
                end = pair.m_end;
            }
        else
            {
                expr(pair);
                start = pair.m_start;
                end = pair.m_end;
            }

        if (CLexGen.AT_EOL == m_spec.m_current_token)
            {
                m_lexGen.advance();
                end.m_next = CAlloc.newCNfa(m_spec);

                /* This is the way he does it. */
                end.m_edge = CNfa.CCL;
                end.m_set = new CSet();

                end.m_set.add('\n');

                if (false == m_spec.m_unix)
                    {
                        end.m_set.add('\r');
                    }

                end = end.m_next;
                anchor = anchor | CSpec.END;
            }

        /* Handle end of regular expression.  See page 103. */
        end.m_accept = m_lexGen.packAccept();
        end.m_anchor = anchor;

        /* Begin: Removed for states. */
        /*m_lexGen.advance();*/
        /* End: Removed for states. */

        if (CUtility.DESCENT_DEBUG)
            {
                CUtility.leave("rule",m_spec.m_lexeme,m_spec.m_current_token);
            }

        return start;
    }

    /***************************************************************
    Function: expr
    Description: Recursive descent regular expression parser.
    **************************************************************/
    private void expr
    (
            CNfaPair pair
            )
            throws java.io.IOException
    {
        CNfaPair e2_pair;
        CNfa p;

        if (CUtility.DESCENT_DEBUG)
            {
                CUtility.enter("expr",m_spec.m_lexeme,m_spec.m_current_token);
            }

        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != pair);
            }

        e2_pair = CAlloc.newCNfaPair();

        cat_expr(pair);

        while (CLexGen.OR == m_spec.m_current_token)
            {
                m_lexGen.advance();
                cat_expr(e2_pair);

                p = CAlloc.newCNfa(m_spec);
                p.m_next2 = e2_pair.m_start;
                p.m_next = pair.m_start;
                pair.m_start = p;

                p = CAlloc.newCNfa(m_spec);
                pair.m_end.m_next = p;
                e2_pair.m_end.m_next = p;
                pair.m_end = p;
            }

        if (CUtility.DESCENT_DEBUG)
            {
                CUtility.leave("expr",m_spec.m_lexeme,m_spec.m_current_token);
            }
    }

    /***************************************************************
    Function: cat_expr
    Description: Recursive descent regular expression parser.
    **************************************************************/
    private void cat_expr
    (
            CNfaPair pair
            )
            throws java.io.IOException
    {
        CNfaPair e2_pair;

        if (CUtility.DESCENT_DEBUG)
            {
                CUtility.enter("cat_expr",m_spec.m_lexeme,m_spec.m_current_token);
            }

        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != pair);
            }

        e2_pair = CAlloc.newCNfaPair();

        if (first_in_cat(m_spec.m_current_token))
            {
                factor(pair);
            }

        while (first_in_cat(m_spec.m_current_token))
            {
                factor(e2_pair);

                /* Destroy */
                pair.m_end.mimic(e2_pair.m_start);
                discardCNfa(e2_pair.m_start);

                pair.m_end = e2_pair.m_end;
            }

        if (CUtility.DESCENT_DEBUG)
            {
                CUtility.leave("cat_expr",m_spec.m_lexeme,m_spec.m_current_token);
            }
    }

    /***************************************************************
    Function: first_in_cat
    Description: Recursive descent regular expression parser.
    **************************************************************/
    private boolean first_in_cat
    (
            int token
            )
    {
        switch (token)
            {
            case CLexGen.CLOSE_PAREN:
            case CLexGen.AT_EOL:
            case CLexGen.OR:
            case CLexGen.EOS:
                return false;

            case CLexGen.CLOSURE:
            case CLexGen.PLUS_CLOSE:
            case CLexGen.OPTIONAL:
                CError.parse_error(CError.E_CLOSE,m_input.m_line_number);
                return false;

            case CLexGen.CCL_END:
                CError.parse_error(CError.E_BRACKET,m_input.m_line_number);
                return false;

            case CLexGen.AT_BOL:
                CError.parse_error(CError.E_BOL,m_input.m_line_number);
                return false;

            default:
                break;
            }

        return true;
    }

    /***************************************************************
    Function: factor
    Description: Recursive descent regular expression parser.
    **************************************************************/
    private void factor
    (
            CNfaPair pair
            )
            throws java.io.IOException
    {
        CNfa start = null;
        CNfa end = null;

        if (CUtility.DESCENT_DEBUG)
            {
                CUtility.enter("factor",m_spec.m_lexeme,m_spec.m_current_token);
            }

        term(pair);

        if (CLexGen.CLOSURE == m_spec.m_current_token
                || CLexGen.PLUS_CLOSE == m_spec.m_current_token
                || CLexGen.OPTIONAL == m_spec.m_current_token)
            {
                start = CAlloc.newCNfa(m_spec);
                end = CAlloc.newCNfa(m_spec);

                start.m_next = pair.m_start;
                pair.m_end.m_next = end;

                if (CLexGen.CLOSURE == m_spec.m_current_token
                        || CLexGen.OPTIONAL == m_spec.m_current_token)
                    {
                        start.m_next2 = end;
                    }

                if (CLexGen.CLOSURE == m_spec.m_current_token
                        || CLexGen.PLUS_CLOSE == m_spec.m_current_token)
                    {
                        pair.m_end.m_next2 = pair.m_start;
                    }

                pair.m_start = start;
                pair.m_end = end;
                m_lexGen.advance();
            }

        if (CUtility.DESCENT_DEBUG)
            {
                CUtility.leave("factor",m_spec.m_lexeme,m_spec.m_current_token);
            }
    }

    /***************************************************************
    Function: term
    Description: Recursive descent regular expression parser.
    **************************************************************/
    private void term
    (
            CNfaPair pair
            )
            throws java.io.IOException
    {
        CNfa start;
        boolean isAlphaL;
      
        if (CUtility.DESCENT_DEBUG)
            {
                CUtility.enter("term",m_spec.m_lexeme,m_spec.m_current_token);
            }

        if (CLexGen.OPEN_PAREN == m_spec.m_current_token)
            {
                m_lexGen.advance();
                expr(pair);

                if (CLexGen.CLOSE_PAREN == m_spec.m_current_token)
                    {
                        m_lexGen.advance();
                    }
                else
                    {
                        CError.parse_error(CError.E_SYNTAX,m_input.m_line_number);
                    }
            }
        else
            {
                start = CAlloc.newCNfa(m_spec);
                pair.m_start = start;

                start.m_next = CAlloc.newCNfa(m_spec);
                pair.m_end = start.m_next;

                if (CLexGen.L == m_spec.m_current_token &&
                        Character.isLetter(m_spec.m_lexeme))
                    {
                        isAlphaL = true;
                    }
                else
                    {
                        isAlphaL = false;
                    }
                if (false == (CLexGen.ANY == m_spec.m_current_token
                        || CLexGen.CCL_START == m_spec.m_current_token
                        || (m_spec.m_ignorecase && isAlphaL)))
                    {
                        start.m_edge = m_spec.m_lexeme;
                        m_lexGen.advance();
                    }
                else
                    {
                        start.m_edge = CNfa.CCL;

                        start.m_set = new CSet();

                        /* Match case-insensitive letters using character class. */
                        if (m_spec.m_ignorecase && isAlphaL)
                            {
                                start.m_set.addncase(m_spec.m_lexeme);
                            }
                        /* Match dot (.) using character class. */
                        else if (CLexGen.ANY == m_spec.m_current_token)
                            {
                                start.m_set.add((byte) '\n');
                                if (false == m_spec.m_unix)
                                    {
                                        start.m_set.add((byte) '\r');
                                    }
                                start.m_set.complement();
                            }
                        else
                            {
                                m_lexGen.advance();
                                if (CLexGen.AT_BOL == m_spec.m_current_token)
                                    {
                                        m_lexGen.advance();

                                        /*start.m_set.add((byte) '\n');
                                          if (false == m_spec.m_unix)
                                          {
                                          start.m_set.add((byte) '\r');
                                          }*/
                                        start.m_set.complement();
                                    }
                                if (false == (CLexGen.CCL_END == m_spec.m_current_token))
                                    {
                                        dodash(start.m_set);
                                    }
                                /*else
                                  {
                                  for (c = 0; c <= ' '; ++c)
                                  {
                                  start.m_set.add((byte) c);
                                  }
                                  }*/
                            }
                        m_lexGen.advance();
                    }
            }

        if (CUtility.DESCENT_DEBUG)
            {
                CUtility.leave("term",m_spec.m_lexeme,m_spec.m_current_token);
            }
    }

    /***************************************************************
    Function: dodash
    Description: Recursive descent regular expression parser.
    **************************************************************/
    private void dodash
    (
            CSet set
            )
            throws java.io.IOException
    {
        int first = -1;

        if (CUtility.DESCENT_DEBUG)
            {
                CUtility.enter("dodash",m_spec.m_lexeme,m_spec.m_current_token);
            }

        while (CLexGen.EOS != m_spec.m_current_token
                && CLexGen.CCL_END != m_spec.m_current_token)
            {
                // DASH loses its special meaning if it is first in class.
                if (CLexGen.DASH == m_spec.m_current_token && -1 != first)
                    {
                        m_lexGen.advance();
                        // DASH loses its special meaning if it is last in class.
                        if (m_spec.m_current_token == CLexGen.CCL_END)
                            {
                                // 'first' already in set.
                                set.add('-');
                                break;
                            }
                        for ( ; first <= m_spec.m_lexeme; ++first)
                            {
                                if (m_spec.m_ignorecase)
                                    set.addncase((char)first);
                                else
                                    set.add(first);
                            }
                    }
                else
                    {
                        first = m_spec.m_lexeme;
                        if (m_spec.m_ignorecase)
                            set.addncase(m_spec.m_lexeme);
                        else
                            set.add(m_spec.m_lexeme);
                    }

                m_lexGen.advance();
            }

        if (CUtility.DESCENT_DEBUG)
            {
                CUtility.leave("dodash",m_spec.m_lexeme,m_spec.m_current_token);
            }
    }
}

/***************************************************************
  Class: CMinimize
 **************************************************************/
class CMinimize
{
    /***************************************************************
                                                                    Member Variables
    **************************************************************/
    CSpec m_spec;
    Vector m_group;
    int m_ingroup[];

    /***************************************************************
    Function: CMinimize
    Description: Constructor.
    **************************************************************/
    CMinimize
    (
     )
    {
        reset();
    }

    /***************************************************************
    Function: reset
    Description: Resets member variables.
    **************************************************************/
    private void reset
    (
     )
    {
        m_spec = null;
        m_group = null;
        m_ingroup = null;
    }

    /***************************************************************
    Function: set
    Description: Sets member variables.
    **************************************************************/
    private void set
    (
            CSpec spec
            )
    {
        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != spec);
            }

        m_spec = spec;
        m_group = null;
        m_ingroup = null;
    }

    /***************************************************************
    Function: min_dfa
    Description: High-level access function to module.
    **************************************************************/
    void min_dfa
    (
            CSpec spec
            )
    {
        set(spec);

        /* Remove redundant states. */
        minimize();

        /* Column and row compression.
           Save accept states in auxiliary vector. */
        reduce();

        reset();
    }

    /***************************************************************
    Function: col_copy
    Description: Copies source column into destination column.
    **************************************************************/
    private void col_copy
    (
            int dest,
            int src
            )
    {
        int n;
        int i;
        CDTrans dtrans;

        n = m_spec.m_dtrans_vector.size();
        for (i = 0; i < n; ++i)
            {
                dtrans = (CDTrans) m_spec.m_dtrans_vector.elementAt(i);
                dtrans.m_dtrans[dest] = dtrans.m_dtrans[src];
            }
    }

    /***************************************************************
    Function: row_copy
    Description: Copies source row into destination row.
    **************************************************************/
    private void row_copy
    (
            int dest,
            int src
            )
    {
        CDTrans dtrans;

        dtrans = (CDTrans) m_spec.m_dtrans_vector.elementAt(src);
        m_spec.m_dtrans_vector.setElementAt(dtrans,dest);
    }

    /***************************************************************
    Function: col_equiv
    Description:
    **************************************************************/
    private boolean col_equiv
    (
            int col1,
            int col2
            )
    {
        int n;
        int i;
        CDTrans dtrans;

        n = m_spec.m_dtrans_vector.size();
        for (i = 0; i < n; ++i)
            {
                dtrans = (CDTrans) m_spec.m_dtrans_vector.elementAt(i);
                if (dtrans.m_dtrans[col1] != dtrans.m_dtrans[col2])
                    {
                        return false;
                    }
            }

        return true;
    }

    /***************************************************************
    Function: row_equiv
    Description:
    **************************************************************/
    private boolean row_equiv
    (
            int row1,
            int row2
            )
    {
        int i;
        CDTrans dtrans1;
        CDTrans dtrans2;

        dtrans1 = (CDTrans) m_spec.m_dtrans_vector.elementAt(row1);
        dtrans2 = (CDTrans) m_spec.m_dtrans_vector.elementAt(row2);

        for (i = 0; i < m_spec.m_dtrans_ncols; ++i)
            {
                if (dtrans1.m_dtrans[i] != dtrans2.m_dtrans[i])
                    {
                        return false;
                    }
            }

        return true;
    }

    /***************************************************************
    Function: reduce
    Description:
    **************************************************************/
    private void reduce
    (
     )
    {
        int i;
        int j;
        int k;
        int nrows;
        int reduced_ncols;
        int reduced_nrows;
        JavaLexBitSet set;
        CDTrans dtrans;
        int size;

        set = new JavaLexBitSet();

        /* Save accept nodes and anchor entries. */
        size = m_spec.m_dtrans_vector.size();
        m_spec.m_anchor_array = new int[size];
        m_spec.m_accept_vector = new Vector();
        for (i = 0; i < size; ++i)
            {
                dtrans = (CDTrans) m_spec.m_dtrans_vector.elementAt(i);
                m_spec.m_accept_vector.addElement(dtrans.m_accept);
                m_spec.m_anchor_array[i] = dtrans.m_anchor;
                dtrans.m_accept = null;
            }

        /* Allocate column map. */
        m_spec.m_col_map = new int[m_spec.m_dtrans_ncols];
        for (i = 0; i < m_spec.m_dtrans_ncols; ++i)
            {
                m_spec.m_col_map[i] = -1;
            }

        /* Process columns for reduction. */
        for (reduced_ncols = 0; ; ++reduced_ncols)
            {
                if (CUtility.DEBUG)
                    {
                        for (i = 0; i < reduced_ncols; ++i)
                            {
                                CUtility.cuassert(-1 != m_spec.m_col_map[i]);
                            }
                    }

                for (i = reduced_ncols; i < m_spec.m_dtrans_ncols; ++i)
                    {
                        if (-1 == m_spec.m_col_map[i])
                            {
                                break;
                            }
                    }

                if (i >= m_spec.m_dtrans_ncols)
                    {
                        break;
                    }

                if (CUtility.DEBUG)
                    {
                        CUtility.cuassert(false == set.get(i));
                        CUtility.cuassert(-1 == m_spec.m_col_map[i]);
                    }

                set.set(i);

                m_spec.m_col_map[i] = reduced_ncols;

                /* UNDONE: Optimize by doing all comparisons in one batch. */
                for (j = i + 1; j < m_spec.m_dtrans_ncols; ++j)
                    {
                        if (-1 == m_spec.m_col_map[j] && true == col_equiv(i,j))
                            {
                                m_spec.m_col_map[j] = reduced_ncols;
                            }
                    }
            }

        /* Reduce columns. */
        k = 0;
        for (i = 0; i < m_spec.m_dtrans_ncols; ++i)
            {
                if (set.get(i))
                    {
                        ++k;

                        set.clear(i);

                        j = m_spec.m_col_map[i];

                        if (CUtility.DEBUG)
                            {
                                CUtility.cuassert(j <= i);
                            }

                        if (j == i)
                            {
                                continue;
                            }

                        col_copy(j,i);
                    }
            }
        m_spec.m_dtrans_ncols = reduced_ncols;

        if (CUtility.DEBUG)
            {
                CUtility.cuassert(k == reduced_ncols);
            }

        /* Allocate row map. */
        nrows = m_spec.m_dtrans_vector.size();
        m_spec.m_row_map = new int[nrows];
        for (i = 0; i < nrows; ++i)
            {
                m_spec.m_row_map[i] = -1;
            }

        /* Process rows to reduce. */
        for (reduced_nrows = 0; ; ++reduced_nrows)
            {
                if (CUtility.DEBUG)
                    {
                        for (i = 0; i < reduced_nrows; ++i)
                            {
                                CUtility.cuassert(-1 != m_spec.m_row_map[i]);
                            }
                    }

                for (i = reduced_nrows; i < nrows; ++i)
                    {
                        if (-1 == m_spec.m_row_map[i])
                            {
                                break;
                            }
                    }

                if (i >= nrows)
                    {
                        break;
                    }

                if (CUtility.DEBUG)
                    {
                        CUtility.cuassert(false == set.get(i));
                        CUtility.cuassert(-1 == m_spec.m_row_map[i]);
                    }

                set.set(i);

                m_spec.m_row_map[i] = reduced_nrows;

                /* UNDONE: Optimize by doing all comparisons in one batch. */
                for (j = i + 1; j < nrows; ++j)
                    {
                        if (-1 == m_spec.m_row_map[j] && true == row_equiv(i,j))
                            {
                                m_spec.m_row_map[j] = reduced_nrows;
                            }
                    }
            }

        /* Reduce rows. */
        k = 0;
        for (i = 0; i < nrows; ++i)
            {
                if (set.get(i))
                    {
                        ++k;

                        set.clear(i);

                        j = m_spec.m_row_map[i];

                        if (CUtility.DEBUG)
                            {
                                CUtility.cuassert(j <= i);
                            }

                        if (j == i)
                            {
                                continue;
                            }

                        row_copy(j,i);
                    }
            }
        m_spec.m_dtrans_vector.setSize(reduced_nrows);

        if (CUtility.DEBUG)
            {
                /*System.out.print("k = " + k + "\nreduced_nrows = " + reduced_nrows + "\n");*/
                CUtility.cuassert(k == reduced_nrows);
            }
    }

    /***************************************************************
    Function: fix_dtrans
    Description: Updates CDTrans table after minimization
    using groups, removing redundant transition table states.
    **************************************************************/
    private void fix_dtrans
    (
     )
    {
        Vector new_vector;
        int i;
        int size;
        Vector dtrans_group;
        CDTrans first;
        int c;

        new_vector = new Vector();

        size = m_spec.m_state_dtrans.length;
        for (i = 0; i < size; ++i)
            {
                if (CDTrans.F != m_spec.m_state_dtrans[i])
                    {
                        m_spec.m_state_dtrans[i] = m_ingroup[m_spec.m_state_dtrans[i]];
                    }
            }

        size = m_group.size();
        for (i = 0; i < size; ++i)
            {
                dtrans_group = (Vector) m_group.elementAt(i);
                first = (CDTrans) dtrans_group.elementAt(0);
                new_vector.addElement(first);

                for (c = 0; c < m_spec.m_dtrans_ncols; ++c)
                    {
                        if (CDTrans.F != first.m_dtrans[c])
                            {
                                first.m_dtrans[c] = m_ingroup[first.m_dtrans[c]];
                            }
                    }
            }

        m_group = null;
        m_spec.m_dtrans_vector = new_vector;
    }

    /***************************************************************
    Function: minimize
    Description: Removes redundant transition table states.
    **************************************************************/
    private void minimize
    (
     )
    {
        Vector dtrans_group;
        Vector new_group;
        int i;
        int j;
        int old_group_count;
        int group_count;
        CDTrans next;
        CDTrans first;
        int goto_first;
        int goto_next;
        int c;
        int group_size;
        boolean added;

        init_groups();

        group_count = m_group.size();
        old_group_count = group_count - 1;

        while (old_group_count != group_count)
            {
                old_group_count = group_count;

                if (CUtility.DEBUG)
                    {
                        CUtility.cuassert(m_group.size() == group_count);
                    }

                for (i = 0; i < group_count; ++i)
                    {
                        dtrans_group = (Vector) m_group.elementAt(i);

                        group_size = dtrans_group.size();
                        if (group_size <= 1)
                            {
                                continue;
                            }

                        new_group = new Vector();
                        added = false;

                        first = (CDTrans) dtrans_group.elementAt(0);
                        for (j = 1; j < group_size; ++j)
                            {
                                next = (CDTrans) dtrans_group.elementAt(j);

                                for (c = 0; c < m_spec.m_dtrans_ncols; ++c)
                                    {
                                        goto_first = first.m_dtrans[c];
                                        goto_next = next.m_dtrans[c];

                                        if (goto_first != goto_next
                                                && (goto_first == CDTrans.F
                                                        || goto_next == CDTrans.F
                                                        || m_ingroup[goto_next] != m_ingroup[goto_first]))
                                            {
                                                if (CUtility.DEBUG)
                                                    {
                                                        CUtility.cuassert(dtrans_group.elementAt(j) == next);
                                                    }

                                                dtrans_group.removeElementAt(j);
                                                --j;
                                                --group_size;
                                                new_group.addElement(next);
                                                if (false == added)
                                                    {
                                                        added = true;
                                                        ++group_count;
                                                        m_group.addElement(new_group);
                                                    }
                                                m_ingroup[next.m_label] = m_group.size() - 1;

                                                if (CUtility.DEBUG)
                                                    {
                                                        CUtility.cuassert(m_group.contains(new_group)
                                                                == true);
                                                        CUtility.cuassert(m_group.contains(dtrans_group)
                                                                == true);
                                                        CUtility.cuassert(dtrans_group.contains(first)
                                                                == true);
                                                        CUtility.cuassert(dtrans_group.contains(next)
                                                                == false);
                                                        CUtility.cuassert(new_group.contains(first)
                                                            == false);
                                                        CUtility.cuassert(new_group.contains(next)
                                                            == true);
                                                        CUtility.cuassert(dtrans_group.size() == group_size);
                                                        CUtility.cuassert(i == m_ingroup[first.m_label]);
                                                        CUtility.cuassert((m_group.size() - 1)
                                                                == m_ingroup[next.m_label]);
                                                    }

                                                break;
                                            }
                                    }
                            }
                    }
            }

        System.out.println(m_group.size() + " states after removal of redundant states.");

        if (m_spec.m_verbose
                && true == CUtility.OLD_DUMP_DEBUG)
            {
                System.out.println("\nStates grouped as follows after minimization");
                pgroups();
            }

        fix_dtrans();
    }

    /***************************************************************
    Function: init_groups
    Description:
    **************************************************************/
    private void init_groups
    (
     )
    {
        int i;
        int j;
        int group_count;
        int size;
        CDTrans dtrans;
        Vector dtrans_group;
        CDTrans first;
        boolean group_found;

        m_group = new Vector();
        group_count = 0;

        size = m_spec.m_dtrans_vector.size();
        m_ingroup = new int[size];

        for (i = 0; i < size; ++i)
            {
                group_found = false;
                dtrans = (CDTrans) m_spec.m_dtrans_vector.elementAt(i);

                if (CUtility.DEBUG)
                    {
                        CUtility.cuassert(i == dtrans.m_label);
                        CUtility.cuassert(false == group_found);
                        CUtility.cuassert(group_count == m_group.size());
                    }

                for (j = 0; j < group_count; ++j)
                    {
                        dtrans_group = (Vector) m_group.elementAt(j);

                        if (CUtility.DEBUG)
                            {
                                CUtility.cuassert(false == group_found);
                                CUtility.cuassert(0 < dtrans_group.size());
                            }

                        first = (CDTrans) dtrans_group.elementAt(0);

                        if (CUtility.SLOW_DEBUG)
                            {
                                CDTrans check;
                                int k;
                                int s;

                                s = dtrans_group.size();
                                CUtility.cuassert(0 < s);

                                for (k = 1; k < s; ++k)
                                    {
                                        check = (CDTrans) dtrans_group.elementAt(k);
                                        CUtility.cuassert(check.m_accept == first.m_accept);
                                    }
                            }

                        if (first.m_accept == dtrans.m_accept)
                            {
                                dtrans_group.addElement(dtrans);
                                m_ingroup[i] = j;
                                group_found = true;

                                if (CUtility.DEBUG)
                                    {
                                        CUtility.cuassert(j == m_ingroup[dtrans.m_label]);
                                    }

                                break;
                            }
                    }

                if (false == group_found)
                    {
                        dtrans_group = new Vector();
                        dtrans_group.addElement(dtrans);
                        m_ingroup[i] = m_group.size();
                        m_group.addElement(dtrans_group);
                        ++group_count;
                    }
            }

        if (m_spec.m_verbose
                && true == CUtility.OLD_DUMP_DEBUG)
            {
                System.out.println("Initial grouping:");
                pgroups();
                System.out.println("");
            }
    }

    /***************************************************************
    Function: pset
    **************************************************************/
    private void pset
    (
            Vector dtrans_group
            )
    {
        int i;
        int size;
        CDTrans dtrans;

        size = dtrans_group.size();
        for (i = 0; i < size; ++i)
            {
                dtrans = (CDTrans) dtrans_group.elementAt(i);
                System.out.print(dtrans.m_label + " ");
            }
    }

    /***************************************************************
    Function: pgroups
    **************************************************************/
    private void pgroups
    (
     )
    {
        int i;
        int dtrans_size;
        int group_size;

        group_size = m_group.size();
        for (i = 0; i < group_size; ++i)
            {
                System.out.print("\tGroup " + i + " {");
                pset((Vector) m_group.elementAt(i));
                System.out.println("}\n");
            }

        System.out.println("");
        dtrans_size = m_spec.m_dtrans_vector.size();
        for (i = 0; i < dtrans_size; ++i)
            {
                System.out.println("\tstate " + i
                        + " is in group "
                        + m_ingroup[i]);
            }
    }
}

/***************************************************************
  Class: CNfa2Dfa
 **************************************************************/
class CNfa2Dfa
{
    /***************************************************************
                                                                    Member Variables
    **************************************************************/
    private CSpec m_spec;
    private int m_unmarked_dfa;
    private CLexGen m_lexGen;

    /***************************************************************
    Constants
    **************************************************************/
    private static final int NOT_IN_DSTATES = -1;

    /***************************************************************
    Function: CNfa2Dfa
    **************************************************************/
    CNfa2Dfa
    (
     )
    {
        reset();
    }

    /***************************************************************
    Function: set
    Description:
    **************************************************************/
    private void set
    (
            CLexGen lexGen,
            CSpec spec
            )
    {
        m_lexGen = lexGen;
        m_spec = spec;
        m_unmarked_dfa = 0;
    }

    /***************************************************************
    Function: reset
    Description:
    **************************************************************/
    private void reset
    (
     )
    {
        m_lexGen = null;
        m_spec = null;
        m_unmarked_dfa = 0;
    }

    /***************************************************************
    Function: make_dfa
    Description: High-level access function to module.
    **************************************************************/
    void make_dfa
    (
            CLexGen lexGen,
            CSpec spec
            )
    {

        reset();
        set(lexGen,spec);

        make_dtrans();
        free_nfa_states();

        if (m_spec.m_verbose && true == CUtility.OLD_DUMP_DEBUG)
            {
                System.out.println(m_spec.m_dfa_states.size()
                        + " DFA states in original machine.");
            }

        free_dfa_states();
    }

    /***************************************************************
    Function: make_dtrans
    Description: Creates uncompressed CDTrans transition table.
    **************************************************************/
    private void make_dtrans
    (
     )
            /* throws java.lang.CloneNotSupportedException*/
    {
        CDfa dfa;
        CBunch bunch;
        int i;
        int nextstate;
        int size;
        CDTrans dtrans;
        CNfa nfa;
        int istate;
        int nstates;

        System.out.print("Working on DFA states.");

        /* Reference passing type and initializations. */
        bunch = new CBunch();
        m_unmarked_dfa = 0;

        /* Allocate mapping array. */
        nstates = m_spec.m_state_rules.length;
        m_spec.m_state_dtrans = new int[nstates];

        for (istate = 0; nstates > istate; ++istate)
            {
                if (0 == m_spec.m_state_rules[istate].size())
                    {
                        m_spec.m_state_dtrans[istate] = CDTrans.F;
                        continue;
                    }

                /* Create start state and initialize fields. */
                bunch.m_nfa_set = (Vector) m_spec.m_state_rules[istate].clone();
                sortStates(bunch.m_nfa_set);

                bunch.m_nfa_bit = new JavaLexBitSet();

                /* Initialize bit set. */
                size = bunch.m_nfa_set.size();
                for (i = 0; size > i; ++i)
                    {
                        nfa = (CNfa) bunch.m_nfa_set.elementAt(i);
                        bunch.m_nfa_bit.set(nfa.m_label);
                    }

                bunch.m_accept = null;
                bunch.m_anchor = CSpec.NONE;
                bunch.m_accept_index = CUtility.INT_MAX;

                e_closure(bunch);
                add_to_dstates(bunch);

                m_spec.m_state_dtrans[istate] = m_spec.m_dtrans_vector.size();

                /* Main loop of CDTrans creation. */
                while (null != (dfa = get_unmarked()))
                    {
                        System.out.print(".");
                        System.out.flush();

                        if (CUtility.DEBUG)
                            {
                                CUtility.cuassert(false == dfa.m_mark);
                            }

                        /* Get first unmarked node, then mark it. */
                        dfa.m_mark = true;

                        /* Allocate new CDTrans, then initialize fields. */
                        dtrans = new CDTrans(m_spec.m_dtrans_vector.size(),m_spec);
                        dtrans.m_accept = dfa.m_accept;
                        dtrans.m_anchor = dfa.m_anchor;

                        /* Set CDTrans array for each character transition. */
                        for (i = 0; i < m_spec.m_dtrans_ncols; ++i)
                            {
                                if (CUtility.DEBUG)
                                    {
                                        CUtility.cuassert(0 <= i);
                                        CUtility.cuassert(m_spec.m_dtrans_ncols > i);
                                    }

                                /* Create new dfa set by attempting character transition. */
                                move(dfa.m_nfa_set,dfa.m_nfa_bit,i,bunch);
                                if (null != bunch.m_nfa_set)
                                    {
                                        e_closure(bunch);
                                    }

                                if (CUtility.DEBUG)
                                    {
                                        CUtility.cuassert((null == bunch.m_nfa_set
                                                && null == bunch.m_nfa_bit)
                                                || (null != bunch.m_nfa_set
                                                        && null != bunch.m_nfa_bit));
                                    }

                                /* Create new state or set state to empty. */
                                if (null == bunch.m_nfa_set)
                                    {
                                        nextstate = CDTrans.F;
                                    }
                                else
                                    {
                                        nextstate = in_dstates(bunch);

                                        if (NOT_IN_DSTATES == nextstate)
                                            {
                                                nextstate = add_to_dstates(bunch);
                                            }
                                    }

                                if (CUtility.DEBUG)
                                    {
                                        CUtility.cuassert(nextstate < m_spec.m_dfa_states.size());
                                    }

                                dtrans.m_dtrans[i] = nextstate;
                            }

                        if (CUtility.DEBUG)
                            {
                                CUtility.cuassert(m_spec.m_dtrans_vector.size() == dfa.m_label);
                            }

                        m_spec.m_dtrans_vector.addElement(dtrans);
                    }
            }

        System.out.println("");
    }

    /***************************************************************
    Function: free_dfa_states
    **************************************************************/
    private void free_dfa_states
    (
     )
    {
        m_spec.m_dfa_states = null;
        m_spec.m_dfa_sets = null;
    }

    /***************************************************************
    Function: free_nfa_states
    **************************************************************/
    private void free_nfa_states
    (
     )
    {
        /* UNDONE: Remove references to nfas from within dfas. */
        /* UNDONE: Don't free CAccepts. */

        m_spec.m_nfa_states = null;
        m_spec.m_nfa_start = null;
        m_spec.m_state_rules = null;
    }

    /***************************************************************
    Function: e_closure
    Description: Alters and returns input set.
    **************************************************************/
    private void e_closure
    (
            CBunch bunch
            )
    {
        Stack nfa_stack;
        int size;
        int i;
        CNfa state;

        /* Debug checks. */
        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != bunch);
                CUtility.cuassert(null != bunch.m_nfa_set);
                CUtility.cuassert(null != bunch.m_nfa_bit);
            }

        bunch.m_accept = null;
        bunch.m_anchor = CSpec.NONE;
        bunch.m_accept_index = CUtility.INT_MAX;

        /* Create initial stack. */
        nfa_stack = new Stack();
        size = bunch.m_nfa_set.size();
        for (i = 0; i < size; ++i)
            {
                state = (CNfa) bunch.m_nfa_set.elementAt(i);

                if (CUtility.DEBUG)
                    {
                        CUtility.cuassert(bunch.m_nfa_bit.get(state.m_label));
                    }

                nfa_stack.push(state);
            }

        /* Main loop. */
        while (false == nfa_stack.empty())
            {
                state = (CNfa) nfa_stack.pop();

                if (CUtility.OLD_DUMP_DEBUG)
                    {
                        if (null != state.m_accept)
                            {
                                System.out.println("Looking at accepting state " + state.m_label
                                        + " with <"
                                        + (new String(state.m_accept.m_action,0,
                                                state.m_accept.m_action_read))
                                        + ">");
                            }
                    }

                if (null != state.m_accept
                        && state.m_label < bunch.m_accept_index)
                    {
                        bunch.m_accept_index = state.m_label;
                        bunch.m_accept = state.m_accept;
                        bunch.m_anchor = state.m_anchor;

                        if (CUtility.OLD_DUMP_DEBUG)
                            {
                                System.out.println("Found accepting state " + state.m_label
                                        + " with <"
                                        + (new String(state.m_accept.m_action,0,
                                                state.m_accept.m_action_read))
                                        + ">");
                            }

                        if (CUtility.DEBUG)
                            {
                                CUtility.cuassert(null != bunch.m_accept);
                                CUtility.cuassert(CSpec.NONE == bunch.m_anchor
                                        || 0 != (bunch.m_anchor & CSpec.END)
                                        || 0 != (bunch.m_anchor & CSpec.START));
                            }
                    }

                if (CNfa.EPSILON == state.m_edge)
                    {
                        if (null != state.m_next)
                            {
                                if (false == bunch.m_nfa_set.contains(state.m_next))
                                    {
                                        if (CUtility.DEBUG)
                                            {
                                                CUtility.cuassert(false == bunch.m_nfa_bit.get(state.m_next.m_label));
                                            }

                                        bunch.m_nfa_bit.set(state.m_next.m_label);
                                        bunch.m_nfa_set.addElement(state.m_next);
                                        nfa_stack.push(state.m_next);
                                    }
                            }

                        if (null != state.m_next2)
                            {
                                if (false == bunch.m_nfa_set.contains(state.m_next2))
                                    {
                                        if (CUtility.DEBUG)
                                            {
                                                CUtility.cuassert(false == bunch.m_nfa_bit.get(state.m_next2.m_label));
                                            }

                                        bunch.m_nfa_bit.set(state.m_next2.m_label);
                                        bunch.m_nfa_set.addElement(state.m_next2);
                                        nfa_stack.push(state.m_next2);
                                    }
                            }
                    }
            }

        if (null != bunch.m_nfa_set)
            {
                sortStates(bunch.m_nfa_set);
            }

        return;
    }

    /***************************************************************
    Function: move
    Description: Returns null if resulting NFA set is empty.
    **************************************************************/
    void move
    (
            Vector nfa_set,
            JavaLexBitSet nfa_bit,
            int b,
            CBunch bunch
            )
    {
        int size;
        int index;
        CNfa state;

        bunch.m_nfa_set = null;
        bunch.m_nfa_bit = null;

        size = nfa_set.size();
        for (index = 0; index < size; ++index)
            {
                state = (CNfa) nfa_set.elementAt(index);

                if (b == state.m_edge
                        || (CNfa.CCL == state.m_edge
                                && true == state.m_set.contains(b)))
                    {
                        if (null == bunch.m_nfa_set)
                            {
                                if (CUtility.DEBUG)
                                    {
                                        CUtility.cuassert(null == bunch.m_nfa_bit);
                                    }

                                bunch.m_nfa_set = new Vector();
                                /*bunch.m_nfa_bit
                                  = new JavaLexBitSet(m_spec.m_nfa_states.size());*/
                                bunch.m_nfa_bit = new JavaLexBitSet();
                            }

                        bunch.m_nfa_set.addElement(state.m_next);
                        /*System.out.println("Size of bitset: " + bunch.m_nfa_bit.size());
                          System.out.println("Reference index: " + state.m_next.m_label);
                          System.out.flush();*/
                        bunch.m_nfa_bit.set(state.m_next.m_label);
                    }
            }

        if (null != bunch.m_nfa_set)
            {
                if (CUtility.DEBUG)
                    {
                        CUtility.cuassert(null != bunch.m_nfa_bit);
                    }

                sortStates(bunch.m_nfa_set);
            }

        return;
    }

    /***************************************************************
    Function: sortStates
    **************************************************************/
    private void sortStates
    (
            Vector nfa_set
            )
    {
        CNfa elem;
        int begin;
        int size;
        int index;
        int value;
        int smallest_index;
        int smallest_value;
        CNfa begin_elem;

        size = nfa_set.size();
        for (begin = 0; begin < size; ++begin)
            {
                elem = (CNfa) nfa_set.elementAt(begin);
                smallest_value = elem.m_label;
                smallest_index = begin;

                for (index = begin + 1; index < size; ++index)
                    {
                        elem = (CNfa) nfa_set.elementAt(index);
                        value = elem.m_label;

                        if (value < smallest_value)
                            {
                                smallest_index = index;
                                smallest_value = value;
                            }
                    }

                begin_elem = (CNfa) nfa_set.elementAt(begin);
                elem = (CNfa) nfa_set.elementAt(smallest_index);
                nfa_set.setElementAt(elem,begin);
                nfa_set.setElementAt(begin_elem,smallest_index);
            }

        if (CUtility.OLD_DEBUG)
            {
                System.out.print("NFA vector indices: ");

                for (index = 0; index < size; ++index)
                    {
                        elem = (CNfa) nfa_set.elementAt(index);
                        System.out.print(elem.m_label + " ");
                    }
                System.out.print("\n");
            }

        return;
    }

    /***************************************************************
    Function: get_unmarked
    Description: Returns next unmarked DFA state.
    **************************************************************/
    private CDfa get_unmarked
    (
     )
    {
        int size;
        CDfa dfa;

        size = m_spec.m_dfa_states.size();
        while (m_unmarked_dfa < size)
            {
                dfa = (CDfa) m_spec.m_dfa_states.elementAt(m_unmarked_dfa);

                if (false == dfa.m_mark)
                    {
                        if (CUtility.OLD_DUMP_DEBUG)
                            {
                                System.out.print("*");
                                System.out.flush();
                            }

                        if (m_spec.m_verbose && true == CUtility.OLD_DUMP_DEBUG)
                            {
                                System.out.println("---------------");
                                System.out.print("working on DFA state "
                                        + m_unmarked_dfa
                                        + " = NFA states: ");
                                m_lexGen.print_set(dfa.m_nfa_set);
                                System.out.print("\n");
                            }

                        return dfa;
                    }

                ++m_unmarked_dfa;
            }

        return null;
    }

    /***************************************************************
    function: add_to_dstates
    Description: Takes as input a CBunch with details of
    a dfa state that needs to be created.
    1) Allocates a new dfa state and saves it in
    the appropriate CSpec vector.
    2) Initializes the fields of the dfa state
    with the information in the CBunch.
    3) Returns index of new dfa.
    **************************************************************/
    private int add_to_dstates
    (
            CBunch bunch
            )
    {
        CDfa dfa;

        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != bunch.m_nfa_set);
                CUtility.cuassert(null != bunch.m_nfa_bit);
                CUtility.cuassert(null != bunch.m_accept
                        || CSpec.NONE == bunch.m_anchor);
            }

        /* Allocate, passing CSpec so dfa label can be set. */
        dfa = CAlloc.newCDfa(m_spec);

        /* Initialize fields, including the mark field. */
        dfa.m_nfa_set = (Vector) bunch.m_nfa_set.clone();
        dfa.m_nfa_bit = (JavaLexBitSet) bunch.m_nfa_bit.clone();
        dfa.m_accept = bunch.m_accept;
        dfa.m_anchor = bunch.m_anchor;
        dfa.m_mark = false;

        /* Register dfa state using BitSet in CSpec Hashtable. */
        m_spec.m_dfa_sets.put(dfa.m_nfa_bit,dfa);
        /*registerCDfa(dfa);*/

        if (CUtility.OLD_DUMP_DEBUG)
            {
                System.out.print("Registering set : ");
                m_lexGen.print_set(dfa.m_nfa_set);
                System.out.println("");
            }

        return dfa.m_label;
    }

    /***************************************************************
    Function: in_dstates
    **************************************************************/
    private int in_dstates
    (
            CBunch bunch
            )
    {
        CDfa dfa;

        if (CUtility.OLD_DEBUG)
            {
                System.out.print("Looking for set : ");
                m_lexGen.print_set(bunch.m_nfa_set);
            }

        dfa = (CDfa) m_spec.m_dfa_sets.get(bunch.m_nfa_bit);

        if (null != dfa)
            {
                if (CUtility.OLD_DUMP_DEBUG)
                    {
                        System.out.println(" FOUND!");
                    }

                return dfa.m_label;
            }

        if (CUtility.OLD_DUMP_DEBUG)
            {
                System.out.println(" NOT FOUND!");
            }
        return NOT_IN_DSTATES;
    }

}

/***************************************************************
  Class: CAlloc
  **************************************************************/
class CAlloc
{
    /***************************************************************
                                                                    Function: newCDfa
    **************************************************************/
    static CDfa newCDfa
    (
            CSpec spec
            )
    {
        CDfa dfa;

        dfa = new CDfa(spec.m_dfa_states.size());
        spec.m_dfa_states.addElement(dfa);

        return dfa;
    }

    /***************************************************************
    Function: newCNfaPair
    Description:
    **************************************************************/
    static CNfaPair newCNfaPair
    (
     )
    {
        CNfaPair pair = new CNfaPair();

        return pair;
    }

    /***************************************************************
    Function: newCNfa
    Description:
    **************************************************************/
    static CNfa newCNfa
    (
            CSpec spec
            )
    {
        CNfa p;

        /* UNDONE: Buffer this? */

        p = new CNfa();

        /*p.m_label = spec.m_nfa_states.size();*/
        spec.m_nfa_states.addElement(p);
        p.m_edge = CNfa.EPSILON;

        return p;
    }
}

/***************************************************************
  Class: Main
  Description: Top-level lexical analyzer generator function.
 **************************************************************/
public class Main
{
    /**
     *  Invoke the lexical analyzer on a file.
     *  @param arg Arguments to JLex.  One and only one argument is
     *  required: The name of the file to be processed.
     *  @exception IOException If there are problems processing the file.
     */
    public static void main
    (
            String arg[]
            )
            throws java.io.IOException
    {
        CLexGen lg;

        if (arg.length < 1)
            {
                System.out.println("Usage: JLex.Main <filename>");
                return;
            }

        /* Note: For debuging, it may be helpful to remove the try/catch
           block and permit the Exception to propagate to the top level.
           This gives more information. */
        try
            {
                lg = new CLexGen(arg[0]);
                lg.generate();
            }
        catch (Error e)
            {
                System.out.println(e.getMessage());
            }
    }
}

/***************************************************************
  Class: CDTrans
  **************************************************************/
class CDTrans
{
    /*************************************************************
                                                                  Member Variables
    ***********************************************************/
    int m_dtrans[];
    CAccept m_accept;
    int m_anchor;
    int m_label;

  /*************************************************************
    Constants
    ***********************************************************/
    static final int F = -1;

    /*************************************************************
    Function: CTrans
    ***********************************************************/
    CDTrans
    (
            int label,
            CSpec spec
            )
    {
        m_dtrans = new int[spec.m_dtrans_ncols];
        m_accept = null;
        m_anchor = CSpec.NONE;
        m_label = label;
    }
}

/***************************************************************
  Class: CDfa
  **************************************************************/
class CDfa
{
    /***************************************************************
                                                                    Member Variables
    ***********************************************************/
    int m_group;
    boolean m_mark;
    CAccept m_accept;
    int m_anchor;
    Vector m_nfa_set;
    JavaLexBitSet m_nfa_bit;
    int m_label;

    /***************************************************************
    Function: CDfa
    **************************************************************/
    CDfa
    (
            int label
            )
    {
        m_group = 0;
        m_mark = false;

        m_accept = null;
        m_anchor = CSpec.NONE;

        m_nfa_set = null;
        m_nfa_bit = null;

        m_label = label;
    }
}

/***************************************************************
  Class: CAccept
 **************************************************************/
class CAccept
{
    /***************************************************************
                                                                    Member Variables
    **************************************************************/
    char m_action[];
    int m_action_read;
    int m_line_number;

    /***************************************************************
    Function: CAccept
    **************************************************************/
    CAccept
    (
            char action[],
            int action_read,
            int line_number
            )
    {
        int elem;

        m_action_read = action_read;

        m_action = new char[m_action_read];
        for (elem = 0; elem < m_action_read; ++elem)
            {
                m_action[elem] = action[elem];
            }

        m_line_number = line_number;
    }

    /***************************************************************
    Function: CAccept
    **************************************************************/
    CAccept
    (
            CAccept accept
            )
    {
        int elem;

        m_action_read = accept.m_action_read;

        m_action = new char[m_action_read];
        for (elem = 0; elem < m_action_read; ++elem)
            {
                m_action[elem] = accept.m_action[elem];
            }

        m_line_number = accept.m_line_number;
    }

    /***************************************************************
    Function: mimic
    **************************************************************/
    void mimic
    (
            CAccept accept
            )
    {
        int elem;

        m_action_read = accept.m_action_read;

        m_action = new char[m_action_read];
        for (elem = 0; elem < m_action_read; ++elem)
            {
                m_action[elem] = accept.m_action[elem];
            }
    }
}

/***************************************************************
  Class: CAcceptAnchor
  **************************************************************/
class CAcceptAnchor
{
    /***************************************************************
                                                                    Member Variables
    **************************************************************/
    CAccept m_accept;
    int m_anchor;

  /***************************************************************
    Function: CAcceptAnchor
    **************************************************************/
    CAcceptAnchor
    (
     )
    {
        m_accept = null;
        m_anchor = CSpec.NONE;
    }
}

/***************************************************************
  Class: CNfaPair
  **************************************************************/
class CNfaPair
{
    /***************************************************************
                                                                    Member Variables
    **************************************************************/
    CNfa m_start;
    CNfa m_end;

  /***************************************************************
    Function: CNfaPair
    **************************************************************/
    CNfaPair
    (
     )
    {
        m_start = null;
        m_end = null;
    }
}

/***************************************************************
  Class: CInput
  Description:
 **************************************************************/
class CInput
{
    /***************************************************************
                                                                    Member Variables
    **************************************************************/
    private InputStream m_input; /* JLex specification file. */

    private byte m_buffer[]; /* Input buffer. */
    private int m_buffer_read; /* Number of bytes read into input buffer. */
    private int m_buffer_index; /* Current index into input buffer. */

    boolean m_eof_reached; /* Whether EOF has been encountered. */
    boolean m_pushback_line;

    char m_line[]; /* Line buffer. */
    int m_line_read; /* Number of bytes read into line buffer. */
    int m_line_index; /* Current index into line buffer. */

    int m_line_number; /* Current line number. */

  /***************************************************************
    Constants
    **************************************************************/
    private static final int BUFFER_SIZE = 1024;
    static final boolean EOF = true;
    static final boolean NOT_EOF = false;

    /***************************************************************
    Function: CInput
    Description:
    **************************************************************/
    CInput
    (
            InputStream input
            )
    {
        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != input);
            }

        /* Initialize input stream. */
        m_input = input;

        /* Initialize buffers and index counters. */
        m_buffer = new byte[BUFFER_SIZE];
        m_buffer_read = 0;
        m_buffer_index = 0;

        m_line = new char[BUFFER_SIZE];
        m_line_read = 0;
        m_line_index = 0;

        /* Initialize state variables. */
        m_eof_reached = false;
        m_line_number = 0;
        m_pushback_line = false;
    }

    /***************************************************************
    Function: getLine
    Description: Returns true on EOF, false otherwise.
    Guarantees not to return a blank line, or a line
    of zero length.
    **************************************************************/
    boolean getLine
    (
     )
            throws java.io.IOException
    {
        int elem;

        /* Has EOF already been reached? */
        if (m_eof_reached)
            {
                if (CUtility.OLD_DEBUG)
                    {
                        System.out.print("Line 1: ");
                        System.out.print(new String(m_line,0,
                                m_line_read));
                    }

                return true;
            }

        /* Pushback current line? */
        if (m_pushback_line)
            {
                m_pushback_line = false;

                /* Check for empty line. */
                for (elem = 0; elem < m_line_read; ++elem)
                    {
                        if (false == CUtility.isspace(m_line[elem]))
                            {
                                break;
                            }
                    }

                /* Nonempty? */
                if (elem < m_line_read)
                    {
                        m_line_index = 0;
                        return false;
                    }

                if (CUtility.OLD_DEBUG)
                    {
                        System.out.print("Line 2: ");
                        System.out.print(new String(m_line,0,
                                m_line_read));
                    }
            }

        while (true)
            {
                /* Refill buffer? */
                if (m_buffer_index >= m_buffer_read)
                    {
                        m_buffer_read = m_input.read(m_buffer);
                        if (-1 == m_buffer_read)
                            {
                                m_eof_reached = true;

                                if (CUtility.OLD_DEBUG)
                                    {
                                        System.out.print("Line 3: ");
                                        System.out.print(new String(m_line,0,
                                                m_line_read));
                                    }

                                m_line_index = 0;
                                return true;
                            }
                        m_buffer_index = 0;
                    }

                m_line_read = 0;
                while ((byte) '\n' != m_buffer[m_buffer_index]
                        && (byte) '\r' != m_buffer[m_buffer_index])
                    {
                        m_line[m_line_read] = (char) m_buffer[m_buffer_index];

                        ++m_buffer_index;

                        /* Refill buffer? */
                        if (m_buffer_index >= m_buffer_read)
                            {
                                m_buffer_read = m_input.read(m_buffer);
                                if (-1 == m_buffer_read)
                                    {
                                        m_eof_reached = true;

                                        /* Check for empty lines and discard them. */
                                        elem = 0;
                                        while (CUtility.isspace(m_line[elem]))
                                            {
                                                ++elem;
                                                if (elem == m_line_read)
                                                    {
                                                        break;
                                                    }
                                            }

                                        if (elem < m_line_read)
                                            {
                                                if (CUtility.OLD_DEBUG)
                                                    {
                                                        System.out.print("Line 4:");
                                                        System.out.print(new String(m_line,
                                                                0,
                                                                m_line_read));
                                                    }


                                                m_line_index = 0;
                                                return false;
                                            }

                                        if (CUtility.OLD_DEBUG)
                                            {
                                                System.out.print("Line <e>:");
                                                System.out.print(new String(m_line,0,
                                                        m_line_read));
                                            }

                                        m_line_index = 0;
                                        return true;
                                    }

                                m_buffer_index = 0;
                            }

                        ++m_line_read;

                        /* Resize line buffer? */
                        if (m_line_read >= m_line.length)
                            {
                                m_line = CUtility.doubleSize(m_line);
                            }
                    }
                /* Save newline in buffer. */
                /* Old Source:
                   m_line[m_line_read] = (char) m_buffer[m_buffer_index];
                   ++m_line_read;
                   ++m_buffer_index;
                   New Source: */
                if ((byte) '\r' == m_buffer[m_buffer_index])
                    {
                        /* This is a hack and may propagate
                           some (pre-existing) line counting bugs on non-UNIX,
                           but otherwise it will work fine.
                           The only effect will be some bad error messages.
                           The other option is to do some massive
                           hacking on the input routines
                           that may destabilize the code. */
                        m_line[m_line_read] = '\n';
                        ++m_line_read;
                        ++m_buffer_index;
                    }
                else
                    {
                        m_line[m_line_read] = (char) m_buffer[m_buffer_index];
                        ++m_line_read;
                        ++m_buffer_index;
                    }
                /* End of New Source. */
                ++m_line_number;

                /* Check for empty lines and discard them. */
                elem = 0;
                while (CUtility.isspace(m_line[elem]))
                    {
                        ++elem;
                        if (elem == m_line_read)
                            {
                                break;
                            }
                    }

                if (elem < m_line_read)
                    {
                        break;
                    }
            }

        if (CUtility.OLD_DEBUG)
            {
                System.out.print("Line <f>:");
                System.out.print(new String(m_line,0,m_line_read));
            }

        m_line_index = 0;
        return false;
    }
}

/********************************************************
  Class: Utility
  *******************************************************/
class CUtility
{
    /********************************************************
                                                             Constants
    *******************************************************/
    static final boolean DEBUG = true;
    static final boolean SLOW_DEBUG = true;
    static final boolean DUMP_DEBUG = true;
    /*static final boolean DEBUG = false;
  static final boolean SLOW_DEBUG = false;
  static final boolean DUMP_DEBUG = false;*/
    static final boolean DESCENT_DEBUG = false;
    static final boolean OLD_DEBUG = false;
    static final boolean OLD_DUMP_DEBUG = false;
    static final boolean FOODEBUG = false;
    static final boolean DO_DEBUG = false;

    /********************************************************
    Constants: Integer Bounds
    *******************************************************/
    static final int INT_MAX = 2147483647;

    /* UNDONE: What about other character values??? */
    static final int MAX_SEVEN_BIT = 127;
    static final int MAX_EIGHT_BIT = 256;

  /********************************************************
    Function: enter
    Description: Debugging routine.
    *******************************************************/
    static void enter
    (
            String descent,
            char lexeme,
            int token
            )
    {
        System.out.println("Entering " + descent
                + " [lexeme: " + lexeme
                + "] [token: " + token + "]");
    }

    /********************************************************
    Function: leave
    Description: Debugging routine.
    *******************************************************/
    static void leave
    (
            String descent,
            char lexeme,
            int token
            )
    {
        System.out.println("Leaving " + descent
                + " [lexeme:" + lexeme
                + "] [token:" + token + "]");
    }

    /********************************************************
    Function: cuassert
    Description: Debugging routine.
    As of release 1.4, cuassert is a keyword, and may not be used as
    an identifier
    *******************************************************/
    static void cuassert
    (
            boolean expr
            )
    {
        if (DEBUG && false == expr)
            {
                System.out.println("Cuassertion Failed");
                throw new Error("Cuassertion Failed.");
            }
    }

    /***************************************************************
    Function: doubleSize
    **************************************************************/
    static char[] doubleSize
    (
            char oldBuffer[]
            )
    {
        char newBuffer[] = new char[2 * oldBuffer.length];
        int elem;

        for (elem = 0; elem < oldBuffer.length; ++elem)
            {
                newBuffer[elem] = oldBuffer[elem];
            }

        return newBuffer;
    }

    /***************************************************************
    Function: doubleSize
    **************************************************************/
    static byte[] doubleSize
    (
            byte oldBuffer[]
            )
    {
        byte newBuffer[] = new byte[2 * oldBuffer.length];
        int elem;

        for (elem = 0; elem < oldBuffer.length; ++elem)
            {
                newBuffer[elem] = oldBuffer[elem];
            }

        return newBuffer;
    }

    /********************************************************
    Function: hex2bin
    *******************************************************/
    static char hex2bin
    (
            char c
            )
    {
        if ('0' <= c && '9' >= c)
            {
                return (char) (c - '0');
            }
        else if ('a' <= c && 'f' >= c)
            {
                return (char) (c - 'a' + 10);
            }
        else if ('A' <= c && 'F' >= c)
            {
                return (char) (c - 'A' + 10);
            }

        CError.impos("Bad hexadecimal digit" + c);
        return 0;
    }

    /********************************************************
    Function: ishexdigit
    *******************************************************/
    static boolean ishexdigit
    (
            char c
            )
    {
        if (('0' <= c && '9' >= c)
                || ('a' <= c && 'f' >= c)
                || ('A' <= c && 'F' >= c))
            {
                return true;
            }

        return false;
    }

    /********************************************************
    Function: oct2bin
    *******************************************************/
    static char oct2bin
    (
            char c
            )
    {
        if ('0' <= c && '7' >= c)
            {
                return (char) (c - '0');
            }

        CError.impos("Bad octal digit " + c);
        return 0;
    }

    /********************************************************
    Function: isoctdigit
    *******************************************************/
    static boolean isoctdigit
    (
            char c
            )
    {
        if ('0' <= c && '7' >= c)
            {
                return true;
            }

        return false;
    }

    /********************************************************
    Function: isspace
    *******************************************************/
    static boolean isspace
    (
            char c
            )
    {
        if ('\b' == c
                || '\t' == c
                || '\n' == c
                || '\f' == c
                || '\r' == c
                || ' ' == c)
            {
                return true;
            }

        return false;
    }

    /********************************************************
    Function: isnewline
    *******************************************************/
    static boolean isnewline
    (
            char c
            )
    {
        if ('\n' == c
                || '\r' == c)
            {
                return true;
            }

        return false;
    }

    /********************************************************
    Function: isalpha
    *******************************************************/
    static boolean isalpha
    (
            char c
            )
    {
        if (('a' <= c && 'z' >= c)
                || ('A' <= c && 'Z' >= c))
            {
                return true;
            }

        return false;
    }

    /********************************************************
    Function: toupper
    *******************************************************/
    static char toupper
    (
            char c
            )
    {
        if (('a' <= c && 'z' >= c))
            {
                return (char) (c - 'a' + 'A');
            }

        return c;
    }

    /********************************************************
    Function: bytencmp
    Description: Compares up to n elements of
    byte array a[] against byte array b[].
    The first byte comparison is made between
    a[a_first] and b[b_first].  Comparisons continue
    until the null terminating byte '\0' is reached
    or until n bytes are compared.
    Return Value: Returns 0 if arrays are the
    same up to and including the null terminating byte
    or up to and including the first n bytes,
    whichever comes first.
    *******************************************************/
    static int bytencmp
    (
            byte a[],
            int a_first,
            byte b[],
            int b_first,
            int n
            )
    {
        int elem;

        for (elem = 0; elem < n; ++elem)
            {
                /*System.out.print((char) a[a_first + elem]);
                  System.out.print((char) b[b_first + elem]);*/

                if ('\0' == a[a_first + elem] && '\0' == b[b_first + elem])
                    {
                        /*System.out.println("return 0");*/
                        return 0;
                    }
                if (a[a_first + elem] < b[b_first + elem])
                    {
                        /*System.out.println("return 1");*/
                        return 1;
                    }
                else if (a[a_first + elem] > b[b_first + elem])
                    {
                        /*System.out.println("return -1");*/
                        return -1;
                    }
            }

        /*System.out.println("return 0");*/
        return 0;
    }

    /********************************************************
    Function: charncmp
    *******************************************************/
    static int charncmp
    (
            char a[],
            int a_first,
            char b[],
            int b_first,
            int n
            )
    {
        int elem;

        for (elem = 0; elem < n; ++elem)
            {
                if ('\0' == a[a_first + elem] && '\0' == b[b_first + elem])
                    {
                        return 0;
                    }
                if (a[a_first + elem] < b[b_first + elem])
                    {
                        return 1;
                    }
                else if (a[a_first + elem] > b[b_first + elem])
                    {
                        return -1;
                    }
            }

        return 0;
    }
}

/********************************************************
  Class: CError
  *******************************************************/
class CError
{
    /********************************************************
                                                             Function: impos
                                                             Description:
    *******************************************************/
    static void impos
    (
            String message
            )
    {
        System.out.println("JLex Error: " + message);
    }

    /********************************************************
    Constants
    Description: Error codes for parse_error().
    *******************************************************/
    static final int E_BADEXPR = 0;
    static final int E_PAREN = 1;
    static final int E_LENGTH = 2;
    static final int E_BRACKET = 3;
    static final int E_BOL = 4;
    static final int E_CLOSE = 5;
    static final int E_NEWLINE = 6;
    static final int E_BADMAC = 7;
    static final int E_NOMAC = 8;
    static final int E_MACDEPTH = 9;
    static final int E_INIT = 10;
    static final int E_EOF = 11;
    static final int E_DIRECT = 12;
    static final int E_INTERNAL = 13;
    static final int E_STATE = 14;
    static final int E_MACDEF = 15;
    static final int E_SYNTAX = 16;
    static final int E_BRACE = 17;
    static final int E_DASH = 18;

    /********************************************************
    Constants
    Description: String messages for parse_error();
    *******************************************************/
    static final String errmsg[] =
    {
        "Malformed regular expression.",
        "Missing close parenthesis.",
        "Too many regular expressions or expression too long.",
        "Missing [ in character class.",
        "^ must be at start of expression or after [.",
        "+ ? or * must follow an expression or subexpression.",
        "Newline in quoted string.",
        "Missing } in macro expansion.",
        "Macro does not exist.",
        "Macro expansions nested too deeply.",
        "JLex has not been successfully initialized.",
        "Unexpected end-of-file found.",
        "Undefined or badly-formed JLex directive.",
        "Internal JLex error.",
        "Uninitialized state name.",
        "Badly formed macro definition.",
        "Syntax error.",
        "Missing brace at start of lexical action.",
        "Special character dash - in character class [...] must\n"
        + "\tbe preceded by start-of-range character."
    };

    /********************************************************
    Function: parse_error
    Description:
    *******************************************************/
    static void parse_error
    (
            int error_code,
            int line_number
            )
    {
        System.out.println("Error: Parse error at line "
                + line_number + ".");
        System.out.println("Description: " + errmsg[error_code]);
        throw new Error("Parse error.");
    }
}

/********************************************************
  Class: CSet
  *******************************************************/
class CSet
{
    /********************************************************
                                                             Member Variables
    *******************************************************/
    private JavaLexBitSet m_set;
    private boolean m_complement;

  /********************************************************
    Function: CSet
    *******************************************************/
    CSet
    (
     )
    {
        m_set = new JavaLexBitSet();
        m_complement = false;
    }

    /********************************************************
    Function: complement
    *******************************************************/
    void complement
    (
     )
    {
        m_complement = true;
    }

    /********************************************************
    Function: add
    *******************************************************/
    void add
    (
            int i
            )
    {
        m_set.set(i);
    }

    /********************************************************
    Function: addncase
    *******************************************************/
    void addncase // add, ignoring case.
    (
            char c
            )
    {
        /* Do this in a Unicode-friendly way. */
        /* (note that duplicate adds have no effect) */
        add(c);
        add(Character.toLowerCase(c));
        add(Character.toTitleCase(c));
        add(Character.toUpperCase(c));
    }

    /********************************************************
    Function: contains
    *******************************************************/
    boolean contains
    (
            int i
            )
    {
        boolean result;

        result = m_set.get(i);

        if (m_complement)
            {
                return (false == result);
            }

        return result;
    }

    /********************************************************
    Function: mimic
    *******************************************************/
    void mimic
    (
            CSet set
            )
    {
        m_complement = set.m_complement;
        m_set = (JavaLexBitSet) set.m_set.clone();
    }
}

/********************************************************
  Class: CNfa
  *******************************************************/
class CNfa
{
    /********************************************************
                                                             Member Variables
    *******************************************************/
    int m_edge;  /* Label for edge type:
                    character code,
                    CCL (character class),
                    [STATE,
                    SCL (state class),]
                    EMPTY,
                    EPSILON. */

    CSet m_set;  /* Set to store character classes. */
    CNfa m_next;  /* Next state (or null if none). */

    CNfa m_next2;  /* Another state with type == EPSILON
                           and null if not used.
                           The NFA construction should result in two
                           outgoing edges only if both are EPSILON edges. */

    CAccept m_accept;  /* Set to null if nonaccepting state. */
    int m_anchor;  /* Says if and where pattern is anchored. */

    int m_label;

    JavaLexBitSet m_states;

  /********************************************************
    Constants
    *******************************************************/
    static final int NO_LABEL = -1;

    /********************************************************
    Constants: Edge Types
    Note: Edge transitions on one specific character
    are labelled with the character Ascii (Unicode)
    codes.  So none of the constants below should
    overlap with the natural character codes.
    *******************************************************/
    static final int CCL = -1;
    static final int EMPTY = -2;
    static final int EPSILON = -3;

    /********************************************************
    Function: CNfa
    *******************************************************/
    CNfa
    (
     )
    {
        m_edge = EMPTY;
        m_set = null;
        m_next = null;
        m_next2 = null;
        m_accept = null;
        m_anchor = CSpec.NONE;
        m_label = NO_LABEL;
        m_states = null;
    }

    /********************************************************
    Function: mimic
    Description: Converts this NFA state into a copy of
    the input one.
    *******************************************************/
    void mimic
    (
            CNfa nfa
            )
    {
        m_edge = nfa.m_edge;

        if (null != nfa.m_set)
            {
                if (null == m_set)
                    {
                        m_set = new CSet();
                    }
                m_set.mimic(nfa.m_set);
            }
        else
            {
                m_set = null;
            }

        m_next = nfa.m_next;
        m_next2 = nfa.m_next2;
        m_accept = nfa.m_accept;
        m_anchor = nfa.m_anchor;

        if (null != nfa.m_states)
            {
                m_states = (JavaLexBitSet) nfa.m_states.clone();
            }
        else
            {
                m_states = null;
            }
    }
}

/***************************************************************
  Class: CLexGen
  **************************************************************/
class CLexGen
{
    /***************************************************************
                                                                    Member Variables
    **************************************************************/
    private InputStream m_instream; /* JLex specification file. */
    private DataOutputStream m_outstream; /* Lexical analyzer source file. */

    private CInput m_input; /* Input buffer class. */

    private Hashtable m_tokens; /* Hashtable that maps characters to their
                                 corresponding lexical code for
                                 the internal lexical analyzer. */
    private CSpec m_spec; /* Spec class holds information
                             about the generated lexer. */
    private boolean m_init_flag; /* Flag set to true only upon
                                    successful initialization. */

    private CMakeNfa m_makeNfa; /* NFA machine generator module. */
    private CNfa2Dfa m_nfa2dfa; /* NFA to DFA machine (transition table)
                                 conversion module. */
    private CMinimize m_minimize; /* Transition table compressor. */
    private CEmit m_emit; /* Output module that emits source code
                           into the generated lexer file. */


  /********************************************************
    Constants
    *******************************************************/
    private static final boolean ERROR = false;
    private static final boolean NOT_ERROR = true;
    private static final int BUFFER_SIZE = 1024;

    /********************************************************
    Constants: Token Types
    *******************************************************/
    static final int EOS = 1;
    static final int ANY = 2;
    static final int AT_BOL = 3;
    static final int AT_EOL = 4;
    static final int CCL_END = 5;
    static final int CCL_START = 6;
    static final int CLOSE_CURLY = 7;
    static final int CLOSE_PAREN = 8;
    static final int CLOSURE = 9;
    static final int DASH = 10;
    static final int END_OF_INPUT = 11;
    static final int L = 12;
    static final int OPEN_CURLY = 13;
    static final int OPEN_PAREN = 14;
    static final int OPTIONAL = 15;
    static final int OR = 16;
    static final int PLUS_CLOSE = 17;

    /***************************************************************
    Function: CLexGen
    **************************************************************/
    CLexGen
    (
            String filename
            )
            throws java.io.FileNotFoundException, java.io.IOException
    {
        /* Successful initialization flag. */
        m_init_flag = false;

        /* Open input stream. */
        m_instream = new FileInputStream(filename);
        if (null == m_instream)
            {
                System.out.println("Error: Unable to open input file "
                        + filename + ".");
                return;
            }

        /* Open output stream. */
        m_outstream
            = new DataOutputStream(new BufferedOutputStream(
                    new FileOutputStream(filename + ".java")));
        if (null == m_outstream)
            {
                System.out.println("Error: Unable to open output file "
                        + filename + ".java.");
                return;
            }

        /* Create input buffer class. */
        m_input = new CInput(m_instream);

        /* Initialize character hash table. */
        m_tokens = new Hashtable();
        m_tokens.put(new Character('$'),new Integer(AT_EOL));
        m_tokens.put(new Character('('),new Integer(OPEN_PAREN));
        m_tokens.put(new Character(')'),new Integer(CLOSE_PAREN));
        m_tokens.put(new Character('*'),new Integer(CLOSURE));
        m_tokens.put(new Character('+'),new Integer(PLUS_CLOSE));
        m_tokens.put(new Character('-'),new Integer(DASH));
        m_tokens.put(new Character('.'),new Integer(ANY));
        m_tokens.put(new Character('?'),new Integer(OPTIONAL));
        m_tokens.put(new Character('['),new Integer(CCL_START));
        m_tokens.put(new Character(']'),new Integer(CCL_END));
        m_tokens.put(new Character('^'),new Integer(AT_BOL));
        m_tokens.put(new Character('{'),new Integer(OPEN_CURLY));
        m_tokens.put(new Character('|'),new Integer(OR));
        m_tokens.put(new Character('}'),new Integer(CLOSE_CURLY));

        /* Initialize spec structure. */
        m_spec = new CSpec(this);

        /* Nfa to dfa converter. */
        m_nfa2dfa = new CNfa2Dfa();
        m_minimize = new CMinimize();
        m_makeNfa = new CMakeNfa();

        m_emit = new CEmit();

        /* Successful initialization flag. */
        m_init_flag = true;
    }

    /***************************************************************
    Function: generate
    Description:
    **************************************************************/
    void generate
    (
     )
            throws java.io.IOException, java.io.FileNotFoundException
    {
        if (false == m_init_flag)
            {
                CError.parse_error(CError.E_INIT,0);
            }

        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != this);
                CUtility.cuassert(null != m_outstream);
                CUtility.cuassert(null != m_input);
                CUtility.cuassert(null != m_tokens);
                CUtility.cuassert(null != m_spec);
                CUtility.cuassert(m_init_flag);
            }

        /*m_emit.emit_imports(m_spec,m_outstream);*/

        if (m_spec.m_verbose)
            {
                System.out.println("Processing first section -- user code.");
            }
        userCode();
        if (m_input.m_eof_reached)
            {
                CError.parse_error(CError.E_EOF,m_input.m_line_number);
            }

        if (m_spec.m_verbose)
            {
                System.out.println("Processing second section -- "
                        + "JLex declarations.");
            }
        userDeclare();
        if (m_input.m_eof_reached)
            {
                CError.parse_error(CError.E_EOF,m_input.m_line_number);
            }

        if (m_spec.m_verbose)
            {
                System.out.println("Processing third section -- lexical rules.");
            }
        userRules();
        if (CUtility.DO_DEBUG)
            {
                print_header();
            }

        if (m_spec.m_verbose)
            {
                System.out.println("Outputting lexical analyzer code.");
            }
        m_emit.emit(m_spec,m_outstream);

        if (m_spec.m_verbose && true == CUtility.OLD_DUMP_DEBUG)
            {
                details();
            }

        m_outstream.close();
    }

    /***************************************************************
    Function: userCode
    Description: Process first section of specification,
    echoing it into output file.
    **************************************************************/
    private void userCode
    (
     )
            throws java.io.IOException
    {

        if (false == m_init_flag)
            {
                CError.parse_error(CError.E_INIT,0);
            }

        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != this);
                CUtility.cuassert(null != m_outstream);
                CUtility.cuassert(null != m_input);
                CUtility.cuassert(null != m_tokens);
                CUtility.cuassert(null != m_spec);
            }

        if (m_input.m_eof_reached)
            {
                CError.parse_error(CError.E_EOF,0);
            }

        while (true)
            {
                if (m_input.getLine())
                    {
                        /* Eof reached. */
                        CError.parse_error(CError.E_EOF,0);
                    }

                if (2 <= m_input.m_line_read
                        && '%' == m_input.m_line[0]
                        && '%' == m_input.m_line[1])
                    {
                        /* Discard remainder of line. */
                        m_input.m_line_index = m_input.m_line_read;
                        return;
                    }

                m_outstream.writeBytes(new String(m_input.m_line,0,
                        m_input.m_line_read));
            }
    }

    /***************************************************************
    Function: getName
    **************************************************************/
    private char[] getName
    (
     )
    {
        char buffer[];
        int elem;

        /* Skip white space. */
        while (m_input.m_line_index < m_input.m_line_read
                && true == CUtility.isspace(m_input.m_line[m_input.m_line_index]))
            {
                ++m_input.m_line_index;
            }

        /* No name? */
        if (m_input.m_line_index >= m_input.m_line_read)
            {
                CError.parse_error(CError.E_DIRECT,0);
            }

        /* Determine length. */
        elem = m_input.m_line_index;
        while (elem < m_input.m_line_read
                && false == CUtility.isnewline(m_input.m_line[elem]))
            {
                ++elem;
            }

        /* Allocate non-terminated buffer of exact length. */
        buffer = new char[elem - m_input.m_line_index];

        /* Copy. */
        elem = 0;
        while (m_input.m_line_index < m_input.m_line_read
                && false == CUtility.isnewline(m_input.m_line[m_input.m_line_index]))
            {
                buffer[elem] = m_input.m_line[m_input.m_line_index];
                ++elem;
                ++m_input.m_line_index;
            }

        return buffer;
    }

    private final int CLASS_CODE = 0;
    private final int INIT_CODE = 1;
    private final int EOF_CODE = 2;
    private final int INIT_THROW_CODE = 3;
    private final int YYLEX_THROW_CODE = 4;
    private final int EOF_THROW_CODE = 5;
    private final int EOF_VALUE_CODE = 6;

  /***************************************************************
    Function: packCode
    Description:
    **************************************************************/
    private char[] packCode
    (
            char start_dir[],
            char end_dir[],
            char prev_code[],
            int prev_read,
            int specified
            )
            throws java.io.IOException
    {
        if (CUtility.DEBUG)
            {
                CUtility.cuassert(INIT_CODE == specified
                        || CLASS_CODE == specified
                        || EOF_CODE == specified
                        || EOF_VALUE_CODE == specified
                        || INIT_THROW_CODE == specified
                        || YYLEX_THROW_CODE == specified
                        || EOF_THROW_CODE == specified);
            }

        if (0 != CUtility.charncmp(m_input.m_line,
                0,
                start_dir,
                0,
                start_dir.length - 1))
            {
                CError.parse_error(CError.E_INTERNAL,0);
            }

        if (null == prev_code)
            {
                prev_code = new char[BUFFER_SIZE];
                prev_read = 0;
            }

        if (prev_read >= prev_code.length)
            {
                prev_code = CUtility.doubleSize(prev_code);
            }

        m_input.m_line_index = start_dir.length - 1;
        while (true)
            {
                while (m_input.m_line_index >= m_input.m_line_read)
                    {
                        if (m_input.getLine())
                            {
                                CError.parse_error(CError.E_EOF,m_input.m_line_number);
                            }

                        if (0 == CUtility.charncmp(m_input.m_line,
                                0,
                                end_dir,
                                0,
                                end_dir.length - 1))
                            {
                                m_input.m_line_index = end_dir.length - 1;

                                switch (specified)
                                    {
                                    case CLASS_CODE:
                                        m_spec.m_class_read = prev_read;
                                        break;

                                    case INIT_CODE:
                                        m_spec.m_init_read = prev_read;
                                        break;

                                    case EOF_CODE:
                                        m_spec.m_eof_read = prev_read;
                                        break;

                                    case EOF_VALUE_CODE:
                                        m_spec.m_eof_value_read = prev_read;
                                        break;

                                    case INIT_THROW_CODE:
                                        m_spec.m_init_throw_read = prev_read;
                                        break;

                                    case YYLEX_THROW_CODE:
                                        m_spec.m_yylex_throw_read = prev_read;
                                        break;

                                    case EOF_THROW_CODE:
                                        m_spec.m_eof_throw_read = prev_read;
                                        break;

                                    default:
                                        CError.parse_error(CError.E_INTERNAL,m_input.m_line_number);
                                        break;
                                    }

                                return prev_code;
                            }
                    }

                while (m_input.m_line_index < m_input.m_line_read)
                    {
                        prev_code[prev_read] = m_input.m_line[m_input.m_line_index];
                        ++prev_read;
                        ++m_input.m_line_index;

                        if (prev_read >= prev_code.length)
                            {
                                prev_code = CUtility.doubleSize(prev_code);
                            }
                    }
            }
    }

    /***************************************************************
    Member Variables: JLex directives.
    **************************************************************/
    private char m_state_dir[] = {
        '%', 's', 't',
        'a', 't', 'e',
        '\0'
    };

    private char m_char_dir[] = {
        '%', 'c', 'h',
        'a', 'r',
        '\0'
    };

    private char m_line_dir[] = {
        '%', 'l', 'i',
        'n', 'e',
        '\0'
    };

    private char m_cup_dir[] = {
        '%', 'c', 'u',
        'p',
        '\0'
    };

    private char m_class_dir[] = {
        '%', 'c', 'l',
        'a', 's', 's',
        '\0'
    };

    private char m_implements_dir[] = {
        '%', 'i', 'm', 'p', 'l', 'e', 'm', 'e', 'n', 't', 's',
        '\0'
    };

    private char m_function_dir[] = {
        '%', 'f', 'u',
        'n', 'c', 't',
        'i', 'o', 'n',
        '\0'
    };

    private char m_type_dir[] = {
        '%', 't', 'y',
        'p', 'e',
        '\0'
    };

    private char m_integer_dir[] = {
        '%', 'i', 'n',
        't', 'e', 'g',
        'e', 'r',
        '\0'
    };

    private char m_intwrap_dir[] = {
        '%', 'i', 'n',
        't', 'w', 'r',
        'a', 'p',
        '\0'
    };

    private char m_full_dir[] = {
        '%', 'f', 'u',
        'l', 'l',
        '\0'
    };

    private char m_unicode_dir[] = {
        '%', 'u', 'n',
        'i', 'c', 'o',
        'd', 'e',
        '\0'
    };

    private char m_ignorecase_dir[] = {
        '%', 'i', 'g',
        'n', 'o', 'r',
        'e', 'c', 'a',
        's', 'e',
        '\0'
    };

    private char m_notunix_dir[] = {
        '%', 'n', 'o',
        't', 'u', 'n',
        'i', 'x',
        '\0'
    };

    private char m_init_code_dir[] = {
        '%', 'i', 'n',
        'i', 't', '{',
        '\0'
    };

    private char m_init_code_end_dir[] = {
        '%', 'i', 'n',
        'i', 't', '}',
        '\0'
    };

    private char m_init_throw_code_dir[] = {
        '%', 'i', 'n',
        'i', 't', 't',
        'h', 'r', 'o',
        'w', '{',
        '\0'
    };

    private char m_init_throw_code_end_dir[] = {
        '%', 'i', 'n',
        'i', 't', 't',
        'h', 'r', 'o',
        'w', '}',
        '\0'
    };

    private char m_yylex_throw_code_dir[] = {
        '%', 'y', 'y', 'l',
        'e', 'x', 't',
        'h', 'r', 'o',
        'w', '{',
        '\0'
    };

    private char m_yylex_throw_code_end_dir[] = {
        '%', 'y', 'y', 'l',
        'e', 'x', 't',
        'h', 'r', 'o',
        'w', '}',
        '\0'
    };

    private char m_eof_code_dir[] = {
        '%', 'e', 'o',
        'f', '{',
        '\0'
    };

    private char m_eof_code_end_dir[] = {
        '%', 'e', 'o',
        'f', '}',
        '\0'
    };

    private char m_eof_value_code_dir[] = {
        '%', 'e', 'o',
        'f', 'v', 'a',
        'l', '{',
        '\0'
    };

    private char m_eof_value_code_end_dir[] = {
        '%', 'e', 'o',
        'f', 'v', 'a',
        'l', '}',
        '\0'
    };

    private char m_eof_throw_code_dir[] = {
        '%', 'e', 'o',
        'f', 't', 'h',
        'r', 'o', 'w',
        '{',
        '\0'
    };

    private char m_eof_throw_code_end_dir[] = {
        '%', 'e', 'o',
        'f', 't', 'h',
        'r', 'o', 'w',
        '}',
        '\0'
    };

    private char m_class_code_dir[] = {
        '%', '{',
        '\0'
    };

    private char m_class_code_end_dir[] = {
        '%', '}',
        '\0'
    };

    private char m_yyeof_dir[] = {
        '%', 'y', 'y',
        'e', 'o', 'f',
        '\0'
    };

    private char m_public_dir[] = {
        '%', 'p', 'u',
        'b', 'l', 'i',
        'c', '\0'
    };

    /***************************************************************
    Function: userDeclare
    Description:
    **************************************************************/
    private void userDeclare
    (
     )
            throws java.io.IOException
    {
        int elem;

        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != this);
                CUtility.cuassert(null != m_outstream);
                CUtility.cuassert(null != m_input);
                CUtility.cuassert(null != m_tokens);
                CUtility.cuassert(null != m_spec);
            }

        if (m_input.m_eof_reached)
            {
                /* End-of-file. */
                CError.parse_error(CError.E_EOF,
                        m_input.m_line_number);
            }

        while (false == m_input.getLine())
            {
                /* Look for double percent. */
                if (2 <= m_input.m_line_read
                        && '%' == m_input.m_line[0]
                        && '%' == m_input.m_line[1])
                    {
                        /* Mess around with line. */
                        for (elem = 0; elem < m_input.m_line.length - 2; ++elem)
                            {
                                m_input.m_line[elem] = m_input.m_line[elem + 2];
                            }
                        m_input.m_line_read = m_input.m_line_read - 2;

                        m_input.m_pushback_line = true;
                        /* Check for and discard empty line. */
                        if (0 == m_input.m_line_read
                                || '\r' == m_input.m_line[0]
                                || '\n' == m_input.m_line[0])
                            {
                                m_input.m_pushback_line = false;
                            }

                        return;
                    }

                if (0 == m_input.m_line_read)
                    {
                        continue;
                    }

                if ('%' == m_input.m_line[0])
                    {
                        /* Special lex declarations. */
                        if (1 >= m_input.m_line_read)
                            {
                                CError.parse_error(CError.E_DIRECT,
                                        m_input.m_line_number);
                                continue;
                            }

                        switch (m_input.m_line[1])
                            {
                            case '{':
                                if (0 == CUtility.charncmp(m_input.m_line,
                                        0,
                                        m_class_code_dir,
                                        0,
                                        m_class_code_dir.length - 1))
                                    {
                                        m_spec.m_class_code = packCode(m_class_code_dir,
                                                m_class_code_end_dir,
                                                m_spec.m_class_code,
                                                m_spec.m_class_read,
                                                CLASS_CODE);
                                        break;
                                    }

                                /* Bad directive. */
                                CError.parse_error(CError.E_DIRECT,
                                        m_input.m_line_number);
                                break;

                            case 'c':
                                if (0 == CUtility.charncmp(m_input.m_line,
                                        0,
                                        m_char_dir,
                                        0,
                                        m_char_dir.length - 1))
                                    {
                                        /* Set line counting to ON. */
                                        m_input.m_line_index = m_char_dir.length;
                                        m_spec.m_count_chars = true;
                                        break;
                                    }
                                else if (0 == CUtility.charncmp(m_input.m_line,
                                        0,
                                        m_class_dir,
                                        0,
                                        m_class_dir.length - 1))
                                    {
                                        m_input.m_line_index = m_class_dir.length;
                                        m_spec.m_class_name = getName();
                                        break;
                                    }
                                else if (0 == CUtility.charncmp(m_input.m_line,
                                        0,
                                        m_cup_dir,
                                        0,
                                        m_cup_dir.length - 1))
                                    {
                                        /* Set Java CUP compatibility to ON. */
                                        m_input.m_line_index = m_cup_dir.length;
                                        m_spec.m_cup_compatible = true;
                                        break;
                                    }

                                /* Bad directive. */
                                CError.parse_error(CError.E_DIRECT,
                                        m_input.m_line_number);
                                break;

                            case 'e':
                                if (0 == CUtility.charncmp(m_input.m_line,
                                        0,
                                        m_eof_code_dir,
                                        0,
                                        m_eof_code_dir.length - 1))
                                    {
                                        m_spec.m_eof_code = packCode(m_eof_code_dir,
                                                m_eof_code_end_dir,
                                                m_spec.m_eof_code,
                                                m_spec.m_eof_read,
                                                EOF_CODE);
                                        break;
                                    }
                                else if (0 == CUtility.charncmp(m_input.m_line,
                                        0,
                                        m_eof_value_code_dir,
                                        0,
                                        m_eof_value_code_dir.length - 1))
                                    {
                                        m_spec.m_eof_value_code = packCode(m_eof_value_code_dir,
                                                m_eof_value_code_end_dir,
                                                m_spec.m_eof_value_code,
                                                m_spec.m_eof_value_read,
                                                EOF_VALUE_CODE);
                                        break;
                                    }
                                else if (0 == CUtility.charncmp(m_input.m_line,
                                        0,
                                        m_eof_throw_code_dir,
                                        0,
                                        m_eof_throw_code_dir.length - 1))
                                    {
                                        m_spec.m_eof_throw_code = packCode(m_eof_throw_code_dir,
                                                m_eof_throw_code_end_dir,
                                                m_spec.m_eof_throw_code,
                                                m_spec.m_eof_throw_read,
                                                EOF_THROW_CODE);
                                        break;
                                    }

                                /* Bad directive. */
                                CError.parse_error(CError.E_DIRECT,
                                        m_input.m_line_number);
                                break;

                            case 'f':
                                if (0 == CUtility.charncmp(m_input.m_line,
                                        0,
                                        m_function_dir,
                                        0,
                                        m_function_dir.length - 1))
                                    {
                                        /* Set line counting to ON. */
                                        m_input.m_line_index = m_function_dir.length;
                                        m_spec.m_function_name = getName();
                                        break;
                                    }
                                else if (0 == CUtility.charncmp(m_input.m_line,
                                        0,
                                        m_full_dir,
                                        0,
                                        m_full_dir.length - 1))
                                    {
                                        m_input.m_line_index = m_full_dir.length;
                                        m_spec.m_dtrans_ncols = CUtility.MAX_EIGHT_BIT + 1;
                                        break;
                                    }

                                /* Bad directive. */
                                CError.parse_error(CError.E_DIRECT,
                                        m_input.m_line_number);
                                break;

                            case 'i':
                                if (0 == CUtility.charncmp(m_input.m_line,
                                        0,
                                        m_integer_dir,
                                        0,
                                        m_integer_dir.length - 1))
                                    {
                                        /* Set line counting to ON. */
                                        m_input.m_line_index = m_integer_dir.length;
                                        m_spec.m_integer_type = true;
                                        break;
                                    }
                                else if (0 == CUtility.charncmp(m_input.m_line,
                                        0,
                                        m_intwrap_dir,
                                        0,
                                        m_intwrap_dir.length - 1))
                                    {
                                        /* Set line counting to ON. */
                                        m_input.m_line_index = m_integer_dir.length;
                                        m_spec.m_intwrap_type = true;
                                        break;
                                    }
                                else if (0 == CUtility.charncmp(m_input.m_line,
                                        0,
                                        m_init_code_dir,
                                        0,
                                        m_init_code_dir.length - 1))
                                    {
                                        m_spec.m_init_code = packCode(m_init_code_dir,
                                                m_init_code_end_dir,
                                                m_spec.m_init_code,
                                                m_spec.m_init_read,
                                                INIT_CODE);
                                        break;
                                    }
                                else if (0 == CUtility.charncmp(m_input.m_line,
                                        0,
                                        m_init_throw_code_dir,
                                        0,
                                        m_init_throw_code_dir.length - 1))
                                    {
                                        m_spec.m_init_throw_code = packCode(m_init_throw_code_dir,
                                                m_init_throw_code_end_dir,
                                                m_spec.m_init_throw_code,
                                                m_spec.m_init_throw_read,
                                                INIT_THROW_CODE);
                                        break;
                                    }
                                else if (0 == CUtility.charncmp(m_input.m_line,
                                        0,
                                        m_implements_dir,
                                        0,
                                        m_implements_dir.length - 1))
                                    {
                                        m_input.m_line_index = m_implements_dir.length;
                                        m_spec.m_implements_name = getName();
                                        break;
                                    }
                                else if (0 == CUtility.charncmp(m_input.m_line,
                                        0,
                                        m_ignorecase_dir,
                                        0,
                                        m_ignorecase_dir.length-1))
                                    {
                                        /* Set m_ignorecase to ON. */
                                        m_input.m_line_index = m_ignorecase_dir.length;
                                        m_spec.m_ignorecase = true;
                                        break;
                                    }

                                /* Bad directive. */
                                CError.parse_error(CError.E_DIRECT,
                                        m_input.m_line_number);
                                break;

                            case 'l':
                                if (0 == CUtility.charncmp(m_input.m_line,
                                        0,
                                        m_line_dir,
                                        0,
                                        m_line_dir.length - 1))
                                    {
                                        /* Set line counting to ON. */
                                        m_input.m_line_index = m_line_dir.length;
                                        m_spec.m_count_lines = true;
                                        break;
                                    }

                                /* Bad directive. */
                                CError.parse_error(CError.E_DIRECT,
                                        m_input.m_line_number);
                                break;

                            case 'n':
                                if (0 == CUtility.charncmp(m_input.m_line,
                                        0,
                                        m_notunix_dir,
                                        0,
                                        m_notunix_dir.length - 1))
                                    {
                                        /* Set line counting to ON. */
                                        m_input.m_line_index = m_notunix_dir.length;
                                        m_spec.m_unix = false;
                                        break;
                                    }

                                /* Bad directive. */
                                CError.parse_error(CError.E_DIRECT,
                                        m_input.m_line_number);
                                break;

                            case 'p':
                                if (0 == CUtility.charncmp(m_input.m_line,
                                        0,
                                        m_public_dir,
                                        0,
                                        m_public_dir.length - 1))
                                    {
                                        /* Set public flag. */
                                        m_input.m_line_index = m_public_dir.length;
                                        m_spec.m_public = true;
                                        break;
                                    }

                                /* Bad directive. */
                                CError.parse_error(CError.E_DIRECT,
                                        m_input.m_line_number);
                                break;

                            case 's':
                                if (0 == CUtility.charncmp(m_input.m_line,
                                        0,
                                        m_state_dir,
                                        0,
                                        m_state_dir.length - 1))
                                    {
                                        /* Recognize state list. */
                                        m_input.m_line_index = m_state_dir.length;
                                        saveStates();
                                        break;
                                    }

                                /* Undefined directive. */
                                CError.parse_error(CError.E_DIRECT,
                                        m_input.m_line_number);
                                break;

                            case 't':
                                if (0 == CUtility.charncmp(m_input.m_line,
                                        0,
                                        m_type_dir,
                                        0,
                                        m_type_dir.length - 1))
                                    {
                                        /* Set Java CUP compatibility to ON. */
                                        m_input.m_line_index = m_type_dir.length;
                                        m_spec.m_type_name = getName();
                                        break;
                                    }

                                /* Undefined directive. */
                                CError.parse_error(CError.E_DIRECT,
                                        m_input.m_line_number);
                                break;

                            case 'u':
                                if (0 == CUtility.charncmp(m_input.m_line,
                                        0,
                                        m_unicode_dir,
                                        0,
                                        m_unicode_dir.length - 1))
                                    {
                                        m_input.m_line_index = m_unicode_dir.length;
                                        /* UNDONE: What to do here? */
                                        break;
                                    }

                                /* Bad directive. */
                                CError.parse_error(CError.E_DIRECT,
                                        m_input.m_line_number);
                                break;

                            case 'y':
                                if (0 == CUtility.charncmp(m_input.m_line,
                                        0,
                                        m_yyeof_dir,
                                        0,
                                        m_yyeof_dir.length - 1))
                                    {
                                        m_input.m_line_index = m_yyeof_dir.length;
                                        m_spec.m_yyeof = true;
                                        break;
                                    } else if (0 == CUtility.charncmp(m_input.m_line,
                                            0,
                                            m_yylex_throw_code_dir,
                                            0,
                                            m_yylex_throw_code_dir.length - 1))
                                        {
                                            m_spec.m_yylex_throw_code = packCode(m_yylex_throw_code_dir,
                                                    m_yylex_throw_code_end_dir,
                                                    m_spec.m_yylex_throw_code,
                                                    m_spec.m_yylex_throw_read,
                                                    YYLEX_THROW_CODE);
                                            break;
                                        }


                                /* Bad directive. */
                                CError.parse_error(CError.E_DIRECT,
                                        m_input.m_line_number);
                                break;

                            default:
                                /* Undefined directive. */
                                CError.parse_error(CError.E_DIRECT,
                                        m_input.m_line_number);
                                break;
                            }
                    }
                else
                    {
                        /* Regular expression macro. */
                        m_input.m_line_index = 0;
                        saveMacro();
                    }

                if (CUtility.OLD_DEBUG)
                    {
                        System.out.println("Line number "
                                + m_input.m_line_number + ":");
                        System.out.print(new String(m_input.m_line,
                                0,m_input.m_line_read));
                    }
            }
    }

    /***************************************************************
    Function: userRules
    Description: Processes third section of JLex
    specification and creates minimized transition table.
    **************************************************************/
    private void userRules
    (
     )
            throws java.io.IOException
    {

        if (false == m_init_flag)
            {
                CError.parse_error(CError.E_INIT,0);
            }

        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != this);
                CUtility.cuassert(null != m_outstream);
                CUtility.cuassert(null != m_input);
                CUtility.cuassert(null != m_tokens);
                CUtility.cuassert(null != m_spec);
            }

        /* UNDONE: Need to handle states preceding rules. */

        if (m_spec.m_verbose)
            {
                System.out.println("Creating NFA machine representation.");
            }
        m_makeNfa.thompson(this,m_spec,m_input);

        /*print_nfa();*/

        if (CUtility.DEBUG)
            {
                CUtility.cuassert(END_OF_INPUT == m_spec.m_current_token);
            }

        if (m_spec.m_verbose)
            {
                System.out.println("Creating DFA transition table.");
            }
        m_nfa2dfa.make_dfa(this,m_spec);

        if (CUtility.FOODEBUG) {
            print_header();
        }

        if (m_spec.m_verbose)
            {
                System.out.println("Minimizing DFA transition table.");
            }
        m_minimize.min_dfa(m_spec);
    }

    /***************************************************************
    Function: printccl
    Description: Debugging routine that outputs readable form
    of character class.
    **************************************************************/
    private void printccl
    (
            CSet set
            )
    {
        int i;

        System.out.print(" [");
        for (i = 0; i < m_spec.m_dtrans_ncols; ++i)
            {
                if (set.contains(i))
                    {
                        System.out.print(interp_int(i));
                    }
            }
        System.out.print(']');
    }

    /***************************************************************
    Function: plab
    Description:
    **************************************************************/
    private String plab
    (
            CNfa state
            )
    {
        int index;

        if (null == state)
            {
                return (new String("--"));
            }

        index = m_spec.m_nfa_states.indexOf(state);

        return ((new Integer(index)).toString());
    }

    /***************************************************************
    Function: interp_int
    Description:
    **************************************************************/
    private String interp_int
    (
            int i
            )
    {
        switch (i)
            {
            case (int) '\b':
                return (new String("\\b"));

            case (int) '\t':
                return (new String("\\t"));

            case (int) '\n':
                return (new String("\\n"));

            case (int) '\f':
                return (new String("\\f"));

            case (int) '\r':
                return (new String("\\r"));

            case (int) ' ':
                return (new String("\\ "));

            default:
                return ((new Character((char) i)).toString());
            }
    }

    /***************************************************************
    Function: print_nfa
    Description:
    **************************************************************/
    void print_nfa
    (
     )
    {
        int elem;
        CNfa nfa;
        int size;
        Enumeration states;
        Integer index;
        int i;
        int j;
        int vsize;
        String state;

        System.out.println("--------------------- NFA -----------------------");

        size = m_spec.m_nfa_states.size();
        for (elem = 0; elem < size; ++elem)
            {
                nfa = (CNfa) m_spec.m_nfa_states.elementAt(elem);

                System.out.print("Nfa state " + plab(nfa) + ": ");

                if (null == nfa.m_next)
                    {
                        System.out.print("(TERMINAL)");
                    }
                else
                    {
                        System.out.print("--> " + plab(nfa.m_next));
                        System.out.print("--> " + plab(nfa.m_next2));

                        switch (nfa.m_edge)
                            {
                            case CNfa.CCL:
                                printccl(nfa.m_set);
                                break;

                            case CNfa.EPSILON:
                                System.out.print(" EPSILON ");
                                break;

                            default:
                                System.out.print(" " + interp_int(nfa.m_edge));
                                break;
                            }
                    }

                if (0 == elem)
                    {
                        System.out.print(" (START STATE)");
                    }

                if (null != nfa.m_accept)
                    {
                        System.out.print(" accepting "
                                + ((0 != (nfa.m_anchor & CSpec.START)) ? "^" : "")
                                + "<"
                                + (new String(nfa.m_accept.m_action,0,
                                        nfa.m_accept.m_action_read))
                                + ">"
                                + ((0 != (nfa.m_anchor & CSpec.END)) ? "$" : ""));
                    }

                System.out.println("");
            }

        states = m_spec.m_states.keys();
        while (states.hasMoreElements())
            {
                state = (String) states.nextElement();
                index = (Integer) m_spec.m_states.get(state);

                if (CUtility.DEBUG)
                    {
                        CUtility.cuassert(null != state);
                        CUtility.cuassert(null != index);
                    }

                System.out.println("State \"" + state
                        + "\" has identifying index "
                        + index.toString() + ".");
                System.out.print("\tStart states of matching rules: ");

                i = index.intValue();
                vsize = m_spec.m_state_rules[i].size();

                for (j = 0; j < vsize; ++j)
                    {
                        nfa = (CNfa) m_spec.m_state_rules[i].elementAt(j);

                        System.out.print(m_spec.m_nfa_states.indexOf(nfa) + " ");
                    }

                System.out.println("");
            }

        System.out.println("-------------------- NFA ----------------------");
    }

    /***************************************************************
    Function: getStates
    Description: Parses the state area of a rule,
    from the beginning of a line.
    < state1, state2 ... > regular_expression { action }
    Returns null on only EOF.  Returns all_states,
    initialied properly to correspond to all states,
    if no states are found.
    Special Notes: This function treats commas as optional
    and permits states to be spread over multiple lines.
    **************************************************************/
    private JavaLexBitSet all_states = null;
    JavaLexBitSet getStates
    (
     )
            throws java.io.IOException
    {
        int start_state;
        int count_state;
        JavaLexBitSet states;
        String name;
        Integer index;
        int i;
        int size;

        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != this);
                CUtility.cuassert(null != m_outstream);
                CUtility.cuassert(null != m_input);
                CUtility.cuassert(null != m_tokens);
                CUtility.cuassert(null != m_spec);
            }

        states = null;

        /* Skip white space. */
        while (CUtility.isspace(m_input.m_line[m_input.m_line_index]))
            {
                ++m_input.m_line_index;

                while (m_input.m_line_index >= m_input.m_line_read)
                    {
                        /* Must just be an empty line. */
                        if (m_input.getLine())
                            {
                                /* EOF found. */
                                return null;
                            }
                    }
            }

        /* Look for states. */
        if ('<' == m_input.m_line[m_input.m_line_index])
            {
                ++m_input.m_line_index;

                states = new JavaLexBitSet();

                /* Parse states. */
                while (true)
                    {
                        /* We may have reached the end of the line. */
                        while (m_input.m_line_index >= m_input.m_line_read)
                            {
                                if (m_input.getLine())
                                    {
                                        /* EOF found. */
                                        CError.parse_error(CError.E_EOF,m_input.m_line_number);
                                        return states;
                                    }
                            }

                        while (true)
                            {
                                /* Skip white space. */
                                while (CUtility.isspace(m_input.m_line[m_input.m_line_index]))
                                    {
                                        ++m_input.m_line_index;

                                        while (m_input.m_line_index >= m_input.m_line_read)
                                            {
                                                if (m_input.getLine())
                                                    {
                                /* EOF found. */
                                                        CError.parse_error(CError.E_EOF,m_input.m_line_number);
                                                        return states;
                                                    }
                                            }
                                    }

                                if (',' != m_input.m_line[m_input.m_line_index])
                                    {
                                        break;
                                    }

                                ++m_input.m_line_index;
                            }

                        if ('>' == m_input.m_line[m_input.m_line_index])
                            {
                                ++m_input.m_line_index;
                                if (m_input.m_line_index < m_input.m_line_read)
                                    {
                                        m_advance_stop = true;
                                    }
                                return states;
                            }

                        /* Read in state name. */
                        start_state = m_input.m_line_index;
                        while (false == CUtility.isspace(m_input.m_line[m_input.m_line_index])
                                && ',' != m_input.m_line[m_input.m_line_index]
                                && '>' != m_input.m_line[m_input.m_line_index])
                            {
                                ++m_input.m_line_index;

                                if (m_input.m_line_index >= m_input.m_line_read)
                                    {
                                        /* End of line means end of state name. */
                                        break;
                                    }
                            }
                        count_state = m_input.m_line_index - start_state;

                        /* Save name after checking definition. */
                        name = new String(m_input.m_line,
                                start_state,
                                count_state);
                        index = (Integer) m_spec.m_states.get(name);
                        if (null == index)
                            {
                                /* Uninitialized state. */
                                System.out.println("Uninitialized State Name: " + name);
                                CError.parse_error(CError.E_STATE,m_input.m_line_number);
                            }
                        states.set(index.intValue());
                    }
            }

        if (null == all_states)
            {
                all_states = new JavaLexBitSet();

                size = m_spec.m_states.size();
                for (i = 0; i < size; ++i)
                    {
                        all_states.set(i);
                    }
            }

        if (m_input.m_line_index < m_input.m_line_read)
            {
                m_advance_stop = true;
            }
        return all_states;
    }

    /********************************************************
    Function: expandMacro
    Description: Returns false on error, true otherwise.
    *******************************************************/
    private boolean expandMacro
    (
     )
    {
        int elem;
        int start_macro;
        int end_macro;
        int start_name;
        int count_name;
        String def;
        int def_elem;
        String name;
        char replace[];
        int rep_elem;

        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != this);
                CUtility.cuassert(null != m_outstream);
                CUtility.cuassert(null != m_input);
                CUtility.cuassert(null != m_tokens);
                CUtility.cuassert(null != m_spec);
            }

        /* Check for macro. */
        if ('{' != m_input.m_line[m_input.m_line_index])
            {
                CError.parse_error(CError.E_INTERNAL,m_input.m_line_number);
                return ERROR;
            }

        start_macro = m_input.m_line_index;
        elem = m_input.m_line_index + 1;
        if (elem >= m_input.m_line_read)
            {
                CError.impos("Unfinished macro name");
                return ERROR;
            }

        /* Get macro name. */
        start_name = elem;
        while ('}' != m_input.m_line[elem])
            {
                ++elem;
                if (elem >= m_input.m_line_read)
                    {
                        CError.impos("Unfinished macro name at line "
                                + m_input.m_line_number);
                        return ERROR;
                    }
            }
        count_name = elem - start_name;
        end_macro = elem;

        /* Check macro name. */
        if (0 == count_name)
            {
                CError.impos("Nonexistent macro name");
                return ERROR;
            }

        /* Debug checks. */
        if (CUtility.DEBUG)
            {
                CUtility.cuassert(0 < count_name);
            }

        /* Retrieve macro definition. */
        name = new String(m_input.m_line,start_name,count_name);
        def = (String) m_spec.m_macros.get(name);
        if (null == def)
            {
                /*CError.impos("Undefined macro \"" + name + "\".");*/
                System.out.println("Error: Undefined macro \"" + name + "\".");
                CError.parse_error(CError.E_NOMAC, m_input.m_line_number);
                return ERROR;
            }
        if (CUtility.OLD_DUMP_DEBUG)
            {
                System.out.println("expanded escape: " + def);
            }

        /* Replace macro in new buffer,
           beginning by copying first part of line buffer. */
        replace = new char[m_input.m_line.length];
        for (rep_elem = 0; rep_elem < start_macro; ++rep_elem)
            {
                replace[rep_elem] = m_input.m_line[rep_elem];

                if (CUtility.DEBUG)
                    {
                        CUtility.cuassert(rep_elem < replace.length);
                    }
            }

        /* Copy macro definition. */
        if (rep_elem >= replace.length)
            {
                replace = CUtility.doubleSize(replace);
            }
        for (def_elem = 0; def_elem < def.length(); ++def_elem)
            {
                replace[rep_elem] = def.charAt(def_elem);

                ++rep_elem;
                if (rep_elem >= replace.length)
                    {
                        replace = CUtility.doubleSize(replace);
                    }
            }

        /* Copy last part of line. */
        if (rep_elem >= replace.length)
            {
                replace = CUtility.doubleSize(replace);
            }
        for (elem = end_macro + 1; elem < m_input.m_line_read; ++elem)
            {
                replace[rep_elem] = m_input.m_line[elem];

                ++rep_elem;
                if (rep_elem >= replace.length)
                    {
                        replace = CUtility.doubleSize(replace);
                    }
            }

        /* Replace buffer. */
        m_input.m_line = replace;
        m_input.m_line_read = rep_elem;

        if (CUtility.OLD_DEBUG)
            {
                System.out.println(new String(m_input.m_line,0,m_input.m_line_read));
            }
        return NOT_ERROR;
    }

    /***************************************************************
    Function: saveMacro
    Description: Saves macro definition of form:
    macro_name = macro_definition
    **************************************************************/
    private void saveMacro
    (
     )
    {
        int elem;
        int start_name;
        int count_name;
        int start_def;
        int count_def;
        boolean saw_escape;
        boolean in_quote;
        boolean in_ccl;

        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != this);
                CUtility.cuassert(null != m_outstream);
                CUtility.cuassert(null != m_input);
                CUtility.cuassert(null != m_tokens);
                CUtility.cuassert(null != m_spec);
            }

        /* Macro declarations are of the following form:
           macro_name macro_definition */

        elem = 0;

        /* Skip white space preceding macro name. */
        while (CUtility.isspace(m_input.m_line[elem]))
            {
                ++elem;
                if (elem >= m_input.m_line_read)
                    {
                        /* End of line has been reached,
                           and line was found to be empty. */
                        return;
                    }
            }

        /* Read macro name. */
        start_name = elem;
        while (false == CUtility.isspace(m_input.m_line[elem])
                && '=' != m_input.m_line[elem])
            {
                ++elem;
                if (elem >= m_input.m_line_read)
                    {
                        /* Macro name but no associated definition. */
                        CError.parse_error(CError.E_MACDEF,m_input.m_line_number);
                    }
            }
        count_name = elem - start_name;

        /* Check macro name. */
        if (0 == count_name)
            {
                /* Nonexistent macro name. */
                CError.parse_error(CError.E_MACDEF,m_input.m_line_number);
            }

        /* Skip white space between name and definition. */
        while (CUtility.isspace(m_input.m_line[elem]))
            {
                ++elem;
                if (elem >= m_input.m_line_read)
                    {
                        /* Macro name but no associated definition. */
                        CError.parse_error(CError.E_MACDEF,m_input.m_line_number);
                    }
            }

        if ('=' == m_input.m_line[elem])
            {
                ++elem;
                if (elem >= m_input.m_line_read)
                    {
                        /* Macro name but no associated definition. */
                        CError.parse_error(CError.E_MACDEF,m_input.m_line_number);
                    }
            }

        /* Skip white space between name and definition. */
        while (CUtility.isspace(m_input.m_line[elem]))
            {
                ++elem;
                if (elem >= m_input.m_line_read)
                    {
                        /* Macro name but no associated definition. */
                        CError.parse_error(CError.E_MACDEF,m_input.m_line_number);
                    }
            }

        /* Read macro definition. */
        start_def = elem;
        in_quote = false;
        in_ccl = false;
        saw_escape = false;
        while (false == CUtility.isspace(m_input.m_line[elem])
                || true == in_quote
                || true == in_ccl
                || true == saw_escape)
            {
                if ('\"' == m_input.m_line[elem] && false == saw_escape)
                    {
                        in_quote = !in_quote;
                    }

                if ('\\' == m_input.m_line[elem] && false == saw_escape)
                    {
                        saw_escape = true;
                    }
                else
                    {
                        saw_escape = false;
                    }
                if (false == saw_escape && false == in_quote) { // CSA, 24-jul-99
                    if ('[' == m_input.m_line[elem] && false == in_ccl)
                        in_ccl = true;
                    if (']' == m_input.m_line[elem] && true == in_ccl)
                        in_ccl = false;
                }

                ++elem;
                if (elem >= m_input.m_line_read)
                    {
                        /* End of line. */
                        break;
                    }
            }
        count_def = elem - start_def;

        /* Check macro definition. */
        if (0 == count_def)
            {
                /* Nonexistent macro name. */
                CError.parse_error(CError.E_MACDEF,m_input.m_line_number);
            }

        /* Debug checks. */
        if (CUtility.DEBUG)
            {
                CUtility.cuassert(0 < count_def);
                CUtility.cuassert(0 < count_name);
                CUtility.cuassert(null != m_spec.m_macros);
            }

        if (CUtility.OLD_DEBUG)
            {
                System.out.println("macro name \""
                        + new String(m_input.m_line,start_name,count_name)
                            + "\".");
                System.out.println("macro definition \""
                        + new String(m_input.m_line,start_def,count_def)
                            + "\".");
            }

        /* Add macro name and definition to table. */
        m_spec.m_macros.put(new String(m_input.m_line,start_name,count_name),
                new String(m_input.m_line,start_def,count_def));
    }

    /***************************************************************
    Function: saveStates
    Description: Takes state declaration and makes entries
    for them in state hashtable in CSpec structure.
    State declaration should be of the form:
    %state name0[, name1, name2 ...]
    (But commas are actually optional as long as there is
    white space in between them.)
    **************************************************************/
    private void saveStates
    (
     )
    {
        int start_state;
        int count_state;

        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != this);
                CUtility.cuassert(null != m_outstream);
                CUtility.cuassert(null != m_input);
                CUtility.cuassert(null != m_tokens);
                CUtility.cuassert(null != m_spec);
            }

        /* EOF found? */
        if (m_input.m_eof_reached)
            {
                return;
            }

        /* Debug checks. */
        if (CUtility.DEBUG)
            {
                CUtility.cuassert('%' == m_input.m_line[0]);
                CUtility.cuassert('s' == m_input.m_line[1]);
                CUtility.cuassert(m_input.m_line_index <= m_input.m_line_read);
                CUtility.cuassert(0 <= m_input.m_line_index);
                CUtility.cuassert(0 <= m_input.m_line_read);
            }

        /* Blank line?  No states? */
        if (m_input.m_line_index >= m_input.m_line_read)
            {
                return;
            }

        while (m_input.m_line_index < m_input.m_line_read)
            {
                if (CUtility.OLD_DEBUG)
                    {
                        System.out.println("line read " + m_input.m_line_read
                                + "\tline index = " + m_input.m_line_index);
                    }

                /* Skip white space. */
                while (CUtility.isspace(m_input.m_line[m_input.m_line_index]))
                    {
                        ++m_input.m_line_index;
                        if (m_input.m_line_index >= m_input.m_line_read)
                            {
                                /* No more states to be found. */
                                return;
                            }
                    }

                /* Look for state name. */
                start_state = m_input.m_line_index;
                while (false == CUtility.isspace(m_input.m_line[m_input.m_line_index])
                        && ',' != m_input.m_line[m_input.m_line_index])
                    {
                        ++m_input.m_line_index;
                        if (m_input.m_line_index >= m_input.m_line_read)
                            {
                                /* End of line and end of state name. */
                                break;
                            }
                    }
                count_state = m_input.m_line_index - start_state;

                if (CUtility.OLD_DEBUG)
                    {
                        System.out.println("State name \""
                                + new String(m_input.m_line,start_state,count_state)
                                    + "\".");
                        System.out.println("Integer index \""
                                + m_spec.m_states.size()
                                + "\".");
                    }

                /* Enter new state name, along with unique index. */
                m_spec.m_states.put(new String(m_input.m_line,start_state,count_state),
                        new Integer(m_spec.m_states.size()));

                /* Skip comma. */
                if (',' == m_input.m_line[m_input.m_line_index])
                    {
                        ++m_input.m_line_index;
                        if (m_input.m_line_index >= m_input.m_line_read)
                            {
                                /* End of line. */
                                return;
                            }
                    }
            }
    }

    /********************************************************
    Function: expandEscape
    Description: Takes escape sequence and returns
    corresponding character code.
    *******************************************************/
    private char expandEscape
    (
     )
    {
        char r;

        /* Debug checks. */
        if (CUtility.DEBUG)
            {
                CUtility.cuassert(m_input.m_line_index < m_input.m_line_read);
                CUtility.cuassert(0 < m_input.m_line_read);
                CUtility.cuassert(0 <= m_input.m_line_index);
            }

        if ('\\' != m_input.m_line[m_input.m_line_index])
            {
                ++m_input.m_line_index;
                return m_input.m_line[m_input.m_line_index - 1];
            }
        else
            {
                ++m_input.m_line_index;
                switch (CUtility.toupper(m_input.m_line[m_input.m_line_index]))
                    {
                    case 'B':
                        ++m_input.m_line_index;
                        return '\b';

                    case 'T':
                        ++m_input.m_line_index;
                        return '\t';

                    case 'N':
                        ++m_input.m_line_index;
                        return '\n';

                    case 'F':
                        ++m_input.m_line_index;
                        return '\f';

                    case 'R':
                        ++m_input.m_line_index;
                        return '\r';

                    case '^':
                        ++m_input.m_line_index;
                        r = (char) (CUtility.toupper(m_input.m_line[m_input.m_line_index])
                                - '@');
                        ++m_input.m_line_index;
                        return r;

                    case 'X':
                        ++m_input.m_line_index;
                        r = 0;
                        if (CUtility.ishexdigit(m_input.m_line[m_input.m_line_index]))
                            {
                                r = CUtility.hex2bin(m_input.m_line[m_input.m_line_index]);
                                ++m_input.m_line_index;
                            }
                        if (CUtility.ishexdigit(m_input.m_line[m_input.m_line_index]))
                            {
                                r = (char) (r << 4);
                                r = (char) (r | CUtility.hex2bin(m_input.m_line[m_input.m_line_index]));
                                ++m_input.m_line_index;
                            }
                        if (CUtility.ishexdigit(m_input.m_line[m_input.m_line_index]))
                            {
                                r = (char) (r << 4);
                                r = (char) (r | CUtility.hex2bin(m_input.m_line[m_input.m_line_index]));
                                ++m_input.m_line_index;
                            }
                        return r;

                    default:
                        if (false == CUtility.isoctdigit(m_input.m_line[m_input.m_line_index]))
                            {
                                r = m_input.m_line[m_input.m_line_index];
                                ++m_input.m_line_index;
                            }
                        else
                            {
                                r = CUtility.oct2bin(m_input.m_line[m_input.m_line_index]);
                                ++m_input.m_line_index;

                                if (CUtility.isoctdigit(m_input.m_line[m_input.m_line_index]))
                                    {
                                        r = (char) (r << 3);
                                        r = (char) (r | CUtility.oct2bin(m_input.m_line[m_input.m_line_index]));
                                        ++m_input.m_line_index;
                                    }

                                if (CUtility.isoctdigit(m_input.m_line[m_input.m_line_index]))
                                    {
                                        r = (char) (r << 3);
                                        r = (char) (r | CUtility.oct2bin(m_input.m_line[m_input.m_line_index]));
                                        ++m_input.m_line_index;
                                    }
                            }
                        return r;
                    }
            }
    }

    /********************************************************
    Function: packAccept
    Description: Packages and returns CAccept
    for action next in input stream.
    *******************************************************/
    CAccept packAccept
    (
     )
            throws java.io.IOException
    {
        CAccept accept;
        char action[];
        int action_index;
        int brackets;
        boolean inquotes;
        boolean instarcomment;
        boolean inslashcomment;
        boolean escaped;
        boolean slashed;

        action = new char[BUFFER_SIZE];
        action_index = 0;

        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != this);
                CUtility.cuassert(null != m_outstream);
                CUtility.cuassert(null != m_input);
                CUtility.cuassert(null != m_tokens);
                CUtility.cuassert(null != m_spec);
            }

        /* Get a new line, if needed. */
        while (m_input.m_line_index >= m_input.m_line_read)
            {
                if (m_input.getLine())
                    {
                        CError.parse_error(CError.E_EOF,m_input.m_line_number);
                        return null;
                    }
            }

        /* Look for beginning of action. */
        while (CUtility.isspace(m_input.m_line[m_input.m_line_index]))
            {
                ++m_input.m_line_index;

                /* Get a new line, if needed. */
                while (m_input.m_line_index >= m_input.m_line_read)
                    {
                        if (m_input.getLine())
                            {
                                CError.parse_error(CError.E_EOF,m_input.m_line_number);
                                return null;
                            }
                    }
            }

        /* Look for brackets. */
        if ('{' != m_input.m_line[m_input.m_line_index])
            {
                CError.parse_error(CError.E_BRACE,m_input.m_line_number);
            }

        /* Copy new line into action buffer. */
        brackets = 0;
        inquotes = inslashcomment = instarcomment = false;
        escaped  = slashed = false;
        while (true)
            {
                action[action_index] = m_input.m_line[m_input.m_line_index];

                /* Look for quotes. */
                if (inquotes && escaped)
                    escaped=false; // only protects one char, but this is enough.
                else if (inquotes && '\\' == m_input.m_line[m_input.m_line_index])
                    escaped=true;
                else if ('\"' == m_input.m_line[m_input.m_line_index])
                    inquotes=!inquotes; // unescaped quote.
                /* Look for comments. */
                if (instarcomment) { // inside "/*" comment; look for "*/"
                    if (slashed && '/' == m_input.m_line[m_input.m_line_index])
                        instarcomment = slashed = false;
                    else // note that inside a star comment, slashed means starred
                        slashed = ('*' == m_input.m_line[m_input.m_line_index]);
                } else if (!inslashcomment) { // not in comment, look for /* or //
                    inslashcomment =
                        (slashed && '/' == m_input.m_line[m_input.m_line_index]);
                    instarcomment =
                        (slashed && '*' == m_input.m_line[m_input.m_line_index]);
                    slashed = ('/' == m_input.m_line[m_input.m_line_index]);
                }

                /* Look for brackets. */
                if (!inquotes && !instarcomment && !inslashcomment) {
                    if ('{' == m_input.m_line[m_input.m_line_index])
                        {
                            ++brackets;
                        }
                    else if ('}' == m_input.m_line[m_input.m_line_index])
                        {
                            --brackets;

                            if (0 == brackets)
                                {
                                    ++action_index;
                                    ++m_input.m_line_index;

                                    break;
                                }
                        }
                }

                ++action_index;
                /* Double the buffer size, if needed. */
                if (action_index >= action.length)
                    {
                        action = CUtility.doubleSize(action);
                    }

                ++m_input.m_line_index;
                /* Get a new line, if needed. */
                while (m_input.m_line_index >= m_input.m_line_read)
                    {
                        inslashcomment = slashed = false;
                        if (inquotes) { // non-fatal
                            CError.parse_error(CError.E_NEWLINE,m_input.m_line_number);
                            inquotes=false;
                        }
                        if (m_input.getLine())
                            {
                                CError.parse_error(CError.E_SYNTAX,m_input.m_line_number);
                                return null;
                            }
                    }
            }

        accept = new CAccept(action,action_index,m_input.m_line_number);

        if (CUtility.DEBUG)
            {
                CUtility.cuassert(null != accept);
            }

        if (CUtility.DESCENT_DEBUG)
            {
                System.out.print("Accepting action:");
                System.out.println(new String(accept.m_action,0,accept.m_action_read));
            }

        return accept;
    }

    /********************************************************
    Function: advance
    Description: Returns code for next token.
    *******************************************************/
    private boolean m_advance_stop = false;
    int advance
    (
     )
            throws java.io.IOException
    {
        boolean saw_escape = false;
        Integer code;

        /*if (m_input.m_line_index > m_input.m_line_read) {
          System.out.println("m_input.m_line_index = " + m_input.m_line_index);
          System.out.println("m_input.m_line_read = " + m_input.m_line_read);
          CUtility.cuassert(m_input.m_line_index <= m_input.m_line_read);
          }*/

        if (m_input.m_eof_reached)
            {
                /* EOF has already been reached,
                   so return appropriate code. */

                m_spec.m_current_token = END_OF_INPUT;
                m_spec.m_lexeme = '\0';
                return m_spec.m_current_token;
            }

        /* End of previous regular expression?
           Refill line buffer? */
        if (EOS == m_spec.m_current_token
                /* ADDED */
                || m_input.m_line_index >= m_input.m_line_read)
            /* ADDED */
            {
                if (m_spec.m_in_quote)
                    {
                        CError.parse_error(CError.E_SYNTAX,m_input.m_line_number);
                    }

                while (true)
                    {
                        if (false == m_advance_stop
                                || m_input.m_line_index >= m_input.m_line_read)
                            {
                                if (m_input.getLine())
                                    {
                                        /* EOF has already been reached,
                                           so return appropriate code. */

                                        m_spec.m_current_token = END_OF_INPUT;
                                        m_spec.m_lexeme = '\0';
                                        return m_spec.m_current_token;
                                    }
                                m_input.m_line_index = 0;
                            }
                        else
                            {
                                m_advance_stop = false;
                            }

                        while (m_input.m_line_index < m_input.m_line_read
                                && true == CUtility.isspace(m_input.m_line[m_input.m_line_index]))
                            {
                                ++m_input.m_line_index;
                            }

                        if (m_input.m_line_index < m_input.m_line_read)
                            {
                                break;
                            }
                    }
            }

        if (CUtility.DEBUG) {
            CUtility.cuassert(m_input.m_line_index <= m_input.m_line_read);
        }

        while (true)
            {
                if (false == m_spec.m_in_quote
                        && '{' == m_input.m_line[m_input.m_line_index])
                    {
                        if (false == expandMacro())
                            {
                                break;
                            }

                        if (m_input.m_line_index >= m_input.m_line_read)
                            {
                                m_spec.m_current_token = EOS;
                                m_spec.m_lexeme = '\0';
                                return m_spec.m_current_token;
                            }
                    }
                else if ('\"' == m_input.m_line[m_input.m_line_index])
                    {
                        m_spec.m_in_quote = !m_spec.m_in_quote;
                        ++m_input.m_line_index;

                        if (m_input.m_line_index >= m_input.m_line_read)
                            {
                                m_spec.m_current_token = EOS;
                                m_spec.m_lexeme = '\0';
                                return m_spec.m_current_token;
                            }
                    }
                else
                    {
                        break;
                    }
            }

        if (m_input.m_line_index > m_input.m_line_read) {
            System.out.println("m_input.m_line_index = " + m_input.m_line_index);
            System.out.println("m_input.m_line_read = " + m_input.m_line_read);
            CUtility.cuassert(m_input.m_line_index <= m_input.m_line_read);
        }

        /* Look for backslash, and corresponding
           escape sequence. */
        if ('\\' == m_input.m_line[m_input.m_line_index])
            {
                saw_escape = true;
            }
        else
            {
                saw_escape = false;
            }

        if (false == m_spec.m_in_quote)
            {
                if (false == m_spec.m_in_ccl &&
                        CUtility.isspace(m_input.m_line[m_input.m_line_index]))
                    {
                        /* White space means the end of
                           the current regular expression. */

                        m_spec.m_current_token = EOS;
                        m_spec.m_lexeme = '\0';
                        return m_spec.m_current_token;
                    }

                /* Process escape sequence, if needed. */
                if (saw_escape)
                    {
                        m_spec.m_lexeme = expandEscape();
                    }
                else
                    {
                        m_spec.m_lexeme = m_input.m_line[m_input.m_line_index];
                        ++m_input.m_line_index;
                    }
            }
        else
            {
                if (saw_escape
                        && (m_input.m_line_index + 1) < m_input.m_line_read
                        && '\"' == m_input.m_line[m_input.m_line_index + 1])
                    {
                        m_spec.m_lexeme = '\"';
                        m_input.m_line_index = m_input.m_line_index + 2;
                    }
                else
                    {
                        m_spec.m_lexeme = m_input.m_line[m_input.m_line_index];
                        ++m_input.m_line_index;
                    }
            }

        code = (Integer) m_tokens.get(new Character(m_spec.m_lexeme));
        if (m_spec.m_in_quote || true == saw_escape)
            {
                m_spec.m_current_token = L;
            }
        else
            {
                if (null == code)
                    {
                        m_spec.m_current_token = L;
                    }
                else
                    {
                        m_spec.m_current_token = code.intValue();
                    }
            }

        if (CCL_START == m_spec.m_current_token) m_spec.m_in_ccl = true;
        if (CCL_END   == m_spec.m_current_token) m_spec.m_in_ccl = false;

        if (CUtility.FOODEBUG)
            {
                System.out.println("Lexeme: " + m_spec.m_lexeme
                        + "\tToken: " + m_spec.m_current_token
                        + "\tIndex: " + m_input.m_line_index);
            }

        return m_spec.m_current_token;
    }

    /***************************************************************
    Function: details
    Description: High level debugging routine.
    **************************************************************/
    private void details
    (
     )
    {
        Enumeration names;
        String name;
        String def;
        Enumeration states;
        String state;
        Integer index;
 
        System.out.println("\n\t** Macros **");
        names = m_spec.m_macros.keys();
        while (names.hasMoreElements())
            {
                name = (String) names.nextElement();
                def = (String) m_spec.m_macros.get(name);

                if (CUtility.DEBUG)
                    {
                        CUtility.cuassert(null != name);
                        CUtility.cuassert(null != def);
                    }

                System.out.println("Macro name \"" + name
                        + "\" has definition \""
                        + def + "\".");
            }

        System.out.println("\n\t** States **");
        states = m_spec.m_states.keys();
        while (states.hasMoreElements())
            {
                state = (String) states.nextElement();
                index = (Integer) m_spec.m_states.get(state);

                if (CUtility.DEBUG)
                    {
                        CUtility.cuassert(null != state);
                        CUtility.cuassert(null != index);
                    }

                System.out.println("State \"" + state
                        + "\" has identifying index "
                        + index.toString() + ".");
            }

        System.out.println("\n\t** Character Counting **");
        if (false == m_spec.m_count_chars)
            {
                System.out.println("Character counting is off.");
            }
        else
            {
                if (CUtility.DEBUG)
                    {
                        CUtility.cuassert(m_spec.m_count_lines);
                    }

                System.out.println("Character counting is on.");
            }

        System.out.println("\n\t** Line Counting **");
        if (false == m_spec.m_count_lines)
            {
                System.out.println("Line counting is off.");
            }
        else
            {
                if (CUtility.DEBUG)
                    {
                        CUtility.cuassert(m_spec.m_count_lines);
                    }

                System.out.println("Line counting is on.");
            }

        System.out.println("\n\t** Operating System Specificity **");
        if (false == m_spec.m_unix)
            {
                System.out.println("Not generating UNIX-specific code.");
                System.out.println("(This means that \"\\r\\n\" is a "
                        + "newline, rather than \"\\n\".)");
            }
        else
            {
                System.out.println("Generating UNIX-specific code.");
                System.out.println("(This means that \"\\n\" is a "
                        + "newline, rather than \"\\r\\n\".)");
            }

        System.out.println("\n\t** Java CUP Compatibility **");
        if (false == m_spec.m_cup_compatible)
            {
                System.out.println("Generating CUP compatible code.");
                System.out.println("(No current results.)");
            }
        else
            {
                System.out.println("Not generating CUP compatible code.");
                System.out.println("(No current results.)");
            }

        if (CUtility.FOODEBUG) {
            if (null != m_spec.m_nfa_states && null != m_spec.m_nfa_start)
                {
                    System.out.println("\n\t** NFA machine **");
                    print_nfa();
                }
        }

        if (null != m_spec.m_dtrans_vector)
            {
                System.out.println("\n\t** DFA transition table **");
                /*print_header();*/
            }

        /*if (null != m_spec.m_accept_vector && null != m_spec.m_anchor_array)
          {
          System.out.println("\n\t** Accept States and Anchor Vector **");
          print_accept();
          }*/
    }

    /***************************************************************
    function: print_set
    **************************************************************/
    void print_set
    (
            Vector nfa_set
            )
    {
        int size;
        int elem;
        CNfa nfa;

        size = nfa_set.size();

        if (0 == size)
            {
                System.out.print("empty ");
            }

        for (elem = 0; elem < size; ++elem)
            {
                nfa = (CNfa) nfa_set.elementAt(elem);
                /*System.out.print(m_spec.m_nfa_states.indexOf(nfa) + " ");*/
                System.out.print(nfa.m_label + " ");
            }
    }

    /***************************************************************
     Function: print_header
     **************************************************************/
    private void print_header
    (
     )
    {
        Enumeration states;
        int i;
        int j;
        int chars_printed=0;
        CDTrans dtrans;
        int last_transition;
        String str;
        CAccept accept;
        String state;
        Integer index;

        System.out.println("/*---------------------- DFA -----------------------");

        states = m_spec.m_states.keys();
        while (states.hasMoreElements())
            {
                state = (String) states.nextElement();
                index = (Integer) m_spec.m_states.get(state);

                if (CUtility.DEBUG)
                    {
                        CUtility.cuassert(null != state);
                        CUtility.cuassert(null != index);
                    }

                System.out.println("State \"" + state
                        + "\" has identifying index "
                        + index.toString() + ".");

                i = index.intValue();
                if (CDTrans.F != m_spec.m_state_dtrans[i])
                    {
                        System.out.println("\tStart index in transition table: "
                                + m_spec.m_state_dtrans[i]);
                    }
                else
                    {
                        System.out.println("\tNo associated transition states.");
                    }
            }

        for (i = 0; i < m_spec.m_dtrans_vector.size(); ++i)
            {
                dtrans = (CDTrans) m_spec.m_dtrans_vector.elementAt(i);

                if (null == m_spec.m_accept_vector && null == m_spec.m_anchor_array)
                    {
                        if (null == dtrans.m_accept)
                            {
                                System.out.print(" * State " + i + " [nonaccepting]");
                            }
                        else
                            {
                                System.out.print(" * State " + i
                                        + " [accepting, line "
                                        + dtrans.m_accept.m_line_number
                                        + " <"
                                        + (new String(dtrans.m_accept.m_action,0,
                                                dtrans.m_accept.m_action_read))
                                        + ">]");
                                if (CSpec.NONE != dtrans.m_anchor)
                                    {
                                        System.out.print(" Anchor: "
                                                + ((0 != (dtrans.m_anchor & CSpec.START))
                                                        ? "start " : "")
                                                + ((0 != (dtrans.m_anchor & CSpec.END))
                                                        ? "end " : ""));
                                    }
                            }
                    }
                else
                    {
                        accept = (CAccept) m_spec.m_accept_vector.elementAt(i);

                        if (null == accept)
                            {
                                System.out.print(" * State " + i + " [nonaccepting]");
                            }
                        else
                            {
                                System.out.print(" * State " + i
                                        + " [accepting, line "
                                        + accept.m_line_number
                                        + " <"
                                        + (new String(accept.m_action,0,
                                                accept.m_action_read))
                                        + ">]");
                                if (CSpec.NONE != m_spec.m_anchor_array[i])
                                    {
                                        System.out.print(" Anchor: "
                                                + ((0 != (m_spec.m_anchor_array[i] & CSpec.START))
                                                        ? "start " : "")
                                                + ((0 != (m_spec.m_anchor_array[i] & CSpec.END))
                                                        ? "end " : ""));
                                    }
                            }
                    }

                last_transition = -1;
                for (j = 0; j < m_spec.m_dtrans_ncols; ++j)
                    {
                        if (CDTrans.F != dtrans.m_dtrans[j])
                            {
                                if (last_transition != dtrans.m_dtrans[j])
                                    {
                                        System.out.print("\n *    goto " + dtrans.m_dtrans[j]
                                                + " on ");
                                        chars_printed = 0;
                                    }

                                str = interp_int((int) j);
                                System.out.print(str);

                                chars_printed = chars_printed + str.length();
                                if (56 < chars_printed)
                                    {
                                        System.out.print("\n *             ");
                                        chars_printed = 0;
                                    }

                                last_transition = dtrans.m_dtrans[j];
                            }
                    }
                System.out.println("");
            }
        System.out.println(" */\n");
    }
}

/*
 * @(#)BitSet.java        1.12 95/12/01
 *
 * Revision: 10/21/96
 * Modified by Elliot Joel Berk (ejberk@princeton.edu), for the purposes
 * of fixing a bug that was causing unjustified exceptions and crashes.
 * This bug was the result of resizing the internal buffer not large enough
 * in some cases.  The name was then changed to JavaLexBitSet.
 *
 * Copyright (c) 1995-2003 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL purposes and without
 * fee is hereby granted provided that this copyright notice
 * appears in all copies. Please refer to the file "copyright.html"
 * for further important copyright and licensing information.
 *
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

/**
 * A set of bits. The set automatically grows as more bits are
 * needed.
 *
 * @version         1.12, 01 Dec 1995
 * @since Ptolemy II 1.0
 * @author Arthur van Hoff, revised by Elliot Joel Berk
 */
final class JavaLexBitSet implements Cloneable {
    final static int BITS = 6;
    final static int MASK = (1<<BITS)-1;
    long bits[];

    /**
     * Creates an empty set.
     */
    public JavaLexBitSet() {
        this(1<<BITS);
    }

    /**
     * Creates an empty set with the specified size.
     * @param nbits the size of the set
     */
    public JavaLexBitSet(int nbits) {
        bits = new long[nbits2size(nbits)];
    }

    private int nbits2size (int nbits) {
        return ((nbits >> BITS) + 1);
    }

    private void resize(int nbits) {
        int newsize = Math.max(bits.length, nbits2size(nbits));
        long newbits[] = new long[newsize];
        System.arraycopy(bits, 0, newbits, 0, bits.length);
        bits = newbits;
    }

    /**
     * Sets a bit.
     * @param bit the bit to be set
     */
    public void set(int bit) {
        int n = bit>>BITS;
        if (n >= bits.length) {
            resize(bit);
        }
        bits[n] |= (1L << (bit & MASK));
    }

    /**
     * Clears a bit.
     * @param bit the bit to be cleared
     */
    public void clear(int bit) {
        int n = bit>>BITS;
        if (n >= bits.length) {
            resize(bit);
        }
        bits[n] &= ~(1L << (bit & MASK));
    }

    /**
     * Gets a bit.
     * @param bit the bit to be gotten
     */
    public boolean get(int bit) {
        int n = bit>>BITS;
        return (n < bits.length) ?
            ((bits[n] & (1L << (bit & MASK))) != 0) :
            false;
    }

    /**
     * Logically ANDs this bit set with the specified set of bits.
     * @param set the bit set to be ANDed with
     */
    public void and(JavaLexBitSet set) {
        int n = Math.min(bits.length, set.bits.length);
        for (int i = n ; i-- > 0 ; ) {
            bits[i] &= set.bits[i];
        }
        for (; n < bits.length ; n++) {
            bits[n] = 0;
        }
    }

    /**
     * Logically ORs this bit set with the specified set of bits.
     * @param set the bit set to be ORed with
     */
    public void or(JavaLexBitSet set) {
        for (int i = Math.min(bits.length, set.bits.length) ; i-- > 0 ;) {
            bits[i] |= set.bits[i];
        }
    }

    /**
     * Logically XORs this bit set with the specified set of bits.
     * @param set the bit set to be XORed with
     */
    public void xor(JavaLexBitSet set) {
        for (int i = Math.min(bits.length, set.bits.length) ; i-- > 0 ;) {
            bits[i] ^= set.bits[i];
        }
    }

    /**
     * Gets the hashcode.
     */
    public int hashCode() {
        long h = 1234;
        for (int i = bits.length; --i >= 0; ) {
            h ^= bits[i] * i;
        }
        return (int)((h >> 32) ^ h);
    }

    /**
     * Calculates and returns the set's size
     */
    public int size() {
        return bits.length << BITS;
    }

    /**
     * Compares this object against the specified object.
     * @param obj the object to compare with
     * @return true if the objects are the same; false otherwise.
     */
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof JavaLexBitSet)) {
            JavaLexBitSet set = (JavaLexBitSet)obj;

            int n = Math.min(bits.length, set.bits.length);
            for (int i = n ; i-- > 0 ;) {
                if (bits[i] != set.bits[i]) {
                    return false;
                }
            }
            if (bits.length > n) {
                for (int i = bits.length ; i-- > n ;) {
                    if (bits[i] != 0) {
                        return false;
                    }
                }
            } else if (set.bits.length > n) {
                for (int i = set.bits.length ; i-- > n ;) {
                    if (set.bits[i] != 0) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Clones the JavaLexBitSet.
     */
    public Object clone() {
        try {
            JavaLexBitSet set = (JavaLexBitSet)super.clone();
            set.bits = new long[bits.length];
            System.arraycopy(bits, 0, set.bits, 0, bits.length);
            return set;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    /**
     * Converts the JavaLexBitSet to a String.
     */
    public String toString() {
        String str = "";
        for (int i = 0 ; i < (bits.length << BITS) ; i++) {
            if (get(i)) {
                if (str.length() > 0) {
                    str += ", ";
                }
                str = str + i;
            }
        }
        return "{" + str + "}";
    }
}

/************************************************************************
                                                                         JLEX COPYRIGHT NOTICE, LICENSE AND DISCLAIMER.

                                                                         Copyright 1996 by Elliot Joel Berk

                                                                         Permission to use, copy, modify, and distribute this software and its
                                                                         documentation for any purpose and without fee is hereby granted,
                                                                         provided that the above copyright notice appear in all copies and that
                                                                         both the copyright notice and this permission notice and warranty
                                                                         disclaimer appear in supporting documentation, and that the name of
                                                                         Elliot Joel Berk not be used in advertising or publicity pertaining
                                                                         to distribution of the software without specific, written prior permission.

                                                                         Elliot Joel Berk disclaims all warranties with regard to this software,
                                                                         including all implied warranties of merchantability and fitness.  In no event
                                                                         shall Elliot Joel Berk be liable for any special, indirect or consequential
                                                                         damages or any damages whatsoever resulting from loss of use, data or
                                                                         profits, whether in an action of contract, negligence or other
                                                                         tortious action, arising out of or in connection with the use or
                                                                         performance of this software.
***********************************************************************/

