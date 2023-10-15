(ns ubinote.api.comment
  (:require
   [compojure.coercions :refer [as-int]]
   [compojure.core :refer [context defroutes POST GET DELETE]]
   [toucan2.core :as tc]
   [ubinote.api.common :as api]
   [ubinote.models.common.schema :as schema]))

(def NewComment
  [:map
   [:annotation_id schema/IntegerGreaterThanZero]
   [:creator_id schema/IntegerGreaterThanZero]
   [:content schema/NonBlankString]])

(defn create-comment
  [{:keys [body] :as _req}]
  (let [{annotation-id :annotation_id} body
        _   (api/check-404 (tc/exists? :m/annotation annotation-id))
        cmt (assoc body :creator_id api/*current-user-id*)]
    (schema/validate-schema cmt NewComment)
    (first (tc/insert-returning-instances! :m/comment cmt))))

(defn get-comment
  [id _req]
  (-> (tc/select-one :m/comment :id id)
      (tc/hydrate :annotation :user)
      api/check-404))

(defn- delete-comment
  [id _req]
  (api/check-404 (tc/select-one :m/comment :id id))
  (tc/delete! :m/comment :id id)
  api/generic-204-response)

(defroutes routes
  (POST "/" [] create-comment)
  (context "/:id" [id :<< as-int]
           (GET "/" [] (partial get-comment id))
           (DELETE "/" [] (partial delete-comment id))))
