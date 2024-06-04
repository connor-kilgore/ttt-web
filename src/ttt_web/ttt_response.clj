(ns ttt-web.ttt-response
  (:require [tic-tac-toe.game-runner :as ttt]
            [tic-tac-toe.ui-interface :as ui]
            [ttt-web.web-ui :as web]
            [ttt-web.form-data-parser :as form])
  (:import (Core ClientRequestParser Packet Response)
           java.net.URLDecoder))

(def requestParser (new ClientRequestParser))

(defn get-new-game [args]
  (let [un-extended-game (ttt/parse-arguments ttt/base-game args)]
    (-> un-extended-game
        (assoc :ui :wui)
        (assoc :save-location :cookies)
        (assoc :parameters (conj (:parameters un-extended-game))))))

(defn is-get-request? [node]
  (= (.getRequestType requestParser (.getStatus node)) "GET"))

(defn start-game [args]
  (let [new-game (get-new-game args)]
    (ttt/get-new-game new-game nil)))
; TODO, get-new-game should take in a potential last game

(defn update-game-from-packet [packet]
  (let [form-data (URLDecoder/decode
                    (Packet/convertBytesToString (.getContent packet)))
        key (form/get-value-key form-data)
        value (form/get-value form-data)]
    (assoc (form/get-game-state form-data) key value)))

(defn board-set? [game]
  (or (nil? (:board game)) (number? (:board game))))

(defn get-round-output [game]
  (let [output (ttt/game-round (form/form-to-game game))]
    (cond (string? output) output
          (or (number? output) (nil? output)) (ui/end-game (form/form-to-game game) output)
          :else (recur (form/game-to-form output)))))

(defn resume-game [packet]
  (let [game (update-game-from-packet packet)]
    (if (board-set? game)
      (ttt/get-new-game game nil)
      (get-round-output game))))

(defn generate-ttt-response [node args]
  (if (is-get-request? node)
    (start-game args)
    (resume-game node)))

(deftype ttt-response-handler [args]
  Response
  (handleResponse [this root clientSocket node]
    (try
      (Packet/sendHTML (generate-ttt-response node args) clientSocket)
      (catch Exception e
        (println "IOException occurred: " (.printStackTrace e))))))

; --- ttt-get-handler ---
; post form that is menu that says start
;

; --- ttt-post-handler ---
; process the data
; send processed data to the browser