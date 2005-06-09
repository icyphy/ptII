/***preinitBlock*/
    FILE* $actorSymbol(filePtr);
    char $actorSymbol(tmpChar)[4];
    unsigned int $actorSymbol(numChannel);
    unsigned int $actorSymbol(bytesPerSample);
    unsigned int $actorSymbol(lengthOfData);
    double $actorSymbol(maxSampleReciprocal);
    int $actorSymbol(result);
    int $actorSymbol(i);
/**/

/*** initBlock */
    if (!($actorSymbol(filePtr) = fopen ($ref(fileOrURL),"r"))) {
        fprintf(stderr,"ERROR: cannot open file for AudioReader actor.\n");
        exit(1);
    }
    
    // "RIFF"
    fscanf($actorSymbol(filePtr),"%c%c%c%c", &$actorSymbol(tmpChar)[0], &$actorSymbol(tmpChar)[1],&$actorSymbol(tmpChar)[2],&$actorSymbol(tmpChar)[3]);
printf("\"RIFF\" = %c%c%c%c\n", $actorSymbol(tmpChar)[0],$actorSymbol(tmpChar)[1],$actorSymbol(tmpChar)[2],$actorSymbol(tmpChar)[3]);

    // length
    fscanf($actorSymbol(filePtr),"%c%c%c%c", &$actorSymbol(tmpChar)[0], &$actorSymbol(tmpChar)[1],&$actorSymbol(tmpChar)[2],&$actorSymbol(tmpChar)[3]);

    // "WAVE"
    fscanf($actorSymbol(filePtr),"%c%c%c%c", &$actorSymbol(tmpChar)[0], &$actorSymbol(tmpChar)[1],&$actorSymbol(tmpChar)[2],&$actorSymbol(tmpChar)[3]);
printf("\n\"WAVE\" = %c%c%c%c\n", $actorSymbol(tmpChar)[0],$actorSymbol(tmpChar)[1],$actorSymbol(tmpChar)[2],$actorSymbol(tmpChar)[3]);

    // "fmt_"
    fscanf($actorSymbol(filePtr),"%c%c%c%c", &$actorSymbol(tmpChar)[0], &$actorSymbol(tmpChar)[1],&$actorSymbol(tmpChar)[2],&$actorSymbol(tmpChar)[3]);
printf("\n\"fmt\" = %c%c%c%c\n", $actorSymbol(tmpChar)[0],$actorSymbol(tmpChar)[1],$actorSymbol(tmpChar)[2],$actorSymbol(tmpChar)[3]);
    // length
    fscanf($actorSymbol(filePtr),"%c%c%c%c", &$actorSymbol(tmpChar)[0], &$actorSymbol(tmpChar)[1],&$actorSymbol(tmpChar)[2],&$actorSymbol(tmpChar)[3]);

    // Always 0x01
    fscanf($actorSymbol(filePtr),"%c%c", &$actorSymbol(tmpChar)[0], &$actorSymbol(tmpChar)[1]);
printf("\n\"0x01\" = %u\n", $actorSymbol(tmpChar)[0] | ($actorSymbol(tmpChar)[1] << 8));

    // #channel
    fscanf($actorSymbol(filePtr),"%c%c", &$actorSymbol(tmpChar)[0], &$actorSymbol(tmpChar)[1]);
    $actorSymbol(numChannel) =  0x0 | $actorSymbol(tmpChar)[0] | ($actorSymbol(tmpChar)[1] << 8);
printf("\nnumChannel = %u\n", $actorSymbol(numChannel));

    // sample rate (binary)
    fscanf($actorSymbol(filePtr),"%c%c%c%c", &$actorSymbol(tmpChar)[0], &$actorSymbol(tmpChar)[1],&$actorSymbol(tmpChar)[2],&$actorSymbol(tmpChar)[3]);
printf("\n\"(sample rate)\" = %u\n", 0x0 | $actorSymbol(tmpChar)[0] | ($actorSymbol(tmpChar)[1] << 8) | ($actorSymbol(tmpChar)[2] << 16) | ($actorSymbol(tmpChar)[3] << 24));

    // bytes per sec
    fscanf($actorSymbol(filePtr),"%c%c%c%c", &$actorSymbol(tmpChar)[0], &$actorSymbol(tmpChar)[1],&$actorSymbol(tmpChar)[2],&$actorSymbol(tmpChar)[3]);
