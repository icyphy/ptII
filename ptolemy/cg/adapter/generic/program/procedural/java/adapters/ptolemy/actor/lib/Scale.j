/***FireBlock***/
// primitive is commutative.
$put(output, $val(factor) * $get(input));
/**/

/***TokenFireBlock***/
if ($param(scaleOnLeft)) {
    $put(output, Scale_scaleOnLeft($get(input), (double) $val(factor)));
} else {
    $put(output, Scale_scaleOnRight($get(input), (double) $val(factor)));
}
/**/

/***Scale_scaleOnLeftBlock***/
Token Scale_scaleOnLeft(Token input, double factor) {
    int i;
    Token result = new Token();

#ifdef PTCG_TYPE_Array
    if (input.type == TYPE_Array) {
            result = $new(Array(((Array)(input.payload)).size, 0));

        for (i = 0; i < ((Array)(input.payload)).size; i++) {
            ((Array)(result.payload)).elements[i] = Scale_scaleOnLeft(Array_get(input, i), factor);
        }

        return result;
    } else {
#endif

        return $tokenFunc($new(Double(factor))::multiply(input));

#ifdef PTCG_TYPE_Array
    }
#endif
}
/**/

/***Scale_scaleOnRightBlock***/
Token Scale_scaleOnRight(Token input, double factor) {
    int i;
    Token result = new Token();

#ifdef PTCG_TYPE_Array
    if (input.type == TYPE_Array) {
            result = $new(Array(((Array)(input.payload)).size, 0));

        for (i = 0; i < ((Array)(input.payload)).size; i++) {
            ((Array)(result.payload)).elements[i] = Scale_scaleOnRight(Array_get(input, i), factor);
        }

        return result;
    } else {
#endif

        return $tokenFunc(input::multiply($new(Double(factor))));

#ifdef PTCG_TYPE_Array
    }
#endif
}
/**/
