(ns firefly.app
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [quil.core :as q]
            [quil.middleware :as m]))

(def width 500)
(def height 500)
(def blackish [50 0 0])

; TODO use width and height instead
; TODO make it move
; TODO idea: color blocks by finding closest square limits surrounding a given point
; TODO idea: combine multiple distortions in sequence

(defn barrel-like-dist [undistorted]
  (+ undistorted (* 2 (Math/pow (Math/log undistorted) 2))))

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

(defn draw-quad [[[t1 t2] [r1 r2] [b1 b2] [l1 l2]]]
  (q/quad t1 t2 r1 r2 b1 b2 l1 l2))

(defn draw-diamond [[t l r _ :as diamond]]
  (println diamond)                                         ;TODO fix case all nil should not happen
  (when (some? t)
    (when (some? l)
      (q/with-stroke ["blue"]
        (q/line t l)))
    (when (some? r)
      (q/with-stroke ["blue"]
        (q/line t r)))
    (when (every? some? diamond)
      (q/with-fill ["yellow"] (draw-quad diamond)))))

(defn filter-center-points [points]
  (mapv
    (fn [[idx row]] (vec (take-nth 2 (if (even? idx) row (rest row)))))
    (map-indexed vector points)))

; (1) generate matrix as vec of vecs
; (2) distort points
; (3) get uneven points in uneven rows and even in even
; (4) map to get-neighbors for these points
; (5) apply draw logic depending if [T L R B] exist + color coding depending on distance

(defn diamonds []
  ;TODO use threading
  (let [steps (range 0 50)                                  ;TODO increase
        points (vec (for [x steps]
                      (vec (for [y steps] [(* 10 x) (* 10 y)]))))
        distorted (map #(map (fn [p] (distort p barrel-like-dist [250 250])) %) points)
        center-points (filter-center-points distorted)
        diamonds (for [i (range 0 50)
                       j (range 0 50)]
                   (get-neighbors [i j] center-points))]
    (q/stroke blackish)
    (q/stroke-weight 2)
    (q/with-stroke ["green"]
      (q/stroke-weight 5)
      (q/point 250 250))
    (q/stroke-weight 2)
    (doseq [d diamonds]
      (draw-diamond d))))

(defn debug-mark-origin []
  (q/stroke "red")
  (q/stroke-weight 5)
  (q/point 0 0))

(defn setup []
  (q/smooth)
  (q/background 230 230 230)
  (q/with-translation [250 250]
    (debug-mark-origin)
    (diamonds)))

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

  (def test-vec (vec (for [i (range 0 5)]
                       (vec (for [j (range 0 5)] [i j])))))

  test-vec
  (mapv (fn [[idx row]] (vec (take-nth 2 (if (even? idx) row (rest row))))) (map-indexed vector test-vec))


  (def p1 [270 270])
  (distort [1 1] 0.1 [250 250])
  (println {:a 444 :b 88}))
