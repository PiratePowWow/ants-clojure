(ns ants-clojure.core
  (:require [clojure.java.io :as io])
  (:import [javafx.application Application]
           [javafx.fxml FXMLLoader]
           [javafx.scene Scene]
           [javafx.scene.paint Color]
           [javafx.animation AnimationTimer])
  (:gen-class :extends javafx.application.Application))

(def width 800)
(def height 600)
(def ant-count 100)
(def ants (atom nil))
(def last-timestamp (atom 0))


(defn create-ants []
  (for [i (range ant-count)]
    {:x (rand-int width)
     :y (rand-int height)
     :color (Color/BLACK)}))

(defn draw-ants! [context]
  (.clearRect context 0 0 width height)
  (doseq [ant @ants]
    ;context.setFill(Color.BLACK)
    (.setFill context (:color ant))
    (.fillOval context (:x ant) (:y ant) 5 5)))

(defn aggravate-ant [ant]
  (let [nearby-ants (filter (fn nearby [angry-ant] 
                              (> 10 (Math/pow (+ (Math/pow (Math/abs (- (:x ant) (:x angry-ant))) 2)
                                                 (Math/pow (Math/abs (- (:y ant) (:y angry-ant))) 2))
                                              0.5)))
                            @ants)]
    (if (= 1 (count nearby-ants))
      (assoc ant :color Color/BLACK)
      (assoc ant :color Color/RED))))

(defn random-step []
  (- (* 2 (rand)) 1))

(defn move-ant [ant]
  (Thread/sleep 1)
  (assoc ant
    :x (+ (random-step) (:x ant))
    :y (+ (random-step) (:y ant))))

(defn fps [now]
  (let [diff (- now @last-timestamp)
        diff-seconds (/ diff 1000000000)]
    (int (/ 1 diff-seconds))))

(defn -start [app stage]
  (let [root (FXMLLoader/load (io/resource "main.fxml"))
        scene (Scene. root width height)
        canvas (.lookup scene "#canvas")
        context (.getGraphicsContext2D canvas)
        fps-label (.lookup scene "#fps")
        timer (proxy [AnimationTimer] []
                (handle [now]
                  (.setText fps-label (str (fps now)))
                  (reset! last-timestamp now)
                  (reset! ants (doall (pmap move-ant @ants)))
                  (reset! ants (doall (pmap aggravate-ant @ants)))
                  (draw-ants! context)))]
    (reset! ants (create-ants))
    (.setTitle stage "Ants")
    (.setScene stage scene)
    (.show stage)
    (.start timer)))

(defn -main []
  (Application/launch ants_clojure.core (into-array String [])))
