(ns sudoku.server
  (:require [compojure.core :as com]
            [ring.middleware.params :as mw]
            [org.httpkit.server :as serv]
            [hiccup.core :as hic]
            [sudoku.core :as core]))

(defn table-gen
  [data]
  (let [data (map vector (range) data)
        cell (fn [idx v]
               (if v
                 [:td.hint [:input {:name idx :value v}]]
                 [:td [:input {:name idx :value " "}]]))
        sqs (core/sqs data)]
    [:form#state
      [:table
        (for [row (partition 3 sqs)]
          [:tr (for [table row]
                 [:td [:table
                        (for [row (partition 3 table)]
                          [:tr (map (partial apply cell) row)])]])])]]))

(def controls
  [:div.controls
    [:form#dashboard
      [:button {:formethod :get
                :formaction "/"
                :type :submit}
        "Generate"]
      [:button {:formmethod :post
                :formaction "/"
                :type :submit
                :form :state}
        "Solve"]
      [:button {:formmethod :post
                :formaction "/clear"
                :type :submit}
        "Clear"]]])

(defn clear
  [_]
  [:main
    [:div.main
      (table-gen (repeat 81 nil))
     controls]
    [:div.extra]])

(defn generate
  [_]
  (let [t0 (java.time.LocalDateTime/now)
        board (core/generator)
        dt (java.time.Duration/between t0 (java.time.LocalDateTime/now))]
    [:main
     [:div.main
          (table-gen board)
      controls]
     [:div.extra
       [:p (format "Table generated in %d ms." (.toMillis dt))]]]))

(defn presolve-parse
  [{:keys [form-params]}]
  (->> (range 81)
       (map str)
       (map #(get form-params %))
       (map clojure.string/trim)
       (map #(if (core/digit-str? %) (Integer/parseInt %) nil))))

(defn solve
  [board]
  (let [board (presolve-parse board)
        t0 (java.time.LocalDateTime/now)
        sols (core/solvero board)
        dt (java.time.Duration/between t0 (java.time.LocalDateTime/now))
        n (count sols)
        msol (->> sols
                 first
                 (map vector (range))
                 core/sqs)]
    [:main
     [:div.main
      [:form#state
       [:table
        (for [row (partition 3 msol)]
          [:tr
           (for [table row]
             [:td
              [:table
               (for [trow (partition 3 table)]
                 [:tr
                   (for [[idx v] trow]
                     [(if (get (apply vector board) idx) :td.hint :td.sol)
                      [:input {:name idx :value v}]])])]])])]]
      controls]
     [:div.extra
      [:p (format "Table solved in %d ms." (.toMillis dt))]
      [:p (case n
            1 "Solution is unique."
            5 "Found at least 5 solutions"
            (format "Found %d solutions" n))]]]))

(defn page
  [f]
  (fn [& args]
    (hic/html
     "<!doctype html>"
     [:html
       [:head
         [:meta {:charset "utf-8"}]
         [:title "Sudoku"]
         [:style
           "
           main {
             width: 600px;
             height: 600px;
           }
           div.main table {
             table-layout: fixed;
             width: 300px;
             height: 300px;
             border: 1px solid;
             border-collapse: collapse;
           }
           div.main table table {
             width: 100%;
             height: 100%;
           }
           td {
             width: 33.33%;
             text-align: center;
             border: 1px solid;
             padding: 0;
             overflow: hidden;
           }
           input {
             width: 100%;
             height: 100%;
             text-align: center;
             border: 0px solid;
           }
           td.hint {
             background-color: lightgray;
           }
           td.hint>input {
             background-color: lightgray;
           }
           td.sol>input {
               color: tomato;
           }"]]

       [:body
         [:header
           [:h1 "Sudoku Solver"]]
         (apply f args)
        [:footer
          [:p "Sudoku © 2019 Ionuț-Daniel Ciumberică"]]]])))

(com/defroutes routes
  (com/GET "/" _
           (page generate))
  (com/POST "/" request
            ((page solve) ((mw/wrap-params identity) request)))
  (com/POST "/clear" request
            (page clear)))

(defn run
  [port]
  (serv/run-server routes
                   {:port port}))

(defn -main
  [& args]
  (run 8080))
