/**
 * dump.c
 * Dumps contents of TVM binary.
 */

#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include "language.h"
#include "load.h"

void *installedBinary;

void dumpCommon()
{
  MasterRecord *pRec;

  pRec = get_master_record();
  printf ("* MASTER RECORD:\n");
  printf ("  magic            : 0x%X\n", (int) (pRec->magicNumber));
  printf ("  constants at     : %d\n", (int) (pRec->constantTableOffset));
  printf ("  statics at       : %d\n", (int) (pRec->staticFieldsOffset));
  printf ("  static state at  : %d\n", (int) (pRec->staticStateOffset));
  printf ("  static state len : %d\n", (int) (pRec->staticStateLength));
  printf ("  last class index : %d\n", (int) (pRec->lastClass));
}

void dumpFields (ClassRecord *classRecord)
{
  byte fType;
  int i;
  printf ("\n");
  printf ("  ====== FIELDS:\n");
  for (i = 0; i < classRecord->numInstanceFields; i++)
  {
    fType = get_field_type (classRecord, i);
    printf ("      %d\n", (int) fType);
  }
}

void dumpMethods (ClassRecord *classRecord)
{
  MethodRecord *pRec;
  int i;
  printf ("\n");
  printf ("  ====== METHODS:\n");
  for (i = 0; i < classRecord->numMethods; i++)
  {
    printf ("  # METHOD %d:\n", i);
    pRec = get_method_record (classRecord, i);
    printf ("    signature     : %d\n", (int) (pRec->signatureId));
    printf ("    exceptions at : %d\n", (int) (pRec->exceptionTable));
    printf ("    code at       : %d\n", (int) (pRec->codeOffset));
  }
}

void dumpClass (int aIndex)
{
  ClassRecord *pRec;

  dumpCommon();  
  pRec = get_class_record (aIndex);
  printf ("* CLASS %d:\n", aIndex);
  printf ("  size / 2    : %d\n", (int) (pRec->classSize));
  printf ("  methods at  : %d\n", (int) (pRec->methodTableOffset));
  printf ("  fields at   : %d\n", (int) (pRec->instanceTableOffset));
  printf ("  num. fields : %d\n", (int) (pRec->numInstanceFields));
  printf ("  num. methods: %d\n", (int) (pRec->numMethods));
  printf ("  super       : %d\n", (int) (pRec->parentClass));
  printf ("  C_INIT      : %d\n", (int) is_initialized (pRec));
  printf ("  C_ARRAY     : %d\n", (int) is_array_class (pRec));
  printf ("  C_HASCLINIT : %d\n", (int) has_clinit (pRec));
  printf ("  C_INTERFACE : %d\n", (int) is_interface (pRec));
  dumpFields (pRec);
  dumpMethods (pRec);
}

void dumpConstant (int aIndex)
{
  dumpCommon();
  printf ("Not implemented yet!\n");
}

int main (int argc, char *argv[])
{
  int REQUEST;

  if (argc != 4)
    abort_tool ("Use: %s <path> [class|constant] <index>\n", argv[0]);
  readBinary (argv[1]);
  if (strcmp (argv[2], "class") == 0)
    dumpClass (atoi (argv[3]));
  else if (strcmp (argv[2], "constant") == 0)
    dumpConstant (atoi (argv[3]));
  else
    abort_tool ("Use: %s <path> [class|constant] <index>\n", argv[0]);
  return 0;
} 



