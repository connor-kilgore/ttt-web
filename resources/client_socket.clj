(ns ttt-web.client-socket
  (:require [ttt-web.ttt-response :as ttt])
  (:import (Core SocketHandler)))

(defn run-normal-request [client-params]
  (let [socketHandler (new SocketHandler (:response client-params)
                           (:rp client-params))]
  (.handleClient socketHandler)))

(defn run-ttt-request [client-params]
  (let [type (.getRequestType (:rp client-params))]
    (ttt/send-response {:cp client-params :type type})))

(defn handle-client [client-params]
  (.buildBody (:rp client-params))
  (if (= (.getPath (:rp client-params)) "/ttt")
    (run-ttt-request client-params)
    (run-normal-request client-params))
  (.close (:socket client-params)))