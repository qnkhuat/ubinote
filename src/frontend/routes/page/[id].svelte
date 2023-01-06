<script>
	import { onMount } from "svelte";
	import * as api from "frontend/api.js";
	import PageView from "frontend/components/Page/View.svelte";

	export let currentRoute;
	const pageId = currentRoute.namedParams.id;
</script>

<div id="page-view">
	<PageView {pageId} />
</div>

