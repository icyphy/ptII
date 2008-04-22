/*** preinitBlock($name, $numThreads, $numBuffers) ***/
    static pthread_cond_t $name_condition[$numBuffers];
    static pthread_mutex_t $name_conditionMutex[$numBuffers];
    
    static struct directorHeader $name = {
        // Only partial initialization.
        0,                  // writeBlockingThreads
        0,                  // readBlockingThreads
        $numThreads,   		// totalNumThreads
        false,              // terminate
        $numBuffers,		// numBuffers
        $name_condition,    // allConditions
        $name_conditionMutex// allConditionMutexes
                            // writeBlockMutex (init by pthread_mutex_init());
                            // readBlockMutex (init by pthread_mutex_init());
    };
/**/

/*** declareBufferHeader($name, $dirHeader, $capacity, $index) ***/
    static struct pnBufferHeader $name = {
        // Only partial initialization.
        0,                              		// writeCount
        0,                              		// readCount
        0,                              		// readOffset
        0,                              		// writeOffset
        $capacity,                      		// capacity
        PN_BUFFER_NO_BLOCKING,          		// pendingFlag
        &($dirHeader_condition[$index]), 		// waitCondition
        &($dirHeader_conditionMutex[$index])	// waitMutex
    };
/**/


/*** initBlock($directorHeader) ***/
    pthread_mutex_init(&$directorHeader.writeBlockMutex, NULL);
    pthread_mutex_init(&$directorHeader.readBlockMutex, NULL);
/**/

/*** initBuffer($buffer) ***/
    pthread_mutex_init($buffer.waitMutex, NULL);
    pthread_cond_init($buffer.waitCondition, NULL);
/**/


/*** wrapupBlock($directorHeader) ***/
    pthread_mutex_destroy(&$directorHeader.writeBlockMutex);
    pthread_mutex_destroy(&$directorHeader.readBlockMutex);
/**/

/*** destroyBuffer($buffer) ***/
    pthread_mutex_destroy($buffer.waitMutex);
    pthread_cond_destroy($buffer.waitCondition);
/**/

/*** updateInputOffset ***/
    incrementOffset();
/**/