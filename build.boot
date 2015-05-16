(set-env!
 :resource-paths #{"src"}
 :dependencies '[[boot/core "2.0.0-rc14" :scope "provided"]
                 [org.clojure/clojure "1.7.0-beta3" :scope "provided"]])

(require '[bootutils.core :refer :all])

(def +version+ "0.1.0-SNAPSHOT")

(task-options!
 pom  {:project     'funcool/bootutils
       :version     +version+
       :description "funcool boot utils"
       :url         "https://github.com/funcool/bootutils"
       :scm         {:url "https://github.com/funcool/bootutils"}
       :license     {"BSD (2 Clause)" "http://opensource.org/licenses/BSD-2-Clause"}})
