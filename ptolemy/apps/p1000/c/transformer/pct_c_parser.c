/* A Bison parser, made by GNU Bison 2.3.  */

/* Skeleton implementation for Bison's Yacc-like parsers in C

   Copyright (C) 1984, 1989, 1990, 2000, 2001, 2002, 2003, 2004, 2005, 2006
   Free Software Foundation, Inc.

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2, or (at your option)
   any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 51 Franklin Street, Fifth Floor,
   Boston, MA 02110-1301, USA.  */

/* As a special exception, you may create a larger work that contains
   part or all of the Bison parser skeleton and distribute that work
   under terms of your choice, so long as that work isn't itself a
   parser generator using the skeleton or a modified version thereof
   as a parser skeleton.  Alternatively, if you modify or redistribute
   the parser skeleton itself, you may (at your option) remove this
   special exception, which will cause the skeleton and the resulting
   Bison output files to be licensed under the GNU General Public
   License without this special exception.

   This special exception was added by the Free Software Foundation in
   version 2.2 of Bison.  */

/* C LALR(1) parser skeleton written by Richard Stallman, by
   simplifying the original so-called "semantic" parser.  */

/* All symbols defined below should begin with yy or YY, to avoid
   infringing on user name space.  This should be done even for local
   variables, as they might otherwise be expanded by user macros.
   There are some unavoidable exceptions within include files to
   define necessary library symbols; they are noted "INFRINGES ON
   USER NAME SPACE" below.  */

/* Identify Bison output.  */
#define YYBISON 1

/* Bison version.  */
#define YYBISON_VERSION "2.3"

/* Skeleton name.  */
#define YYSKELETON_NAME "yacc.c"

/* Pure parsers.  */
#define YYPURE 0

/* Using locations.  */
#define YYLSP_NEEDED 0

/* Substitute the variable and function names.  */
#define yyparse c_parse
#define yylex   c_lex
#define yyerror c_error
#define yylval  c_lval
#define yychar  c_char
#define yydebug c_debug
#define yynerrs c_nerrs


/* Tokens.  */
#ifndef YYTOKENTYPE
# define YYTOKENTYPE
   /* Put the tokens into the symbol table, so that GDB and other debuggers
      know about them.  */
   enum yytokentype {
     IDENTIFIER = 258,
     CONSTANT = 259,
     STRING_LITERAL = 260,
     SIZEOF = 261,
     PTR_OP = 262,
     INC_OP = 263,
     DEC_OP = 264,
     LEFT_OP = 265,
     RIGHT_OP = 266,
     LE_OP = 267,
     GE_OP = 268,
     EQ_OP = 269,
     NE_OP = 270,
     AND_OP = 271,
     OR_OP = 272,
     MUL_ASSIGN = 273,
     DIV_ASSIGN = 274,
     MOD_ASSIGN = 275,
     ADD_ASSIGN = 276,
     SUB_ASSIGN = 277,
     LEFT_ASSIGN = 278,
     RIGHT_ASSIGN = 279,
     AND_ASSIGN = 280,
     XOR_ASSIGN = 281,
     OR_ASSIGN = 282,
     TYPEDEF = 283,
     EXTERN = 284,
     STATIC = 285,
     AUTO = 286,
     REGISTER = 287,
     CHAR = 288,
     SHORT = 289,
     INT = 290,
     LONG = 291,
     SIGNED = 292,
     UNSIGNED = 293,
     FLOAT = 294,
     DOUBLE = 295,
     CONST = 296,
     VOLATILE = 297,
     VOID = 298,
     STRUCT = 299,
     UNION = 300,
     ENUM = 301,
     ELLIPSIS = 302,
     CASE = 303,
     DEFAULT = 304,
     IF = 305,
     ELSE = 306,
     SWITCH = 307,
     WHILE = 308,
     DO = 309,
     FOR = 310,
     GOTO = 311,
     CONTINUE = 312,
     BREAK = 313,
     RETURN = 314,
     LEXICAL_TOKEN = 315,
     AMBIGUOUS_TYPE_NAME = 316,
     COMPLEX = 317,
     IMAGINARY = 318,
     RESTRICT = 319,
     BOOLEAN = 320,
     INLINE = 321,
     ARRAY_ACCESS = 322,
     ARGUMENT_EXPRESSION_LIST = 323,
     PARAMETERIZED_EXPRESSION = 324,
     FIELD_ACCESS = 325,
     INC_DEC_EXPRESSION = 326,
     STRUCT_CONSTANT = 327,
     UNARY_EXPRESSION = 328,
     SIZEOF_EXPRESSION = 329,
     CAST_EXPRESSION = 330,
     BINARY_EXPRESSION = 331,
     CONDITIONAL_EXPRESSION = 332,
     COMMA_EXPRESSION = 333,
     DECLARATION = 334,
     STORAGE_CLASS_SPECIFIER = 335,
     STRUCT_OR_UNION = 336,
     STRUCT_DECLARATION = 337,
     STRUCT_DECLARATION_LIST = 338,
     PRIMITIVE_TYPE_SPECIFIER = 339,
     NAMED_TYPE_SPECIFIER = 340,
     DECLARATION_SPECIFIERS = 341,
     INIT_DECLARATOR_LIST = 342,
     INIT_DECLARATOR = 343,
     DECLARATOR = 344,
     SPECIFIER_QUALIFIER_LIST = 345,
     STRUCT_DECLARATOR_LIST = 346,
     STRUCT_DECLARATOR = 347,
     ENUM_SPECIFIER = 348,
     ENUMERATOR_LIST = 349,
     ENUMERATOR = 350,
     TYPE_QUALIFIER = 351,
     FUNCTION_SPECIFIER = 352,
     POINTER = 353,
     TYPE_QUALIFIER_LIST = 354,
     NAMED_DIRECT_DECLARATOR = 355,
     ARRAY_DIRECT_DECLARATOR = 356,
     PARAMETER_LIST = 357,
     PARAMETERIZED_DIRECT_DECLARATOR = 358,
     PARAMETER_DECLARATION = 359,
     IDENTIFIER_LIST = 360,
     TYPE_NAME = 361,
     ABSTRACT_DECLARATOR = 362,
     ARRAY_DIRECT_ABSTRACT_DECLARATOR = 363,
     PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR = 364,
     INITIALIZER_LIST = 365,
     DESIGNATOR_LIST = 366,
     ARRAY_DESIGNATOR = 367,
     DOT_DESIGNATOR = 368,
     IDENTIFIER_LABELED_STATEMENT = 369,
     CASE_LABELED_STATEMENT = 370,
     BLOCK_ITEM_LIST = 371,
     EXPRESSION_STATEMENT = 372,
     IF_STATEMENT = 373,
     SWITCH_STATEMENT = 374,
     WHILE_STATEMENT = 375,
     DO_WHILE_STATEMENT = 376,
     FOR_STATEMENT = 377,
     GOTO_STATEMENT = 378,
     LOOP_JUMP_STATEMENT = 379,
     RETURN_STATEMENT = 380,
     TRANSLATION_UNIT = 381,
     FUNCTION_DEFINITION = 382,
     DECLARATION_LIST = 383,
     COMPOUND_STATEMENT = 384
   };
#endif
/* Tokens.  */
#define IDENTIFIER 258
#define CONSTANT 259
#define STRING_LITERAL 260
#define SIZEOF 261
#define PTR_OP 262
#define INC_OP 263
#define DEC_OP 264
#define LEFT_OP 265
#define RIGHT_OP 266
#define LE_OP 267
#define GE_OP 268
#define EQ_OP 269
#define NE_OP 270
#define AND_OP 271
#define OR_OP 272
#define MUL_ASSIGN 273
#define DIV_ASSIGN 274
#define MOD_ASSIGN 275
#define ADD_ASSIGN 276
#define SUB_ASSIGN 277
#define LEFT_ASSIGN 278
#define RIGHT_ASSIGN 279
#define AND_ASSIGN 280
#define XOR_ASSIGN 281
#define OR_ASSIGN 282
#define TYPEDEF 283
#define EXTERN 284
#define STATIC 285
#define AUTO 286
#define REGISTER 287
#define CHAR 288
#define SHORT 289
#define INT 290
#define LONG 291
#define SIGNED 292
#define UNSIGNED 293
#define FLOAT 294
#define DOUBLE 295
#define CONST 296
#define VOLATILE 297
#define VOID 298
#define STRUCT 299
#define UNION 300
#define ENUM 301
#define ELLIPSIS 302
#define CASE 303
#define DEFAULT 304
#define IF 305
#define ELSE 306
#define SWITCH 307
#define WHILE 308
#define DO 309
#define FOR 310
#define GOTO 311
#define CONTINUE 312
#define BREAK 313
#define RETURN 314
#define LEXICAL_TOKEN 315
#define AMBIGUOUS_TYPE_NAME 316
#define COMPLEX 317
#define IMAGINARY 318
#define RESTRICT 319
#define BOOLEAN 320
#define INLINE 321
#define ARRAY_ACCESS 322
#define ARGUMENT_EXPRESSION_LIST 323
#define PARAMETERIZED_EXPRESSION 324
#define FIELD_ACCESS 325
#define INC_DEC_EXPRESSION 326
#define STRUCT_CONSTANT 327
#define UNARY_EXPRESSION 328
#define SIZEOF_EXPRESSION 329
#define CAST_EXPRESSION 330
#define BINARY_EXPRESSION 331
#define CONDITIONAL_EXPRESSION 332
#define COMMA_EXPRESSION 333
#define DECLARATION 334
#define STORAGE_CLASS_SPECIFIER 335
#define STRUCT_OR_UNION 336
#define STRUCT_DECLARATION 337
#define STRUCT_DECLARATION_LIST 338
#define PRIMITIVE_TYPE_SPECIFIER 339
#define NAMED_TYPE_SPECIFIER 340
#define DECLARATION_SPECIFIERS 341
#define INIT_DECLARATOR_LIST 342
#define INIT_DECLARATOR 343
#define DECLARATOR 344
#define SPECIFIER_QUALIFIER_LIST 345
#define STRUCT_DECLARATOR_LIST 346
#define STRUCT_DECLARATOR 347
#define ENUM_SPECIFIER 348
#define ENUMERATOR_LIST 349
#define ENUMERATOR 350
#define TYPE_QUALIFIER 351
#define FUNCTION_SPECIFIER 352
#define POINTER 353
#define TYPE_QUALIFIER_LIST 354
#define NAMED_DIRECT_DECLARATOR 355
#define ARRAY_DIRECT_DECLARATOR 356
#define PARAMETER_LIST 357
#define PARAMETERIZED_DIRECT_DECLARATOR 358
#define PARAMETER_DECLARATION 359
#define IDENTIFIER_LIST 360
#define TYPE_NAME 361
#define ABSTRACT_DECLARATOR 362
#define ARRAY_DIRECT_ABSTRACT_DECLARATOR 363
#define PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR 364
#define INITIALIZER_LIST 365
#define DESIGNATOR_LIST 366
#define ARRAY_DESIGNATOR 367
#define DOT_DESIGNATOR 368
#define IDENTIFIER_LABELED_STATEMENT 369
#define CASE_LABELED_STATEMENT 370
#define BLOCK_ITEM_LIST 371
#define EXPRESSION_STATEMENT 372
#define IF_STATEMENT 373
#define SWITCH_STATEMENT 374
#define WHILE_STATEMENT 375
#define DO_WHILE_STATEMENT 376
#define FOR_STATEMENT 377
#define GOTO_STATEMENT 378
#define LOOP_JUMP_STATEMENT 379
#define RETURN_STATEMENT 380
#define TRANSLATION_UNIT 381
#define FUNCTION_DEFINITION 382
#define DECLARATION_LIST 383
#define COMPOUND_STATEMENT 384




/* Copy the first part of user declarations.  */
#line 1 "../../../c/transformer/pct_c_parser.y"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "pct_util.h"

void check_type_name_declaration(struct AST_NODE* declaration);
int c_error(char* const error);

extern int c_lex();


/* Enabling traces.  */
#ifndef YYDEBUG
# define YYDEBUG 0
#endif

/* Enabling verbose error messages.  */
#ifdef YYERROR_VERBOSE
# undef YYERROR_VERBOSE
# define YYERROR_VERBOSE 1
#else
# define YYERROR_VERBOSE 0
#endif

/* Enabling the token table.  */
#ifndef YYTOKEN_TABLE
# define YYTOKEN_TABLE 0
#endif

#if ! defined YYSTYPE && ! defined YYSTYPE_IS_DECLARED
typedef int YYSTYPE;
# define yystype YYSTYPE /* obsolescent; will be withdrawn */
# define YYSTYPE_IS_DECLARED 1
# define YYSTYPE_IS_TRIVIAL 1
#endif



/* Copy the second part of user declarations.  */


/* Line 216 of yacc.c.  */
#line 384 "../../../c/transformer/pct_c_parser.c"

#ifdef short
# undef short
#endif

#ifdef YYTYPE_UINT8
typedef YYTYPE_UINT8 yytype_uint8;
#else
typedef unsigned char yytype_uint8;
#endif

#ifdef YYTYPE_INT8
typedef YYTYPE_INT8 yytype_int8;
#elif (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
typedef signed char yytype_int8;
#else
typedef short int yytype_int8;
#endif

#ifdef YYTYPE_UINT16
typedef YYTYPE_UINT16 yytype_uint16;
#else
typedef unsigned short int yytype_uint16;
#endif

#ifdef YYTYPE_INT16
typedef YYTYPE_INT16 yytype_int16;
#else
typedef short int yytype_int16;
#endif

#ifndef YYSIZE_T
# ifdef __SIZE_TYPE__
#  define YYSIZE_T __SIZE_TYPE__
# elif defined size_t
#  define YYSIZE_T size_t
# elif ! defined YYSIZE_T && (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
#  include <stddef.h> /* INFRINGES ON USER NAME SPACE */
#  define YYSIZE_T size_t
# else
#  define YYSIZE_T unsigned int
# endif
#endif

#define YYSIZE_MAXIMUM ((YYSIZE_T) -1)

#ifndef YY_
# if YYENABLE_NLS
#  if ENABLE_NLS
#   include <libintl.h> /* INFRINGES ON USER NAME SPACE */
#   define YY_(msgid) dgettext ("bison-runtime", msgid)
#  endif
# endif
# ifndef YY_
#  define YY_(msgid) msgid
# endif
#endif

/* Suppress unused-variable warnings by "using" E.  */
#if ! defined lint || defined __GNUC__
# define YYUSE(e) ((void) (e))
#else
# define YYUSE(e) /* empty */
#endif

/* Identity function, used to suppress warnings about constant conditions.  */
#ifndef lint
# define YYID(n) (n)
#else
#if (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
static int
YYID (int i)
#else
static int
YYID (i)
    int i;
#endif
{
  return i;
}
#endif

#if ! defined yyoverflow || YYERROR_VERBOSE

/* The parser invokes alloca or malloc; define the necessary symbols.  */

# ifdef YYSTACK_USE_ALLOCA
#  if YYSTACK_USE_ALLOCA
#   ifdef __GNUC__
#    define YYSTACK_ALLOC __builtin_alloca
#   elif defined __BUILTIN_VA_ARG_INCR
#    include <alloca.h> /* INFRINGES ON USER NAME SPACE */
#   elif defined _AIX
#    define YYSTACK_ALLOC __alloca
#   elif defined _MSC_VER
#    include <malloc.h> /* INFRINGES ON USER NAME SPACE */
#    define alloca _alloca
#   else
#    define YYSTACK_ALLOC alloca
#    if ! defined _ALLOCA_H && ! defined _STDLIB_H && (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
#     include <stdlib.h> /* INFRINGES ON USER NAME SPACE */
#     ifndef _STDLIB_H
#      define _STDLIB_H 1
#     endif
#    endif
#   endif
#  endif
# endif

# ifdef YYSTACK_ALLOC
   /* Pacify GCC's `empty if-body' warning.  */
#  define YYSTACK_FREE(Ptr) do { /* empty */; } while (YYID (0))
#  ifndef YYSTACK_ALLOC_MAXIMUM
    /* The OS might guarantee only one guard page at the bottom of the stack,
       and a page size can be as small as 4096 bytes.  So we cannot safely
       invoke alloca (N) if N exceeds 4096.  Use a slightly smaller number
       to allow for a few compiler-allocated temporary stack slots.  */
#   define YYSTACK_ALLOC_MAXIMUM 4032 /* reasonable circa 2006 */
#  endif
# else
#  define YYSTACK_ALLOC YYMALLOC
#  define YYSTACK_FREE YYFREE
#  ifndef YYSTACK_ALLOC_MAXIMUM
#   define YYSTACK_ALLOC_MAXIMUM YYSIZE_MAXIMUM
#  endif
#  if (defined __cplusplus && ! defined _STDLIB_H \
       && ! ((defined YYMALLOC || defined malloc) \
	     && (defined YYFREE || defined free)))
#   include <stdlib.h> /* INFRINGES ON USER NAME SPACE */
#   ifndef _STDLIB_H
#    define _STDLIB_H 1
#   endif
#  endif
#  ifndef YYMALLOC
#   define YYMALLOC malloc
#   if ! defined malloc && ! defined _STDLIB_H && (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
void *malloc (YYSIZE_T); /* INFRINGES ON USER NAME SPACE */
#   endif
#  endif
#  ifndef YYFREE
#   define YYFREE free
#   if ! defined free && ! defined _STDLIB_H && (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
void free (void *); /* INFRINGES ON USER NAME SPACE */
#   endif
#  endif
# endif
#endif /* ! defined yyoverflow || YYERROR_VERBOSE */


#if (! defined yyoverflow \
     && (! defined __cplusplus \
	 || (defined YYSTYPE_IS_TRIVIAL && YYSTYPE_IS_TRIVIAL)))

/* A type that is properly aligned for any stack member.  */
union yyalloc
{
  yytype_int16 yyss;
  YYSTYPE yyvs;
  };

/* The size of the maximum gap between one aligned stack and the next.  */
# define YYSTACK_GAP_MAXIMUM (sizeof (union yyalloc) - 1)

/* The size of an array large to enough to hold all stacks, each with
   N elements.  */
# define YYSTACK_BYTES(N) \
     ((N) * (sizeof (yytype_int16) + sizeof (YYSTYPE)) \
      + YYSTACK_GAP_MAXIMUM)

/* Copy COUNT objects from FROM to TO.  The source and destination do
   not overlap.  */
# ifndef YYCOPY
#  if defined __GNUC__ && 1 < __GNUC__
#   define YYCOPY(To, From, Count) \
      __builtin_memcpy (To, From, (Count) * sizeof (*(From)))
#  else
#   define YYCOPY(To, From, Count)		\
      do					\
	{					\
	  YYSIZE_T yyi;				\
	  for (yyi = 0; yyi < (Count); yyi++)	\
	    (To)[yyi] = (From)[yyi];		\
	}					\
      while (YYID (0))
#  endif
# endif

/* Relocate STACK from its old location to the new one.  The
   local variables YYSIZE and YYSTACKSIZE give the old and new number of
   elements in the stack, and YYPTR gives the new location of the
   stack.  Advance YYPTR to a properly aligned location for the next
   stack.  */
# define YYSTACK_RELOCATE(Stack)					\
    do									\
      {									\
	YYSIZE_T yynewbytes;						\
	YYCOPY (&yyptr->Stack, Stack, yysize);				\
	Stack = &yyptr->Stack;						\
	yynewbytes = yystacksize * sizeof (*Stack) + YYSTACK_GAP_MAXIMUM; \
	yyptr += yynewbytes / sizeof (*yyptr);				\
      }									\
    while (YYID (0))

#endif

/* YYFINAL -- State number of the termination state.  */
#define YYFINAL  70
/* YYLAST -- Last index in YYTABLE.  */
#define YYLAST   1660

/* YYNTOKENS -- Number of terminals.  */
#define YYNTOKENS  154
/* YYNNTS -- Number of nonterminals.  */
#define YYNNTS  70
/* YYNRULES -- Number of rules.  */
#define YYNRULES  245
/* YYNRULES -- Number of states.  */
#define YYNSTATES  409

/* YYTRANSLATE(YYLEX) -- Bison symbol number corresponding to YYLEX.  */
#define YYUNDEFTOK  2
#define YYMAXUTOK   384

#define YYTRANSLATE(YYX)						\
  ((unsigned int) (YYX) <= YYMAXUTOK ? yytranslate[YYX] : YYUNDEFTOK)

/* YYTRANSLATE[YYLEX] -- Bison symbol number corresponding to YYLEX.  */
static const yytype_uint8 yytranslate[] =
{
       0,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,   143,     2,     2,     2,   145,   138,     2,
     130,   131,   139,   140,   137,   141,   134,   144,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,   151,   153,
     146,   152,   147,   150,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,   132,     2,   133,   148,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,   135,   149,   136,   142,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     1,     2,     3,     4,
       5,     6,     7,     8,     9,    10,    11,    12,    13,    14,
      15,    16,    17,    18,    19,    20,    21,    22,    23,    24,
      25,    26,    27,    28,    29,    30,    31,    32,    33,    34,
      35,    36,    37,    38,    39,    40,    41,    42,    43,    44,
      45,    46,    47,    48,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    58,    59,    60,    61,    62,    63,    64,
      65,    66,    67,    68,    69,    70,    71,    72,    73,    74,
      75,    76,    77,    78,    79,    80,    81,    82,    83,    84,
      85,    86,    87,    88,    89,    90,    91,    92,    93,    94,
      95,    96,    97,    98,    99,   100,   101,   102,   103,   104,
     105,   106,   107,   108,   109,   110,   111,   112,   113,   114,
     115,   116,   117,   118,   119,   120,   121,   122,   123,   124,
     125,   126,   127,   128,   129
};

#if YYDEBUG
/* YYPRHS[YYN] -- Index of the first RHS symbol of rule number YYN in
   YYRHS.  */
static const yytype_uint16 yyprhs[] =
{
       0,     0,     3,     5,     8,    10,    12,    14,    18,    20,
      25,    29,    34,    38,    42,    45,    48,    55,    63,    65,
      69,    71,    74,    77,    80,    83,    88,    90,    92,    94,
      96,    98,   100,   102,   107,   109,   113,   117,   121,   123,
     127,   131,   133,   137,   141,   143,   147,   151,   155,   159,
     161,   165,   169,   171,   175,   177,   181,   183,   187,   189,
     193,   195,   199,   201,   207,   209,   213,   215,   217,   219,
     221,   223,   225,   227,   229,   231,   233,   235,   237,   241,
     243,   246,   250,   252,   255,   257,   260,   262,   265,   267,
     270,   272,   276,   278,   282,   284,   286,   288,   290,   292,
     294,   296,   298,   300,   302,   304,   306,   308,   310,   312,
     314,   316,   318,   320,   322,   328,   333,   336,   339,   341,
     343,   345,   348,   352,   355,   358,   360,   363,   365,   367,
     371,   373,   376,   380,   385,   391,   397,   404,   407,   410,
     412,   416,   418,   422,   424,   426,   428,   430,   433,   435,
     437,   441,   447,   452,   457,   464,   471,   477,   482,   486,
     491,   496,   500,   502,   505,   508,   512,   514,   517,   519,
     523,   525,   529,   532,   535,   537,   539,   543,   545,   548,
     550,   552,   555,   559,   562,   566,   570,   575,   579,   584,
     587,   591,   595,   600,   602,   606,   611,   613,   616,   620,
     625,   628,   630,   633,   637,   640,   642,   644,   646,   648,
     650,   652,   656,   661,   665,   668,   672,   674,   677,   679,
     681,   683,   686,   692,   700,   706,   712,   720,   727,   735,
     742,   750,   754,   757,   760,   763,   767,   769,   772,   774,
     776,   780,   783,   788,   792,   794
};

