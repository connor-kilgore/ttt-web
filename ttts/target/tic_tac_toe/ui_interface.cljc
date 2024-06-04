(ns tic-tac-toe.ui-interface)

(defmulti initialize-ui :ui)
(defmulti initialize-game :ui)
(defmulti round-output :ui)
(defmulti make-move :ui)
(defmulti print-text :ui)
(defmulti end-game :ui)
(defmulti get-selection :ui)
(defmulti close-program :ui)