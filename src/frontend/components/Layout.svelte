<script>
  import {
    Header,
    HeaderNavItem,
    HeaderUtilities,
    SkipToContent,
    Content,
  } from "carbon-components-svelte";

  import { navigateTo } from "svelte-router-spa";

  import {logout} from "frontend/stores/user.js";

  function onLogout(e) {
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

<Content>
  <slot></slot>
</Content>
