/**
 * javaexec.c
 * By Jose Solorzano
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include "util.h"

#ifdef __CYGWIN__
#include <sys/cygwin.h>
#endif

#define TRACE 0
#define MAX_PATH 255

#define TOOL_NAME "java"
#define TOOL_ALT_VAR "JAVA"
#define CLASS_NAME "js.tinyvm.TinyVM"

#ifndef WRITE_ORDER
#error WRITE_ORDER undefined
#endif

#ifndef LOADER_TOOL
#error LOADER_TOOL undefined
#endif

#ifdef WINNT
#define PATH_SEPARATOR ";"
#else
#define PATH_SEPARATOR ":"
#endif

#define REL_TOOLS_JAR "/../lib/jtools.jar"
#define REL_LIB_JAR   "/../lib/classes.jar"
#define TOOLS_JAR "/lib/jtools.jar"
#define LIB_JAR   "/lib/classes.jar"

char *get_classpath (char *program, char *relpath)
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
  lejosjar = append (dname, relpath);
  
  #ifdef __CYGWIN__
  auxstr = (char *) malloc (MAX_PATH);
  cygwin_conv_to_win32_path (lejosjar, auxstr);
  lejosjar = auxstr;

  #if TRACE
  printf ("converted=%s\n", lejosjar);
  #endif
  
  #endif __CYGWIN__
  
  oldcpath = getenv ("CLASSPATH");
  if (oldcpath == NULL)
    oldcpath = "";
  cpath = lejosjar;   
  if (strcmp (oldcpath, "") != 0)
  {
    cpath = append (cpath, PATH_SEPARATOR);
    cpath = append (cpath, oldcpath);
  }
  return cpath;
}

void set_classpath (char *toolsPath)
{
  char *envasg;
  
  #ifdef __CYGWIN__
  char *auxstr;
  #endif __CYGWIN__

  #ifdef __CYGWIN__
  auxstr = (char *) malloc (MAX_PATH);
  cygwin_conv_to_win32_path (toolsPath, auxstr);
  toolsPath = auxstr;

  #if TRACE
  printf ("converted=%s\n", toolsPath);
  #endif
  
  #endif __CYGWIN__

  envasg = append ("CLASSPATH=", toolsPath);
  #if TRACE
  printf ("setting %s\n", envasg);
  #endif
  putenv (envasg); 	
}

char *get_loader_classpath (char *libpath)
{
  char *cpath, *oldcpath;
  #ifdef __CYGWIN__
  char *auxstr;
  #endif __CYGWIN__

  #ifdef __CYGWIN__
  auxstr = (char *) malloc (MAX_PATH);
  cygwin_conv_to_win32_path (libpath, auxstr);
  libpath = auxstr;

  #if TRACE
  printf ("converted=%s\n", libpath);
  #endif
  
  #endif __CYGWIN__
  
  oldcpath = getenv ("CLASSPATH");
  if (oldcpath == NULL)
    oldcpath = "";
  cpath = libpath;   
  if (strcmp (oldcpath, "") != 0)
  {
    cpath = append (cpath, PATH_SEPARATOR);
    cpath = append (cpath, oldcpath);
  }
  return cpath;
}

int main (int argc, char *argv[])
{
  int pStatus;
  int count, i;
  char *toolName;
  char *toolsPath, *libPath;
  char *tinyvmHome;
  char *directory;
  char **newargv;
  #ifdef __CYGWIN__
  char *auxstr;
  #endif __CYGWIN__
  
  directory = pdirname (argv[0]);
  if (directory == NULL)
  {
    fprintf (stderr, "Unexpected: Can't find %s in PATH\n", argv[0]);
    exit (1);
  }
  tinyvmHome = append (directory, "/..");   

  #ifdef __CYGWIN__
  auxstr = (char *) malloc (MAX_PATH);
  cygwin_conv_to_win32_path (tinyvmHome, auxstr);
  tinyvmHome = auxstr;

  #if TRACE
  printf ("converted=%s\n", tinyvmHome);
  #endif
  
  #endif __CYGWIN__

  newargv = (char **) malloc (sizeof (char *) * (argc + 20));
  count = 0;
  toolsPath = append (tinyvmHome, TOOLS_JAR);
  libPath = append (tinyvmHome, LIB_JAR);

  toolName = getenv (TOOL_ALT_VAR);
  if (toolName == NULL)
    toolName = TOOL_NAME;
  
  newargv[count++] = toolName;
  newargv[count++] = "-Dtinyvm.write.order=" WRITE_ORDER;
  newargv[count++] = "-Dtinyvm.loader=" LOADER_TOOL;
  newargv[count++] = append ("-Dtinyvm.home=", tinyvmHome); 
  newargv[count++] = CLASS_NAME;  
  newargv[count++] = "-classpath";
  newargv[count++] = get_loader_classpath (libPath);
  for (i = 1; i < argc; i++)
  {
    newargv[count++] = argv[i];	  
  }
  newargv[count++] = NULL;
    
  #if TRACE
  printf ("toolName=%s\n", toolName);
  for (i = 0; newargv[i] != NULL; i++)
    printf ("arg[%d]=%s\n", i, newargv[i]);
  #endif

  set_classpath (toolsPath);  
  pStatus = execvp (toolName, newargv);
  fprintf (stderr, "Unable to execute %s. Return status of exec is %d.\n", toolName, (int) pStatus);
  fprintf (stderr, "Make sure %s is in the PATH, or define " TOOL_ALT_VAR ".\n", toolName);
  return 1;	
}
