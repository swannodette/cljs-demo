(defproject cljs-demo "0.1.0-SNAPSHOT"
  :description "ClojureScripting!"
  :dependencies [[org.clojure/clojure "1.4.0-beta3"]]
  :cljsbuild {:builds [{:source-path "src"
                        :compiler {:output-to "main.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}]})