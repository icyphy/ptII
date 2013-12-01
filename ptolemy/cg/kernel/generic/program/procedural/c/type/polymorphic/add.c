/*** add_Array_Array() ***/
Token* add_Array_Array(Token *a1, Token *a2) {
    return $Array_add(a1, a2);
}
/**/

/*** add_Array_Double() ***/
Token* add_Array_Double(Token *a1, double a2) {
    return $add_Double_Array(a2, a1);
}
/**/

/*** add_Array_Int() ***/
Token* add_Array_Int(Token *a1, int a2) {
    return $add_Int_Array(a2, a1);
}
/**/

/*** add_Array_Long() ***/
Token* add_Array_Long(Token *a1, long long a2) {
    return $add_Long_Array(a2, a1);
}
/**/

/*** add_Array_Scalar() ***/
Token* add_Array_Scalar(Token *a1, Scalar a2) {
    return $add_Scalar_Array(a2, a1);
}
/**/

/*** add_BooleanArray_BooleanArray() ***/
Token* add_BooleanArray_BooleanArray(Token *a1, Token *a2) {
    return $BooleanArray_add(a1, a2);
}
/**/

/*** add_Boolean_Boolean() ***/
boolean add_Boolean_Boolean(boolean a1, boolean a2) {
    return a1 | a2;
}
/**/

/*** add_Boolean_Int() ***/
int add_Boolean_Int(boolean a1, int a2) {
    return $add_Int_Boolean(a2, a1);
}
/**/

/*** add_Boolean_String() ***/
char* add_Boolean_String(boolean a1, char* a2) {
    char* result = (char*) malloc(sizeof(char) * ((a1 ? 5 : 6) + strlen(a2)));
    strcpy(result, a2);
    strcat(result, (a1 ? "true" : "false"));
    return result;
}
/**/

/*** add_DoubleArray_Double() ***/
Token* add_DoubleArray_Double(Token *a1, double a2) {
    return $add_Double_DoubleArray(a2, a1);
}
/**/

/*** add_DoubleArray_DoubleArray() ***/
Token* add_DoubleArray_DoubleArray(Token *a1, Token *a2) {
    return $DoubleArray_add(a1, a2);
}
/**/

