#include "GC.h"
#include "stdio.h"

/*

defining the max size of allocation ie) max heap size - restricted . Our Periodic GC algorithm runs when ever the no of allocated bytes equals the below number 640K bytes

*/

size_t gcG_max_heap_size = 1024*320 /*1024 * 640*/;

/* 

defining the incremet size of of the heap

*/

/* size_t gcG_heap_increment = 50000 * 1024 ; */

/*

 used to keep trace of the no of bytes so far allocated

 */

size_t gcG_current_heap_size = 0;  

/*

Function Name : set_max_heap_size

Argument : Heap limit

Return Type : None

*/

void GC_set_gc_rate( size_t max_heap_size )
{
	gcG_max_heap_size = max_heap_size;
}

/*

 Function Name : GC_collect_book_keeping_garbage.

 Aruguments : None.

 Return Type : void.

 Explanation : This Function searches the book-keeping linked list that we maintain and frees up the nodes that have their gcS_used field set to FALSE and gcS_collectable set to TRUE. The nodes freed by this procedure correspond to the ones that have their gcS_allocated_heap_ptr field pointing to memory regions which have been declared as garbage and freed up by our GC algorithm. As those garbages have already been freed , the nodes that maintain their book-keeping information serve no purpose. So they are also getting freed up by the "GC_collect_book_keeping_garbage" procedure.
 
*/

void GC_collect_book_keeping_garbage()
{
	/*

	gcA_prev , gcA_current , gcA_temp : These variables point to the nodes of the book-keeping list that we maintain and are used in the process of pointer adjustments that occur during the deletion of an element in a singly linked list.

	*/

	_book_keeping_block * gcA_prev , * gcA_current , * gcA_temp;
	
	/*

	gcA_prev : initially points the node previous to the first node in the book-keeping list.

	*/

	gcA_prev = gcG_book_keeping_header;

	/*

	gcA_current : initially points to the first node in the book-keeping list , which is the node next to the header of the book-keeping list.

	*/

	gcA_current = gcG_book_keeping_header->gcS_next_bk;


	/* While the end of the list is reached */ 
	while( gcA_current != NULL ) 
	{
		/* if the current book-keeping node points ( field - gcS_allocated_heap_ptr ) to a garbage */
		if( gcA_current->gcS_used == FALSE )
		{
			/*
			The following 3 lines of code do the pointer manipulations that we need to do when deleting a node from the linked list.
			*/
			
			gcA_temp = gcA_prev->gcS_next_bk;
			gcA_prev->gcS_next_bk = gcA_current->gcS_next_bk;
			gcA_current = gcA_prev->gcS_next_bk;
			
			/*
			
			The following 6 lines of code write dummy/NULL values in the fields of the node ( book keeping ) that is to be freed just before the node ( book keeping )
			
			*/


			
			gcA_temp -> gcS_allocated_heap_ptr = NULL ;
			gcA_temp -> gcS_used = -1;
			gcA_temp -> gcS_next_bk = NULL ;
			gcA_temp -> gcS_size_allocated = 0;
			gcA_temp -> gcS_finalizer = NULL;
			gcA_temp -> gcS_checked = -1;

			/*
			
			free the selected book keeping node 

			*/

			GC_free(gcA_temp);
		}
		/* if the current book-keeping node points ( field - gcS_allocated_heap_ptr ) to a node that is NOT a garbage */
		else
		{
			/* The following 2 lines of code adjust the pointers in such a way that they point to the next nodes to be taken in to consideration */
			gcA_prev = gcA_prev->gcS_next_bk;
			gcA_current = gcA_current->gcS_next_bk;
		}
	}
}


/*

Function Name : GC_malloc_uncollectable

Arguments : Size of the memory to be allocated

Return Type : Address of the allocated object if successful , NULL if not.

*/

void * GC_malloc_uncollectable( size_t gc_size)
{
	/* 
	Allocate using the standard library malloc routine and return
	*/

	return( malloc( gc_size ) );
}

