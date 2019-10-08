(ns collectdata.db
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [com.github.sebhoss.math :refer :all]
            [clojure.string :as str]))


;; Read cpi data start
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

;;(data_2_records (read_cpi_through_month "resources/CPI_through_month.csv"))

(defn insert-data
  [data table]
  (jdbc/insert-multi! db-conf table data))


;;(insert-data (data_2_records (read_cpi_through_month "resources/CPI_through_month.csv")) :eco_cpi_month)

(def get_all_eco_cpi_month
  "select *
  from eco_cpi_month where 1 = 1")

(def get_eco_cpi_month_on_year
  "select *
  from eco_cpi_month where year = ?")

(def get_years
  "select distinct year
  from eco_cpi_month where 1 = 1")

(jdbc/query db-conf [get_all_eco_cpi_month])



;Nếu Po là mức giá cả trung bình của kỳ hiện tại và P-1 là mức giá của kỳ trước, thì tỷ lệ lạm phát của kỳ hiện tại là:

;            Tỷ lệ lạm phát = 100% x   Po – P-1
;                                         P-1

;Có một số công thức khác nữa, ví dụ:

;            Tỷ lệ lạm phát = (log Po - log P-1) x 100%)

;Về phương pháp tính ra tỷ lệ lạm phát, hai phương pháp thường được sử dụng là:

;    căn cứ thời gian: đo sự thay đổi giá cả của giỏ hàng hóa theo thời gian
;    căn cứ thời gian và cơ cấu giỏ hàng hóa. Phương pháp này ít phổ biến hơn vì còn phải tính toán sự thay đổi cơ cấu, nội dung giỏ hàng hóa.)

;Thông thường, số liệu tỷ lệ lạm phát được công bố trên báo chí hàng năm được tính theo cách cộng phần trăm tăng CPI của từng tháng trong năm.

; a = x1 * x2 * x3 * ....
; log a = log x1 + log x2 + ....
; a = e^(log x1 + log x2 + ...)

(map #(:cpi %) (jdbc/query db-conf [get_eco_cpi_month_on_year 2008]))

(->> (jdbc/query db-conf [get_eco_cpi_month_on_year 2006])
     (map #(:cpi %))
     (map #(/ % 100))
     (map #(ln %))
     (reduce +)
     (exp)
     (* 100)
     (- 100)
     (-))


(jdbc/query db-conf [get_years])

(defn get_years_list
  []
  (->> (jdbc/query db-conf [get_years])
       (map #(:year %))
       (sort)))

(get_years_list)

(defn get_cpi_by_year
  [year]
  (->> (jdbc/query db-conf [get_eco_cpi_month_on_year year])
       (map #(:cpi %))
       (map #(/ % 100))
       (map #(ln %))
       (reduce +)
       (exp)
       (* 100)
       (- 100)
       (-)))

(map #(get_cpi_by_year %) (get_years_list))

(defn get_cpi_vs_year
  [years]
  (map #(get_cpi_by_year %) years))

(get_cpi_vs_year (get_years_list))



;; Read Cpi data end

;; Read


















