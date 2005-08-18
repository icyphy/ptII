// AudioPlayer.c uses SDL to gain access to the target's audio hardware. 
// SDL is a cross-platform multimedia library designed to provide low level 
// hardware access. AudioPlayer.c uses a semaphore to prevent overfilling
// the user sound data buffer. The callback method post/signal the semaphore,
// while invoking putsample() in the fire code wait the semaphore.

/***sharedBlock ***/
    // Shared block contains code that is shared by multiple instances
    // of the helper from the same type, so it should not contain any
    // $actorSymbol(), $val(), $ref(), or any actor specific marcos.
    // Any method or type declarations should be prefixed with the actor
    // type name followed by an underscore (e.g. ActorName_method)./**/
    struct AudioPlayer_sample {
        Uint8 *data;            /* Pointer to wave data */
        Uint32 dataPosition;    /* Position of the next data to be mixed */
        Uint32 dataLength;      /* Length of wave data */
    };
    
    // FIXME: how should we determine the buffer size??
    #define AudioPlayer_BUFFER_SIZE 32500   // ~50 KB buffer size

    // FIXME: what should we set the audio buffer size in samples equals to??
    #define AudioPlayer_SAMPLE_BUFFER_SIZE 16384
    
    double AudioPlayer_clip (double num) {
        return num > 1.0 ? 1.0 : num < -1.0 ? -1.0 : num;
    }
/**/

/*** preinitBlock_8 ***/
    int $actorSymbol(i), $actorSymbol(j);
    Uint8 $actorSymbol(convertedSample);    
    
    //const int _debug = 0;
        
    struct AudioPlayer_sample $actorSymbol(sounds)[$val(channels)];
        
    SDL_sem *$actorSymbol(sem);
    SDL_AudioSpec $actorSymbol(fmt);

    // Callback method associated with this instance. It mixes the audio data
    // and then signals the semaphore.
    void $actorSymbol(mixaudio)(void *unused, Uint8 *stream, int len)
    {
        int i;
        int _resetSize = 0;
        Uint32 amount;
    
        //if (_debug) {
        //    fprintf(stdout, "*******************************mixaudio running (data_length= %d), (len= %d)\n", $actorSymbol(sounds)[0].dataLength, len);
        //}
    
        // mixing sound data into output stream
        for ( i=0; i<$val(channels); i++ ) {
            while ($actorSymbol(sounds)[i].dataLength > $actorSymbol(sounds)[i].dataPosition) {
                amount = ($actorSymbol(sounds)[i].dataLength - $actorSymbol(sounds)[i].dataPosition);
                if ( amount > len ) {
                    amount = len;
                }
                SDL_MixAudio(stream, &$actorSymbol(sounds)[i].data[$actorSymbol(sounds)[i].dataPosition], amount, SDL_MIX_MAXVOLUME);
                $actorSymbol(sounds)[i].dataPosition += amount;
            }
        }
        
        // reset dataPosition, so mixing would start from the beginning of the buffer next time 
        // reset dataLength, so we would put sample from the beginning
        for ( i=0; i<$val(channels); i++ ) {
            _resetSize += $actorSymbol(sounds)[i].dataLength;
            $actorSymbol(sounds)[i].dataLength = 0;
            $actorSymbol(sounds)[i].dataPosition = 0;
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

    // Wait for the semaphore, lock out the callback and put sample into
    // the sound data buffer.
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
    
        $actorSymbol(sounds)[channel].data[$actorSymbol(sounds)[channel].dataLength] = data;

        //if (_debug) {
        //    fprintf(stdout, "putSample locked audio, (data= %d), (sound.data= %d)\n", data, $actorSymbol(sounds)[channel].data[sounds[channel].dataLength]);
        //}
        $actorSymbol(sounds)[channel].dataLength++;
    
        SDL_UnlockAudio();
    }
/**/

// preinitBlock_16 block declares $actorSymbol(convertedSample) to be Uint16
// (instead of Uint8 in preinitBlock_8). The putSample() method chops the
// 16-bit sound sample into two and puts into the buffer two samples at a
// time, instead of one.
/*** preinitBlock_16 ***/    
    int $actorSymbol(i), $actorSymbol(j);
    Uint16 $actorSymbol(convertedSample);    
    
    //const int _debug = 0;
        
    struct AudioPlayer_sample $actorSymbol(sounds)[$val(channels)];
        
    SDL_sem *$actorSymbol(sem);
    SDL_AudioSpec $actorSymbol(fmt);

    // Callback method associated with this instance. It mixes the audio data
    // and then signals the semaphore.
    void $actorSymbol(mixaudio)(void *unused, Uint8 *stream, int len)
    {
        int i;
        int _resetSize = 0;
        Uint32 amount;
    
        //if (_debug) {
        //    fprintf(stdout, "*******************************mixaudio running (data_length= %d), (len= %d)\n", sounds[0].dataLength, len);
        //}
    
        // mixing sound data into output stream
        for ( i=0; i<$val(channels); i++ ) {
            while ($actorSymbol(sounds)[i].dataLength > $actorSymbol(sounds)[i].dataPosition) {
                amount = ($actorSymbol(sounds)[i].dataLength - $actorSymbol(sounds)[i].dataPosition);
                if ( amount > len ) {
                    amount = len;
                }
                SDL_MixAudio(stream, &$actorSymbol(sounds)[i].data[$actorSymbol(sounds)[i].dataPosition], amount, SDL_MIX_MAXVOLUME);
                $actorSymbol(sounds)[i].dataPosition += amount;
            }
        }
        // reset dataPosition, so mixing would start from the beginning of the buffer next time 
        // reset dataLength, so we would put sample from the beginning
        for ( i=0; i<$val(channels); i++ ) {
            _resetSize += $actorSymbol(sounds)[i].dataLength;
            $actorSymbol(sounds)[i].dataLength = 0;
            $actorSymbol(sounds)[i].dataPosition = 0;
        }
    
        ///////////// UNLOCK //////////////////////////////
        _resetSize /= 2;
        for (i=0; i<_resetSize; i++) {
            if (SDL_SemPost($actorSymbol(sem)) == -1) {
                fprintf(stderr, "Error posting semaphor: %s\n",SDL_GetError());
                exit(1);
            }
        }
        ///////////// UNLOCK //////////////////////////////
    }

    // Wait for the semaphore, lock out the callback and put sample into
    // the sound data buffer.
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

        $actorSymbol(sounds)[channel].data[$actorSymbol(sounds)[channel].dataLength] = (Uint8) data;    
        //if (_debug) {
        //    fprintf(stdout, "putSample locked audio, (data= %d), (sound.data= %d)\n", data >> 8, sounds[channel].data[sounds[channel].dataLength]);
        //}
        $actorSymbol(sounds)[channel].dataLength++;
        $actorSymbol(sounds)[channel].data[$actorSymbol(sounds)[channel].dataLength] = (Uint8) (data >> 8);

        //if (_debug) {
        //    fprintf(stdout, "putSample locked audio, (data= %d), (sound.data= %d)\n", data, sounds[channel].data[sounds[channel].dataLength]);
        //}
        $actorSymbol(sounds)[channel].dataLength++;
    
        SDL_UnlockAudio();
    }