/*

Function Name : GC_search_in_referenced_allocated_block

Aruguments : a) gc_search_start_ptr - address of the location from which search is to commence.
			 b) gc_allocated_size - This field specifies the size of the memory region starting from " gc_search_start_ptr " in which search for other live objects is to be made.

Return Type : int 

Explanation : This function " GC_search_in_referenced_allocated_block " takes the address of an object that has already been declared as live and the size of the live object as arguments and searches inside it for the presence of pointers to other objects so that they also could be declared live.

*/

int GC_search_in_referenced_allocated_block( void * gc_search_start_ptr , size_t gc_allocated_size )
{
	/*

	gc_temp initially points to the starting address of the block to be searched.
	gc_refered_count is initially set to the value of zero and is used to count the number of live objects to which the the current live object points to. This serves as the stopping condition for the function that calls it , if this is non zero then we have more live objects which are also to be subject to the same search.
	gcA_temp initially points to the first block in the book-keeping linked list.

	*/

	void *gc_temp = gc_search_start_ptr;
	int gc_refered_count=0;
	_book_keeping_block * gcA_temp ;
	gcA_temp = gcG_book_keeping_header->gcS_next_bk;

	/* while end of the current block ( gc_temp+gc_allocated_size - 4 ) is reached */
	while( gc_search_start_ptr <= (gc_temp + gc_allocated_size - 4) )
	{
		/* gcA_temp is made to point to the first block in the book-keeping linked list */
		gcA_temp = gcG_book_keeping_header->gcS_next_bk;

		/* while the end of the book keeping block is not reached */
		while(gcA_temp!=NULL)
		{
			/* if the object pointed to by this book keeping block is currently not considered to be live ( this check is made as there is no point in making an already live object live again ) && if the block that is being subject to search has a pointer to another allocated object */
			if( gcA_temp->gcS_used == FALSE && *(void**)gc_search_start_ptr == gcA_temp->gcS_allocated_heap_ptr)
			{
				/* Increase the gc_refered_count value and declare the object that is pointed to by this live object as also live */
				gc_refered_count++;
				gcA_temp->gcS_used = TRUE ;
			}
			
			/*

			make gcA_temp to point to the next block in the book keeping list.

			*/
			gcA_temp = gcA_temp->gcS_next_bk;  
		}

		/* Initially " gc_search_start_ptr " points to the first location of the current block which is subject to search for other live objects. The following statement increments that pointer to point to the next location. Actually , this processing may be concidered to be just like a sliding window that starts from the start of the block ( gc_search_start_ptr ) and then slides down till the end of the block. The sliding being effected by incrementing the pointer and thats what the next statement does */
		gc_search_start_ptr++;
	}

	/* The following statement returns the number of new live objects that have been identified */
	return gc_refered_count;
} 

/*

Function Name : GC_sig

Aruguments : an integer 

Return Type : void

Explanation : This function is used as the signal handler. The signals that this function is designated to handle are the signals SIGBUS and SIGSEGV. These signals correspond to the bus error and segmentation violation.

*/

void GC_sig(int gc_val)
{
/*

	The function longjmp is used for the purpose of non-local jumping , that is jumping between functions. The control is returned to the location where setjmp has been set and the return value is given as the second argument ( here its 1 ).

    Actual Declarations : 
     1) int setjmp(jmp_buf env)
     2) void longjmp(jmp_buf env, int val)
                                     
    The setjmp() function save their calling environment in env. Each of these functions returns 0.

    The corresponding longjmp() functions restore the environment saved by their most recent respective invocations of the setjmp() function.  They then return so that program execution continues as if the corresponding invocation of the setjmp() call had just returned  the value specified by val, instead of 0.

	Refer the book " Advanced programming in the Unix environment" by Richard Stevens for more details.

*/
	longjmp(gcG_jmpbuffer,1);
}								 

/*

Function Name : GC_free

Aruguments : Address of the location thats to be freed. This address corresponds to the one returned by malloc. 

Return Type : void

Explanation : Memory allocated using malloc get de-allocated(freed) that is added to the free list so that they can be re-used by using the function "free".

*/

