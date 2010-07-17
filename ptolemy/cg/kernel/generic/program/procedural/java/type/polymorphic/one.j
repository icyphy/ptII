/*** one_Array() ***/
static Token one_Array(Token a) {
        return $Array_one(a);
}
/**/

/*** one_Boolean() ***/
static boolean one_Boolean() {
        return true;
}
/**/

/*** one_Double() ***/
double one_Double() {
        return 1.0;
}
/**/

/*** one_DoubleArray() ***/
static Token one_DoubleArray(Token a) {
        return $DoubleArray_one(a);
}
/**/

/*** one_Int() ***/
int one_Int() {
        return 1;
}
/**/

/*** one_IntArray() ***/
static Token one_IntArray(Token a) {
        return $IntArray_one(a);
}
/**/

/*** one_Long() ***/
long long one_Long() {
        return 1;
}
/**/

/*** one_String() ***/
// one_String is not supported.
/**/

/*** one_Token() ***/
static Token one_Token(Token a) {
        return $tokenFunc(a::one());
}
/**/

