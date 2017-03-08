(ns sprint-planner.core-test
  (:use midje.sweet)
  (:require [clojure.test :refer :all]
            [sprint-planner.core :refer :all]
            [clojure.spec.test :as st]))




(facts "testing function number-of-days-in-sprint returns expected values"
       (fact " -> throws error when the input is negative"
             (number-of-days-in-sprint -1 ) => (throws AssertionError))
       (fact " -> run st/check"
             (let [check-result (st/check `number-of-days-in-sprint)]
               (and (= (count check-result) 1)
                    (get-in (nth check-result 0) [:clojure.spec.test.check/ret :result]))) => true))

(facts "testing function base-hours-in-sprint-per-dev"
       (fact " -> throws error when one or more of the inputs is negative"
             (base-hours-in-sprint-per-dev -1 1 1) => (throws AssertionError)
             (base-hours-in-sprint-per-dev 1 -1 1) => (throws AssertionError)
             (base-hours-in-sprint-per-dev 1 1 -1) => (throws AssertionError)
             (base-hours-in-sprint-per-dev -1 -1 -1) => (throws AssertionError)

             )
       (fact " -> run st/check"
             (let [check-result (st/check `base-hours-in-sprint-per-dev)]
               (and (= (count check-result) 1)
                    (get-in (nth check-result 0) [:clojure.spec.test.check/ret :result]))) => true))

(facts "testing function calculate-hours-for-developer"
       (fact " -> run st/check"
             (let [check-result (st/check `calculate-hours-for-developer)]
               (and (= (count check-result) 1)
                    (get-in (nth check-result 0) [:clojure.spec.test.check/ret :result]))) => true))


(facts "testing function calculate-hours"
       (fact " -> run st/check"
             (let [check-result (st/check `calculate-hours)]
               (and (= (count check-result) 1)
                    (get-in (nth check-result 0) [:clojure.spec.test.check/ret :result]))) => true))


(def test-team-001-modifiers "Returns team wide items affecting the sprint in a map"
  {; We don't want to work on holidays.
   ::company-holiday-days-during-sprint 0
   ::number-of-weeks-in-sprint           2
   ; Lost work days due to delayed start /early end of sprint"
   ::lost-work-days                      0
   ; Percentage of time allocated to non-sprint work (email, company meetings, reading)
   ::time-for-non-sprint-work 0.20
   ; Length of sprint planning (hours)
   ::sprint-hours 2
   ; Length of daily standup (hours)
   ::standup-hours 0.25
   ; Percentage of time allocated to unplanned work
   ::unplanned-work-modifier 0.40
   })

(def test-team-001 "Returns a List of maps representing the test team"
  (list {::name "Hulk" ::personal-vacation-days 0 ::gross-availability 0.5}))

(def test-team-003 "Returns a List of maps representing the test team"
  (list {::name "Hulk" ::personal-vacation-days 0 ::gross-availability 0.5}
        {::name "Captain America" ::personal-vacation-days 0 ::gross-availability 1}
        {::name "Thor" ::personal-vacation-days 0 ::gross-availability 1}))

;(facts "function calculate-hours returns expected values"
;       (fact "something"
;             ;(s/valid? ::team-modifiers test-team-001-modifiers) => true?
;
;             (calculate-hours-team ..team..) => 16
;             (provided (s/valid? ..team..) => true)
;             ))
