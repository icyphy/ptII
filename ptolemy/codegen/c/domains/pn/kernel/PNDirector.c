/*** preinitBlock($name, $numThreads, $numBuffers, $actorNameFile) ***/
        // generate $declarePNDirectorStruct()
        // The above comment is necessary in this code block
        // to ensure the "declarePNDirectorStruct" code block is generated
        // before this code block.
    static pthread_cond_t $name_condition[$numBuffers];
    static pthread_mutex_t $name_conditionMutex[$numBuffers];

    static struct directorHeader $name = {
        // Only partial initialization.
        0,                  // writeBlockingThreads
        0,                  // readBlockingThreads
        $numThreads,                   // totalNumThreads
        false,              // terminate
        $numBuffers,                // numBuffers
        $name_condition,    // allConditions
                            // writeBlockMutex (init by pthread_mutex_init());
                            // readBlockMutex (init by pthread_mutex_init());
    };

    #ifdef DEBUG_PN
    FILE *$actorNameFile;
        #endif
/**/

/*** declareBufferHeader($name, $dirHeader, $capacity, $index) ***/
// generate $declarePNStruct()
// The above comment is necessary in this code block
// to ensure the "declarePNStruct" code block is generated
// before this code block.
    static struct pnBufferHeader $name = {
        // Only partial initialization.
        0,                                              // writeCount
        0,                                              // readCount
        0,                                              // readOffset
        0,                                              // writeOffset
        $capacity,                                      // capacity
        PN_BUFFER_NO_BLOCKING,                          // pendingFlag
        &($dirHeader_condition[$index]),                 // waitCondition
        &($dirHeader_conditionMutex[$index])        // waitMutex
    };
/**/


/*** initBlock($directorHeader, $actorNameFile) ***/
    pthread_mutex_init(&$directorHeader.writeBlockMutex, NULL);
    pthread_mutex_init(&$directorHeader.readBlockMutex, NULL);

        #ifdef DEBUG_PN
    $actorNameFile = fopen("$actorNameFile.txt", "w");
        #endif
/**/

/*** initBuffer($buffer) ***/
    pthread_mutex_init($buffer.waitMutex, NULL);
    pthread_cond_init($buffer.waitCondition, NULL);
/**/


/*** wrapupBlock($directorHeader, $actorNameFile) ***/
    pthread_mutex_destroy(&$directorHeader.writeBlockMutex);
    pthread_mutex_destroy(&$directorHeader.readBlockMutex);

        #ifdef DEBUG_PN
        fclose($actorNameFile);
        #endif
/**/

/*** destroyBuffer($buffer) ***/
    pthread_mutex_destroy($buffer.waitMutex);
    pthread_cond_destroy($buffer.waitCondition);
/**/

/*** updateInputOffset ***/
    incrementOffset();
/**/


/*** printThreadName($actorNameFile, $name) ***/
        #ifdef DEBUG_PN
        fprintf($actorNameFile, "$name,%x\n", pthread_self());
        fflush($actorNameFile);
        #endif
/**/
