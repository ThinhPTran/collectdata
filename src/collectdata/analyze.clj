(ns collectdata.analyze
  (:require [collectdata.db :as db]
            [com.hypirion.clj-xchart :as c]))

(db/get_years_list)
(db/get_cpi_vs_year (db/get_years_list))
(def cpi_years_press (range 1996 2019))
(def cpi_press '(4.5 3.6 9.2 0.1 -0.6 0.8 4.04 3.01 9.67 8.71 7.5 8.3 22.97 6.88 9.19 18.58 9.21 6.6 4.09 0.63 4.74 3.53 3.54))

;;(range 2002 2016)

(c/view
 (c/xy-chart
  {"CPI by gso.gov.vn" {:x (vec (db/get_years_list))
                        :y (vec (db/get_cpi_vs_year (db/get_years_list)))
                        :style {:marker-type :triangle-up}
                        :marker-color :black
                        :line-color :green}
   "Data from press"   {:x (vec cpi_years_press)
                        :y (vec cpi_press)
                        :style {:marker-type :triangle-down}
                        :marker-color :black
                        :line-color :orange}}

  {:title "CPI vs. Years"}))


