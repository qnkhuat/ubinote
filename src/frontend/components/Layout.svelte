<script>
  import {
    Header,
    HeaderNavItem,
    HeaderUtilities,
    SkipToContent,
  } from "carbon-components-svelte";
  import { navigateTo } from "svelte-router-spa";
  import { currentUser, logout} from "frontend/stores/user.js";

  let currentUserValue;
  currentUser.subscribe(value => {currentUserValue = value});

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
    {#if currentUserValue}
      <HeaderNavItem on:click={(e) => navigateTo("/user")} text="User" />
      <HeaderNavItem on:click={(e) => onLogout(e)} text="Logout" />
    {/if}
  </HeaderUtilities>
</Header>

<div id="ubinote-content">
  <slot></slot>
</div>

<style lang="scss">
  :global(#ubinote-content) {
    padding-left: 0px !important;
    padding-right:0px !important;
    padding-top: 3rem !important;
    margin-left: 0px !important;
    margin-right: 0px !important;
  }
</style>
