# Clojure currying and partial application

> Split out of `open-hax/knoxx` `docs/notes/reference/clojure-currying-partial.md` (lines 11–324) at knoxx `0fdaae13` on 2026-09-30. Primary relationship: general Clojure reference. Why it left Knoxx: [knoxx-documentation-extraction.md](../lineage/knoxx-documentation-extraction.md).

# how do you curry a function in clojure?

In Clojure, you usually do **not** “curry” functions automatically the way Haskell does; the common idiom is to use `partial` for partial application, or to define a chain of one-argument functions when you want true currying.[^1_4][^1_5][^1_9]

## Common pattern

For most day-to-day Clojure code, `partial` is the right tool because it pre-fills some arguments and returns a new function.[^1_5][^1_10][^1_4]

```clojure
(defn add3 [a b c]
  (+ a b c))

(def add1 (partial add3 1))

(add1 2 3)
;; => 6
```

This is usually what people mean in practice when they ask about currying in Clojure, even though technically it is partial application rather than true currying.[^1_2][^1_4]

## True currying

If you want a genuinely curried function, define it as nested unary functions so each call returns the next function.[^1_9][^1_2]

```clojure
(defn curried-add3 [a]
  (fn [b]
    (fn [c]
      (+ a b c))))

(((curried-add3 1) 2) 3)
;; => 6
```

