<script>
	import { onMount } from "svelte";
	import Settings from "carbon-icons-svelte/lib/Settings.svelte";
	import {
		CopyButton,
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
	let openPublicSettings = false;
	const pageId = parseInt(currentRoute.namedParams.id);

	function onTogglePublic(is_public) {
		if (is_public && page.public_uuid == null) {
			api.createPublicPage(pageId).then((data) => {
				page.public_uuid = data.data;
			});
		} else if (!is_public && page.public_uuid != null) {
			api.deletePublicPage(pageId).then((_data) => {
				page.public_uuid = null;
			});
		}
	}

	function publicPageURL() {
		const location = window.location;
		return `${location.protocol}//${location.host}/public/page/${page.public_uuid}`
	}

	onMount(function loadPage() {
		api.getPage(pageId).then((resp) => {
			page = resp.data;
		}).catch(err => {
			console.error("Failed to load page detail: ", err);
		});;
	})
</script>

{#if page}
	<div id="ubinote-page-view">
		<div id="ubinote-page-settings">
			<p>{page.title}</p>
			<OverflowMenu flipped iconDescription="Settings" icon={Settings}>
			<OverflowMenuItem text="Public settings" on:click={() => {openPublicSettings = true}}/>
			</OverflowMenu>
		</div>
		<PageView {page} />
	</div>
{:else}
	<Loading />
{/if}

<Modal
	bind:open={openPublicSettings}
	modalHeading={"Public settings"}
	danger={true}
	passiveModal={true}>

<Toggle toggled={page?.public_uuid != null}
				style="padding: 0.5rem;"
		on:toggle={(toggled) => onTogglePublic(toggled.detail.toggled)}/>
		{#if page?.public_uuid}
			<div style="display:flex; align-items: center; z-index:1000; overflow:hidden;">
				<span style="padding-right: 10px;">Public UUID: <a href={publicPageURL()}>{publicPageURL()}</span>
				<CopyButton text={publicPageURL()}/>
			</div>
		{/if}

</Modal>

<style lang="scss">
	#ubinote-page-settings {
		background-color: #f4f4f4;
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 0.5rem;
	}

</style>
