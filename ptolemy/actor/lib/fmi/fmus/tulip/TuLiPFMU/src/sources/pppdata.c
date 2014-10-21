#include "pppdata.h"

idxint nRegion = 20;
Polytope* regions[20];
/****Polytope 0 ****/
idxint p0k = 4;
idxint p0l = 2;
pfloat p0A[] = {-0.0,-1.0,1.0,-0.0,-1.0,-0.0,-0.0,1.0,};
pfloat p0b[] = {-1.72,-2.72,2.0,1.0,};
pfloat p0center[] = {2.36,1.36,};

/****Polytope 1 ****/
idxint p1k = 4;
idxint p1l = 2;
pfloat p1A[] = {1.0,-0.0,-1.0,-0.0,-0.0,1.0,-0.0,-1.0,};
pfloat p1b[] = {0.28,0.28,-1.0,-1.0,};
pfloat p1center[] = {0.64,0.64,};

/****Polytope 2 ****/
idxint p2k = 4;
idxint p2l = 2;
pfloat p2A[] = {-1.0,1.0,-0.0,-0.0,-0.0,-0.0,-1.0,1.0,};
pfloat p2b[] = {-2.0,1.28,-1.0,0.28,};
pfloat p2center[] = {1.64,0.64,};

/****Polytope 3 ****/
idxint p3k = 4;
idxint p3l = 2;
pfloat p3A[] = {-1.0,-0.0,1.0,-0.0,-0.0,1.0,-0.0,-1.0,};
pfloat p3b[] = {-2.72,0.28,2.0,-1.0,};
pfloat p3center[] = {2.36,0.64,};

/****Polytope 4 ****/
idxint p4k = 4;
idxint p4l = 2;
pfloat p4A[] = {1.0,-0.0,-1.0,-0.0,-0.0,-1.0,-0.0,1.0,};
pfloat p4b[] = {0.28,-1.72,-1.0,1.0,};
pfloat p4center[] = {0.64,1.36,};

/****Polytope 5 ****/
idxint p5k = 4;
idxint p5l = 2;
pfloat p5A[] = {-0.0,-0.0,1.0,-1.0,1.0,-1.0,-0.0,-0.0,};
pfloat p5b[] = {1.0,-1.72,1.0,-1.28,};
pfloat p5center[] = {1.14,1.14,};

/****Polytope 6 ****/
idxint p6k = 4;
idxint p6l = 2;
pfloat p6A[] = {-1.0,0.0,1.0,0.0,-0.0,1.0,0.0,-1.0,};
pfloat p6b[] = {-3.0,-0.0,2.0,-0.28,};
pfloat p6center[] = {2.14,0.14,};

/****Polytope 7 ****/
idxint p7k = 4;
idxint p7l = 2;
pfloat p7A[] = {-0.0,1.0,-1.0,0.0,-1.0,0.0,-0.0,1.0,};
pfloat p7b[] = {-2.0,-0.0,-1.0,1.72,};
pfloat p7center[] = {0.14,1.86,};

/****Polytope 8 ****/
idxint p8k = 4;
idxint p8l = 2;
pfloat p8A[] = {-0.0,1.0,-1.0,0.0,-1.0,0.0,0.0,1.0,};
pfloat p8b[] = {-2.0,1.0,-1.28,1.72,};
pfloat p8center[] = {1.14,1.86,};

/****Polytope 9 ****/
idxint p9k = 4;
idxint p9l = 2;
pfloat p9A[] = {0.0,-1.0,0.0,1.0,1.0,-0.0,-1.0,0.0,};
pfloat p9b[] = {-0.0,-2.0,-0.28,1.72,};
pfloat p9center[] = {1.86,0.14,};

/****Polytope 10 ****/
idxint p10k = 4;
idxint p10l = 2;
pfloat p10A[] = {-1.0,-0.0,0.0,1.0,-0.0,-1.0,1.0,0.0,};
pfloat p10b[] = {-3.0,-2.0,1.0,2.72,};
pfloat p10center[] = {2.86,1.14,};

/****Polytope 11 ****/
idxint p11k = 4;
idxint p11l = 2;
pfloat p11A[] = {-0.0,-1.0,-0.0,1.0,1.0,-0.0,-1.0,0.0,};
pfloat p11b[] = {0.28,-3.0,-1.0,2.72,};
pfloat p11center[] = {2.86,0.42,};

