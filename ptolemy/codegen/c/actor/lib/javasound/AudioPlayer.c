/***structBlock ***/
/**/

/*** preinitBlock(<bits>) ***/
    int $actorSymbol(i), $actorSymbol(j);
    Uint<bits> $actorSymbol(convertedSample);    
    
    //const int _debug = 0;
    const int $actorSymbol(NUM_SOUNDS) = $val(channels);    // mono or stereo
    // FIXME: how should we determine the buffer size??
    const int $actorSymbol(BUFFER_SIZE) = 32750*<bits>/8;   // ~50 KB buffer size
        
    struct sample {
        Uint8 *data;
        Uint32 dpos;
        Uint32 dlen;
    } $actorSymbol(sounds)[$actorSymbol(NUM_SOUNDS)];
        
    SDL_sem *$actorSymbol(sem);
    SDL_mutex *$actorSymbol(mut);
    SDL_AudioSpec $actorSymbol(fmt);

    void $actorSymbol(mixaudio)(void *unused, Uint8 *stream, int len)
    {
        int i;
        int _resetSize = 0;
        Uint32 amount;
    
        //if (_debug) {
        //    fprintf(stdout, "*******************************mixaudio running (data_length= %d), (len= %d)\n", sounds[0].dlen, len);
        //}
    
        // mixing sound data into output stream
        for ( i=0; i<$actorSymbol(NUM_SOUNDS); i++ ) {
            while ($actorSymbol(sounds)[i].dlen > $actorSymbol(sounds)[i].dpos) {
                amount = ($actorSymbol(sounds)[i].dlen - $actorSymbol(sounds)[i].dpos);
                if ( amount > len ) {
                    amount = len;
                }
                SDL_MixAudio(stream, &$actorSymbol(sounds)[i].data[$actorSymbol(sounds)[i].dpos], amount, SDL_MIX_MAXVOLUME);
                $actorSymbol(sounds)[i].dpos += amount;
            }
        }
        // reset DPOS, so mixing would start from the beginning of the buffer next time 
        // reset DLEN, so we would put sample from the beginning
        for ( i=0; i<$actorSymbol(NUM_SOUNDS); i++ ) {
            _resetSize += $actorSymbol(sounds)[i].dlen;
            $actorSymbol(sounds)[i].dlen = 0;
            $actorSymbol(sounds)[i].dpos = 0;
        }
    
        ///////////// UNLOCK //////////////////////////////
        
        for (i=0; i<_resetSize*<bits>/8; i++) {
            if (SDL_SemPost($actorSymbol(sem)) == -1) {
                fprintf(stderr, "Error posting semaphor: %s\n",SDL_GetError());
                exit(1);
            }
        }
        ///////////// UNLOCK //////////////////////////////
    }
/**/

/*** preinitBlock_8 ***/    
    void $actorSymbol(putSample) (Uint8 data, int channel) {
        int i;
        //if (_debug) 
        //    fprintf(stdout, "putSample running (data= %d), (sem= %d)\n", data, SDL_SemValue(sem));
        /////////////// LOCK //////////////////////////////
        if (SDL_SemWait($actorSymbol(sem)) != 0) {
            fprintf(stderr, "Error waiting for semaphore: %s\n",SDL_GetError());
            exit(1);
        }
        /////////////// LOCK //////////////////////////////
        SDL_LockAudio();
    
        $actorSymbol(sounds)[channel].data[$actorSymbol(sounds)[channel].dlen] = data;

        //if (_debug) {
        //    fprintf(stdout, "putSample locked audio, (data= %d), (sound.data= %d)\n", data, sounds[channel].data[sounds[channel].dlen]);
        //}
        $actorSymbol(sounds)[channel].dlen++;
    
        //if (_debug) {
        //    fprintf(stdout, "putSample unlocked audio\n");
        //}
        SDL_UnlockAudio();
    }
/**/


