(ns schwibbes.sat-cljs.solver
  (:require [schwibbes.sat-cljs.data :as data]))

(defn- propagate-or
  "simplify a disjunctive clause by applying lit to it"
  [lit clause]
  (if (contains? clause lit) :solved (disj clause (- lit))))

(defn- unbound
  "collect all unbound variables in a seq of clauses"
  [clauses]
  (->> clauses
    (reduce concat)
    (map #(Math/abs %))))

(defn- apply-unit [unit clauses]
  (->> clauses 
    (map (partial propagate-or unit))
    (remove #(= % :solved))))

(defn apply-units
  "simplify set of clauses by applying all currently known information (in form of existing clauses of length 1)"
  [solver-state]
  (loop [{:keys [asserted clauses unit status] :as all} solver-state]
    #_(prn "u->" unit)
    (cond
      (some #(contains? asserted (- %)) unit) (assoc all :status :conflict)
      (some empty? clauses) (assoc all :status :conflict)
      (empty? clauses) (assoc all :status :solution)
      (empty? unit) (assoc all :status :still-open)
      :else (let [unit-head (first unit)
                  unit-tail (set (next unit))
                  updated-clauses (apply-unit unit-head clauses)
                  updated-units (into #{} cat (filter #(= 1 (count %)) updated-clauses))]
        (recur (assoc all
          :unit (clojure.set/union unit-tail updated-units)
          :clauses updated-clauses
          :asserted (conj asserted unit-head)))))))

(defn- choose-next-unit [clauses]
  (let [unbound-vars (unbound clauses)
        next-unit (first unbound-vars)]
    next-unit))

(defn solve
  "solve a given SAT instance given a queue of solver-state instances."
  [queue]
  (loop [[current & remaining :as q] queue n 59999]
   (let [{:keys [clauses unit status]} current]
    #_(prn "s->" q)
    (cond
      (neg? n) (assoc current :status :abbort)
      (= :solution status) current
      (= :conflict status) (if (empty? remaining) 
        current
        (recur remaining (dec n)))
      (not-empty unit) (recur (conj remaining (apply-units current)) (dec n))
      :else 
      (let [pos (choose-next-unit clauses)
            neg (- pos)
            queue+ (conj remaining (update current :unit conj pos) 
                                   (update current :unit conj neg))]
        (recur queue+ (dec n)))))))

(defn from-clauses
  "create solver input data structur from coll of or-clauses"
  [coll]
  {:status :open
   :asserted #{}
   :unit #{}
   :clauses coll})
 