/* YYRHS -- A `-1'-separated list of the rules' RHS.  */
static const yytype_int16 yyrhs[] =
{
     220,     0,    -1,     5,    -1,   155,     5,    -1,     3,    -1,
       4,    -1,   155,    -1,   130,   175,   131,    -1,   156,    -1,
     157,   132,   175,   133,    -1,   157,   130,   131,    -1,   157,
     130,   158,   131,    -1,   157,   134,     3,    -1,   157,     7,
       3,    -1,   157,     8,    -1,   157,     9,    -1,   130,   203,
     131,   135,   207,   136,    -1,   130,   203,   131,   135,   207,
     137,   136,    -1,   173,    -1,   158,   137,   173,    -1,   157,
      -1,     8,   159,    -1,     9,   159,    -1,   160,   161,    -1,
       6,   159,    -1,     6,   130,   203,   131,    -1,   138,    -1,
     139,    -1,   140,    -1,   141,    -1,   142,    -1,   143,    -1,
     159,    -1,   130,   203,   131,   161,    -1,   161,    -1,   162,
     139,   161,    -1,   162,   144,   161,    -1,   162,   145,   161,
      -1,   162,    -1,   163,   140,   162,    -1,   163,   141,   162,
      -1,   163,    -1,   164,    10,   163,    -1,   164,    11,   163,
      -1,   164,    -1,   165,   146,   164,    -1,   165,   147,   164,
      -1,   165,    12,   164,    -1,   165,    13,   164,    -1,   165,
      -1,   166,    14,   165,    -1,   166,    15,   165,    -1,   166,
      -1,   167,   138,   166,    -1,   167,    -1,   168,   148,   167,
      -1,   168,    -1,   169,   149,   168,    -1,   169,    -1,   170,
      16,   169,    -1,   170,    -1,   171,    17,   170,    -1,   171,
      -1,   171,   150,   175,   151,   172,    -1,   172,    -1,   159,
     174,   173,    -1,   152,    -1,    18,    -1,    19,    -1,    20,
      -1,    21,    -1,    22,    -1,    23,    -1,    24,    -1,    25,
      -1,    26,    -1,    27,    -1,   173,    -1,   175,   137,   173,
      -1,   172,    -1,   178,   153,    -1,   178,   179,   153,    -1,
     181,    -1,   181,   178,    -1,   182,    -1,   182,   178,    -1,
     193,    -1,   193,   178,    -1,   194,    -1,   194,   178,    -1,
     180,    -1,   179,   137,   180,    -1,   195,    -1,   195,   152,
     206,    -1,    28,    -1,    29,    -1,    30,    -1,    31,    -1,
      32,    -1,    43,    -1,    33,    -1,    34,    -1,    35,    -1,
      36,    -1,    39,    -1,    40,    -1,    37,    -1,    38,    -1,
      65,    -1,    62,    -1,    63,    -1,   183,    -1,   190,    -1,
      61,    -1,   184,     3,   135,   185,   136,    -1,   184,   135,
     185,   136,    -1,   184,     3,    -1,   184,    61,    -1,    44,
      -1,    45,    -1,   186,    -1,   185,   186,    -1,   187,   188,
     153,    -1,   187,   153,    -1,   182,   187,    -1,   182,    -1,
     193,   187,    -1,   193,    -1,   189,    -1,   188,   137,   189,
      -1,   195,    -1,   151,   176,    -1,   195,   151,   176,    -1,
      46,   135,   191,   136,    -1,    46,     3,   135,   191,   136,
      -1,    46,   135,   191,   137,   136,    -1,    46,     3,   135,
     191,   137,   136,    -1,    46,     3,    -1,    46,    61,    -1,
     192,    -1,   191,   137,   192,    -1,     3,    -1,     3,   152,
     176,    -1,    41,    -1,    64,    -1,    42,    -1,    66,    -1,
     197,   196,    -1,   196,    -1,     3,    -1,   130,   195,   131,
      -1,   196,   132,   198,   173,   133,    -1,   196,   132,   198,
     133,    -1,   196,   132,   173,   133,    -1,   196,   132,    30,
     198,   173,   133,    -1,   196,   132,   198,    30,   173,   133,
      -1,   196,   132,   198,   139,   133,    -1,   196,   132,   139,
     133,    -1,   196,   132,   133,    -1,   196,   130,   199,   131,
      -1,   196,   130,   202,   131,    -1,   196,   130,   131,    -1,
     139,    -1,   139,   198,    -1,   139,   197,    -1,   139,   198,
     197,    -1,   193,    -1,   198,   193,    -1,   200,    -1,   200,
     137,    47,    -1,   201,    -1,   200,   137,   201,    -1,   178,
     195,    -1,   178,   204,    -1,   178,    -1,     3,    -1,   202,
     137,     3,    -1,   187,    -1,   187,   204,    -1,   197,    -1,
     205,    -1,   197,   205,    -1,   130,   204,   131,    -1,   132,
     133,    -1,   132,   173,   133,    -1,   205,   132,   133,    -1,
     205,   132,   173,   133,    -1,   132,   139,   133,    -1,   205,
     132,   139,   133,    -1,   130,   131,    -1,   130,   199,   131,
      -1,   205,   130,   131,    -1,   205,   130,   199,   131,    -1,
     173,    -1,   135,   207,   136,    -1,   135,   207,   137,   136,
      -1,   206,    -1,   208,   206,    -1,   207,   137,   206,    -1,
     207,   137,   208,   206,    -1,   209,   152,    -1,   210,    -1,
     209,   210,    -1,   132,   176,   133,    -1,   134,     3,    -1,
     212,    -1,   213,    -1,   216,    -1,   217,    -1,   218,    -1,
     219,    -1,     3,   151,   211,    -1,    48,   176,   151,   211,
      -1,    49,   151,   211,    -1,   135,   136,    -1,   135,   214,
     136,    -1,   215,    -1,   214,   215,    -1,   177,    -1,   211,
      -1,   153,    -1,   175,   153,    -1,    50,   130,   175,   131,
     211,    -1,    50,   130,   175,   131,   211,    51,   211,    -1,
      52,   130,   175,   131,   211,    -1,    53,   130,   175,   131,
     211,    -1,    54,   211,    53,   130,   175,   131,   153,    -1,
      55,   130,   216,   216,   131,   211,    -1,    55,   130,   216,
     216,   175,   131,   211,    -1,    55,   130,   177,   216,   131,
     211,    -1,    55,   130,   177,   216,   175,   131,   211,    -1,
      56,     3,   153,    -1,    57,   153,    -1,    58,   153,    -1,
      59,   153,    -1,    59,   175,   153,    -1,   221,    -1,   220,
     221,    -1,   222,    -1,   177,    -1,   195,   223,   213,    -1,
     195,   213,    -1,   178,   195,   223,   213,    -1,   178,   195,
     213,    -1,   177,    -1,   223,   177,    -1
};

/* YYRLINE[YYN] -- source line where rule number YYN was defined.  */
static const yytype_uint16 yyrline[] =
{
       0,    55,    55,    56,    69,    70,    71,    72,    82,    83,
      91,   101,   111,   119,   125,   133,   141,   153,   170,   176,
     187,   188,   194,   200,   208,   214,   227,   228,   229,   230,
     231,   232,   236,   237,   248,   249,   257,   265,   276,   277,
     285,   296,   297,   305,   316,   317,   325,   333,   341,   352,
     353,   361,   372,   373,   384,   385,   396,   397,   408,   409,
     420,   421,   432,   433,   446,   447,   458,   459,   460,   461,
     462,   463,   464,   465,   466,   467,   468,   472,   473,   482,
     486,   492,   502,   508,   512,   518,   522,   528,   532,   538,
     545,   549,   558,   562,   571,   579,   587,   595,   603,   614,
     622,   630,   638,   646,   654,   662,   670,   678,   686,   694,
     702,   710,   711,   712,   723,   737,   749,   759,   772,   773,
     777,   783,   792,   798,   807,   813,   819,   825,   834,   840,
     851,   855,   861,   870,   880,   892,   904,   918,   928,   941,
     945,   954,   960,   971,   977,   983,   992,  1003,  1007,  1018,
    1026,  1033,  1043,  1053,  1063,  1076,  1089,  1101,  1113,  1123,
    1132,  1141,  1153,  1159,  1165,  1171,  1180,  1184,  1191,  1192,
    1199,  1203,  1211,  1217,  1223,  1232,  1238,  1249,  1253,  1260,
    1264,  1268,  1275,  1282,  1291,  1300,  1309,  1318,  1329,  1340,
    1349,  1358,  1367,  1379,  1380,  1387,  1399,  1403,  1407,  1413,
    1422,  1430,  1434,  1441,  1449,  1458,  1459,  1460,  1461,  1462,
    1463,  1467,  1476,  1486,  1499,  1507,  1518,  1522,  1529,  1530,
    1534,  1540,  1549,  1559,  1571,  1584,  1594,  1608,  1618,  1629,
    1639,  1652,  1662,  1672,  1682,  1690,  1701,  1706,  1714,  1715,
    1719,  1725,  1731,  1737,  1746,  1750
};
#endif

#if YYDEBUG || YYERROR_VERBOSE || YYTOKEN_TABLE
/* YYTNAME[SYMBOL-NUM] -- String name of the symbol SYMBOL-NUM.
   First, the terminals, then, starting at YYNTOKENS, nonterminals.  */
static const char *const yytname[] =
{
  "$end", "error", "$undefined", "IDENTIFIER", "CONSTANT",
  "STRING_LITERAL", "SIZEOF", "PTR_OP", "INC_OP", "DEC_OP", "LEFT_OP",
  "RIGHT_OP", "LE_OP", "GE_OP", "EQ_OP", "NE_OP", "AND_OP", "OR_OP",
  "MUL_ASSIGN", "DIV_ASSIGN", "MOD_ASSIGN", "ADD_ASSIGN", "SUB_ASSIGN",
  "LEFT_ASSIGN", "RIGHT_ASSIGN", "AND_ASSIGN", "XOR_ASSIGN", "OR_ASSIGN",
  "TYPEDEF", "EXTERN", "STATIC", "AUTO", "REGISTER", "CHAR", "SHORT",
  "INT", "LONG", "SIGNED", "UNSIGNED", "FLOAT", "DOUBLE", "CONST",
  "VOLATILE", "VOID", "STRUCT", "UNION", "ENUM", "ELLIPSIS", "CASE",
  "DEFAULT", "IF", "ELSE", "SWITCH", "WHILE", "DO", "FOR", "GOTO",
  "CONTINUE", "BREAK", "RETURN", "LEXICAL_TOKEN", "AMBIGUOUS_TYPE_NAME",
  "COMPLEX", "IMAGINARY", "RESTRICT", "BOOLEAN", "INLINE", "ARRAY_ACCESS",
  "ARGUMENT_EXPRESSION_LIST", "PARAMETERIZED_EXPRESSION", "FIELD_ACCESS",
  "INC_DEC_EXPRESSION", "STRUCT_CONSTANT", "UNARY_EXPRESSION",
  "SIZEOF_EXPRESSION", "CAST_EXPRESSION", "BINARY_EXPRESSION",
  "CONDITIONAL_EXPRESSION", "COMMA_EXPRESSION", "DECLARATION",
  "STORAGE_CLASS_SPECIFIER", "STRUCT_OR_UNION", "STRUCT_DECLARATION",
  "STRUCT_DECLARATION_LIST", "PRIMITIVE_TYPE_SPECIFIER",
  "NAMED_TYPE_SPECIFIER", "DECLARATION_SPECIFIERS", "INIT_DECLARATOR_LIST",
  "INIT_DECLARATOR", "DECLARATOR", "SPECIFIER_QUALIFIER_LIST",
  "STRUCT_DECLARATOR_LIST", "STRUCT_DECLARATOR", "ENUM_SPECIFIER",
  "ENUMERATOR_LIST", "ENUMERATOR", "TYPE_QUALIFIER", "FUNCTION_SPECIFIER",
  "POINTER", "TYPE_QUALIFIER_LIST", "NAMED_DIRECT_DECLARATOR",
  "ARRAY_DIRECT_DECLARATOR", "PARAMETER_LIST",
  "PARAMETERIZED_DIRECT_DECLARATOR", "PARAMETER_DECLARATION",
  "IDENTIFIER_LIST", "TYPE_NAME", "ABSTRACT_DECLARATOR",
  "ARRAY_DIRECT_ABSTRACT_DECLARATOR",
  "PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR", "INITIALIZER_LIST",
  "DESIGNATOR_LIST", "ARRAY_DESIGNATOR", "DOT_DESIGNATOR",
  "IDENTIFIER_LABELED_STATEMENT", "CASE_LABELED_STATEMENT",
  "BLOCK_ITEM_LIST", "EXPRESSION_STATEMENT", "IF_STATEMENT",
  "SWITCH_STATEMENT", "WHILE_STATEMENT", "DO_WHILE_STATEMENT",
  "FOR_STATEMENT", "GOTO_STATEMENT", "LOOP_JUMP_STATEMENT",
  "RETURN_STATEMENT", "TRANSLATION_UNIT", "FUNCTION_DEFINITION",
  "DECLARATION_LIST", "COMPOUND_STATEMENT", "'('", "')'", "'['", "']'",
  "'.'", "'{'", "'}'", "','", "'&'", "'*'", "'+'", "'-'", "'~'", "'!'",
  "'/'", "'%'", "'<'", "'>'", "'^'", "'|'", "'?'", "':'", "'='", "';'",
  "$accept", "string_literal", "primary_expression", "postfix_expression",
  "argument_expression_list", "unary_expression", "unary_operator",
  "cast_expression", "multiplicative_expression", "additive_expression",
  "shift_expression", "relational_expression", "equality_expression",
  "and_expression", "exclusive_or_expression", "inclusive_or_expression",
  "logical_and_expression", "logical_or_expression",
  "conditional_expression", "assignment_expression", "assignment_operator",
  "expression", "constant_expression", "declaration",
  "declaration_specifiers", "init_declarator_list", "init_declarator",
  "storage_class_specifier", "type_specifier", "struct_or_union_specifier",
  "struct_or_union", "struct_declaration_list", "struct_declaration",
  "specifier_qualifier_list", "struct_declarator_list",
  "struct_declarator", "enum_specifier", "enumerator_list", "enumerator",
  "type_qualifier", "function_specifier", "declarator",
  "direct_declarator", "pointer", "type_qualifier_list",
  "parameter_type_list", "parameter_list", "parameter_declaration",
  "identifier_list", "type_name", "abstract_declarator",
  "direct_abstract_declarator", "initializer", "initializer_list",
  "designation", "designator_list", "designator", "statement",
  "labeled_statement", "compound_statement", "block_item_list",
  "block_item", "expression_statement", "selection_statement",
  "iteration_statement", "jump_statement", "translation_unit",
  "external_declaration", "function_definition", "declaration_list", 0
};
#endif

# ifdef YYPRINT
/* YYTOKNUM[YYLEX-NUM] -- Internal token number corresponding to
   token YYLEX-NUM.  */
static const yytype_uint16 yytoknum[] =
{
       0,   256,   257,   258,   259,   260,   261,   262,   263,   264,
     265,   266,   267,   268,   269,   270,   271,   272,   273,   274,
     275,   276,   277,   278,   279,   280,   281,   282,   283,   284,
     285,   286,   287,   288,   289,   290,   291,   292,   293,   294,
     295,   296,   297,   298,   299,   300,   301,   302,   303,   304,
     305,   306,   307,   308,   309,   310,   311,   312,   313,   314,
     315,   316,   317,   318,   319,   320,   321,   322,   323,   324,
     325,   326,   327,   328,   329,   330,   331,   332,   333,   334,
     335,   336,   337,   338,   339,   340,   341,   342,   343,   344,
     345,   346,   347,   348,   349,   350,   351,   352,   353,   354,
     355,   356,   357,   358,   359,   360,   361,   362,   363,   364,
     365,   366,   367,   368,   369,   370,   371,   372,   373,   374,
     375,   376,   377,   378,   379,   380,   381,   382,   383,   384,
      40,    41,    91,    93,    46,   123,   125,    44,    38,    42,
      43,    45,   126,    33,    47,    37,    60,    62,    94,   124,
      63,    58,    61,    59
};
# endif

/* YYR1[YYN] -- Symbol number of symbol that rule YYN derives.  */
static const yytype_uint8 yyr1[] =
{
       0,   154,   155,   155,   156,   156,   156,   156,   157,   157,
     157,   157,   157,   157,   157,   157,   157,   157,   158,   158,
     159,   159,   159,   159,   159,   159,   160,   160,   160,   160,
     160,   160,   161,   161,   162,   162,   162,   162,   163,   163,
     163,   164,   164,   164,   165,   165,   165,   165,   165,   166,
     166,   166,   167,   167,   168,   168,   169,   169,   170,   170,
     171,   171,   172,   172,   173,   173,   174,   174,   174,   174,
     174,   174,   174,   174,   174,   174,   174,   175,   175,   176,
     177,   177,   178,   178,   178,   178,   178,   178,   178,   178,
     179,   179,   180,   180,   181,   181,   181,   181,   181,   182,
     182,   182,   182,   182,   182,   182,   182,   182,   182,   182,
     182,   182,   182,   182,   183,   183,   183,   183,   184,   184,
     185,   185,   186,   186,   187,   187,   187,   187,   188,   188,
     189,   189,   189,   190,   190,   190,   190,   190,   190,   191,
     191,   192,   192,   193,   193,   193,   194,   195,   195,   196,
     196,   196,   196,   196,   196,   196,   196,   196,   196,   196,
     196,   196,   197,   197,   197,   197,   198,   198,   199,   199,
     200,   200,   201,   201,   201,   202,   202,   203,   203,   204,
     204,   204,   205,   205,   205,   205,   205,   205,   205,   205,
     205,   205,   205,   206,   206,   206,   207,   207,   207,   207,
     208,   209,   209,   210,   210,   211,   211,   211,   211,   211,
     211,   212,   212,   212,   213,   213,   214,   214,   215,   215,
     216,   216,   217,   217,   217,   218,   218,   218,   218,   218,
     218,   219,   219,   219,   219,   219,   220,   220,   221,   221,
     222,   222,   222,   222,   223,   223
};

/* YYR2[YYN] -- Number of symbols composing right hand side of rule YYN.  */
static const yytype_uint8 yyr2[] =
{
       0,     2,     1,     2,     1,     1,     1,     3,     1,     4,
       3,     4,     3,     3,     2,     2,     6,     7,     1,     3,
       1,     2,     2,     2,     2,     4,     1,     1,     1,     1,
       1,     1,     1,     4,     1,     3,     3,     3,     1,     3,
       3,     1,     3,     3,     1,     3,     3,     3,     3,     1,
       3,     3,     1,     3,     1,     3,     1,     3,     1,     3,
       1,     3,     1,     5,     1,     3,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     3,     1,
       2,     3,     1,     2,     1,     2,     1,     2,     1,     2,
       1,     3,     1,     3,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     5,     4,     2,     2,     1,     1,
       1,     2,     3,     2,     2,     1,     2,     1,     1,     3,
       1,     2,     3,     4,     5,     5,     6,     2,     2,     1,
       3,     1,     3,     1,     1,     1,     1,     2,     1,     1,
       3,     5,     4,     4,     6,     6,     5,     4,     3,     4,
       4,     3,     1,     2,     2,     3,     1,     2,     1,     3,
       1,     3,     2,     2,     1,     1,     3,     1,     2,     1,
       1,     2,     3,     2,     3,     3,     4,     3,     4,     2,
       3,     3,     4,     1,     3,     4,     1,     2,     3,     4,
       2,     1,     2,     3,     2,     1,     1,     1,     1,     1,
       1,     3,     4,     3,     2,     3,     1,     2,     1,     1,
       1,     2,     5,     7,     5,     5,     7,     6,     7,     6,
       7,     3,     2,     2,     2,     3,     1,     2,     1,     1,
       3,     2,     4,     3,     1,     2
};

/* YYDEFACT[STATE-NAME] -- Default rule to reduce with in state
   STATE-NUM when YYTABLE doesn't specify something else to do.  Zero
   means the default is an error.  */
static const yytype_uint8 yydefact[] =
{
       0,   149,    94,    95,    96,    97,    98,   100,   101,   102,
     103,   106,   107,   104,   105,   143,   145,    99,   118,   119,
       0,   113,   109,   110,   144,   108,   146,     0,   162,   239,
       0,    82,    84,   111,     0,   112,    86,    88,     0,   148,
       0,     0,   236,   238,   137,   138,     0,     0,   166,   164,
     163,    80,     0,    90,    92,    83,    85,   116,   117,     0,
      87,    89,     0,   244,     0,   241,     0,     0,     0,   147,
       1,   237,     0,   141,     0,   139,   150,   167,   165,     0,
      81,     0,   243,     0,     0,   125,     0,   120,     0,   127,
       4,     5,     2,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,   214,    26,
      27,    28,    29,    30,    31,   220,     6,     8,    20,    32,
       0,    34,    38,    41,    44,    49,    52,    54,    56,    58,
      60,    62,    64,    77,     0,   218,   219,   205,   206,     0,
     216,   207,   208,   209,   210,    92,   245,   240,   175,   161,
     174,     0,   168,   170,     0,     4,     0,   158,    27,     0,
       0,     0,     0,   133,     0,    91,     0,   193,    93,   242,
       0,   124,   115,   121,     0,   123,     0,   128,   130,   126,
       0,     0,    24,     0,    21,    22,    32,    79,     0,     0,
       0,     0,     0,     0,     0,     0,   232,   233,   234,     0,
       0,   177,     0,     3,     0,    14,    15,     0,     0,     0,
      67,    68,    69,    70,    71,    72,    73,    74,    75,    76,
      66,     0,    23,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,   221,   215,   217,     0,     0,   172,   179,
     173,   180,   159,     0,   160,     0,     0,   157,   153,     0,
     152,    27,     0,   134,     0,   142,   135,   140,     0,     0,
     196,     0,     0,     0,   201,   114,   131,     0,   122,     0,
     211,     0,     0,     0,   213,     0,     0,     0,     0,     0,
       0,   231,   235,     7,     0,   179,   178,     0,    13,    10,
       0,    18,     0,    12,    65,    35,    36,    37,    39,    40,
      42,    43,    47,    48,    45,    46,    50,    51,    53,    55,
      57,    59,    61,     0,    78,   189,     0,     0,   183,    27,
       0,   181,     0,     0,   169,   171,   176,     0,     0,   156,
     151,   136,     0,   204,   194,     0,   197,   200,   202,   129,
     132,    25,     0,   212,     0,     0,     0,     0,     0,     0,
       0,    33,    11,     0,     9,     0,   190,   182,   187,   184,
     191,     0,   185,    27,     0,   154,   155,   203,   195,   198,
       0,   222,   224,   225,     0,     0,     0,     0,     0,     0,
      19,    63,   192,   188,   186,   199,     0,     0,   229,     0,
     227,     0,    16,     0,   223,   226,   230,   228,    17
};

