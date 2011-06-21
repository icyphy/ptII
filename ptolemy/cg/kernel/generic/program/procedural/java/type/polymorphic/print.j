/*** print_Array() ***/
static void print_Array(Array a) {
     $Array_print(a);
}
/**/

/*** print_Boolean() ***/
static void print_Double(boolean b) {
     System.out.println(b);
}
/**/

*** print_Double() ***/
static void print_Double(double d) {
     System.out.println(d);
}
/**/

/*** print_Integer() ***/
static void print_Integer(int i) {
     System.out.println(i);
}
/**/

/*** print_Long() ***/
static void print_Integer(long l) {
    System.out.println(l);
}
/**/

/*** print_Token() ***/
static void print_Token(Token token) {
     //$tokenFunc(a::print());
    switch (token.type) {
        case TYPE_Double:
            $Double_print(token);
            break;
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

/*** print_Token_Token() ***/
static Token print_Token_Token(Token a) {
     $print_Token(a);
     return null;
}
/**/