void GC_free( void * gc_waste )
{
	/*

	deallocate the memory region pointed to by gc_waste using the function "free". The function "free" adds the memory region to the free-list.

	*/
	free(gc_waste);
	return;
}


/*

Function Name : GC_find_stack_high_ptr

Aruguments : void 

Return Type : address , void pointer .

Explanation : Inorder to search the stack , we need to know the address bounds of the stack , that is the low and the high address of the stack. The high address of the stack is returned by this function. What we do is we declare a variable "gc_current" and store the address of this variable in "gc_ptr". "gc_ptr" now holds an address which in the stack as "gc_current" was declared as a local variable. We now keep on incrementing this address and dereference the incremented address.The variable "gc_dereference" is used for this purpose. When we just cross the stack boundary , the system will alert us by producing the signal SIGBUS or SIGSEGV. So, we have crossed the high boundary of the stack now and " gc_ptr " points to the memory location that is one location beyond the boundary. So we return " gc_ptr-1 " as the return value of this function. Please note that the signal is produced only if the memory is dereferenced.

*/

void* GC_find_stack_high_ptr()
{
	int *gc_ptr,gc_current,gc_dereference;
	/*
	store a stack address in gc_ptr
	*/
	gc_ptr=&gc_current;
	/*
	set the point of return for the longjmp function returning with value 1.
	*/
	if( setjmp(gcG_jmpbuffer)!=0 )
	{
		return gc_ptr-1;		
	}
	while(1)
	{
		gc_ptr++;
		gc_dereference=*gc_ptr;
		if(signal(SIGBUS,GC_sig)==SIG_ERR || signal(SIGSEGV,GC_sig)==SIG_ERR )
		{ 
			/* exit if there is any error in declaring the signal handlers */
			fprintf(stderr,"\n\n\nFATAL ERROR ( IN MESSAGE PASSING USING SIGNALS ) WHILE TRYING TO GARBAGE COLLECT. GC ROUTINE IS EXITING . CONTACT GC PROGRAMMERS \n\n\n");
			exit(1);
		}
	}	   
  return gc_ptr-1;
}				

/*

Function Name : GC_search_in_addr_space

Aruguments : a) gc_low_addr - holds the low address of the memory/address space to be searched.

			 b) gc_high_addr - holds the high address of the memory/address space to be searched.
			 
			 c) gc_to_search_addr - holds the address to be searched in the memory region bounded by "gc_low_addr" and "gc_high_addr".

Return Type : BOOL. 

Explanation : This function searches for the presence of the address " gc_to_search_addr " in the memory region bounded by "gc_low_addr" and "gc_high_addr".It returns TRUE if the searched address was found in the address space and returns FALSE if the searched address was not found in the address space.

*/

BOOL GC_search_in_addr_space(void * gc_low_addr , void * gc_high_addr , void * gc_to_search_addr)
{
	/*

	gc_ptr_to_memory_ptr : This is declared as a double pointer as we are searching an address in the address space. so any variable that holds an address is a pointer to a pointer. 
	gc_unit_increment : This is used increment the pointer / addresses by one so as to comepare from the next location.Actually , this processing may be considered to be just like a sliding window that starts from the start of the block ( gc_low_addr ) and then slides down till the end of the block ( gc_high_addr ) . The sliding being effected by incrementing this pointer. The pointer slides by 1 unit to take the next 32bits into consideration each time.

	*/
	void ** gc_ptr_to_memory_ptr;
	void * gc_unit_increment;

	/*

	gc_unit_increment is made to point initially to the high address of the search space given by gc_high_addr.

	*/
	gc_unit_increment = gc_high_addr;

	/* while the low address of the search space is reached */
	while( gc_unit_increment > gc_low_addr )
	{
		/* position the sliding window to the correct location */
		gc_unit_increment--;
		/* store the contents of that location in gc_ptr_to_memory_ptr */
		gc_ptr_to_memory_ptr = (void**)gc_unit_increment;
		/* If the contents of the location under consideration ( position of the sliding window ) is the address we are searching for then return TRUE ( search hit ) */
		if( (*gc_ptr_to_memory_ptr) == gc_to_search_addr )
			return TRUE;
	}

	/* If the address we are searching for is not in the search space , then return FALSE ( search miss ) */
	return FALSE;	  
} 

