(ns firefly.app
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [quil.core :as q]
            [quil.middleware :as m]))

(def width 500)
(def height 500)
(def steps 80)

; TODO make it move
; TODO idea: combine multiple distortions in sequence

(def colors {:blackish [50 0 0]
             :yellow [244 226 133]
             :orange [244 162 89]
             :blue   [180 210 231]})

(defn colorize [[p1-x p1-y] [p2-x p2-y]]
  (let [dist (Math/sqrt (+ (Math/pow (- p1-y p2-y) 2) (Math/pow (- p1-x p2-x) 2)))
        relative-dist (/ dist (Math/sqrt (+ (Math/pow (* 0.5 height) 2) (Math/pow (* 0.5 width) 2))))]
    ((cond
       (< relative-dist 0.3) :orange
       (>= 0.7 relative-dist 0.3) :blue
       :else :yellow) colors)))

(defn barrel-like-dist [undistorted]
  (+ undistorted (* 2 (Math/pow (Math/log undistorted) 2)) (* 5 (Math/log undistorted))))

(defn pincushion-like-dist [undistorted]
  (+ undistorted (/ (Math/pow undistorted 2) 300)))

(defn distort [[x y] fn [center-x center-y]]
  (let [[v-x v-y] [(- x center-x) (- y center-y)]
        v-len (Math/sqrt (+ (Math/pow v-x 2) (Math/pow v-y 2)))
        [v-norm-x v-norm-y] [(/ v-x v-len) (/ v-y v-len)]
        distorted (fn v-len)
        [new-v-x new-v-y] [(* distorted v-norm-x) (* distorted v-norm-y)]
        new-p [(+ new-v-x center-x) (+ new-v-y center-y)]]
    new-p))

(defn within-bounds [location dim]
  (every? #(< -1 % dim) location))

(defn get-neighbors [[x y] points]
  (map #(get-in points %) [[x (dec y)] [(dec x) y] [(inc x) y] [x (inc y)]]))

(defn draw-quad [[[t1 t2] [l1 l2] [r1 r2] [b1 b2]]]
  (q/quad t1 t2 r1 r2 b1 b2 l1 l2))

(defn draw-diamond [diamond]
  (when (every? some? diamond)
    (q/with-fill [(colorize (first diamond) [250 250])] (draw-quad diamond))))

(defn diamond-centers []
  (filter (fn [p] (every? even? p)) (for [i (range 0 steps) j (range 0 steps)] [i j])))

; (1) generate matrix as vec of vecs
; (2) distort points
; (3) get uneven points in uneven rows and even in even
; (4) map to get-neighbors for these points
; (5) apply draw logic depending if [T L R B] exist + color coding depending on distance

(defn diamonds []
  ;TODO use threading
  (let [points (vec (for [x (range 0 steps)]
                      (vec (for [y (range 0 steps)] [(* (/ width steps) x) (* (/ height steps) y)]))))
        distorted (mapv #(mapv (fn [p] (distort p barrel-like-dist [250 250])) %) points)
        centers (diamond-centers)]
    (q/stroke (:blackish colors))
    (q/stroke-weight 2)
    (doseq [c centers]
      (draw-diamond (get-neighbors c distorted)))
    (q/with-stroke ["green"]
      (q/stroke-weight 5)
      (q/point 250 250))))

(defn debug-mark-center []
  (q/with-stroke ["green"]
    (q/stroke-weight 5)
    (q/point 250 250)))

(defn debug-mark-origin []
  (q/stroke "red")
  (q/stroke-weight 5)
  (q/point 0 0))

(defn setup []
  (q/smooth)
  (q/background 230 230 230)
  (q/with-translation [250 250]
    (diamonds)
    (debug-mark-origin)
    (debug-mark-center)))

(defn canvas []
  (r/create-class
    {:component-did-mount
     (fn [component]
       (let [node (rdom/dom-node component)
             c-width (* 2 width)
             c-height (* 2 height)]
         (q/sketch
           :title "Red cross"
           :host node
           :setup setup
           :size [c-width c-height]
           :middleware [m/fun-mode])))
     :render
     (fn [] [:div])}))


(defn ^:dev/after-load run []
  (rdom/render [canvas] (js/document.getElementById "app")))

(comment
  ;; switch to CLJS REPL
  (shadow/repl :app)
  (js/alert "Bonjour from REPL")
  (even? 0)
  (def test-vec (vec (for [i (range 0 5)]
                       (vec (for [j (range 0 5)] [i j])))))

  (colorize [24 24] [250 250])
  (diamond-centers)
  test-vec
  )
