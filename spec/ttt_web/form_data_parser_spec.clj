(ns ttt-web.form-data-parser-spec
  (:require [speclj.core :refer :all]
            [ttt-web.form-data-parser :refer :all]))

(def example-form-1 (str "players=one-player&game-state="
                         "{:players nil, :difficulty nil, :round 1,"
                         ":three-d? nil, :board nil, :ui :wui, :save-location"
                         " :edndb, :parameters ()}"))

(def example-form-2 (str "difficulty=10&game-state="
                         "{:players nil, :difficulty 9, :round 6,"
                         ":three-d? false, :board [0 0 0 0 0 0 0 0 0], :ui :wui, :save-location"
                         " :psqldb, :parameters ()}"))

(describe "Form Data Parser"
  (context "parses form data by"
    (it "getting the key passed in"
      (should= :players
               (get-value-key example-form-1))
      (should= :difficulty
               (get-value-key example-form-2))
      )

    (it "getting the value of a given key"
      (should= :one-player
               (get-value example-form-1))
      (should= :10
               (get-value example-form-2)))

    (it "getting the previous game state"
      (should= {:players       nil, :difficulty nil,
                :round         1, :three-d? nil,
                :board         nil, :ui :wui,
                :save-location :edndb, :parameters ()}
               (get-game-state example-form-1))

      (should= {:players       nil, :difficulty 9,
                :round         6, :three-d? false,
                :board         [0 0 0 0 0 0 0 0 0], :ui :wui,
                :save-location :psqldb, :parameters ()}
               (get-game-state example-form-2)))
    )
  )