/*

Function Name : GC_collect_collectables.

Arguments : None.

Return Type : void.

Explanation : This function frees the memory locations ( sweep phase ) that have been declared as garbage. Just before freeing the locations this function writes the value NULL into the locations being freed using the function bzero. We write NULL to the locations that are being freed up because the inbuilt function "free" that is available in the standard library does not initialize the locations that are being freed up. This (using bzero) is needed because when we search the heap say next time these locations (freed up) may be retaining their values and search on the heap for addresses may result in false hits. This inturn may lead to the declaration of unlive objects as live by our GC algorithm.

*/

void GC_collect_collectables()
{ 
	/*
	gcA_temp is made to initially point to the first node ( next to the header ) of the book-keeping list
	*/
	_book_keeping_block * gcA_temp ;
	gcA_temp = gcG_book_keeping_header->gcS_next_bk;
	/* SWEEP PHASE */
	/* while the end of the book-keeping linked list is reached */
	while( gcA_temp != NULL) 
	{
		/* if the node under consideration is a garbage and if its collectable */
		if( gcA_temp->gcS_used == FALSE ) 
		{
/* first write NULL values in the location that is going to be freed up */
//bzero(gcA_temp->gcS_allocated_heap_ptr , gcA_temp->gcS_size_allocated );
			/* free the location , that is add the memory area to the free list */
			if( gcA_temp->gcS_finalizer != NULL)
				(* gcA_temp->gcS_finalizer)();
			
 			gcG_current_heap_size -= gcA_temp->gcS_size_allocated; 
			GC_free(gcA_temp->gcS_allocated_heap_ptr);
		}
		/* gcA_temp is made to point to the next block inorder to take the next block into consideration */
		gcA_temp = gcA_temp->gcS_next_bk;
	}

	/* Now as we are freeing up some locations  which have been declared as garbage and were allocated using GC_malloc , the book-keeping that we have been maintaining for those locations are also unnesesary. The following function " GC_collect_book_keeping_garbage " is used to free those locations */
	GC_collect_book_keeping_garbage();
}


/*

*/

