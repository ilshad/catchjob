(ns catchjob.re)

(def date-regexp
  #"(^|\s|;)(0[1-9]|1[0-9]|2[0-9]|3[01]).(0[1-9]|1[012]).([0-9]{4})")

(defn find-date [s]
  (when-let [[_ _ d m y] (re-find date-regexp s)]
    {:day d :month m :year y}))

(def money-regexp #"(^|\s|;)\$\s*([0-9]+)")

(defn find-money [s]
  (when-let [[_ _ v] (re-find money-regexp s)]
    v))
