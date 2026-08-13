---
original_name: "2026.08.13.16.46.08.md (chatgpt export, lines 11-441)"
title: "Hiccup vs Helix: Choosing a Rendering Representation"
summary: "Compares Hiccup (data notation) vs Helix (direct React element construction) for a static-rendering DSL, and concludes SSR should reuse the existing Helix frontend rather than introduce a second HTML DSL."
category: "architecture"
created: "2026-08-13"
---

# you asked

cljs helix vs hiccup

---

# chatgpt response

## Signal

(世, p=0.98) **Helix and Hiccup solve different layers of the problem.**

- **Hiccup** is a *data notation for trees*:
  ```clojure
  [:div.card
   [:h1 "Hello"]
   [:button {:on-click save!} "Save"]]
  ```

- **Helix** is a *ClojureScript interface to React*:
  ```clojure
  ($ "div" {:className "card"}
    ($ "h1" "Hello")
    ($ "button" {:onClick save!} "Save"))
  ```

(己, p=0.95) For the static-rendering DSL you were just thinking about, **I would choose Hiccup as the canonical representation and treat Helix/React as one possible renderer**, rather than making Helix the representation.

## Evidence

(世, p=0.99) Hiccup's important property is that the tree is ordinary persistent Clojure data. You can inspect, transform, validate, serialize, diff, walk, or render it without React:

```clojure
(def page
  [:html
   [:body
    [:h1 "hello"]]])

(walk/postwalk transform page)
```

(世, p=0.98) Helix expressions directly construct React elements. That gives you excellent React interop and React's component/hook lifecycle, but you have crossed the abstraction boundary immediately:

```clojure
(defnc counter []
  (let [[n set-n] (hooks/use-state 0)]
    ($ :button
       {:onClick #(set-n inc)}
       (str n))))
```

(世, p=0.97) So roughly:

| Property | Hiccup | Helix |
|---|---|---|
| Representation | CLJ(S) data | React elements |
| React dependency | No, inherently | Yes |
| Easy tree transforms | Excellent | Poorer |
| Hooks/components | Renderer-dependent | Excellent |
| JS/React ecosystem interop | Indirect | Excellent |
| SSR/static HTML | Natural | React SSR |
| Portable across runtimes | Very high | React-bound |
| Compile-time DSL potential | Excellent | Possible, but less natural |

## Frames

(世, p=0.96) **Frame 1 — UI application:** If you're writing a React application and want idiomatic hooks, context, Suspense, npm React components, etc., Helix is the stronger primitive. It deliberately avoids much of Reagent's extra abstraction.

(世, p=0.98) **Frame 2 — document/tree DSL:** If what you're modeling is fundamentally a tree that might become HTML, markdown, React, terminal UI, static output, or something else, Hiccup is much stronger:

```clojure
[:article
 {:lang "en"}
 [:title "A thing"]
 [:section
  [:p "Some content"]]]
```

The renderer becomes:

```clojure
(render :html tree)
(render :react tree)
(render :text tree)
```

(己, p=0.94) **Frame 3 — hybrid:** The interesting architecture is probably:

```text
domain DSL
    ↓
canonical hiccup-ish tree
    ├──→ static HTML
    ├──→ React / Helix
    ├──→ markdown
    └──→ validation / transforms
```

rather than:

```text
Helix/React tree
    ↓
everything else
```

## Countermoves

(世, p=0.94) Don't equate **Hiccup with Reagent**. Reagent popularized Hiccup-style React rendering, but the notation itself does not require Reagent or even React.

(世, p=0.95) Also don't assume you need to reproduce traditional Hiccup exactly. For a new static DSL, you can retain the valuable property—**trees are immutable CLJS values**—while tightening the grammar:

```clojure
[:div {:class ["card" "selected"]}
  "hello"]
```

or even make components explicit:

```clojure
[:component Card {:selected? true}
  [:text "hello"]]
```

That can eliminate some of Hiccup's historical ambiguities around tags, components, props, and children.

## Next

(己, p=0.97) Define the **smallest renderer-independent Hiccup AST** first; Helix can then be a ~thin interpreter from that AST into React elements.

---

# you asked

ok so I guess I never fully understood what helix was then. helix is still essentially interacting with hiccup like data?

---

# chatgpt response

## Signal

(世, p=0.99) **Not quite. Helix does *not* primarily manipulate Hiccup data.** It gives ClojureScript syntax/macros for constructing **React elements directly**.

A Helix expression like:

```clojure
($ :div {:className "card"}
  ($ :span "hello"))
```

looks Lisp-y and tree-shaped, but conceptually it is much closer to:

```js
React.createElement(
  "div",
  { className: "card" },
  React.createElement("span", null, "hello")
)
```

than to:

```clojure
[:div {:class "card"}
 [:span "hello"]]
```

## Evidence

(世, p=0.99) The key distinction is **evaluation time**.

With Hiccup:

```clojure
(def x
  [:div {:class "card"}
   [:span "hello"]])
```

`x` is literally a Clojure vector. You can do:

```clojure
(first x)
;; => :div

(count x)
;; => 3

(update x 1 assoc :id "foo")
```

With Helix:

```clojure
(def x
  ($ :div {:className "card"}
    ($ :span "hello")))
```

