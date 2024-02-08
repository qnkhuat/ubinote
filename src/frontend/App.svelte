<script>
  import { onMount } from "svelte";
  import { Router } from "svelte-router-spa";
  import { Loading } from "carbon-components-svelte";

  import Layout from "frontend/components/Layout.svelte";
  import routes  from "frontend/routes";
  import { getCurrentUser } from "frontend/stores/user.js";
  import { getSessionProperties } from "frontend/stores/sessionProperties.js";
  let loaded = false;

  onMount(() => {
    getSessionProperties().then(() => {
      getCurrentUser()
        .catch((err) => {
          console.log("User not found");
        })
        .finally(() => {
          loaded=true;
        });
    })
  })
  // get user on first load so routes could determine if user is logged in

</script>


{#if loaded}
<Layout>
  <Router {routes}/>
</Layout>
{:else}
<Loading/>
{/if}

<style>

</style>