/*** add_Double_Array() ***/
Token* add_Double_Array(double a1, Token *a2) {
    int i;
    Token *result = $new(Array(a2->payload.Array->size, 0));

    for (i = 0; i < a2->payload.Array->size; i++) {
        Array_set(result, i, $add_Double_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** add_Double_Double() ***/
double add_Double_Double(double a1, double a2) {
    return a1 + a2;
}
/**/

/*** add_Double_DoubleArray() ***/
Token* add_Double_DoubleArray(double a1, Token *a2) {
    int i;
    Token *result = $new(DoubleArray(a2->payload.Array->size, 0));

    for (i = 0; i < a2->payload.DoubleArray->size; i++) {
            DoubleArray_set(result, i, $add_Double_Double(a1, DoubleArray_get(a2, i)));
    }
    return result;
}
/**/

/*** add_Double_Int() ***/
double add_Double_Int(double a1, int a2) {
    return a1 + a2;
}
/**/

/*** add_Double_String() ***/
char* add_Double_String(double a1, char* a2) {
    char* string = (char*) malloc(sizeof(char) * (20 + strlen(a2)));
    sprintf((char*) string, "%g%s", a1, a2);
    return string;
}
/**/

/*** add_Double_Token() ***/
Token* add_Double_Token(double a1, Token *a2) {
    Token *token = $new(Double(a1));
    return $add_Token_Token(token, a2);
}
/**/

/*** add_IntArray_IntArray() ***/
Token* add_IntArray_IntArray(Token *a1, Token *a2) {
    return $IntArray_add(a1, a2);
}
/**/

/*** add_Int_IntArray() ***/
#define add_Int_IntArray(a1, a2) $add_IntArray_Int(a2, a1)
/**/

/*** add_IntArray_Int() ***/
Token* add_IntArray_Int(Token *a1, int a2) {
    int i;
    Token *result = $new(IntArray(a1->payload.IntArray->size, 0));

    for (i = 0; i < a1->payload.IntArray->size; i++) {
            IntArray_set(result, i, $add_Int_Int(IntArray_get(a1, i), a2));
    }
    return result;
}
/**/

/*** add_Int_Array() ***/
Token* add_Int_Array(int a1, Token *a2) {
    int i;
    Token *result = $new(Array(a2->payload.Array->size, 0));

    for (i = 0; i < a2->payload.Array->size; i++) {
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
int add_Int_Int(int a1, int a2) {
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
int add_Int_Token(int a1, Token *a2) {
    Token *token = $new(Int(a1));
    return $typeFunc(TYPE_Int::add(token, a2));
}
/**/

/*** add_Long_Array() ***/
Token* add_Long_Array(long long a1, Token *a2) {
    int i;
    Token *result = $new(Array(a2->payload.Array->size, 0));

    for (i = 0; i < a2->payload.Array->size; i++) {
        Array_set(result, i, $add_Long_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** add_Long_Long() ***/
long long add_Long_Long(long long a1, long long a2) {
    return a1 + a2;
}
/**/

/*** add_Long_Token() ***/
Token* add_Long_Token(long long a1, Token *a2) {
    Token *token = $new(Long(a1));
    return $add_Token_Token(token, a2);
}
/**/

/*** add_Scalar_Array() ***/
Token* add_Scalar_Array(Scalar a1, Token *a2) {
    int i;
    Token *result = $new(Array(a2->payload.Array->size, 0));

    for (i = 0; i < a2->payload.Array->size; i++) {
        Array_set(result, i, $add_Scalar_Token(a1, Array_get(a2, i)));
    }
    return result;
}
/**/

/*** add_Scalar_Scalar() ***/
Scalar add_Scalar_Scalar(Scalar a1, Scalar a2) {
    return a1 + a2;
}
/**/

/*** add_Scalar_DoubleArray() ***/
Token* add_Scalar_DoubleArray(Scalar a1, Token *a2) {
    int i;
    Token *result = $new(DoubleArray(a2->payload.Array->size, 0));

    for (i = 0; i < a2->payload.DoubleArray->size; i++) {
            DoubleArray_set(result, i, $add_Scalar_Double(a1, DoubleArray_get(a2, i)));
    }
    return result;
}
/**/

/*** add_Scalar_Int() ***/
Scalar add_Scalar_Int(Scalar a1, int a2) {
    return a1 + a2;
}
/**/

/*** add_Scalar_String() ***/
char* add_Scalar_String(Scalar a1, char* a2) {
    char* string = (char*) malloc(sizeof(char) * (20 + strlen(a2)));
    sprintf((char*) string, "%g%s", a1, a2);
    return string;
}
/**/

/*** add_Scalar_Token() ***/
Token* add_Scalar_Token(Scalar a1, Token *a2) {
    Token *token = $new(Scalar(a1));
    return $add_Token_Token(token, a2);
}
/**/

/*** add_StringArray_StringArray() ***/
Token* add_StringArray_StringArray(Token *a1, Token *a2) {
    return $StringArray_add(a1, a2);
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
char* add_String_Int(char* a1, int a2) {
    char* string = (char*) malloc(sizeof(char) * (12 + strlen(a1)));
    sprintf((char*) string, "%s%d", a1, a2);
    return string;
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
Token* add_Token_Double(Token *a1, double a2) {
    return $add_Double_Token(a2, a1);
}
/**/

/*** add_Token_Int() ***/
int add_Token_Int(Token *a1, int a2) {
    return $add_Int_Token(a2, a1);
}
/**/

/*** add_Token_Token() ***/
Token* add_Token_Token(Token *a1, Token *a2) {
    return $tokenFunc(a1::add(a2));
}
/**/

