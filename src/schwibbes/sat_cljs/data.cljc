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

(defn- starts-with-digit [s]
  (re-matches #"^[\-\d].*$" s))

(defn- split-at-separators [s]
  (str/split s #"\r?\n|\W0"))

(defn into-literals [s]
  (-> s
    (str/split #" ")
    (strcoll-to-intcoll)
    (set)))

(defn load-sat
  "load sat problem into clojure data structure"
  [s]
  (let [lines (split-at-separators s)]
    (->> lines
     (map #(str/trim %))
     (filter starts-with-digit)
     (map into-literals))))
