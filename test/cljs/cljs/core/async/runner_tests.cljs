(ns cljs.core.async.runner-tests
  (:require [cljs.core.async :refer [buffer dropping-buffer sliding-buffer put! take! chan close!]]
            [cljs.core.async.impl.dispatch :as dispatch]
            [cljs.core.async.impl.buffers :as buff]
            [cljs.core.async.impl.protocols :refer [full? add! remove!]])
  (:require-macros [cljs.core.async.test-helpers :as h :refer [is= deftest testing runner]]
                   [cljs.core.async.macros :as m :refer [go]]))


(deftest runner-tests
  (testing "do blocks"
    (is= 42
        (runner (do (pause 42))))
    (is= 42
        (runner (do (pause 44)
                    (pause 42)))))
  (testing "if expressions"
    (is= true
        (runner (if (pause true)
                  (pause true)
                  (pause false))))
    (is= false
        (runner (if (pause false)
                  (pause true)
                  (pause false))))
    (is= true 
        (runner (when (pause true)
                  (pause true))))
    (is= nil
        (runner (when (pause false)
                  (pause true)))))
  
  (testing "loop expressions"
    (is= 100
        (runner (loop [x 0]
                  (if (< x 100)
                    (recur (inc (pause x)))
                    (pause x))))))
  
  (testing "let expressions"
    (is= 3
        (runner (let [x 1 y 2]
                  (+ x y)))))
  
  (testing "vector destructuring"
    (is= 3
         (runner (let [[x y] [1 2]]
                     (+ x y)))))

  (testing "hash-map destructuring"
    (is= 3
        (runner (let [{:keys [x y] x2 :x y2 :y :as foo} {:x 1 :y 2}]
                  (assert (and foo (pause x) y x2 y2 foo))
                  (+ x y)))))
  
  (testing "hash-map literals"
    (is= {:1 1 :2 2 :3 3}
        (runner {:1 (pause 1)
                 :2 (pause 2)
                 :3 (pause 3)})))
  (testing "hash-set literals"
    (is= #{1 2 3}
        (runner #{(pause 1)
                  (pause 2)
                  (pause 3)})))
  (testing "vector literals"
    (is= [1 2 3]
        (runner [(pause 1)
                 (pause 2)
                 (pause 3)])))
  (testing "dotimes"
      (is= 42 (runner
                (dotimes [x 10]
                  (pause x))
                42)))
  
  (testing "fn closures"
    (is= 42
        (runner
         (let [x 42
               _ (pause x)
               f (fn [] x)]
           (f)))))

  (testing "case"
    (is= 43
        (runner
         (let [value :bar]
           (case value
             :foo (pause 42)
             :bar (pause 43)
             :baz (pause 44)))))
    (is= :default
        (runner
         (case :baz
           :foo 44
           :default)))))
