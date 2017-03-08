(defproject sprint-planning "0.1.0-SNAPSHOT"
  :description "This project is meant to provide some commonly calculated values I use in sprint planning.  Simple right?"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]]
  :main ^:skip-aot sprint-planning.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev     {:dependencies [[midje "1.9.0-alpha6"]
                                      [im.chit/hara "2.4.8"] ; Needed to run the repl in intellij
                                      [org.clojure/test.check "0.9.0"]
                                      ]}})