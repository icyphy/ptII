/*** convert_Array_Array() ***/
#define convert_Array_Array(a) a
/**/

/*** convert_Array_String() ***/
char* convert_Array_String(Token *a) {
        return $toString_Array(a);
}
/**/

/*** convert_Array_Token() ***/
Token* convert_Array_Token(Token *a) {
        return a;
}
/**/

/*** convert_Boolean_Array() ***/
Token* convert_Boolean_Array(boolean a) {
  return $new(Array(1, 1, $new(Boolean(a)), TYPE_Boolean));
}
/**/

/*** convert_Boolean_Token() ***/
// FIXME: don't know if this is correct.
Token* convert_Boolean_Array(boolean a) {
  return $new(Int(a));
}
/**/

/*** convert_Boolean_Boolean() ***/
#define convert_Boolean_Boolean(a) a
/**/

/*** convert_Boolean_Int() ***/
int convert_Boolean_Int(boolean a) {
    return a ? 1 : 0;
}
/**/

/*** convert_Boolean_Scalar() ***/
Scalar convert_Boolean_Scalar(boolean a) {
    return a ? 1.0 : 0.0;
}
/**/

/*** convert_Boolean_String() ***/
char* convert_Boolean_String(boolean a) {
        return $toString_Boolean(a);
}
/**/

/*** convert_DoubleArray_Array() ***/
Token* convert_DoubleArray_Array(Token *token) {
        int i;
        int length = token->payload.DoubleArray->size;
        Token *result = $new(Array(length, 0));
        for (i = 0; i < length; i++) {
                Array_set(result, i, $convert_Double_Token(DoubleArray_get(token, i)));
        }
        return result;
}
/**/

/*** convert_DoubleArray_DoubleArray() ***/
#define convert_DoubleArray_DoubleArray(a) a
/**/

/*** convert_DoubleArray_IntArray() ***/
Token* convert_DoubleArray_IntArray(Token *token) {
        int i;
        int length = token->payload.DoubleArray->size;
        Token *result = $new(IntArray(length, 0));
        for (i = 0; i < length; i++) {
                IntArray_set(result, i, (int) DoubleArray_get(token, i));
        }
        return result;
}
/**/

/*** convert_DoubleArray_StringArray() ***/
Token* convert_DoubleArray_StringArray(Token *token) {
        int i;
        int length = token->payload.DoubleArray->size;
        Token *result = $new(StringArray(length, 0));
        for (i = 0; i < length; i++) {
                StringArray_set(result, i, $convert_Double_String(DoubleArray_get(token, i)));
        }
        return result;
}
/**/

/*** convert_DoubleArray_Token() ***/
Token* convert_DoubleArray_Token(Token *a) {
        return a;
}
/**/

/*** convert_Double_Array() ***/
Token* convert_Double_Array(double a) {
  return $new(Array(1, 1, $new(Double(a)), TYPE_Double));
}
/**/

/*** convert_Double_Double() ***/
double convert_Double_Double(double a) {
    return a;
}
/**/

/*** convert_Double_DoubleArray() ***/
Token* convert_Double_DoubleArray(double a) {
  return $new(DoubleArray(1, 1, a));
}
/**/

/*** convert_Double_Int() ***/
int convert_Double_Int(double a) {
    return (int) a;
}
/**/

/*** convert_Double_Scalar() ***/
int convert_Double_Scalar(double a) {
    return (Scalar) a;
}
/**/

/*** convert_Double_String() ***/
char* convert_Double_String(double a) {
        return $toString_Double(a);
}
/**/

/*** convert_Double_StringArray() ***/
Token* convert_Double_StringArray(double d) {
        Token *result = $new(StringArray(1, 0));
        StringArray_set(result, 0, $convert_Double_String(d));
        return result;
}
/**/

/*** convert_Double_Token() ***/
Token* convert_Double_Token(double a) {
    return $new(Double(a));
}
/**/

/*** convert_IntArray_Array() ***/
Token* convert_IntArray_Array(Token *token) {
        int i;
        int length = token->payload.IntArray->size;
        Token *result = $new(Array(length, 0));
        for (i = 0; i < length; i++) {
                Array_set(result, i, $convert_Int_Token(IntArray_get(token, i)));
        }
        return result;
}
/**/

/*** convert_IntArray_DoubleArray() ***/
Token* convert_IntArray_DoubleArray(Token *token) {
#ifdef TYPE_IntArray
        int i;
        int length = token->payload.IntArray->size;
        Token *result = $new(DoubleArray(length, 0));
        for (i = 0; i < length; i++) {
                DoubleArray_set(result, i, (double) IntArray_get(token, i));
        }
        return result;
#endif
        return token;
}
/**/

/*** convert_IntArray_IntArray() ***/
#define convert_IntArray_IntArray(a) a
/**/

