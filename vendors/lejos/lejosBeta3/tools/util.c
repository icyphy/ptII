/**
 * util.c
 * By Jose Solorzano
 */

#include <sys/types.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/stat.h>
#include "util.h"

int indexOf (char *string, char character)
{
  int pLen;
  int pIdx;
  char c;
  
  pIdx = 0;
  while ((c = string[pIdx]) != 0)
  {
    if (c == character)
      return pIdx;
    pIdx++;
  }
  return -1;	
}

int lastIndexOf (char *string, char character)
{
  int pLen;
  int pIdx;
  
  pLen = strlen (string);
  pIdx = pLen - 1;
  while (pIdx > 0)
  {
    char c = string[pIdx];
    if (c == character)
      return pIdx;
    pIdx--;
  }
  return -1;
}

char *newString (char *string)
{
  char *n;
  
  n = (char *) malloc (strlen(string) + 1);
  strcpy (n, string);
  return n;
}

/**
 * Allocates a substring.
 */
char *substring (char *string, int aIdx1, int aIdx2)
{
  int pLen;
  char *pNewStr;
  
  pLen = aIdx2 - aIdx1;
  if (pLen < 0)
    return NULL;
  pNewStr = (char *) malloc (pLen + 1);
  strncpy (pNewStr, string + aIdx1, pLen);
  pNewStr[pLen] = 0;
  return pNewStr;
}

/**
 * Allocates a string containing the directory name
 * for a file.
 */
char *dirname (char *fileName)
{
  int pIdx = lastIndexOf (fileName, '/');
  if (pIdx == -1)
    return newString ("");
  return substring (fileName, 0, pIdx);	
}

char *pdirname (char *argv0)
{
  char *d;
  char *fullpath;
  
  d = dirname (argv0);
  if (strcmp (d, "") == 0)
  {
    free (d);
    fullpath = which (argv0);
    if (fullpath == NULL)
      return NULL;
    d = dirname (fullpath);
    free (fullpath);
  }
  return d;
}

char *append (char *str1, char *str2)
{
  char *str;
  
  str = (char *) malloc (strlen (str1) + strlen (str2) + 1);
  strcpy (str, str1);
  strcat (str, str2);
  return str;
}

int fileExists (char *path)
{
  struct stat st;
  int pErr;
  
  pErr = stat (path, &st);
  return (pErr >= 0);
}

char *whichp (char *program, char *path)
{
  char *ptr;
  char *pDir, *pPath1, *pPath2;
  int  pRelIdx;
  
  ptr = path;
  for (;;)
  {
    pRelIdx = indexOf (ptr, ':');
    pDir = (pRelIdx == -1) ? newString (ptr) : substring (ptr, 0, pRelIdx);
    pPath1 = append (pDir, "/");
    pPath2 = append (pPath1, program);
    free (pDir);
    free (pPath1);
    if (fileExists (pPath2))
      return pPath2;
    free (pPath2);
    if (pRelIdx == -1)
      break;
    ptr += pRelIdx + 1;
  }
  return NULL;
}

/**
 * Allocates the full path to a program.
 */
char *which (char *program)
{
  char *path;

  path = getenv ("PATH");
  if (path == NULL)
    return NULL;
  return whichp (program, path);
}

