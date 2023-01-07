<script>
	import { onMount } from 'svelte';
	import { navigateTo } from "svelte-router-spa";

	import "carbon-components-svelte/css/white.css";
	import {
		Button,
		DataTable,
	} from "carbon-components-svelte";

	import { listPages } from "frontend/api.js";

	let pages = [];

	onMount(() => {
		listPages().then(resp => {
			pages = resp.data.map((page) => {return {...page, ...{"open": page["id"]}}});
		});
	});

	// when click on a row, open the page in a new tab
	function openPage(cell) {
		if (cell.detail.key == "open")
			navigateTo("/page/" + cell.detail.value);
	}


</script>

<DataTable
	sortable
	on:click:cell={openPage}
	headers={[{key: "domain", value: "Domain"},
	{key: "title", value: "Title"},
	{key: "url", value: "URL"},
	{key: "created_at", value: "Created At"},
	{key: "updated_at", value: "Updated At"},
	{key: "open", value: "Open", display: (_) => "Open"}]}
	rows={pages}
	/>