void  GC_collect_garbage( void (*gc_finalizer)() )
{
        /*

    gc_stack_low_ptr : used to store the low address of the stack.
        gc_stack_high_ptr : used to store the high address of the stack.
        gc_stack_current_var : local variable declared to take the low address o
f the stack(address of this variable itself as the stack low adddress !!!. This
is because local variables are allocated from the stack and each allocated varia
ble from the stack is allocated in a lower address location than its predecessor
).
        gcA_temp : used while traversing the book keeping linked list.
        gc_ptr : used to store the return address of malloc.
        gc_flag : a static variable used to make sure that certain statements ar
e executed only once.
        gc_refered_count : used to keep track of the number of new live objects
have been detected and which are to be searched again for the presence of pointe
rs to other objects , which can also be declared alive. Search for more live obj
ects can be stopped once the number of live objects found in one epoch is zero.

        */              
	static void * gc_stack_high_ptr;
    	void * gc_stack_low_ptr;
        int gc_stack_current_var;
        _book_keeping_block * gcA_temp;
        /*void *gc_ptr;*/
        static int gc_flag = 0; // flag to find the stack boundary just once
        int gc_refered_count=1;



        /*

        The following line of code finds the stack low address

    */

        gc_stack_low_ptr = &gc_stack_current_var;


        /*

        The following is a block thats executed only once and is used to finding
 the stack high address.       

	 */
        if(gc_flag == 0)
        {
                gc_stack_high_ptr = GC_find_stack_high_ptr();
                gc_flag = 1;
        }

        /*

        gcA_temp is made to initially point to the first node in the linked list
, the node next to the header of the list.

        */
        gcA_temp = gcG_book_keeping_header->gcS_next_bk;

        /* while the end of the book-keeping list is reached */
        while( gcA_temp != NULL )
        {
                /* Initialize fields of the book-keeping list so as to assume th
at none of the objects are in use initially ( gcA_temp->used=FALSE ) and none ha
ve been checked for the presence of pointers to other objects( gcA_temp->gcS_che
cked=FALSE )*/  
		gcA_temp->gcS_used = FALSE;
                gcA_temp->gcS_checked = FALSE;
                /* to take the next block into consideration */
                gcA_temp = gcA_temp->gcS_next_bk;
    	}

        /*

                gcA_temp is made to initially point to the first node in the lin
ked list, the node next to the header of the list.

        */
        gcA_temp = gcG_book_keeping_header->gcS_next_bk;

        /* while the end of the book-keeping list is reached */
        while( gcA_temp != NULL )
        {
            /* search for the presence of the allocated objects addresses in the
 stack. If they are in the stack then they are in use and are not garbage. TRUE is
 returned if they are available in the stack and FALSE if not.*/
                gcA_temp->gcS_used = GC_search_in_addr_space( gc_stack_low_ptr ,
 gc_stack_high_ptr , gcA_temp->gcS_allocated_heap_ptr);
                /* if the searched address was not in the stack */     
		if( gcA_temp->gcS_used == FALSE )
                        /* search for the presence of the allocated objects addr
esses in the GLOBAL VARIABLE ADDRESS SPACE whose bouds are given by the values &
etext( low address , address next to the text segment ) and &end( high address ,
 address just before the start of the heap ). If they are in the heap then they
are in use and are not garbage. TRUE is returned if they are available in the st
ack and FALSE if not.*/
                         gcA_temp->gcS_used = GC_search_in_addr_space(&etext , &
end , gcA_temp->gcS_allocated_heap_ptr );
                /* to take the next block into consideration */
                gcA_temp = gcA_temp->gcS_next_bk;

        }

        /* While there are objects that have been newly declared as live */
        while( gc_refered_count > 0 )
        {
                /* initialize the count of new live objects to 0 */
                gc_refered_count=0;
                /* gcA_temp is made to initially point to the first node in the
linked list, the node next to the header of the list. */
                gcA_temp = gcG_book_keeping_header->gcS_next_bk;     
		gcA_temp = gcG_book_keeping_header->gcS_next_bk;
                /* while the end of the book-keeping list is reached */
                while( gcA_temp != NULL )
                {
                        /* If the object under consideration is live ( gcA_temp-
>gcS_used == TRUE ) and it is yet to  be searched for the prevence of pointers t
o other live objects ( gcA_temp->gcS_checked == FALSE )*/
                        if( gcA_temp->gcS_used == TRUE && gcA_temp->gcS_checked
== FALSE )
                        {
                                /* Set the field stating that the object pointed
 to by this book-keeping block has already been subject to search */
                                gcA_temp->gcS_checked = TRUE;
                                /* search for the precence of pointers to other
objects inside this live object */
                   gc_refered_count += GC_search_in_referenced_allocated_block(gcA_temp->gcS_allocated_heap_ptr , gcA_temp->gcS_size_allocated ); 
                        }
                        /* to take the next block into consideration */
                        gcA_temp = gcA_temp->gcS_next_bk;
                }

        }                   	
	 /* Collect the objects that have been declared as garbage */
        GC_collect_collectables();   
        return;
}
 
/*

Function Name : GC_collect

Arguments : The size of memory request that made " malloc " return NULL which in turn resulted in the envoking of the GC routine.

Return Type :  A pointer which corresponds to the address of the memory location of size " gc_size " , allocated using malloc after the completion of GC. 

Explanation :  This function tries to resatisfy the request for memory after garbage collection.

*/