/* YYDEFGOTO[NTERM-NUM].  */
static const yytype_int16 yydefgoto[] =
{
      -1,   116,   117,   118,   300,   119,   120,   121,   122,   123,
     124,   125,   126,   127,   128,   129,   130,   131,   132,   133,
     221,   134,   188,    29,    64,    52,    53,    31,    32,    33,
      34,    86,    87,    88,   176,   177,    35,    74,    75,    36,
      37,    38,    39,    40,    50,   326,   152,   153,   154,   202,
     327,   251,   270,   271,   272,   273,   274,   136,   137,   138,
     139,   140,   141,   142,   143,   144,    41,    42,    43,    66
};

/* YYPACT[STATE-NUM] -- Index in YYTABLE of the portion describing
   STATE-NUM.  */
#define YYPACT_NINF -321
static const yytype_int16 yypact[] =
{
    1188,  -321,  -321,  -321,  -321,  -321,  -321,  -321,  -321,  -321,
    -321,  -321,  -321,  -321,  -321,  -321,  -321,  -321,  -321,  -321,
      17,  -321,  -321,  -321,  -321,  -321,  -321,    16,    -5,  -321,
      60,  1581,  1581,  -321,    43,  -321,  1581,  1581,  1349,    84,
      19,  1072,  -321,  -321,  -117,  -321,    32,   -76,  -321,  -321,
      -5,  -321,   -89,  -321,  1271,  -321,  -321,   -93,  -321,  1595,
    -321,  -321,   357,  -321,    60,  -321,  1349,  1232,   668,    84,
    -321,  -321,    32,   -75,   151,  -321,  -321,  -321,  -321,    16,
    -321,   764,  -321,  1349,  1595,  1595,  1458,  -321,    68,  1595,
     -49,  -321,  -321,   428,   928,   928,   950,   -26,   -50,     3,
      13,   324,   111,   177,    83,    99,   132,   651,  -321,  -321,
    -321,  -321,  -321,  -321,  -321,  -321,   251,  -321,   224,    90,
     950,  -321,    95,   153,   296,    49,   301,   126,   121,   130,
     274,    -3,  -321,  -321,   -71,  -321,  -321,  -321,  -321,   498,
    -321,  -321,  -321,  -321,  -321,   145,  -321,  -321,  -321,  -321,
      12,   168,   164,  -321,   -52,  -321,     9,  -321,   171,   187,
     696,   182,   950,  -321,    14,  -321,   750,  -321,  -321,  -321,
    1472,  -321,  -321,  -321,   950,  -321,   -47,  -321,   173,  -321,
     324,   651,  -321,   651,  -321,  -321,  -321,  -321,   175,   324,
     950,   950,   950,   287,   581,   188,  -321,  -321,  -321,    72,
     -43,    78,   212,  -321,   346,  -321,  -321,   809,   950,   347,
    -321,  -321,  -321,  -321,  -321,  -321,  -321,  -321,  -321,  -321,
    -321,   950,  -321,   950,   950,   950,   950,   950,   950,   950,
     950,   950,   950,   950,   950,   950,   950,   950,   950,   950,
     950,   950,   950,  -321,  -321,  -321,  1116,   841,  -321,    73,
    -321,   131,  -321,  1534,  -321,   352,   779,  -321,  -321,   950,
    -321,   226,   231,  -321,    18,  -321,  -321,  -321,   950,   354,
    -321,   186,   764,   -85,  -321,  -321,  -321,    67,  -321,   950,
    -321,   236,   237,   324,  -321,   -40,   -32,    -9,   239,   305,
     305,  -321,  -321,  -321,  1310,   154,  -321,   855,  -321,  -321,
      45,  -321,   116,  -321,  -321,  -321,  -321,  -321,    95,    95,
     153,   153,   296,   296,   296,   296,    49,    49,   301,   126,
     121,   130,   274,  -106,  -321,  -321,   240,   244,  -321,   271,
     275,   131,  1424,   892,  -321,  -321,  -321,   291,   292,  -321,
    -321,  -321,   293,  -321,  -321,   465,  -321,  -321,  -321,  -321,
    -321,   235,   235,  -321,   324,   324,   324,   950,   907,   921,
     750,  -321,  -321,   950,  -321,   950,  -321,  -321,  -321,  -321,
    -321,   253,  -321,   294,   295,  -321,  -321,  -321,  -321,  -321,
     764,   378,  -321,  -321,   106,   324,   107,   324,   120,   200,
    -321,  -321,  -321,  -321,  -321,  -321,   324,   277,  -321,   324,
    -321,   324,  -321,   736,  -321,  -321,  -321,  -321,  -321
};

/* YYPGOTO[NTERM-NUM].  */
static const yytype_int16 yypgoto[] =
{
    -321,  -321,  -321,  -321,  -321,   -67,  -321,  -104,   112,   117,
      50,   113,   202,   203,   201,   210,   211,  -321,   -87,   -68,
    -321,    -6,  -150,     6,     2,  -321,   362,  -321,   903,  -321,
    -321,   358,   -46,    41,  -321,   176,  -321,   380,  -141,   161,
    -321,   -23,   -34,   -18,   -60,   -66,  -321,   197,  -321,  -129,
    -120,  -238,   -78,    96,  -320,  -321,   184,   -96,  -321,    20,
    -321,   316,   -63,  -321,  -321,  -321,  -321,   419,  -321,   407
};

/* YYTABLE[YYPACT[STATE-NUM]].  What to do in state STATE-NUM.  If
   positive, shift that token.  If negative, reduce the rule which
   number is the opposite.  If zero, do what YYDEFACT says.
   If YYTABLE_NINF, syntax error.  */
#define YYTABLE_NINF -1
static const yytype_uint16 yytable[] =
{
     159,   151,    30,   168,    47,   193,    69,    54,   160,   187,
      49,   331,   265,   167,   240,     1,   222,    73,    72,     1,
      44,    73,     1,   267,   276,   380,   182,   184,   185,   186,
     250,   242,    78,    55,    56,    73,    15,    16,    60,    61,
     173,   145,    84,    30,    63,   365,    57,   268,    79,   269,
      15,    16,   281,   186,   282,    76,   145,   331,    65,    24,
      63,   230,   231,     1,    80,   178,   242,   347,   135,   150,
       1,     1,   146,    24,    82,   187,     1,   162,    45,   254,
     190,   296,   243,   380,   280,   255,   147,   187,   293,   146,
     277,   354,   262,   284,   242,   186,   256,   242,   167,   355,
     199,   200,   180,   169,    58,   242,   278,   186,   210,   211,
     212,   213,   214,   215,   216,   217,   218,   219,   342,   305,
     306,   307,   356,   267,   173,   189,   171,   248,   242,   350,
     179,   290,   249,   191,    28,   155,    91,    92,    93,   301,
      94,    95,   246,   192,   247,   135,    27,   241,   201,    27,
     266,    28,    46,   304,   341,    28,   186,   186,   186,   186,
     186,   186,   186,   186,   186,   186,   186,   186,   186,   186,
     186,   186,   186,   186,   324,   200,   362,   200,    59,   330,
     195,   187,   363,   295,   285,   286,   287,   353,   337,    48,
      27,   338,   187,   361,   346,   232,   233,    27,    27,    28,
     289,   186,   302,   246,   167,   247,    28,    28,   294,   242,
     247,    77,   186,    51,    67,    69,    68,    28,   174,   174,
      89,   175,   201,    47,   201,   292,   358,   359,   249,    48,
     186,   204,   205,   206,   223,   323,   196,   397,   399,   224,
     225,   194,   220,   242,   242,    89,    89,    89,   150,   364,
      89,   401,   197,   242,   178,   150,   203,   242,   381,   382,
     383,   332,   107,   333,   236,   374,   371,   379,    89,   237,
     109,   110,   111,   112,   113,   114,   295,   167,   391,   238,
     312,   313,   314,   315,   294,   198,   247,   163,   164,   398,
     239,   400,   167,   226,   227,   390,   150,    81,   186,   252,
     404,   253,   395,   406,   257,   407,   228,   229,   155,    91,
      92,    93,   167,    94,    95,   234,   235,    48,   263,   264,
     258,    77,   344,   345,   279,   379,   283,    90,    91,    92,
      93,    89,    94,    95,   150,   167,   402,   403,   308,   309,
     288,   291,    89,   297,    89,   310,   311,   316,   317,   298,
     303,   384,   386,   388,   207,   336,   208,   343,   209,   339,
      90,    91,    92,    93,   340,    94,    95,   351,   352,   357,
     360,   366,    96,    97,    98,   367,    99,   100,   101,   102,
     103,   104,   105,   106,   392,     2,     3,     4,     5,     6,
       7,     8,     9,    10,    11,    12,    13,    14,    15,    16,
      17,    18,    19,    20,   368,    96,    97,    98,   369,    99,
     100,   101,   102,   103,   104,   105,   106,    77,    21,    22,
      23,    24,    25,    26,   375,   376,   377,   393,   394,   396,
     405,   155,    91,    92,    93,   107,    94,    95,   318,   320,
     319,   165,   170,   109,   110,   111,   112,   113,   114,   321,
     335,   322,   161,   349,   107,   245,   389,   348,   115,    62,
      71,    83,   109,   110,   111,   112,   113,   114,   155,    91,
      92,    93,     0,    94,    95,     0,     0,   115,     0,     0,
       0,     0,     0,     0,     0,     0,     0,   107,     0,     0,
       0,     0,    62,   108,     0,   109,   110,   111,   112,   113,
     114,    90,    91,    92,    93,     0,    94,    95,     0,     0,
     115,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     2,     3,     4,     5,
       6,     7,     8,     9,    10,    11,    12,    13,    14,    15,
      16,    17,    18,    19,    20,     0,    96,    97,    98,     0,
      99,   100,   101,   102,   103,   104,   105,   106,   181,    21,
      22,    23,    24,    25,    26,     0,   109,   110,   111,   112,
     113,   114,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   155,    91,    92,    93,     0,    94,
      95,     0,     0,     0,     0,   107,     0,   268,     0,   269,
     166,   378,     0,   109,   110,   111,   112,   113,   114,     2,
       3,     4,     5,     6,     7,     8,     9,    10,    11,    12,
      13,    14,    15,    16,    17,    18,    19,    20,   107,     0,
       0,     0,     0,    62,   244,     0,   109,   110,   111,   112,
     113,   114,    21,    22,    23,    24,    25,    26,     0,     0,
       0,   115,     0,     0,   155,    91,    92,    93,     0,    94,
      95,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,   155,    91,    92,    93,     0,    94,    95,     0,     0,
       0,     0,     0,     0,     7,     8,     9,    10,    11,    12,
      13,    14,    15,    16,    17,    18,    19,    20,   156,   155,
      91,    92,    93,     0,    94,    95,     0,     0,     0,    15,
      16,   107,    21,    22,    23,    24,    25,     0,     0,   109,
     110,   111,   112,   113,   114,     0,   259,     0,     0,     0,
       0,     0,    24,     0,   115,     0,     0,    15,    16,   155,
      91,    92,    93,     0,    94,    95,     0,     0,     0,     0,
       0,     0,     0,   155,    91,    92,    93,     0,    94,    95,
      24,     0,     0,     0,     0,     0,     0,   155,    91,    92,
      93,     0,    94,    95,     0,     0,     0,     0,     0,     0,
       0,   107,   155,    91,    92,    93,     0,    94,    95,   109,
     110,   111,   112,   113,   114,     0,     0,     0,   107,     0,
       0,   157,     0,     0,     0,     0,   109,   158,   111,   112,
     113,   114,   155,    91,    92,    93,     0,    94,    95,     0,
      15,    16,     0,     0,     0,     0,   107,     0,     0,   260,
       0,     0,     0,     0,   109,   261,   111,   112,   113,   114,
       0,     0,     0,    24,   155,    91,    92,    93,     0,    94,
      95,     0,     0,     0,     0,     0,     0,     0,   155,    91,
      92,    93,     0,    94,    95,     0,   107,     0,   268,     0,
     269,   166,   408,     0,   109,   110,   111,   112,   113,   114,
     107,     0,   268,     0,   269,   166,     0,     0,   109,   110,
     111,   112,   113,   114,   107,   155,    91,    92,    93,   166,
      94,    95,   109,   110,   111,   112,   113,   114,     0,   107,
     155,    91,    92,    93,     0,    94,    95,   109,   110,   111,
     112,   113,   114,     0,   155,    91,    92,    93,     0,    94,
      95,   155,    91,    92,    93,     0,    94,    95,     0,   107,
     299,     0,     0,     0,     0,     0,     0,   109,   110,   111,
     112,   113,   114,   155,    91,    92,    93,     0,    94,    95,
       0,     0,    85,     0,     0,     0,     0,     0,     0,     0,
       0,   107,     0,     0,   328,     0,     0,     0,     0,   109,
     329,   111,   112,   113,   114,   107,     0,    85,    85,    85,
     360,     0,    85,   109,   110,   111,   112,   113,   114,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      85,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,   107,     0,     0,   372,     0,     0,     0,     0,
     109,   373,   111,   112,   113,   114,     0,   107,   385,     0,
       0,     0,     0,     0,     0,   109,   110,   111,   112,   113,
     114,   107,   387,     0,     0,     0,     0,     0,   183,   109,
     110,   111,   112,   113,   114,     0,   109,   110,   111,   112,
     113,   114,    70,    85,     0,     1,     0,     0,     0,     0,
     107,     0,     0,     0,    85,     0,    85,     0,   109,   110,
     111,   112,   113,   114,     0,     0,     0,     0,     0,     0,
       2,     3,     4,     5,     6,     7,     8,     9,    10,    11,
      12,    13,    14,    15,    16,    17,    18,    19,    20,     1,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    21,    22,    23,    24,    25,    26,     0,
       0,     0,     0,     0,     2,     3,     4,     5,     6,     7,
       8,     9,    10,    11,    12,    13,    14,    15,    16,    17,
      18,    19,    20,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    21,    22,    23,
      24,    25,    26,     0,     0,     0,     0,     0,     0,     0,
       0,     1,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,    27,     0,     0,     0,     0,     0,     0,     0,
       0,    28,     0,     0,     0,     0,     2,     3,     4,     5,
       6,     7,     8,     9,    10,    11,    12,    13,    14,    15,
      16,    17,    18,    19,    20,   148,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,   246,   325,   247,    21,
      22,    23,    24,    25,    26,    28,     0,     0,     0,     0,
       2,     3,     4,     5,     6,     7,     8,     9,    10,    11,
      12,    13,    14,    15,    16,    17,    18,    19,    20,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    21,    22,    23,    24,    25,    26,     2,
       3,     4,     5,     6,     7,     8,     9,    10,    11,    12,
      13,    14,    15,    16,    17,    18,    19,    20,    27,     0,
       0,     0,     0,     0,     0,     0,     0,    28,     0,     0,
       0,     0,    21,    22,    23,    24,    25,    26,     2,     3,
       4,     5,     6,     7,     8,     9,    10,    11,    12,    13,
      14,    15,    16,    17,    18,    19,    20,     0,     0,     0,
       0,     0,     0,   149,     0,     0,     0,     0,     0,     0,
       0,    21,    22,    23,    24,    25,    26,     2,     3,     4,
       5,     6,     7,     8,     9,    10,    11,    12,    13,    14,
      15,    16,    17,    18,    19,    20,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,    62,     0,     0,     0,
      21,    22,    23,    24,    25,    26,     0,     0,     0,     0,
       0,     0,     0,    81,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     294,   325,   247,     0,     0,     0,     0,     0,     0,    28,
       0,     0,     2,     3,     4,     5,     6,     7,     8,     9,
      10,    11,    12,    13,    14,    15,    16,    17,    18,    19,
      20,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,    62,    21,    22,    23,    24,    25,
      26,     7,     8,     9,    10,    11,    12,    13,    14,    15,
      16,    17,    18,    19,    20,     7,     8,     9,    10,    11,
      12,    13,    14,    15,    16,    17,    18,    19,    20,    21,
      22,    23,    24,    25,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    21,    22,    23,    24,    25,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,   370,     0,     0,     0,     0,
       0,     0,     2,     3,     4,     5,     6,     7,     8,     9,
      10,    11,    12,    13,    14,    15,    16,    17,    18,    19,
      20,   334,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,   172,    21,    22,    23,    24,    25,
      26,     0,     0,     0,     0,     0,     0,     0,   275,     2,
       3,     4,     5,     6,     7,     8,     9,    10,    11,    12,
      13,    14,    15,    16,    17,    18,    19,    20,     7,     8,
       9,    10,    11,    12,    13,    14,    15,    16,    17,    18,
      19,    20,    21,    22,    23,    24,    25,    26,     0,     0,
       0,     0,     0,     0,     0,     0,    21,    22,    23,    24,
      25
};

static const yytype_int16 yycheck[] =
{
      68,    67,     0,    81,    27,   101,    40,    30,    68,    96,
      28,   249,   162,    81,    17,     3,   120,     3,   135,     3,
       3,     3,     3,   164,   174,   345,    93,    94,    95,    96,
     150,   137,    50,    31,    32,     3,    41,    42,    36,    37,
      86,    64,   135,    41,    38,   151,     3,   132,   137,   134,
      41,    42,   181,   120,   183,   131,    79,   295,    38,    64,
      54,    12,    13,     3,   153,    88,   137,   152,    62,    67,
       3,     3,    66,    64,    54,   162,     3,   152,    61,   131,
     130,   201,   153,   403,   180,   137,    66,   174,   131,    83,
     137,   131,   160,   189,   137,   162,   156,   137,   166,   131,
     106,   107,   151,    83,    61,   137,   153,   174,    18,    19,
      20,    21,    22,    23,    24,    25,    26,    27,   268,   223,
     224,   225,   131,   264,   170,   151,    85,   150,   137,   279,
      89,   194,   150,   130,   139,     3,     4,     5,     6,   207,
       8,     9,   130,   130,   132,   139,   130,   150,   107,   130,
     136,   139,   135,   221,   136,   139,   223,   224,   225,   226,
     227,   228,   229,   230,   231,   232,   233,   234,   235,   236,
     237,   238,   239,   240,   242,   181,   131,   183,   135,   247,
       3,   268,   137,   201,   190,   191,   192,   283,   256,    28,
     130,   259,   279,   297,   272,   146,   147,   130,   130,   139,
     194,   268,   208,   130,   272,   132,   139,   139,   130,   137,
     132,    50,   279,   153,   130,   249,   132,   139,   151,   151,
      59,   153,   181,   246,   183,   153,   289,   290,   246,    68,
     297,     7,     8,     9,   139,   241,   153,   131,   131,   144,
     145,   130,   152,   137,   137,    84,    85,    86,   246,   133,
      89,   131,   153,   137,   277,   253,     5,   137,   354,   355,
     356,   130,   130,   132,   138,   333,   332,   345,   107,   148,
     138,   139,   140,   141,   142,   143,   294,   345,   365,   149,
     230,   231,   232,   233,   130,   153,   132,   136,   137,   385,
      16,   387,   360,   140,   141,   363,   294,   152,   365,   131,
     396,   137,   380,   399,   133,   401,    10,    11,     3,     4,
       5,     6,   380,     8,     9,    14,    15,   156,   136,   137,
     133,   160,   136,   137,   151,   403,   151,     3,     4,     5,
       6,   170,     8,     9,   332,   403,   136,   137,   226,   227,
      53,   153,   181,   131,   183,   228,   229,   234,   235,     3,
       3,   357,   358,   359,   130,     3,   132,     3,   134,   133,
       3,     4,     5,     6,   133,     8,     9,   131,   131,   130,
     135,   131,    48,    49,    50,   131,    52,    53,    54,    55,
      56,    57,    58,    59,   131,    28,    29,    30,    31,    32,
      33,    34,    35,    36,    37,    38,    39,    40,    41,    42,
      43,    44,    45,    46,   133,    48,    49,    50,   133,    52,
      53,    54,    55,    56,    57,    58,    59,   256,    61,    62,
      63,    64,    65,    66,   133,   133,   133,   133,   133,    51,
     153,     3,     4,     5,     6,   130,     8,     9,   236,   238,
     237,    79,    84,   138,   139,   140,   141,   142,   143,   239,
     253,   240,    72,   277,   130,   139,   360,   273,   153,   135,
      41,    54,   138,   139,   140,   141,   142,   143,     3,     4,
       5,     6,    -1,     8,     9,    -1,    -1,   153,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   130,    -1,    -1,
      -1,    -1,   135,   136,    -1,   138,   139,   140,   141,   142,
     143,     3,     4,     5,     6,    -1,     8,     9,    -1,    -1,
     153,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    28,    29,    30,    31,
      32,    33,    34,    35,    36,    37,    38,    39,    40,    41,
      42,    43,    44,    45,    46,    -1,    48,    49,    50,    -1,
      52,    53,    54,    55,    56,    57,    58,    59,   130,    61,
      62,    63,    64,    65,    66,    -1,   138,   139,   140,   141,
     142,   143,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,     3,     4,     5,     6,    -1,     8,
       9,    -1,    -1,    -1,    -1,   130,    -1,   132,    -1,   134,
     135,   136,    -1,   138,   139,   140,   141,   142,   143,    28,
      29,    30,    31,    32,    33,    34,    35,    36,    37,    38,
      39,    40,    41,    42,    43,    44,    45,    46,   130,    -1,
      -1,    -1,    -1,   135,   136,    -1,   138,   139,   140,   141,
     142,   143,    61,    62,    63,    64,    65,    66,    -1,    -1,
      -1,   153,    -1,    -1,     3,     4,     5,     6,    -1,     8,
       9,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,     3,     4,     5,     6,    -1,     8,     9,    -1,    -1,
      -1,    -1,    -1,    -1,    33,    34,    35,    36,    37,    38,
      39,    40,    41,    42,    43,    44,    45,    46,    30,     3,
       4,     5,     6,    -1,     8,     9,    -1,    -1,    -1,    41,
      42,   130,    61,    62,    63,    64,    65,    -1,    -1,   138,
     139,   140,   141,   142,   143,    -1,    30,    -1,    -1,    -1,
      -1,    -1,    64,    -1,   153,    -1,    -1,    41,    42,     3,
       4,     5,     6,    -1,     8,     9,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,     3,     4,     5,     6,    -1,     8,     9,
      64,    -1,    -1,    -1,    -1,    -1,    -1,     3,     4,     5,
       6,    -1,     8,     9,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   130,     3,     4,     5,     6,    -1,     8,     9,   138,
     139,   140,   141,   142,   143,    -1,    -1,    -1,   130,    -1,
      -1,   133,    -1,    -1,    -1,    -1,   138,   139,   140,   141,
     142,   143,     3,     4,     5,     6,    -1,     8,     9,    -1,
      41,    42,    -1,    -1,    -1,    -1,   130,    -1,    -1,   133,
      -1,    -1,    -1,    -1,   138,   139,   140,   141,   142,   143,
      -1,    -1,    -1,    64,     3,     4,     5,     6,    -1,     8,
       9,    -1,    -1,    -1,    -1,    -1,    -1,    -1,     3,     4,
       5,     6,    -1,     8,     9,    -1,   130,    -1,   132,    -1,
     134,   135,   136,    -1,   138,   139,   140,   141,   142,   143,
     130,    -1,   132,    -1,   134,   135,    -1,    -1,   138,   139,
     140,   141,   142,   143,   130,     3,     4,     5,     6,   135,
       8,     9,   138,   139,   140,   141,   142,   143,    -1,   130,
       3,     4,     5,     6,    -1,     8,     9,   138,   139,   140,
     141,   142,   143,    -1,     3,     4,     5,     6,    -1,     8,
       9,     3,     4,     5,     6,    -1,     8,     9,    -1,   130,
     131,    -1,    -1,    -1,    -1,    -1,    -1,   138,   139,   140,
     141,   142,   143,     3,     4,     5,     6,    -1,     8,     9,
      -1,    -1,    59,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   130,    -1,    -1,   133,    -1,    -1,    -1,    -1,   138,
     139,   140,   141,   142,   143,   130,    -1,    84,    85,    86,
     135,    -1,    89,   138,   139,   140,   141,   142,   143,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     107,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   130,    -1,    -1,   133,    -1,    -1,    -1,    -1,
     138,   139,   140,   141,   142,   143,    -1,   130,   131,    -1,
      -1,    -1,    -1,    -1,    -1,   138,   139,   140,   141,   142,
     143,   130,   131,    -1,    -1,    -1,    -1,    -1,   130,   138,
     139,   140,   141,   142,   143,    -1,   138,   139,   140,   141,
     142,   143,     0,   170,    -1,     3,    -1,    -1,    -1,    -1,
     130,    -1,    -1,    -1,   181,    -1,   183,    -1,   138,   139,
     140,   141,   142,   143,    -1,    -1,    -1,    -1,    -1,    -1,
      28,    29,    30,    31,    32,    33,    34,    35,    36,    37,
      38,    39,    40,    41,    42,    43,    44,    45,    46,     3,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    61,    62,    63,    64,    65,    66,    -1,
      -1,    -1,    -1,    -1,    28,    29,    30,    31,    32,    33,
      34,    35,    36,    37,    38,    39,    40,    41,    42,    43,
      44,    45,    46,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    61,    62,    63,
      64,    65,    66,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,     3,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,   130,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,   139,    -1,    -1,    -1,    -1,    28,    29,    30,    31,
      32,    33,    34,    35,    36,    37,    38,    39,    40,    41,
      42,    43,    44,    45,    46,     3,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   130,   131,   132,    61,
      62,    63,    64,    65,    66,   139,    -1,    -1,    -1,    -1,
      28,    29,    30,    31,    32,    33,    34,    35,    36,    37,
      38,    39,    40,    41,    42,    43,    44,    45,    46,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    61,    62,    63,    64,    65,    66,    28,
      29,    30,    31,    32,    33,    34,    35,    36,    37,    38,
      39,    40,    41,    42,    43,    44,    45,    46,   130,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,   139,    -1,    -1,
      -1,    -1,    61,    62,    63,    64,    65,    66,    28,    29,
      30,    31,    32,    33,    34,    35,    36,    37,    38,    39,
      40,    41,    42,    43,    44,    45,    46,    -1,    -1,    -1,
      -1,    -1,    -1,   131,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    61,    62,    63,    64,    65,    66,    28,    29,    30,
      31,    32,    33,    34,    35,    36,    37,    38,    39,    40,
      41,    42,    43,    44,    45,    46,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,   135,    -1,    -1,    -1,
      61,    62,    63,    64,    65,    66,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,   152,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
     130,   131,   132,    -1,    -1,    -1,    -1,    -1,    -1,   139,
      -1,    -1,    28,    29,    30,    31,    32,    33,    34,    35,
      36,    37,    38,    39,    40,    41,    42,    43,    44,    45,
      46,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   135,    61,    62,    63,    64,    65,
      66,    33,    34,    35,    36,    37,    38,    39,    40,    41,
      42,    43,    44,    45,    46,    33,    34,    35,    36,    37,
      38,    39,    40,    41,    42,    43,    44,    45,    46,    61,
      62,    63,    64,    65,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    61,    62,    63,    64,    65,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,   131,    -1,    -1,    -1,    -1,
      -1,    -1,    28,    29,    30,    31,    32,    33,    34,    35,
      36,    37,    38,    39,    40,    41,    42,    43,    44,    45,
      46,    47,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    -1,   136,    61,    62,    63,    64,    65,
      66,    -1,    -1,    -1,    -1,    -1,    -1,    -1,   136,    28,
      29,    30,    31,    32,    33,    34,    35,    36,    37,    38,
      39,    40,    41,    42,    43,    44,    45,    46,    33,    34,
      35,    36,    37,    38,    39,    40,    41,    42,    43,    44,
      45,    46,    61,    62,    63,    64,    65,    66,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    61,    62,    63,    64,
      65
};

