(ns ttt-web.server
  (:require [ttt-web.client-socket :as socket]))

(def server (atom nil))

(defn get-client-params [cSocket]
  (let [rp (.newRP (.getRPFactory @server) cSocket)
        response (.getResponseFactory @server)]
    {:rp          rp
     :response    response
     :socket      cSocket
     :socket-type :client}))

(defn server-loop [sSocket]
  (let [cSocket (.acceptClient @server sSocket)
        client-params (get-client-params cSocket)]
    (future (socket/handle-client client-params))
    (if (.isRunning @server)
      (recur sSocket)
      nil)))

(defn initialize [ap]
  (let [root (.getRoot ap)]
    (reset! server (new Server ap nil
                        (new ServerResponseFactory root)
                        (new ClientRPFactory)))
    (.initializeServer @server)))

(defn run-server [ap]
  (let [sever-socket (initialize ap)]
    (server-loop sever-socket)))

(defn start-server [& args]
  (let [arguments (if (nil? (first args)) "" args)]
    (let [ap (new ArgumentParser (into-array String (flatten arguments)))]
      (future (run-server ap)))))