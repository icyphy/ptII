
/*
Support macros required for handling arrays in Java to C translation. This
will be replaced by more comprehensive support for arrays in future versions of
the pccg runtime library.

@author Shuvra S. Bhattacharyya
@version $Id$
*/


#define PCCG_ARRAY_INSTANCE void*
#define PCCG_ARRAY_ACCESS(arrayBase, arrayIndex) \
(((float*)(arrayBase))[arrayIndex])
