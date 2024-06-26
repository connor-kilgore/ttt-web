// Compiled by ClojureScript 1.11.60 {:optimizations :none}
goog.provide('reagent.debug');
goog.require('cljs.core');
reagent.debug.has_console = (typeof console !== 'undefined');
reagent.debug.tracking = false;
if((typeof reagent !== 'undefined') && (typeof reagent.debug !== 'undefined') && (typeof reagent.debug.warnings !== 'undefined')){
} else {
reagent.debug.warnings = cljs.core.atom.call(null,null);
}
if((typeof reagent !== 'undefined') && (typeof reagent.debug !== 'undefined') && (typeof reagent.debug.track_console !== 'undefined')){
} else {
reagent.debug.track_console = (function (){var o = ({});
(o.warn = (function() { 
var G__6489__delegate = function (args){
return cljs.core.swap_BANG_.call(null,reagent.debug.warnings,cljs.core.update_in,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"warn","warn",-436710552)], null),cljs.core.conj,cljs.core.apply.call(null,cljs.core.str,args));
};
var G__6489 = function (var_args){
var args = null;
if (arguments.length > 0) {
var G__6490__i = 0, G__6490__a = new Array(arguments.length -  0);
while (G__6490__i < G__6490__a.length) {G__6490__a[G__6490__i] = arguments[G__6490__i + 0]; ++G__6490__i;}
  args = new cljs.core.IndexedSeq(G__6490__a,0,null);
} 
return G__6489__delegate.call(this,args);};
G__6489.cljs$lang$maxFixedArity = 0;
G__6489.cljs$lang$applyTo = (function (arglist__6491){
var args = cljs.core.seq(arglist__6491);
return G__6489__delegate(args);
});
G__6489.cljs$core$IFn$_invoke$arity$variadic = G__6489__delegate;
return G__6489;
})()
);

(o.error = (function() { 
var G__6492__delegate = function (args){
return cljs.core.swap_BANG_.call(null,reagent.debug.warnings,cljs.core.update_in,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"error","error",-978969032)], null),cljs.core.conj,cljs.core.apply.call(null,cljs.core.str,args));
};
var G__6492 = function (var_args){
var args = null;
if (arguments.length > 0) {
var G__6493__i = 0, G__6493__a = new Array(arguments.length -  0);
while (G__6493__i < G__6493__a.length) {G__6493__a[G__6493__i] = arguments[G__6493__i + 0]; ++G__6493__i;}
  args = new cljs.core.IndexedSeq(G__6493__a,0,null);
} 
return G__6492__delegate.call(this,args);};
G__6492.cljs$lang$maxFixedArity = 0;
G__6492.cljs$lang$applyTo = (function (arglist__6494){
var args = cljs.core.seq(arglist__6494);
return G__6492__delegate(args);
});
G__6492.cljs$core$IFn$_invoke$arity$variadic = G__6492__delegate;
return G__6492;
})()
);

return o;
})();
}
reagent.debug.track_warnings = (function reagent$debug$track_warnings(f){
(reagent.debug.tracking = true);

cljs.core.reset_BANG_.call(null,reagent.debug.warnings,null);

f.call(null);

var warns = cljs.core.deref.call(null,reagent.debug.warnings);
cljs.core.reset_BANG_.call(null,reagent.debug.warnings,null);

(reagent.debug.tracking = false);

return warns;
});

//# sourceMappingURL=debug.js.map
