/**
 * load.c
 * Loads binary into VM memory.
 */

#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include "language.h"
#include "magic.h"
#include "trace.h"
#include "types.h"

#ifndef O_BINARY
#define O_BINARY 0
#endif

void abort_tool (char *msg, char *arg)
{
  printf (msg, arg);
  exit (1);
}

void readBinary (char *fileName)
{
  int pDesc;
  int pLength;
  int pTotal;
  int pNumRead;
  byte *pBinary;

  pDesc = open (fileName, O_RDONLY | O_BINARY);
  if (pDesc == -1)
    abort_tool ("Unable to open %s\n", fileName);
  pLength = lseek (pDesc, 0, SEEK_END);
  lseek (pDesc, 0, SEEK_SET);
  pBinary = (void *) malloc (pLength);
  pTotal = 0;
  while (pTotal < pLength)
  {
    pNumRead = read (pDesc, pBinary + pTotal, pLength - pTotal);
    if (pNumRead == -1)
    {
      printf ("Unexpected EOF\n");
      exit (1);
    }
    pTotal += pNumRead;
  }
  #if DEBUG_STARTUP
  printf ("Installing binary %d\n", (int) pBinary);
  #endif
  install_binary (pBinary);

  #if DEBUG_STARTUP
  printf ("Checking validity of magic number\n");
  #endif
  if (get_master_record()->magicNumber != MAGIC)
  {
    printf ("Fatal: bad magic number: 0x%X. Linked for RCX?"
            "\n", get_master_record()->magicNumber);
    exit(1); 
  }
}