/**/


/*** initBlock ***/
    /* Load the SDL library */
    if ( SDL_Init(SDL_INIT_AUDIO | SDL_INIT_EVENTTHREAD) < 0 ) {
        fprintf(stderr, "Couldn't initialize SDL: %s\n",SDL_GetError());
        exit(1);
    }

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
    for ( $actorSymbol(i)=0; $actorSymbol(i)<$val(channels); ++$actorSymbol(i) ) {
        $actorSymbol(sounds)[$actorSymbol(i)].data = (Uint8*) malloc(AudioPlayer_BUFFER_SIZE);
        $actorSymbol(sounds)[$actorSymbol(i)].dataPosition = 0;
        $actorSymbol(sounds)[$actorSymbol(i)].dataLength = 0;
    }
/**/


/*** fireBlock_8 ***/
    SDL_PauseAudio(0);      /* Start the callback */
    
    // FIXME: we should not put the same sample into different channels
    for ( $actorSymbol(j)=0; $actorSymbol(j)<$val(channels); ++$actorSymbol(j) ) {
        // Convert sample (Analog to Digital)
        // Input range [-1.0, 1.0] --> output range [0, 255]
        
        //Use the following line if RINT() is not defined
        //$actorSymbol(convertedSample) = floor((AudioPlayer_clip($ref(input)) * 127) + 128); //128 = 2^7
        $actorSymbol(convertedSample) = rint((AudioPlayer_clip($ref(input)) * 127) + 128); //128 = 2^7
        $actorSymbol(putSample) ($actorSymbol(convertedSample), $actorSymbol(j));
    }
/**/

/*** fireBlock_16 ***/
    SDL_PauseAudio(0);      /* Start the callback */

    // FIXME: we should not put the same sample into different channels
    for ( $actorSymbol(j)=0; $actorSymbol(j)<$val(channels); ++$actorSymbol(j) ) {
        // Convert sample (Analog to Digital)
        // Input range [-1.0, 1.0] --> output range [0, 65535]

        //Use the following line if RINT() is not defined
        //$actorSymbol(convertedSample) = floor((AudioPlayer_clip($ref(input)) * 32767) + 32768); //32768 = 2^15
        $actorSymbol(convertedSample) = rint((AudioPlayer_clip($ref(input)) * 32767) + 32768); //32768 = 2^15
        $actorSymbol(putSample) ($actorSymbol(convertedSample), $actorSymbol(j));
    }
/**/

/*** wrapupBlock ***/
    SDL_PauseAudio(1);      /* Stop the callback */
    SDL_CloseAudio();

    if ($actorSymbol(sem) != NULL) {
        SDL_DestroySemaphore($actorSymbol(sem));
        $actorSymbol(sem) = NULL;
    }
    for ( $actorSymbol(j)=0; $actorSymbol(j)<$val(channels); ++$actorSymbol(j) ) {
        free($actorSymbol(sounds)[$actorSymbol(j)].data);
    }
    SDL_Quit();
/**/

