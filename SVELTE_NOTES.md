### Reactivity
local variable act like a state itself
use $ to declare function to keep varaibles in sync: `$: doubled = count * 2` it could be blocked too.
only updates dom when see assignments, array push or pop... will not trigger DOM reload

### props
define props using `extend let prop1 = 1;`


### events

a DOM can receive event by attribute: `on:click={handleclick}`
event modifiers `on:click|once={handleclickonce}`
components can have event by creating dispatch


### Bindings
you can bind a variable so that if a sub component modify the value, it'll modify the original value. like pointer I guess

bind can bind groups too
useful for when we need to capture input or selection I guess


### life cycle

onMount calls after component is rendered
if onMount returns a function, it's used as cleanup func

onDestroy calls when component is destroyed

beforeUpdate, afterUpdatea

Svelte batches update together, they have a function calls `tick` which will block the thread until the next current microtasks finished.

### stores
svelte stores could be accessed by normal JS!

stores could have multipe types:
- writable which makes store can `update` and `set`
- readable
- derived -- like inherit
- custom stores with method is pretty cool


You have to subscribe to values on stores, but also when you're done, you should unsubcribe it otherwise we'll get memory leak

but it gets boilterplate, so sometimes you could just use `$` to read a store value without subscribe to it

### actions


it has something called `use` directive, pretty cool but not understand what it's for
