'use strict';

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

Object.defineProperty(exports, "__esModule", {
  value: true
});

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var ClassListMock = function () {
  function ClassListMock() {
    _classCallCheck(this, ClassListMock);

    this._test = 'ClassList';
    this._value = [];
  }

  _createClass(ClassListMock, [{
    key: 'add',
    value: function add(className) {
      for (var _len = arguments.length, nth = Array(_len > 1 ? _len - 1 : 0), _key = 1; _key < _len; _key++) {
        nth[_key - 1] = arguments[_key];
      }

      var a = [].concat.apply([], [className, nth]);
      this._value = this._value.concat(a);
    }
  }, {
    key: 'remove',
    value: function remove(className) {
      var a = this._value,
          n = [];
      for (var x in a) {
        if (a[x] !== className) {
          n.push(a[x]);
        }
      }
      this._value = n;
    }
  }, {
    key: 'toString',
    value: function toString() {
      return this._value.toString();
    }
  }]);

  return ClassListMock;
}();

exports.default = ClassListMock;