printf("\n\"(bytes per sec)\" = %u\n", 0x0 | $actorSymbol(tmpChar)[0] | ($actorSymbol(tmpChar)[1] << 8) | ($actorSymbol(tmpChar)[2] << 16) | ($actorSymbol(tmpChar)[3] << 24));

    // bytes per sample
    fscanf($actorSymbol(filePtr),"%c%c", &$actorSymbol(tmpChar)[0], &$actorSymbol(tmpChar)[1]);
    $actorSymbol(bytesPerSample) =  0x0 | $actorSymbol(tmpChar)[0] | ($actorSymbol(tmpChar)[1] << 8);
printf("\nbytesPerSampel = %u\n", $actorSymbol(bytesPerSample));

    // bits per sample
    fscanf($actorSymbol(filePtr),"%c%c", &$actorSymbol(tmpChar)[0], &$actorSymbol(tmpChar)[1]);
printf("\n\"(bits per sample)\" = %u\n", 0x0 | $actorSymbol(tmpChar)[0] | ($actorSymbol(tmpChar)[1] << 8));

    // "data"
    fscanf($actorSymbol(filePtr),"%c%c%c%c", &$actorSymbol(tmpChar)[0], &$actorSymbol(tmpChar)[1],&$actorSymbol(tmpChar)[2],&$actorSymbol(tmpChar)[3]);
printf("\n\"data\" = %c%c%c%c\n", $actorSymbol(tmpChar)[0],$actorSymbol(tmpChar)[1],$actorSymbol(tmpChar)[2],$actorSymbol(tmpChar)[3]);

    // length
    fscanf($actorSymbol(filePtr),"%c%c%c%c", &$actorSymbol(tmpChar)[0], &$actorSymbol(tmpChar)[1],&$actorSymbol(tmpChar)[2],&$actorSymbol(tmpChar)[3]);
    $actorSymbol(lengthOfData) = 0x0 | $actorSymbol(tmpChar)[0] | ($actorSymbol(tmpChar)[1] << 8) | ($actorSymbol(tmpChar)[2] << 16) | ($actorSymbol(tmpChar)[3] << 24);
printf("\nlengthOfData = %u\n", $actorSymbol(lengthOfData));

    //$actorSymbol(maxSampleReciprocal) = 1/(pow(2, 8 * $actorSymbol(bytesPerSample) - 1));
    if ($actorSymbol(bytesPerSample) == 2) {
        // 1 / 32768
        $actorSymbol(maxSampleReciprocal) = 3.0517578125e-5;
    } else if ($actorSymbol(bytesPerSample) == 1) {
        // 1 / 128
        $actorSymbol(maxSampleReciprocal) = 7.8125e-3;
    } else if ($actorSymbol(bytesPerSample) == 3) {
        // 1 / 8388608
        $actorSymbol(maxSampleReciprocal) = 1.1920928955e07;
    } else if ($actorSymbol(bytesPerSample) == 4) {
        // 1 / 147483648e9
        $actorSymbol(maxSampleReciprocal) = 4.655661287308e-10;
    } else {
        // Should not happen.
        $actorSymbol(maxSampleReciprocal) = 0;
    }

/**/

// FIXME: do we need to check for EOF??
/*** readSoundFile */
    //fscanf($actorSymbol(filePtr),"%c", &$actorSymbol(tmpChar)[0]);
    //$ref(output#0) = $actorSymbol(tmpChar)[0];
    for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(bytesPerSample); $actorSymbol(i) += 1) {
        fscanf($actorSymbol(filePtr),"%c", &$actorSymbol(tmpChar)[$actorSymbol(i)]);
    }
    $actorSymbol(result) = ($actorSymbol(tmpChar)[0] >> 7);

    for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(bytesPerSample); $actorSymbol(i) += 1) {
        $actorSymbol(result) = ($actorSymbol(result) << 8) + ($actorSymbol(tmpChar)[$actorSymbol(i)] & 0xff);
    }
    $ref(output#0) = ((double) $actorSymbol(result) * $actorSymbol(maxSampleReciprocal));
/**/

/*** wrapupBlock */
    fclose($actorSymbol(filePtr));
/**/

