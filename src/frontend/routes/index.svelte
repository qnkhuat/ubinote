<script>
	import { onMount } from 'svelte';

	import "carbon-components-svelte/css/white.css";
	import {
		Button,
		DataTable,
	} from "carbon-components-svelte";

	import { listPages } from "frontend/api.js";

	let pages = [];

	onMount(() => {
		listPages().then(resp => {
			pages = resp.data;
		});
	});

	// when click on a row, open the page in a new tab
	function openPage(row) {
		window.open(`/page/${row.detail.id}`, "_blank");
	}


</script>

<DataTable
	sortable
	on:click:row={openPage}
	headers={
		[
			{key: "domain", value: "Domain"},
			{key: "title", value: "Title"},
			{key: "url", value: "URL"},
			{key: "created_at", value: "Created At"},
			{key: "updated_at", value: "Updated At"},
		]
	}
	rows={pages}
/>
