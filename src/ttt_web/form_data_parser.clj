(ns ttt-web.form-data-parser
  (:require [clojure.string :as str]))

(defn keyword-to-str [word]
  (subs (str word) 1))

(defn keyword-to-int [word]
  (Integer/parseInt (keyword-to-str word)))

(def player-mappings
  {:two-player {"Player 1" 1 "Player 2" 2}
   :ai-vs-ai   {"AI 1" 1 "AI 2" 2}
   :x          {"Player" 1 "AI" 2}
   :o          {"Player" 2 "AI" 1}})

(def player-mappings-reversed
  {{"Player 1" 1 "Player 2" 2} :two-player
   {"AI 1" 1 "AI 2" 2}         :ai-vs-ai
   {"Player" 1 "AI" 2}         :x
   {"Player" 2 "AI" 1}         :o})

(defn form-to-game [game]
  (-> game
      (assoc :players (get player-mappings (:players game)))
      (assoc :round (:round game))
      (assoc :difficulty (:difficulty game))))

(defn game-to-form [game]
  (-> game
      (assoc :players (get player-mappings-reversed (:players game)))))

(defn get-value-key [form-data]
  (-> form-data
      (str/split #"&")
      (first)
      (str/split #"=")
      (first)
      (keyword)))

(defn get-value [form-data]
  (-> form-data
      (str/split #"&")
      (first)
      (str/split #"=")
      (second)
      (read-string)))

(defn get-game-state [form-data]
  (let [split-data (-> form-data
                       (str/split #"&")
                       (second))
        data-to-manipulate (if (nil? split-data) form-data split-data)]
    (-> data-to-manipulate
        (str/split #"=")
        (second)
        (read-string))))