Each call to `curried-add3` accepts one argument and returns the next unary function, so the nested call form is required. `(f x y)` passes two arguments in a single call and is not equivalent to `((f x) y)` in Clojure. See the [Clojure function guide](https://clojure.org/guides/learn/functions).

## Why Clojure works this way

Clojure does not support automatic currying by default, and calling a function with too few arguments normally does not create another function for you.[^1_1][^1_4]

One reason commonly given is that Clojure leans heavily on fixed-arity and variadic functions, and automatic currying does not fit that model cleanly.[^1_11][^1_5]

For example, `(+ 3)` evaluates to `3`, not to a function that adds 3 to something else.[^1_4]

## Practical rule

Use `partial` when you just want to lock in some arguments and keep the code idiomatic.[^1_5][^1_4]

Write nested `fn` forms only when you specifically want a curried API, such as for functional composition experiments or teaching the concept.[^1_2][^1_9]

If you want, I can show three versions side by side: plain function, `partial`, and a reusable `curry` helper in idiomatic Clojure.
<span style="display:none">[^1_3][^1_6][^1_7][^1_8]</span>

<div align="center">⁂</div>

[^1_1]: https://dragan.rocks/articles/18/Fluokitten-080-Fast-function-currying-in-Clojure

[^1_2]: https://www.reddit.com/r/compsci/comments/wkjfv/function_currying_in_clojure_and_haskell/

[^1_3]: https://stackoverflow.com/questions/49493398/clojure-currying-technicalities

[^1_4]: https://practical.li/learn-clojure/thinking-functionally/partial-functions/

[^1_5]: https://groups.google.com/g/clojure/c/cE2FUrkPW8I

[^1_6]: https://stackoverflow.com/questions/31373507/rich-hickeys-reason-for-not-auto-currying-clojure-functions

[^1_7]: https://github.com/janetacarr/masala

[^1_8]: http://lorettahe.github.io/clojure/2016/09/22/clojure-auto-curry

[^1_9]: https://news.ycombinator.com/item?id=7052206

[^1_10]: http://xahlee.info/clojure/clojure_function_currying.html

[^1_11]: https://ask.clojure.org/index.php/8381/currying-consideration-early-clojures-design-development


---

# ```cljs

;; I want a macro like this
(defroute register-mcp-http-routes!
{:method "get"
:path "/.well-known/oauth-authorization-server"
:controller
'(let [issuer (js/URL. (.toString base))]
(.send reply
#js {:issuer (-> (.toString issuer) (.replace (js/RegExp. "/$") ""))
                  :authorization_endpoint (.toString (js/URL. "/api/mcp/oauth/authorize" issuer))
                  :token_endpoint (.toString (js/URL. "/api/mcp/oauth/token" issuer))
                  :registration_endpoint (.toString (js/URL. "/api/mcp/oauth/register" issuer))
                  :response_types_supported #js ["code"]
                  :grant_types_supported #js ["authorization_code"]
                  :code_challenge_methods_supported #js ["S256"]
                  :token_endpoint_auth_methods_supported #js ["none"]}))})
;; it'll expand something like this
(defn register-mcp-http-routes! [app runtime config]
  (defroute app .get "/.well-known/oauth-authorization-server"
    (let [issuer (js/URL. (.toString base))]
      (.send reply
             #js {:issuer (-> (.toString issuer) (.replace (js/RegExp. "/$") ""))
:authorization_endpoint (.toString (js/URL. "/api/mcp/oauth/authorize" issuer))
:token_endpoint (.toString (js/URL. "/api/mcp/oauth/token" issuer))
:registration_endpoint (.toString (js/URL. "/api/mcp/oauth/register" issuer))
:response_types_supported #js ["code"]
:grant_types_supported #js ["authorization_code"]
:code_challenge_methods_supported #js ["S256"]
:token_endpoint_auth_methods_supported #js ["none"]}))))

```

I'm not totally sure about it but... my agents are havin a hard time with node cause... imports.
No matter what I do, I endup with this server.mjs file when they get too confused to write the cljs

Yes — a macro can generate that `defn`, but the cleaner move is to keep the route body as ordinary CLJS code and have the macro only assemble the repetitive wrapper. In ClojureScript, macros run at compile time, must live in a macro namespace, and are mainly for defining new syntactic constructs rather than ordinary runtime behavior.[^2_1][^2_2][^2_3]

## Core distinction

What you sketched mixes two different ideas: one `defroute` that defines a route-registration function, and another `defroute` that seems to register a route on `app`. That is workable, but it will be easier for both humans and agents if the outer form is a macro like `defroutes` or `defhttp-route-fn`, while the inner runtime registration remains an ordinary function call.[^2_3][^2_1]

Also, your `:controller` value is quoted with `'(...)`, which means the macro will receive a literal list rather than executable body forms; that can be useful, but in this case it is usually simpler to pass the body directly as forms and splice them into the expansion with syntax quote and unquote. Syntax quote, unquote, and unquote-splicing are the standard tools for writing macros that generate code cleanly.[^2_4][^2_5][^2_1]

## A workable shape

A practical version is to make the macro accept the route name and a map, destructure the map, and emit a `defn` that performs the route registration. ClojureScript macros are defined with `defmacro`, and the CLJS docs note they must live in a macro namespace such as a `.clj` or `.cljc` file rather than ordinary runtime REPL code.[^2_3]

```clj
;; src/my/app/routes/macros.clj or .cljc
(ns my.app.routes.macros)

(defmacro defroute-fn
  [fname {:keys [method path controller]}]
  (let [method-sym (symbol (str "." method))]
    `(defn ~fname [app runtime config]
       (defroute app ~method-sym ~path
         ~controller))))
```

Then from your CLJS namespace:

```cljs
(ns my.app.routes
  (:require-macros [my.app.routes.macros :refer [defroute-fn]]))

(defroute-fn register-mcp-http-routes!
  {:method "get"
   :path "/.well-known/oauth-authorization-server"
   :controller
   (let [issuer (js/URL. (.toString base))]
     (.send reply
            #js {:issuer (-> (.toString issuer)
                             (.replace (js/RegExp. "/$") ""))
                 :authorization_endpoint
                 (.toString (js/URL. "/api/mcp/oauth/authorize" issuer))
                 :token_endpoint
                 (.toString (js/URL. "/api/mcp/oauth/token" issuer))
                 :registration_endpoint
                 (.toString (js/URL. "/api/mcp/oauth/register" issuer))
                 :response_types_supported #js ["code"]
                 :grant_types_supported #js ["authorization_code"]
                 :code_challenge_methods_supported #js ["S256"]
                 :token_endpoint_auth_methods_supported #js ["none"]}))})
```

That shape avoids the extra quote around `:controller`, so the body becomes part of the emitted code instead of data you must later unwrap manually.[^2_1][^2_4]

## If you want multiple body forms

If `:controller` sometimes needs several forms, make the macro accept route options plus `& body` instead of forcing the body into the map. This is usually more idiomatic because macros naturally handle raw forms as body arguments.[^2_2][^2_1]

```clj
(ns my.app.routes.macros)

(defmacro defroute-fn
  [fname {:keys [method path]} & body]
  (let [method-sym (symbol (str "." method))]
    `(defn ~fname [app runtime config]
       (defroute app ~method-sym ~path
         ~@body))))
```

Usage:

```cljs
(defroute-fn register-mcp-http-routes!
  {:method "get"
   :path "/.well-known/oauth-authorization-server"}
  (let [issuer (js/URL. (.toString base))]
    (.send reply
           #js {:issuer (-> (.toString issuer)
                            (.replace (js/RegExp. "/$") ""))
                :authorization_endpoint
                (.toString (js/URL. "/api/mcp/oauth/authorize" issuer))
                :token_endpoint
                (.toString (js/URL. "/api/mcp/oauth/token" issuer))
                :registration_endpoint
                (.toString (js/URL. "/api/mcp/oauth/register" issuer))
                :response_types_supported #js ["code"]
                :grant_types_supported #js ["authorization_code"]
                :code_challenge_methods_supported #js ["S256"]
                :token_endpoint_auth_methods_supported #js ["none"]})))
```

That version is likely the least confusing for agents, because the map contains only declarative metadata and the executable code stays where code normally lives.[^2_2][^2_1]

## Node and imports

Your “agents end up writing `server.mjs`” problem is probably not really about macros; it is more about Node module format friction. A documented shadow-cljs pain point is that `:target :node-script` produces CommonJS-style loading, and older Node configurations can throw `ERR_REQUIRE_ESM` when that code tries to `require()` an ESM-only dependency. Node 20.19, 22.12, and newer releases can require synchronous ESM graphs without the former experimental flag. Graphs containing top-level `await` instead raise `ERR_REQUIRE_ASYNC_MODULE`; use dynamic `import()` for those graphs. See [Node module loading documentation](https://nodejs.org/api/modules.html#loading-ecmascript-modules-using-require).

That means if your route code touches modern ESM-only npm packages, the agent often “escapes” into handwritten `.mjs` because Node’s module boundary is easier to satisfy there than from a CommonJS CLJS target. One documented route for real ESM output is `:target :esm` with `:js-options {:js-provider :import}`, which keeps imports as ESM rather than bundling them as CommonJS-style requires.[^2_6][^2_7]

## Recommended framing

Facts: CLJS macros can generate the wrapper function you want, they belong in a macro namespace, and Node ESM/CJS mismatch is a real separate issue when using shadow-cljs with Node targets.[^2_7][^2_3]

Interpretation: the macro is fine, but the bigger design win is to reduce how much “magic” the agent must infer — keep the macro shallow, keep route bodies unquoted, and decide explicitly whether your runtime target is CommonJS or ESM before trying to solve imports through syntax alone.[^2_2][^2_3]

Here is the version I would personally use because it minimizes hidden machinery:

```clj
;; macros.clj
(ns my.app.routes.macros)

(defmacro defroute-fn
  [fname {:keys [method path]} & body]
  (let [m (symbol (str "." method))]
    `(defn ~fname [app runtime config]
       (defroute app ~m ~path
         ~@body))))
```

```cljs
;; routes.cljs
(ns my.app.routes
  (:require-macros [my.app.routes.macros :refer [defroute-fn]]))

(defroute-fn register-mcp-http-routes!
  {:method "get"
   :path "/.well-known/oauth-authorization-server"}
  (let [issuer (js/URL. (.toString base))]
    (.send reply
           #js {:issuer (-> (.toString issuer)
                            (.replace (js/RegExp. "/$") ""))
                :authorization_endpoint (.toString (js/URL. "/api/mcp/oauth/authorize" issuer))
                :token_endpoint (.toString (js/URL. "/api/mcp/oauth/token" issuer))
                :registration_endpoint (.toString (js/URL. "/api/mcp/oauth/register" issuer))
                :response_types_supported #js ["code"]
                :grant_types_supported #js ["authorization_code"]
                :code_challenge_methods_supported #js ["S256"]
                :token_endpoint_auth_methods_supported #js ["none"]})))
```

Would you like a version that expands to Fastify `.get` directly, or one tailored to your existing `defroute` helper?
<span style="display:none">[^2_10][^2_11][^2_12][^2_13][^2_14][^2_15][^2_16][^2_17][^2_18][^2_19][^2_20][^2_21][^2_22][^2_23][^2_24][^2_25][^2_26][^2_8][^2_9]</span>

<div align="center">⁂</div>

[^2_1]: https://clojure-doc.org/articles/language/macros/

[^2_2]: https://ericnormand.me/mini-guide/when-to-use-a-macro-in-clojure

[^2_3]: https://dragan.rocks/articles/18/Fluokitten-080-Fast-function-currying-in-Clojure

[^2_4]: https://www.braveclojure.com/writing-macros/

[^2_5]: http://bryangilbert.com/post/code/clojure/anatomy-of-a-clojure-macro/

[^2_6]: https://widdindustries.com/blog/clojurescript-importmap.html

[^2_7]: https://clojureverse.org/t/generating-es-modules-browser-deno/6116

[^2_8]: https://clojureverse.org/t/a-macro-between-quote-and-syntax-quote/4466

[^2_9]: https://cljs.github.io/api/cljs.core/defmacro

[^2_10]: https://hackernoon.com/clojurescript-macros-a-not-so-long-journey-part-ii-u3183uqa

[^2_11]: https://www.youtube.com/watch?v=GBdkaFpO_1s

[^2_12]: https://stackoverflow.com/questions/13244550/clojure-macro-getting-syntax-quoted-namespace-qualified-symbol

[^2_13]: https://clojureverse.org/t/use-esm-with-node-shadow-cljs/9363

[^2_14]: https://stackoverflow.com/questions/76259989/how-to-import-npm-pacakge-from-clojurescript

[^2_15]: https://github.com/thheller/shadow-cljs/blob/master/doc/esm.md

[^2_16]: https://www.reddit.com/r/Clojure/comments/97cprw/i_do_not_use_macros_am_i_doing_it_wrong/

[^2_17]: https://github.com/thheller/shadow-cljs/issues/1219

[^2_18]: https://shadow-cljs.github.io/docs/UsersGuide.html

[^2_19]: https://stackoverflow.com/questions/75864233/how-to-import-a-shadow-cljs-project-into-another-shadow-cljs-project-with-a-diff

[^2_20]: https://gist.github.com/borkdude/7e548f06fbefeb210f3fcf14eef019e0

[^2_21]: https://github.com/thheller/shadow-cljs/issues/1102

[^2_22]: https://blog.arcjet.com/nodejs-22-support-esm-require-for-nestjs/

[^2_23]: https://ask.clojure.org/index.php/12333/cannot-import-certain-npm-modules-with-shadow-cljs

[^2_24]: https://www.metosin.fi/blog/2024-09-05-using-shadow-cljs-with-esbuild

[^2_25]: https://www.reddit.com/r/Clojure/comments/14jfs48/a_case_for_clojurescript_20/

[^2_26]: https://joyeecheung.github.io/blog/2024/03/18/require-esm-in-node-js/


---