/*** preinitBlock_16 ***/    
    void $actorSymbol(putSample) (Uint16 data, int channel) {
        int i;
        //if (_debug) 
        //    fprintf(stdout, "putSample running (data= %d), (sem= %d)\n", data, SDL_SemValue(sem));
        /////////////// LOCK //////////////////////////////
        if (SDL_SemWait($actorSymbol(sem)) != 0) {
            fprintf(stderr, "Error waiting for semaphore: %s\n",SDL_GetError());
            exit(1);
        }
        /////////////// LOCK //////////////////////////////
        SDL_LockAudio();
    
        $actorSymbol(sounds)[channel].data[$actorSymbol(sounds)[channel].dlen] = (data >> 8);
        //if (_debug) {
        //    fprintf(stdout, "putSample locked audio, (data= %d), (sound.data= %d)\n", data >> 8, sounds[channel].data[sounds[channel].dlen]);
        //}
        $actorSymbol(sounds)[channel].dlen++;
        $actorSymbol(sounds)[channel].data[$actorSymbol(sounds)[channel].dlen] = data;

        //if (_debug) {
        //    fprintf(stdout, "putSample locked audio, (data= %d), (sound.data= %d)\n", data, sounds[channel].data[sounds[channel].dlen]);
        //}
        $actorSymbol(sounds)[channel].dlen++;
    
        //if (_debug) {
        //    fprintf(stdout, "putSample unlocked audio\n");
        //}
        SDL_UnlockAudio();
    }
/**/



/*** initBlock ***/
    if (($actorSymbol(sem) = SDL_CreateSemaphore($actorSymbol(BUFFER_SIZE)/2)) == NULL) {
        fprintf(stderr, "Error creating semaphor: %s\n",SDL_GetError());
        exit(1);
    }

    /* Set 16-bit stereo audio at 22Khz */
    $actorSymbol(fmt).freq = $val(sampleRate);
    $actorSymbol(fmt).format = AUDIO_U$val(bitsPerSample);
    $actorSymbol(fmt).channels = $val(channels);
    $actorSymbol(fmt).samples = 20000;    // FIXME: don't know the proper value for this
    $actorSymbol(fmt).callback = $actorSymbol(mixaudio);
    $actorSymbol(fmt).userdata = NULL;

    /* Open the audio device and start playing sound! */
    if ( SDL_OpenAudio(&$actorSymbol(fmt), NULL) < 0 ) {
        fprintf(stderr, "Unable to open audio: %s\n", SDL_GetError());
        exit(1);
    }
    for ( $actorSymbol(i)=0; $actorSymbol(i)<$actorSymbol(NUM_SOUNDS); ++$actorSymbol(i) ) {
        $actorSymbol(sounds)[$actorSymbol(i)].data = (Uint8*) malloc($actorSymbol(BUFFER_SIZE));
        $actorSymbol(sounds)[$actorSymbol(i)].dpos = 0;
        $actorSymbol(sounds)[$actorSymbol(i)].dlen = 0;
    }
    SDL_PauseAudio(0);
/**/


/*** fireBlock(<bits>) ***/
    for ( $actorSymbol(j)=0; $actorSymbol(j)<$actorSymbol(NUM_SOUNDS); ++$actorSymbol(j) ) {
        // convert sample
        //Use the following line if RINT() is not defined
        //convertedSample = floor($ref(input) * 32767); //32767 = 2^15-1
        $actorSymbol(convertedSample) = rint(($ref(input) * (pow(2, <bits> - 1) - 1)) + (pow(2, <bits> - 1) - 1)); //2^(bits-1)-1
        $actorSymbol(putSample) ($actorSymbol(convertedSample), $actorSymbol(j));
    }
/**/

/*** wrapupBlock ***/
    fprintf(stdout, "close audio\n");
    //SDL_PauseAudio(1);
    SDL_CloseAudio();

    fprintf(stdout, "kill semaphore\n");
    if ($actorSymbol(sem) != NULL) {
        SDL_DestroySemaphore($actorSymbol(sem));
        $actorSymbol(sem) = NULL;
    }
    for ( $actorSymbol(j)=0; $actorSymbol(j)<$actorSymbol(NUM_SOUNDS); ++$actorSymbol(j) ) {
        SDL_FreeWAV($actorSymbol(sounds)[$actorSymbol(NUM_SOUNDS)].data);
    }
    fprintf(stdout, "done\n");
/**/

