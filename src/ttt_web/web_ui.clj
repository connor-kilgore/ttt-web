(ns ttt-web.web-ui
  (:require [tic-tac-toe.game-runner :as game]
            [tic-tac-toe.option-selector :as menu]
            [tic-tac-toe.save-interface :as save]
            [tic-tac-toe.tic-tac-toe-board :as tttb]
            [tic-tac-toe.ui-interface :as ui]
            [ttt-web.form-data-parser :as form]))

(def forms {menu/menu-options "game-mode.html"})

(defmethod ui/initialize-ui :wui [_])

(defmethod ui/get-selection :wui [options]
  (get forms (:options options)))

(defn game-mode-form [game]
  (str
    "<!DOCTYPE><html><head><title>TicTacToe</title></head>"
    "<body><div><br><br><form method=\"post\">Game-Mode<br>"
    "<input type=\"radio\" id=\"one-player\" name=\"players\"
     value=\":one-player\" checked=\"checked\">"
    "<label for=\"one-player\">One Player</label><br>"
    "<input type=\"radio\" id=\"two-player\" name=\"players\"
     value=\":two-player\">"
    "<label for=\"two-player\">Two Player</label><br>"
    "<input type=\"radio\" id=\"ai-vs-ai\" name=\"players\"
     value=\":ai-vs-ai\">"
    "<input type=\"hidden\" name=\"game-state\" value=\"" game "\">"
    "<label for=\"ai-vs-ai\">AI vs. AI</label><br>"
    "<input type=\"submit\" value=\"Next\"></form></div></body></html>"))

(defn symbol-form [game]
  (str
    "<!DOCTYPE><html><head><title>TicTacToe</title></head>"
    "<body><div><br><br><form method=\"post\">Symbol<br>"
    "<input type=\"radio\" id=\"x\" name=\"players\"
     value=\":x\" checked=\"checked\">"
    "<label for=\"x\">X (First Turn)</label><br>"
    "<input type=\"radio\" id=\"o\" name=\"players\"
     value=\":o\">"
    "<label for=\"o\">O (Second Turn)</label><br>"
    "<input type=\"hidden\" name=\"game-state\" value=\"" game "\">"
    "<input type=\"submit\" value=\"Next\"></form></div></body></html>"))

(defn difficulty-form [game]
  (str
    "<!DOCTYPE><html><head><title>TicTacToe</title></head>"
    "<body><div><br><br><form method=\"post\">Difficulty<br>"
    "<input type=\"radio\" id=\"unbeatable\" name=\"difficulty\"
     value=\"10\" checked=\"checked\">"
    "<label for=\"unbeatable\">Unbeatable</label><br>"
    "<input type=\"radio\" id=\"medium\" name=\"difficulty\"
     value=\"9\">"
    "<label for=\"medium\">Medium</label><br>"
    "<input type=\"radio\" id=\"easy\" name=\"difficulty\"
     value=\"-1\">"
    "<label for=\"easy\">Easy</label><br>"
    "<input type=\"hidden\" name=\"game-state\" value=\"" game "\">"
    "<input type=\"submit\" value=\"Next\"></form></div></body></html>"))

(defn size-form [game]
  (str
    "<!DOCTYPE><html><head><title>TicTacToe</title></head>"
    "<body><div><br><br><form method=\"post\">Board Size<br>"
    "<input type=\"radio\" id=\"3x3\" name=\"board\"
     value=\"1\" checked=\"checked\">"
    "<label for=\"3x3\">3x3 (Classic)</label><br>"
    "<input type=\"radio\" id=\"4x4\" name=\"board\"
     value=\"2\">"
    "<label for=\"4x4\">4x4</label><br>"
    "<input type=\"radio\" id=\"3x3x3\" name=\"board\"
     value=\"3\">"
    "<label for=\"3x3x3\">3x3x3</label><br>"
    "<input type=\"hidden\" name=\"game-state\" value=\"" game "\">"
    "<input type=\"submit\" value=\"next\"></form></div></body></html>"))

