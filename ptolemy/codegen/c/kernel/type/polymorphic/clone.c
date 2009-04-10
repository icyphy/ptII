/*** clone_Array() ***/
#define clone_Array(a) $Array_clone(a)
/**/

/*** clone_Boolean() ***/
#define clone_Boolean(a) a
/**/

/*** clone_BooleanArray() ***/
#define clone_BooleanArray(a) $BooleanArray_clone(a)
/**/

/*** clone_Double() ***/
#define clone_Double(a) a
/**/

/*** clone_DoubleArray() ***/
#define clone_DoubleArray(a) $DoubleArray_clone(a)
/**/

/*** clone_Int() ***/
#define clone_Int(a) a
/**/

/*** clone_IntArray() ***/
#define clone_IntArray(a) $IntArray_clone(a)
/**/

/*** clone_Long() ***/
#define clone_Long(a) a
/**/

/*** clone_String() ***/
#define clone_String(a) strdup(a)
/**/

/*** clone_StringArray() ***/
#define clone_StringArray(a) $StringArray_clone(a)
/**/

/*** clone_Token() ***/
#define clone_Token(a) $tokenFunc(a::clone())
/**/

