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

(comment
  ;; symbols are automatically namespaced for you by the compiler
  (def x 5)
  (in-ns 'cljs.user)
  (identity x)
  (in-ns 'cljs-demo.core)
  (identity x)
  )

;; Functions

(defn foo
  ([] :zero)
  ([a] :one)
  ([a b] :two)
  ([a b & rest] :variadic))

(comment
  ;; would be much more tedious in JS
  ;; arguments boilerplate

  (foo)
  (foo 1)
  (foo 1 2)
  (foo 1 2 3)
  (foo 1 2 3 4)

  ;; anonymous functions, nice and short
  (fn [x] (+ x 1))
  )

;; Data - Better Collections

(comment
  ;; In JavaScript, Objects do double duty as types and maps

  ;; real hashmaps
  (def david {:first "David" :middle "E" :last "Nolen"})
  (def bob {:first "Bob" :middle "J" :last "Smith"})
  (def cathy {:first "Cathy" :middle "S" :last "White"})

  ;; accessing properties, notice this is first class access
  (get david :first)
  (get david :last)

  ;; keywords are functions
  (:first david)

  (map :first [david bob cathy])

  ;; there are large number of awesome functions
  (map (juxt :last :first) [david bob cathy])

  ;; arbitrary keys
  (get {[1 2] :next-level} [1 2])

  ;; real sets
  (def aset #{:cat :bird :dog :zebra})

  (conj aset :bird)

  ;; many data structures work as functions
  (def address {:street "101 Foo Ave."
                :city "New York"
                :state "NY"
                :zip 11111})

  ;; we can use hash maps as functions
  ;; there's no magic here, you can implement
  ;; your function-like type as well
  (map address [:street :zip])

  ;; normally in JS we need something like Underscore.js
  ;; even then, still verbose, wrapping in functions
  ;; so much for beautiful OOP

  ;; Deep equality is the default!
  (= david {:last "Nolen" :middle "E" :first "David"})
  (= '(1 2 3) [1 2 3])
  (= #{1 3 2} #{2 3 1})
  )

(comment
  ;; JavaScript Destructuring Assignment
  ;;
  ;; var a = 1;  
  ;; var b = 3;  
  ;; [a, b] = [b, a];

  (def address {:street "101 Foo Ave."
                :city "New York"
                :state "NY"
                :zip 11211})

  ;; this is just sugar
  ;; we'll see how this works below
  (let [{city :city} address]
    city)

  ;; nested destructuring
  (let [{[_ c] :city} address]
    c)

  (let [{[_ c :as city] :city} address]
    [c city])

  (name->str ["Nolen" "David"])

  ;; advanced example
  (->> [david bob cathy]
    (map (juxt :last :first))
    (map name->str))
  )

;; Destructuring & First Class Everything
(defn name->str [[last first]]
  (str last ", " first))

;; Less WAT

(comment
  ;; 0 and "" are not truthy
  (if 0 true false)
  (if "" true false)

  ;; only nil and false are falsey
  (if nil true false)
  (if false true false)
  )

(defn sensible-loops []
  (loop [i 0]
    (when (< i 10)
      (js/setTimeout (fn [] (println i)))
      (recur (inc i)))))

(comment
  ;; ClojureScript generate optimal loops that properly
  ;; close over loop locals
  (sensible-loops)
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

  ;; Look Ma, no TCO!
  (take 1000 (interleave (repeat "foo") (repeat "bar")))

  (take 10 (map #(* % 2) (map inc (range))))

  (take 50 (fib 0 1))

  ;; 3ms
  (time (last (take 1000 (fib 0 1))))

  ;; we didn't blow the stack!  
  (time (last (take 300000 (fib 0 1))))
  )

;; Making a type

(deftype Foo [a b])

(comment
  ;; In JavaScript
  ;;
  ;; function Foo(a, b) {
  ;;   this.a = a;
  ;;   this.b = b;
  ;; }

  ;; new Foo(1, 2)
  (Foo. 1 2)

  ;; access fields of a type
  (.-a (Foo. 1 2))
  (.-b (Foo. 1 2))
  )

;; We always extend *existing* abstractions
;; what do we mean by "abstraction"?

(comment
  ;; Standard iteration protocol

  ;; we can convert array into ClojureScript sequences
  (seq (array 1 2 3 4 5))

  ;; first does this and gives the first element
  (first (array 1 2 3 4 5))
  ;; rest does this and gives us the rest of the sequence
  (rest (array 1 2 3 4 5))

    ;; strings work!
  (first "David Nolen")
  (rest "David Nolen")

  ;; arrays can be converted into Seqs!
  (satisfies? ISeqable (array))
  ;; but not numbers
  (satisfies? ISeqable 1)

  ;; maps too
  (first {:first "David" :last "Nolen"})
  (rest {:first "David" :last "Nolen"})

  ;; everything works as long as the type
  ;; implements -seq
  (-seq "David Nolen")

  ;; this is the same as if the JS object had
  ;; a method _seq - duck typing FTW!
  )

(deftype MyThing [a b]
  ISeqable
  (-seq [_]
    (list a b)))

(comment
  ;; destructuring just works
  (let [[f _] (MyThing. :a :b)]
    f)
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

  ;; But, performance?

  ;; ~90ms
  (let [c (Cat.)]
    (dotimes [_ 5]
      (time
        (dotimes [_ 1000000]
          (-sound c)))))
  )

;; MACROS YEAH!
;; we can extend the language without modifying the compiler

(comment
  ;; fizzbuzz
  (doseq [n (range 1 101)]
    (println 
      (match [(mod n 3) (mod n 5)]
        [0 0] "FizzBuzz"
        [0 _] "Fizz"
        [_ 0] "Buzz"
        :else n)))

  ;; this compiles down to very, very, very efficient tests

  ;; core.match is bigger than the ClojureScript compiler

  ;; IcedClojureScript not necessary, just make a macro library!
  )

;; Extending Abstractions safely to native types

(extend-type js/Text
  IPrintable
  (-pr-seq [this options]
    (list "#<text>")))

(extend-type js/Element
  IPrintable
  (-pr-seq [this options]
    (let [id     (.-id this)
          id-str (if-not (s/blank? id)
                   (str " id='" id "'") "")
          class  (.-className this)
          cl-str (if-not (s/blank? class) 
                   (str " class='" class "'") "")]
      (list "#<" (.toLowerCase (.-tagName this)) id-str cl-str ">"))))

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
    (->> (js-keys this)
      (filter #(.hasOwnProperty this %))
      (map #(vector % (aget this %)))))
  IPrintable
  (-pr-seq [this options]
    (-pr-seq (into {} (-seq this)) options)))

(comment
  (def body (d/single-node (dc/sel "body")))
  (.-childNodes body)

  (def box (d/single-node (dc/sel "#box")))
  (.-style box)

  ;; interactive live w/ the browser
  (set! (.-height (.-style box)) "300px")
  (set! (.-height (.-style box)) "100px")
  (set! (.-backgroundColor (.-style box)) "blue")
  (set! (.-backgroundColor (.-style box)) "red")

  ;; interop, can always call methods with .foo
  (.getElementById js/document "box")

  ;; iteration protocol just works!
  (first (.-style box))
  (rest (.-style box))
  )

;; JSON.next
(comment
  (r/read-string "(+ 1 2)")
  (r/read-string "#{1 [2 3] {:foo :bar}}")
  (type (r/read-string "#{1 [2 3] {:foo :bar}}"))
  (pr-str #{1 [2 3] {:foo :bar}})
  )