/* YYSTOS[STATE-NUM] -- The (internal number of the) accessing
   symbol of state STATE-NUM.  */
static const yytype_uint8 yystos[] =
{
       0,     3,    28,    29,    30,    31,    32,    33,    34,    35,
      36,    37,    38,    39,    40,    41,    42,    43,    44,    45,
      46,    61,    62,    63,    64,    65,    66,   130,   139,   177,
     178,   181,   182,   183,   184,   190,   193,   194,   195,   196,
     197,   220,   221,   222,     3,    61,   135,   195,   193,   197,
     198,   153,   179,   180,   195,   178,   178,     3,    61,   135,
     178,   178,   135,   177,   178,   213,   223,   130,   132,   196,
       0,   221,   135,     3,   191,   192,   131,   193,   197,   137,
     153,   152,   213,   223,   135,   182,   185,   186,   187,   193,
       3,     4,     5,     6,     8,     9,    48,    49,    50,    52,
      53,    54,    55,    56,    57,    58,    59,   130,   136,   138,
     139,   140,   141,   142,   143,   153,   155,   156,   157,   159,
     160,   161,   162,   163,   164,   165,   166,   167,   168,   169,
     170,   171,   172,   173,   175,   177,   211,   212,   213,   214,
     215,   216,   217,   218,   219,   195,   177,   213,     3,   131,
     178,   199,   200,   201,   202,     3,    30,   133,   139,   173,
     198,   191,   152,   136,   137,   180,   135,   173,   206,   213,
     185,   187,   136,   186,   151,   153,   188,   189,   195,   187,
     151,   130,   159,   130,   159,   159,   159,   172,   176,   151,
     130,   130,   130,   211,   130,     3,   153,   153,   153,   175,
     175,   187,   203,     5,     7,     8,     9,   130,   132,   134,
      18,    19,    20,    21,    22,    23,    24,    25,    26,    27,
     152,   174,   161,   139,   144,   145,   140,   141,    10,    11,
      12,    13,   146,   147,    14,    15,   138,   148,   149,    16,
      17,   150,   137,   153,   136,   215,   130,   132,   195,   197,
     204,   205,   131,   137,   131,   137,   198,   133,   133,    30,
     133,   139,   173,   136,   137,   176,   136,   192,   132,   134,
     206,   207,   208,   209,   210,   136,   176,   137,   153,   151,
     211,   203,   203,   151,   211,   175,   175,   175,    53,   177,
     216,   153,   153,   131,   130,   197,   204,   131,     3,   131,
     158,   173,   175,     3,   173,   161,   161,   161,   162,   162,
     163,   163,   164,   164,   164,   164,   165,   165,   166,   167,
     168,   169,   170,   175,   173,   131,   199,   204,   133,   139,
     173,   205,   130,   132,    47,   201,     3,   173,   173,   133,
     133,   136,   176,     3,   136,   137,   206,   152,   210,   189,
     176,   131,   131,   211,   131,   131,   131,   130,   216,   216,
     135,   161,   131,   137,   133,   151,   131,   131,   133,   133,
     131,   199,   133,   139,   173,   133,   133,   133,   136,   206,
     208,   211,   211,   211,   175,   131,   175,   131,   175,   207,
     173,   172,   131,   133,   133,   206,    51,   131,   211,   131,
     211,   131,   136,   137,   211,   153,   211,   211,   136
};

#define yyerrok		(yyerrstatus = 0)
#define yyclearin	(yychar = YYEMPTY)
#define YYEMPTY		(-2)
#define YYEOF		0

#define YYACCEPT	goto yyacceptlab
#define YYABORT		goto yyabortlab
#define YYERROR		goto yyerrorlab


/* Like YYERROR except do call yyerror.  This remains here temporarily
   to ease the transition to the new meaning of YYERROR, for GCC.
   Once GCC version 2 has supplanted version 1, this can go.  */

#define YYFAIL		goto yyerrlab

#define YYRECOVERING()  (!!yyerrstatus)

#define YYBACKUP(Token, Value)					\
do								\
  if (yychar == YYEMPTY && yylen == 1)				\
    {								\
      yychar = (Token);						\
      yylval = (Value);						\
      yytoken = YYTRANSLATE (yychar);				\
      YYPOPSTACK (1);						\
      goto yybackup;						\
    }								\
  else								\
    {								\
      yyerror (YY_("syntax error: cannot back up")); \
      YYERROR;							\
    }								\
while (YYID (0))


#define YYTERROR	1
#define YYERRCODE	256


/* YYLLOC_DEFAULT -- Set CURRENT to span from RHS[1] to RHS[N].
   If N is 0, then set CURRENT to the empty location which ends
   the previous symbol: RHS[0] (always defined).  */

#define YYRHSLOC(Rhs, K) ((Rhs)[K])
#ifndef YYLLOC_DEFAULT
# define YYLLOC_DEFAULT(Current, Rhs, N)				\
    do									\
      if (YYID (N))                                                    \
	{								\
	  (Current).first_line   = YYRHSLOC (Rhs, 1).first_line;	\
	  (Current).first_column = YYRHSLOC (Rhs, 1).first_column;	\
	  (Current).last_line    = YYRHSLOC (Rhs, N).last_line;		\
	  (Current).last_column  = YYRHSLOC (Rhs, N).last_column;	\
	}								\
      else								\
	{								\
	  (Current).first_line   = (Current).last_line   =		\
	    YYRHSLOC (Rhs, 0).last_line;				\
	  (Current).first_column = (Current).last_column =		\
	    YYRHSLOC (Rhs, 0).last_column;				\
	}								\
    while (YYID (0))
#endif


/* YY_LOCATION_PRINT -- Print the location on the stream.
   This macro was not mandated originally: define only if we know
   we won't break user code: when these are the locations we know.  */

#ifndef YY_LOCATION_PRINT
# if YYLTYPE_IS_TRIVIAL
#  define YY_LOCATION_PRINT(File, Loc)			\
     fprintf (File, "%d.%d-%d.%d",			\
	      (Loc).first_line, (Loc).first_column,	\
	      (Loc).last_line,  (Loc).last_column)
# else
#  define YY_LOCATION_PRINT(File, Loc) ((void) 0)
# endif
#endif


/* YYLEX -- calling `yylex' with the right arguments.  */

#ifdef YYLEX_PARAM
# define YYLEX yylex (YYLEX_PARAM)
#else
# define YYLEX yylex ()
#endif

/* Enable debugging if requested.  */
#if YYDEBUG

# ifndef YYFPRINTF
#  include <stdio.h> /* INFRINGES ON USER NAME SPACE */
#  define YYFPRINTF fprintf
# endif

# define YYDPRINTF(Args)			\
do {						\
  if (yydebug)					\
    YYFPRINTF Args;				\
} while (YYID (0))

# define YY_SYMBOL_PRINT(Title, Type, Value, Location)			  \
do {									  \
  if (yydebug)								  \
    {									  \
      YYFPRINTF (stderr, "%s ", Title);					  \
      yy_symbol_print (stderr,						  \
		  Type, Value); \
      YYFPRINTF (stderr, "\n");						  \
    }									  \
} while (YYID (0))


/*--------------------------------.
| Print this symbol on YYOUTPUT.  |
`--------------------------------*/

/*ARGSUSED*/
#if (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
static void
yy_symbol_value_print (FILE *yyoutput, int yytype, YYSTYPE const * const yyvaluep)
#else
static void
yy_symbol_value_print (yyoutput, yytype, yyvaluep)
    FILE *yyoutput;
    int yytype;
    YYSTYPE const * const yyvaluep;
#endif
{
  if (!yyvaluep)
    return;
# ifdef YYPRINT
  if (yytype < YYNTOKENS)
    YYPRINT (yyoutput, yytoknum[yytype], *yyvaluep);
# else
  YYUSE (yyoutput);
# endif
  switch (yytype)
    {
      default:
	break;
    }
}


/*--------------------------------.
| Print this symbol on YYOUTPUT.  |
`--------------------------------*/

#if (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
static void
yy_symbol_print (FILE *yyoutput, int yytype, YYSTYPE const * const yyvaluep)
#else
static void
yy_symbol_print (yyoutput, yytype, yyvaluep)
    FILE *yyoutput;
    int yytype;
    YYSTYPE const * const yyvaluep;
#endif
{
  if (yytype < YYNTOKENS)
    YYFPRINTF (yyoutput, "token %s (", yytname[yytype]);
  else
    YYFPRINTF (yyoutput, "nterm %s (", yytname[yytype]);

  yy_symbol_value_print (yyoutput, yytype, yyvaluep);
  YYFPRINTF (yyoutput, ")");
}

/*------------------------------------------------------------------.
| yy_stack_print -- Print the state stack from its BOTTOM up to its |
| TOP (included).                                                   |
`------------------------------------------------------------------*/

#if (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
static void
yy_stack_print (yytype_int16 *bottom, yytype_int16 *top)
#else
static void
yy_stack_print (bottom, top)
    yytype_int16 *bottom;
    yytype_int16 *top;
#endif
{
  YYFPRINTF (stderr, "Stack now");
  for (; bottom <= top; ++bottom)
    YYFPRINTF (stderr, " %d", *bottom);
  YYFPRINTF (stderr, "\n");
}

# define YY_STACK_PRINT(Bottom, Top)				\
do {								\
  if (yydebug)							\
    yy_stack_print ((Bottom), (Top));				\
} while (YYID (0))


/*------------------------------------------------.
| Report that the YYRULE is going to be reduced.  |
`------------------------------------------------*/

#if (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
static void
yy_reduce_print (YYSTYPE *yyvsp, int yyrule)
#else
static void
yy_reduce_print (yyvsp, yyrule)
    YYSTYPE *yyvsp;
    int yyrule;
#endif
{
  int yynrhs = yyr2[yyrule];
  int yyi;
  unsigned long int yylno = yyrline[yyrule];
  YYFPRINTF (stderr, "Reducing stack by rule %d (line %lu):\n",
	     yyrule - 1, yylno);
  /* The symbols being reduced.  */
  for (yyi = 0; yyi < yynrhs; yyi++)
    {
      fprintf (stderr, "   $%d = ", yyi + 1);
      yy_symbol_print (stderr, yyrhs[yyprhs[yyrule] + yyi],
		       &(yyvsp[(yyi + 1) - (yynrhs)])
		       		       );
      fprintf (stderr, "\n");
    }
}

# define YY_REDUCE_PRINT(Rule)		\
do {					\
  if (yydebug)				\
    yy_reduce_print (yyvsp, Rule); \
} while (YYID (0))

/* Nonzero means print parse trace.  It is left uninitialized so that
   multiple parsers can coexist.  */
int yydebug;
#else /* !YYDEBUG */
# define YYDPRINTF(Args)
# define YY_SYMBOL_PRINT(Title, Type, Value, Location)
# define YY_STACK_PRINT(Bottom, Top)
# define YY_REDUCE_PRINT(Rule)
#endif /* !YYDEBUG */


/* YYINITDEPTH -- initial size of the parser's stacks.  */
#ifndef	YYINITDEPTH
# define YYINITDEPTH 200
#endif

/* YYMAXDEPTH -- maximum size the stacks can grow to (effective only
   if the built-in stack extension method is used).

   Do not make this value too large; the results are undefined if
   YYSTACK_ALLOC_MAXIMUM < YYSTACK_BYTES (YYMAXDEPTH)
   evaluated with infinite-precision integer arithmetic.  */

#ifndef YYMAXDEPTH
# define YYMAXDEPTH 10000
#endif



#if YYERROR_VERBOSE

# ifndef yystrlen
#  if defined __GLIBC__ && defined _STRING_H
#   define yystrlen strlen
#  else
/* Return the length of YYSTR.  */
#if (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
static YYSIZE_T
yystrlen (const char *yystr)
#else
static YYSIZE_T
yystrlen (yystr)
    const char *yystr;
#endif
{
  YYSIZE_T yylen;
  for (yylen = 0; yystr[yylen]; yylen++)
    continue;
  return yylen;
}
#  endif
# endif

# ifndef yystpcpy
#  if defined __GLIBC__ && defined _STRING_H && defined _GNU_SOURCE
#   define yystpcpy stpcpy
#  else
/* Copy YYSRC to YYDEST, returning the address of the terminating '\0' in
   YYDEST.  */
#if (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
static char *
yystpcpy (char *yydest, const char *yysrc)
#else
static char *
yystpcpy (yydest, yysrc)
    char *yydest;
    const char *yysrc;
#endif
{
  char *yyd = yydest;
  const char *yys = yysrc;

  while ((*yyd++ = *yys++) != '\0')
    continue;

  return yyd - 1;
}
#  endif
# endif

# ifndef yytnamerr
/* Copy to YYRES the contents of YYSTR after stripping away unnecessary
   quotes and backslashes, so that it's suitable for yyerror.  The
   heuristic is that double-quoting is unnecessary unless the string
   contains an apostrophe, a comma, or backslash (other than
   backslash-backslash).  YYSTR is taken from yytname.  If YYRES is
   null, do not copy; instead, return the length of what the result
   would have been.  */
static YYSIZE_T
yytnamerr (char *yyres, const char *yystr)
{
  if (*yystr == '"')
    {
      YYSIZE_T yyn = 0;
      char const *yyp = yystr;

      for (;;)
	switch (*++yyp)
	  {
	  case '\'':
	  case ',':
	    goto do_not_strip_quotes;

	  case '\\':
	    if (*++yyp != '\\')
	      goto do_not_strip_quotes;
	    /* Fall through.  */
	  default:
	    if (yyres)
	      yyres[yyn] = *yyp;
	    yyn++;
	    break;

	  case '"':
	    if (yyres)
	      yyres[yyn] = '\0';
	    return yyn;
	  }
    do_not_strip_quotes: ;
    }

  if (! yyres)
    return yystrlen (yystr);

  return yystpcpy (yyres, yystr) - yyres;
}
# endif

/* Copy into YYRESULT an error message about the unexpected token
   YYCHAR while in state YYSTATE.  Return the number of bytes copied,
   including the terminating null byte.  If YYRESULT is null, do not
   copy anything; just return the number of bytes that would be
   copied.  As a special case, return 0 if an ordinary "syntax error"
   message will do.  Return YYSIZE_MAXIMUM if overflow occurs during
   size calculation.  */
static YYSIZE_T
yysyntax_error (char *yyresult, int yystate, int yychar)
{
  int yyn = yypact[yystate];

  if (! (YYPACT_NINF < yyn && yyn <= YYLAST))
    return 0;
  else
    {
      int yytype = YYTRANSLATE (yychar);
      YYSIZE_T yysize0 = yytnamerr (0, yytname[yytype]);
      YYSIZE_T yysize = yysize0;
      YYSIZE_T yysize1;
      int yysize_overflow = 0;
      enum { YYERROR_VERBOSE_ARGS_MAXIMUM = 5 };
      char const *yyarg[YYERROR_VERBOSE_ARGS_MAXIMUM];
      int yyx;

# if 0
      /* This is so xgettext sees the translatable formats that are
	 constructed on the fly.  */
      YY_("syntax error, unexpected %s");
      YY_("syntax error, unexpected %s, expecting %s");
      YY_("syntax error, unexpected %s, expecting %s or %s");
      YY_("syntax error, unexpected %s, expecting %s or %s or %s");
      YY_("syntax error, unexpected %s, expecting %s or %s or %s or %s");
# endif
      char *yyfmt;
      char const *yyf;
      static char const yyunexpected[] = "syntax error, unexpected %s";
      static char const yyexpecting[] = ", expecting %s";
      static char const yyor[] = " or %s";
      char yyformat[sizeof yyunexpected
		    + sizeof yyexpecting - 1
		    + ((YYERROR_VERBOSE_ARGS_MAXIMUM - 2)
		       * (sizeof yyor - 1))];
      char const *yyprefix = yyexpecting;

      /* Start YYX at -YYN if negative to avoid negative indexes in
	 YYCHECK.  */
      int yyxbegin = yyn < 0 ? -yyn : 0;

      /* Stay within bounds of both yycheck and yytname.  */
      int yychecklim = YYLAST - yyn + 1;
      int yyxend = yychecklim < YYNTOKENS ? yychecklim : YYNTOKENS;
      int yycount = 1;

      yyarg[0] = yytname[yytype];
      yyfmt = yystpcpy (yyformat, yyunexpected);

      for (yyx = yyxbegin; yyx < yyxend; ++yyx)
	if (yycheck[yyx + yyn] == yyx && yyx != YYTERROR)
	  {
	    if (yycount == YYERROR_VERBOSE_ARGS_MAXIMUM)
	      {
		yycount = 1;
		yysize = yysize0;
		yyformat[sizeof yyunexpected - 1] = '\0';
		break;
	      }
	    yyarg[yycount++] = yytname[yyx];
	    yysize1 = yysize + yytnamerr (0, yytname[yyx]);
	    yysize_overflow |= (yysize1 < yysize);
	    yysize = yysize1;
	    yyfmt = yystpcpy (yyfmt, yyprefix);
	    yyprefix = yyor;
	  }

      yyf = YY_(yyformat);
      yysize1 = yysize + yystrlen (yyf);
      yysize_overflow |= (yysize1 < yysize);
      yysize = yysize1;

      if (yysize_overflow)
	return YYSIZE_MAXIMUM;

      if (yyresult)
	{
	  /* Avoid sprintf, as that infringes on the user's name space.
	     Don't have undefined behavior even if the translation
	     produced a string with the wrong number of "%s"s.  */
	  char *yyp = yyresult;
	  int yyi = 0;
	  while ((*yyp = *yyf) != '\0')
	    {
	      if (*yyp == '%' && yyf[1] == 's' && yyi < yycount)
		{
		  yyp += yytnamerr (yyp, yyarg[yyi++]);
		  yyf += 2;
		}
	      else
		{
		  yyp++;
		  yyf++;
		}
	    }
	}
      return yysize;
    }
}
#endif /* YYERROR_VERBOSE */


