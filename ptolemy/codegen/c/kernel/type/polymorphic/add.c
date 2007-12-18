/*** add_Array_Array() ***/
inline Token add_Array_Array(Token a1, Token a2) {
    return $Array_add(a1, a2);
}
/**/

/*** add_Array_Double() ***/
inline Token add_Array_Double(Token a1, double a2) {
    return $add_Double_Array(a2, a1);
}
/**/

/*** add_Array_Int() ***/
inline Token add_Int_Array(Token a1, int a2) {
    return $add_Array_Int(a2, a1);
}
/**/

/*** add_Array_Long() ***/
inline Token add_Long_Array(Token a1, long long a2) {
    return $add_Array_Long(a2, a1);
}
/**/

/*** add_Boolean_Boolean() ***/
inline boolean add_Boolean_Boolean(boolean a1, boolean a2) {
    return a1 | a2;
}
/**/

/*** add_Boolean_Int() ***/
inline int add_Boolean_Int(boolean a1, int a2) {
    return $add_Int_Boolean(a2, a1);
}
/**/

/*** add_Boolean_String() ***/
inline char* add_Boolean_String(boolean a1, char* a2) {
    return $add_String_Boolean(a2, a1);
}
/**/

/*** add_Double_Array() ***/
Token add_Double_Array(double a1, Token a2) {
    int i;
    Token result = $new(Array(a2.payload.Array->size, 0));

    for (i = 0; i < a2.payload.Array->size; i++) {
        Array_set(result, i, $add_Double_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** add_Double_Double() ***/
inline double add_Double_Double(double a1, double a2) {
    return a1 + a2;
}
/**/

/*** add_Double_String() ***/
inline char* add_Double_String(double a1, char* a2) {
    return $add_String_Double(a2, a1);
}
/**/

/*** add_Double_Token() ***/
Token add_Double_Token(double a1, Token a2) {
    Token token = $new(Double(a1));
    return $add_Token_Token(token, a2);
}
/**/

/*** add_Int_Array() ***/
Token add_Int_Array(int a1, Token a2) {
    int i;
    Token result = $new(Array(a2.payload.Array->size, 0));

    for (i = 0; i < a2.payload.Array->size; i++) {
        Array_set(result, i, $add_Int_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** add_Int_Boolean() ***/
int add_Int_Boolean(int a1, boolean a2) {
    return a1 + (a2 ? 1 : 0);
}
/**/

/*** add_Int_Int() ***/
inline int add_Int_Int(int a1, int a2) {
    return a1 + a2;
}
/**/

/*** add_Int_String() ***/
char* add_Int_String(int a1, char* a2) {
    char* string = (char*) malloc(sizeof(char) * (12 + strlen(a2)));
    sprintf((char*) string, "%d%s", a1, a2);
    return string;
}
/**/

/*** add_Int_Token() ***/
int add_Int_Token(int a1, Token a2) {
    Token token = $new(Int, a1);
    return $typeFunc(TYPE_Int::add(token, a2));
}
/**/

/*** add_Long_Array() ***/
Token add_Long_Array(long long a1, Token a2) {
    int i;
    Token result = $new(Array(a2.payload.Array->size, 0));

    for (i = 0; i < a2.payload.Array->size; i++) {
        Array_set(result, i, $add_Long_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** add_Long_Long() ***/
inline long long add_Long_Long(long long a1, long long a2) {
    return a1 + a2;
}
/**/

/*** add_Long_Token() ***/
Token add_Long_Token(long long a1, Token a2) {
    Token token = $new(Long(a1));
    return $add_Token_Token(token, a2);
}
/**/

/*** add_String_Boolean() ***/
char* add_String_Boolean(char* a1, boolean a2) {
    char* result = (char*) malloc(sizeof(char) * ((a2 ? 5 : 6) + strlen(a1)));
    strcpy(result, a1);
    strcat(result, (a2 ? "true" : "false");
    return result;
}
/**/

/*** add_String_Double() ***/
char* add_String_Double(char* a1, double a2) {
    char* string = (char*) malloc(sizeof(char) * (20 + strlen(a1)));
    sprintf((char*) string, "%s%g", a1, a2);
    return string;
}
/**/

/*** add_String_Int() ***/
inline char* add_String_Int(char* a1, int a2) {
    return $add_Int_String(a2, a1);
}
/**/

/*** add_String_String() ***/
char* add_String_String(char* a1, char* a2) {
    char* result = (char*) malloc(sizeof(char) * (1 + strlen(a1) + strlen(a2)));
    strcpy(result, a1);
    strcat(result, a2);
    return result;
}
/**/

/*** add_Token_Double() ***/
inline Token add_Token_Double(Token a1, int a2) {
    return $add_Double_Token(a2, a1);
}
/**/

/*** add_Token_Int() ***/
inline int add_Token_Int(Token a1, int a2) {
    return $add_Int_Token(a2, a1);
}
/**/

/*** add_Token_Token() ***/
inline Token add_Token_Token(Token a1, Token a2) {
    return $tokenFunc(a1::add(a2));
}
/**/

