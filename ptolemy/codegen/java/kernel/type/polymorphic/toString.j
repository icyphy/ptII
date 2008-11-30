
/*** toString_Token_Token() ***/
Token toString_Token_Token(Token thisToken) {
    Token result = null;
    switch (thisToken.type) {
#ifdef PTCG_TYPE_Double
    case TYPE_Double:
       	result = Double_toString(thisToken);
	result.type = TYPE_String;
	return result;
	break;
#endif
    default:
        throw new InternalError("toString_Token_Token_(): unsupported type: "
	    + thisToken.type);

    }
}
/**/