<script>
  import { Router } from 'svelte-router-spa'
  import routes  from 'frontend/routes'
  import { getCurrentUser } from "frontend/stores/user.js";
  let loaded = false;

  // get user on first load so routes could determine if user is logged in
  getCurrentUser().then(() => {
    loaded = true;
  });

</script>


{#if loaded}
<Router {routes}/>
{:else}
<div>Loading...</div>
{/if}

<style>
</style>
