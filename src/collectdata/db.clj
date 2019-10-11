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

(data_2_records (read_cpi_through_month "resources/CPI_through_month.csv"))

(defn insert-data
  [data table]
  (jdbc/insert-multi! db-conf table data))


;;(insert-data (data_2_records (read_cpi_through_month "resources/CPI_through_month.csv")) :eco_cpi_month)

(def get_all_eco_cpi_month_query
  "select *
  from eco_cpi_month where 1 = 1")

(def get_eco_cpi_month_on_year_query
  "select *
  from eco_cpi_month where year = ?")

(def get_years_query
  "select distinct year
  from eco_cpi_month where 1 = 1")

(jdbc/query db-conf [get_all_eco_cpi_month_query])



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

(map #(:cpi %) (jdbc/query db-conf [get_eco_cpi_month_on_year_query 2008]))

(->> (jdbc/query db-conf [get_eco_cpi_month_on_year_query 2006])
     (map #(:cpi %))
     (map #(/ % 100))
     (map #(ln %))
     (reduce +)
     (exp)
     (* 100)
     (- 100)
     (-))


(jdbc/query db-conf [get_years_query])

(defn get_years_list
  []
  (->> (jdbc/query db-conf [get_years_query])
       (map #(:year %))
       (sort)))

(get_years_list)

(defn get_cpi_by_year
  [year]
  (->> (jdbc/query db-conf [get_eco_cpi_month_on_year_query year])
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

;; Read Cpi from press start

(def cpi_years_press (range 1996 2019))
(def cpi_press '(4.5 3.6 9.2 0.1 -0.6 0.8 4.04 3.01 9.67 8.71 7.5 8.3 22.97 6.88 9.19 18.58 9.21 6.6 4.09 0.63 4.74 3.53 3.54))

(defn create-table
  "create db and table"
  [table_name table_scheme]
  (try (jdbc/db-do-commands db-conf
            (jdbc/create-table-ddl table_name table_scheme))
       (catch Exception e
         (println (.getMessage e)))))

(defn drop-table
  [table_name]
  (try (jdbc/db-do-commands db-conf
            (jdbc/drop-table-ddl table_name))
       (catch Exception e
         (println (.getMessage e)))))

;; Create table
;; (create-table :eco_cpi_press [[:year :int] [:cpi :real]])

;(insert-data (map (fn [year cpi] {:year year :cpi cpi})  cpi_years_press cpi_press ) :eco_cpi_press)

(def get_cpi_from_press_query
  "select *
  from eco_cpi_press where 1 = 1")

(def get_year_list_from_press_query
  "select distinct year
  from eco_cpi_press where 1 = 1 order by year asc")

(def get_cpi_from_press_by_year_query
  "select cpi
  from eco_cpi_press where year = ?")


(jdbc/query db-conf [get_cpi_from_press_query])

(jdbc/query db-conf [get_year_list_from_press_query])

(defn get_year_list_from_press
  []
  (->> (jdbc/query db-conf get_year_list_from_press_query)
       (map #(:year %))))

(get_year_list_from_press)

(defn get_cpi_list_from_press_by_years
  [year_list]
  (->> (map #(jdbc/query db-conf [get_cpi_from_press_by_year_query %]) year_list)
       (map #(first %))
       (map #(:cpi %))))

(get_cpi_list_from_press_by_years (get_year_list_from_press))

;;(map #(jdbc/query db-conf [get_cpi_from_press_by_year_query %]) (get_year_list_from_press))



;; Read cpi from press end

;; Read gdp start

;;(create-table :eco_gdp_year [[:year :int]
;;                             [:total :real]
;;                             [:kt_nha_nuoc :real]
;;                             [:kt_ngoai_nha_nuoc :real]
;;                             [:kt_tap_the :real]
;;                             [:kt_tu_nhan :real]
;;                             [:kt_ca_the :real]
;;                             [:kt_co_von_nn :real]
;;                             [:nong_lam_thuy :real]
;;                             [:khai_khoang :real]
;;                             [:cn_che_bien :real]
;;                             [:sx_pp_dien_khi_nuoc :real]
;;                             [:cc_nuoc_xl_rac_nuoc :real]
;;                             [:xay_dung :real]
;;                             [:bb_xe_dong_co :real]
;;                             [:van_tai :real]
;;                             [:dv_luu_tru :real]
;;                             [:truyen_thong :real]
;;                             [:tai_chinh_nh_bh :real]
;;                             [:bds :real]
;;                             [:khoa_hoc_cn :real]
;;                             [:hc_dv_ho_tro :real]
;;                             [:dcs :real]
;;                             [:giao_duc :real]
;;                             [:yte_xh :real]
;;                             [:nt_vchoi_gtri :real]
;;                             [:dv_khac :real]
;;                             [:lthue_cv_gd :real])

(def eco_gdp_year_key_vec [:year
                           :total
                           :kt_nha_nuoc
                           :kt_ngoai_nha_nuoc
                           :kt_tap_the
                           :kt_tu_nhan
                           :kt_ca_the
                           :kt_co_von_nn
                           :nong_lam_thuy
                           :khai_khoang
                           :cn_che_bien
                           :sx_pp_dien_khi_nuoc
                           :cc_nuoc_xl_rac_nuoc
                           :xay_dung
                           :bb_xe_dong_co
                           :van_tai
                           :dv_luu_tru
                           :truyen_thong
                           :tai_chinh_nh_bh
                           :bds
                           :khoa_hoc_cn
                           :hc_dv_ho_tro
                           :dcs
                           :giao_duc
                           :yte_xh
                           :nt_vchoi_gtri
                           :dv_khac
                           :lthue_cv_gd])

(count eco_gdp_year_key_vec)

(defn read_gdp_from_file
  [filename]
  (let [reader (io/reader filename)
        rawdata (->> (csv/read-csv reader)
                     (map #(drop-last %))
                     (apply map list)
                     (map drop-last)
                     (map drop-last)
                     (map drop-last)
                     (map drop-last)
                     (map #(zipmap eco_gdp_year_key_vec (vec %))))]
    rawdata))

(read_gdp_from_file "resources/GDP_2005_2018.csv")

;;(insert-data (read_gdp_from_file "resources/GDP_2005_2018.csv") :eco_gdp_year)


(def get_gdp_through_years_query
  "select year, total from eco_gdp_year where 1 = 1 order by year asc")

(jdbc/query db-conf [get_gdp_through_years_query])

(defn get_gdp_through_years
  []
  (jdbc/query db-conf [get_gdp_through_years_query]))


(get_gdp_through_years)


;; Read gdp end



















