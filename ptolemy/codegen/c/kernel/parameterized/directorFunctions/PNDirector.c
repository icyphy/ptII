/*** declarePNDirectorStruct() ***/
    pthread_attr_t pthread_custom_attr;

    struct directorHeader {
        //-------------------------------------------------------------------//
        int writeBlockingThreads;
        int readBlockingThreads;

        // FIXME: we can determine this statically, and don't need to store it.
        int totalNumThreads;
        //-------------------------------------------------------------------//
        boolean terminate;

        // FIXME: we can determine this statically, and don't need to store it.
        int numBuffers;

        // Array of the condition variables of buffers
        // controlled by this director (needed to handle real deadlock).
        pthread_cond_t* allConditions;

        // FIXME: We can also use only one mutex to guard both variables.
        pthread_mutex_t writeBlockMutex;
        pthread_mutex_t readBlockMutex;
    };
/**/

/*** declarePNStruct() ***/
    #define PN_BUFFER_NO_BLOCKING 0x0
    #define PN_BUFFER_PENDING_READ 0x1
    #define PN_BUFFER_PENDING_WRITE 0x2
    #define PN_BUFFER_CLEAR_WRITE 0xfffffffd
    #define PN_BUFFER_CLEAR_READ 0xfffffffe

    struct pnBufferHeader {
        // FIXME: how to statically assign values in struct?
        // FIXME: how to instantiate struct with different initial values?

        // Using a separate write count and read count,
        // we can avoid using any semaphores. To determine
        // the number of items in the buffer, we simply
        // do (writeCount - readCount). Unsigned is necessary
        // for working with wrap-around.
        unsigned int writeCount;    // Number of items written.
        unsigned int readCount;     // Number of items retrieved.

        int readOffset;             // Next index to read.
        int writeOffset;            // Next index to write.
        int capacity;               // Capacity of the buffer

        //-------------------------------------------------------------------//
        // PN_BUFFER_NO_BLOCKING        (no waiting threads),
        // PN_BUFFER_PENDING_READ       (pending read),
        // PN_BUFFER_PENDING_WRITE      (pending write).
        unsigned char pendingFlag;

        // Synchronization variables.
        pthread_cond_t* waitCondition;  // Condition variable for a buffer.
        pthread_mutex_t* waitMutex;      // Mutex used by the condition variable.
        //-------------------------------------------------------------------//
    };
/**/

/*** getWriteOffset() ***/
// generate $declarePNDirectorStruct() && $declarePNStruct() && $incrementWriteBlockingThreads()
// The above comment is necessary in this code block
// to ensure the "declarePNStruct" code block is generated
// before this code block.

// Get the current write offset.
int getWriteOffset(struct pnBufferHeader* header, struct directorHeader* directorHeader) {

    if ((signed int)(header->writeCount - header->readCount) >= header->capacity) {

        //-------------------------------------------------------------------//
        pthread_mutex_lock(header->waitMutex);

        // If it is full, then wait;
        if ((signed int)(header->writeCount - header->readCount) >= header->capacity) {

            //directorHeader->writeBlockingThreads++;
            incrementWriteBlockingThreads(directorHeader);

            //pthread_mutex_lock(header->waitMutex);
            header->pendingFlag |= 0x2;

                        #ifdef DEBUG_PN
                        printf("[%x] waits to write [%d].\n", pthread_self(), header->waitCondition);
                        fflush(stdout);
                        #endif

            pthread_cond_wait(header->waitCondition, header->waitMutex);

            #ifdef DEBUG_PN
                        printf("[%x] wakes up.\n", pthread_self());
                        fflush(stdout);
                        #endif

            if (directorHeader->terminate) {
                pthread_exit(NULL);
            }

        }
        pthread_mutex_unlock(header->waitMutex);
        //-------------------------------------------------------------------//
    }

    // Cannot signal the waiting thread here
    // because the data is not on the buffer yet.

    return header->writeOffset;
}
/**/

/*** getAdvancedWriteOffset() ***/
// generate $declarePNDirectorStruct() && $declarePNStruct() && $incrementWriteBlockingThreads()
// The above comment is necessary in this code block
// to ensure the "declarePNStruct" code block is generated
// before this code block.


