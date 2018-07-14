(defproject krueger "0.1.0"

  :description "federated news"
  :url "https://github.com/yogthos/krueger"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[alpha-id "0.2"]
                 [buddy "2.0.0"]
                 [camel-snake-kebab "0.4.0"]
                 [cljsjs/react "16.4.0-0"]
                 [cljs-ajax "0.7.3"]
                 [compojure "1.6.1"]
                 [com.cognitect/transit-clj "0.8.309"]
                 [com.cognitect/transit-cljs "0.8.256"]
                 [com.draines/postal "2.0.2"]
                 [com.fasterxml.jackson.module/jackson-modules-java8 "2.9.6" :extension "pom"]
                 [conman "0.8.1"]
                 [cprop "0.1.11"]
                 [funcool/struct "1.3.0"]
                 [kee-frame "0.2.4"]
                 [luminus-immutant "0.2.4"]
                 [luminus-migrations "0.5.0"]
                 [luminus-nrepl "0.1.4"]
                 [luminus/ring-ttl-session "0.3.2"]
                 [metosin/compojure-api "1.1.12"]
                 [metosin/muuntaja "0.5.0"]
                 [metosin/ring-http-response "0.9.0"]
                 [mount "0.1.12"]
                 [org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.339" :scope "provided"]
                 [org.clojure/tools.cli "0.3.7"]
                 [org.clojure/tools.logging "0.4.1"]
                 [org.clojure/tools.reader "1.2.2"]
                 [org.postgresql/postgresql "42.2.2"]
                 [org.webjars/font-awesome "5.1.0"]
                 [prismatic/schema "1.1.9"]
                 [re-frame "0.10.5"]
                 [reagent "0.8.1"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-defaults "0.3.2"]
                 [secretary "1.2.3"]
                 [selmer "1.11.8"]
                 [metosin/reitit "0.1.3"]
                 [metosin/schema-tools "0.10.3"]
                 [cljsjs/semantic-ui-react "0.81.1-0"]
                 [cljsjs/react-dom "16.4.0-0"]
                 [com.taoensso/sente "1.12.0"]
                 [clojure.java-time "0.3.2"]]

  :min-lein-version "2.0.0"

  :source-paths ["src/clj" "src/cljc"]
  :test-paths ["test/clj"]
  :resource-paths ["resources" "target/cljsbuild"]
  :target-path "target/%s/"
  :main ^:skip-aot krueger.core
  :migratus {:store :database :db ~(get (System/getenv) "DATABASE_URL")}

  :plugins [[migratus-lein "0.5.4"]
            [lein-cljsbuild "1.1.5"]
            [lein-immutant "2.1.0"]]
  :clean-targets ^{:protect false}
[:target-path [:cljsbuild :builds :app :compiler :output-dir] [:cljsbuild :builds :app :compiler :output-to]]
  :figwheel
  {:http-server-root "public"
   :nrepl-port       7002
   :css-dirs         ["resources/public/css"]
   :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}


  :profiles
  {:uberjar {:omit-source true
             :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
             :cljsbuild
                          {:builds
                           {:min
                            {:source-paths ["src/cljc" "src/cljs" "env/prod/cljs"]
                             :compiler
                                           {:output-dir "target/cljsbuild/public/js"
                                            :output-to "target/cljsbuild/public/js/app.js"
                                            :source-map "target/cljsbuild/public/js/app.js.map"
                                            :optimizations :advanced
                                            :pretty-print false
                                            :closure-warnings
                                            {:externs-validation :off :non-standard-jsdoc :off}
                                            :externs ["react/externs/react.js"]}}}}


             :aot :all
             :uberjar-name "krueger.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev  {:jvm-opts ["-Dconf=dev-config.edn"]
                  :dependencies [[binaryage/devtools "0.9.10"]
                                 [com.cemerick/piggieback "0.2.2"]
                                 #_[day8.re-frame/re-frame-10x "0.3.3-react16"]
                                 [doo "0.1.10"]
                                 [expound "0.7.1"]
                                 [figwheel-sidecar "0.5.16"]
                                 [pjstadig/humane-test-output "0.8.3"]
                                 [prone "1.6.0"]
                                 [ring/ring-devel "1.6.3"]
                                 [ring/ring-mock "0.3.2"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.19.0"]
                                 [lein-doo "0.1.10"]
                                 [lein-figwheel "0.5.16"]]
                  :cljsbuild
                  {:builds
                   {:app
                    {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
                     :figwheel {:on-jsload "krueger.app/mount-components"}
                     :compiler
                                   {:main "krueger.app"
                                    :asset-path "/js/out"
                                    :output-to "target/cljsbuild/public/js/app.js"
                                    :output-dir "target/cljsbuild/public/js/out"
                                    :source-map true
                                    :optimizations :none
                                    :pretty-print true
                                    :closure-defines {"re_frame.trace.trace_enabled_QMARK_" true}
                                    :preloads [day8.re-frame-10x.preload]}}}}



                  :doo {:build "test"}
                  :source-paths ["env/dev/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:jvm-opts ["-Dconf=test-config.edn"]
                  :resource-paths ["env/test/resources"]
                  :cljsbuild
                  {:builds
                   {:test
                    {:source-paths ["src/cljc" "src/cljs" "test/cljs"]
                     :compiler
                                   {:output-to "target/test.js"
                                    :main "krueger.doo-runner"
                                    :optimizations :whitespace
                                    :pretty-print true}}}}

                  }
   :profiles/dev {}
   :profiles/test {}})
