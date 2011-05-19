/* A Bison parser, made by GNU Bison 2.3.  */

/* Skeleton interface for Bison's Yacc-like parsers in C

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




#if ! defined YYSTYPE && ! defined YYSTYPE_IS_DECLARED
typedef int YYSTYPE;
# define yystype YYSTYPE /* obsolescent; will be withdrawn */
# define YYSTYPE_IS_DECLARED 1
# define YYSTYPE_IS_TRIVIAL 1
#endif

extern YYSTYPE c_lval;