// Get the current write offset.
int getAdvancedWriteOffset(int offset, struct pnBufferHeader* header, struct directorHeader* directorHeader) {

    if ((signed int)(header->writeCount - header->readCount) >= header->capacity - offset) {

        //-------------------------------------------------------------------//
        pthread_mutex_lock(header->waitMutex);

        // If it is full, then wait;
        if ((signed int)(header->writeCount - header->readCount) >= header->capacity - offset) {

            //directorHeader->writeBlockingThreads++;
            incrementWriteBlockingThreads(directorHeader);

            //pthread_mutex_lock(header->waitMutex);
            header->pendingFlag |= 0x2;

                        #ifdef DEBUG_PN
                        printf("[%x] waits to write [%d].\n", pthread_self(), header->waitCondition);
                        fflush(stdout);
                        #endif

                        // Since this request is for multiple buffer spaces, there
            // may not be enough even if the thread is waken up.
            do {
                    pthread_cond_wait(header->waitCondition, header->waitMutex);
            } while ((header->writeCount - header->readCount) >= header->capacity - offset && !directorHeader->terminate);

            #ifdef DEBUG_PN
                        printf("[%x] wakes up.\n", pthread_self());
                        fflush(stdout);
                        #endif

            if (directorHeader->terminate) {
                pthread_exit(NULL);
            }
        }
        pthread_mutex_unlock(header->waitMutex);
        //-------------------------------------------------------------------//
    }

    // Cannot signal the waiting thread here
    // because the data is not on the buffer yet.

    return (header->writeOffset + offset) % header->capacity;
}
/**/


/*** getReadOffset() ***/
// generate $declarePNDirectorStruct() && $declarePNStruct() && $incrementReadBlockingThreads()
// The above comment is necessary in this code block
// to ensure the "declarePNStruct" code block is generated
// before this code block.

// Get the current read offset.
int getReadOffset(struct pnBufferHeader* header, struct directorHeader* directorHeader) {

    if ((signed int)(header->writeCount - header->readCount) <= 0) {
        // If buffer is empty, then wait on the condition variable.
        //-------------------------------------------------------------------//
        pthread_mutex_lock(header->waitMutex);
        if ((signed int)(header->writeCount - header->readCount) <= 0) {

            incrementReadBlockingThreads(directorHeader);

            // FIXME: use #define
            header->pendingFlag |= 0x1;

                        #ifdef DEBUG_PN
                        printf("[%x] waits to read [%d].\n", pthread_self(), header->waitCondition);
                        fflush(stdout);
                        #endif

            pthread_cond_wait(header->waitCondition, header->waitMutex);

                        #ifdef DEBUG_PN
                        printf("[%x] wakes up.\n", pthread_self());
                        fflush(stdout);
                        #endif
            if (directorHeader->terminate) {
                // FIXME: need to call actor's wrapup()
                pthread_exit(NULL);
            }
        }
        pthread_mutex_unlock(header->waitMutex);
        //-------------------------------------------------------------------//
    }

    // Cannot signal the waiting thread here
    // because the data is not on the buffer yet.

    return header->readOffset;
}
/**/



/*** getAdvancedReadOffset() ***/
// generate $declarePNDirectorStruct() && $declarePNStruct() && $incrementReadBlockingThreads()
// The above comment is necessary in this code block
// to ensure the "declarePNStruct" code block is generated
// before this code block.

// Get the current read offset.
int getAdvancedReadOffset(int offset, struct pnBufferHeader* header, struct directorHeader* directorHeader) {

    if ((signed int)(header->writeCount - header->readCount) <= offset) {
        // If buffer is empty, then wait on the condition variable.
        //-------------------------------------------------------------------//
        pthread_mutex_lock(header->waitMutex);
        if ((signed int)(header->writeCount - header->readCount) <= offset) {

            incrementReadBlockingThreads(directorHeader);

            // FIXME: use #define
            header->pendingFlag |= 0x1;

                        #ifdef DEBUG_PN
                        printf("[%x] waits to read [%d].\n", pthread_self(), header->waitCondition);
                        fflush(stdout);
                        #endif

                        // Since this request is for multiple buffer spaces, there
            // may not be enough even if the thread is waken up.
            do {
                    pthread_cond_wait(header->waitCondition, header->waitMutex);
            } while ((header->writeCount - header->readCount) <= offset && !directorHeader->terminate);

                        #ifdef DEBUG_PN
                        printf("[%x] wakes up.\n", pthread_self());
                        fflush(stdout);
                        #endif
            if (directorHeader->terminate) {

                // FIXME: need to call actor's wrapup()
                pthread_exit(NULL);
            }

        }
        pthread_mutex_unlock(header->waitMutex);
        //-------------------------------------------------------------------//
    }

    // Cannot signal the waiting thread here
    // because the data is not on the buffer yet.

    return (header->readOffset + offset) % header->capacity;
}
/**/


