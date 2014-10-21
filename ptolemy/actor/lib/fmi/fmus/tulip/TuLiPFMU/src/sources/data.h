#ifndef __DATA_H__
#define __DATA_H__

#include "polytope.h"
#include <ecos.h>
//the dimension of the state space
extern idxint n;

//the dimension of the observations
extern idxint m;

//the dimension of the control input
extern idxint p;

//the initial state
extern pfloat x0[];

extern idxint dRegion0;

extern pfloat A[];

extern pfloat B[];

//number of steps taken from one region to another
extern idxint totalSteps;

extern Polytope *input_bound;

void init_input_bound(void);

void free_input_bound(void);
#endif

