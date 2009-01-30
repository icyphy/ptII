/*** print_Array() ***/
#define print_Array(a) $Array_print(a)
/**/

/*** print_Boolean() ***/
inline void print_Double(boolean b) {
    printf(b ? "true" : "false");
}
/**/

/*** print_Double() ***/
inline void print_Double(double d) {
    printf("%g", d);
}
/**/

/*** print_Int() ***/
inline void print_Int(int i) {
    printf("%d", i);
}
/**/

/*** print_Long() ***/
inline void print_Int(long long l) {
    printf("%d", l);
}
/**/

/*** print_Token() ***/
void print_Token(Token a) {
     //$tokenFunc(a::print());
    switch (token.type) {
	case TYPE_Integer: 
	    $Integer_print(token);
	    break;
	case TYPE_Array: 
	    $Array_print(token);
	    break;
        default:
	    System.out.println(token);
	    break;
    }		   
}
/**/

