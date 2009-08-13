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

/***sharedScaleOnLeftBlock***/
Token Scale_scaleOnLeft(Token input, double factor) {
    int i;
    Token result = new Token();

    if (input.type == TYPE_Array) {
            result = $new(Array(((Array)(input.payload)).size, 0));

        for (i = 0; i < ((Array)(input.payload)).size; i++) {
            ((Array)(result.payload)).elements[i] = Scale_scaleOnLeft(Array_get(input, i), factor);
        }

        return result;
    } else {
        return $tokenFunc($new(Double(factor))::multiply(input));
    }
}
/**/

/***sharedScaleOnRightBlock***/
Token Scale_scaleOnRight(Token input, double factor) {
    int i;
    Token result = new Token();

    if (input.type == TYPE_Array) {
            result = $new(Array(((Array)(input.payload)).size, 0));

        for (i = 0; i < ((Array)(input.payload)).size; i++) {
            ((Array)(result.payload)).elements[i] = Scale_scaleOnRight(Array_get(input, i), factor);
        }

        return result;
    } else {
        return $tokenFunc(input::multiply($new(Double(factor))));
    }
}
/**/