void * GC_collect( size_t gc_size,void (*gc_finalizer)() )
{
	/*

    gc_stack_low_ptr : used to store the low address of the stack.
	gc_stack_high_ptr : used to store the high address of the stack.
	gc_stack_current_var : local variable declared to take the low address of the stack(address of this variable itself as the stack low adddress !!!. This is because local variables are allocated from the stack and each allocated variable from the stack is allocated in a lower address location than its predecessor).
	gcA_temp : used while traversing the book keeping linked list.
	gc_ptr : used to store the return address of malloc.
	gc_flag : a static variable used to make sure that certain statements are executed only once.
	gc_refered_count : used to keep track of the number of new live objects have been detected and which are to be searched again for the presence of pointers to other objects , which can also be declared alive. Search for more live objects can be stopped once the number of live objects found in one epoch is zero.

	*/
	static void * gc_stack_high_ptr;
	void * gc_stack_low_ptr;
	int gc_stack_current_var;
	_book_keeping_block * gcA_temp;
	void *gc_ptr;
	static int gc_flag = 0; // flag to find the stack boundary just once
	int gc_refered_count=1;
	

	/*

	The following line of code finds the stack low address 

    */

	gc_stack_low_ptr = &gc_stack_current_var;


	/*

	The following is a block thats executed only once and is used to finding the stack high address.

	*/
	if(gc_flag == 0)
	{
		
		gc_stack_high_ptr = GC_find_stack_high_ptr();
		gc_flag = 1;
	} 
	
	/*

	gcA_temp is made to initially point to the first node in the linked list, the node next to the header of the list.

	*/

	gcA_temp = gcG_book_keeping_header->gcS_next_bk; 
	
	/* while the end of the book-keeping list is reached */
	while( gcA_temp != NULL )
	{
		/* Initialize fields of the book-keeping list so as to assume that none of the objects are in use initially ( gcA_temp->used=FALSE ) and none have been checked for the presence of pointers to other objects( gcA_temp->gcS_checked=FALSE )*/
		gcA_temp->gcS_used = FALSE;
 		gcA_temp->gcS_checked = FALSE;
		/* to take the next block into consideration */
		gcA_temp = gcA_temp->gcS_next_bk;
    }

	/*

		gcA_temp is made to initially point to the first node in the linked list, the node next to the header of the list.

	*/
	gcA_temp = gcG_book_keeping_header->gcS_next_bk; 

	/* while the end of the book-keeping list is reached */
	while( gcA_temp != NULL )
	{
	    /* search for the presence of the allocated objects addresses in the stack. If they are in the stack then they are in use and are not garbage. TRUE is returned if they are available in the stack and FALSE if not.*/	
		gcA_temp->gcS_used = GC_search_in_addr_space( gc_stack_low_ptr , gc_stack_high_ptr , gcA_temp->gcS_allocated_heap_ptr);
		/* if the searched address was not in the stack */
		if( gcA_temp->gcS_used == FALSE )
			/* search for the presence of the allocated objects addresses in the GLOBAL VARIABLE ADDRESS SPACE whose bouds are given by the values &etext( low address , address next to the text segment ) and &end( high address , address just before the start of the heap ). If they are in the heap then they are in use and are not garbage. TRUE is returned if they are available in the stack and FALSE if not.*/	 
			 gcA_temp->gcS_used = GC_search_in_addr_space(&etext , &end , gcA_temp->gcS_allocated_heap_ptr );
		/* to take the next block into consideration */
		gcA_temp = gcA_temp->gcS_next_bk;

	}  
	
	/* While there are objects that have been newly declared as live */
	while( gc_refered_count > 0 )
	{
		/* initialize the count of new live objects to 0 */
		gc_refered_count=0;
		/* gcA_temp is made to initially point to the first node in the linked list, the node next to the header of the list. */
		gcA_temp = gcG_book_keeping_header->gcS_next_bk; 
		/* while the end of the book-keeping list is reached */
		while( gcA_temp != NULL )
		{
			/* If the object under consideration is live ( gcA_temp->gcS_used == TRUE ) and it is yet to  be searched for the prevence of pointers to other live objects ( gcA_temp->gcS_checked == FALSE )*/
			if( gcA_temp->gcS_used == TRUE && gcA_temp->gcS_checked == FALSE )
			{
				/* Set the field stating that the object pointed to by this book-keeping block has already been subject to search */
				gcA_temp->gcS_checked = TRUE;
				/* search for the precence of pointers to other objects inside this live object */
				gc_refered_count = GC_search_in_referenced_allocated_block(gcA_temp->gcS_allocated_heap_ptr , gcA_temp->gcS_size_allocated );
			}
			/* to take the next block into consideration */
			gcA_temp = gcA_temp->gcS_next_bk;
		}

 	}


   /* Collect the objects that have been declared as garbage */
	GC_collect_collectables();
	
	/* try to resatisfy the request after garbage collection */
	
	gc_ptr = malloc(gc_size); 
	
	/* make an entry in the book-keeping liked list for the newly allocated block , if one has been successfully allocated.*/
	if( gc_ptr != NULL )
		GC_do_book_keeping(gc_ptr,gc_size,gc_finalizer);
	
	printf("\n Exiting from GC");

	/* Return the pointer returned by malloc */
	return (gc_ptr);
		
}

