function c = configuration(varargin)
% CONIGURATION Constructor function for configuration object.
% 
% C = CONFIGURATION Returns an empty configuration object, with x, y, and 
% theta fields.
% 
% C = CONFIGURATION(O) Returns O, if O is an object.
% 
% C = CONFIGURATION(X, Y, THETA)  Returns a configuration object with x = X,
% y = Y, and theta = THETA.

switch nargin
case 0
    c.x = [];
    c.y = [];
    c.theta = [];
    c = class(c, 'configuration');
case 1
    if isa(varargin(1), 'configuration')
        c = varargin(1);
    else
        error('Wrong input type')
    end
case 3
    c.x = varargin(1);
    c.y = varargin(2);
    c.theta = varargin(3);
    c = class(c, 'configuration');
otherwise
    error('Wrong number of inputs');
end