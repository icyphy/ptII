function r = getBCVTBLibName()
% GETBCVTBLIBNAME - Gets the name of the BCVTB library
%
%  This function returns the name of the BCVTB library, which is 
%  operating-system dependent.

%  Revision history
%  ----------------  
%  2009-06-26 MWetter@lbl.gov: First version.
if (strcmp(deblank(getenv('BCVTB_OS')),'windows')) % have Windows  
  r='bcvtb';
else
  r='libbcvtb';
end
