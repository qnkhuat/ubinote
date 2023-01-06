<script>
	import { onMount } from "svelte";
	import * as api from "frontend/api.js";
	export let currentRoute;
	let pageDetail;
	let pageContent;
	const pageId = currentRoute.namedParams.id;
	console.log(pageDetail);

	onMount(() => {
		api.getPage(pageId)
			.then(resp =>pageDetail = resp.data);

		api.getPageContent(pageId)
			.then(resp => pageContent = resp.data);

	});

</script>

<div id="page-view">
	{#if pageContent}
		<div id="page-content">
			{@html pageContent}
		</div>
	{:else}
		<div>Loading...</div>
	{/if}
</div>

<style>
	#page-content {
		width: 100%;
		positiion: relative;
	}

</style>
