
/*** toString_Token_Token() ***/
static Token toString_Token_Token(Token thisToken, Token... otherToken) {
    Token result = null;
    if (thisToken == null) {
        return result;
    }
    switch (thisToken.type) {
#ifdef PTCG_TYPE_Double
    case TYPE_Double:
               result = Double_toString(thisToken);
        result.type = TYPE_String;
        return result;
#endif
#ifdef PTCG_TYPE_Integer
    case TYPE_Integer:
               result = Integer_toString(thisToken);
        result.type = TYPE_String;
        return result;
#endif
#ifdef PTCG_TYPE_Array
     case TYPE_Array:
               result = Array_toString(thisToken);
               result.type = TYPE_String;
               return result;
#endif
    default:
        throw new InternalError("toString_Token_Token_(): unsupported type: "
            + thisToken.type);

    }
}
/**/
