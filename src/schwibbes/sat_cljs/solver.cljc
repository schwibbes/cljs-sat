(ns schwibbes.sat-cljs.solver
  (:require [schwibbes.sat-cljs.data :as data]))

(defn- propagate-or
  "simplify a disjunctive clause by applying lit to it"
  [lit clause]
  (if (some #(= lit %) clause)
   :solved
   (remove #(= % (- lit)) clause)))

(defn- unbound
  "collect all unbound variables in a seq of clauses"
  [clauses]
  (->> clauses
    (reduce concat)
    (map #(Math/abs %))
    (distinct)))

(defn- apply-unit [unit clauses]
  (->> clauses 
    (map (partial propagate-or unit))
    (remove #(= % :solved))))

(defn apply-units
  "simplify set of clauses by applying all currently known information (in form of existing clauses of length 1)"
  [solver-state]
  (loop [{:keys [asserted clauses unit status] :as all} solver-state]
    (prn unit)
    (cond
      (some #(contains? asserted (- %)) unit) (assoc all :status :conflict)
      (some empty? clauses) (assoc all :status :conflict)
      (empty? clauses) (assoc all :status :solution)
      (empty? unit) (assoc all :status :still-open)
      :else (let [unit-head (first unit)
                  unit-tail (rest unit)
                  updated-clauses (apply-unit unit-head clauses)
                  updated-units (filter #(= 1 (count %)) updated-clauses)]
        (recur (assoc all
          :unit (flatten (conj unit-tail updated-units))
          :clauses updated-clauses
          :asserted (conj asserted unit-head)))))))

(defn- choose-next-unit [clauses]
  (let [unbound-vars (unbound clauses)
        next-unit (first unbound-vars)]
    next-unit))

(defn solve
  "solve a given SAT instance"
  [solver-state]
  (loop [{:keys [asserted clauses unit status] :as all} solver-state 
         n 50]
    (prn (str n ":" status))
    (cond
      (neg? n) (assoc all :status :abbort)
      (= :solution status) all
      (= :conflict status) all
      (not-empty unit) (recur (apply-units all) (dec n))
      :else 
      (let [next (choose-next-unit clauses)]
        (recur (update all :unit conj next) (dec n))))))

(defn from-clauses
  "create solver input data structur from coll of or-clauses"
  [coll]
  {:status :open
   :asserted #{}
   :unit #{}
   :clauses coll})
