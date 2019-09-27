(ns collectdata.db
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [clojure.string :as str]))

;; Read cpi data
;; Create DB
(def db-conf {:classname "org.sqlite.JDBC"
              :subprotocol "sqlite"
              :subname "resources/database.db"})


(defn create-db
  "create db and table"
  []
  (try (jdbc/db-do-commands db-conf
            (jdbc/create-table-ddl :eco_cpi_month
                                             [[:year :int]
                                              [:month :int]
                                              [:cpi :real]]))

       (catch Exception e
         (println (.getMessage e)))))

(defn drop-eco_cpi_month
  []
  (try (jdbc/db-do-commands db-conf
            (jdbc/drop-table-ddl :eco_cpi_month))
       (catch Exception e
         (println (.getMessage e)))))

;;(create-db)
;;(drop-eco_cpi_month)

(defn read_cpi_through_month
  [filename]
  (let [reader (io/reader filename)
        rawdata (csv/read-csv reader)
        years (-> rawdata
                  first
                  rest)
        months (->> rawdata
                   rest
                   (map #(first %)))
        data (->> rawdata
                  rest
                  vec
                  (mapv #(-> %
                             rest
                             vec)))]
      {:years years
       :months months
       :data data}))

(let [result (read_cpi_through_month "resources/CPI_through_month.csv")
      data (:data result)]
  (get-in data [0 0]))

(defn data_2_records
  [{:keys [years months data]}]
  (let [result (transient [])]
    (doseq [iyear (range 24)
            imonth (range 12)]
      (conj! result {:year (+ 1995 iyear)
                     :month (+ 1 imonth)
                     :cpi (get-in data [imonth iyear])}))
    (persistent! result)))

(data_2_records (read_cpi_through_month "resources/CPI_through_month.csv"))


(conj () {:a 1
          :b 2
          :c 3})

(range 23)






