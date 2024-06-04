(ns ttt-web.cookies
  (:require [tic-tac-toe.save-interface :as save]))

; TODO, make this all work

(defmethod save/save-game! :cookies [game])

(defmethod save/archive-game :cookies [game])