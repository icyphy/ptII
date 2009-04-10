/*** toString_Array() ***/
char* toString_Array(Token a) {
	return $Array_toString(a)
}
/**/

/*** toString_Boolean() ***/
char* toString_Boolean(boolean a) {
	return BooleantoString(a);
}
/**/

/*** toString_BooleanArray() ***/
char* toString_BooleanArray(Token a) {
	return $BooleanArray_toString(a);
}
/**/

/*** toString_Double() ***/
char* toString_Double(double a) {
	return DoubletoString(a);
}
/**/

/*** toString_DoubleArray() ***/
char* toString_DoubleArray(Token a) {
	return $DoubleArray_toString(a);
}
/**/

/*** toString_Int() ***/
char* toString_Int(int a) {
	return InttoString(a);
}
/**/

/*** toString_IntArray() ***/
char* toString_IntArray(Token a) {
	return $IntArray_toString(a);
}
/**/

/*** toString_Long() ***/
char* toString_Long(long long a) {
	return LongtoString(a);
}
/**/

/*** toString_String() ***/
char* toString_String(char* a) {
	return (a);
}
/**/

/*** toString_StringArray() ***/
char* toString_StringArray(Token a) {
	return $StringArray_toString(a);
}
/**/

/*** toString_Token() ***/
char* toString_Token(Token a) {
	return $tokenFunc(a::toString());
}
/**/