/*

Function Name : GC_do_book_keeping

Arguments : The address of the object for which book keeping is to be done and the size of that object.

Return Type : void.

Explanation : This function is used to keep track of the details about the objects that we ahve allocated. Its actually a singly linked list and has a list header node as the first node , followed by the nodes maintaining the object information. New elements are added at the front of the list.

*/

void GC_do_book_keeping(void * gc_ptr , size_t gc_size , void (* gc_finalizer)() )
{
	_book_keeping_block * gcA_temp;
	/* if the linked list is empty , create header and initialize it */
	if(gcG_book_keeping_header == NULL)
	{
		/* allocate memory for the linked list header and initialize the fields */
		gcG_book_keeping_header = malloc(sizeof(_book_keeping_block));
		gcG_book_keeping_header->gcS_allocated_heap_ptr = NULL;
		/* used is set as true as this header should never be collected */
		gcG_book_keeping_header->gcS_used = TRUE;
		gcG_book_keeping_header->gcS_size_allocated = 0;
		gcG_book_keeping_header->gcS_finalizer = NULL;
		gcG_book_keeping_header->gcS_next_bk = NULL;
		/* This field is set as TRUE as we this node is not going to point to any data and no processing is necessary */
		gcG_book_keeping_header->gcS_checked = TRUE;
		
	}

	/* allocate memory to store the details of the new object */
	gcA_temp=malloc(sizeof(_book_keeping_block));
	/* make the  "gcS_allocated_heap_ptr" field to point to the new object */
	gcA_temp->gcS_allocated_heap_ptr = gc_ptr;
	/* set the "gcS_size_allocated" field to the size of the new object */ 
	gcA_temp->gcS_size_allocated = gc_size;
	/* All objects are initially assumed to be not in use */
	gcA_temp->gcS_used = FALSE;
	/* The object pointed to by this node is yet to be subjected to the search for the availability of the pointers to other objects */
	gcA_temp->gcS_checked = FALSE; 
	/* insert the new node in to the beginning of the list ( next 2 lines )*/
	gcA_temp->gcS_next_bk = gcG_book_keeping_header->gcS_next_bk;

	gcA_temp->gcS_finalizer = gc_finalizer;
	gcG_book_keeping_header->gcS_next_bk = gcA_temp;
	return;
}				 

