(ns ttt-web.ttt-response-spec
  (:require [speclj.core :refer :all]
            [tic-tac-toe.game-runner :as ttt]
            [ttt-web.ttt-response :refer :all])
  (:import (Core Packet)))

(def example-form-1 (str "players=one-player&game-state="
                         "{:players nil, :difficulty nil, :round 1,"
                         ":three-d? nil, :board nil, :ui :wui, :save-location"
                         " :edndb, :parameters ()}"))

(def example-form-2 (str "difficulty=10&game-state="
                         "{:players nil, :difficulty 9, :round 6,"
                         ":three-d? false, :board [0 0 0 0 0 0 0 0 0], :ui :wui, :save-location"
                         " :psqldb, :parameters ()}"))

(describe "TTT Response"
  (context "initializes arguments of"
    (it "ui setting :wui as the value no matter what"
      (should= :wui (:ui (get-new-game [])))
      (should= :wui (:ui (get-new-game ["--gui"]))))

    (it "save-location setting :cookies as the value no matter what"
      (should= :cookies (:save-location (get-new-game [])))
      (should= :cookies (:save-location (get-new-game ["--psqldb"]))))

    (it "any other arguments"
      (should= ["parameter1" "parameter2"]
               (:parameters (get-new-game ["parameter1" "parameter2"])))))


  (context "starts a new game on a"
    (it "GET request"
      (with-redefs [is-get-request? (fn [_] true)
                    start-game (fn [_] "game starting!")]
        (should= "game starting!" (generate-ttt-response nil ""))))

    (it "POST request"
      (with-redefs [is-get-request? (fn [_] false)
                    resume-game (fn [_] "game resuming!")]
        (should= "game resuming!" (generate-ttt-response nil "")))))

  (context "checks if an incoming request node is a"
    (it "GET request"
      (let [get-node (new Packet "GET /hello HTTP/1.1" nil nil)]
        (should (is-get-request? get-node))))

    (it "POST request"
      (let [post-node (new Packet "POST /hello HTTP/1.1" nil nil)]
        (should-not (is-get-request? post-node)))))

  (it "starts a new game with a new-game template"
    (with-redefs [ttt/get-new-game (fn [new-game _] new-game)]
      (should= {:players    nil, :difficulty nil,
                :round      1, :three-d? nil, :board nil,
                :ui         :wui, :save-location :cookies,
                :parameters ()} (start-game []))))

  (context "resumes the game by"
    (it "getting the game state from a given packet"
      (with-redefs [ttt/get-new-game (fn [game _] game)]
        (let [packet (new Packet nil nil (.getBytes example-form-1))]
          (should= {:players       :one-player, :difficulty nil,
                    :round         1, :three-d? nil,
                    :board         nil, :ui :wui,
                    :save-location :edndb, :parameters ()}
                   (update-game-from-packet packet)))

        (let [packet (new Packet nil nil (.getBytes example-form-2))]
          (should= {:players nil, :difficulty :10,
                    :round 6, :three-d? false,
                    :board [0 0 0 0 0 0 0 0 0], :ui :wui,
                    :save-location :psqldb, :parameters ()}
                   (update-game-from-packet packet)))))

    (it "falling back to making a new game if the board is not set"
      (with-redefs [update-game-from-packet (fn [_] {:board nil})
                    ttt/get-new-game (fn [_ _] "falling back to new game")]
        (should= "falling back to new game" (resume-game nil)))

      (with-redefs [update-game-from-packet (fn [_] {:board 1})
                    ttt/get-new-game (fn [_ _] "falling back to new game")]
        (should= "falling back to new game" (resume-game nil))))

    (it "giving the round output if the board exists"
      (with-redefs [update-game-from-packet (fn [_] {:board [0 0 0 0 0 0 0 0 0]})
                    get-round-output (fn [_] "output!")]
        (should= "output!" (resume-game nil))))))