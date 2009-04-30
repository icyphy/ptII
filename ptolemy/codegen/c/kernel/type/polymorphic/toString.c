/*** toString_Array() ***/
char* toString_Array(Token thisToken) {
    int i;
    int currentSize, allocatedSize;
    char* string;
    Token elementString;

    allocatedSize = 256;
    string = (char*) malloc(allocatedSize);
    string[0] = '{';
    string[1] = '\0';

    // Space for '{', '}', and '\0' characters.
    currentSize = 3;

    for (i = 0; i < thisToken.payload.Array->size; i++) {

            // Calculate the require storage size.
            elementString = functionTable[(int)thisToken.payload.Array->elements[i].type][FUNC_toString](thisToken.payload.Array->elements[i]);
            currentSize += strlen(elementString.payload.String);
            if (i != 0) {
                    currentSize += 2;
            }

            // Re-allocate storage.
            if (currentSize > allocatedSize) {
                    allocatedSize *= 2;
                    string = (char*) realloc(string, allocatedSize);
            }

            // Concat the element strings and separators.
            if (i != 0) {
                    strcat(string, ", ");
            }
            strcat(string, elementString.payload.String);
            free(elementString.payload.String);
    }

    strcat(string, "}");
    return string;
}
/**/

/*** toString_Boolean() ***/
char* toString_Boolean(boolean a) {
	return BooleantoString(a);
}
/**/

/*** toString_BooleanArray() ***/
char* toString_BooleanArray(Token thisToken) {
	int i;
    int currentSize, allocatedSize;
    char* string;
    char elementString[6];
    allocatedSize = 256;
    string = (char*) malloc(allocatedSize);
    string[0] = '{';
    string[1] = '\0';

    // Space for '{', '}', and '\0' characters.
    currentSize = 3;

    //printf("%d\n", thisToken.payload.BooleanArray->size);
    for (i = 0; i < thisToken.payload.BooleanArray->size; i++) {
		// Calculate the require storage size.

    	// boolean temp = BooleanArray_get(thisToken, i);
        sprintf(elementString, "%s", BooleantoString(BooleanArray_get(thisToken, i)));
        currentSize += strlen(elementString);
		if (i != 0) {
			currentSize += 2;
		}

		// Re-allocate storage.
		if (currentSize > allocatedSize) {
            allocatedSize *= 2;
            string = (char*) realloc(string, allocatedSize);
        }

		// Concat the element strings and separators.
		if (i != 0) {
            strcat(string, ", ");
        }
        strcat(string, elementString);
    }

    strcat(string, "}");
    return string;
}
/**/

/*** toString_Double() ***/
char* toString_Double(double a) {
	return DoubletoString(a);
}
/**/

/*** toString_DoubleArray() ***/
char* toString_DoubleArray(Token thisToken) {
	int i;
    int currentSize, allocatedSize;
    char* string;
    char* elementString;
    allocatedSize = 256;
    string = (char*) malloc(allocatedSize);
    string[0] = '{';
    string[1] = '\0';

    // Space for '{', '}', and '\0' characters.
    currentSize = 3;

    //printf("%d\n", thisToken.payload.DoubleArray->size);
    for (i = 0; i < thisToken.payload.DoubleArray->size; i++) {
		// Calculate the require storage size.

    	// double temp = DoubleArray_get(thisToken, i);
        elementString = $toString_Double(DoubleArray_get(thisToken, i));
        currentSize += strlen(elementString);
		if (i != 0) {
			currentSize += 2;
		}

		// Re-allocate storage.
		if (currentSize > allocatedSize) {
            allocatedSize *= 2;
            string = (char*) realloc(string, allocatedSize);
        }

		// Concat the element strings and separators.
		if (i != 0) {
            strcat(string, ", ");
        }
        strcat(string, elementString);
    }

    strcat(string, "}");
    return string;
}
/**/

/*** toString_Int() ***/
char* toString_Int(int a) {
	return InttoString(a);
}
/**/

/*** toString_IntArray() ***/
char* toString_IntArray(Token a) {
	return $IntArray_toString(a);
}
/**/

/*** toString_Long() ***/
char* toString_Long(long long a) {
	return LongtoString(a);
}
/**/

/*** toString_String() ***/
char* toString_String(char* a) {
	return (a);
}
/**/

/*** toString_StringArray() ***/
char* toString_StringArray(Token thisToken) {
	int i;
    int currentSize, allocatedSize;

    char* string;
    char* elementString;
    allocatedSize = 256;
    string = (char*) malloc(allocatedSize);
    string[0] = '{';
    // Pos[1] is for the open quote "\"".
    string[2] = '\0';

    // Since '{' is already printed, we initial this to pos 1.
    int charIndex = 1;

    // Space for '{', '}', and '\0' characters.
    currentSize = 3;

    //printf("%d\n", thisToken.payload.StringArray->size);
    for (i = 0; i < thisToken.payload.StringArray->size; i++) {
		// Calculate the require storage size.

    	// string temp = StringArray_get(thisToken, i);
        elementString = StringArray_get(thisToken, i);

        // make space also for the quotes "\"" characters.
        currentSize += strlen(elementString) + 2;
		if (i != 0) {
			currentSize += 2;
		}

		// Re-allocate storage.
		if (currentSize > allocatedSize) {
            allocatedSize *= 2;
            string = (char*) realloc(string, allocatedSize);
        }

		// Concat the element strings and separators.
		if (i != 0) {
            string[charIndex++] = ',';
            string[charIndex++] = ' ';
        }
        string[charIndex++] = '\"';
        strcat(string, elementString);
        charIndex += strlen(elementString);
        string[charIndex++] = '\"';
    }
    strcat(string, "}");
    return string;
}
/**/

/*** toString_Token() ***/
char* toString_Token(Token a) {
	return $tokenFunc(a::toString()).payload.String;
}
/**/