after evaluation, `x` is a **React element object**, not a Clojure vector representing markup.

(世, p=0.98) So the superficial syntax is similar because both describe nested trees, but they have different semantics:

```text
Hiccup
CLJS data
   ↓
renderer interprets it later
   ↓
React / HTML / whatever
```

versus:

```text
Helix
CLJS expression
   ↓
construct React element now
   ↓
React
```

## Frames

(世, p=0.96) One useful way to think about Helix is: **JSX translated into idiomatic ClojureScript syntax**.

JSX:

```jsx
<Card selected={true}>
  <span>Hello</span>
</Card>
```

Helix:

```clojure
($ Card {:selected true}
  ($ :span "Hello"))
```

Hiccup:

```clojure
[Card {:selected true}
 [:span "Hello"]]
```

(世, p=0.95) That's why Helix can initially *feel* like Hiccup. Both exploit Lisp's natural tree syntax. But Hiccup says **"here is a tree as data"**, while Helix says **"call the React-element constructor in this tree shape."**

## Countermoves

(世, p=0.97) There is still plenty of ordinary Clojure data *around* Helix. Props can come from maps, children can be generated with `map`, components are normal CLJS functions, etc.:

```clojure
(for [item items]
  ($ Item {:item item}))
```

So you're still programming with ClojureScript data structures. It's specifically the **render tree itself** that isn't Hiccup data.

(世, p=0.96) This distinction is also why Helix tends to be thinner than Reagent. Reagent accepts Hiccup-like values and interprets them into React. Helix mostly lets you talk to React directly without that intermediate Hiccup interpretation layer.

## Next

(己, p=0.98) Mentally substitute **“Helix `$` ≈ JSX / `React.createElement`, not Hiccup”**; that one distinction makes most of Helix's design snap into place.

---

# you asked

I started this one projects frontend in helix already, and I was wanting to do some server side rendering for the CMS stuff, and didn't really want two competing html dsls

---

# chatgpt response

## Signal

(己, p=0.99) In that case, **I would not introduce Hiccup just for SSR**. You already picked Helix/React as the frontend rendering language, and React itself gives you the server-rendering path.

Helix intentionally stays very close to React: its goal is a small runtime and few additional semantics, with its macros constructing React components/elements. ([GitHub](https://github.com/lilactown/helix/blob/master/docs/motivation.md?utm_source=chatgpt.com))
So your architecture can stay:

```text
shared Helix components
        │
        ├── browser → react-dom/client → interactive UI
        │
        └── server  → react-dom/server → HTML
```

One HTML/component DSL. No parallel Hiccup templates.

## Evidence

(世, p=0.99) A component can remain ordinary Helix:

```clojure
(defnc article-page [{:keys [article]}]
  (d/main
    (d/h1 (:title article))
    (d/article (:body article))))
```

The browser consumes that React tree normally.

The SSR process consumes **the same component tree** and asks React's server renderer to turn it into HTML. That's exactly the normal React SSR model: render the React component tree to HTML server-side, then optionally hydrate it client-side. ([Patterns](https://www.patterns.dev/react/server-side-rendering/?utm_source=chatgpt.com))
That means you don't need this:

```text
CMS pages        → Hiccup
interactive app  → Helix
```

which would indeed give you two subtly different notions of:

```text
element
props
children
component
fragment
escaping
class names
events
```

## Frames

(己, p=0.97) There are actually **two kinds of CMS output** worth distinguishing.

For interactive CMS pages:

```text
Helix component
      ↓
SSR React
      ↓
HTML
      ↓
hydrate
```

For genuinely static CMS output:

```text
Helix component
      ↓
React server renderer
      ↓
HTML string/file
```

No hydration required.

So even static generation doesn't force a second DSL.

(世, p=0.96) The interesting boundary should probably be **below the view layer**, not between two markup DSLs:

```clojure
(def article
  {:title "Hello"
   :slug "hello"
   :blocks [...]})
```

Then:

```clojure
($ ArticlePage {:article article})
```

Your CMS/domain representation is data. Helix is merely its HTML/React projection.

That buys you most of what I was praising Hiccup for earlier **without making your canonical content representation Hiccup itself**.

## Countermoves

(世, p=0.95) The one thing I'd avoid is making the **React element tree** your CMS document model.

Instead of storing something like:

```clojure
[:article
 [:h1 "Foo"]
 [:p "bar"]]
```

or some Helix-equivalent tree, store semantic content:

```clojure
{:type :article
 :title "Foo"
 :blocks
 [{:type :paragraph
   :text "bar"}]}
```

Then your renderer is Helix:

```clojure
(defnc block [{:keys [type text] :as block}]
  (case type
    :paragraph (d/p text)
    :image     ($ ImageBlock {:block block})
    ...))
```

(己, p=0.98) That gives you the separation you were reaching toward:

```text
CMS AST / domain data
          ↓
        Helix
          ↓
        React
       ↙     ↘
 browser     SSR/static HTML
```

You retain one UI DSL **and** your CMS remains renderer-independent.

## Next

(己, p=0.99) Keep the existing Helix frontend, and make the CMS content model plain CLJS data rather than introducing Hiccup as a second rendering representation.

---

