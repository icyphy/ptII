#ifndef _PLATFORM_HOOKS_H
#define _PLATFORM_HOOKS_H

// Methods declared here must be implemented by
// each platform.

#include "types.h"
#include "classes.h"
#include "language.h"

/**
 * Called when thread is about to die due to an uncaught exception.
 */
extern void handle_uncaught_exception (Object *exception,
                                       const Thread *thread,
				       const MethodRecord *methodRecord,
				       const MethodRecord *rootMethod,
				       byte *pc);
				       
/**
 * Called when the current thread is switched.
 * Will be called continuously if all threads
 * are waiting or sleeping. 
 */
extern void switch_thread_hook();

/**
 * Dispatches a native method.
 */
extern void dispatch_native (TWOBYTES signature, STACKWORD *paramBase);				      

#endif _PLATFORM_HOOKS_H
