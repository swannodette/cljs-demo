goog.provide('cljs_demo.core');
goog.require('cljs.core');
/**
* I don't do a whole lot.
* @param {...*} var_args
*/
cljs_demo.core._main = (function() { 
var _main__delegate = function (args){
return cljs.core.println.call(null,"Hello, World!");
};
var _main = function (var_args){
var args = null;
if (goog.isDef(var_args)) {
  args = cljs.core.array_seq(Array.prototype.slice.call(arguments, 0),0);
} 
return _main__delegate.call(this, args);
};
_main.cljs$lang$maxFixedArity = 0;
_main.cljs$lang$applyTo = (function (arglist__3075){
var args = cljs.core.seq( arglist__3075 );;
return _main__delegate.call(this, args);
});
return _main;
})()
;
