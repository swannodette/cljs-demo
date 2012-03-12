(ns cljs-demo.core
  (:require [clojure.string :as s]
            [clojure.browser.repl :as repl]
            [domina :as d]
            [domina.css :as dc]))

(repl/connect "http://localhost:9000/repl")

(extend-type js/Element
  IPrintable
  (-pr-seq [this options]
    (let [id (.-id this)
          id-str (if (not (s/blank? id)) (str " id=\"" id "\"") "")
          class (.-className this)
          class-str (if (not (s/blank? class)) (str " class=\"" class "\"") "")]
      (if id
        (list "<"
              (.toLowerCase (.-tagName this))
              id-str
              class-str
              ">")))))

(extend-type js/NodeList
  ISeqable
  (-seq [this]
    (IndexedSeq. this 0))
  IPrintable
  (-pr-seq [this options]
    (-pr-seq (-seq this) options)))

(extend-type js/CSSStyleDeclaration
  ISeqable
  (-seq [this]
    (let [ks (js-keys this)]
      (into {}
        (->> ks
             (filter #(.hasOwnProperty this %))
             (map #(vector % (aget this %)))))))
  IPrintable
  (-pr-seq [this options]
    (-pr-seq (-seq this) options)))

(def box (d/single-node (dc/sel "#box")))

(comment
  ;; 100-200ms
  (let [ss (.-style box)]
   (dotimes [_ 10]
     (time
      (dotimes [_ 1e3] 
        (-seq ss)))))

  ;; 200-300ms
  (let [ss (.-style box)]
   (dotimes [_ 10]
     (time
      (dotimes [_ 1e4] 
        (js-keys ss)))))
  )