lein clean
lein deps 
lein cljsbuild once 
echo "After you see the cljs repl, open index.html in a browser"
echo "the cljs repl will look like"
echo 
echo
echo "ClojureScript:cljs.user>"
echo 
echo
echo "Once the page has finished loading, this repl can be used like the browser console"
rlwrap lein trampoline cljsbuild repl-listen 
