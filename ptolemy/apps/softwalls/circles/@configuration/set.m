function c = set(c, varargin)
% SET Set configuration object properties
% 
% C = SET(C, 'Property1', Value1, 'Property2', Value2, ...) Sets the properties
% to the values given.
% 
% The following are valid properties:
% 'X', 'Y', 'Theta'.

property_argin = varargin;
while length(property_argin >= 2);
    prop = property_argin(1);
    val = property_argin(2);
    switch prop
    case 'X'
        c.x = val;
    case 'Y'
        c.y = val;
    case 'Theta'
        c.theta = val;
    otherwise
        error('Configuration properties: X, Y, Theta');
    end
end