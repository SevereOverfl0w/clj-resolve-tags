(ns resolve-tags
  (:require
    [rewrite-clj.parser :refer [parse-file]]
    [io.dominic.ednup.core :as ednup]
    [clojure.tools.gitlibs :as gitlibs]))

(defn- resolve-git-dep
  [counter {:keys [git/url sha tag] :as git-coord}]
  (if (and (not sha) tag)
    (let [sha (gitlibs/resolve url tag)]
      (println "Resolved" tag "=>" sha "in" url)
      (swap! counter inc)
      (assoc git-coord :sha sha))
    git-coord))

(defn- resolve-git-deps
  [counter {:keys [deps] :as deps-map}]
  (let [f (partial resolve-git-dep counter)]
    (assoc deps-map
           :deps
           (reduce
             (fn [deps k]
               (if (get-in deps [k :git/url])
                 (update deps k f)
                 deps))
             deps
             (keys deps)))))

(defn -main
  [& [deps-file]]
  (let [counter (atom 0)]
    (spit deps-file
          (ednup/nm-string
            (resolve-git-deps
              counter
              (ednup/->NodeMap (parse-file deps-file)))))))

(comment
  (-main "not-deps.edn"))
