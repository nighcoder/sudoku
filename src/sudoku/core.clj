(ns sudoku.core
  (:require [clojure.core.logic :as mk]
            [clojure.core.logic.fd :as fd]))

(def BOARD
  '(5 3 nil nil 7 nil nil nil nil,
    6 nil nil 1 9 5 nil nil nil,
    nil 9 8 nil nil nil nil 6 nil,
    8 nil nil nil 6 nil nil nil 3,
    4 nil nil 8 nil 3 nil nil 1,
    7 nil nil nil 2 nil nil nil 6,
    nil 6 nil nil nil nil 2 8 nil,
    nil nil nil 4 1 9 nil nil 5,
    nil nil nil nil 8 nil nil 7 9))
    
(defn lines
  [col]
  (partition 9 col))

(defn cols
  [col]
  (apply map list (lines col)))
 
(defn squares
  [col]
  (->> col
       lines
       (map (partial partition 3))
       (apply mapcat list)
       (partition 3)
       (map #(mapcat list* %))))
       
(defn init
  [vars board]
  (if (seq vars)
    (mk/all
      (if-let [elem (first board)]
        (mk/== (first vars) elem)
        mk/succeed)
      (init (rest vars) (rest board)))
    mk/succeed))

(defn solvero
  [board]
  (let [vars (repeatedly 81 mk/lvar)
        hor (lines vars)
        ver (cols vars)
        sqrs (squares vars)]
    (mk/run* [q]
         (mk/== q vars)
         (mk/everyg #(fd/in % (fd/domain 1 2 3 4 5 6 7 8 9)) vars)
         (init vars board)
         (mk/everyg fd/distinct hor)
         (mk/everyg fd/distinct ver)
         (mk/everyg fd/distinct sqrs))))
