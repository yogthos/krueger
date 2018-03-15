(ns krueger.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [krueger.core-test]))

(doo-tests 'krueger.core-test)

