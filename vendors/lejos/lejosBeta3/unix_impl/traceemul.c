
#include <stdio.h>
#include "trace.h"

void assert (boolean aCond, int aCode)
{
  if (aCond)
    return;
  printf ("Assertion violation: %d\n", aCode);
  exit (aCode);
}

