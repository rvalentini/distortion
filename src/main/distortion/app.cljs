(ns distortion.app
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            ["d3" :as d3]))

(def width 500)
(def height 500)
(def steps 70)
(def fps 20)
(def not-zero 0.00001)

;TODO move to util ns
(defn visualize-function [f]
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


(def colors {:blackish [50 0 0]
             :yellow [244 226 133]
             :orange [244 162 89]
             :blue [180 210 231]})

(declare sinoid-distortion)
(declare pincushion-like-dist)
(declare barrel-like-distortion)

(def state (r/atom {:distortions [{:fun #'sinoid-distortion
                                   :center {:angle 0
                                            :r 150}
                                   :update-frq 500}
                                  {:fun #'pincushion-like-dist
                                   :center {:angle Math/PI
                                            :r 150}
                                   :update-frq 100}
                                  #_{:fun #'barrel-like-distortion
                                     :center {:angle (/ Math/PI 2)
                                              :r 150}
                                     :update-frq 500}]}))

; TODO colorize separately and use colors that increase the optical magnification effect: "low" vs. "high"

(defn pol->cart [{:keys [angle r]}]
  (let [x (+ (/ width 2) (* r (Math/cos angle)))
        y (+ (/ height 2) (* r (Math/sin angle)))]
    [x y]))

(defn relative-dist-to-nearest-center [x y]
  (->> (:distortions @state)
    (map (comp pol->cart :center))
    (map (fn [[c-x c-y]] (Math/sqrt (+ (Math/pow (- y c-y) 2) (Math/pow (- x c-x) 2)))))
    (map (fn [dist] (/ dist (Math/sqrt (+ (Math/pow (* 0.5 height) 2) (Math/pow (* 0.5 width) 2))))))
    (apply min)))

(defn colorize [[x y]]
  (let [dist (relative-dist-to-nearest-center x y)]
    ((cond
       (< dist 0.3) :orange
       (>= 0.5 dist 0.3) :blue
       :else :yellow) colors)))

(defn gaussian [[a b c] x]
  (* a (Math/exp (- (/ (Math/pow (- x b) 2) (* 2 (Math/pow c 2)))))))

;; good vizualization: y [-30 30] x [0 400]
(defn barrel-like-distortion [undistorted]
  (apply + (map #(gaussian % undistorted) [[7 0 100] [9 30 100] [9 -30 100]])))

(defn sigmoid [x mid steepness]
  (/ 1 (+ 1 (Math/pow Math/E (* (- steepness) (- x mid))))))

(defn pincushion-like-dist [undist]
  (if (< undist 100)
    (- (* (- 1 (sigmoid undist 50 0.1)) undist))
    0))

(defn sinoid-distortion [undist]
  (* 4 (Math/sin undist)))

(defn distort [[x y] fun [center-x center-y]]
  (let [[v-x v-y] [(- x center-x) (- y center-y)]
        v-len (Math/sqrt (+ (Math/pow v-x 2) (Math/pow v-y 2)))
        [v-norm-x v-norm-y] [(/ v-x v-len) (/ v-y v-len)]
        distorted (+ v-len (fun v-len))
        [dist-x dist-y] [(* distorted v-norm-x) (* distorted v-norm-y)]]
    [(+ dist-x center-x) (+ dist-y center-y)]))

(defn draw-diamond [[[t1 t2] [l1 l2] [r1 r2] [b1 b2] :as diamond]]
  (when (every? some? diamond)
    (q/with-fill [(colorize (first diamond))] (q/quad t1 t2 r1 r2 b1 b2 l1 l2))))

(defn map-each-point [f matrix]
  (mapv (fn [row] (mapv (fn [point] (f point)) row)) matrix))

(defn apply-distortion [points {:keys [fun center]}]
  (map-each-point (fn [p] (distort p fun (pol->cart center))) points))

(defn points->diamonds [points]
  (->> (range 0 steps)
    (#(for [i % j %] [i j]))
    (filter #(every? even? %))
    (map (fn [[x y]] [[x (dec y)] [(dec x) y] [(inc x) y] [x (inc y)]]))
    (map #(map (fn [p] (get-in points p)) %))))

(defn draw-diamonds [grid]
  (let [distorted (reduce #(apply-distortion %1 %2) grid (:distortions @state))]
    (q/stroke (:blackish colors))
    (q/stroke-weight 2)
    (doseq [d (points->diamonds distorted)]
      (draw-diamond d))))

(defn draw [grid]
  (q/clear)
  (q/background 230 230 230)
  (q/with-translation [250 250]
    (draw-diamonds grid)))


(defn move [distortion]
  (update-in distortion [:center :angle]
    #(mod (+ % (/ Math/PI (:update-frq distortion))) (* 2 Math/PI))))

(defn update-state [state]
  (swap! state update :distortions #(map move %))
  state)

(defn setup []
  (q/smooth)
  (q/frame-rate fps)
  state)

(defn build-grid []
  (let [r (range 0 steps)
        w-scale (/ width steps)
        h-scale (/ height steps)]
    (vec (for [x r] (vec (for [y r] [(* w-scale x) (* h-scale y)]))))))

(defn visualize [name f]
  [:<>
   [:p name]
   [:div {:style {:border-style "solid"
                  :border-width "1px"}}
    (visualize-function f)]])

(defn inspection-util []
  [:td {:style {:vertical-align "top"
                :max-width "400px"}}
   #_(visualize "Barrel new" barrel-like-distortion)
   (visualize "Pincushion Distortion" pincushion-like-dist)])

(defn canvas []
  (r/create-class
    {:component-did-mount
     (fn [component]
       (let [node (rdom/dom-node component)]
         (q/sketch
           :title "Red cross"
           :host node
           :update update-state
           :setup setup
           :draw #(draw (build-grid))
           :size [(* 2 width) (* 2 height)]
           :middleware [m/fun-mode])))
     :render
     (fn [] [:td])}))


(defn ^:dev/after-load run []
  (rdom/render [:div
                [canvas state]
                [inspection-util]] (js/document.getElementById "app")))

(comment
  ;; switch to CLJS REPL
  (shadow/repl :app)

  (def lat (build-grid))
  lat
  )
