/*** hasLocalInput() ***/
boolean hasLocalInput(struct mpiLocalBufferHeader* header) {
	return header->writeOffset != header->readOffset;
}
/**/

/*** isMpiBufferFull() ***/
boolean isMpiBufferFull(struct mpiBufferHeader* header, int space) {
	return (header->available - header->current) < space;
}
/**/

/*** isLocalBufferFull() ***/
boolean isLocalBufferFull(struct mpiLocalBufferHeader* header) {
	// FIXME: local buffer size (50) is hardcoded for now.
	int diff = header->writeOffset - header->readOffset;
	return diff == -1 || (diff >= 50 - 1);
}
/**/

/*** MPI_declareStruct() ***/
	// MPI buffer header definitions.
    struct mpiBufferHeader {
        unsigned int current;    	
        unsigned int available;     
    }; 

    struct mpiLocalBufferHeader {    
        int readOffset;             // Next index to read.
        int writeOffset;            // Next index to write.
    }; 
/**/

/*** getWriteOffset() ***/
// generate $declarePNStruct() && $incrementWriteBlockingThreads()
// The above comment is necessary in this code block
// to ensure the "declarePNStruct" code block is generated
// before this code block.

// Get the current write offset.
int getWriteOffset(struct pnBufferHeader* header, struct directorHeader* directorHeader) {

    if ((header->writeCount - header->readCount) >= header->capacity) {

        //-------------------------------------------------------------------//
        pthread_mutex_lock(header->waitMutex);
    
        // If it is full, then wait;
        if ((header->writeCount - header->readCount) >= header->capacity) {
    
            //printf("readBlock=%d, writeBlock=%d\n", directorHeader->readBlockingThreads, directorHeader->writeBlockingThreads + 1);
            //fflush(stdout);
    
            //directorHeader->writeBlockingThreads++;
            incrementWriteBlockingThreads(directorHeader);
    
            //pthread_mutex_lock(header->waitMutex);
            header->pendingFlag |= 0x2;
       
			#ifdef DEBUG_PN
				printf("[%x] sleeps on [%d].\n", pthread_self(), header->waitMutex);
			#endif
				
            pthread_cond_wait(header->waitCondition, header->waitMutex);

            #ifdef DEBUG_PN
				printf("[%x] wakes up.\n", pthread_self());
			#endif
			
            if (directorHeader->terminate) {
                //printf("terminate read thread\n");
                //fflush(stdout);
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
// generate $declarePNStruct() && $incrementWriteBlockingThreads()
// The above comment is necessary in this code block
// to ensure the "declarePNStruct" code block is generated
// before this code block.


// Get the current write offset.
int getAdvancedWriteOffset(int offset, struct pnBufferHeader* header, struct directorHeader* directorHeader) {

    if ((header->writeCount - header->readCount) >= header->capacity - offset) {

        //-------------------------------------------------------------------//
        pthread_mutex_lock(header->waitMutex);
    
        // If it is full, then wait;
        if ((header->writeCount - header->readCount) >= header->capacity - offset) {
    
            //printf("readBlock=%d, writeBlock=%d\n", directorHeader->readBlockingThreads, directorHeader->writeBlockingThreads + 1);
            //fflush(stdout);
    
            //directorHeader->writeBlockingThreads++;
            incrementWriteBlockingThreads(directorHeader);
    
            //pthread_mutex_lock(header->waitMutex);
            header->pendingFlag |= 0x2;

			#ifdef DEBUG_PN
			printf("[%x] sleeps on [%d].\n", pthread_self(), header->waitMutex);
			#endif

			// Since this request is for multiple buffer spaces, there
            // may not be enough even if the thread is waken up.
            do {
            	pthread_cond_wait(header->waitCondition, header->waitMutex);
            } while ((header->writeCount - header->readCount) >= header->capacity - offset && !directorHeader->terminate);

            #ifdef DEBUG_PN
				printf("[%x] wakes up.\n", pthread_self());
			#endif
            
            if (directorHeader->terminate) {
                //printf("terminate read thread\n");
                //fflush(stdout);
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
// generate $declarePNStruct() && $incrementReadBlockingThreads()
// The above comment is necessary in this code block
// to ensure the "declarePNStruct" code block is generated
// before this code block.

// Get the current read offset.
int getReadOffset(struct pnBufferHeader* header, struct directorHeader* directorHeader) {

    if ((header->writeCount - header->readCount) <= 0) {
        // If buffer is empty, then wait on the condition variable.
        //-------------------------------------------------------------------//
        pthread_mutex_lock(header->waitMutex);
        if ((header->writeCount - header->readCount) <= 0) {

            //printf("readBlock=%d, writeBlock=%d\n", directorHeader->readBlockingThreads + 1, directorHeader->writeBlockingThreads);
            //fflush(stdout);

            incrementReadBlockingThreads(directorHeader);

            //pthread_mutex_lock(header->waitMutex);
            // FIXME: use #define
            header->pendingFlag |= 0x1;

            #ifdef DEBUG_PN
			printf("[%x] sleeps on [%d].\n", pthread_self(), header->waitMutex);
			#endif
				
            pthread_cond_wait(header->waitCondition, header->waitMutex);
            
			#ifdef DEBUG_PN
				printf("[%x] wakes up.\n", pthread_self());
			#endif
            if (directorHeader->terminate) {
                //printf("terminate read thread\n");
                //fflush(stdout);

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
// generate $declarePNStruct() && $incrementReadBlockingThreads()
// The above comment is necessary in this code block
// to ensure the "declarePNStruct" code block is generated
// before this code block.


// Get the current read offset.
int getAdvancedReadOffset(int offset, struct pnBufferHeader* header, struct directorHeader* directorHeader) {

    if ((header->writeCount - header->readCount) <= offset) {
        // If buffer is empty, then wait on the condition variable.
        //-------------------------------------------------------------------//
        pthread_mutex_lock(header->waitMutex);
        if ((header->writeCount - header->readCount) <= offset) {

            //printf("readBlock=%d, writeBlock=%d\n", directorHeader->readBlockingThreads + 1, directorHeader->writeBlockingThreads);
            //fflush(stdout);

            incrementReadBlockingThreads(directorHeader);

            //pthread_mutex_lock(header->waitMutex);
            // FIXME: use #define
            header->pendingFlag |= 0x1;

			#ifdef DEBUG_PN
			printf("[%x] sleeps on [%d].\n", pthread_self(), header->waitMutex);
			#endif

			// Since this request is for multiple buffer spaces, there
            // may not be enough even if the thread is waken up.
            do {
            	pthread_cond_wait(header->waitCondition, header->waitMutex);
            } while ((header->writeCount - header->readCount) <= offset && !directorHeader->terminate);
            
			#ifdef DEBUG_PN
				printf("[%x] wakes up.\n", pthread_self());
			#endif
            if (directorHeader->terminate) {
                //printf("terminate read thread\n");
                //fflush(stdout);

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