/****Polytope 12 ****/
idxint p12k = 4;
idxint p12l = 2;
pfloat p12A[] = {-1.0,-0.0,1.0,0.0,-0.0,-1.0,-0.0,1.0,};
pfloat p12b[] = {-2.72,-2.0,2.0,1.72,};
pfloat p12center[] = {2.14,1.86,};

/****Polytope 13 ****/
idxint p13k = 4;
idxint p13l = 2;
pfloat p13A[] = {-1.0,1.0,-0.0,0.0,-0.0,-0.0,-1.0,1.0,};
pfloat p13b[] = {-2.0,1.28,-2.0,1.72,};
pfloat p13center[] = {1.42,1.86,};

/****Polytope 14 ****/
idxint p14k = 4;
idxint p14l = 2;
pfloat p14A[] = {-1.0,1.0,0.0,0.0,-0.0,0.0,1.0,-1.0,};
pfloat p14b[] = {-1.0,-0.0,-0.0,-0.28,};
pfloat p14center[] = {0.14,0.14,};

/****Polytope 15 ****/
idxint p15k = 4;
idxint p15l = 2;
pfloat p15A[] = {1.0,-1.0,-0.0,-0.0,-0.0,-0.0,1.0,-1.0,};
pfloat p15b[] = {1.0,-1.72,-0.0,-0.28,};
pfloat p15center[] = {1.14,0.14,};

/****Polytope 16 ****/
idxint p16k = 4;
idxint p16l = 2;
pfloat p16A[] = {-0.0,-0.0,1.0,-1.0,1.0,-1.0,-0.0,0.0,};
pfloat p16b[] = {0.28,-1.0,-0.0,-0.28,};
pfloat p16center[] = {0.14,0.42,};

/****Polytope 17 ****/
idxint p17k = 4;
idxint p17l = 2;
pfloat p17A[] = {-0.0,1.0,-0.0,-1.0,-1.0,-0.0,1.0,0.0,};
pfloat p17b[] = {-1.72,-0.0,1.0,-0.28,};
pfloat p17center[] = {0.14,1.14,};

/****Polytope 18 ****/
idxint p18k = 4;
idxint p18l = 2;
pfloat p18A[] = {-1.0,1.0,-0.0,0.0,-0.0,-0.0,-1.0,1.0,};
pfloat p18b[] = {-2.0,1.28,-1.72,1.0,};
pfloat p18center[] = {1.64,1.36,};

/****Polytope 19 ****/
idxint p19k = 4;
idxint p19l = 2;
pfloat p19A[] = {-0.0,1.0,-1.0,0.0,1.0,-0.0,-0.0,-1.0,};
pfloat p19b[] = {0.28,1.0,-1.28,-1.0,};
pfloat p19center[] = {1.14,0.42,};

void init_region() {
   regions[0]=create_poly(p0k,p0l,p0A,p0b,p0center);
   regions[1]=create_poly(p1k,p1l,p1A,p1b,p1center);
   regions[2]=create_poly(p2k,p2l,p2A,p2b,p2center);
   regions[3]=create_poly(p3k,p3l,p3A,p3b,p3center);
   regions[4]=create_poly(p4k,p4l,p4A,p4b,p4center);
   regions[5]=create_poly(p5k,p5l,p5A,p5b,p5center);
   regions[6]=create_poly(p6k,p6l,p6A,p6b,p6center);
   regions[7]=create_poly(p7k,p7l,p7A,p7b,p7center);
   regions[8]=create_poly(p8k,p8l,p8A,p8b,p8center);
   regions[9]=create_poly(p9k,p9l,p9A,p9b,p9center);
   regions[10]=create_poly(p10k,p10l,p10A,p10b,p10center);
   regions[11]=create_poly(p11k,p11l,p11A,p11b,p11center);
   regions[12]=create_poly(p12k,p12l,p12A,p12b,p12center);
   regions[13]=create_poly(p13k,p13l,p13A,p13b,p13center);
   regions[14]=create_poly(p14k,p14l,p14A,p14b,p14center);
   regions[15]=create_poly(p15k,p15l,p15A,p15b,p15center);
   regions[16]=create_poly(p16k,p16l,p16A,p16b,p16center);
   regions[17]=create_poly(p17k,p17l,p17A,p17b,p17center);
   regions[18]=create_poly(p18k,p18l,p18A,p18b,p18center);
   regions[19]=create_poly(p19k,p19l,p19A,p19b,p19center);
};

void free_region() {
    int i;
    for (i=0;i<nRegion;i++)
    {
        FREE(regions[i]);
    }
};