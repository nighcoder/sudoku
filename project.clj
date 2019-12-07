(defproject sudoku "0.1.0-SNAPSHOT"
  :description "Simple sudoku solver"
  :url ""
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.520"]
                 [org.clojure/core.logic "0.8.11"]
                 [ring/ring-core "1.7.1"]
                 [http-kit "2.3.0"]
                 [hiccup "1.0.5"]
                 [compojure "1.6.1"]]
  :main ^:skip-aot sudoku.server
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
