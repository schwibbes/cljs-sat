(ns ^:figwheel-hooks schwibbes.sat-cljs.core
  (:require
    [goog.dom :as gdom]
    [reagent.core :as reagent :refer [atom]]
    [schwibbes.sat-cljs.data :as data]
    [schwibbes.sat-cljs.solver :as solver]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<! >! chan close! sliding-buffer put! alts!]])
  (:require-macros 
    [cljs.core.async.macros :refer [go alt!]]))

(enable-console-print!)

(defonce app-state (atom {:raw { :unit "" :clauses "" }
                          :data { :unit #{} :clauses [#{}]}}))

(defn input-given []
  [:p
   [:span "given literals: "]
   [:input {:type "text"
            :style {:width "95%"}
            :value (get-in @app-state [:raw :unit])
            :on-change (fn [event] 
                         (let [str-val (-> event .-target .-value)
                               given (data/str-to-intset str-val)] 
                           (do (swap! app-state assoc-in 
                                      [:raw :unit] str-val)
                             (swap! app-state assoc-in 
                                    [:data :unit] given))))}]
   #_[:span (get-in @app-state [:data :unit])]])

(defn input-clauses [] 
  [:p
   [:span "clauses: "] 
   [:textarea {
               :rows "12"
               :style {:width "95%" :height "80%"} 
               :value (get-in @app-state [:raw :clauses])
               :on-change (fn [event] 
                            (let [str-val (-> event .-target .-value)
                                  clauses (data/cnfstr-to-intsets str-val)] 
                              (do (swap! app-state assoc-in 
                                         [:raw :clauses] str-val)
                                (swap! app-state assoc-in 
                                       [:data :clauses] clauses))))}]
   #_[:span (get-in @app-state [:data :clauses])]])


(defn load-template 
  "load data from relative url in /data/ subdir"
  [filename]
  (go (let [url (str "data/" filename)
            response (<! (http/get url))
            body (:body response)]
        (swap! app-state assoc-in [:raw :clauses] body))))

(defn input-file []
  (for [file ["unsat-simple.cnf" "sat100.cnf" "sat250.cnf" "unsat250.cnf"]]
    ^{:key file} [:input {:type "button" 
                          :value file
                          :on-click #(load-template file)}]))

(defn solve []
  [:input {:type "button" :value "solve!"
           :on-click (fn [event]
                       (let [input (vector (:data @app-state))
                             result (solver/solve input)]
                         (swap! app-state assoc :solution result)))}])

(defn render-block [label content] 
  [:div
   [:p 
    [:span [:strong (str label ": " content)]]]])

(defn entrypoint []
  [:div.container
   [:div
    [:h1 "way too simple SAT-solver running in the browser"]
    [:p "for educational purposes only;)"]]
   [:div
    [:div
     (input-file)
     (solve)
     (input-given)
     (input-clauses)
     (render-block "result" (:solution @app-state))]]
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
