(defproject com.outpace/schema-transit "0.3.0-SNAPSHOT"
  :description "A library for serializing Prismatic Schema definitions with
  Transit."
  :url "http://github.com/outpace/schema-transit"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :deploy-repositories [["releases" :clojars]]
  :dependencies [[com.cognitect/transit-clj "0.8.259"]
                 [com.cognitect/transit-cljs "0.8.192"]
                 [prismatic/schema "0.3.7"]]
  :plugins [[com.cemerick/clojurescript.test "0.3.3"]
            [lein-cljsbuild "1.0.3"]]
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :cljsbuild {:test-commands {"unit" ["node" :node-runner
                                      "this.literal_js_was_evaluated=true"
                                      "target/unit-test.js"]}
              :builds
              {:generate {:source-paths ["src/clj" "src/cljs" "test/cljs"]
                          :compiler {:output-to "target/generate.js"
                                     :target :nodejs
                                     :optimizations :simple}}
               :dev {:source-paths ["src/clj" "src/cljs"]
                     :compiler {:output-to "target/main.js"
                                :target :nodejs
                                :optimizations :simple}}
               :test {:source-paths ["src/clj" "test/clj"
                                     "src/cljs" "test/cljs"]
                      :compiler {:output-to "target/unit-test.js"
                                 :optimizations :simple}}}}
  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.1.3"]
                                  [org.clojure/clojure "1.6.0"]
                                  [org.clojure/clojurescript "0.0-2371"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})
