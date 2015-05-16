(ns bootutils.core
  {:boot/export-tasks true}
  (:require [boot.core :refer :all]
            [boot.task.built-in :refer :all]))

(deftask clojars-credentials
  []
  (fn [next-handler]
    (fn [fileset]
      (let [clojars-creds (atom {})]
        (print "Username: ")
        (swap! clojars-creds assoc :username (read-line))
        (print "Password: ")
        (swap! clojars-creds assoc :password
               (apply str (.readPassword (System/console))))
        (merge-env!
         :repositories [["deploy-clojars" (merge @clojars-creds {:url "https://clojars.org/repo"})]])
        (next-handler fileset)))))

(deftask push-snapshot
  "Deploy snapshot version to Clojars."
  [f file PATH str "The jar file to deploy."]
  (comp (clojars-credentials)
        (push :file file
              :ensure-snapshot true
              :repo "deploy-clojars"
              :ensure-clean false
              :ensure-branch "master")))

(deftask push-release
  "Deploy snapshot version to Clojars."
  [f file PATH str "The jar file to deploy."]
  (comp (clojars-credentials)
        (push :file file
              :ensure-release true
              :repo "deploy-clojars"
              :ensure-clean true
              :ensure-branch "master")))

(deftask build-jar []
  (comp (pom)
        (jar)))

(deftask deploy-snapshot []
  (comp (build-jar)
        (push-snapshot)))

(deftask deploy-release []
  (comp (build-jar)
        (push-release)))

