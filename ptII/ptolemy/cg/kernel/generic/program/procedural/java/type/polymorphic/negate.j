/*** negate_Array() ***/
static Token negate_Array(Token a) {
     return $Array_negate(a);
}
/**/

/*** negate_Boolean() ***/
static boolean negate_Boolean(boolean a) {
     return !a;
}
/**/

/*** negate_Double() ***/
double negate_Double(double a) {
     return -a;
}
/**/

/*** negate_Integer() ***/
int negate_Integer(int a) {
     return -a;
}
/**/

/*** negate_Long() ***/
long negate_Long(long a) {
     return -a;
}
/**/

/*** negate_Token() ***/
static Token negate_Token(Token a) {
      return $tokenFunc(a::negate());
}
/**/

/*** negate_Token_Token() ***/
static Token negate_Token_Token(Token token) {
      switch (token.type) {
#ifdef PTCG_TYPE_Array
    case TYPE_Array:
        return $Array_negate(token);
#endif

#ifdef PTCG_TYPE_Boolean
    case TYPE_Boolean:
        return $Boolean_negate(token);
#endif

#ifdef PTCG_TYPE_Integer
    case TYPE_Integer:
        return $Integer_negate(token);
#endif

#ifdef PTCG_TYPE_Double
    case TYPE_Double:
        return $Double_negate(token);
#endif

#ifdef PTCG_TYPE_String
    case TYPE_String:
        return $String_negate(token);
#endif
    default:
        throw new RuntimeException("negate_Token_Token(): Conversion from an unsupported type: "
         + token.type);
    }
}
/**/


