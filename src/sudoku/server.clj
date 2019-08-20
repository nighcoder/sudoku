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
                 [:td [:input {:name " "}]]))
        sqs (core/squares data)
        subtb (fn [v]
                (let [v (partition 3 v)]
                  [:table (for [i (range 3)]
                            [:tr (map cell)])]))]
    [:form#state
      [:table
        (for [row (partition 3 sqs)]
          [:tr (for [table row]
                 [:td [:table
                        (for [row (partition 3 table)]
                          [:tr (map (partial apply cell) row)])]])])]]))

(defn page
  [board]
  (hic/html
    "<!doctype html>"
    [:html
      [:head
        [:meta {:charset "utf-8"}]
        [:title "Sudoku"]
        [:style
          "div.main table table {
             width: 100%;
             height: 100%;
           }
           td {
             width: 33.33%;
             height: 100%;
             text-align: center;
             border: 1px solid;
             padding: 0;
           }
           td.hint {
             background-color: lightgray;
             width: 33.33%;
             height: 100%;
           }
           td.hint>input {
             background-color: lightgray;
           }
           input {
             width: 100%;
             height: 100%;
             border: 1px solid;
             text-align: center;
           }
           div.main {
             width: 800px;
             height:800px;
           }
           div.main table {
             width: 40%;
             height: 40%;
             border: 1px solid;
             border-collapse: collapse;
           }"]]
      [:body
        [:header
          [:h1 "Sudoku Solver"]]
        [:main
          [:div.main
               (table-gen board)
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
                  "Clear"]]]]
         [:div.extra]]]
      [:footer
        [:p "Sudoku © 2019 Ionuț-Daniel Ciumberică"]]]))
            
(com/defroutes routes
  (com/GET "/" _ 
           (page '(5 3 nil nil 7 nil nil nil nil,
                   6 nil nil 1 9 5 nil nil nil,
                   nil 9 8 nil nil nil nil 6 nil,
                   8 nil nil nil 6 nil nil nil 3,
                   4 nil nil 8 nil 3 nil nil 1,
                   7 nil nil nil 2 nil nil nil 6,
                   nil 6 nil nil nil nil 2 8 nil,
                   nil nil nil 4 1 9 nil nil 5,
                   nil nil nil nil 8 nil nil 7 9)))
  (com/POST "/" request
            (mw/wrap-params 
              (fn [{:keys [form-params]}]
                (let [res (map #(get form-params %) (map str (range 81)))
                      int-res (map #(if  % (Integer/parseInt %)) res)]
                  (page (first (core/solvero int-res)))))))
                  
  (com/POST "/clear" request
            (page (repeat 81 nil))))

(defn run
  [port]
  (serv/run-server routes
                   {:ip "localhost"
                    :port port}))

(defn -main
  [& args]
  (run 8080))
