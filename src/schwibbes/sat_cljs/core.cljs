(ns ^:figwheel-hooks schwibbes.sat-cljs.core
  (:require
    [goog.dom :as gdom]
    [reagent.core :as reagent :refer [atom]]
    [schwibbes.sat-cljs.data :as data]
    [schwibbes.sat-cljs.solver :as solver]))

(enable-console-print!)

(defonce app-state (atom {:unit "" :clauses "#  1 XOR 2, (-1) path impossible\n-1 -2 \n -1" }))

(defn input-unit []
  [:p
   [:span "given literals: "]
   [:input {:type "text"
            :style {:width "95%"}
            :value (:unit @app-state)
            :on-change #(swap! app-state assoc :unit (-> % .-target .-value))}]])

(defn input-clauses []
  [:p
   [:span "clauses: "]
   [:textarea {
               :rows "12"
               :style {:width "95%" :height "80%"}
               :value (:clauses @app-state)
               :on-change #(swap! app-state assoc :clauses (-> % .-target .-value))}]])

(defn result-line [content label] 
  [:div
   [:p 
    [:span (str label ": " content)]]])

(defn state-to-input [state]
  (-> state
      (:clauses)
      (data/load-sat)
      (solver/from-clauses)
      (assoc :unit (data/into-literals (:unit state)))))

(defn input [] 
  (-> @app-state
      (state-to-input)
      (result-line "input")))

(defn simplified [] 
  (-> @app-state
      (state-to-input)
      (solver/apply-units)
      (result-line "simplified")))

(defn complete [] 
  (-> @app-state
      (state-to-input)
      (vector)
      (solver/solve)
      (result-line "complete")))

(defn entrypoint []
  [:div.container
    [:div
     [:h1 "way too simple SAT-solver running in the browser"]
     [:p "for educational purposes only;)"]]
    [:div
     [:div
      (input-unit)
      (input-clauses)
      #_(input)
      #_(simplified)
      (complete)]]
    [:div.col]])

;; hook into web page
;; ;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-app-element []
  (gdom/getElement "app"))

(defn mount [el]
  (reagent/render-component [entrypoint] el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))

(mount-app-element)

(defn ^:after-load on-reload []
  (mount-app-element))
