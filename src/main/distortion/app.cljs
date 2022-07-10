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

(defonce state (r/atom {:alpha {:center {:angle 0
                                         :r 150}}
                        :beta {:center {:angle Math/PI
                                        :r 150}}}))

; TODO colorize separately and use colors that increase the optical magnification effect: "low" vs. "high"
; TODO refactor double distortion
; TODO try to increase radius of the pincushion distortion

(defn pol->cart [{:keys [angle r]}]
  (let [x (+ (/ width 2) (* r (Math/cos angle)))
        y (+ (/ height 2) (* r (Math/sin angle)))]
    [x y]))

(defn colorize [[x y]]
  (let [[c-x c-y] (pol->cart (get-in @state [:beta :center]))
        dist (Math/sqrt (+ (Math/pow (- y c-y) 2) (Math/pow (- x c-x) 2)))
        relative-dist (/ dist (Math/sqrt (+ (Math/pow (* 0.5 height) 2) (Math/pow (* 0.5 width) 2))))]
    ((cond
       (< relative-dist 0.3) :orange
       (>= 0.5 relative-dist 0.3) :blue
       :else :yellow) colors)))

(defn gaussian [[a b c] x]
  (* a (Math/exp (- (/ (Math/pow (- x b) 2) (* 2 (Math/pow c 2)))))))

;; good vizualization: y [-30 30] x [0 400]
(defn barrel-like-distortion [undistorted]
  (apply + (map #(gaussian % undistorted) [[7 0 100] [9 30 100] [9 -30 100]])))

(defn pincushion-like-dist [undistorted]
  (+ undistorted (/ (Math/pow undistorted 1.8) 300) (* 3 (- (Math/log undistorted)))))

(defn distort [[x y] fn [center-x center-y]]
  (let [[v-x v-y] [(- x center-x) (- y center-y)]
        v-len (Math/sqrt (+ (Math/pow v-x 2) (Math/pow v-y 2)))
        [v-norm-x v-norm-y] [(/ v-x v-len) (/ v-y v-len)]
        distorted (+ v-len (fn v-len))
        [dist-x dist-y] [(* distorted v-norm-x) (* distorted v-norm-y)]]
    [(+ dist-x center-x) (+ dist-y center-y)]))

(defn draw-quad [[[t1 t2] [l1 l2] [r1 r2] [b1 b2]]]
  (q/quad t1 t2 r1 r2 b1 b2 l1 l2))

(defn draw-diamond [diamond]
  (when (every? some? diamond)
    (q/with-fill [(colorize (first diamond))] (draw-quad diamond))))


(defn map-each-point [f matrix]
  (mapv (fn [row] (mapv (fn [point] (f point)) row)) matrix))

(defn apply-distortion [dist-fn center points]
  (map-each-point (fn [p] (distort p dist-fn (pol->cart center))) points))

(defn get-neighbors [[x y] points]
  (map #(get-in points %) [[x (dec y)] [(dec x) y] [(inc x) y] [x (inc y)]]))

(defn draw-diamonds [points centers]
  (let [distorted (->> points
                    #_(apply-distortion pincushion-like-dist (get-in @state [:alpha :center]))
                    (apply-distortion barrel-like-distortion (get-in @state [:beta :center])))]
    (q/stroke (:blackish colors))
    (q/stroke-weight 2)
    (doseq [c centers]
      (draw-diamond (get-neighbors c distorted)))))

(defn draw [{:keys [points centers]}]
  (q/clear)
  (q/background 230 230 230)
  (q/with-translation [250 250]
    (draw-diamonds points centers)))

(defn diamond-centers []
  (let [r (range 0 steps)]
    (filter (fn [p] (every? even? p)) (for [i r j r] [i j]))))

(defn move [center]
  (update center :angle #(mod (+ % (/ Math/PI 100)) (* 2 Math/PI))))

(defn update-state [state]
  (swap! state #(-> %
                  (update-in [:alpha :center] move)
                  (update-in [:beta :center] move)))
  state)

(defn setup []
  (q/smooth)
  (q/frame-rate fps)
  state)

(defn lattice []
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
   (visualize "Barrel new" barrel-like-distortion)

   #_(visualize "Pincushion Distortion" pincushion-like-dist)])

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
           :draw #(draw {:points (lattice)
                         :centers (diamond-centers)})
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
  (diamond-centers)
  line
  (.log js/console line)
  )
