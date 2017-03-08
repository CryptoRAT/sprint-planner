(ns sprint-planner.core
  (:require [clojure.test.check.generators]
            [clojure.spec :as s]
            [clojure.spec.test :as st]
            [clojure.pprint :refer [pprint]])
  (:gen-class))

(s/def ::integer int?)
(s/def ::number number?)
(s/def ::non-negative #(>= % 0))
(s/def ::non-negative-integer (s/and ::integer ::non-negative))
(s/def ::non-negative-number (s/and ::number ::non-negative))

(s/def ::valid-weeks-in-year (s/int-in 0 53))
; I have arbitrarily decided that a 1 year sprint is sadly possible.  Actually, it just makes the
; calculations easier/logical.
(s/def ::valid-number-of-days-in-sprint (s/and ::non-negative-integer (s/int-in 0 367)))

(s/def ::company-holiday-days-during-sprint ::valid-number-of-days-in-sprint)
(s/def ::number-of-weeks-in-sprint ::valid-weeks-in-year)
(s/def ::lost-work-days ::valid-number-of-days-in-sprint)
(s/def ::time-for-non-sprint-work (s/double-in 0.0 1.0))
(s/def ::sprint-hours ::non-negative-integer)
; The team should spend very little time in standup, but for our purposes the
; max is the same as the number of hours in a day.
(s/def ::standup-hours (s/double-in 0.0 24.0))
(s/def ::unplanned-work-modifier (s/double-in 0.0 1.0))

(s/def ::team-modifiers (s/keys :req [::company-holiday-days-during-sprint
                                      ::number-of-weeks-in-sprint
                                      ::lost-work-days
                                      ::time-for-non-sprint-work
                                      ::sprint-hours
                                      ::standup-hours
                                      ::unplanned-work-modifier]))




(s/def ::name (s/and string? #(not (clojure.string/blank? %))))
(s/def ::personal-vacation-days ::valid-number-of-days-in-sprint)
(s/def ::gross-availability (s/double-in 0.0 1.0))
(s/def ::team-member (s/keys :req [::name
                                   ::personal-vacation-days
                                   ::gross-availability]))

(s/def ::time-for-sprint-work (s/keys :req [::hours
                                   ::days]))


(s/def ::team (s/coll-of ::team-member :kind list?))


(comment
  (s/exercise `number-of-days-in-sprint)
  (st/check `number-of-days-in-sprint)
  )

(s/fdef number-of-days-in-sprint
        :args (s/cat :number-of-weeks ::valid-weeks-in-year)
        :ret ::non-negative-integer)

(defn number-of-days-in-sprint
  "Calculates the number of days there are in the current sprint."
  [number-of-weeks]
  {:pre  [(s/valid? ::valid-weeks-in-year number-of-weeks)]
   :post [(s/valid? ::non-negative-integer %)]}
  (* 5 number-of-weeks))

(comment
  (s/exercise `base-hours-in-sprint-per-dev)
  (st/check `base-hours-in-sprint-per-dev)
  )
(s/fdef base-hours-in-sprint-per-dev
        :args (s/cat :sprint-days ::valid-number-of-days-in-sprint
                     :holidays-in-sprint ::valid-number-of-days-in-sprint
                     :days-lost-in-sprint ::valid-number-of-days-in-sprint)
        :ret ::integer)

(defn base-hours-in-sprint-per-dev
  "Calculates the base number of hours in a sprint for a full time engineer."
  [sprint-days holidays lost-days]
  {:pre  [(s/valid? ::valid-number-of-days-in-sprint sprint-days)
          (s/valid? ::valid-number-of-days-in-sprint holidays)
          (s/valid? ::valid-number-of-days-in-sprint lost-days)]
   :post [(s/valid? ::non-negative-integer %)]}

  ; get the number of days in the sprint
  (let [num-days (- sprint-days (+ holidays lost-days))
        hours-per-day 8]
    ; if there are less than 0 days then just say 0, else calculate by the total hours in a day.
    (if (neg? num-days)
      0
      (* hours-per-day num-days))))

(comment
  (s/exercise `calculate-hours-for-developer)
  (st/check `calculate-hours-for-developer)
  )
(s/fdef calculate-hours-for-developer
        :args (s/cat :developer ::team-member
                     :base-hours ::non-negative-number)
        :ret ::number)

(defn calculate-hours-for-developer
  "Determines how many hours the dev is available after subtracting all the overhead/vacation, etc."
  [developer base-hours]
  {:pre  [(s/valid? ::team-member developer)
          ; At first I thought base-hours needed to be non-negative.
          ; It is possible that there is so much time consumed by ceremonies that there is negative
          ; time available.  If this happens your team is in a bad place, but it is possible.
          (s/valid? ::number base-hours)]                   ;
   :post [(s/valid? ::number %)]}
  (let [vacation-days (get developer ::personal-vacation-days)
        availibility (get developer ::gross-availability)]
    (* (- base-hours (* vacation-days 8)) availibility)))

(comment
  (s/exercise `calculate-hours)
  (st/check `calculate-hours)
  )
(s/fdef calculate-hours
        :args (s/cat :team-modifiers ::team-modifiers
                     :team ::team)
        :ret ::time-for-sprint-work)

(defn calculate-hours
  "Given the team makeup and the way time the team needs for sprint planning and such, how much
  time is left for sprint work."
  [team-mods team]
  {:pre  [(s/valid? ::team-modifiers team-mods)
          (s/valid? ::team team)]
   :post [(s/valid? ::time-for-sprint-work %)]}


  (let [num-days (number-of-days-in-sprint (get team-mods ::number-of-weeks-in-sprint))
        holidays-in-sprint (get team-mods ::company-holiday-days-during-sprint)
        lost-days-in-sprint (get team-mods ::lost-work-days)
        percentage-of-time-for-non-sprint-work (get team-mods ::time-for-non-sprint-work)
        percentage-of-time-for-unplanned-work (get team-mods ::unplanned-work-modifier)
        time-for-sprint-standups (+ (* (get team-mods ::standup-hours) num-days))
        time-for-sprint-planning (get team-mods ::sprint-hours)
        base-hours-from-days (base-hours-in-sprint-per-dev num-days holidays-in-sprint lost-days-in-sprint)
        base-hours-minus-sprint-prep (- base-hours-from-days
                                        time-for-sprint-planning
                                        time-for-sprint-standups)
        time-set-aside-for-non-sprint-work (- 1 (+ percentage-of-time-for-non-sprint-work percentage-of-time-for-unplanned-work))
        base-hours-per-dev (* base-hours-minus-sprint-prep time-set-aside-for-non-sprint-work)
        ]

    (let [hours (reduce + (map #(calculate-hours-for-developer % base-hours-per-dev) team))]

      (hash-map ::hours (format "%.2f" (double hours))
           ::days (format "%.2f" (double (/ hours 8))))
      )

    )


  )



(comment
  (def example-team "Returns a List of maps representing the interweavers team"
    (list {::name "The Doctor" ::personal-vacation-days 0 ::gross-availability 1.0}
          {::name "Amy" ::personal-vacation-days 0 ::gross-availability 1}
          {::name "Rory" ::personal-vacation-days 0 ::gross-availability 1}
          {::name "River" ::personal-vacation-days 0 ::gross-availability 0.2})))



(comment
  (def example-team-modifiers "Returns team wide items affecting the sprint in a map"
    {; We don't want to work on holidays.
     ::company-holiday-days-during-sprint 0
     ::number-of-weeks-in-sprint          2
     ; Lost work days due to delayed start /early end of sprint"
     ::lost-work-days                     0
     ; Percentage of time allocated to non-sprint work (email, company meetings, reading)
     ::time-for-non-sprint-work           0.20
     ; Length of sprint planning (hours)
     ::sprint-hours                       2
     ; Length of daily standup (hours)
     ::standup-hours                      0.25
     ; Percentage of time allocated to unplanned work
     ::unplanned-work-modifier            0.40
     }))
