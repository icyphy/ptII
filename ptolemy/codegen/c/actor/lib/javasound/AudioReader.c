/***structBlock***/
    struct {
        SDL_AudioSpec spec;
        Uint8   *sound;         /* Pointer to wave data */
        Uint32   soundlen;      /* Length of wave data */
        int      soundpos;      /* Current read position */
    } $actorSymbol(wave);
/**/

/***preinitBlock***/
    struct {
        SDL_AudioSpec spec;
        Uint8   *sound;         /* Pointer to wave data */
        Uint32   soundlen;      /* Length of wave data */
        int      soundpos;      /* Current read position */
    } $actorSymbol(wave);
    
    unsigned int $actorSymbol(bitsPerSample);     
/**/

/*** initBlock(<arg>) ***/
    $actorSymbol(wave).soundpos = 0;
    
    /* Load the SDL library */
    if ( SDL_Init(SDL_INIT_AUDIO) < 0 ) {
        fprintf(stderr, "Couldn't initialize SDL: %s\n",SDL_GetError());
        exit(1);
    }
    atexit(SDL_Quit);
    if ( SDL_LoadWAV("<arg>", &$actorSymbol(wave).spec, &$actorSymbol(wave).sound, &$actorSymbol(wave).soundlen) == NULL ) {
        fprintf(stderr, "Couldn't load %s: %s\n", argv[1], SDL_GetError());
        exit(1);
    }
    if ($actorSymbol(wave).spec.format == AUDIO_U8 || $actorSymbol(wave).spec.format == AUDIO_S8) {
        $actorSymbol(bitsPerSample) = 8;
    }
    else {
        $actorSymbol(bitsPerSample) = 16;        
    }
/**/

/*** fireBlock ***/
    if ($actorSymbol(wave).soundpos >= $actorSymbol(wave).soundlen) {
       break;   // exit fire loop
    }
    if ($actorSymbol(bitsPerSample) = 8) {
        $ref(output) = $actorSymbol(wave).sound[$actorSymbol(wave).soundpos];
        $ref(output) -= 128;  // 2^7
        $ref(output) /= (double) 128;  // 2^7 - 1
        $actorSymbol(wave).soundpos++;    
    }
    else {
        $ref(output) = $actorSymbol(wave).sound[$actorSymbol(wave).soundpos] & ($actorSymbol(wave).sound[$actorSymbol(wave).soundpos] << 8);
        $ref(output) -= 32768;  // 2^15
        $ref(output) /= (double) 32768; // 2^15 - 1
        $actorSymbol(wave).soundpos += 2;
    }    
/**/

/*** wrapupBlock ***/
    SDL_FreeWAV($actorSymbol(wave).sound);
/**/

