/*** print_Array() ***/
#define print_Array(a) $Array_print(a)
/**/

/*** print_Boolean() ***/
inline void print_Boolean(boolean b) {
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
inline void print_Long(long long l) {
    printf("%d", l);
}
/**/

/*** print_Token() ***/
#define print_Token(a) $tokenFunc(a::print())
/**/

