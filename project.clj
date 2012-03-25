(defproject cljs-demo "0.1.0-SNAPSHOT"
  :description "ClojureScripting!"
  :dependencies [[org.clojure/clojure "1.4.0-beta3"]
                 [domina "1.0.0-alpha2"]
                 [org.clojure/core.match "0.2.0-alpha9"]
                 [org.clojure/tools.macro "0.1.1"]
                 [core.logic "0.6.9"]]
  :dev-dependencies [[lein-cljsbuild "0.1.3"]]
  :cljsbuild {:builds [{:source-path "src"
                        :compiler {:optimizations :simple
                                   :pretty-print true}}
                       #_{:source-path "src"
                        :compiler {:optimizations :advanced
                                   :output-to "mainadv.js"}}]})