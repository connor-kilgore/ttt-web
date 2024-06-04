(ns ttt-web.web-ui-spec
  (:require [speclj.core :refer :all]
            [ttt-web.web-ui :refer :all]
            [tic-tac-toe.option-selector :as menu]
            [tic-tac-toe.ui-interface :as ui]))
(describe "Web UI"
  (it "Gets the html file-name of a given set of menu options"
    (should= "game-mode.html" (ui/get-selection
                                {:options menu/menu-options :ui :wui}))))