/*

Function Name : GC_checkforlimit

Arguments : address of the finalizer if any , else NULL

Return type : void 

Explanation : Our GC Algorithm runs periodically. That is when the total size of the allocated objects is greater than the previously allocated sizelimit fixed by the variable gcG_max_heap_size.


*/
void GC_checkforlimit( void ( * gc_finalizer)() )
{
        //printf("PCCG: Creent Heap Size is %d\n", gcG_current_heap_size);
	/*char ch;*/
        if( gcG_current_heap_size > gcG_max_heap_size )
        {
                //printf("PCCG:COLLECT\n");
                GC_collect_garbage(gc_finalizer);
                gcG_current_heap_size =0;
        }
        return;
}   


/*

Function Name : GC_malloc

Arguments : Size of the object thats to be allocated from the heap.

Return Type : Address of the newly allocated object of size gc_size if successful and NULL if allocation is not possible 

Explanation : This function is what the standard library function malloc does but the difference being this that the memory allocated by this function will be subject to garbage collection , that is will be garbage collected by our GC algorithm.

*/

void * GC_malloc(size_t gc_size)
{ 

	/* This variable "gc_ptr" is used to store the address of the newly allocated object */	
	void * gc_ptr;
	/* This variable is used to as for choice from the user */
	char gc_continue;

	/* If the size of the object that has been requested for allocation is < 0 then the followinf is the handler for this unusual case in which the choice of continuing or exiting from the program will be given to the user */
	if(gc_size < 0)
	{
		printf("\nGC_ERROR 1 : The argument for the GC_malloc function should always be positive");
		printf("\nDo u want to exit or continue without allocation (y/n ) : ");
		scanf(" %c",&gc_continue);
		if(gc_continue=='y')
			exit(1);
		else
			 return NULL;
	}


	/* call the library routing malloc to allocate memory */
	gc_ptr=malloc(gc_size);

	/* if malloc succeeds in allocating */

	if(gc_ptr!=NULL)
	{
		/* do book keeping */
		GC_do_book_keeping(gc_ptr,gc_size,NULL);
		GC_checkforlimit(NULL);
	}
	else
	{
		/* if malloc fails to allocate , call the GC routine */
		printf("\nEntering into GC routine");
		gc_ptr = GC_collect(gc_size,NULL);

		/* if allocation also fails after GC */

		if(gc_ptr == NULL)
		{
			/* return NULL */
			printf("\nYour Request cannot be satisfied even after Garbage Collection \n");
			return NULL;		
		}
	}	

	/* if allocation suceeds return the pointer to the new object */
	gcG_current_heap_size += gc_size;
	return gc_ptr;
}

void * GC_malloc_finalizer(size_t gc_size , void ( * gc_finalizer)() )
{ 
	/* This variable "gc_ptr" is used to store the address of the newly allocated object */	
	void * gc_ptr;
	/* This variable is used to as for choice from the user */
	char gc_continue;

	/* If the size of the object that has been requested for allocation is < 0 then the followinf is the handler for this unusual case in which the choice of continuing or exiting from the program will be given to the user */
	if(gc_size < 0)
	{
		printf("\nGC_ERROR 1 : The argument for the GC_malloc function should always be positive");
		printf("\nDo u want to exit or continue without allocation (y/n ) : ");
		scanf(" %c",&gc_continue);
		if(gc_continue=='y')
			exit(1);
		else
			 return NULL;
	}

	/* call the library routing malloc to allocate memory */

	gc_ptr=malloc(gc_size);
	/* if malloc succeeds in allocating */

	if(gc_ptr!=NULL)
	{
		/* do book keeping */
		GC_do_book_keeping(gc_ptr,gc_size,gc_finalizer);
		GC_checkforlimit(gc_finalizer);
	}
	else
	{
		/* if malloc fails to allocate , call the GC routine */
		printf("\nEntering into GC routine");
		gc_ptr = GC_collect(gc_size,gc_finalizer);

		/* if allocation also fails after GC */

		if(gc_ptr == NULL)
		{
			/* return NULL */
			printf("\nYour Request cannot be satisfied even after Garbage Collection \n");
			return NULL;		
		}
	}	

	/* if allocation suceeds return the pointer to the new object */
	gcG_current_heap_size += gc_size;
	return gc_ptr;
}


