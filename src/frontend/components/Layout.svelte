<script>
  import {
    Header,
    HeaderNavItem,
    HeaderUtilities,
    SkipToContent,
  } from "carbon-components-svelte";

  import { navigateTo } from "svelte-router-spa";

  import {logout} from "frontend/stores/user.js";

  function onLogout(_e) {
    logout().then(() => {
      navigateTo("/login");
    });
  }

</script>

<Header platformName="Ubinote" href="/">
  <svelte:fragment slot="skip-to-content">
    <SkipToContent />
  </svelte:fragment>
  <HeaderUtilities>
    <HeaderNavItem on:click={(e) => onLogout(e)} text="Logout" />
  </HeaderUtilities>
</Header>

<div id="content">
  <slot></slot>
</div>

<style lang="scss">
  :global(#content) {
    padding-left: 0px !important;
    padding-right:0px !important;
    padding-top: 3rem !important;
    margin-left: 0px !important;
    margin-right: 0px !important;
  }
</style>
