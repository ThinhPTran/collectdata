(ns collectdata.analyze
  (:require [collectdata.db :as db]
            [com.hypirion.clj-xchart :as c]))

(db/get_years_list)
(db/get_cpi_vs_year (db/get_years_list))


(c/view
 (c/xy-chart
  {"CPI by gso.gov.vn" {:x (vec (db/get_years_list))
                        :y (vec (db/get_cpi_vs_year (db/get_years_list)))
                        :style {:marker-type :triangle-up}
                        :marker-color :black
                        :line-color :green}}
  {:title "CPI vs. Years"}))
