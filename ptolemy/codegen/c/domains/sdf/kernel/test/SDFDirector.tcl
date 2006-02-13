# C codegen SDFDirector test
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2005 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
#######################################################################

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs sdfModel] == "" } then {
    source [file join $PTII util testsuite models.tcl]
}

#####
test SDFDirector-1.1 {Call generateCode(StringBuffer)} {
    set model [sdfModel]
    set codeGenerator \
	    [java::new ptolemy.codegen.kernel.CodeGenerator \
	    $model "myCodeGenerator"]

    set results [java::new StringBuffer]
    $codeGenerator generateCode $results
    list [$results toString]
} {{/* Generate shared code for .top */
/* Finished generate shared code for .top */
/* Generate type resolution code for .top */

#define MISSING 0
#define boolean unsigned char
#define false 0
#define true 1
#define TYPE_String 0
#define TYPE_Double 1
#define TYPE_Boolean 2
#define TYPE_Int 3
typedef struct token Token;
typedef char* StringToken;

typedef double DoubleToken;

typedef char BooleanToken;

typedef int IntToken;

struct token {                  // Base type for tokens.
    unsigned char type;         // TYPE field has to be the first field.
    union typeMembers {
        // type member declarations [i.e. Type1Token Type1;]
		StringToken String;
		DoubleToken Double;
		BooleanToken Boolean;
		IntToken Int;
                 
    } payload;
};

Token String_convert(Token token);
Token String_print(Token thisToken);

Token Double_convert(Token token);
Token Double_print(Token thisToken);

Token Boolean_convert(Token token);
Token Boolean_print(Token thisToken);

Token Int_convert(Token token);
Token Int_print(Token thisToken);

//int atoi (char* s);             // standard c function.
//double atof (char* s);          // standard c function.
//long atol (char* s);            // standard c function.
    
char* itoa (int i) {
    char* string = (char*) malloc(sizeof(char) * 12);
    sprintf((char*) string, "%d", i);
    return string;       
}

char* ltoa (long l) {
    char* string = (char*) malloc(sizeof(char) * 22);
    sprintf((char*) string, "%d", l);
    return string;       
}

char* ftoa (double d) {
    char* string = (char*) malloc(sizeof(char) * 12);
    sprintf((char*) string, "%g", d);
    return string;       
}

char* btoa (char b) {
    if (b) {
        return "true";
    } else {
        return "false";
    }
}

int ftoi (double d) {
    return floor(d);
}

double itof (int i) {
    return (double) i;
}

// make a new integer token from the given value.
Token String_new(char* s) {
    Token result;
    result.type = TYPE_String;
    result.payload.String = s;
    return result;
}

// make a new integer token from the given value.
Token Double_new(double d) {
    Token result;
    result.type = TYPE_Double;
    result.payload.Double = d;
    return result;
}

// make a new integer token from the given value.
Token Boolean_new(char b) {
    Token result;
    result.type = TYPE_Boolean;
    result.payload.Boolean = b;
    return result;
}

// make a new integer token from the given value.
Token Int_new(int i) {
    Token result;
    result.type = TYPE_Int;
    result.payload.Int = i;
    return result;
}


/* Variable Declarations .top */

/* Composite actor's variable declarations. */

/* top's variable declarations. */

/* preinitialize top */
/* The preinitialization of the director. */
static int iteration = 0;


main(int argc, char *argv[]) {


/* Variable initialization .top */

/* Composite actor's variable initializations. */

/* top's variable initialization. */
/* 
Initialize .top */
/* The initialization of the director. */
/* Wrapup .top */
/* The wrapup of the director. */
exit(0);
}
}}
