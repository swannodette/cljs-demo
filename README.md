If you want to get up and running with a clojurescript REPL as fast as possible, this project is for you.

The only dependency of this project should be [Leiningen](https://github.com/technomancy/leiningen). Follow the instructions at that page to install it for your system. Leiningen is the build tool for clojure, similar to maven. 

Once you have leiningen installed, From this repo's directory run:

```shell
chmod +x run-repl.sh
./run-repl.sh
```

After you see the cljs repl, open index.html in the root of this repository in your favorite browser. You may need to refresh the browser if you don't get a response right away.

Now, in the shell where you ran repl-listen, your clojurescript repl is connected directly to the browser. You can try it by running

```clojure
(js/alert "Hey, it works!")
```

From here, you can interact with the DOM, or print to the console directly from the clojurescript repl.

Copyright © 2012 David Nolen, John K. Paul

Distributed under the Eclipse Public License, the same as Clojure.
