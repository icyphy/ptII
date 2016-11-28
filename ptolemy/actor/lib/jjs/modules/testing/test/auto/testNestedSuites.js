// Test commonHost accessor functionality.

// This file requires mocha and chai.  The Test accessor handles the requires().
// Note that chai's expect() does not work in strict mode; assert() and should() do.
var code, instance, a, b, c, d, e, f, g;

// FIXME:  Figure out how to move this requires statement to testing module.
var chai = require('testing/chai/chai.js');
var assert = chai.assert;        
var should = chai.should();


describe('Outer suite', function () {
    before(function() {
        // Read the accessor source code.
        code = getAccessorCode('test/TestAccessor');
        
        instance = new commonHost.Accessor('TestAccessor', code);
        
        // Invoke the initialize function.
        instance.initialize();
        
    });
    
    describe('Inner suite 1', function () {
        before(function() {
            // Read the accessor source code.
            code = getAccessorCode('test/TestAccessor');
            
            instance = new commonHost.Accessor('TestAccessor', code);
            
            // Invoke the initialize function.
            instance.initialize();
            
        });
        
        it('Passing inner 1', function(){
            instance.getParameter('p').should.equal(42);
        });
        
        it('Failing inner 1', function(){
            instance.get('numeric').should.equal(1);
        });        
        
        describe('Inner suite 1.1', function () {
            before(function() {
                // Read the accessor source code.
                code = getAccessorCode('test/TestAccessor');
                
                instance = new commonHost.Accessor('TestAccessor', code);
                
                // Invoke the initialize function.
                instance.initialize();
                
            });
            
            it('Passing inner 1.1', function(){
                instance.getParameter('p').should.equal(42);
            });
            
            it('Failing inner 1.1', function(){
                instance.get('numeric').should.equal(1);
            });                
        });
        
        describe('Inner suite 1.2', function () {
            before(function() {
                // Read the accessor source code.
                code = getAccessorCode('test/TestAccessor');
                
                instance = new commonHost.Accessor('TestAccessor', code);
                
                // Invoke the initialize function.
                instance.initialize();
                
            });
            
            describe('Inner suite 1.2.1', function () {
                before(function() {
                    // Read the accessor source code.
                    code = getAccessorCode('test/TestAccessor');
                    
                    instance = new commonHost.Accessor('TestAccessor', code);
                    
                    // Invoke the initialize function.
                    instance.initialize();
                    
                });
                
                it('Passing inner 1.2.1', function(){
                    instance.getParameter('p').should.equal(42);
                });
                
                it('Failing inner 1.2.1', function(){
                    instance.get('numeric').should.equal(1);
                });                
            });
            
            it('Passing inner 1.2', function(){
                instance.getParameter('p').should.equal(42);
            });
            
            it('Failing inner 1.2', function(){
                instance.get('numeric').should.equal(1);
            });                
        });
    });
    
    describe('Inner suite 2', function () {
        before(function() {
            // Read the accessor source code.
            code = getAccessorCode('test/TestAccessor');
            
            instance = new commonHost.Accessor('TestAccessor', code);
            
            // Invoke the initialize function.
            instance.initialize();
            
        });
        
        it('Passing inner 2', function(){
            instance.getParameter('p').should.equal(42);
        });
        
        it('Failing inner 2', function(){
            instance.get('numeric').should.equal(1);
        });                
    });
    
    
    describe('Inner suite 3 no tests', function () {
        
    });
    
    
    it('Passing outer', function(){
        instance.getParameter('p').should.equal(42);
    });
    
    it('Failing outer', function(){
        instance.get('numeric').should.equal(1);
    });        
});

