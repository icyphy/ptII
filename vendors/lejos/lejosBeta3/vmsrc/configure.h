
#ifndef _CONFIGURE_H
#define _CONFIGURE_H

/**
 * Maximum level of recursion.
 */
#define MAX_STACK_FRAMES                 10
 
/**
 * Maximum number of words in a thread's stack
 * (for both locals and operands).
 */
#define STACK_SIZE                       70

/**
 * Should always be 1.
 */
#define STACK_CHECKING                   1

/**
 * Should always be 1.
 */
#define ARRAY_CHECKING                   1

/**
 * Iff not 0, threads in the DEAD state are
 * removed from the circular list. Recommended.
 */
#define REMOVE_DEAD_THREADS              1

/**
 * Slightly safer code (?)
 */
#define SAFE                             1

#endif







