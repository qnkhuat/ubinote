<script>
	import { onMount } from "svelte";
	import * as api from "frontend/api.js";
	import { highlightRange } from "frontend/lib/highlight/higlight-dom-range";
	import { fromRange, toRange } from "dom-anchor-text-position";

	//------------------------ props  ------------------------//
	export let pageId;

	//------------------------ constants  ------------------------//

	const colorToCSS = {
		"red": "highlight-red",
		"green": "highlight-green",
		"blue": "highlight-blue",
		"yellow": "highlight-yellow"
	}

	//------------------------ states  ------------------------//
	let pageDetail;
	let pageContent;

	//------------------------ utils  ------------------------//
	// from range wrt body
	const fromRangeBody = (range) => fromRange(document.body, range);
	// to range wrt body
	const toRangeBody = (range) => toRange(document.body, range);

	//------------------------ functions ------------------------//
	const addAnnotation = async (pageId, selection, color = "red") => {
		if (selection == null) {
			console.error("Attempted to add annotation when selection is null");
			return
		}

		const range = selection.getRangeAt(0);
		// need to calculate textpos before highlight, otherwise the position will be messed up
		// when try to highlight on re-load
		const textPos = fromRangeBody(range);

		// save it
		const resp = await api.createAnnotation({
			coordinate: textPos,
			page_id: pageId
		}).catch(err => {
			console.error("Failed to add annotaiton: ", err);
			return null;
		});

		let highlightElements;
		// if save successful, draw it
		if (resp)
			[highlightElements] = highlightRange(range, 'span', {class: colorToCSS[color]});
		return highlightElements;
	}

	//------------------------ reactive functions  ------------------------//

	// draw annotations once pageDetail is ready
	$ : if (pageDetail && pageContent) {
		pageDetail.annotations.forEach(annotation => {
			const range = toRangeBody(annotation.coordinate);
			highlightRange(range, 'span', {class: colorToCSS[annotation.color]});
		});
	}

	onMount(() => {
		api.getPage(pageId)
			.then(resp =>pageDetail = resp.data);

		api.getPageContent(pageId)
			.then(resp => pageContent = resp.data);

		document.addEventListener("mouseup", () => {
			const selection = window.getSelection();
			if (!selection.isCollapse) {
				addAnnotation(pageId, selection);
			}
			else {
			}
		});
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
		position: relative;
	}

	/* highlight colors */
	:global(.highlight-red) {
		color: red;
	}

	:global(.highlight-green) {
		color: green;
	}

	:global(.highlight-blue) {
		color: blue;
	}

	:global(.highlight-yellow) {
		color: yellow;
	}
</style>
