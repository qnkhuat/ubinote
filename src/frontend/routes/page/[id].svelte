<script>
	import { onMount } from "svelte";
	import * as api from "frontend/api.js";
	export let currentRoute;
	let pageDetail;
	let pageContent;
	const pageId = currentRoute.namedParams.id;
	console.log("PAGEID", pageId);

	onMount(() => {
		api.getPage(pageId).then(resp => {
			pageDetail = resp.data;
		}).then(() => {
		});
		api.getPageContent(pageId).then(resp => {
			console.log("GOT PAGE CONTENT", resp);
			pageContent = resp.data;
		});
	});

</script>

<div id="page-view">
	{#if pageContent}
		<div id="ubinote-page-content"
			dangerouslySetInnerHTML={{__html: pageContent}}>
		</div>
	{:else}
		<div>Loading...</div>
	{/if}
</div>
