function [retVal, flaRea, simTimRea, dblValRea ] = ...
    exchangeDoublesWithSocket(sockfd, flaWri, nDblRea, simTimWri, dblValWri)
% EXCHANGEDOUBLESWITHSOCKET - Exchanges data with the BCVTB
%
%  [retVal, flaRea, simTimRea, dblValRea] = exchangeDoublesWithSocket( ...
%                            sockfd, flaWri, nDblRea, simTimWri, dblValWri);
%  exchanges data with the BCVTB. 
%
%  The input arguments are:
%    sockfd    - Socket file descripter
%    flaWri    - Communication flag to write to the socket stream. Set to zero
%                for normal operation, or to a negative value to stop 
%                the exchange.
%    nDblRea   - Number of double values to read.
%    simTimWri - Current simulation time in seconds to write to the BCVTB.
%    dblValWri - Vector of double values to write to the BCVTB.
%
%  The return values are:
%    retVal    - A non-negative value if the data exchange was successfull,
%                or a negative value if an error occured.
%    flaRea    - Communication flag read from the socket stream. 
%                flaRea < 0 indicates that the BCVTB will stop due to an error
%                  and not send any more data.
%                flaRea == 0 is for normal operation.
%                flaRea == 1 indicates that the final simulation time has 
%                  been reached and no more data wil be exchanged.
%    simTimRea - Current simulation time in seconds read from the socket.
%    dblValRea - Vector of double values read from the socket.


%  Revision history
%  ----------------  
%  2009-06-26 MWetter@lbl.gov: First version, based on code of 
%             Charles Corbin, UC Boulder
  
  % Maximum number of double values. See defines.h for how to change it.
  NDBLMAX=1024;
  
  % Ensure that we will not attempt to read too many double values
  if ( nDblRea > NDBLMAX )
    msg=['Attempted to read ', int2str(nDblRea), ...
         ' double values which is above the limit of ', ...
         int2str(NDBLMAX), '.'];
    ME = MException('BCVTB:ConfigurationError', ...
                    msg);
    throw(ME);
    retVal = -1;
  end
  
  % Get pointer of arguments
  INT32PTR='int32Ptr';
  DBLPTR='doublePtr';
  myFlaWri = libpointer(INT32PTR, int32(flaWri));
  myFlaRea = libpointer(INT32PTR, int32(0));
  
  nDblWri = libpointer(INT32PTR, length(dblValWri));
  
  mySimTimWri = libpointer(DBLPTR, double(simTimWri));
  myDblValWri = libpointer(DBLPTR, double(dblValWri));
  
  mySimTimRea = libpointer(DBLPTR, double(0));
  myNDblRea   = libpointer(INT32PTR, int32(0));
  myDblValRea = libpointer(DBLPTR, double( zeros(1, nDblRea) ));

  % Exchange data
  retVal = calllib(getBCVTBLibName(),'exchangedoubleswithsocket', sockfd, ...
                   myFlaWri, myFlaRea, ...
                   nDblWri, ...
                   myNDblRea, ...
                   mySimTimWri, ...
                   myDblValWri, ...
                   mySimTimRea, ...
                   myDblValRea);
  VAL='Value';
  nDblReceived = get(myNDblRea, VAL);
  
  % Ensure that we read as many double values as expected
  if ( nDblReceived ~= nDblRea )
    msg=['Read ', int2str(nDblReceived), ...
         ' double values but expected to read ', int2str(nDblRea), '.'];
    ME = MException('BCVTB:ConfigurationError', ...
                    msg);
    throw(ME);
    retVal = -1;
  end
    
  % Get return values from pointers
  flaRea = get(myFlaRea, VAL);
  simTimRea = get(mySimTimRea, VAL);
  dblValRea = get(myDblValRea, VAL);
% end function
