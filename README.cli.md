# cljs-demo


To get to a command line repl that runs code directly in the browser, rather than using Sublime Text 2, check the README.cli.md

From this repo's directory run:

```shell
lein deps
```

To get this project's dependencies.

In order to have the most pleasant workflow you should setup lein-cljsbuild to watch your ClojureScript files with

```shell
lein cljsbuild auto
```

in another shell
```shell
lein trampoline cljsbuild repl-listen
```

in another shell
```shell
open index.html || firefox index.html || chrome index.html
```

You may need to refresh the browser if you don't get a response right away.

Now, in the shell where you ran repl-listen, your clojurescript repl is connected directly to the browser. You can try it by running

```clojure
(js/alert "Hey, it works!")
```

From here, you can interact with the DOM, or print to the console directly from the clojurescript repl.

Copyright © 2012 David Nolen

Distributed under the Eclipse Public License, the same as Clojure.
