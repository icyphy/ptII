/*** negate_Array() ***/
array negate_Array(array a) {
     return $Array_negate(a)
}
/**/

/*** negate_Boolean() ***/
boolean negate_Boolean(boolean a) {
     return !a;
}
/**/

/*** negate_Double() ***/
double negate_Double(double a) {
     return -a;
}
/**/

/*** negate_Int() ***/
int negate_Int(int a) {
     return -a;
}
/**/

/*** negate_Long() ***/
long negate_Long(long a) {
     return -a;
}
/**/

/*** negate_Token() ***/
Token negate_Token(Token a) {
      return $tokenFunc(a::negate());
}
/**/

/*** negate_Token_Token() ***/
Token negate_Token_Token(Token a) {
      return $tokenFunc(a::negate());
}
/**/


