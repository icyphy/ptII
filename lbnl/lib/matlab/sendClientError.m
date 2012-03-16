function retVal = sendClientError(sockfd, flaWri)
% SENDCLIENTERROR - Sends an error flag to the BCVTB.
%
%   retVal = sendClientError(sockfd, flaWri) sends flaWri
%   to the BCVTB through the socket with file descriptor sockfd.
%   Set flaWri to a negative value when calling this function.
%   If successful, retVal will be zero.

%  Revision history
%  ----------------  
%  2009-06-26 MWetter@lbl.gov: First version.
  
if (sockfd < 0 )
  fprintf('Warning: Cannot close socket in closeIPC(%d) because argument is negative', sockfd);
  retVal = -1;
elseif ~libisloaded(getBCVTBLibName())
  fprintf('Warning: Cannot close socket in closeIPC because BCVTB library is not loaded');
  retVal = -2;
else
  retVal = calllib(getBCVTBLibName(),'sendclienterror',int16(sockfd), int16(flaWri));
end