/*** convert_IntArray_StringArray() ***/
Token* convert_IntArray_StringArray(Token *token) {
        int i;
        int length = token->payload.IntArray->size;
        Token *result = $new(StringArray(length, 0));
        for (i = 0; i < length; i++) {
                StringArray_set(result, i, $convert_Int_String(IntArray_get(token, i)));
        }
        return result;
}
/**/

/*** convert_IntArray_Token() ***/
#define convert_IntArray_Token(a) a
/**/

/*** convert_Int_IntArray() ***/
Token* convert_Int_IntArray(int a) {
  return $new(IntArray(1, 1, a));
}
/**/
/*** convert_Int_Array() ***/
Token* convert_Int_Array(int a) {
  return $new(Array(1, 1, $new(Int(a)), TYPE_Int));
}
/**/

/*** convert_Int_Boolean() ***/
boolean convert_Int_Boolean(int a) {
    return (a != 0) ? true : false;
}
/**/

/*** convert_Int_Double() ***/
double convert_Int_Double(int a) {
    return (double) a;
}
/**/

/*** convert_Int_Int() ***/
int convert_Int_Int(int a) {
    return a;
}
/**/

/*** convert_Int_Long() ***/
long long convert_Int_Long(int a) {
    return (long long) a;
}
/**/

/*** convert_Int_String() ***/
char* convert_Int_String(int a) {
        return $toString_Int(a);
}
/**/

/*** convert_Int_Scalar() ***/
Scalar convert_Int_Scalar(int a) {
    return (Scalar) a;
}
/**/

/*** convert_Int_StringArray() ***/
Token* convert_Int_StringArray(int i) {
        Token *result = $new(StringArray(1, 0));
        StringArray_set(result, 0, $convert_Int_String(i));
        return result;
}
/**/

/*** convert_Int_Token() ***/
Token* convert_Int_Token(int a) {
    return $new(Int(a));
}
/**/

/*** convert_Long_Array() ***/
Token* convert_Long_Array(long long a) {
  return $new(Array(1, 1, $new(Long(a)), TYPE_Long));
}
/**/

/*** convert_Long_Long() ***/
#define convert_Long_Long(a) a
/**/

/*** convert_Long_Scalar() ***/
#define convert_Long_Scalar(a) a
/**/

/*** convert_Long_String() ***/
char* convert_Long_String(int a) {
        return $toString_Long(a);
}
/**/

/*** convert_Long_Token() ***/
Token* convert_Long_Token(long long a) {
    return $new(Long(a));
}
/**/

/*** convert_Matrix_Matrix() ***/
Token* convert_Matrix_Matrix(Token *a1) {
    return a1;
}
/**/

/*** convert_StringArray_StringArray() ***/
#define convert_StringArray_StringArray(a) a
/**/

/*** convert_String_Array() ***/
Token* convert_String_Array(char* a) {
  return $new(Array(1, 1, $new(String(a)), TYPE_String));
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

/*** convert_String_Long() ***/
#define convert_String_Int atol
/**/

/*** convert_String_String() ***/
#define convert_String_String(a) a
/**/

/*** convert_String_StringArray() ***/
Token* convert_String_StringArray(char* s) {
        Token *result = $new(StringArray(1, 0));
        StringArray_set(result, 0, s);
        return result;
}
/**/

/*** convert_String_Token() ***/
Token* convert_String_Token(char* s) {
    return String_new(s);
}
/**/

/*** convert_Token_Token() ***/
#define convert_Token_Token(a) a
/**/

/*** convert_Token_String() ***/
char*  convert_Token_String(Token *token) {
    char* stringPointer;

    switch (token->type) {
#ifdef TYPE_Boolean
    case TYPE_Boolean:
        stringPointer = BooleantoString(token->payload.Boolean);
        break;
#endif

#ifdef TYPE_Int
    case TYPE_Int:
        stringPointer = InttoString(token->payload.Int);
        break;
#endif

#ifdef TYPE_Double
    case TYPE_Double:
        stringPointer = DoubletoString(token->payload.Double);
        break;
#endif

#ifdef TYPE_String
    case TYPE_String:
        stringPointer = thisToken->payload.String;
        break;
#endif

    default:
        // FIXME: not finished
        fprintf(stderr, "String_convert(): Conversion from an unsupported type. (%d)\n", token->type);
        break;
    }
    return stringPointer;
}
/**/

/*** convert_Pointer_Token() ***/
Token* convert_Pointer_Token(void *a) {
    // FIXME: this will only work on 32 bit machines.
    return $new(Int((int)a));
}
/**/


/*** convert_UnsignedByte_String() ***/
char* convert_UnsignedByte_String(unsigned char a) {
        return $toString_UnsignedByte(a);
}
/**/
