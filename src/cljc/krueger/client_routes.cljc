(ns krueger.client-routes)

(def routes
  [["/" :home]
   ["/admin" :admin]
   ["/comments" :comments]
   ["/messages" :messages]
   ["/post/:id" :post]
   ["/posts/new" :submit-post]
   ["/profile" :profile]
   ["/submit" :submit]])