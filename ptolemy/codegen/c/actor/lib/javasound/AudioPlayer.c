/***sharedBlock ***/
    struct AudioPlayer_sample {
        Uint8 *data;
        Uint32 dpos;
        Uint32 dlen;
    };
    
    // FIXME: how should we determine the buffer size??
    #define AudioPlayer_BUFFER_SIZE 32500   // ~50 KB buffer size

    // FIXME: what should we set the audio buffer size in samples equals to??
    #define AudioPlayer_SAMPLE_BUFFER_SIZE 16384

    atexit(SDL_Quit);
    
    double AudioPlayer_clip (double num) {
        return num > 1.0 ? 1.0 : num < -1.0 ? -1.0 : num;
    }
/**/

/*** preinitBlock_8 ***/
    int $actorSymbol(i), $actorSymbol(j);
    Uint8 $actorSymbol(convertedSample);    
    
    //const int _debug = 0;
    #define $actorSymbol(NUM_SOUNDS) $val(channels)    // mono or stereo
        
    struct AudioPlayer_sample $actorSymbol(sounds)[$actorSymbol(NUM_SOUNDS)];
        
    SDL_sem *$actorSymbol(sem);
    SDL_AudioSpec $actorSymbol(fmt);

    void $actorSymbol(mixaudio)(void *unused, Uint8 *stream, int len)
    {
        int i;
        int _resetSize = 0;
        Uint32 amount;
    
        //if (_debug) {
        //    fprintf(stdout, "*******************************mixaudio running (data_length= %d), (len= %d)\n", $actorSymbol(sounds)[0].dlen, len);
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
        
        for (i=0; i<_resetSize; i++) {
            if (SDL_SemPost($actorSymbol(sem)) == -1) {
                fprintf(stderr, "Error posting semaphor: %s\n",SDL_GetError());
                exit(1);
            }
        }
        ///////////// UNLOCK //////////////////////////////
    }

    void $actorSymbol(putSample) (Uint8 data, int channel) {
        //if (_debug) 
        //    fprintf(stdout, "putSample running (data= %d), (sem= %d)\n", data, SDL_SemValue($actorSymbol(sem)));
        /////////////// LOCK //////////////////////////////
        if (SDL_SemWait($actorSymbol(sem)) != 0) {
            fprintf(stderr, "Error waiting for semaphore: %s\n",SDL_GetError());
            exit(1);
        }
        /////////////// LOCK //////////////////////////////
        SDL_LockAudio();
    
        $actorSymbol(sounds)[channel].data[$actorSymbol(sounds)[channel].dlen] = data;

        //if (_debug) {
        //    fprintf(stdout, "putSample locked audio, (data= %d), (sound.data= %d)\n", data, $actorSymbol(sounds)[channel].data[sounds[channel].dlen]);
        //}
        $actorSymbol(sounds)[channel].dlen++;
    
        SDL_UnlockAudio();
    }
/**/


/*** preinitBlock_16 ***/    
    int $actorSymbol(i), $actorSymbol(j);
    Uint16 $actorSymbol(convertedSample);    
    
    //const int _debug = 0;
    #define $actorSymbol(NUM_SOUNDS) $val(channels)    // mono or stereo
        
    struct AudioPlayer_sample $actorSymbol(sounds)[$actorSymbol(NUM_SOUNDS)];
        
    SDL_sem *$actorSymbol(sem);
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
        _resetSize /= 2;
        //printf("resetSize = %d", _resetSize);
        for (i=0; i<_resetSize; i++) {
            if (SDL_SemPost($actorSymbol(sem)) == -1) {
                fprintf(stderr, "Error posting semaphor: %s\n",SDL_GetError());
                exit(1);
            }
        }
        ///////////// UNLOCK //////////////////////////////
    }

    void $actorSymbol(putSample) (Uint16 data, int channel) {
        //if (_debug) 
        //    fprintf(stdout, "putSample running (data= %d), (sem= %d)\n", data, SDL_SemValue(sem));
        /////////////// LOCK //////////////////////////////
        if (SDL_SemWait($actorSymbol(sem)) != 0) {
            fprintf(stderr, "Error waiting for semaphore: %s\n",SDL_GetError());
            exit(1);
        }
        /////////////// LOCK //////////////////////////////
        SDL_LockAudio();

        $actorSymbol(sounds)[channel].data[$actorSymbol(sounds)[channel].dlen] = (Uint8) data;    
        //if (_debug) {
        //    fprintf(stdout, "putSample locked audio, (data= %d), (sound.data= %d)\n", data >> 8, sounds[channel].data[sounds[channel].dlen]);
        //}
        $actorSymbol(sounds)[channel].dlen++;
        $actorSymbol(sounds)[channel].data[$actorSymbol(sounds)[channel].dlen] = (Uint8) (data >> 8);

        //if (_debug) {
        //    fprintf(stdout, "putSample locked audio, (data= %d), (sound.data= %d)\n", data, sounds[channel].data[sounds[channel].dlen]);
        //}
        $actorSymbol(sounds)[channel].dlen++;
    
        SDL_UnlockAudio();
    }
