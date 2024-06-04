(ns ttt-web.core
  (:require [ttt-web.ttt-response :as tttr]
            [ttt-web.cookies])
  (:import (Core Main)))

(defn -main [& args]
  (Main/main (into-array String args))
  (Main/addFeature "/ttt" (tttr/->ttt-response-handler args))
  )