/*** preinitBlock($name, $numThreads, $numBuffers) ***/
	// generate $declarePNDirectorStruct()
	// The above comment is necessary in this code block
	// to ensure the "declarePNDirectorStruct" code block is generated
	// before this code block.
    static pthread_cond_t $name_condition[$numBuffers];
    static HANDLE $name_conditionMutex[$numBuffers];

    static struct directorHeader $name = {
        // Only partial initialization.
        0,                  // writeBlockingThreads
        0,                  // readBlockingThreads
        $numThreads,   		// totalNumThreads
        false,              // terminate
        $numBuffers,		// numBuffers
        $name_condition,    // allConditions
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
    &$directorHeader.writeBlockMutex = CreateMutex(NULL, FALSE, NULL);
    &$directorHeader.readBlockMutex = CreateMutex(NULL, FALSE, NULL);
/**/

/*** initBuffer($buffer) ***/
    $buffer.waitMutex = CreateMutex(NULL, FALSE, NULL);
    //pthread_cond_init($buffer.waitCondition, NULL);
/**/


/*** wrapupBlock($directorHeader) ***/
    CloseHandle(&$directorHeader.writeBlockMutex);
    CloseHandle(&$directorHeader.readBlockMutex);
/**/

/*** destroyBuffer($buffer) ***/
    CloseHandle($buffer.waitMutex);
    pthread_cond_destroy($buffer.waitCondition);
/**/

/*** updateInputOffset ***/
    incrementOffset();
/**/
