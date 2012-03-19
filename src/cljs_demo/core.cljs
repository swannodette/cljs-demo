;; proper namespaces
(ns cljs-demo.core
  (:use-macros [clojure.core.match.js :only [match]])
  (:require [clojure.string :as s]
            [cljs.reader :as r]
            [clojure.browser.repl :as repl]
            [domina :as d]
            [domina.css :as dc]))

;; browser connected REPL

(repl/connect "http://localhost:9000/repl")

;; Functions

(defn foo
  ([] :zero)
  ([a] :one)
  ([a b] :two)
  ([a b & rest] :variadic))

;; Data - Better Collections

(comment
  (def david {:first "David" :last "Nolen"})
  (def bob {:first "Bob" :last "Smith"})
  (def cathy {:first "Cathy" :last "White"})

  (map :first [david bob cathy])

  (def aset #{:cat :bird :dog :zebra})

  (conj aset :bird)

  ;; many data structures work as functions
  (def address {:street "101 Foo Ave."
                :city "New York"
                :state "NY"
                :zip 11211})

  ;; we can use hash maps as functions
  ;; there's no magic here, you can implement
  ;; such a type as well
  (map address [:street :zip])
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

;; Less WAT

(defn sensible-loops []
  (loop [i 0]
    (when (< i 10)
      (js/setTimeout (fn [] (println i)))
      (recur (inc i)))))

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
  ;; destructuring just works
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
                (throw (js/Error. "Index out of bounds"))
                not-found)))))

(comment
  ;; destructuring is just sugar over function calls!
  (let [[f & r] (make-mything :a :b)]
    f)
  )

(comment
  ;; fizzbuzz
  (doseq [n (range 1 101)]
    (println 
      (match [(mod n 3) (mod n 5)]
        [0 0] "FizzBuzz"
        [0 _] "Fizz"
        [_ 0] "Buzz"
        :else n)))

  ;; pattern matching red black trees
  (let [n [:black [:red [:red 1 2 3] 3 4] 5 6]]
     (match [n]
       [(:or [:black [:red [:red a x b] y c] z d]
             [:black [:red a x [:red b y c]] z d]
             [:black a x [:red [:red b y c] z d]]
             [:black a x [:red b y [:red c z d]]])] :balance
       :else :valid))

  ;; ~1s, we're not even using an optimal datastructure!
  (let [n [:black [:red [:red 1 2 3] 3 4] 5 6]]
    (dotimes [_ 1]
      (time
        (dotimes [_ 50000]
          (match [n]
             [(:or [:black [:red [:red a x b] y c] z d]
                   [:black [:red a x [:red b y c]] z d]
                   [:black a x [:red [:red b y c] z d]]
                   [:black a x [:red b y [:red c z d]]])] :balance
             :else :valid)))))
  )

;; Polymorphic Functions!

(defprotocol ISound
  (-sound [this]))

(deftype Cat []
  ISound
  (-sound [_] "meow!"))

(deftype Dog []
  ISound
  (-sound [_] "woof!"))

(extend-type default
  ISound
  (-sound [_] "... silence ..."))

(comment
  (-sound (Cat.))
  (-sound (Dog.))
  (-sound 1)

  (let [c (Cat.)]
    (dotimes [_ 5]
      (time
        (dotimes [_ 10000000]
          (-sound c)))))
  )

;; Extending Abstractions safely to native types

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

;; JSON.next
(comment
  (r/read-string "(+ 1 2)")
  (r/read-string "#{1 [2 3] {:foo :bar}}")
  (type (r/read-string "#{1 [2 3] {:foo :bar}}"))
  )