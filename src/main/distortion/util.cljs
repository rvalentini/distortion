(ns distortion.util
  (:require ["d3" :as d3]))

(def not-zero 0.00001)

(defn visualize-function [f width]
  (let
    [size 300
     data (into [] (map #(identity {:x % :y (f %)}) (range not-zero width)))
     x (->
         (d3/scaleLinear)
         (.domain (into-array [0 400]))
         (.range (into-array [not-zero size])))
     y (->
         (d3/scaleLinear)
         (.domain (into-array [-30 30]))
         (.range (into-array [size not-zero])))
     line (->
            (d3/line)
            (.x (fn [d] (x (:x d))))
            (.y (fn [d] (y (:y d)))))
     base-line (->
                 (d3/line)
                 (.x (fn [d] (x (:x d))))
                 (.y (constantly (/ size 2))))]
    [:svg
     {:viewBox (str 0 " " 0 " " size " " size)}
     [:path
      {:d (base-line data),
       :fill "transparent",
       :stroke (second d3/schemeCategory10)}]
     [:path
      {:d (line data),
       :fill "transparent",
       :stroke (first d3/schemeCategory10)}]]))

(defn inspector [distortion-fns width]
  [:td {:style {:vertical-align "top"
                :max-width "400px"}}
   (for [{:keys [name f]} distortion-fns]
     (do (println "name " name)
         (println "called " (f 4.444))
         [:<>
          [:p name]
          [:div {:style {:border-style "solid"
                         :border-width "1px"}}
           (visualize-function f width)]]))])
