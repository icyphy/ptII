/*Polytope support--------------------------------*/

#ifndef __POLYTOPE_H__
#define __POLYTOPE_H__

#include <ecos.h>

/*
 * Polytope of the form Ax >= b 
 * A is a k by l  matrix
 * the entries of A is assumed to be arranged as[A11,A21,...,Ak1,A12,...,Ak2,...,A1l,...,Akl]
 * */
typedef struct Polytope {
	idxint k;
	idxint l;
	pfloat *A;
	pfloat *b;
	pfloat *center;
} Polytope;

void display_polytope(Polytope* p);

/*Check if a point x belongs to the Polytope p----*/
int isInside(const Polytope* p, const pfloat x[]);

/* create a polytope of the form
 * l_i<=x_i<=u_i, i =0,...,l-1
 * upper = [u_0,...,u_l-1]
 * lower = [l_0,...,l_l-1]
 */
Polytope* from_box(idxint l,pfloat* upper, pfloat* lower);

Polytope* create_poly(idxint k,idxint l,pfloat* A,pfloat* b,pfloat* center);

Polytope* cartesian_prod(Polytope* p1, Polytope* p2);

void free_polytope(Polytope* p);
#endif