/*-----------------------------------------------.
| Release the memory associated to this symbol.  |
`-----------------------------------------------*/

/*ARGSUSED*/
#if (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
static void
yydestruct (const char *yymsg, int yytype, YYSTYPE *yyvaluep)
#else
static void
yydestruct (yymsg, yytype, yyvaluep)
    const char *yymsg;
    int yytype;
    YYSTYPE *yyvaluep;
#endif
{
  YYUSE (yyvaluep);

  if (!yymsg)
    yymsg = "Deleting";
  YY_SYMBOL_PRINT (yymsg, yytype, yyvaluep, yylocationp);

  switch (yytype)
    {

      default:
	break;
    }
}


/* Prevent warnings from -Wmissing-prototypes.  */

#ifdef YYPARSE_PARAM
#if defined __STDC__ || defined __cplusplus
int yyparse (void *YYPARSE_PARAM);
#else
int yyparse ();
#endif
#else /* ! YYPARSE_PARAM */
#if defined __STDC__ || defined __cplusplus
int yyparse (void);
#else
int yyparse ();
#endif
#endif /* ! YYPARSE_PARAM */



/* The look-ahead symbol.  */
int yychar;

/* The semantic value of the look-ahead symbol.  */
YYSTYPE yylval;

/* Number of syntax errors so far.  */
int yynerrs;



/*----------.
| yyparse.  |
`----------*/

#ifdef YYPARSE_PARAM
#if (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
int
yyparse (void *YYPARSE_PARAM)
#else
int
yyparse (YYPARSE_PARAM)
    void *YYPARSE_PARAM;
#endif
#else /* ! YYPARSE_PARAM */
#if (defined __STDC__ || defined __C99__FUNC__ \
     || defined __cplusplus || defined _MSC_VER)
int
yyparse (void)
#else
int
yyparse ()

#endif
#endif
{
  
  int yystate;
  int yyn;
  int yyresult;
  /* Number of tokens to shift before error messages enabled.  */
  int yyerrstatus;
  /* Look-ahead token as an internal (translated) token number.  */
  int yytoken = 0;
#if YYERROR_VERBOSE
  /* Buffer for error messages, and its allocated size.  */
  char yymsgbuf[128];
  char *yymsg = yymsgbuf;
  YYSIZE_T yymsg_alloc = sizeof yymsgbuf;
#endif

  /* Three stacks and their tools:
     `yyss': related to states,
     `yyvs': related to semantic values,
     `yyls': related to locations.

     Refer to the stacks thru separate pointers, to allow yyoverflow
     to reallocate them elsewhere.  */

  /* The state stack.  */
  yytype_int16 yyssa[YYINITDEPTH];
  yytype_int16 *yyss = yyssa;
  yytype_int16 *yyssp;

  /* The semantic value stack.  */
  YYSTYPE yyvsa[YYINITDEPTH];
  YYSTYPE *yyvs = yyvsa;
  YYSTYPE *yyvsp;



#define YYPOPSTACK(N)   (yyvsp -= (N), yyssp -= (N))

  YYSIZE_T yystacksize = YYINITDEPTH;

  /* The variables used to return semantic value and location from the
     action routines.  */
  YYSTYPE yyval;


  /* The number of symbols on the RHS of the reduced rule.
     Keep to zero when no symbol should be popped.  */
  int yylen = 0;

  YYDPRINTF ((stderr, "Starting parse\n"));

  yystate = 0;
  yyerrstatus = 0;
  yynerrs = 0;
  yychar = YYEMPTY;		/* Cause a token to be read.  */

  /* Initialize stack pointers.
     Waste one element of value and location stack
     so that they stay on the same level as the state stack.
     The wasted elements are never initialized.  */

  yyssp = yyss;
  yyvsp = yyvs;

  goto yysetstate;

/*------------------------------------------------------------.
| yynewstate -- Push a new state, which is found in yystate.  |
`------------------------------------------------------------*/
 yynewstate:
  /* In all cases, when you get here, the value and location stacks
     have just been pushed.  So pushing a state here evens the stacks.  */
  yyssp++;

 yysetstate:
  *yyssp = yystate;

  if (yyss + yystacksize - 1 <= yyssp)
    {
      /* Get the current used size of the three stacks, in elements.  */
      YYSIZE_T yysize = yyssp - yyss + 1;

#ifdef yyoverflow
      {
	/* Give user a chance to reallocate the stack.  Use copies of
	   these so that the &'s don't force the real ones into
	   memory.  */
	YYSTYPE *yyvs1 = yyvs;
	yytype_int16 *yyss1 = yyss;


	/* Each stack pointer address is followed by the size of the
	   data in use in that stack, in bytes.  This used to be a
	   conditional around just the two extra args, but that might
	   be undefined if yyoverflow is a macro.  */
	yyoverflow (YY_("memory exhausted"),
		    &yyss1, yysize * sizeof (*yyssp),
		    &yyvs1, yysize * sizeof (*yyvsp),

		    &yystacksize);

	yyss = yyss1;
	yyvs = yyvs1;
      }
#else /* no yyoverflow */
# ifndef YYSTACK_RELOCATE
      goto yyexhaustedlab;
# else
      /* Extend the stack our own way.  */
      if (YYMAXDEPTH <= yystacksize)
	goto yyexhaustedlab;
      yystacksize *= 2;
      if (YYMAXDEPTH < yystacksize)
	yystacksize = YYMAXDEPTH;

      {
	yytype_int16 *yyss1 = yyss;
	union yyalloc *yyptr =
	  (union yyalloc *) YYSTACK_ALLOC (YYSTACK_BYTES (yystacksize));
	if (! yyptr)
	  goto yyexhaustedlab;
	YYSTACK_RELOCATE (yyss);
	YYSTACK_RELOCATE (yyvs);

#  undef YYSTACK_RELOCATE
	if (yyss1 != yyssa)
	  YYSTACK_FREE (yyss1);
      }
# endif
#endif /* no yyoverflow */

      yyssp = yyss + yysize - 1;
      yyvsp = yyvs + yysize - 1;


      YYDPRINTF ((stderr, "Stack size increased to %lu\n",
		  (unsigned long int) yystacksize));

      if (yyss + yystacksize - 1 <= yyssp)
	YYABORT;
    }

  YYDPRINTF ((stderr, "Entering state %d\n", yystate));

  goto yybackup;

/*-----------.
| yybackup.  |
`-----------*/
yybackup:

  /* Do appropriate processing given the current state.  Read a
     look-ahead token if we need one and don't already have one.  */

  /* First try to decide what to do without reference to look-ahead token.  */
  yyn = yypact[yystate];
  if (yyn == YYPACT_NINF)
    goto yydefault;

  /* Not known => get a look-ahead token if don't already have one.  */

  /* YYCHAR is either YYEMPTY or YYEOF or a valid look-ahead symbol.  */
  if (yychar == YYEMPTY)
    {
      YYDPRINTF ((stderr, "Reading a token: "));
      yychar = YYLEX;
    }

  if (yychar <= YYEOF)
    {
      yychar = yytoken = YYEOF;
      YYDPRINTF ((stderr, "Now at end of input.\n"));
    }
  else
    {
      yytoken = YYTRANSLATE (yychar);
      YY_SYMBOL_PRINT ("Next token is", yytoken, &yylval, &yylloc);
    }

  /* If the proper action on seeing token YYTOKEN is to reduce or to
     detect an error, take that action.  */
  yyn += yytoken;
  if (yyn < 0 || YYLAST < yyn || yycheck[yyn] != yytoken)
    goto yydefault;
  yyn = yytable[yyn];
  if (yyn <= 0)
    {
      if (yyn == 0 || yyn == YYTABLE_NINF)
	goto yyerrlab;
      yyn = -yyn;
      goto yyreduce;
    }

  if (yyn == YYFINAL)
    YYACCEPT;

  /* Count tokens shifted since error; after three, turn off error
     status.  */
  if (yyerrstatus)
    yyerrstatus--;

  /* Shift the look-ahead token.  */
  YY_SYMBOL_PRINT ("Shifting", yytoken, &yylval, &yylloc);

  /* Discard the shifted token unless it is eof.  */
  if (yychar != YYEOF)
    yychar = YYEMPTY;

  yystate = yyn;
  *++yyvsp = yylval;

  goto yynewstate;


/*-----------------------------------------------------------.
| yydefault -- do the default action for the current state.  |
`-----------------------------------------------------------*/
yydefault:
  yyn = yydefact[yystate];
  if (yyn == 0)
    goto yyerrlab;
  goto yyreduce;


/*-----------------------------.
| yyreduce -- Do a reduction.  |
`-----------------------------*/
yyreduce:
  /* yyn is the number of a rule to reduce with.  */
  yylen = yyr2[yyn];

  /* If YYLEN is nonzero, implement the default value of the action:
     `$$ = $1'.

     Otherwise, the following line sets YYVAL to garbage.
     This behavior is undocumented and Bison
     users should not rely upon it.  Assigning to YYVAL
     unconditionally makes the parser a bit smaller, and it avoids a
     GCC warning that YYVAL may be used uninitialized.  */
  yyval = yyvsp[1-yylen];


  YY_REDUCE_PRINT (yyn);
  switch (yyn)
    {
        case 2:
#line 55 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 3:
#line 56 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = (yyvsp[(1) - (2)]);
		(yyval)->string_literal.text = (char*) realloc((yyval)->string_literal.text,
			strlen((yyval)->string_literal.text) + strlen((yyvsp[(2) - (2)])->string_literal.text)
					- 1);
		strcpy((yyval)->string_literal.text + strlen((yyval)->string_literal.text) - 1,
			(yyvsp[(2) - (2)])->string_literal.text + 1);
		move_and_append_comments((yyval), (yyvsp[(2) - (2)]));
		free((yyvsp[(2) - (2)])->string_literal.text);
		free((yyvsp[(2) - (2)]));
	}
    break;

  case 4:
#line 69 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 5:
#line 70 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 6:
#line 71 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 7:
#line 72 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = (yyvsp[(2) - (3)]);
		move_and_prepend_comments((yyval), (yyvsp[(1) - (3)]));
		free((yyvsp[(1) - (3)]));
		move_and_append_comments((yyvsp[(2) - (3)]), (yyvsp[(3) - (3)]));
		free((yyvsp[(3) - (3)]));
	}
    break;

  case 8:
#line 82 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 9:
#line 83 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ARRAY_ACCESS, (yyvsp[(1) - (4)]));
		(yyval)->array_access = (ARRAY_ACCESS_STRUCT) { (yyvsp[(1) - (4)]), (yyvsp[(3) - (4)]) };
		move_and_append_comments((yyvsp[(1) - (4)]), (yyvsp[(2) - (4)]));
		free((yyvsp[(2) - (4)]));
		move_and_append_comments((yyvsp[(3) - (4)]), (yyvsp[(4) - (4)]));
		free((yyvsp[(4) - (4)]));
	}
    break;

  case 10:
#line 91 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PARAMETERIZED_EXPRESSION, (yyvsp[(1) - (3)]));
		(yyval)->parameterized_expression = (PARAMETERIZED_EXPRESSION_STRUCT) {
			(yyvsp[(1) - (3)]), NULL
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(3) - (3)]));
		free((yyvsp[(3) - (3)]));
	}
    break;

  case 11:
#line 101 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PARAMETERIZED_EXPRESSION, (yyvsp[(1) - (4)]));
		(yyval)->parameterized_expression = (PARAMETERIZED_EXPRESSION_STRUCT) {
			(yyvsp[(1) - (4)]), (yyvsp[(3) - (4)])
		};
		move_and_append_comments((yyvsp[(1) - (4)]), (yyvsp[(2) - (4)]));
		free((yyvsp[(2) - (4)]));
		move_and_append_comments((yyvsp[(3) - (4)]), (yyvsp[(4) - (4)]));
		free((yyvsp[(4) - (4)]));
	}
    break;

  case 12:
#line 111 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(FIELD_ACCESS, (yyvsp[(1) - (3)]));
		(yyval)->field_access = (FIELD_ACCESS_STRUCT) { 0, (yyvsp[(1) - (3)]), (yyvsp[(3) - (3)])->identifier.id };
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(3) - (3)]));
		free((yyvsp[(3) - (3)]));
	}
    break;

  case 13:
#line 119 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(FIELD_ACCESS, (yyvsp[(1) - (3)]));
		(yyval)->field_access = (FIELD_ACCESS_STRUCT) { 1, (yyvsp[(1) - (3)]), (yyvsp[(3) - (3)])->identifier.id };
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 14:
#line 125 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(INC_DEC_EXPRESSION, (yyvsp[(1) - (2)]));
		(yyval)->inc_dec_expression = (INC_DEC_EXPRESSION_STRUCT) {
			POSTFIX_INC, (yyvsp[(1) - (2)])
		};
		move_and_append_comments((yyvsp[(1) - (2)]), (yyvsp[(2) - (2)]));
		free((yyvsp[(2) - (2)]));
	}
    break;

  case 15:
#line 133 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(INC_DEC_EXPRESSION, (yyvsp[(1) - (2)]));
		(yyval)->inc_dec_expression = (INC_DEC_EXPRESSION_STRUCT) {
			POSTFIX_DEC, (yyvsp[(1) - (2)])
		};
		move_and_append_comments((yyvsp[(1) - (2)]), (yyvsp[(2) - (2)]));
		free((yyvsp[(2) - (2)]));
	}
    break;

  case 16:
#line 141 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(STRUCT_CONSTANT, (yyvsp[(1) - (6)]));
		(yyval)->struct_constant = (STRUCT_CONSTANT_STRUCT) { (yyvsp[(2) - (6)]), (yyvsp[(5) - (6)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (6)]));
		free((yyvsp[(1) - (6)]));
		move_and_append_comments((yyvsp[(2) - (6)]), (yyvsp[(3) - (6)]));
		free((yyvsp[(3) - (6)]));
		move_and_append_comments((yyvsp[(2) - (6)]), (yyvsp[(4) - (6)]));
		free((yyvsp[(4) - (6)]));
		move_and_append_comments((yyvsp[(5) - (6)]), (yyvsp[(6) - (6)]));
		free((yyvsp[(6) - (6)]));
	}
    break;

  case 17:
#line 153 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(STRUCT_CONSTANT, (yyvsp[(1) - (7)]));
		(yyval)->struct_constant = (STRUCT_CONSTANT_STRUCT) { (yyvsp[(2) - (7)]), (yyvsp[(5) - (7)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (7)]));
		free((yyvsp[(1) - (7)]));
		move_and_append_comments((yyvsp[(2) - (7)]), (yyvsp[(3) - (7)]));
		free((yyvsp[(3) - (7)]));
		move_and_append_comments((yyvsp[(2) - (7)]), (yyvsp[(4) - (7)]));
		free((yyvsp[(4) - (7)]));
		move_and_append_comments((yyvsp[(5) - (7)]), (yyvsp[(6) - (7)]));
		free((yyvsp[(6) - (7)]));
		move_and_append_comments((yyvsp[(5) - (7)]), (yyvsp[(7) - (7)]));
		free((yyvsp[(7) - (7)]));
	}
    break;

  case 18:
#line 170 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ARGUMENT_EXPRESSION_LIST, (yyvsp[(1) - (1)]));
		(yyval)->argument_expression_list = (ARGUMENT_EXPRESSION_LIST_STRUCT) {
			NULL, (yyvsp[(1) - (1)])
		};
	}
    break;

  case 19:
#line 176 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ARGUMENT_EXPRESSION_LIST, (yyvsp[(1) - (3)]));
		(yyval)->argument_expression_list = (ARGUMENT_EXPRESSION_LIST_STRUCT) {
			(yyvsp[(1) - (3)]), (yyvsp[(3) - (3)])
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 20:
#line 187 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 21:
#line 188 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(INC_DEC_EXPRESSION, (yyvsp[(1) - (2)]));
		(yyval)->inc_dec_expression = (INC_DEC_EXPRESSION_STRUCT) { PREFIX_INC, (yyvsp[(2) - (2)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (2)]));
		free((yyvsp[(1) - (2)]));
	}
    break;

  case 22:
#line 194 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(INC_DEC_EXPRESSION, (yyvsp[(1) - (2)]));
		(yyval)->inc_dec_expression = (INC_DEC_EXPRESSION_STRUCT) { PREFIX_DEC, (yyvsp[(2) - (2)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (2)]));
		free((yyvsp[(1) - (2)]));
	}
    break;

  case 23:
#line 200 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(UNARY_EXPRESSION, (yyvsp[(1) - (2)]));
		(yyval)->unary_expression = (UNARY_EXPRESSION_STRUCT) {
			(yyvsp[(1) - (2)])->lexical_token, (yyvsp[(2) - (2)])
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (2)]));
		free((yyvsp[(1) - (2)]));
	}
    break;

  case 24:
#line 208 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(SIZEOF_EXPRESSION, (yyvsp[(1) - (2)]));
		(yyval)->sizeof_expression = (SIZEOF_EXPRESSION_STRUCT) { 0, (yyvsp[(2) - (2)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (2)]));
		free((yyvsp[(1) - (2)]));
	}
    break;

  case 25:
#line 214 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(SIZEOF_EXPRESSION, (yyvsp[(1) - (4)]));
		(yyval)->sizeof_expression = (SIZEOF_EXPRESSION_STRUCT) { 1, (yyvsp[(3) - (4)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (4)]));
		free((yyvsp[(1) - (4)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (4)]));
		free((yyvsp[(2) - (4)]));
		move_and_append_comments((yyvsp[(3) - (4)]), (yyvsp[(4) - (4)]));
		free((yyvsp[(4) - (4)]));
	}
    break;

  case 26:
#line 227 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 27:
#line 228 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 28:
#line 229 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 29:
#line 230 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 30:
#line 231 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 31:
#line 232 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 32:
#line 236 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 33:
#line 237 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(CAST_EXPRESSION, (yyvsp[(1) - (4)]));
		(yyval)->cast_expression = (CAST_EXPRESSION_STRUCT) { (yyvsp[(2) - (4)]), (yyvsp[(4) - (4)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (4)]));
		free((yyvsp[(1) - (4)]));
		move_and_append_comments((yyvsp[(2) - (4)]), (yyvsp[(3) - (4)]));
		free((yyvsp[(3) - (4)]));
	}
    break;

  case 34:
#line 248 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 35:
#line 249 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(BINARY_EXPRESSION, (yyvsp[(1) - (3)]));
		(yyval)->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			(yyvsp[(1) - (3)]), (yyvsp[(2) - (3)])->lexical_token, (yyvsp[(3) - (3)])
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 36:
#line 257 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(BINARY_EXPRESSION, (yyvsp[(1) - (3)]));
		(yyval)->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			(yyvsp[(1) - (3)]), (yyvsp[(2) - (3)])->lexical_token, (yyvsp[(3) - (3)]) 
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 37:
#line 265 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(BINARY_EXPRESSION, (yyvsp[(1) - (3)]));
		(yyval)->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			(yyvsp[(1) - (3)]), (yyvsp[(2) - (3)])->lexical_token, (yyvsp[(3) - (3)])
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 38:
#line 276 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 39:
#line 277 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(BINARY_EXPRESSION, (yyvsp[(1) - (3)]));
		(yyval)->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			(yyvsp[(1) - (3)]), (yyvsp[(2) - (3)])->lexical_token, (yyvsp[(3) - (3)])
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 40:
#line 285 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(BINARY_EXPRESSION, (yyvsp[(1) - (3)]));
		(yyval)->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			(yyvsp[(1) - (3)]), (yyvsp[(2) - (3)])->lexical_token, (yyvsp[(3) - (3)])
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 41:
#line 296 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 42:
#line 297 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(BINARY_EXPRESSION, (yyvsp[(1) - (3)]));
		(yyval)->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			(yyvsp[(1) - (3)]), (yyvsp[(2) - (3)])->lexical_token, (yyvsp[(3) - (3)])
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 43:
#line 305 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(BINARY_EXPRESSION, (yyvsp[(1) - (3)]));
		(yyval)->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			(yyvsp[(1) - (3)]), (yyvsp[(2) - (3)])->lexical_token, (yyvsp[(3) - (3)])
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 44:
#line 316 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 45:
#line 317 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(BINARY_EXPRESSION, (yyvsp[(1) - (3)]));
		(yyval)->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			(yyvsp[(1) - (3)]), (yyvsp[(2) - (3)])->lexical_token, (yyvsp[(3) - (3)])
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 46:
#line 325 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(BINARY_EXPRESSION, (yyvsp[(1) - (3)]));
		(yyval)->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			(yyvsp[(1) - (3)]), (yyvsp[(2) - (3)])->lexical_token, (yyvsp[(3) - (3)])
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 47:
#line 333 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(BINARY_EXPRESSION, (yyvsp[(1) - (3)]));
		(yyval)->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			(yyvsp[(1) - (3)]), (yyvsp[(2) - (3)])->lexical_token, (yyvsp[(3) - (3)])
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 48:
#line 341 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(BINARY_EXPRESSION, (yyvsp[(1) - (3)]));
		(yyval)->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			(yyvsp[(1) - (3)]), (yyvsp[(2) - (3)])->lexical_token, (yyvsp[(3) - (3)])
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 49:
#line 352 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 50:
#line 353 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(BINARY_EXPRESSION, (yyvsp[(1) - (3)]));
		(yyval)->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			(yyvsp[(1) - (3)]), (yyvsp[(2) - (3)])->lexical_token, (yyvsp[(3) - (3)])
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 51:
#line 361 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(BINARY_EXPRESSION, (yyvsp[(1) - (3)]));
		(yyval)->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			(yyvsp[(1) - (3)]), (yyvsp[(2) - (3)])->lexical_token, (yyvsp[(3) - (3)])
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 52:
#line 372 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 53:
#line 373 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(BINARY_EXPRESSION, (yyvsp[(1) - (3)]));
		(yyval)->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			(yyvsp[(1) - (3)]), (yyvsp[(2) - (3)])->lexical_token, (yyvsp[(3) - (3)])
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 54:
#line 384 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 55:
#line 385 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(BINARY_EXPRESSION, (yyvsp[(1) - (3)]));
		(yyval)->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			(yyvsp[(1) - (3)]), (yyvsp[(2) - (3)])->lexical_token, (yyvsp[(3) - (3)])
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 56:
#line 396 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 57:
#line 397 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(BINARY_EXPRESSION, (yyvsp[(1) - (3)]));
		(yyval)->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			(yyvsp[(1) - (3)]), (yyvsp[(2) - (3)])->lexical_token, (yyvsp[(3) - (3)])
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 58:
#line 408 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 59:
#line 409 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(BINARY_EXPRESSION, (yyvsp[(1) - (3)]));
		(yyval)->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			(yyvsp[(1) - (3)]), (yyvsp[(2) - (3)])->lexical_token, (yyvsp[(3) - (3)])
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 60:
#line 420 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 61:
#line 421 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(BINARY_EXPRESSION, (yyvsp[(1) - (3)]));
		(yyval)->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			(yyvsp[(1) - (3)]), (yyvsp[(2) - (3)])->lexical_token, (yyvsp[(3) - (3)])
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 62:
#line 432 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 63:
#line 433 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(CONDITIONAL_EXPRESSION, (yyvsp[(1) - (5)]));
		(yyval)->conditional_expression = (CONDITIONAL_EXPRESSION_STRUCT) {
			(yyvsp[(1) - (5)]), (yyvsp[(3) - (5)]), (yyvsp[(5) - (5)])
		};
		move_and_append_comments((yyvsp[(1) - (5)]), (yyvsp[(2) - (5)]));
		free((yyvsp[(2) - (5)]));
		move_and_append_comments((yyvsp[(3) - (5)]), (yyvsp[(4) - (5)]));
		free((yyvsp[(4) - (5)]));
	}
    break;

  case 64:
#line 446 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 65:
#line 447 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(BINARY_EXPRESSION, (yyvsp[(1) - (3)]));
		(yyval)->binary_expression = (BINARY_EXPRESSION_STRUCT) {
			(yyvsp[(1) - (3)]), (yyvsp[(2) - (3)])->lexical_token, (yyvsp[(3) - (3)])
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 66:
#line 458 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 67:
#line 459 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 68:
#line 460 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 69:
#line 461 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 70:
#line 462 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 71:
#line 463 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 72:
#line 464 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 73:
#line 465 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 74:
#line 466 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 75:
#line 467 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 76:
#line 468 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 77:
#line 472 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 78:
#line 473 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(COMMA_EXPRESSION, (yyvsp[(1) - (3)]));
		(yyval)->comma_expression = (COMMA_EXPRESSION_STRUCT) { (yyvsp[(1) - (3)]), (yyvsp[(3) - (3)]) };
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 79:
#line 482 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 80:
#line 486 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(DECLARATION, (yyvsp[(1) - (2)]));
		(yyval)->declaration = (DECLARATION_STRUCT) { (yyvsp[(1) - (2)]), NULL };
		move_and_append_comments((yyvsp[(1) - (2)]), (yyvsp[(2) - (2)]));
		free((yyvsp[(2) - (2)]));
	}
    break;

  case 81:
#line 492 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(DECLARATION, (yyvsp[(1) - (3)]));
		(yyval)->declaration = (DECLARATION_STRUCT) { (yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]) };
		check_type_name_declaration((yyval));
		move_and_append_comments((yyvsp[(2) - (3)]), (yyvsp[(3) - (3)]));
		free((yyvsp[(3) - (3)]));
	}
    break;

  case 82:
#line 502 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(DECLARATION_SPECIFIERS, (yyvsp[(1) - (1)]));
		(yyval)->declaration_specifiers = (DECLARATION_SPECIFIERS_STRUCT) {
			(yyvsp[(1) - (1)]), NULL
		};
	}
    break;

  case 83:
#line 508 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(DECLARATION_SPECIFIERS, (yyvsp[(1) - (2)]));
		(yyval)->declaration_specifiers = (DECLARATION_SPECIFIERS_STRUCT) { (yyvsp[(1) - (2)]), (yyvsp[(2) - (2)]) };
	}
    break;

  case 84:
#line 512 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(DECLARATION_SPECIFIERS, (yyvsp[(1) - (1)]));
		(yyval)->declaration_specifiers = (DECLARATION_SPECIFIERS_STRUCT) {
			(yyvsp[(1) - (1)]), NULL
		};
	}
    break;

  case 85:
#line 518 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(DECLARATION_SPECIFIERS, (yyvsp[(1) - (2)]));
		(yyval)->declaration_specifiers = (DECLARATION_SPECIFIERS_STRUCT) { (yyvsp[(1) - (2)]), (yyvsp[(2) - (2)]) };
	}
    break;

  case 86:
#line 522 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(DECLARATION_SPECIFIERS, (yyvsp[(1) - (1)]));
		(yyval)->declaration_specifiers = (DECLARATION_SPECIFIERS_STRUCT) {
			(yyvsp[(1) - (1)]), NULL
		};
	}
    break;

  case 87:
#line 528 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(DECLARATION_SPECIFIERS, (yyvsp[(1) - (2)]));
		(yyval)->declaration_specifiers = (DECLARATION_SPECIFIERS_STRUCT) { (yyvsp[(1) - (2)]), (yyvsp[(2) - (2)]) };
	}
    break;

  case 88:
#line 532 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(DECLARATION_SPECIFIERS, (yyvsp[(1) - (1)]));
		(yyval)->declaration_specifiers = (DECLARATION_SPECIFIERS_STRUCT) {
			(yyvsp[(1) - (1)]), NULL
		};
	}
    break;

  case 89:
#line 538 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(DECLARATION_SPECIFIERS, (yyvsp[(1) - (2)]));
		(yyval)->declaration_specifiers = (DECLARATION_SPECIFIERS_STRUCT) { (yyvsp[(1) - (2)]), (yyvsp[(2) - (2)]) };
	}
    break;

  case 90:
#line 545 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(INIT_DECLARATOR_LIST, (yyvsp[(1) - (1)]));
		(yyval)->init_declarator_list = (INIT_DECLARATOR_LIST_STRUCT) { NULL, (yyvsp[(1) - (1)]) };
	}
    break;

  case 91:
#line 549 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(INIT_DECLARATOR_LIST, (yyvsp[(1) - (3)]));
		(yyval)->init_declarator_list = (INIT_DECLARATOR_LIST_STRUCT) { (yyvsp[(1) - (3)]), (yyvsp[(3) - (3)]) };
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 92:
#line 558 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(INIT_DECLARATOR, (yyvsp[(1) - (1)]));
		(yyval)->init_declarator = (INIT_DECLARATOR_STRUCT) { (yyvsp[(1) - (1)]), NULL };
	}
    break;

  case 93:
#line 562 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(INIT_DECLARATOR, (yyvsp[(1) - (3)]));
		(yyval)->init_declarator = (INIT_DECLARATOR_STRUCT) { (yyvsp[(1) - (3)]), (yyvsp[(3) - (3)]) };
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 94:
#line 571 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(STORAGE_CLASS_SPECIFIER, (yyvsp[(1) - (1)]));
		(yyval)->storage_class_specifier = (STORAGE_CLASS_SPECIFIER_STRUCT) {
			(yyvsp[(1) - (1)])->lexical_token
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 95:
#line 579 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(STORAGE_CLASS_SPECIFIER, (yyvsp[(1) - (1)]));
		(yyval)->storage_class_specifier = (STORAGE_CLASS_SPECIFIER_STRUCT) {
			(yyvsp[(1) - (1)])->lexical_token
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 96:
#line 587 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(STORAGE_CLASS_SPECIFIER, (yyvsp[(1) - (1)]));
		(yyval)->storage_class_specifier = (STORAGE_CLASS_SPECIFIER_STRUCT) {
			(yyvsp[(1) - (1)])->lexical_token
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 97:
#line 595 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(STORAGE_CLASS_SPECIFIER, (yyvsp[(1) - (1)]));
		(yyval)->storage_class_specifier = (STORAGE_CLASS_SPECIFIER_STRUCT) {
			(yyvsp[(1) - (1)])->lexical_token
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 98:
#line 603 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(STORAGE_CLASS_SPECIFIER, (yyvsp[(1) - (1)]));
		(yyval)->storage_class_specifier = (STORAGE_CLASS_SPECIFIER_STRUCT) {
			(yyvsp[(1) - (1)])->lexical_token
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 99:
#line 614 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PRIMITIVE_TYPE_SPECIFIER, (yyvsp[(1) - (1)]));
		(yyval)->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			(yyvsp[(1) - (1)])->lexical_token
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 100:
#line 622 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PRIMITIVE_TYPE_SPECIFIER, (yyvsp[(1) - (1)]));
		(yyval)->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			(yyvsp[(1) - (1)])->lexical_token
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 101:
#line 630 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PRIMITIVE_TYPE_SPECIFIER, (yyvsp[(1) - (1)]));
		(yyval)->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			(yyvsp[(1) - (1)])->lexical_token
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 102:
#line 638 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PRIMITIVE_TYPE_SPECIFIER, (yyvsp[(1) - (1)]));
		(yyval)->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			(yyvsp[(1) - (1)])->lexical_token
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 103:
#line 646 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PRIMITIVE_TYPE_SPECIFIER, (yyvsp[(1) - (1)]));
		(yyval)->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			(yyvsp[(1) - (1)])->lexical_token
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 104:
#line 654 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PRIMITIVE_TYPE_SPECIFIER, (yyvsp[(1) - (1)]));
		(yyval)->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			(yyvsp[(1) - (1)])->lexical_token
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 105:
#line 662 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PRIMITIVE_TYPE_SPECIFIER, (yyvsp[(1) - (1)]));
		(yyval)->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			(yyvsp[(1) - (1)])->lexical_token
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 106:
#line 670 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PRIMITIVE_TYPE_SPECIFIER, (yyvsp[(1) - (1)]));
		(yyval)->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			(yyvsp[(1) - (1)])->lexical_token
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 107:
#line 678 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PRIMITIVE_TYPE_SPECIFIER, (yyvsp[(1) - (1)]));
		(yyval)->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			(yyvsp[(1) - (1)])->lexical_token
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 108:
#line 686 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PRIMITIVE_TYPE_SPECIFIER, (yyvsp[(1) - (1)]));
		(yyval)->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			(yyvsp[(1) - (1)])->lexical_token
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 109:
#line 694 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PRIMITIVE_TYPE_SPECIFIER, (yyvsp[(1) - (1)]));
		(yyval)->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			(yyvsp[(1) - (1)])->lexical_token
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 110:
#line 702 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PRIMITIVE_TYPE_SPECIFIER, (yyvsp[(1) - (1)]));
		(yyval)->primitive_type_specifier = (PRIMITIVE_TYPE_SPECIFIER_STRUCT) {
			(yyvsp[(1) - (1)])->lexical_token
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 111:
#line 710 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 112:
#line 711 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 113:
#line 712 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(NAMED_TYPE_SPECIFIER, (yyvsp[(1) - (1)]));
		(yyval)->named_type_specifier = (NAMED_TYPE_SPECIFIER_STRUCT) {
			(yyvsp[(1) - (1)])->identifier.id
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 114:
#line 723 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(STRUCT_OR_UNION, (yyvsp[(1) - (5)]));
		(yyval)->struct_or_union = (STRUCT_OR_UNION_STRUCT) {
			(yyvsp[(1) - (5)])->lexical_token == STRUCT, (yyvsp[(2) - (5)])->identifier.id, (yyvsp[(4) - (5)])
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (5)]));
		free((yyvsp[(1) - (5)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (5)]));
		free((yyvsp[(2) - (5)]));
		move_and_append_comments((yyval), (yyvsp[(3) - (5)]));
		free((yyvsp[(3) - (5)]));
		move_and_append_comments((yyvsp[(4) - (5)]), (yyvsp[(5) - (5)]));
		free((yyvsp[(5) - (5)]));
	}
    break;

  case 115:
#line 737 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(STRUCT_OR_UNION, (yyvsp[(1) - (4)]));
		(yyval)->struct_or_union = (STRUCT_OR_UNION_STRUCT) {
			(yyvsp[(1) - (4)])->lexical_token == STRUCT, NULL, (yyvsp[(3) - (4)])
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (4)]));
		free((yyvsp[(1) - (4)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (4)]));
		free((yyvsp[(2) - (4)]));
		move_and_append_comments((yyvsp[(3) - (4)]), (yyvsp[(4) - (4)]));
		free((yyvsp[(4) - (4)]));
	}
    break;

  case 116:
#line 749 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(STRUCT_OR_UNION, (yyvsp[(1) - (2)]));
		(yyval)->struct_or_union = (STRUCT_OR_UNION_STRUCT) {
			(yyvsp[(1) - (2)])->lexical_token == STRUCT, (yyvsp[(2) - (2)])->identifier.id, NULL
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (2)]));
		free((yyvsp[(1) - (2)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (2)]));
		free((yyvsp[(2) - (2)]));
	}
    break;

  case 117:
#line 759 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(STRUCT_OR_UNION, (yyvsp[(1) - (2)]));
		(yyval)->struct_or_union = (STRUCT_OR_UNION_STRUCT) {
			(yyvsp[(1) - (2)])->lexical_token == STRUCT, (yyvsp[(2) - (2)])->identifier.id, NULL
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (2)]));
		free((yyvsp[(1) - (2)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (2)]));
		free((yyvsp[(2) - (2)]));
	}
    break;

  case 118:
#line 772 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 119:
#line 773 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 120:
#line 777 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(STRUCT_DECLARATION_LIST, (yyvsp[(1) - (1)]));
		(yyval)->struct_declaration_list = (STRUCT_DECLARATION_LIST_STRUCT) {
			NULL, (yyvsp[(1) - (1)])
		};
	}
    break;

  case 121:
#line 783 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(STRUCT_DECLARATION_LIST, (yyvsp[(1) - (2)]));
		(yyval)->struct_declaration_list = (STRUCT_DECLARATION_LIST_STRUCT) {
			(yyvsp[(1) - (2)]), (yyvsp[(2) - (2)])
		};
	}
    break;

  case 122:
#line 792 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(STRUCT_DECLARATION, (yyvsp[(1) - (3)]));
		(yyval)->struct_declaration = (STRUCT_DECLARATION_STRUCT) { (yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]) };
		move_and_append_comments((yyvsp[(2) - (3)]), (yyvsp[(3) - (3)]));
		free((yyvsp[(3) - (3)]));
	}
    break;

  case 123:
#line 798 "../../../c/transformer/pct_c_parser.y"
    {	/* GCC extension */
		(yyval) = create_node(STRUCT_DECLARATION, (yyvsp[(1) - (2)]));
		(yyval)->struct_declaration = (STRUCT_DECLARATION_STRUCT) { (yyvsp[(1) - (2)]), NULL };
		move_and_append_comments((yyvsp[(1) - (2)]), (yyvsp[(2) - (2)]));
		free((yyvsp[(2) - (2)]));
	}
    break;

  case 124:
#line 807 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(SPECIFIER_QUALIFIER_LIST, (yyvsp[(1) - (2)]));
		(yyval)->specifier_qualifier_list = (SPECIFIER_QUALIFIER_LIST_STRUCT) {
			(yyvsp[(1) - (2)]), (yyvsp[(2) - (2)])
		};
	}
    break;

  case 125:
#line 813 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(SPECIFIER_QUALIFIER_LIST, (yyvsp[(1) - (1)]));
		(yyval)->specifier_qualifier_list = (SPECIFIER_QUALIFIER_LIST_STRUCT) {
			(yyvsp[(1) - (1)]), NULL
		};
	}
    break;

  case 126:
#line 819 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(SPECIFIER_QUALIFIER_LIST, (yyvsp[(1) - (2)]));
		(yyval)->specifier_qualifier_list = (SPECIFIER_QUALIFIER_LIST_STRUCT) {
			(yyvsp[(1) - (2)]), (yyvsp[(2) - (2)])
		};
	}
    break;

  case 127:
#line 825 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(SPECIFIER_QUALIFIER_LIST, (yyvsp[(1) - (1)]));
		(yyval)->specifier_qualifier_list = (SPECIFIER_QUALIFIER_LIST_STRUCT) {
			(yyvsp[(1) - (1)]), NULL
		};
	}
    break;

  case 128:
#line 834 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(STRUCT_DECLARATOR_LIST, (yyvsp[(1) - (1)]));
		(yyval)->struct_declarator_list = (STRUCT_DECLARATOR_LIST_STRUCT) {
			(yyvsp[(1) - (1)]), NULL
		};
	}
    break;

  case 129:
#line 840 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(STRUCT_DECLARATOR_LIST, (yyvsp[(1) - (3)]));
		(yyval)->struct_declarator_list = (STRUCT_DECLARATOR_LIST_STRUCT) {
			(yyvsp[(1) - (3)]), (yyvsp[(3) - (3)])
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 130:
#line 851 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(STRUCT_DECLARATOR, (yyvsp[(1) - (1)]));
		(yyval)->struct_declarator = (STRUCT_DECLARATOR_STRUCT) { (yyvsp[(1) - (1)]), NULL };
	}
    break;

  case 131:
#line 855 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(STRUCT_DECLARATOR, (yyvsp[(1) - (2)]));
		(yyval)->struct_declarator = (STRUCT_DECLARATOR_STRUCT) { NULL, (yyvsp[(2) - (2)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (2)]));
		free((yyvsp[(1) - (2)]));
	}
    break;

  case 132:
#line 861 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(STRUCT_DECLARATOR, (yyvsp[(1) - (3)]));
		(yyval)->struct_declarator = (STRUCT_DECLARATOR_STRUCT) { (yyvsp[(1) - (3)]), (yyvsp[(3) - (3)]) };
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 133:
#line 870 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ENUM_SPECIFIER, (yyvsp[(1) - (4)]));
		(yyval)->enum_specifier = (ENUM_SPECIFIER_STRUCT) { NULL, (yyvsp[(3) - (4)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (4)]));
		free((yyvsp[(1) - (4)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (4)]));
		free((yyvsp[(2) - (4)]));
		move_and_append_comments((yyvsp[(3) - (4)]), (yyvsp[(4) - (4)]));
		free((yyvsp[(4) - (4)]));
	}
    break;

  case 134:
#line 880 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ENUM_SPECIFIER, (yyvsp[(1) - (5)]));
		(yyval)->enum_specifier = (ENUM_SPECIFIER_STRUCT) { (yyvsp[(2) - (5)])->identifier.id, (yyvsp[(4) - (5)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (5)]));
		free((yyvsp[(1) - (5)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (5)]));
		free((yyvsp[(2) - (5)]));
		move_and_append_comments((yyval), (yyvsp[(3) - (5)]));
		free((yyvsp[(3) - (5)]));
		move_and_append_comments((yyvsp[(4) - (5)]), (yyvsp[(5) - (5)]));
		free((yyvsp[(5) - (5)]));
	}
    break;

  case 135:
#line 892 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ENUM_SPECIFIER, (yyvsp[(1) - (5)]));
		(yyval)->enum_specifier = (ENUM_SPECIFIER_STRUCT) { NULL, (yyvsp[(3) - (5)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (5)]));
		free((yyvsp[(1) - (5)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (5)]));
		free((yyvsp[(2) - (5)]));
		move_and_append_comments((yyvsp[(3) - (5)]), (yyvsp[(4) - (5)]));
		free((yyvsp[(4) - (5)]));
		move_and_append_comments((yyvsp[(3) - (5)]), (yyvsp[(5) - (5)]));
		free((yyvsp[(5) - (5)]));
	}
    break;

  case 136:
#line 904 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ENUM_SPECIFIER, (yyvsp[(1) - (6)]));
		(yyval)->enum_specifier = (ENUM_SPECIFIER_STRUCT) { (yyvsp[(2) - (6)])->identifier.id, (yyvsp[(4) - (6)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (6)]));
		free((yyvsp[(1) - (6)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (6)]));
		free((yyvsp[(2) - (6)]));
		move_and_append_comments((yyval), (yyvsp[(3) - (6)]));
		free((yyvsp[(3) - (6)]));
		move_and_append_comments((yyvsp[(4) - (6)]), (yyvsp[(5) - (6)]));
		free((yyvsp[(5) - (6)]));
		move_and_append_comments((yyvsp[(4) - (6)]), (yyvsp[(6) - (6)]));
		free((yyvsp[(6) - (6)]));
	}
    break;

  case 137:
#line 918 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ENUM_SPECIFIER, (yyvsp[(1) - (2)]));
		(yyval)->enum_specifier = (ENUM_SPECIFIER_STRUCT) {
			(yyvsp[(2) - (2)])->identifier.id, NULL
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (2)]));
		free((yyvsp[(1) - (2)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (2)]));
		free((yyvsp[(2) - (2)]));
	}
    break;

  case 138:
#line 928 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ENUM_SPECIFIER, (yyvsp[(1) - (2)]));
		(yyval)->enum_specifier = (ENUM_SPECIFIER_STRUCT) {
			(yyvsp[(2) - (2)])->identifier.id, NULL
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (2)]));
		free((yyvsp[(1) - (2)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (2)]));
		free((yyvsp[(2) - (2)]));
	}
    break;

  case 139:
#line 941 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ENUMERATOR_LIST, (yyvsp[(1) - (1)]));
		(yyval)->enumerator_list = (ENUMERATOR_LIST_STRUCT) { NULL, (yyvsp[(1) - (1)]) };
	}
    break;

  case 140:
#line 945 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ENUMERATOR_LIST, (yyvsp[(1) - (3)]));
		(yyval)->enumerator_list = (ENUMERATOR_LIST_STRUCT) { (yyvsp[(1) - (3)]), (yyvsp[(3) - (3)]) };
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 141:
#line 954 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ENUMERATOR, (yyvsp[(1) - (1)]));
		(yyval)->enumerator = (ENUMERATOR_STRUCT) { (yyvsp[(1) - (1)])->identifier.id, NULL };
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 142:
#line 960 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ENUMERATOR, (yyvsp[(1) - (3)]));
		(yyval)->enumerator = (ENUMERATOR_STRUCT) { (yyvsp[(1) - (3)])->identifier.id, (yyvsp[(3) - (3)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (3)]));
		free((yyvsp[(1) - (3)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 143:
#line 971 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(TYPE_QUALIFIER, (yyvsp[(1) - (1)]));
		(yyval)->type_qualifier = (TYPE_QUALIFIER_STRUCT) { (yyvsp[(1) - (1)])->lexical_token };
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 144:
#line 977 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(TYPE_QUALIFIER, (yyvsp[(1) - (1)]));
		(yyval)->type_qualifier = (TYPE_QUALIFIER_STRUCT) { (yyvsp[(1) - (1)])->lexical_token };
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 145:
#line 983 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(TYPE_QUALIFIER, (yyvsp[(1) - (1)]));
		(yyval)->type_qualifier = (TYPE_QUALIFIER_STRUCT) { (yyvsp[(1) - (1)])->lexical_token };
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 146:
#line 992 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(FUNCTION_SPECIFIER, (yyvsp[(1) - (1)]));
		(yyval)->function_specifier = (FUNCTION_SPECIFIER_STRUCT) {
			(yyvsp[(1) - (1)])->lexical_token
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 147:
#line 1003 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(DECLARATOR, (yyvsp[(1) - (2)]));
		(yyval)->declarator = (DECLARATOR_STRUCT) { (yyvsp[(1) - (2)]), (yyvsp[(2) - (2)]) };
	}
    break;

  case 148:
#line 1007 "../../../c/transformer/pct_c_parser.y"
    {
		if ((yyvsp[(1) - (1)])->type == DECLARATOR)
			(yyval) = (yyvsp[(1) - (1)]);
		else {
			(yyval) = create_node(DECLARATOR, (yyvsp[(1) - (1)]));
			(yyval)->declarator = (DECLARATOR_STRUCT) { NULL, (yyvsp[(1) - (1)]) };
		}
	}
    break;

  case 149:
#line 1018 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(NAMED_DIRECT_DECLARATOR, (yyvsp[(1) - (1)]));
		(yyval)->named_direct_declarator = (NAMED_DIRECT_DECLARATOR_STRUCT) {
			(yyvsp[(1) - (1)])->identifier.id
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 150:
#line 1026 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = (yyvsp[(2) - (3)]);
		move_and_prepend_comments((yyval), (yyvsp[(1) - (3)]));
		free((yyvsp[(1) - (3)]));
		move_and_append_comments((yyvsp[(2) - (3)]), (yyvsp[(3) - (3)]));
		free((yyvsp[(3) - (3)]));
	}
    break;

  case 151:
#line 1033 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ARRAY_DIRECT_DECLARATOR, (yyvsp[(1) - (5)]));
		(yyval)->array_direct_declarator = (ARRAY_DIRECT_DECLARATOR_STRUCT) {
			(yyvsp[(1) - (5)]), 0, (yyvsp[(3) - (5)]), 0, (yyvsp[(4) - (5)]), 0
		};
		move_and_append_comments((yyvsp[(1) - (5)]), (yyvsp[(2) - (5)]));
		free((yyvsp[(2) - (5)]));
		move_and_append_comments((yyvsp[(4) - (5)]), (yyvsp[(5) - (5)]));
		free((yyvsp[(5) - (5)]));
	}
    break;

  case 152:
#line 1043 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ARRAY_DIRECT_DECLARATOR, (yyvsp[(1) - (4)]));
		(yyval)->array_direct_declarator = (ARRAY_DIRECT_DECLARATOR_STRUCT) {
			(yyvsp[(1) - (4)]), 0, (yyvsp[(3) - (4)]), 0, NULL, 0
		};
		move_and_append_comments((yyvsp[(1) - (4)]), (yyvsp[(2) - (4)]));
		free((yyvsp[(2) - (4)]));
		move_and_append_comments((yyvsp[(3) - (4)]), (yyvsp[(4) - (4)]));
		free((yyvsp[(4) - (4)]));
	}
    break;

  case 153:
#line 1053 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ARRAY_DIRECT_DECLARATOR, (yyvsp[(1) - (4)]));
		(yyval)->array_direct_declarator = (ARRAY_DIRECT_DECLARATOR_STRUCT) {
			(yyvsp[(1) - (4)]), 0, NULL, 0, (yyvsp[(3) - (4)]), 0
		};
		move_and_append_comments((yyvsp[(1) - (4)]), (yyvsp[(2) - (4)]));
		free((yyvsp[(2) - (4)]));
		move_and_append_comments((yyvsp[(3) - (4)]), (yyvsp[(4) - (4)]));
		free((yyvsp[(4) - (4)]));
	}
    break;

  case 154:
