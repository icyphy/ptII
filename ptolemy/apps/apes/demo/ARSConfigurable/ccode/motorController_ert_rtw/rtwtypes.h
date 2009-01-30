/*    
 * File: rtwtypes.h
 *
 * Definitions required by Real-Time Workshop generated code.
 *  
 * Real-Time Workshop version: 6.0
 * Generated on: 2009-01-20 14:56:56
 */

#ifndef __RTWTYPES_H__
#define __RTWTYPES_H__
#ifndef __TMWTYPES__
#define __TMWTYPES__

#include <limits.h>

/*=======================================================================*  
 * Fixed width word size data types:                                     *  
 *   int8_T, int16_T, int32_T     - signed 8, 16, or 32 bit integers     *  
 *   uint8_T, uint16_T, uint32_T  - unsigned 8, 16, or 32 bit integers   *  
 *   real32_T, real64_T           - 32 and 64 bit floating point numbers *  
 *=======================================================================*/

/* This ID is used to detect inclusion of an incompatible rtwtypes.h */
#define RTWTYPES_ID_C08S16I32L32N32F1

typedef signed char int8_T;
typedef unsigned char uint8_T;
typedef short int16_T;
typedef unsigned short uint16_T;
typedef int int32_T;
typedef unsigned int uint32_T;
typedef float real32_T;
typedef double real64_T;

/*===========================================================================*  
 * Generic type definitions: real_T, time_T, boolean_T, char_T, int_T,       *  
 *                           uint_T and byte_T.                              *  
 *===========================================================================*/

typedef double real_T;
typedef double time_T;
typedef unsigned char boolean_T;
typedef int int_T;
typedef unsigned int uint_T;
typedef char char_T;
typedef char_T byte_T;

/*===========================================================================*  
 * Complex number type definitions                                           *  
 *===========================================================================*/
#define CREAL_T

typedef struct {
  real32_T re;
  real32_T im;
} creal32_T;

typedef struct {
  real64_T re;
  real64_T im;
} creal64_T;

typedef struct {
  real_T re;
  real_T im;
} creal_T;

typedef struct {
  int8_T re;
  int8_T im;
} cint8_T;

typedef struct {
  uint8_T re;
  uint8_T im;
} cuint8_T;

typedef struct {
  int16_T re;
  int16_T im;
} cint16_T;

typedef struct {
  uint16_T re;
  uint16_T im;
} cuint16_T;

typedef struct {
  int32_T re;
  int32_T im;
} cint32_T;

typedef struct {
  uint32_T re;
  uint32_T im;
} cuint32_T;

/*=======================================================================*  
 * Min and Max:                                                          *  
 *   int8_T, int16_T, int32_T     - signed 8, 16, or 32 bit integers     *  
 *   uint8_T, uint16_T, uint32_T  - unsigned 8, 16, or 32 bit integers   *  
 *=======================================================================*/

#define MAX_int8_T                      ((int8_T)(127))
#define MIN_int8_T                      ((int8_T)(-128))
#define MAX_uint8_T                     ((uint8_T)(255))
#define MIN_uint8_T                     ((uint8_T)(0))
#define MAX_int16_T                     ((int16_T)(32767))
#define MIN_int16_T                     ((int16_T)(-32768))
#define MAX_uint16_T                    ((uint16_T)(65535))
#define MIN_uint16_T                    ((uint16_T)(0))
#define MAX_int32_T                     ((int32_T)(2147483647))
#define MIN_int32_T                     ((int32_T)(-2147483647-1))
#define MAX_uint32_T                    ((uint32_T)(0xFFFFFFFFU))
#define MIN_uint32_T                    ((uint32_T)(0))

/* Logical type definitions */
#if !defined(__cplusplus) && !defined(__bool_true_false_are_defined)
# ifndef _bool_T
# define _bool_T
typedef boolean_T bool;
# ifndef false
# define false (0)
# endif
# ifndef true
# define true (1)
# endif
# endif
#endif

#ifndef TRUE
# define TRUE (1)
#endif
#ifndef FALSE
# define FALSE (0)
#endif

/*   
 * Real-Time Workshop assumes the code is compiled on a target using a 2's compliment representation
 * for signed integer values.    
 */
#if ((SCHAR_MIN + 1) != -SCHAR_MAX)
#error "This code must be compiled using a 2's complement representation for signed integer values"
#endif

/*  
 * Maximum length of a MATLAB identifier (function/variable/model)  
 * including the null-termination character. Referenced by 
 * rt_logging.c and rt_matrx.c. 
 */
#define TMW_NAME_LENGTH_MAX             64
#endif                                  /* __TMWTYPES__ */

/* Block D-Work pointer type */
typedef void * pointer_T;

/* Simulink specific types */

#ifndef __SIMSTRUC_TYPES_H__
#define __SIMSTRUC_TYPES_H__

/* States of an enabled subsystem */
typedef enum {
  SUBSYS_DISABLED = 0,
  SUBSYS_ENABLED = 2,
  SUBSYS_BECOMING_DISABLED = 4,
  SUBSYS_BECOMING_ENABLED = 8,
  SUBSYS_TRIGGERED = 16
  } CondStates;

/* Trigger directions: falling, either, and rising */
typedef enum {
  FALLING_ZERO_CROSSING = -1,
  ANY_ZERO_CROSSING = 0,
  RISING_ZERO_CROSSING = 1
  } ZCDirection;

/* Previous state of a trigger signal */
typedef enum {
  NEG_ZCSIG = -1,
  ZERO_ZCSIG = 0,
  POS_ZCSIG = 1,
  ZERO_RISING_EV_ZCSIG = 100,           /* zero and had a rising event  */
  ZERO_FALLING_EV_ZCSIG = 101,          /* zero and had a falling event */
  UNINITIALIZED_ZCSIG = INT_MAX
  } ZCSigState;

/* Current state of a trigger signal */
typedef enum {
  FALLING_ZCEVENT = -1,
  NO_ZCEVENT = 0,
  RISING_ZCEVENT = 1
  } ZCEventType;

/* Enumeration of built-in data types */
typedef enum {
  SS_DOUBLE = 0,                        /* real_T    */
  SS_SINGLE = 1,                        /* real32_T  */
  SS_INT8 = 2,                          /* int8_T    */
  SS_UINT8 = 3,                         /* uint8_T   */
  SS_INT16 = 4,                         /* int16_T   */
  SS_UINT16 = 5,                        /* uint16_T  */
  SS_INT32 = 6,                         /* int32_T   */
  SS_UINT32 = 7,                        /* uint32_T  */
  SS_BOOLEAN = 8                        /* boolean_T */
  } BuiltInDTypeId;

#define SS_NUM_BUILT_IN_DTYPE           ((int)SS_BOOLEAN+1)

typedef int_T DTypeId;

#endif                                  /* __SIMSTRUC_TYPES_H__ */
#endif                                  /* __RTWTYPES_H__ */
