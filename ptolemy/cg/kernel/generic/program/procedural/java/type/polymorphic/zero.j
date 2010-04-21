/*** zero_Array() ***/
Token zero_Array() {
        return $Array_new(0,0);
}
/**/

/*** zero_ArrayOneArg() ***/
Token zero_ArrayOneArg(Token a) {
        return $Array_zero(a);
}
/**/
 
/*** zero_Boolean() ***/
boolean zero_Boolean() {
        return false;
}
/**/

/*** zero_BooleanOneArg() ***/
boolean zero_BooleanOneArg(boolean a) {
        return false;
}
/**/

/*** zero_Double() ***/
double zero_Double() {
        return 0.0;
}
/**/

/*** zero_DoubleOneArg() ***/
double zero_DoubleOneArg(double a) {
        return 0.0;
}
/**/

/*** zero_DoubleArray() ***/
Token zero_DoubleArray() {
        return $new_DoubleArray(0,0);
}
/**/

/*** zero_DoubleArrayOneArg() ***/
Token zero_DoubleArrayOneArg(double [] a) {
        return $DoubleArray_zero(a);
}
/**/

/*** zero_Integer() ***/
int zero_Integer() {
        return 0;
}
/**/

/*** zero_IntegerOneArg() ***/
int zero_IntegerOneArg(int a) {
        return 0;
}
/**/

/*** zero_IntArray() ***/
Token zero_IntArray() {
        return $IntArray_new(0,0);
}
/**/

/*** zero_IntArrayOneArg() ***/
Token zero_IntArrayOneArg(int [] a) {
        return $IntArray_zero(a);
}
/**/

/*** zero_Long() ***/
long long zero_Long() {
        return 0;
}
/**/

/*** zero_LongOneArg() ***/
long long zero_LongOneArg(long[] a) {
        return 0;
}
/**/

/*** zero_String() ***/
String zero_String() {
        return "";
}
/**/

/*** zero_StringOneArg() ***/
String zero_StringOneArg(String a) {
        return "";
}
/**/

/*** zero_Token() ***/
Token zero_Token(Token a) {
        return $tokenFunc(a::zero());
}
/**/

/*** zero_TokenOneArg() ***/
Token zero_TokenOneArg(Token a) {
        return $tokenFunc(a::zero());
}
/**/

/*** zero_Token_Token() ***/
Token zero_Token_Token(Token token, Token... b) {

    Token result = null;
    switch (token.type) {
#ifdef PTCG_TYPE_Array
    case TYPE_Array:
        result = $Array_zero(token);
        return result;
#endif
#ifdef PTCG_TYPE_Boolean
    case TYPE_Boolean:
        result = $Boolean_new($zero_Boolean());
        return result;
#endif

#ifdef PTCG_TYPE_Double
    case TYPE_Double:
        result = $Double_new($zero_Double());
        return result;
#endif

#ifdef PTCG_TYPE_Integer
    case TYPE_Integer:
        result = $Integer_new($zero_Integer());
        return result;
#endif

    case TYPE_String:
        result = $String_new($zero_String());
        return result;
        // FIXME: not finished
    default:
        throw new RuntimeException("zero_Token_Token(): Conversion from an unsupported type: "
         + token.type);
    }
}
/**/

/*** zero_Token_TokenOneArg() ***/
Token zero_Token_TokenOneArg(Token a, Token... b) {
      return zero_Token_Token(a, b);
}
/**/