/*** incrementReadBlockingThreads ***/
// generate $declarePNDirectorStruct()
// The above comment is necessary in this code block
// to ensure the "declarePNDirectorStruct" code block is generated
// before this code block.
void incrementReadBlockingThreads(struct directorHeader* directorHeader) {
    int i;
    pthread_mutex_lock(&directorHeader->readBlockMutex);
    directorHeader->readBlockingThreads++;

    #ifdef DEBUG_PN
            //printf("[%x] Read Block++ rbt[%d], wbt[%d]\n", pthread_self(), directorHeader->readBlockingThreads, directorHeader->writeBlockingThreads);
        #endif

    pthread_mutex_unlock(&directorHeader->readBlockMutex);

    if (directorHeader->readBlockingThreads == directorHeader->totalNumThreads) {
            // Real deadlock;
        directorHeader->terminate = true;

        for (i = 0; i < directorHeader->numBuffers; i++) {
                        #ifdef DEBUG_PN
                        printf("[%x] broadcast [%d].\n", pthread_self(), &directorHeader->allConditions[i]);
                        fflush(stdout);
                        #endif
            pthread_cond_broadcast(&directorHeader->allConditions[i]);
        }
        pthread_exit(NULL);
    }
    // else if (directorHeader->readBlockingThreads + directorHeader->readBlockingThreads == directorHeader->totalNumThreads) {
            // FIXME: handle artificial deadlock.
        // write deadlock;
    //}
}
/**/

/*** incrementWriteBlockingThreads ***/
// generate $declarePNDirectorStruct()
// The above comment is necessary in this code block
// to ensure the "declarePNDirectorStruct" code block is generated
// before this code block.
void incrementWriteBlockingThreads(struct directorHeader* directorHeader) {
    pthread_mutex_lock(&directorHeader->writeBlockMutex);
    directorHeader->writeBlockingThreads++;
        #ifdef DEBUG_PN
                //printf("[%x] Write Block++ rbt[%d], wbt[%d]\n", pthread_self(), directorHeader->readBlockingThreads, directorHeader->writeBlockingThreads);
        #endif
    pthread_mutex_unlock(&directorHeader->writeBlockMutex);

    /*
    if (directorHeader->readBlockingThreads + directorHeader->readBlockingThreads == directorHeader->totalNumThreads) {
        // write deadlock;
    }
    */
}
/**/

/*** incrementWriteOffset ***/
// generate $declarePNDirectorStruct() && $declarePNStruct()
// The above comment is necessary in this code block
// to ensure the "declarePNStruct" code block is generated
// before this code block.

// Increment the write offset by 1.
void incrementWriteOffset(struct pnBufferHeader* header, struct directorHeader* directorHeader) {
        #ifdef DEBUG_PN
        printf("[%x] increments [%d] write offset [%d]->[%d]\n", pthread_self(), header->waitCondition, header->writeOffset, (header->writeOffset + 1) % header->capacity);
        fflush(stdout);
        #endif

    header->writeOffset = (header->writeOffset + 1) % header->capacity;
    header->writeCount++;

    //-------------------------------------------------------------------//
    // FIXME: Have to signal in where we wait; otherwise, we'll
    // have a deadlock.

    pthread_mutex_lock(header->waitMutex);

    if (header->pendingFlag != PN_BUFFER_NO_BLOCKING) {

        header->pendingFlag &= PN_BUFFER_CLEAR_READ;  // Clear the pending read.

        pthread_mutex_lock(&directorHeader->readBlockMutex);
        directorHeader->readBlockingThreads--;
                #ifdef DEBUG_PN
                        //printf("[%x] Read Block-- rbt[%d], wbt[%d]\n", pthread_self(), directorHeader->readBlockingThreads, directorHeader->writeBlockingThreads);
                #endif
        pthread_mutex_unlock(&directorHeader->readBlockMutex);

                #ifdef DEBUG_PN
                printf("[%x] signals [%d]\n", pthread_self(), header->waitCondition);
                fflush(stdout);
                #endif

            // Signal the consumer thread.
            pthread_cond_signal(header->waitCondition);
    }

    pthread_mutex_unlock(header->waitMutex);
    //-------------------------------------------------------------------//
}
/**/

/*** incrementWriteOffsetBy ***/
// generate $declarePNDirectorStruct() && $declarePNStruct()
// The above comment is necessary in this code block
// to ensure the "declarePNStruct" code block is generated
// before this code block.

