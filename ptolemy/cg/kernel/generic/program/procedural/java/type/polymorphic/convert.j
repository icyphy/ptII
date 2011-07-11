/*** convert_Array_Array() ***/
static Token convert_Array_Array(Token a) {
       return a;
}
/**/

/*** convert_Boolean_Boolean() ***/
static boolean convert_Boolean_Boolean(boolean a) {
        return a;
}
/**/

/*** convert_Boolean_Integer() ***/
int convert_Boolean_Integer(boolean a) {
    return a ? 1 : 0;
}
/**/

/*** convert_Boolean_String() ***/
String convert_Boolean_String(boolean a) {
    return Boolean.toString(a);
}
/**/

/*** convert_Double_Array() ***/
static Token convert_Double_Array(double a) {
    return $new(Array(1, 1, $new(Double(a))));
}
/**/

/*** convert_Complex_Complex() ***/
static Token convert_Complex_Complex(Token a) {
    return $Complex_new(a);
}
/**/

/*** convert_Double_Complex() ***/
static Token convert_Double_Complex(double a) {
    return Complex_new(a);
}
/**/

/*** convert_Double_Double() ***/
double convert_Double_Double(double a) {
    return a;
}
/**/

/*** convert_Double_Integer() ***/
int convert_Double_Integer(double a) {
    return (int) a;
}
/**/

/*** convert_Double_String() ***/
String convert_Double_String(double a) {
       return Double.toString(a);
}
/**/

/*** convert_Double_Token() ***/
static Token convert_Double_Token(double a) {
    return $new(Double(a));
}
/**/

/*** convert_Integer_Array() ***/
static Token convert_Integer_Array(int a) {
    return $new(Array(1, 1, $new(Integer(a)), TYPE_Integer));
}
/**/

/*** convert_Integer_Boolean() ***/
static boolean convert_Integer_Boolean(int a) {
    return (a != 0) ? true : false;
}
/**/

/*** convert_Integer_Complex() ***/
static Token convert_Integer_Complex(int a) {
    return Complex_new(a);
}
/**/

/*** convert_Integer_Double() ***/
double convert_Integer_Double(int a) {
    return (double) a;
}
/**/

/*** convert_Integer_Integer() ***/
int convert_Integer_Integer(int a) {
    return a;
}
/**/

/*** convert_Integer_String() ***/
String convert_Integer_String(int a) {
      return Integer.toString(a);
}
/**/

/*** convert_Integer_Token() ***/
static Token convert_Integer_Token(int a) {
    return $new(Integer(a));
}
/**/

/*** convert_Long_Array() ***/
static Token convert_Long_Array(long long a) {
    return $new(Array(1, 1, $new(Long(a))));
}
/**/

/*** convert_Long_Long() ***/
long convert_Long_Long(long a) {
     return a;
}
/**/

/*** convert_Long_Token() ***/
static Token convert_Long_Token(long long a) {
    return $new(Long(a));
}
/**/

/*** convert_Matrix_Matrix() ***/
static Token convert_Matrix_Matrix(Token a1) {
    return a1;
}
/**/


/*** convert_String_Boolean() ***/
String convert_String_Boolean(String a) {
       return Boolean.toString(a);
}
/**/

/*** convert_String_Complex() ***/
String convert_String_Complex(String a) {
       Double real = 0.0;
       Double imag = 0.0;
       a = a.trim();
       int plusIndex, minusIndex;
       if (plusIndex = a.indexOf("+") == -1 && minusIndex = a.indexOf("-") == -1) {
           real = Double.toString(a);
       } else {
                  if (plusIndex > 0) {
               // We have an imaginary part that is positive.
               real = Double.toString(a.substring(0, plusIndex - 1));
               imag = Double.toString(a.substring(0, plusIndex ));
           } else if (minusIndex > 0) {
               // We have an imaginary part that is negative.
               real = Double.toString(a.substring(0, minusIndex - 1));
               imag = Double.toString(a.substring(0, minusIndex ));
           } else {
               // We have only a real part that has a sign
               real = Double.toString(a);
           }
       }

       return $Complex_new(real, imag);
}
/**/

/*** convert_String_Double() ***/
String convert_String_Double(String a) {
       return Double.toString(a);
}
/**/

/*** convert_String_Integer() ***/
String convert_String_Integer(String a) {
       return Integer.toString(a);
}
/**/

/*** convert_String_String() ***/
String convert_String_String(String a) {
       return a;
}
/**/

/*** convert_Token_Token() ***/
static Token convert_Token_Token(Token a) {
       return a;
}
/**/

/*** convert_Token_Token2() ***/
static Token convert_Token_Token2(Short type1, Token token, Short type2) {
      switch (type1) {
#ifdef PTCG_TYPE_Array
    case TYPE_Array:
        token.payload = ArraytoString((Boolean)(token.payload));
        return token;
#endif

#ifdef PTCG_TYPE_Boolean
    case TYPE_Boolean:
        token.payload = BooleantoString((Boolean)(token.payload));
        return token;
#endif

#ifdef PTCG_TYPE_Integer
    case TYPE_Integer:
        token.payload = IntegertoString((Integer(token.payload));
        return token;
#endif

#ifdef PTCG_TYPE_Double
    case TYPE_Double:
        token.payload = DoubletoString((Double)(token.payload));
        return token;
#endif

#ifdef PTCG_TYPE_String
    case TYPE_String:
        return token;
        break;
#endif
    default:
        throw new RuntimeException("convert_Token_Token2(): Conversion from an unsupported type: "
         + token.type);
    }

}
/**/

