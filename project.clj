(defproject promptus "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2202"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [om "0.6.2"]]
  :plugins [[lein-cljsbuild "1.0.3"]]
  :source-paths ["src"]
  :cljsbuild {:builds
              {:dev {:source-paths ["src"]
                     :compiler {:output-to "app.js"
                                :output-dir "out"
                                :optimizations :none
                                :source-map true}}}})
