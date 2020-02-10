(ns collectdata.analyze
  (:require [collectdata.db :as db]
            [com.github.sebhoss.math :refer :all]
            [com.hypirion.clj-xchart :as c]))

(db/get_years_list)
(db/get_cpi_vs_year (db/get_years_list))
(db/get_year_list_from_press)
(db/get_cpi_list_from_press_by_years (db/get_year_list_from_press))

(db/get_gdp_through_years)

;;(range 2002 2016)

(c/view
 (c/xy-chart
  {"CPI by gso.gov.vn" {:x (vec (db/get_years_list))
                        :y (vec (db/get_cpi_vs_year (db/get_years_list)))
                        :style {:marker-type :triangle-up}
                        :marker-color :black
                        :line-color :green}
   "Data from press"   {:x (vec (db/get_year_list_from_press))
                        :y (vec (db/get_cpi_list_from_press_by_years (db/get_year_list_from_press)))
                        :style {:marker-type :triangle-down}
                        :marker-color :black
                        :line-color :orange}}

  {:title "CPI vs. Years"}))


;; Estimate gold price based on CPI

(->> (db/get_years_list)
     (filter #(>= % 2000))
     (db/get_cpi_vs_year)
     (map #(/ % 100))
     (map #(+ % 1))
     (map #(ln %))
     (reduce +)
     (exp)
     (* 300))
