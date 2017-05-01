'use strict';

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

Object.defineProperty(exports, "__esModule", {
  value: true
});

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var WindowApplicationCacheMock = function () {
  function WindowApplicationCacheMock() {
    _classCallCheck(this, WindowApplicationCacheMock);

    this._test = 'ApplicationCache';
  }

  _createClass(WindowApplicationCacheMock, [{
    key: 'addEventListener',
    value: function addEventListener() {}
  }, {
    key: 'removeEventListener',
    value: function removeEventListener() {}
  }]);

  return WindowApplicationCacheMock;
}();

exports.default = WindowApplicationCacheMock;