(defn start-form [game]
  (str
    "<!DOCTYPE><html><head><title>TicTacToe</title></head>"
    "<body><div><br><br><form method=\"post\">Ready?<br>"
    "<input type=\"hidden\" name=\"round\" value=\"1\">"
    "<input type=\"hidden\" name=\"game-state\" value=\"" game "\">"
    "<input type=\"submit\" value=\"Start!\"></form></div></body></html>"))


(defn make-board-html [state]
  (str "<div class=\"board\">"
       (apply str (for [[i v] (map-indexed vector (:board state))]
                    (cond (= v 0)
                          (if (nil? (:symbol state))
                            (str "<div class=\"cell\"> </div>")
                            (str "<input class=\"cell\" name=\"board\" type=\"radio\" value=\""
                                 (tttb/place-value-into-tttb (:board state) (:symbol state) i) "\" "
                                 "checked=\"checked\">"))
                            (= v 1) (str "<div class=\"cell\">X</div>")
                            (= v 2) (str "<div class=\"cell\">O</div>"))))
              "</div>"))

  (defn board-form [game]
    (let [side-len (tttb/get-side-len (:game game))]
      (str "<!DOCTYPE html><html><head>"
           "<link rel=\"stylesheet\" href=\"resources/styles.css\">"
           "<style>.board {grid-template-columns: repeat(" side-len ", 100px);"
           "grid-template-rows: repeat(" side-len ", 100px);}</style>"
           "<title>Tic Tac Toe</title>"
           "</head><body><form method=\"post\">"
           (make-board-html {:board (:board (:game game)) :symbol (:symbol game) :three-d? (:three-d? (:game game))})
           "<input type=\"hidden\" name=\"game-state\" value=\""
           (form/game-to-form (assoc (:game game) :round (inc (:round (:game game))))) "\">"
           "<input type=\"submit\"></form></body></html>")))

  (defn send-end-state [game winner]
    (save/archive-game game)
    (let [end-str (game/get-end-condition-string winner (:players game))
          side-len (tttb/get-side-len game)]
      (str "<!DOCTYPE html><html><head>"
           "<link rel=\"stylesheet\" href=\"resources/styles.css\">"
           "<style>.board {grid-template-columns: repeat(" side-len ", 100px);"
           "grid-template-rows: repeat(" side-len ", 100px);}</style>"
           "<title>Tic Tac Toe</title></head><body>"
           (make-board-html {:board (:board game) :three-d? (:three-d? game)})
           "<h1>" end-str "</h1>"
           "<p>Retry?</p>"
           "<form action=\"/ttt\" method=\"post\">"
           "<input type=\"hidden\" name=\"new-game?\" value=false>"
           "<input type=\"hidden\" name=\"game-state\" value=\""
           (-> (game/reset-game game) (assoc :round 1) (form/game-to-form)) "\">"
           "<input type=\"submit\" value=\"yes\">"
           "</form>"
           "<form action=\"/ttt\" method=\"get\">"
           "<input type=\"submit\" value=\"no\">"
           "</form>"
           "</body></html>")))

  (defmethod ui/end-game :wui [game winner]
    (send-end-state game winner))

  (defn start-game [game]
    (let [dimensions (get menu/board-size-options (str (:board game)))
          new-board (tttb/make-board dimensions)
          game-with-board (-> game (assoc :board new-board)
                              (assoc :three-d? (:three-d? dimensions)))]
      (start-form game-with-board)))

  (defmethod ui/initialize-game :wui [game]
    (cond (nil? (:players game)) (game-mode-form game)
          (= (:players game) :one-player) (symbol-form game)
          (nil? (:difficulty game)) (difficulty-form game)
          (nil? (:board game)) (size-form game)
          :else (start-game game)))

  (defmethod ui/round-output :wui [game])

  (defmethod ui/make-move :wui [game]
    (board-form game))