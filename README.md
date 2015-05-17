# bootutils #

```clojure
[funcool/bootutils "0.1.0"]
```

A collection of tasks for deploy artifacts to clojars.


## Usage

Add `bootutils` to your `build.boot` dependencies and `require` the namespace:

```clj
(set-env! :dependencies '[[funcool/bootutils "0.1.0" :scope "test"]])
(require '[funcool.bootutils :refer :all])

(def +version+ "0.1.0-SNAPSHOT")

(task-options!
 pom  {:project     'funcool/bootutils
       :version     +version+
       :description "funcool boot utils"
       :url         "https://github.com/funcool/bootutils"
       :scm         {:url "https://github.com/funcool/bootutils"}
       :license     {"BSD (2 Clause)" "http://opensource.org/licenses/BSD-2-Clause"}})
```

This is a list of available tasks:

- build-jar: is a composition of `pom` and `jar` tasks.
- deploy-snapshot: build and deploy a snapshot artifact.
- deploy-release: build an deploy a release artifact.
