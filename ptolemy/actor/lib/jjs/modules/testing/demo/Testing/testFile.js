// Test commonHost accessor functionality.

// This file requires mocha and chai.  The Test accessor handles the requires().
// Note that chai's expect() does not work in strict mode; assert() and should() do.
var code, instance, a, b, c, d, e, f, g;

// FIXME:  Figure out how to move this requires statement to testing module.
var chai = require('testing/chai/chai.js');
var assert = chai.assert;        
var should = chai.should();

describe('Common host: Basic', function () {
        before(function() {
                // Read the accessor source code.
                code = getAccessorCode('test/TestAccessor');
                
                instance = new commonHost.Accessor('TestAccessor', code);
                
                // Invoke the initialize function.
                instance.initialize();
                
                // Examine the instance in JSON format.
                console.log('Instance of TestAccessor: %j\nTests:', instance);
        });

        
        it('this.getParameter() returns default value.', function(){
                instance.getParameter('p').should.equal(42);
        });
        
        it('this.getParameter() returns value set by this.setParameter().', function(){
                instance.setParameter('p', 12);
                instance.getParameter('p').should.equal(12);
        });
        
        it('this.get() returns default value.', function(){
                instance.get('numeric').should.equal(0);
        });

        it('this.get() returns null if no input yet provided.', function(){
                // Use asset for null check.  Can't use object.should since object
                // is null and null.should is an error. 
                assert(instance.get('untyped') === null);
        });
        
        it('this.get() returns null if no input yet provided but type being boolean.', function(){
                assert(instance.get('boolean') === null);
        });
        
        it('this.get() returns value provided by provideInput().', function(){
                instance.provideInput('boolean', true);
                instance.get('boolean').should.equal(true);
        });
        
        it('latestOutput contains value provided by inputHandlers and send().', function(){
                instance.react();
                instance.latestOutput('negation').should.equal(false);
        });
});

describe('Common host: Composite accessors', function(){
        before(function() {
                // Have to provide an implementation of this.instantiate(), which in this case will only
                // instantiate accessors founds in the accessors repo directory.
                code = getAccessorCode('test/TestComposite');
                a = new commonHost.Accessor('TestComposite', code, getAccessorCode);
        });
        
        it('Composite accessor produces correct output when manually scheduled.', function(){
                a.initialize();
                a.provideInput('input', 10);
                a.containedAccessors[0].react();
                a.containedAccessors[1].react();
                a.latestOutput('output').should.equal(50);
        });
        
        it('Composite accessor produces correct output when automatically scheduled.', function(){
                a.initialize();
                a.provideInput('input', 5);
                a.react();
                a.latestOutput('output').should.equal(25);
        });
});

describe('Common host: Spontaneous accessors', function(){
        // Increase default mocha timeout (originally 2000).  See https://mochajs.org/#timeouts
    this.timeout(3000);
    
        before(function() {
                b = commonHost.instantiateAccessor('TestSpontaneous', 'test/TestSpontaneous',
                        getAccessorCode);
                c = commonHost.instantiateAccessor(
                        'TestCompositeSpontaneous', 'test/TestCompositeSpontaneous', getAccessorCode);
                
        });
        
        it('Spontaneous accessor produces correct outputs after 1 and 2 seconds.', function(done){
                // Note that the following two tests will run concurrently (!)
                // initialize() must go in the test case, not in before(), since a 
                // spontaneous accessor will start producing inputs after initialize().
                b.initialize();
                setTimeout(function() {
                        b.latestOutput('output').should.equal(0);
                }, 1500);
                
                setTimeout(function() {
                        b.latestOutput('output').should.equal(1);
                    b.wrapup();
                    done();
                }, 2500);
        });
        
        it('Composite spontaneous accessor produces correct outputs after 1 and 2 seconds.', function(done){
                // Note that the following two tests will run concurrently (!)
                // initialize() must go in the test case, not in before(), since a 
                // spontaneous accessor will start producing inputs after initialize().
                c.initialize();
                setTimeout(function() {
                        c.latestOutput('output').should.equal(0);
                }, 1500);
                setTimeout(function() {
                        c.latestOutput('output').should.equal(4);
                    c.wrapup();
                    done();
                }, 2500);
        });
});
        
describe('Common host: Inheritance', function(){
        before(function(){
                d = commonHost.instantiateAccessor(
                        'TestInheritance', 'test/TestInheritance', getAccessorCode);
                d.initialize();
                e = commonHost.instantiateAccessor(
                        'TestImplement', 'test/TestImplement', getAccessorCode);
                e.initialize();
                f = commonHost.instantiateAccessor(
                        'TestDerivedC', 'test/TestDerivedC', getAccessorCode);
                f.initialize();
                g = commonHost.instantiateAccessor(
                        'TestDerivedAgainA', 'test/TestDerivedAgainA', getAccessorCode);
                g.initialize();
        });
                
        it('Inheritance, function overriding, and variable visiility should work properly.', function(){
                d.provideInput('untyped', 'foo');
                d.react();
                d.latestOutput('jsonOfUntyped').should.equal('hello');
        });
                
        it('Implementing an interface should work properly.', function(){
                e.provideInput('numeric', '42');
                e.react();
                e.latestOutput('numericPlusP').should.equal(84);
        });
                
        it('Exported fields of base classes can be accessed and initialize() is properly scoped.', function(){
                f.initialize();
                f.provideInput('in1', '42');
                f.react();
                f.latestOutput('out1').should.equal(2);
        });
                
        it('Two-level inheritance should work properly.', function(){
                g.provideInput('in1', 42);
                g.react();
                g.latestOutput('out1').should.equal(2);
                g.latestOutput('out2').should.equal(2);
        });
});
