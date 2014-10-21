#ifndef __PPPDATA_H__
#define __PPPDATA_H__

#include "polytope.h"
#include <ecos.h>

extern idxint nRegion;
extern Polytope* regions[];

void init_region();
void free_region();
#endif
