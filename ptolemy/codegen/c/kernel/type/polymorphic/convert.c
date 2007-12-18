/*** convert_Array_Array() ***/
#define convert_Array_Array(a) a
/**/

/*** convert_Boolean_Boolean() ***/
#define convert_Boolean_Boolean(a) a
/**/

/*** convert_Boolean_Int() ***/
inline int convert_Boolean_Int(boolean a) {
    return a ? 1 : 0;
}
/**/

/*** convert_Boolean_String() ***/
char* convert_Boolean_String(boolean a) {
    char *results;
    if (b) {
        // AVR does not have strdup
        results = (char*) malloc(sizeof(char) * 5);
        strcpy(results, "true");
    } else {
        results = (char*) malloc(sizeof(char) * 6);
        strcpy(results, "false");
    }
    return results;
}
/**/

/*** convert_Double_Array() ***/
inline Token convert_Double_Array(double a) {
    return $new(Array(1, 1, $new(Double(a))));
}
/**/

/*** convert_Double_Double() ***/
inline double convert_Double_Double(double a) {
    return a;
}
/**/

/*** convert_Double_Int() ***/
inline int convert_Double_Int(double a) {
    return (int) a;
}
/**/

/*** convert_Double_String() ***/
char* convert_Double_String(double a) {
    int index;
    char* string = (char*) malloc(sizeof(char) * 20);
    sprintf(string, "%.14g", a);

    // Make sure that there is a decimal point.
    if (strrchr(string, '.') == NULL) {
        index = strlen(string);
        if (index == 20) {
            string = (char*) realloc(string, sizeof(char) * 22);
        }
        string[index] = '.';
        string[index + 1] = '0';
        string[index + 2] = '\0';
    }
    return string;
}
/**/

/*** convert_Double_Token() ***/
inline Token convert_Double_Token(double a) {
    return $new(Double(a));
}
/**/

/*** convert_Int_Array() ***/
Token convert_Int_Array(int a) {
    return $new(Array(1, 1, $new(Int(a))));
}
/**/

/*** convert_Int_Boolean() ***/
inline boolean convert_Int_Boolean(int a) {
    return (a != 0) ? true : false;
}
/**/

/*** convert_Int_Double() ***/
inline double convert_Int_Double(int a) {
    return (double) a;
}
/**/

/*** convert_Int_Int() ***/
#define convert_Int_Int(a) a
/**/

/*** convert_Int_String() ***/
char* convert_Int_String(int a) {
    char* string = (char*) malloc(sizeof(char) * 12);
    sprintf((char*) string, "%d", a);
    return string;
}
/**/

/*** convert_Int_Token() ***/
inline Token convert_Int_Token(int a) {
    return $new(Int(a));
}
/**/

/*** convert_Long_Array() ***/
Token convert_Long_Array(long long a) {
    return $new(Array(1, 1, $new(Long(a))));
}
/**/

/*** convert_Long_Long() ***/
#define convert_Long_Long(a) a
/**/

/*** convert_Long_Token() ***/
inline Token convert_Long_Token(long long a) {
    return $new(Long(a));
}
/**/

/*** convert_String_Boolean() ***/
char* convert_String_Boolean(char* a) {

}
/**/

/*** convert_String_Double() ***/
#define convert_String_Double atof
/**/

/*** convert_String_Int() ***/
#define convert_String_Int atoi
/**/

/*** convert_String_String() ***/
#define convert_String_String(a) a
/**/

/*** convert_Token_Token() ***/
#define convert_Token_Token(a) a
/**/

