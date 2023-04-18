<script>
	import { onMount } from "svelte";
	import Settings from "carbon-icons-svelte/lib/Settings.svelte";
	import {
		Button,
		Toggle,
		Modal,
		Loading,
		OverflowMenu,
		OverflowMenuItem
	} from "carbon-components-svelte";

	import PageView from "frontend/components/Page/View.svelte";
	import * as api from "frontend/api.js";

	export let currentRoute;

	let page;
	const pageUuid = currentRoute.namedParams.uuid;

	onMount(function loadPage() {
		api.getPublicPage(pageUuid).then((resp) => {
			page = resp.data;
		}).catch(err => {
			console.error("Failed to load page detail: ", err);
		});;
	})
</script>

{#if page}
	<div id="ubinote-page-view">
		<PageView page={page} isPublic={true}/>
	</div>
{:else}
	<Loading />
{/if}

<style lang="scss">
	#ubinote-page-settings {
		background-color: #f4f4f4;
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 3rem;
	}

</style>
