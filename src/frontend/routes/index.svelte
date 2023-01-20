<script>
	import { onMount } from 'svelte';
	import { navigateTo } from "svelte-router-spa";
	import * as api from "frontend/api";

	import "carbon-components-svelte/css/white.css";
	import {
		Form,
		Button,
		DataTable,
		TextInput,
		InlineLoading,
		ToastNotification,
		Toolbar,
		ToolbarContent,
		ToolbarSearch,
	} from "carbon-components-svelte";

	import { listPages } from "frontend/api.js";


	//------------------------ states  ------------------------//
	let newPageURL = null;
	let pages = [];
	let addingPage = false;
	let notificationState = null;

	function openPage(cell) {
		if (cell.detail.key == "open")
			navigateTo("/page/" + cell.detail.value);
	}

	function loadPages() {
		listPages().then(resp => {
			let data = resp.data;
			pages = data
				.sort((a, b) =>	new Date(b.updated_at) - new Date(a.updated_at))
				.map((page) =>	Object.assign(page, {open: page["id"]}));
		});
	}

	function setNotificationState(state) {
		notificationState = state;
		setTimeout(() => {
			notificationState = null;
		}, 5000);
	}

	function newPage(e) {
		e.preventDefault();
		if(newPageURL) {
			addingPage = true;
			api.createPage({url: newPageURL}).
				then(function(resp) {
					loadPages();
					newPageURL = null;
					setNotificationState({
						kind: "success",
						title: `Page added`,
						subtitle: `New page added: ${resp.data.title}`,
					});
				})
				.catch(function(err) {
					setNotificationState({
						kind: "error",
						title: `Error adding page`,
						subtitle: `Failed to add page: ${JSON.stringify(err.response.data.errors)}`,
					})

				})
				.finally(function() {
					addingPage = false;
				});
		}
	}

	onMount(() => {
		loadPages();
	});

</script>

<div>
	<Form id="new-page">
		<TextInput placeholder="Archive a page" bind:value={newPageURL}/>
		<Button class="add-button" size="small" type="submit"
																					on:click={newPage}>
			{#if addingPage}
				<InlineLoading class="add-button" description="Adding..." />
			{:else}
				New
			{/if}
		</Button>
	</Form>

	<DataTable
	 sortable
	 on:click:cell={openPage}
	 headers={[
	 {key: "open", value: "Open", display: (_) => "Open", sort: false},
	 {key: "title", value: "Title"},
	 {key: "domain", value: "Domain"},
	 {key: "url", value: "URL"},
	 {key: "updated_at", value: "Last updated"},
	 ]}
	 rows={pages}>
	 <Toolbar>
		 <ToolbarContent>
			 <ToolbarSearch
			persistent
			shouldFilterRows={(row, value) => {
			return (row.title.toLowerCase().includes(value.toLowerCase()));
			}}
			/>
		 </ToolbarContent>
	 </Toolbar>
	</DataTable>

	{#if notificationState}
		<ToastNotification
			class="notification"
	 lowContrast
	 {...notificationState}
	 />
	{/if}

</div>

<style lang="scss">
	:global(#new-page) {
		margin-bottom: 20px;
		display: flex;
			:global(.bx--inline-loading__text) {
				color: white;
			}
	}

	:global(.add-button) {
		width: 100px;
	}

	:global(.notification) {
		position: absolute;
		left: 0;
		bottom: 0;
	}

</style>
