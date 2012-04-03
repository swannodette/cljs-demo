# cljs-demo

A demo ClojureScript project for BK.js and NYC.js. I recommend using this project with Sublime Text 2.
Install Sublime Text 2, install Sublime Text Package Control, use that to install the SublimeREPL package, 
and finally, install Leiningen 1.7.

From the Sublime Text 2 menu, Preferences > Browse Packages. You'll be taken to the Packages folder, open
SublimeREPL/config/Clojure/Main.sublime-menu. Add a new command that looks like this:

To get to a command line repl that runs code directly in the browser, rather than using Sublime Text 2, check the README.cli.md

```javascript
{"command": "repl_open", 
 "caption": "ClojureScript",
 "id": "repl_clojurescript",
 "args": {
    "type": "subprocess",
    "encoding": "utf8",
    "cmd": {"windows": ["lein", "trampoline", "cljsbuild", "repl-listen"],
            "linux": ["lein", "trampoline", "cljsbuild", "repl-listen"],
            "osx":  ["lein", "trampoline", "cljsbuild", "repl-listen"]},
    "soft_quit": "\n(. System exit 0)\n",
    "cwd": {"windows":"c:/Clojure", // where the lein.bat lives!
            "linux": "$file_path",
            "osx": "$file_path"},
    "syntax": "Packages/Clojure/Clojure.tmLanguage",
    "external_id": "clojure",
    "extend_env": {"INSIDE_EMACS": "1"}
    }
}
```

and save the file.

From this repo's directory run:

```shell
lein deps
```

To get this project's dependencies.

In order to have the most pleasant workflow you should setup lein-cljsbuild to watch your ClojureScript files with

```shell
lein cljsbuild auto
```

Now open the src/cljs_demo/core.cljs file from this repo. With the file focused you can start a Clojure SublimeREPL via
the menu Tools > SublimeREPL > Clojure > ClojureScript. You should see a new REPL. This REPL will hang until you open
index.html (you can it find at the root level of the repo) in a browser. Once the browser has loaded try to run a simple
expression like (+ 1 2) at the REPL. If you've successfully connected, you should see a proper return value for that 
expression. You may need to refresh the browser if you don't get a response right away.

F2+b will trigger evaluating the s-expression before the cursor. Evaluate the namespace expression first, this will put
the REPL in the right namespace. You can now evaluate the various forms in file at your leisure. Happy hacking!

Copyright © 2012 David Nolen

Distributed under the Eclipse Public License, the same as Clojure.
