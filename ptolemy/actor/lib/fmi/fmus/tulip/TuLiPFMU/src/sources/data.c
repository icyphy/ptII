#include "data.h"
idxint n = 2;
idxint m = 2;
idxint p = 2;
pfloat A[] = {1.0,0.0,0.0,1.0,};
pfloat B[] = {0.1,0.0,0.0,0.1,};
idxint totalSteps = 8;
Polytope *input_bound;
idxint puk = 4;
idxint pul = 2;
pfloat puA[] = {-1.0,-0.0,1.0,0.0,-0.0,-1.0,0.0,1.0,};
pfloat pub[] = {-1.0,-1.0,-1.0,-1.0,};
pfloat pucenter[] = {0.0,0.0,};

void init_input_bound(void){
   input_bound=create_poly(puk,pul,puA,pub,pucenter);
};

void free_input_bound(void){
                FREE(input_bound);
};
pfloat x0[] = {1.5,1.5,};
idxint dRegion0 = 18;
