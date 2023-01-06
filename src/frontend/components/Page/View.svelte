<script>
	import { onMount } from "svelte";
	import * as api from "frontend/api.js";

	// props
	export let pageId;

	// state
	let pageDetail;
	let pageContent;

	onMount(() => {
		api.getPage(pageId)
			.then(resp =>pageDetail = resp.data);

		api.getPageContent(pageId)
			.then(resp => pageContent = resp.data);
	});

</script>

{#if pageContent}
<div id="page-content">
	{@html pageContent}
</div>
{:else}
<div>Loading...</div>
{/if}

<style>
	#page-content {
		width: 100%;
		positiion: relative;
	}
</style>
