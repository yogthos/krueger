(ns krueger.client-routes)

(def routes
  [["/" :home]
   ["/comments" :comments]
   ["/messages" :messages]
   ["/post/:id" :post]
   ["/posts/new" :submit-post]
   ["/profile" :profile]
   ["/submit" :submit]])