#line 1064 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ARRAY_DIRECT_DECLARATOR, (yyvsp[(1) - (6)]));
		(yyval)->array_direct_declarator = (ARRAY_DIRECT_DECLARATOR_STRUCT) {
			(yyvsp[(1) - (6)]), 1, (yyvsp[(4) - (6)]), 0, (yyvsp[(5) - (6)]), 0
		};
		move_and_append_comments((yyvsp[(1) - (6)]), (yyvsp[(2) - (6)]));
		free((yyvsp[(2) - (6)]));
		move_and_append_comments((yyvsp[(1) - (6)]), (yyvsp[(3) - (6)]));
		free((yyvsp[(3) - (6)]));
		move_and_append_comments((yyvsp[(5) - (6)]), (yyvsp[(6) - (6)]));
		free((yyvsp[(6) - (6)]));
	}
    break;

  case 155:
#line 1077 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ARRAY_DIRECT_DECLARATOR, (yyvsp[(1) - (6)]));
		(yyval)->array_direct_declarator = (ARRAY_DIRECT_DECLARATOR_STRUCT) {
			(yyvsp[(1) - (6)]), 0, (yyvsp[(3) - (6)]), 1, (yyvsp[(5) - (6)]), 0
		};
		move_and_append_comments((yyvsp[(1) - (6)]), (yyvsp[(2) - (6)]));
		free((yyvsp[(2) - (6)]));
		move_and_append_comments((yyvsp[(3) - (6)]), (yyvsp[(4) - (6)]));
		free((yyvsp[(4) - (6)]));
		move_and_append_comments((yyvsp[(5) - (6)]), (yyvsp[(6) - (6)]));
		free((yyvsp[(6) - (6)]));
	}
    break;

  case 156:
#line 1089 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ARRAY_DIRECT_DECLARATOR, (yyvsp[(1) - (5)]));
		(yyval)->array_direct_declarator = (ARRAY_DIRECT_DECLARATOR_STRUCT) {
			(yyvsp[(1) - (5)]), 0, (yyvsp[(3) - (5)]), 0, NULL, 1
		};
		move_and_append_comments((yyvsp[(1) - (5)]), (yyvsp[(2) - (5)]));
		free((yyvsp[(2) - (5)]));
		move_and_append_comments((yyvsp[(3) - (5)]), (yyvsp[(4) - (5)]));
		free((yyvsp[(4) - (5)]));
		move_and_append_comments((yyvsp[(3) - (5)]), (yyvsp[(5) - (5)]));
		free((yyvsp[(5) - (5)]));
	}
    break;

  case 157:
#line 1101 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ARRAY_DIRECT_DECLARATOR, (yyvsp[(1) - (4)]));
		(yyval)->array_direct_declarator = (ARRAY_DIRECT_DECLARATOR_STRUCT) {
			(yyvsp[(1) - (4)]), 0, NULL, 0, NULL, 1
		};
		move_and_append_comments((yyvsp[(1) - (4)]), (yyvsp[(2) - (4)]));
		free((yyvsp[(2) - (4)]));
		move_and_append_comments((yyvsp[(1) - (4)]), (yyvsp[(3) - (4)]));
		free((yyvsp[(3) - (4)]));
		move_and_append_comments((yyvsp[(1) - (4)]), (yyvsp[(4) - (4)]));
		free((yyvsp[(4) - (4)]));
	}
    break;

  case 158:
#line 1113 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ARRAY_DIRECT_DECLARATOR, (yyvsp[(1) - (3)]));
		(yyval)->array_direct_declarator = (ARRAY_DIRECT_DECLARATOR_STRUCT) {
			(yyvsp[(1) - (3)]), 0, NULL, 0, NULL, 0
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(3) - (3)]));
		free((yyvsp[(3) - (3)]));
	}
    break;

  case 159:
#line 1123 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PARAMETERIZED_DIRECT_DECLARATOR, (yyvsp[(1) - (4)]));
		(yyval)->parameterized_direct_declarator =
			(PARAMETERIZED_DIRECT_DECLARATOR_STRUCT) { (yyvsp[(1) - (4)]), (yyvsp[(3) - (4)]), NULL };
		move_and_append_comments((yyvsp[(1) - (4)]), (yyvsp[(2) - (4)]));
		free((yyvsp[(2) - (4)]));
		move_and_append_comments((yyvsp[(3) - (4)]), (yyvsp[(4) - (4)]));
		free((yyvsp[(4) - (4)]));
	}
    break;

  case 160:
#line 1132 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PARAMETERIZED_DIRECT_DECLARATOR, (yyvsp[(1) - (4)]));
		(yyval)->parameterized_direct_declarator =
			(PARAMETERIZED_DIRECT_DECLARATOR_STRUCT) { (yyvsp[(1) - (4)]), NULL, (yyvsp[(3) - (4)]) };
		move_and_append_comments((yyvsp[(1) - (4)]), (yyvsp[(2) - (4)]));
		free((yyvsp[(2) - (4)]));
		move_and_append_comments((yyvsp[(3) - (4)]), (yyvsp[(4) - (4)]));
		free((yyvsp[(4) - (4)]));
	}
    break;

  case 161:
#line 1141 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PARAMETERIZED_DIRECT_DECLARATOR, (yyvsp[(1) - (3)]));
		(yyval)->parameterized_direct_declarator =
			(PARAMETERIZED_DIRECT_DECLARATOR_STRUCT) { (yyvsp[(1) - (3)]), NULL, NULL };
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(3) - (3)]));
		free((yyvsp[(3) - (3)]));
	}
    break;

  case 162:
#line 1153 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(POINTER, (yyvsp[(1) - (1)]));
		(yyval)->pointer = (POINTER_STRUCT) { NULL, NULL };
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 163:
#line 1159 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(POINTER, (yyvsp[(1) - (2)]));
		(yyval)->pointer = (POINTER_STRUCT) { (yyvsp[(2) - (2)]), NULL };
		move_and_append_comments((yyval), (yyvsp[(1) - (2)]));
		free((yyvsp[(1) - (2)]));
	}
    break;

  case 164:
#line 1165 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(POINTER, (yyvsp[(1) - (2)]));
		(yyval)->pointer = (POINTER_STRUCT) { NULL, (yyvsp[(2) - (2)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (2)]));
		free((yyvsp[(1) - (2)]));
	}
    break;

  case 165:
#line 1171 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(POINTER, (yyvsp[(1) - (3)]));
		(yyval)->pointer = (POINTER_STRUCT) { (yyvsp[(2) - (3)]), (yyvsp[(3) - (3)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (3)]));
		free((yyvsp[(1) - (3)]));
	}
    break;

  case 166:
#line 1180 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(TYPE_QUALIFIER_LIST, (yyvsp[(1) - (1)]));
		(yyval)->type_qualifier_list = (TYPE_QUALIFIER_LIST_STRUCT) { NULL, (yyvsp[(1) - (1)]) };
	}
    break;

  case 167:
#line 1184 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(TYPE_QUALIFIER_LIST, (yyvsp[(1) - (2)]));
		(yyval)->type_qualifier_list = (TYPE_QUALIFIER_LIST_STRUCT) { (yyvsp[(1) - (2)]), (yyvsp[(2) - (2)]) };
	}
    break;

  case 168:
#line 1191 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 169:
#line 1192 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = (yyvsp[(1) - (3)]);
		(yyval)->parameter_list.has_ellipsis = 1;
	}
    break;

  case 170:
#line 1199 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PARAMETER_LIST, (yyvsp[(1) - (1)]));
		(yyval)->parameter_list = (PARAMETER_LIST_STRUCT) { NULL, (yyvsp[(1) - (1)]), 0 };
	}
    break;

  case 171:
#line 1203 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PARAMETER_LIST, (yyvsp[(1) - (3)]));
		(yyval)->parameter_list = (PARAMETER_LIST_STRUCT) { (yyvsp[(1) - (3)]), (yyvsp[(3) - (3)]), 0 };
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 172:
#line 1211 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PARAMETER_DECLARATION, (yyvsp[(1) - (2)]));
		(yyval)->parameter_declaration = (PARAMETER_DECLARATION_STRUCT) {
			(yyvsp[(1) - (2)]), (yyvsp[(2) - (2)]), NULL
		};
	}
    break;

  case 173:
#line 1217 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PARAMETER_DECLARATION, (yyvsp[(1) - (2)]));
		(yyval)->parameter_declaration = (PARAMETER_DECLARATION_STRUCT) {
			(yyvsp[(1) - (2)]), NULL, (yyvsp[(2) - (2)])
		};
	}
    break;

  case 174:
#line 1223 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PARAMETER_DECLARATION, (yyvsp[(1) - (1)]));
		(yyval)->parameter_declaration = (PARAMETER_DECLARATION_STRUCT) {
			(yyvsp[(1) - (1)]), NULL, NULL
		};
	}
    break;

  case 175:
#line 1232 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(IDENTIFIER_LIST, (yyvsp[(1) - (1)]));
		(yyval)->identifier_list = (IDENTIFIER_LIST_STRUCT) {
			NULL, (yyvsp[(1) - (1)])->identifier.id
		};
	}
    break;

  case 176:
#line 1238 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(IDENTIFIER_LIST, (yyvsp[(1) - (3)]));
		(yyval)->identifier_list = (IDENTIFIER_LIST_STRUCT) {
			(yyvsp[(1) - (3)]), (yyvsp[(3) - (3)])->identifier.id
		};
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 177:
#line 1249 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(TYPE_NAME, (yyvsp[(1) - (1)]));
		(yyval)->type_name = (TYPE_NAME_STRUCT) { (yyvsp[(1) - (1)]), NULL };
	}
    break;

  case 178:
#line 1253 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(TYPE_NAME, (yyvsp[(1) - (2)]));
		(yyval)->type_name = (TYPE_NAME_STRUCT) { (yyvsp[(1) - (2)]), (yyvsp[(2) - (2)]) };
	}
    break;

  case 179:
#line 1260 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ABSTRACT_DECLARATOR, (yyvsp[(1) - (1)]));
		(yyval)->abstract_declarator = (ABSTRACT_DECLARATOR_STRUCT) { (yyvsp[(1) - (1)]), NULL };
	}
    break;

  case 180:
#line 1264 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ABSTRACT_DECLARATOR, (yyvsp[(1) - (1)]));
		(yyval)->abstract_declarator = (ABSTRACT_DECLARATOR_STRUCT) { NULL, (yyvsp[(1) - (1)]) };
	}
    break;

  case 181:
#line 1268 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ABSTRACT_DECLARATOR, (yyvsp[(1) - (2)]));
		(yyval)->abstract_declarator = (ABSTRACT_DECLARATOR_STRUCT) { (yyvsp[(1) - (2)]), (yyvsp[(2) - (2)]) };
	}
    break;

  case 182:
#line 1275 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = (yyvsp[(2) - (3)]);
		move_and_prepend_comments((yyval), (yyvsp[(1) - (3)]));
		free((yyvsp[(1) - (3)]));
		move_and_append_comments((yyvsp[(2) - (3)]), (yyvsp[(3) - (3)]));
		free((yyvsp[(3) - (3)]));
	}
    break;

  case 183:
#line 1282 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ARRAY_DIRECT_ABSTRACT_DECLARATOR, (yyvsp[(1) - (2)]));
		(yyval)->array_direct_abstract_declarator =
			(ARRAY_DIRECT_ABSTRACT_DECLARATOR_STRUCT) { NULL, NULL, 0 };
		move_and_append_comments((yyval), (yyvsp[(1) - (2)]));
		free((yyvsp[(1) - (2)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (2)]));
		free((yyvsp[(2) - (2)]));
	}
    break;

  case 184:
#line 1291 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ARRAY_DIRECT_ABSTRACT_DECLARATOR, (yyvsp[(1) - (3)]));
		(yyval)->array_direct_abstract_declarator =
			(ARRAY_DIRECT_ABSTRACT_DECLARATOR_STRUCT) { NULL, (yyvsp[(2) - (3)]), 0 };
		move_and_append_comments((yyval), (yyvsp[(1) - (3)]));
		free((yyvsp[(1) - (3)]));
		move_and_append_comments((yyvsp[(2) - (3)]), (yyvsp[(3) - (3)]));
		free((yyvsp[(3) - (3)]));
	}
    break;

  case 185:
#line 1300 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ARRAY_DIRECT_ABSTRACT_DECLARATOR, (yyvsp[(1) - (3)]));
		(yyval)->array_direct_abstract_declarator =
			(ARRAY_DIRECT_ABSTRACT_DECLARATOR_STRUCT) { (yyvsp[(1) - (3)]), NULL, 0 };
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(3) - (3)]));
		free((yyvsp[(3) - (3)]));
	}
    break;

  case 186:
#line 1309 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ARRAY_DIRECT_ABSTRACT_DECLARATOR, (yyvsp[(1) - (4)]));
		(yyval)->array_direct_abstract_declarator =
			(ARRAY_DIRECT_ABSTRACT_DECLARATOR_STRUCT) { (yyvsp[(1) - (4)]), (yyvsp[(3) - (4)]), 0 };
		move_and_append_comments((yyvsp[(1) - (4)]), (yyvsp[(2) - (4)]));
		free((yyvsp[(2) - (4)]));
		move_and_append_comments((yyvsp[(3) - (4)]), (yyvsp[(4) - (4)]));
		free((yyvsp[(4) - (4)]));
	}
    break;

  case 187:
#line 1318 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ARRAY_DIRECT_ABSTRACT_DECLARATOR, (yyvsp[(1) - (3)]));
		(yyval)->array_direct_abstract_declarator =
			(ARRAY_DIRECT_ABSTRACT_DECLARATOR_STRUCT) { NULL, NULL, 1 };
		move_and_append_comments((yyval), (yyvsp[(1) - (3)]));
		free((yyvsp[(1) - (3)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
		move_and_append_comments((yyval), (yyvsp[(3) - (3)]));
		free((yyvsp[(3) - (3)]));
	}
    break;

  case 188:
#line 1329 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ARRAY_DIRECT_ABSTRACT_DECLARATOR, (yyvsp[(1) - (4)]));
		(yyval)->array_direct_abstract_declarator =
			(ARRAY_DIRECT_ABSTRACT_DECLARATOR_STRUCT) { (yyvsp[(1) - (4)]), NULL, 1 };
		move_and_append_comments((yyvsp[(1) - (4)]), (yyvsp[(2) - (4)]));
		free((yyvsp[(2) - (4)]));
		move_and_append_comments((yyvsp[(1) - (4)]), (yyvsp[(3) - (4)]));
		free((yyvsp[(3) - (4)]));
		move_and_append_comments((yyvsp[(1) - (4)]), (yyvsp[(4) - (4)]));
		free((yyvsp[(4) - (4)]));
	}
    break;

  case 189:
#line 1340 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR, (yyvsp[(1) - (2)]));
		(yyval)->parameterized_direct_abstract_declarator =
			(PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR_STRUCT) { NULL, NULL };
		move_and_append_comments((yyval), (yyvsp[(1) - (2)]));
		free((yyvsp[(1) - (2)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (2)]));
		free((yyvsp[(2) - (2)]));
	}
    break;

  case 190:
#line 1349 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR, (yyvsp[(1) - (3)]));
		(yyval)->parameterized_direct_abstract_declarator =
			(PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR_STRUCT) { NULL, (yyvsp[(2) - (3)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (3)]));
		free((yyvsp[(1) - (3)]));
		move_and_append_comments((yyvsp[(2) - (3)]), (yyvsp[(3) - (3)]));
		free((yyvsp[(3) - (3)]));
	}
    break;

  case 191:
#line 1358 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR, (yyvsp[(1) - (3)]));
		(yyval)->parameterized_direct_abstract_declarator =
			(PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR_STRUCT) { (yyvsp[(1) - (3)]), NULL };
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(3) - (3)]));
		free((yyvsp[(3) - (3)]));
	}
    break;

  case 192:
#line 1367 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR, (yyvsp[(1) - (4)]));
		(yyval)->parameterized_direct_abstract_declarator =
			(PARAMETERIZED_DIRECT_ABSTRACT_DECLARATOR_STRUCT) { (yyvsp[(1) - (4)]), (yyvsp[(3) - (4)]) };
		move_and_append_comments((yyvsp[(1) - (4)]), (yyvsp[(2) - (4)]));
		free((yyvsp[(2) - (4)]));
		move_and_append_comments((yyvsp[(3) - (4)]), (yyvsp[(4) - (4)]));
		free((yyvsp[(4) - (4)]));
	}
    break;

  case 193:
#line 1379 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 194:
#line 1380 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = (yyvsp[(2) - (3)]);
		move_and_prepend_comments((yyval), (yyvsp[(1) - (3)]));
		free((yyvsp[(1) - (3)]));
		move_and_append_comments((yyvsp[(2) - (3)]), (yyvsp[(3) - (3)]));
		free((yyvsp[(3) - (3)]));
	}
    break;

  case 195:
#line 1387 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = (yyvsp[(2) - (4)]);
		move_and_prepend_comments((yyval), (yyvsp[(1) - (4)]));
		free((yyvsp[(1) - (4)]));
		move_and_append_comments((yyvsp[(2) - (4)]), (yyvsp[(3) - (4)]));
		free((yyvsp[(3) - (4)]));
		move_and_append_comments((yyvsp[(2) - (4)]), (yyvsp[(4) - (4)]));
		free((yyvsp[(4) - (4)]));
	}
    break;

  case 196:
#line 1399 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(INITIALIZER_LIST, (yyvsp[(1) - (1)]));
		(yyval)->initializer_list = (INITIALIZER_LIST_STRUCT) { NULL, NULL, (yyvsp[(1) - (1)]) };
	}
    break;

  case 197:
#line 1403 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(INITIALIZER_LIST, (yyvsp[(1) - (2)]));
		(yyval)->initializer_list = (INITIALIZER_LIST_STRUCT) { NULL, (yyvsp[(1) - (2)]), (yyvsp[(2) - (2)]) };
	}
    break;

  case 198:
#line 1407 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(INITIALIZER_LIST, (yyvsp[(1) - (3)]));
		(yyval)->initializer_list = (INITIALIZER_LIST_STRUCT) { (yyvsp[(1) - (3)]), NULL, (yyvsp[(3) - (3)]) };
		move_and_append_comments((yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 199:
#line 1413 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(INITIALIZER_LIST, (yyvsp[(1) - (4)]));
		(yyval)->initializer_list = (INITIALIZER_LIST_STRUCT) { (yyvsp[(1) - (4)]), (yyvsp[(3) - (4)]), (yyvsp[(4) - (4)]) };
		move_and_append_comments((yyvsp[(1) - (4)]), (yyvsp[(2) - (4)]));
		free((yyvsp[(2) - (4)]));
	}
    break;

  case 200:
#line 1422 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = (yyvsp[(1) - (2)]);
		move_and_append_comments((yyvsp[(1) - (2)]), (yyvsp[(2) - (2)]));
		free((yyvsp[(2) - (2)]));
	}
    break;

  case 201:
#line 1430 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(DESIGNATOR_LIST, (yyvsp[(1) - (1)]));
		(yyval)->designator_list = (DESIGNATOR_LIST_STRUCT) { NULL, (yyvsp[(1) - (1)]) };
	}
    break;

  case 202:
#line 1434 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(DESIGNATOR_LIST, (yyvsp[(1) - (2)]));
		(yyval)->designator_list = (DESIGNATOR_LIST_STRUCT) { (yyvsp[(1) - (2)]), (yyvsp[(2) - (2)]) };
	}
    break;

  case 203:
#line 1441 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(ARRAY_DESIGNATOR, (yyvsp[(1) - (3)]));
		(yyval)->array_designator = (ARRAY_DESIGNATOR_STRUCT) { (yyvsp[(2) - (3)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (3)]));
		free((yyvsp[(1) - (3)]));
		move_and_append_comments((yyvsp[(2) - (3)]), (yyvsp[(3) - (3)]));
		free((yyvsp[(3) - (3)]));
	}
    break;

  case 204:
#line 1449 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(DOT_DESIGNATOR, (yyvsp[(1) - (2)]));
		(yyval)->dot_designator = (DOT_DESIGNATOR_STRUCT) { (yyvsp[(2) - (2)])->identifier.id };
		move_and_append_comments((yyval), (yyvsp[(1) - (2)]));
		free((yyvsp[(1) - (2)]));
	}
    break;

  case 205:
