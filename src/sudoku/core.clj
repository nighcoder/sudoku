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

(defn digit-str?
  [x]
  (#{"1" "2" "3" "4" "5" "6" "7" "8" "9"} x))

(defn lines
  [col]
  (partition 9 col))

(defn cols
  [col]
  (apply map list (lines col)))

(defn sqs
  [col]
  (loop [i 0
         data col
         res []
         res0 []
         res1 []
         res2 []]
    (if (seq data)
     (cond
       (= 26 (mod i 27))
       (recur (inc i) (rest data) (conj res res0 res1 (conj res2 (first data))) [] [] [])
       (< (mod i 9) 3)
       (recur (inc i) (rest data) res (conj res0 (first data)) res1 res2)
       (< 2 (mod i 9) 6)
       (recur (inc i) (rest data) res res0 (conj res1 (first data)) res2)
       (< 5 (mod i 9))
       (recur (inc i) (rest data) res res0 res1 (conj res2 (first data))))
     res)))

(defn squares
  "Returns a list of of lists, each containing the elements of a 3x3 subtable"
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
  ([board] (solvero board 5))
  ([board n]
   (let [vars (repeatedly 81 mk/lvar)
         hor (lines vars)
         ver (cols vars)
         sqrs (squares vars)]
     (mk/run n [q]
          (mk/== q vars)
          (mk/everyg #(fd/in % (fd/domain 1 2 3 4 5 6 7 8 9)) vars)
          (init vars board)
          (mk/everyg fd/distinct hor)
          (mk/everyg fd/distinct ver)
          (mk/everyg fd/distinct sqrs)))))

(defn generator
  []
  (let [board (first (solvero (shuffle (range 1 10))))]
    (loop [board (into [] board)]
      (let [idx (->> board
                     (zipmap (range))
                     (filter #(int? (second %)))
                     (mapv first))]
        (if (= (count idx) 30)
          board
          (let [i (rand-nth idx)
                n (count (solvero (assoc board i nil) 2))]
            (if (= n 1)
             (recur (assoc board i nil))
             (recur board))))))))

; Unusable
(defn generatero
  []
  (let [vars (repeatedly 81 mk/lvar)
        board (first (solvero (shuffle (range 1 10))))]
    (mk/run 1 [q]
      (mk/== q vars)
      (mk/everyg
        #(mk/conde
           ((mk/nilo %))
           ((mk/== % (get (zipmap vars board) %)))) vars))))
