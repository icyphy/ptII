/*** preinitBlock ***/
    struct {
        SDL_AudioSpec spec;
        Uint8   *sound;         
        Uint32   soundlen;      
        int      soundpos;      
    } wave;

    void fillerup(void *unused, Uint8 *stream, int len)
    {
        Uint8 *waveptr;
        int    waveleft;

        waveptr = wave.sound + wave.soundpos;
        waveleft = wave.soundlen - wave.soundpos;

        while ( waveleft <= len ) {
            SDL_MixAudio(stream, waveptr, waveleft, SDL_MIX_MAXVOLUME);
            stream += waveleft;
            len -= waveleft;
            waveptr = wave.sound;
            waveleft = wave.soundlen;
            wave.soundpos = 0;
        }
        SDL_MixAudio(stream, waveptr, len, SDL_MIX_MAXVOLUME);
        wave.soundpos += len;
    }
/**/

/*** initBlock ***/
    /* Load the SDL library */
    if ( SDL_Init(SDL_INIT_AUDIO) < 0 ) {
        fprintf(stderr, "Couldn't initialize SDL: %s\n",SDL_GetError());
        exit(1);
    }
    atexit(SDL_Quit);

    if ( argv[1] == NULL ) {
        fprintf(stderr, "Usage: %s <wavefile>\n", argv[0]);
        exit(1);
    }

    /* Load the wave file into memory */
    if ( SDL_LoadWAV(argv[1],
            &wave.spec, &wave.sound, &wave.soundlen) == NULL ) {
        fprintf(stderr, "Couldn't load %s: %s\n",
                        argv[1], SDL_GetError());
        exit(1);
    }

    wave.spec.callback = fillerup;
    /* Set the signals */
#ifdef SIGHUP
    signal(SIGHUP, poked);
#endif
    signal(SIGINT, poked);
#ifdef SIGQUIT
    signal(SIGQUIT, poked);
#endif
    signal(SIGTERM, poked);

    /* Initialize fillerup() variables */
    if ( SDL_OpenAudio(&wave.spec, NULL) < 0 ) {
        fprintf(stderr, "Couldn't open audio: %s\n", SDL_GetError());
        SDL_FreeWAV(wave.sound);
        exit(2);
    }
    SDL_PauseAudio(0);
/**/

/*** codeBlock1 ***/
    if ( ! done && (SDL_GetAudioStatus() == SDL_AUDIO_PLAYING) ) {
        SDL_Delay(1000);
    }
/**/

/*** codeBlock2 ***/
/**/

/*** codeBlock3 ***/
/**/

/*** codeBlock4 ***/
/**/

/*** wrapupBlock ***/
    /* Clean up on signal */
    SDL_CloseAudio();
    SDL_FreeWAV(wave.sound);
/**/

