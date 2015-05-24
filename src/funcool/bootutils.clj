(ns funcool.bootutils
  {:boot/export-tasks true}
  (:require [boot.core :refer :all :as core]
            [boot.pod  :as pod]
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
              :ensure-clean false)))

(deftask push-release
  "Deploy snapshot version to Clojars."
  [f file PATH str "The jar file to deploy."]
  (comp (clojars-credentials)
        (push :file file
              :ensure-release true
              :repo "deploy-clojars"
              :ensure-clean false)))

(deftask build-jar []
  (comp (pom)
        (jar)))

(deftask deploy-snapshot []
  (comp (build-jar)
        (push-snapshot)))

(deftask deploy-release []
  (comp (build-jar)
        (push-release)))


(def test-pod-deps
  '[[org.clojure/tools.namespace "0.2.10" :exclusions [org.clojure/clojure]]])

(defn init [fresh-pod]
  (doto fresh-pod
    (pod/with-eval-in
     (require '[clojure.test :as t]
              '[clojure.java.io :as io]
              '[clojure.tools.namespace.find :refer [find-namespaces-in-dir]])
     (defn all-ns* [& dirs]
       (mapcat #(find-namespaces-in-dir (io/file %)) dirs))
     (defn test-ns* [pred ns]
       (binding [t/*report-counters* (ref t/*initial-report-counters*)]
         (let [ns-obj (the-ns ns)]
           (t/do-report {:type :begin-test-ns :ns ns-obj})
           (t/test-vars (filter pred (vals (ns-publics ns))))
           (t/do-report {:type :end-test-ns :ns ns-obj}))
         @t/*report-counters*)))))


(deftask run-tests
  [n namespaces NAMESPACE #{sym} "The set of namespace symbols to run tests in."
   f filters    EXPR      #{edn} "The set of expressions to use to filter namespaces."
   s tname      NAME      sym    "The name symbol for filtering."]
  (let [worker-pods (pod/pod-pool (update-in (core/get-env) [:dependencies] into test-pod-deps) :init init)]
    (core/cleanup (worker-pods :shutdown))
    (core/with-pre-wrap fileset
      (let [worker-pod (worker-pods :refresh)
            namespaces (or (seq namespaces)
                           (pod/with-eval-in worker-pod
                             (all-ns* ~@(->> fileset
                                             core/input-dirs
                                             (map (memfn getPath))))))]
        (if (seq namespaces)
          (let [filterf `(~'fn [~'%]
                           (if '~tname
                             (= (:name (meta ~'%)) '~tname)
                             (and ~@filters)))
                summary (pod/with-eval-in worker-pod
                          (doseq [ns '~namespaces] (require ns))
                          (let [ns-results (map (partial test-ns* ~filterf) '~namespaces)]
                            (-> (reduce (partial merge-with +) ns-results)
                                (assoc :type :summary)
                                (doto t/do-report))))]
            (when (> (apply + (map summary [:fail :error])) 0)
              (throw (ex-info "Some tests failed or errored" summary))))
          (println "No namespaces were tested."))
        fileset))))
