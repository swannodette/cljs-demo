;; proper namespaces
(ns cljs-demo.core
  (:require [clojure.browser.repl :as repl]
            [domina :as domina]
            [domina.css :as domina.css]))

;; browser connected REPL
(repl/connect "http://localhost:9000/repl")

(defn setup-example [] 

  (def body (domina/single-node (domina.css/sel "body")))
  (.-childNodes body)

  (def box (domina/single-node (domina.css/sel "#box")))
  (.-style box)
  (get-next-page))

(defn get-next-page []
    (let [pres (domina/nodes (domina.css/sel "pre"))
          first-pre (first pres)
          second-pre (second pres)]
    (set! (.-display (.-style first-pre)) "none")
    (set! (.-display (.-style second-pre)) "block"))
      )
