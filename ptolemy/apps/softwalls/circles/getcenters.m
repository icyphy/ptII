function [leftposition, rightposition] = getcenters(position,theta)
%[L,R] = GETCENTERS(POSITION,THETA)
%
%   Given the (x,y) (=) position and heading theta of the aircraft, it 
%   calculates the centers of the 2 circles used in flight calculation.
%
%   L corresponds to the circle on the aircraft's left and R to the right.

offset = [0.15;
    0];

leftposition = rotmatrix(theta + pi/2) * offset + position;

rightposition = rotmatrix(theta - pi/2) * offset + position;

% end getcenters

%
%=============================================================================
% rotmatrix
% Returns a 2D rotation matrix.
%=============================================================================
%


function R = rotmatrix(theta)
%R = ROTMATRIX(THETA)
%
%   Returns a 2D rotation matrix (in SO(2)) corresponding to right-handed
%   rotation by theta.

R = [cos(theta) -sin(theta);
    sin(theta) cos(theta)];

% end rotmatrix