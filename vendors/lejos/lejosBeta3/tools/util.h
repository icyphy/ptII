/**
 * util.h
 * By Jose Solorzano
 */
 
#ifndef _UTIL_H
#define _UTIL_H

extern int lastIndexOf (char *string, char character);
extern char *newString (char *string);
extern char *substring (char *string, int aIdx1, int aIdx2);
extern char *dirname (char *fileName);
extern char *which (char *program);
extern char *append (char *str1, char *str2);
extern char *pdirname (char *argv0);

#endif _UTIL_H

