/*** fireBlock ***/
        $get(control, 0)
        if ($ref(control)) {
                $generateTrueOutputs()
        } else {
                $generateFalseOutputs()
        }
/**/

/*** trueBlock($channel) ***/
        $get(input, $channel)
        $ref(trueOutput#$channel) = $ref(input#$channel);
        $send(trueOutput, $channel)
/**/

/*** falseBlock($channel) ***/
        $get(input, $channel)
        $ref(falseOutput#$channel) = $ref(input#$channel);
        $send(falseOutput, $channel)
/**/
