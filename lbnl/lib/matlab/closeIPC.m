function retVal = closeIPC(sockfd)
% CLOSEIPC - Closes the interprocess communication
%
%   status = closeIPC(sockfd) closes the socket 
%   connection with file descriptor sockfd.
%   If successful, retVal will be zero.

%  Revision history
%  ----------------  
%  2009-09-20 Metter@lbl.gov: Removed warning if bcvtb library is not loaded.
%  2009-06-26 MWetter@lbl.gov: First version, based on code of 
%             Charles Corbin, UC Boulder
if (sockfd < 0 )
  fprintf('Warning: Cannot close socket in closeIPC(%d) because argument is negative', sockfd);
  retVal = -1;
elseif ~libisloaded(getBCVTBLibName())
  % bcvtb library is not loaded
  retVal = -2;
else
  retVal = calllib(getBCVTBLibName(),'closeipc',int16(sockfd));
end
