/*** negate_Array() ***/
#define negate_Array(a) $Array_negate(a)
/**/

/*** negate_Boolean() ***/
#define negate_Boolean(a) !a
/**/

/*** negate_Double() ***/
#define negate_Double(a) -a
/**/

/*** negate_DoubleArray() ***/
#define negate_DoubleArray(a) $DoubleArray_negate(a)
/**/

/*** negate_Int() ***/
#define negate_Int(a) -a
/**/

/*** negate_Long() ***/
#define negate_Long(a) -a
/**/

/*** negate_Token() ***/
#define negate_Token(a) $tokenFunc(a::negate())
/**/

