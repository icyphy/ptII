/**
 * jtoolexec.c
 * By Jose Solorzano
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include "util.h"

#ifdef __CYGWIN__
#include <sys/cygwin.h>
#endif

#define TRACE 1
#define MAX_PATH 255

#ifndef TOOL_NAME
#error TOOL_NAME undefined
#endif

#ifndef TOOL_ALT_VAR
#error TOOL_ALT_VAR undefined
#endif

#ifdef WINNT
#define PATH_SEPARATOR ";"
#else
#define PATH_SEPARATOR ":"
#endif

#define REL_JAR_PATH "/../lib/classes.jar"

char *get_classpath (char *program)
{
  char *dname;
  char *cpath;
  char *oldcpath;
  char *lejosjar;
  #ifdef __CYGWIN__
  char *auxstr;
  #endif __CYGWIN__

  dname = pdirname (program);
  if (dname == NULL)
    return NULL;
  lejosjar = append (dname, REL_JAR_PATH);
  
  #ifdef __CYGWIN__
  auxstr = (char *) malloc (MAX_PATH);
  cygwin_conv_to_win32_path (lejosjar, auxstr);
  lejosjar = auxstr;

  #if TRACE
  printf ("converted=%s\n", lejosjar);
  #endif
  
  #endif __CYGWIN__
  
  cpath = lejosjar;   
  
  #ifndef JAVA2
  oldcpath = getenv ("CLASSPATH");
  if (oldcpath == NULL)
    oldcpath = "";
  if (strcmp (oldcpath, "") != 0)
  {
    cpath = append (cpath, PATH_SEPARATOR);
    cpath = append (cpath, oldcpath);
  }
  #endif
  
  return cpath;
}

int main (int argc, char *argv[])
{
  int pStatus;
  int count, i;
  char *toolName;
  char *cpath;
  char **newargv;

  
  newargv = (char **) malloc (sizeof (char *) * (argc + 3));
  count = 0;
  cpath = get_classpath (argv[0]);
  
  #if TRACE
  printf ("classpath=%s\n", cpath);
  #endif
  
  if (cpath == NULL)
  {
    fprintf (stderr, "Unexpected: Can't find %s in PATH\n", argv[0]);
    exit (1);
  }
  newargv[count++] = toolName;
  
  #ifdef JAVA2
  newargv[count++] = "-bootclasspath";
  #else
  newargv[count++] = "-classpath";
  #endif
  
  newargv[count++] = cpath;
  for (i = 1; i < argc; i++)
  {
    newargv[count++] = argv[i];	  
  }
  newargv[count++] = NULL;
  toolName = getenv (TOOL_ALT_VAR);
  if (toolName == NULL)
    toolName = TOOL_NAME;
    
  #if TRACE
  printf ("toolName=%s\n", toolName);
  #endif
  
  pStatus = execvp (toolName, newargv);
  fprintf (stderr, "Unable to execute %s. Return status of exec is %d.\n", toolName, (int) pStatus);
  fprintf (stderr, "Make sure %s is in the PATH, or define " TOOL_ALT_VAR ".\n", toolName);
  return 1;	
}
