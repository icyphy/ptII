/* 
*
* THIS MATERIAL IS PROVIDED AS IS, WITH ABSOLUTELY NO WARRANTY EXPRESSED
* OR IMPLIED.  ANY USE IS AT YOUR OWN RISK.                                   
*
*		This code is derived from GC software contributed to Mepco by
* R.Baskar, S.Price Philomen Raj, V.Ramakrishnan, V.S.Vijayraj.
*
*	The Students of the Computer Science and Engineering Department ,
* Mepco Schlenk Engineering College , Sivakasi , Tamil Nadu , INDIA
* Madurai Kamaraj University .  
*
*		Permission is hereby granted to use or copy this program
* for any purpose,  provided the above notices are retained on all copies.
* Permission to modify the code and to distribute modified code is granted,
* provided the above notices are retained, and a notice that the code was
* modified is included with the above
*/

/*

		<<Inclusion of the Header Files used in the project an the reason for their inclusion follows>>
		
*/

/* 

	The header file signal.h is used to handle the signals while finding the stack address limit

*/

#include <signal.h>

/*

  The header file stdio.h is used for standard input and output operations.
 
*/

#include <stdio.h>

/*
	
  The stdlib.h file is included inorder to make use of the functions available in the standard library like malloc

*/

#include <stdlib.h>

/*

	The setjmp.h has the definition for longjmp and setjmp operations used for non local jumping.

*/

#include <setjmp.h>

/* 
	
  The unistd.h is used to read the values of environment variables

*/

#include <unistd.h>

/*

  The file stddef included as it defines datatypes like size_t

*/

#include <stddef.h>

/*

  This header file is included inorder to enable us to do string operations

*/

#include <string.h>

/*

  Defining booloean values

*/

#define TRUE 1
#define FALSE 0

/*

The following declaration using typedef makes data type "int" and "BOOL" to mean the same.

*/

typedef int BOOL;


/*

  Coding Conventions :

  GC  : Functions.
  gc  : Local variables.
  gcS : variables inside a structure
  gcG : global variables
  _   : datatypes which are of type structure
  gcA : user defined data type
  
*/

/*

  Book keeping list structure

*/

/*

  The following Structure defines the contents of each of the nodes in the book keeping linked list maintatined by us.

*/

typedef struct bk
{
	/*

	gcS_allocated_heap_ptr : This holds the address of the memory block thats getting allocated.

	gcS_size_allocated : This holds the size of the memory block thats being pointed to by the gcS_allocated_heap_ptr

	gcS_used : This field denotes whether the memory region pointed to by the gcS_allocated_heap_ptr is currently being referenced or not . This is the field that the GC routine checks when trying to free the gcS_allocated_hep_ptr.

	gcS_checked : This field says whether the memory pointed to by gcS_allocated_heap_ptr must be subject to a sliding window search for the presence of pointers to other live heap objects. 

	gcS_next_bk : This points to the next block in the book keeping linked list.

	*/

	void * gcS_allocated_heap_ptr; 
	size_t gcS_size_allocated;
	BOOL gcS_used;
	BOOL gcS_checked;
	void (*gcS_finalizer)();
	struct bk * gcS_next_bk;

}_book_keeping_block;  



/* 

  GLOBAL VARIABLE DECLARATIONS START HERE 

*/

/*

  end : Points to the memory location which marks the end of the datasegment ( initialized and initialized data segments )

  etext : Points to the memory location next to the text segment.

*/

int end,etext;

/*

  gcG_book_keeping_header : holds the address of the book keeping header

*/

_book_keeping_block * gcG_book_keeping_header;

/*

  gcG_jmpbuffer : This forms the argument to the setjmp and the longjmp functions used for non local jumping

*/

jmp_buf gcG_jmpbuffer;

/*

defining the max size of allocation ie) max heap size - restricted . Our Periodic GC algorithm runs when ever the no of allocated bytes equals the below number 640K bytes

*/

size_t gcG_max_heap_size;

/* 

defining the incremet size of of the heap

*/

/* size_t gcG_heap_increment = 50000 * 1024 ; */

/*

 used to keep trace of the no of bytes so far allocated

 */

size_t gcG_current_heap_size;  

/* GLOBAL VARIABLE DECLARATIONS END HERE */

extern void GC_checkforlimit( void ( * gc_finalizer)() );
extern void GC_check_before_freeing( void * gc_allocated_heap_ptr , size_t gc_allocated_size );
extern void * GC_collect( size_t gc_size , void (*gc_finalizer)() );
extern void GC_collect_book_keeping_garbage();
extern void GC_collect_collectables();
extern void GC_do_book_keeping( void * gc_ptr , size_t gc_size , void (*gc_finalizer)() );
extern void * GC_find_stack_high_ptr();
extern void GC_free( void * gc_waste );
extern int GC_get_to_be_freed_count();
extern void * GC_malloc( size_t gc_size );
extern void * GC_malloc_uncollectable( size_t gc_size );
extern void * GC_malloc_finalizer( size_t gc_size , void ( * gc_finalizer)() );
extern BOOL GC_search_in_addr_space( void * gc_low_addr , void * gc_high_addr , void * gc_to_search_addr );
extern BOOL GC_search_in_heap_addr_space( void * gc_heap_low_addr , void * gc_heap_high_addr , void * gc_to_search_addr);
extern void GC_sig( int gc_val );
extern void GC_set_gc_rate( size_t max_heap_size );
extern int GC_search_in_referenced_allocated_block( void * gc_ptr , size_t gc_size );