#line 1458 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 206:
#line 1459 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 207:
#line 1460 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 208:
#line 1461 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 209:
#line 1462 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 210:
#line 1463 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 211:
#line 1467 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(IDENTIFIER_LABELED_STATEMENT, (yyvsp[(1) - (3)]));
		(yyval)->identifier_labeled_statement =
			(IDENTIFIER_LABELED_STATEMENT_STRUCT) { (yyvsp[(1) - (3)])->identifier.id, (yyvsp[(3) - (3)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (3)]));
		free((yyvsp[(1) - (3)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 212:
#line 1476 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(CASE_LABELED_STATEMENT, (yyvsp[(1) - (4)]));
		(yyval)->case_labeled_statement = (CASE_LABELED_STATEMENT_STRUCT) {
			0, (yyvsp[(2) - (4)]), (yyvsp[(4) - (4)])
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (4)]));
		free((yyvsp[(1) - (4)]));
		move_and_append_comments((yyvsp[(2) - (4)]), (yyvsp[(3) - (4)]));
		free((yyvsp[(3) - (4)]));
	}
    break;

  case 213:
#line 1486 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(CASE_LABELED_STATEMENT, (yyvsp[(1) - (3)]));
		(yyval)->case_labeled_statement = (CASE_LABELED_STATEMENT_STRUCT) {
			1, NULL, (yyvsp[(3) - (3)])
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (3)]));
		free((yyvsp[(1) - (3)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
	}
    break;

  case 214:
#line 1499 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(COMPOUND_STATEMENT, (yyvsp[(1) - (2)]));
		(yyval)->compound_statement = (COMPOUND_STATEMENT_STRUCT) { NULL };
		move_and_append_comments((yyval), (yyvsp[(1) - (2)]));
		free((yyvsp[(1) - (2)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (2)]));
		free((yyvsp[(2) - (2)]));
	}
    break;

  case 215:
#line 1507 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(COMPOUND_STATEMENT, (yyvsp[(1) - (3)]));
		(yyval)->compound_statement = (COMPOUND_STATEMENT_STRUCT) { (yyvsp[(2) - (3)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (3)]));
		free((yyvsp[(1) - (3)]));
		move_and_append_comments((yyvsp[(2) - (3)]), (yyvsp[(3) - (3)]));
		free((yyvsp[(3) - (3)]));
	}
    break;

  case 216:
#line 1518 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(BLOCK_ITEM_LIST, (yyvsp[(1) - (1)]));
		(yyval)->block_item_list = (BLOCK_ITEM_LIST_STRUCT) { NULL, (yyvsp[(1) - (1)]) };
	}
    break;

  case 217:
#line 1522 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(BLOCK_ITEM_LIST, (yyvsp[(1) - (2)]));
		(yyval)->block_item_list = (BLOCK_ITEM_LIST_STRUCT) { (yyvsp[(1) - (2)]), (yyvsp[(2) - (2)]) };
	}
    break;

  case 218:
#line 1529 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 219:
#line 1530 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 220:
#line 1534 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(EXPRESSION_STATEMENT, (yyvsp[(1) - (1)]));
		(yyval)->expression_statement = (EXPRESSION_STATEMENT_STRUCT) { NULL };
		move_and_append_comments((yyval), (yyvsp[(1) - (1)]));
		free((yyvsp[(1) - (1)]));
	}
    break;

  case 221:
#line 1540 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(EXPRESSION_STATEMENT, (yyvsp[(1) - (2)]));
		(yyval)->expression_statement = (EXPRESSION_STATEMENT_STRUCT) { (yyvsp[(1) - (2)]) };
		move_and_append_comments((yyvsp[(1) - (2)]), (yyvsp[(2) - (2)]));
		free((yyvsp[(2) - (2)]));
	}
    break;

  case 222:
#line 1549 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(IF_STATEMENT, (yyvsp[(1) - (5)]));
		(yyval)->if_statement = (IF_STATEMENT_STRUCT) { (yyvsp[(3) - (5)]), (yyvsp[(5) - (5)]), NULL };
		move_and_append_comments((yyval), (yyvsp[(1) - (5)]));
		free((yyvsp[(1) - (5)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (5)]));
		free((yyvsp[(2) - (5)]));
		move_and_append_comments((yyvsp[(3) - (5)]), (yyvsp[(4) - (5)]));
		free((yyvsp[(4) - (5)]));
	}
    break;

  case 223:
#line 1559 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(IF_STATEMENT, (yyvsp[(1) - (7)]));
		(yyval)->if_statement = (IF_STATEMENT_STRUCT) { (yyvsp[(3) - (7)]), (yyvsp[(5) - (7)]), (yyvsp[(7) - (7)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (7)]));
		free((yyvsp[(1) - (7)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (7)]));
		free((yyvsp[(2) - (7)]));
		move_and_append_comments((yyvsp[(3) - (7)]), (yyvsp[(4) - (7)]));
		free((yyvsp[(4) - (7)]));
		move_and_append_comments((yyvsp[(5) - (7)]), (yyvsp[(6) - (7)]));
		free((yyvsp[(6) - (7)]));
	}
    break;

  case 224:
#line 1571 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(SWITCH_STATEMENT, (yyvsp[(1) - (5)]));
		(yyval)->switch_statement = (SWITCH_STATEMENT_STRUCT) { (yyvsp[(3) - (5)]), (yyvsp[(5) - (5)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (5)]));
		free((yyvsp[(1) - (5)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (5)]));
		free((yyvsp[(2) - (5)]));
		move_and_append_comments((yyvsp[(3) - (5)]), (yyvsp[(4) - (5)]));
		free((yyvsp[(4) - (5)]));
	}
    break;

  case 225:
#line 1584 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(WHILE_STATEMENT, (yyvsp[(1) - (5)]));
		(yyval)->while_statement = (WHILE_STATEMENT_STRUCT) { (yyvsp[(3) - (5)]), (yyvsp[(5) - (5)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (5)]));
		free((yyvsp[(1) - (5)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (5)]));
		free((yyvsp[(2) - (5)]));
		move_and_append_comments((yyvsp[(3) - (5)]), (yyvsp[(4) - (5)]));
		free((yyvsp[(4) - (5)]));
	}
    break;

  case 226:
#line 1594 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(DO_WHILE_STATEMENT, (yyvsp[(1) - (7)]));
		(yyval)->do_while_statement = (DO_WHILE_STATEMENT_STRUCT) { (yyvsp[(2) - (7)]), (yyvsp[(5) - (7)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (7)]));
		free((yyvsp[(1) - (7)]));
		move_and_append_comments((yyvsp[(2) - (7)]), (yyvsp[(3) - (7)]));
		free((yyvsp[(3) - (7)]));
		move_and_append_comments((yyvsp[(2) - (7)]), (yyvsp[(4) - (7)]));
		free((yyvsp[(4) - (7)]));
		move_and_append_comments((yyvsp[(5) - (7)]), (yyvsp[(6) - (7)]));
		free((yyvsp[(6) - (7)]));
		move_and_append_comments((yyvsp[(5) - (7)]), (yyvsp[(7) - (7)]));
		free((yyvsp[(7) - (7)]));
	}
    break;

  case 227:
#line 1608 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(FOR_STATEMENT, (yyvsp[(1) - (6)]));
		(yyval)->for_statement = (FOR_STATEMENT_STRUCT) { (yyvsp[(3) - (6)]), (yyvsp[(4) - (6)]), NULL, (yyvsp[(6) - (6)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (6)]));
		free((yyvsp[(1) - (6)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (6)]));
		free((yyvsp[(2) - (6)]));
		move_and_append_comments((yyvsp[(4) - (6)]), (yyvsp[(5) - (6)]));
		free((yyvsp[(5) - (6)]));
	}
    break;

  case 228:
#line 1619 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(FOR_STATEMENT, (yyvsp[(1) - (7)]));
		(yyval)->for_statement = (FOR_STATEMENT_STRUCT) { (yyvsp[(3) - (7)]), (yyvsp[(4) - (7)]), (yyvsp[(5) - (7)]), (yyvsp[(7) - (7)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (7)]));
		free((yyvsp[(1) - (7)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (7)]));
		free((yyvsp[(2) - (7)]));
		move_and_append_comments((yyvsp[(5) - (7)]), (yyvsp[(6) - (7)]));
		free((yyvsp[(6) - (7)]));
	}
    break;

  case 229:
#line 1629 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(FOR_STATEMENT, (yyvsp[(1) - (6)]));
		(yyval)->for_statement = (FOR_STATEMENT_STRUCT) { (yyvsp[(3) - (6)]), (yyvsp[(4) - (6)]), NULL, (yyvsp[(6) - (6)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (6)]));
		free((yyvsp[(1) - (6)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (6)]));
		free((yyvsp[(2) - (6)]));
		move_and_append_comments((yyvsp[(4) - (6)]), (yyvsp[(5) - (6)]));
		free((yyvsp[(5) - (6)]));
	}
    break;

  case 230:
#line 1639 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(FOR_STATEMENT, (yyvsp[(1) - (7)]));
		(yyval)->for_statement = (FOR_STATEMENT_STRUCT) { (yyvsp[(3) - (7)]), (yyvsp[(4) - (7)]), (yyvsp[(5) - (7)]), (yyvsp[(7) - (7)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (7)]));
		free((yyvsp[(1) - (7)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (7)]));
		free((yyvsp[(2) - (7)]));
		move_and_append_comments((yyvsp[(5) - (7)]), (yyvsp[(6) - (7)]));
		free((yyvsp[(6) - (7)]));
	}
    break;

  case 231:
#line 1652 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(GOTO_STATEMENT, (yyvsp[(1) - (3)]));
		(yyval)->goto_statement = (GOTO_STATEMENT_STRUCT) { (yyvsp[(2) - (3)])->identifier.id };
		move_and_append_comments((yyval), (yyvsp[(1) - (3)]));
		free((yyvsp[(1) - (3)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (3)]));
		free((yyvsp[(2) - (3)]));
		move_and_append_comments((yyval), (yyvsp[(3) - (3)]));
		free((yyvsp[(3) - (3)]));
	}
    break;

  case 232:
#line 1662 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(LOOP_JUMP_STATEMENT, (yyvsp[(1) - (2)]));
		(yyval)->loop_jump_statement = (LOOP_JUMP_STATEMENT_STRUCT) {
			CONTINUE_STATEMENT
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (2)]));
		free((yyvsp[(1) - (2)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (2)]));
		free((yyvsp[(2) - (2)]));
	}
    break;

  case 233:
#line 1672 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(LOOP_JUMP_STATEMENT, (yyvsp[(1) - (2)]));
		(yyval)->loop_jump_statement = (LOOP_JUMP_STATEMENT_STRUCT) {
			BREAK_STATEMENT
		};
		move_and_append_comments((yyval), (yyvsp[(1) - (2)]));
		free((yyvsp[(1) - (2)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (2)]));
		free((yyvsp[(2) - (2)]));
	}
    break;

  case 234:
#line 1682 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(RETURN_STATEMENT, (yyvsp[(1) - (2)]));
		(yyval)->return_statement = (RETURN_STATEMENT_STRUCT) { NULL };
		move_and_append_comments((yyval), (yyvsp[(1) - (2)]));
		free((yyvsp[(1) - (2)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (2)]));
		free((yyvsp[(2) - (2)]));
	}
    break;

  case 235:
#line 1690 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(RETURN_STATEMENT, (yyvsp[(1) - (3)]));
		(yyval)->return_statement = (RETURN_STATEMENT_STRUCT) { (yyvsp[(2) - (3)]) };
		move_and_append_comments((yyval), (yyvsp[(1) - (3)]));
		free((yyvsp[(1) - (3)]));
		move_and_append_comments((yyval), (yyvsp[(2) - (3)]));
		free((yyvsp[(3) - (3)]));
	}
    break;

  case 236:
#line 1701 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(TRANSLATION_UNIT, (yyvsp[(1) - (1)]));
		(yyval)->translation_unit = (TRANSLATION_UNIT_STRUCT) { NULL, (yyvsp[(1) - (1)]), 0, NULL};
		c_translation_unit = (yyval);
	}
    break;

  case 237:
#line 1706 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(TRANSLATION_UNIT, (yyvsp[(1) - (2)]));
		(yyval)->translation_unit = (TRANSLATION_UNIT_STRUCT) { (yyvsp[(1) - (2)]), (yyvsp[(2) - (2)]), 0, NULL };
		c_translation_unit = (yyval);
	}
    break;

  case 238:
#line 1714 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 239:
#line 1715 "../../../c/transformer/pct_c_parser.y"
    { (yyval) = (yyvsp[(1) - (1)]); }
    break;

  case 240:
#line 1719 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(FUNCTION_DEFINITION, (yyvsp[(1) - (3)]));
		(yyval)->function_definition = (FUNCTION_DEFINITION_STRUCT) {
			NULL, (yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]), (yyvsp[(3) - (3)])
		};
	}
    break;

  case 241:
#line 1725 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(FUNCTION_DEFINITION, (yyvsp[(1) - (2)]));
		(yyval)->function_definition = (FUNCTION_DEFINITION_STRUCT) {
			NULL, (yyvsp[(1) - (2)]), NULL, (yyvsp[(2) - (2)])
		};
	}
    break;

  case 242:
#line 1731 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(FUNCTION_DEFINITION, (yyvsp[(1) - (4)]));
		(yyval)->function_definition = (FUNCTION_DEFINITION_STRUCT) {
			(yyvsp[(1) - (4)]), (yyvsp[(2) - (4)]), (yyvsp[(3) - (4)]), (yyvsp[(4) - (4)])
		};
	}
    break;

  case 243:
#line 1737 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(FUNCTION_DEFINITION, (yyvsp[(1) - (3)]));
		(yyval)->function_definition = (FUNCTION_DEFINITION_STRUCT) {
			(yyvsp[(1) - (3)]), (yyvsp[(2) - (3)]), NULL, (yyvsp[(3) - (3)])
		};
	}
    break;

  case 244:
#line 1746 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(DECLARATION_LIST, (yyvsp[(1) - (1)]));
		(yyval)->declaration_list = (DECLARATION_LIST_STRUCT) { NULL, (yyvsp[(1) - (1)]) };
	}
    break;

  case 245:
#line 1750 "../../../c/transformer/pct_c_parser.y"
    {
		(yyval) = create_node(DECLARATION_LIST, (yyvsp[(1) - (2)]));
		(yyval)->declaration_list = (DECLARATION_LIST_STRUCT) { (yyvsp[(1) - (2)]), (yyvsp[(2) - (2)]) };
	}
    break;


/* Line 1267 of yacc.c.  */
#line 4756 "../../../c/transformer/pct_c_parser.c"
      default: break;
    }
  YY_SYMBOL_PRINT ("-> $$ =", yyr1[yyn], &yyval, &yyloc);

  YYPOPSTACK (yylen);
  yylen = 0;
  YY_STACK_PRINT (yyss, yyssp);

  *++yyvsp = yyval;


  /* Now `shift' the result of the reduction.  Determine what state
     that goes to, based on the state we popped back to and the rule
     number reduced by.  */

  yyn = yyr1[yyn];

  yystate = yypgoto[yyn - YYNTOKENS] + *yyssp;
  if (0 <= yystate && yystate <= YYLAST && yycheck[yystate] == *yyssp)
    yystate = yytable[yystate];
  else
    yystate = yydefgoto[yyn - YYNTOKENS];

  goto yynewstate;


/*------------------------------------.
| yyerrlab -- here on detecting error |
`------------------------------------*/
yyerrlab:
  /* If not already recovering from an error, report this error.  */
  if (!yyerrstatus)
    {
      ++yynerrs;
#if ! YYERROR_VERBOSE
      yyerror (YY_("syntax error"));
#else
      {
	YYSIZE_T yysize = yysyntax_error (0, yystate, yychar);
	if (yymsg_alloc < yysize && yymsg_alloc < YYSTACK_ALLOC_MAXIMUM)
	  {
	    YYSIZE_T yyalloc = 2 * yysize;
	    if (! (yysize <= yyalloc && yyalloc <= YYSTACK_ALLOC_MAXIMUM))
	      yyalloc = YYSTACK_ALLOC_MAXIMUM;
	    if (yymsg != yymsgbuf)
	      YYSTACK_FREE (yymsg);
	    yymsg = (char *) YYSTACK_ALLOC (yyalloc);
	    if (yymsg)
	      yymsg_alloc = yyalloc;
	    else
	      {
		yymsg = yymsgbuf;
		yymsg_alloc = sizeof yymsgbuf;
	      }
	  }

	if (0 < yysize && yysize <= yymsg_alloc)
	  {
	    (void) yysyntax_error (yymsg, yystate, yychar);
	    yyerror (yymsg);
	  }
	else
	  {
	    yyerror (YY_("syntax error"));
	    if (yysize != 0)
	      goto yyexhaustedlab;
	  }
      }
#endif
    }



  if (yyerrstatus == 3)
    {
      /* If just tried and failed to reuse look-ahead token after an
	 error, discard it.  */

      if (yychar <= YYEOF)
	{
	  /* Return failure if at end of input.  */
	  if (yychar == YYEOF)
	    YYABORT;
	}
      else
	{
	  yydestruct ("Error: discarding",
		      yytoken, &yylval);
	  yychar = YYEMPTY;
	}
    }

  /* Else will try to reuse look-ahead token after shifting the error
     token.  */
  goto yyerrlab1;


/*---------------------------------------------------.
| yyerrorlab -- error raised explicitly by YYERROR.  |
`---------------------------------------------------*/
yyerrorlab:

  /* Pacify compilers like GCC when the user code never invokes
     YYERROR and the label yyerrorlab therefore never appears in user
     code.  */
  if (/*CONSTCOND*/ 0)
     goto yyerrorlab;

  /* Do not reclaim the symbols of the rule which action triggered
     this YYERROR.  */
  YYPOPSTACK (yylen);
  yylen = 0;
  YY_STACK_PRINT (yyss, yyssp);
  yystate = *yyssp;
  goto yyerrlab1;


/*-------------------------------------------------------------.
| yyerrlab1 -- common code for both syntax error and YYERROR.  |
`-------------------------------------------------------------*/
yyerrlab1:
  yyerrstatus = 3;	/* Each real token shifted decrements this.  */

  for (;;)
    {
      yyn = yypact[yystate];
      if (yyn != YYPACT_NINF)
	{
	  yyn += YYTERROR;
	  if (0 <= yyn && yyn <= YYLAST && yycheck[yyn] == YYTERROR)
	    {
	      yyn = yytable[yyn];
	      if (0 < yyn)
		break;
	    }
	}

      /* Pop the current state because it cannot handle the error token.  */
      if (yyssp == yyss)
	YYABORT;


      yydestruct ("Error: popping",
		  yystos[yystate], yyvsp);
      YYPOPSTACK (1);
      yystate = *yyssp;
      YY_STACK_PRINT (yyss, yyssp);
    }

  if (yyn == YYFINAL)
    YYACCEPT;

  *++yyvsp = yylval;


  /* Shift the error token.  */
  YY_SYMBOL_PRINT ("Shifting", yystos[yyn], yyvsp, yylsp);

  yystate = yyn;
  goto yynewstate;


/*-------------------------------------.
| yyacceptlab -- YYACCEPT comes here.  |
`-------------------------------------*/
yyacceptlab:
  yyresult = 0;
  goto yyreturn;

/*-----------------------------------.
| yyabortlab -- YYABORT comes here.  |
`-----------------------------------*/
yyabortlab:
  yyresult = 1;
  goto yyreturn;

#ifndef yyoverflow
/*-------------------------------------------------.
| yyexhaustedlab -- memory exhaustion comes here.  |
`-------------------------------------------------*/
yyexhaustedlab:
  yyerror (YY_("memory exhausted"));
  yyresult = 2;
  /* Fall through.  */
#endif

yyreturn:
  if (yychar != YYEOF && yychar != YYEMPTY)
     yydestruct ("Cleanup: discarding lookahead",
		 yytoken, &yylval);
  /* Do not reclaim the symbols of the rule which action triggered
     this YYABORT or YYACCEPT.  */
  YYPOPSTACK (yylen);
  YY_STACK_PRINT (yyss, yyssp);
  while (yyssp != yyss)
    {
      yydestruct ("Cleanup: popping",
		  yystos[*yyssp], yyvsp);
      YYPOPSTACK (1);
    }
#ifndef yyoverflow
  if (yyss != yyssa)
    YYSTACK_FREE (yyss);
#endif
#if YYERROR_VERBOSE
  if (yymsg != yymsgbuf)
    YYSTACK_FREE (yymsg);
#endif
  /* Make sure YYID is used.  */
  return YYID (yyresult);
}


#line 1756 "../../../c/transformer/pct_c_parser.y"


/* Check the following patterns:
 *   ... typedef ... id;
 *   ... typedef ... (*id)(...);
 *   etc
 * If found, "id" will be recognized as a type name in the remainder of the
 * source; otherwise, "id" will be reported as an ordinary identifier.
 */
void check_type_name_declaration(struct AST_NODE* declaration)
{
	BOOL has_typedef = 0;
	struct AST_NODE* init_declarator;
	struct AST_NODE* declarator;
	struct AST_NODE* direct_declarator;
	struct AST_NODE* declaration_specifiers;
	struct AST_NODE* list;
	char* identifier;
	
	if (declaration->type == DECLARATION) {
		declaration_specifiers =
			declaration->declaration.declaration_specifiers;
		while (declaration_specifiers != NULL) {
			if (declaration_specifiers->declaration_specifiers
					.specifier_or_qualifier != NULL
					&& declaration_specifiers->declaration_specifiers
					.specifier_or_qualifier->type == STORAGE_CLASS_SPECIFIER
					&& declaration_specifiers->declaration_specifiers
					.specifier_or_qualifier->storage_class_specifier
					.storage_class_specifier == TYPEDEF) {
				has_typedef = 1;
				break;
			} else
				declaration_specifiers =
					declaration_specifiers->declaration_specifiers
							.more_declaration_specifiers;
		}		
	}

	if (has_typedef) {
		list = declaration->declaration.init_declarator_list;
		while (list != NULL) {
			init_declarator = list->init_declarator_list.init_declarator;
			if (init_declarator != NULL) {
				declarator = init_declarator->init_declarator.declarator;
				if (declarator != NULL) {
					direct_declarator =
						declarator->declarator.direct_declarator;
					
					while (direct_declarator != NULL && direct_declarator->type
							!= NAMED_DIRECT_DECLARATOR)
						if (direct_declarator->type == ARRAY_DIRECT_DECLARATOR)
							direct_declarator = direct_declarator
									->array_direct_declarator.direct_declarator;
						else if (direct_declarator->type
								== PARAMETERIZED_DIRECT_DECLARATOR)
							direct_declarator = direct_declarator
									->parameterized_direct_declarator
											.direct_declarator;
						else if (direct_declarator->type == DECLARATOR)
							direct_declarator = direct_declarator
									->declarator.direct_declarator;
						else
							c_error("unknown direct declarator type");
					
					if (direct_declarator != NULL) {
						identifier = direct_declarator
								->named_direct_declarator.identifier;
						strlist_add(type_names, identifier);
					}
				}
			}
			list = list->init_declarator_list.left_init_declarator_list;
		}
	}
}

int c_error(char* const error)
{
	c_default_error_handler(error);
	return 0;
}