// Increment the write offset by a specified increment.
void incrementWriteOffsetBy(int increment, struct pnBufferHeader* header, struct directorHeader* directorHeader) {
        #ifdef DEBUG_PN
        printf("[%x] increments [%d] write offset [%d]->[%d]\n", pthread_self(), header->waitCondition, header->writeOffset, (header->writeOffset + increment) % header->capacity);
        fflush(stdout);
        #endif

        header->writeOffset = (header->writeOffset + increment) % header->capacity;
    header->writeCount += increment;

    //-------------------------------------------------------------------//
    // FIXME: Have to signal in where we wait; otherwise, we'll
    // have a deadlock.

    pthread_mutex_lock(header->waitMutex);

    if (header->pendingFlag != PN_BUFFER_NO_BLOCKING) {

        header->pendingFlag &= PN_BUFFER_CLEAR_READ;  // Clear the pending read.

        pthread_mutex_lock(&directorHeader->readBlockMutex);
        directorHeader->readBlockingThreads--;
        pthread_mutex_unlock(&directorHeader->readBlockMutex);

                #ifdef DEBUG_PN
                printf("[%x] signals [%d]\n", pthread_self(), header->waitCondition);
                fflush(stdout);
                #endif

        // Signal the consumer thread.
        pthread_cond_signal(header->waitCondition);
    }

    pthread_mutex_unlock(header->waitMutex);
    //-------------------------------------------------------------------//
}
/**/

/*** incrementReadOffset ***/
// generate $declarePNDirectorStruct() && $declarePNStruct()
// The above comment is necessary in this code block
// to ensure the "declarePNStruct" code block is generated
// before this code block.

// Increment the read offset by 1.
void incrementReadOffset(struct pnBufferHeader* header, struct directorHeader* directorHeader) {
        #ifdef DEBUG_PN
        printf("[%x] increments [%d] read offset [%d]->[%d]\n", pthread_self(), header->waitCondition, header->readOffset, (header->readOffset + 1) % header->capacity);
        fflush(stdout);
        #endif

    header->readOffset = (header->readOffset + 1) % header->capacity;
    header->readCount++;

    //-------------------------------------------------------------------//
    // FIXME: Have to signal in where we wait; otherwise, we'll
    // have a deadlock.

    pthread_mutex_lock(header->waitMutex);

    if (header->pendingFlag != PN_BUFFER_NO_BLOCKING) {

        header->pendingFlag &= PN_BUFFER_CLEAR_WRITE;  // Clear the pending write.

        pthread_mutex_lock(&directorHeader->writeBlockMutex);
        directorHeader->writeBlockingThreads--;
                #ifdef DEBUG_PN
                        //printf("[%x] Write Block-- rbt[%d], wbt[%d]\n", pthread_self(), directorHeader->readBlockingThreads, directorHeader->writeBlockingThreads);
                #endif
        pthread_mutex_unlock(&directorHeader->writeBlockMutex);

                #ifdef DEBUG_PN
                printf("[%x] signals [%d]\n", pthread_self(), header->waitCondition);
                fflush(stdout);
                #endif

        // Signal the consumer thread.
        pthread_cond_signal(header->waitCondition);
    }

    pthread_mutex_unlock(header->waitMutex);
    //-------------------------------------------------------------------//
}
/**/

/*** incrementReadOffsetBy ***/
// generate $declarePNDirectorStruct() && $declarePNStruct()
// The above comment is necessary in this code block
// to ensure the "declarePNStruct" code block is generated
// before this code block.

// Increment the read offset by a specified increment.
void incrementReadOffsetBy(int increment, struct pnBufferHeader* header, struct directorHeader* directorHeader) {
        #ifdef DEBUG_PN
        printf("[%x] increment [%d] read offset [%d]->[%d]\n", pthread_self(), header->waitCondition, header->readOffset, (header->readOffset + increment) % header->capacity);
        fflush(stdout);
        #endif

    header->readOffset = (header->readOffset + increment) % header->capacity;
    header->readCount += increment;

    //-------------------------------------------------------------------//
    // FIXME: Have to signal in where we wait; otherwise, we'll
    // have a deadlock.

    pthread_mutex_lock(header->waitMutex);

    if (header->pendingFlag != PN_BUFFER_NO_BLOCKING) {

        header->pendingFlag &= PN_BUFFER_CLEAR_WRITE;  // Clear the pending write.

        pthread_mutex_lock(&directorHeader->writeBlockMutex);
        directorHeader->writeBlockingThreads--;
        pthread_mutex_unlock(&directorHeader->writeBlockMutex);

                #ifdef DEBUG_PN
                printf("[%x] signals [%d]\n", pthread_self(), header->waitCondition);
                fflush(stdout);
                #endif

        // Signal the consumer thread.
        pthread_cond_signal(header->waitCondition);
    }

    pthread_mutex_unlock(header->waitMutex);
    //-------------------------------------------------------------------//
}
/**/

