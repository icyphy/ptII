'use strict';

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _windowNodeMock = require('./window-node-mock');

var _windowNodeMock2 = _interopRequireDefault(_windowNodeMock);

var _windowFragmentMock = require('./window-fragment-mock');

var _windowFragmentMock2 = _interopRequireDefault(_windowFragmentMock);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var WindowDocumentMock = function () {
  function WindowDocumentMock() {
    _classCallCheck(this, WindowDocumentMock);

    this._test = 'Document';
    this.body = new _windowNodeMock2.default();
  }

  _createClass(WindowDocumentMock, [{
    key: 'createDocumentFragment',
    value: function createDocumentFragment() {
      return new _windowFragmentMock2.default();
    }
  }, {
    key: 'createElement',
    value: function createElement() {
      return new _windowNodeMock2.default();
    }
  }, {
    key: 'querySelector',
    value: function querySelector() {
      return new _windowNodeMock2.default();
    }
  }, {
    key: 'querySelectorAll',
    value: function querySelectorAll() {
      return [new _windowNodeMock2.default()];
    }
  }]);

  return WindowDocumentMock;
}();

exports.default = WindowDocumentMock;