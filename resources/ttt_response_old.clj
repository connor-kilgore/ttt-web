(ns ttt-web.ttt-response-old
  (:require [clojure.string :as str]
            [tic-tac-toe.tic-tac-toe-board :as tttb]
            [tic-tac-toe.turn-system :as turn]
            [tic-tac-toe.turn-system :as turn]
            [tic-tac-toe.ai-player :as ai]
            [tic-tac-toe.win-checker :as win]
            [tic-tac-toe.game-runner :as game]
            [tic-tac-toe.save-interface :as save]
            [tic-tac-toe.edndb]
            [tic-tac-toe.psqldb])
  (:import (Core ResponseNode ServerResponseDelegator)
    java.net.URLDecoder))

(defn get-parameters [bytes]
  (let [settings (str/split (URLDecoder/decode (apply str (for [b bytes] (char b)))) #"&")]
    (apply merge (for [s settings]
                   {(keyword (first (str/split s #"=")))
                    (str (second (str/split s #"=")))}))))

(defn get-players [parameters]
  (let [symbol (Integer/parseInt (:symbol parameters))]
    (cond
      (= (:players parameters) "one-player") {:Player symbol
                                              :AI     (inc (mod symbol 2))}
      (= (:players parameters) "two-player") {:Player-1 1 :Player-2 2}
      :else {:AI-1 1 :AI-2 2})))

(defn get-dimensions [size]
  (let [sides (str/split size #"x")]
    {:size (Integer/parseInt (first sides)) :three-d (= (count sides) 3)}))

(defmulti get-ttt-state :new-game?)

(defmethod get-ttt-state "true" [parameters]
  (let [dimensions (get-dimensions (:size parameters))]
    {:save-location :edndb
     :difficulty    (Integer/parseInt (:difficulty parameters))
     :three-d       (:three-d dimensions)
     :round         1
     :ui            nil
     :three-d?      nil
     :players       (get-players parameters)
     :parameters    nil
     :board         (tttb/make-board dimensions)}))

(defn convert-key-player [player-key]
  (str/replace (subs (str player-key) 1) "-" " "))

(defn parse-players [players]
  {(convert-key-player (key (first players)))  (val (first players))
   (convert-key-player (key (second players))) (val (second players))})

(defn get-new-board [move symbol state]
  (cond (and (nil? move) (> (:round state) 0)) (ai/play-turn state symbol)
        (> (:round state) 0) (tttb/place-value-into-tttb (:board state) symbol (Integer/parseInt move))
        :else (:board state)))

(defmethod get-ttt-state "false" [parameters]
  (let [state (read-string (:game-state parameters))
        round (:round state)
        symbol (second (turn/get-current-player (:players state) round))]
    (-> state
        (assoc :round (inc round))
        (assoc :board (get-new-board (:cell parameters) symbol state)))))

(defmulti make-board-html :ai-turn)

(defmethod make-board-html false [state]
  (str "<div class=\"board\">"
       (apply str (for [[i v] (map-indexed vector (:board (:state state)))]
                    (cond (= v 0) (str "<input class=\"cell\" name=\"cell\" type=\"radio\" value=\"" i "\" "
                                       "checked=\"checked\">")
                          (= v 1) (str "<div class=\"cell\">X</div>")
                          (= v 2) (str "<div class=\"cell\">O</div>"))))
       "</div>"))

(defmethod make-board-html true [state]
  (str "<div class=\"board\">"
       (apply str (for [[i v] (map-indexed vector (:board (:state state)))]
                    (cond (= v 0) (str "<div class=\"cell\"></div>")
                          (= v 1) (str "<div class=\"cell\">X</div>")
                          (= v 2) (str "<div class=\"cell\">O</div>"))))
       "</div>"))

(defn ai-turn? [players round]
  (turn/is-ai? (turn/get-current-player (parse-players players) round)))

(defn send-ttt-state [state client-params]
  (save/save-game! state)
  (let [side-len (tttb/get-side-len state)
        status "HTTP/1.1 200 OK"
        headers (into-array String ["Content-Type: text/html" "Server: Example Server"])
        content (.getBytes (str "<!DOCTYPE html><html lang=\"en\"><head> "
                                "<meta charset=\"UTF-8\"> <meta name=\"viewport\" "
                                "content=\"width=device-width, initial-scale=1.0\"> "
                                "<title>Tic Tac Toe</title> <style>  .board {    "
                                "display: grid;    grid-template-columns: repeat(" side-len ", 100px);    "
                                "grid-template-rows: repeat(" side-len ", 100px);    gap: 2px;    "
                                "justify-content: center;    align-items: center;    margin: 50px "
                                "auto;    border: 2px solid black;    background-color: #eee;  "
                                "}  .cell {    width: 100px;    height: 100px;    display: "
                                "flex;    justify-content: center;    align-items: center;    "
                                "border: 1px solid black;    cursor: pointer;    font-size: 36px;  "
                                "} </style></head><body><form action=\"/ttt\" method=\"post\" accept-charset=\"utf-8\">"
                                (make-board-html {:state state :ai-turn (ai-turn? (:players state) (:round state))})
                                "<input type=\"hidden\" name=\"new-game?\" value=false>"
                                "<input type=\"hidden\" name=\"game-state\" value=\"" state "\">"
                                "<input type=\"submit\"></form></body></html>"))
        response-node (new ResponseNode status headers content)
        server-response (new ServerResponseDelegator nil nil nil)]
    (.sendResponse server-response response-node (:socket (:cp client-params)))))

(defn send-end-state [state client-params]
  (save/archive-game state)
  (let [end-str (game/get-end-condition-string (win/get-winner state) (parse-players (:players state)))
        status "HTTP/1.1 200 OK"
        headers (into-array String ["Content-Type: text/html" "Server: Example Server"])
        content (.getBytes (str "<!DOCTYPE html><html lang=\"en\"><head> "
                                "<meta charset=\"UTF-8\"> <meta name=\"viewport\" "
                                "content=\"width=device-width, initial-scale=1.0\"> "
                                "<title>Tic Tac Toe</title></head><body>"
                                "<h1>" end-str "</h1>"
                                "<p>Retry?</p>"
                                "<form action=\"/ttt\" method=\"post\">"
                                "<input type=\"hidden\" name=\"new-game?\" value=false>"
                                "<input type=\"hidden\" name=\"game-state\" value=\""
                                (assoc (game/reset-game state) :round 0) "\">"
                                "<input type=\"submit\" value=\"yes\">"
                                "</form>"
                                "<form action=\"/ttt\" method=\"get\">"
                                "<input type=\"submit\" value=\"no\">"
                                "</form>"
                                "</body></html>"))
        response-node (new ResponseNode status headers content)
        server-response (new ServerResponseDelegator nil nil nil)]
    (.sendResponse server-response response-node (:socket (:cp client-params)))))

(defmulti send-response :type)

(defmethod send-response "GET" [client-params]
  (let [fr (new FileResponse)
        content-file (.getFile fr "." "start-menu.html")
        response-node (.generateFile fr content-file)
        server-response (new ServerResponseDelegator nil nil nil)]
    (.sendResponse server-response
                   response-node (:socket (:cp client-params)))))

(defmethod send-response "POST" [client-params]
  (let [content (.getContent (:rp (:cp client-params)))
        state (get-ttt-state (get-parameters content))]
    (if (nil? (win/get-winner-or-tie state))
      (send-ttt-state state client-params)
      (send-end-state state client-params))))
