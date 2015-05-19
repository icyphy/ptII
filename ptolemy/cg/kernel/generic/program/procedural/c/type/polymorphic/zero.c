/*** zero_Array() ***/
Token* zero_Array(Token *a) {
        return $Array_zero(a);
}
/**/

/*** zero_Boolean() ***/
boolean zero_Boolean() {
        return false;
}
/**/

/*** zero_Double() ***/
double zero_Double() {
        return 0.0;
}
/**/

/*** zero_DoubleArray() ***/
Token* zero_DoubleArray(Token *a) {
        return $DoubleArray_zero(a);
}
/**/

/*** zero_Int() ***/
int zero_Int() {
        return 0;
}
/**/

/*** zero_IntArray() ***/
Token* zero_IntArray(Token *a) {
        return $IntArray_zero(a);
}
/**/

/*** zero_Long() ***/
long long zero_Long() {
        return 0;
}
/**/

/*** zero_Matrix() ***/
Token* zero_Matrix(Token *a) {
        return $Matrix_zero(a);
}
/**/

/*** zero_String() ***/
char* zero_String() {
        return "";
}
/**/

/*** zero_Token() ***/
Token* zero_Token(Token *a) {
        return $tokenFunc(a::zero());
}
/**/