/**/


/*** initBlock ***/
    if (($actorSymbol(sem) = SDL_CreateSemaphore(AudioPlayer_BUFFER_SIZE)) == NULL) {
        fprintf(stderr, "Error creating semaphor: %s\n",SDL_GetError());
        exit(1);
    }

    /* Set 16-bit stereo audio at 22Khz */
    $actorSymbol(fmt).freq = $val(sampleRate);
    $actorSymbol(fmt).format = AUDIO_U$val(bitsPerSample);
    $actorSymbol(fmt).channels = $val(channels);
    $actorSymbol(fmt).samples = AudioPlayer_SAMPLE_BUFFER_SIZE;    
    $actorSymbol(fmt).callback = $actorSymbol(mixaudio);
    $actorSymbol(fmt).userdata = NULL;

    /* Open the audio device and start playing sound! */
    if ( SDL_OpenAudio(&$actorSymbol(fmt), NULL) < 0 ) {
        fprintf(stderr, "Unable to open audio: %s\n", SDL_GetError());
        exit(1);
    }
    for ( $actorSymbol(i)=0; $actorSymbol(i)<$actorSymbol(NUM_SOUNDS); ++$actorSymbol(i) ) {
        $actorSymbol(sounds)[$actorSymbol(i)].data = (Uint8*) malloc(AudioPlayer_BUFFER_SIZE);
        $actorSymbol(sounds)[$actorSymbol(i)].dpos = 0;
        $actorSymbol(sounds)[$actorSymbol(i)].dlen = 0;
    }
    SDL_PauseAudio(0);
/**/


/*** fireBlock_8 ***/
    for ( $actorSymbol(j)=0; $actorSymbol(j)<$actorSymbol(NUM_SOUNDS); ++$actorSymbol(j) ) {
        // convert sample
        //Use the following line if RINT() is not defined
        //convertedSample = floor($ref(input) * 128 + 127); //32767 = 2^15-1
        $actorSymbol(convertedSample) = rint((AudioPlayer_clip($ref(input)) * 128) + 127); //128 = 2^7
        $actorSymbol(putSample) ($actorSymbol(convertedSample), $actorSymbol(j));
    }
/**/

/*** fireBlock_16 ***/
    for ( $actorSymbol(j)=0; $actorSymbol(j)<$actorSymbol(NUM_SOUNDS); ++$actorSymbol(j) ) {
        $actorSymbol(convertedSample) = rint((AudioPlayer_clip($ref(input)) * 32768) + 32767); //32768 = 2^15
        $actorSymbol(putSample) ($actorSymbol(convertedSample), $actorSymbol(j));
    }
/**/

/*** wrapupBlock ***/
    //fprintf(stdout, "close audio\n");
    //SDL_PauseAudio(1);
    SDL_CloseAudio();

    //fprintf(stdout, "kill semaphore\n");
    if ($actorSymbol(sem) != NULL) {
        SDL_DestroySemaphore($actorSymbol(sem));
        $actorSymbol(sem) = NULL;
    }
    for ( $actorSymbol(j)=0; $actorSymbol(j)<$actorSymbol(NUM_SOUNDS); ++$actorSymbol(j) ) {
        SDL_FreeWAV($actorSymbol(sounds)[$actorSymbol(NUM_SOUNDS)].data);
    }
    //fprintf(stdout, "done\n");
/**/

