function val = get(c, prop_name)
% GET Returns a property of a configuration object
% 
% VAL = GET(C, PROPERTY) Returns the PROPERTY of C which could be:
% 'X', 'Y', or 'Theta'

switch prop_name
case 'X'
    val = c.x{1,1};
case 'Y'
    val = c.y{1,1};
case 'Theta'
    val = c.theta{1,1};
otherwise
    error([prop_name 'is not a valid property']);
end