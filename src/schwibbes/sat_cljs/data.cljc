(ns schwibbes.sat-cljs.data
  "parse sat problems into data structures (format as defined in https://www.satcompetition.org/2009/format-benchmarks2009.html)"
  (:require [clojure.string :as str]))

(defn to-int [s]
  #?(:clj  (java.lang.Integer/parseInt s)
   :cljs (let [ n (js/parseInt s) 
                nan (js/isNaN n) ]
                (if-not nan n))))

(defn- strcoll-to-intcoll
  [coll]
  (->> coll
    (map to-int)
    (remove nil?)))

(defn- ints-only? [s]
  (re-matches #"^[\-\d\W]+$" s))

(defn- split-at-separators [s]
  (str/split s #"\r?\n|\W0"))

(defn str-to-intset 
  "convert string of space separated ints to proper set of signed ints"
  [s]
  (-> s
    (str/split #" ")
    (strcoll-to-intcoll)
    (set)))

(defn cnfstr-to-intsets
  "convert string of cnf-clauses into coll of int-sets"
  [s]
  (let [lines (split-at-separators s)]
    (->> lines
     (map #(str/trim %))
     (filter ints-only?)
     (map str-to-intset))))
