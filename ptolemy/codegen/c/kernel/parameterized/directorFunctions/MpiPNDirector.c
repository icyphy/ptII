/*** hasLocalInput() ***/
boolean hasLocalInput(struct mpiLocalBufferHeader* header) {
	return header->writeOffset != header->readOffset;
}
/**/

/*** isMpiBufferFull() ***/
boolean isMpiBufferFull(struct mpiBufferHeader* header, int space) {
	if ((header->available - header->current) < space) {
#ifdef _DEBUG	
		printf("mpiBuffer[%d] is full.\n", header->id);
#endif
		return true;
	}
	return false;
}
/**/

/*** isLocalBufferFull() ***/
boolean isLocalBufferFull(struct mpiLocalBufferHeader* header) {
	// FIXME: local buffer size (50) is hardcoded for now.
	int diff = header->writeOffset - header->readOffset;

	if (diff == -1 || (diff >= 50 - 1)) {
#ifdef _DEBUG
		printf("localBuffer[%d] is full.\n", header->id);
#endif
		return true;
	}
	return false;
}
/**/

/*** MPI_declareStruct() ***/
// MPI buffer header definitions.
struct mpiBufferHeader {
	unsigned int current;    	
	unsigned int available;    
	int id;
}; 

struct mpiLocalBufferHeader {    
	int readOffset;             // Next index to read.
	int writeOffset;            // Next index to write.
	int id;
}; 
/**/