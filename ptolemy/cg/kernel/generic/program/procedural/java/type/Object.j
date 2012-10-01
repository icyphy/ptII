/***declareBlock***/
// ptolemy/cg/kernel/generic/program/procedural/java/type/Object.j
// Named ObjectCG because we also have java.lang.Boject
public class ObjectCG {
    public Object object;
};
/**/

/***funcDeclareBlock***/
/**/


/***Object_new***/
static Token Object_new() {
    Token result = null;
#ifdef PTCG_TYPE_Object
    result = Object_new();
#endif
    return result;
}

static Token Object_new(double real) {
    Token result = null;
#ifdef PTCG_TYPE_Object
    result = Object_new(new Double(real));
#endif
    return result;
}

static Token Object_new(Object object) {
    Token result = new Token();
    result.payload = new ObjectCG();
    result.type = TYPE_Object;
    ((ObjectCG)result.payload).object = object;
    return result;
}

static Token Object_new(Token thisToken) {
    if (thisToken == null) {
        Token result = new Token();
        result.payload = new ObjectCG();
        result.type = TYPE_Object;
        ((ObjectCG)result.payload).object = null;
        return result;
    }
    switch (thisToken.type) {
#ifdef PTCG_TYPE_Object
    case TYPE_Object:
        return Object_new(((ObjectCG)thisToken.payload).object);
#endif

#ifdef PTCG_TYPE_Double
    case TYPE_Double:
        return Object_new(new Double((Double)thisToken.payload);
#endif

        // FIXME: not finished
    default:
        throw new RuntimeException("Object_new(Token): Conversion from an unsupported type. ("
                + thisToken.type + ")");
    }
}
/**/

/***Object_equals***/
static Token Object_equals(Token thisToken, Token... tokens) {
    Token otherToken;
    otherToken = tokens[0];
    Token result = null;
#ifdef PTCG_TYPE_Object
    result = Boolean_new(((ObjectCG)thisToken.payload).equals((ObjectCG)otherToken.payload));
#endif
    return result;
}
#endif
/**/

/***Object_isCloseTo***/
static Token Object_isCloseTo(Token thisToken, Token... tokens) {
    Token otherToken;
    Token tolerance;
    otherToken = tokens[0];
    tolerance = tokens[1];
    Token result = null;
#ifdef PTCG_TYPE_Object
    result = Object_equals(thisToken, otherToken);
#endif
    return result;
}
/**/

/***Object_delete***/
/* Instead of Object_delete(), we call scalarDelete(). */
/**/

/***Object_print***/
static Token Object_print(Token thisToken, Token... tokens) {
       return Object_toString(thisToken);
}
/**/


/***ObjecttoString***/
static String ObjecttoString(Token thisToken) {
    String result = null;
#ifdef PTCG_TYPE_Object
    if (thisToken == null) {
        result = "object(null)";
    } else {
        result = ((ObjectCG)(thisToken.payload)).object.toString();
    }
#endif
    return result;
}
/**/

/***Object_toString***/
static Token Object_toString(Token thisToken, Token... tokens) {
    Token result = null;
#ifdef PTCG_TYPE_Object
    if (thisToken == null) {
        result = String_new("object(null)");
    } else {
        result = String_new(((ObjectCG)thisToken.payload).object.toString());
    }
#endif
    return result;
}
/**/

/***Object_add***/
static Token Object_add(Token thisToken, Token... tokens) {
    Token result = null;
#ifdef PTCG_TYPE_Object
    throw new RuntimeException("Object_add(" + thisToken + ", ..): not supported");
#endif
    return result;
}
/**/

/***Object_subtract***/
static Token Object_subtract(Token thisToken, Token... tokens) {
    Token result = null;
#ifdef PTCG_TYPE_Object
    throw new RuntimeException("Object_subtract(" + thisToken + ", ..): not supported");
#endif
    return result;
}
/**/

/***Object_multiply***/
static Token Object_multiply(Token thisToken, Token... tokens) {
    Token result = null;
#ifdef PTCG_TYPE_Object
    throw new RuntimeException("Object_multiply(" + thisToken + ", ..): not supported");
#endif
    return result;
}
/**/

/***Object_divide***/
static Token Object_divide(Token thisToken, Token... tokens) {
    Token result = null;
#ifdef PTCG_TYPE_Object
    throw new RuntimeException("Object_divide(" + thisToken + ", ..): not supported");
#endif
    return result;
}

/**/

/***Object_negate***/
static Token Object_negate(Token thisToken, Token... tokens) {
    Token result = null;
#ifdef PTCG_TYPE_Object
    throw new RuntimeException("Object_negate(" + thisToken + ", ..): not supported");
#endif
    return result;
}
/**/

/***Object_zero***/
static Token Object_zero(Token token, Token... tokens) {
    Token result = null;
#ifdef PTCG_TYPE_Object
    throw new RuntimeException("Object_zero(" + thisToken + ", ..): not supported");
#endif
    return result;
}
/**/

/***Object_one***/
static Token Object_one(Token token, Token... tokens) {
    Token result = null;
#ifdef PTCG_TYPE_Object
    throw new RuntimeException("Object_one(" + thisToken + ", ..): not supported");
#endif
    return result;
}

/**/

/***Object_clone***/
static Token Object_clone(Token thisToken, Token... tokens) {
    return thisToken;
}
/**/

---------------- static functions -----------------------

/***Object_convert***/
static Token Object_convert(Token token, Token... tokens) {
    Token result = null;
#ifdef PTCG_TYPE_Object
    result = $Object_new();
#endif
    switch (token.type) {

#ifdef PTCG_TYPE_Double
    case TYPE_Double:
        if (token.payload != null) {
            ((ObjectCG)result.payload).object = new Double ((Double)token.payload).doubleValue();
        }
        break;
#endif

        // FIXME: not finished
    default:
        System.err.println("Object_convert(): Conversion from an unsupported type. ("
                + token.type + ")");
        System.exit(1);
        break;
    }
    return result;
}
/**/

