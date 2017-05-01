'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var WindowNavigatorMock = function WindowNavigatorMock() {
  _classCallCheck(this, WindowNavigatorMock);

  this._test = 'Navigator';
  this.userAgent = '';
};

exports.default = WindowNavigatorMock;