(ns cljs-demo.core
  (:use-macros [clojure.core.match.js :only [match]])
  (:require [clojure.string :as s]
            [clojure.browser.repl :as repl]
            [domina :as d]
            [domina.css :as dc]))

(repl/connect "http://localhost:9000/repl")

;; Functions

(defn foo
  ([] :zero)
  ([a] :one)
  ([a b] :two)
  ([a b & rest] :variadic))

;; Less WAT

(defn sensible-loops []
  (loop [i 0]
    (when (< i 10)
      (js/setTimeout (fn [] (println i)))
      (recur (inc i)))))

;; Better Collections

(comment
  (def david {:first "David" :last "Nolen"})
  (def bob {:first "Bob" :last "Smith"})
  (def cathy {:first "Cathy" :last "White"})

  (map :first [david bob cathy])

  (def aset #{:cat :bird :dog :zebra})
  )

;; Lazy Sequences

(comment
  ;; Look Ma, no TCO!
  (take 1000 (interleave (repeat "foo") (repeat "bar")))
  )

;; First Class Everything

(comment
  (def address {:street "101 Foo Ave."
                :city "New York"
                :state "NY"
                :zip 11211})

  ;; this is just sugar
  (let [{:keys [street zip]} address]
    [street zip])
  )

;; Making a type

(deftype Foo [a b])

(deftype MyThing [a b]
  ISeqable
  (-seq [_]
    (list a b))
  IIndexed
  (-nth [coll n]
    (-nth coll n ::error))
  (-nth [coll n not-found]
    (condp = n
      0 a
      1 b
      :else (if (= not-found ::error) 
              (throw (js/Error. "Index out of boudns"))
              not-found))))

(comment
  (let [[f & r] (MyThing. :a :b)]
    f)
  )

;; Self Documenting Closures!

(defn make-mything [a b]
  (reify IIndexed
    ISeqable
    (-seq [_]
      (list a b))
    (-nth [coll n]
      (-nth coll n ::error))
    (-nth [coll n not-found]
      (condp = n
        0 a
        1 b
        :else (if (= not-found ::error) 
                (throw (js/Error. "Index out of boudns"))
                not-found)))))

(comment
  (let [[f & r] (make-mything :a :b)]
    f)
  )

;; Macros FTW, Scala/Haskell style pattern matching

(comment
  (doseq [n (range 1 101)]
    (println 
      (match [(mod n 3) (mod n 5)]
        [0 0] "FizzBuzz"
        [0 _] "Fizz"
        [_ 0] "Buzz"
        :else n)))
  )

;; Extending Abstractions

(extend-type js/Text
  IPrintable
  (-pr-seq [this options]
    (list "<text>")))

(extend-type js/Element
  IPrintable
  (-pr-seq [this options]
    (let [id (.-id this)
          id-str (if (not (s/blank? id)) 
                   (str " id='" id "'") "")
          class (.-className this)
          class-str (if (not (s/blank? class)) 
                      (str " class='" class "'") "")]
      (list "<"
            (str (.toLowerCase (.-tagName this))
                 id-str class-str)
            ">"))))

(extend-type js/NodeList
  ISeqable
  (-seq [this]
    (IndexedSeq. this 0))
  IPrintable
  (-pr-seq [this options]
    (-pr-seq
     (filter (fn [x]
               (if (not (undefined? x))
                   x))
             (-seq this))
     options)))

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

(comment
  (def body (d/single-node (dc/sel "body")))
  (def box (d/single-node (dc/sel "#box")))
  )