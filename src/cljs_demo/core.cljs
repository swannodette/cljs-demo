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

(comment
  (foo)
  (foo 1)
  (foo 1 2)
  (foo 1 2 3)
  )

;; Data - Better Collections

(comment
  (def david {:first "David" :last "Nolen"})
  (def bob {:first "Bob" :last "Smith"})
  (def cathy {:first "Cathy" :last "White"})

  (get david :first)
  (get david :last)
  (:first david)

  (map :first [david bob cathy])

  (get {[1 2] :next} [1 2])

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

(defn fib [a b] 
  (lazy-seq (cons a (fib b (+ b a)))))

(comment
  ;; don't do this
  (range)

  (take 10 (range))

  ;; 0-999
  (take 1000 (range))

  ;; positive integers! don't do this at the repl
  (def ints (map inc (range)))

  ;; Look Ma, no TCO!
  (take 10 (interleave (repeat "foo") (repeat "bar")))

  (take 10 (map #(* % 2) (map inc (range))))

  (take 36 (fib 0 1))

  ;; 3ms
  (time (last (take 1000 (fib 0 1))))

  ;; we didn't blow the stack!  
  (time (last (take 40000 (fib 0 1))))
  )

;; Destructuring & First Class Everything

(comment
  (def address {:street "101 Foo Ave."
                :city "New York"
                :state "NY"
                :zip 11211})

  ;; this is just sugar
  ;; we'll see how this works below
  (let [{:keys [street zip]} address]
    [street zip])
  )

;; Less WAT

(comment
  (if 0 true false)
  (if "" true false)

  (if nil true false)
  (if false true false)
  )

(defn sensible-loops []
  (loop [i 0]
    (when (< i 10)
      (js/setTimeout (fn [] (println i)))
      (recur (inc i)))))

(comment
  (sensible-loops)
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
  ;; new Foo(1, 2)
  (Foo. 1 2)
  (.-a (Foo. 1 2))
  (.-b (Foo. 1 2))

  (nth (MyThing. 1 2) 0)
  (nth (MyThing. 1 2) 1)

  ;; destructuring just works
  (let [[f & r] (MyThing. :a :b)]
    f)
  )

;; Self Documenting Closures!

;; function(a, b) {
;;   return {
;;     a: function ...
;;     b: function ...
;;   };
;; }

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

;; MACROS YEAH!

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
        (dotimes [_ 1000000]
          (-sound c)))))
  )

;; Extending Abstractions safely to native types

(extend-type js/Text
  IPrintable
  (-pr-seq [this options]
    (list "#<text>")))

(extend-type js/Element
  IPrintable
  (-pr-seq [this options]
    (let [id (.-id this)
          id-str (if (not (s/blank? id))
                   (str " id='" id "'") "")
          class (.-className this)
          class-str (if (not (s/blank? class)) 
                      (str " class='" class "'") "")]
      (list "#<" (.toLowerCase (.-tagName this)) id-str class-str ">"))))

(extend-type js/NodeList
  ISeqable
  (-seq [this]
    (IndexedSeq. this 0))
  IPrintable
  (-pr-seq [this options]
    (-pr-seq
     (filter (fn [x]
               (when-not (undefined? x)
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
  (.-childNodes body)

  (def box (d/single-node (dc/sel "#box")))
  (.-style box)

  (set! (.-height (.-style box)) "300px")
  (set! (.-height (.-style box)) "100px")
  (set! (.-backgroundColor (.-style box)) "blue")

  (.-style box)
  )

;; JSON.next
(comment
  (r/read-string "(+ 1 2)")
  (r/read-string "#{1 [2 3] {:foo :bar}}")
  (type (r/read-string "#{1 [2 3] {:foo :bar}}"))
  (pr-str #{1 [2 3] {:foo :bar}})
  )