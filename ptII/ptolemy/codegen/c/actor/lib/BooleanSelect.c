/*** fireBlock ***/
        $get(control, 0)
        if ($ref(control)) {
                $sendTrueInputs()
        } else {
                $sendFalseInputs()
        }
/**/

/*** trueBlock($channel) ***/
        $get(trueInput, $channel)
        $ref(output#$channel) = $ref(trueInput#$channel);
        $send(output, $channel)
/**/

/*** falseBlock($channel) ***/
        $get(falseInput, $channel)
        $ref(output#$channel) = $ref(falseInput#$channel);
        $send(output, $channel)
/**/
