<script>
	import { onMount } from "svelte";
	import { fromRange, toRange } from "dom-anchor-text-position";

	import { Loading } from "carbon-components-svelte";


	import * as api from "frontend/api.js";
	import { highlightRange } from "frontend/lib/highlight/higlight-dom-range";
	import CreateAnnotation from "frontend/components/Page/CreateAnnotation.svelte";

	//------------------------ props  ------------------------//
	export let pageId;

	//------------------------ states  ------------------------//
	let pageDetail;
	let pageContent;
	let showCreateAnnotation;
	let createAnnotationPosition = {x: 0, y: 0};
	let annotations = {};

	//------------------------ constants  ------------------------//

	const colorToCSS = {
		"red": "highlight-red",
		"green": "highlight-green",
		"blue": "highlight-blue",
		"yellow": "highlight-yellow"
	}


	//------------------------ utils  ------------------------//
	// from range wrt body
	function fromRangeBody (range) {
		return fromRange(document.body, range)
	};
	// to range wrt body
	function toRangeBody (range) {
		return toRange(document.body, range)
	};
	function isSelecting(selection) {
		const boundingRect = selection.getRangeAt(0).getBoundingClientRect();
		return !selection.isCollapsed && boundingRect.width > 2;
	}

	//------------------------ functions ------------------------//

	// highlight on DOM
	function annotateOnDOM(range, annotationId, color) {
		const [_, removeHighlight] =  highlightRange(range, 'span',
			{
				class: colorToCSS[color],
				onclick: `onClickAnnotation(${annotationId})`
			})
		annotations[annotationId] = removeHighlight;
	}

	// call API to add the annotation
	function addAnnotation(pageId, selection, color) {
		if (selection == null) {
			console.error("Attempted to add annotation when selection is null");
			return
		}

		const range = selection.getRangeAt(0);
		// need to calculate textpos before highlight, otherwise the position will be messed up
		// when try to highlight on re-load
		const textPos = fromRangeBody(range);

		// save it
		return api.createAnnotation({
			coordinate: textPos,
			page_id: pageId,
			color: color,
		}).then((resp) => {
			return [range, resp.data.id];
		});
	}

	function deleteAnnotation(annotationId) {
		api.deleteAnnotation(annotationId).
			then((_err) => {
				// remove annotation on DOm
				annotations[annotationId]()
			}).
			catch((err) => {
				console.error("Failed to delete annotation", annotationId, err)
			})
	}

	function onAnnotate(color) {
		return addAnnotation(pageId, window.getSelection(), color).
			then((resp) => {
				const [range, annotationId] = resp;
				annotateOnDOM(range, annotationId, color);
			}).catch((err) => {
				console.log("Failed to add annotation", err);
			})
	}


	//------------------------ reactive functions  ------------------------//

	$ : if (pageContent && pageDetail) {
		pageDetail.annotations.forEach(annotation => {
			const range = toRangeBody(annotation.coordinate);
			try {
				annotateOnDOM(range, annotation.id, annotation.color);
			} catch(e) {
				console.error("Failed to annotate", annotation);
			}
		});
	}

	onMount(function loadContent() {
		api.getPageContent(pageId)
			.then(resp => {
				pageContent = resp.data;
			}).catch(err => {
				console.error("Failed to load page content: ", err);
			}).then(() => {
				// load page detail after page content is loaded
				// so that we could render page content before render annotations
				api.getPage(pageId)
					.then(resp => {
						pageDetail = resp.data;
					}).catch(err => {
						console.error("Failed to load page detail: ", err);
					});
			});

		document.addEventListener("mouseup", () => {
			const selection = window.getSelection();
			if (isSelecting(selection)) {
				const boundingRect = selection.getRangeAt(0).getBoundingClientRect();
				// show the annotation at the middle of the bottom left of the selection
				createAnnotationPosition = {
					x: boundingRect.left + window.scrollX + boundingRect.width / 2,
					y: boundingRect.bottom + window.scrollY
				}
				showCreateAnnotation = true;
			}
			else {
				showCreateAnnotation = false;
			}
		});
	});


	onMount(function registerGlobalFunctions() {
		function onClickAnnotation(annotationId) {
			deleteAnnotation(annotationId)
		}

		window.onClickAnnotation = onClickAnnotation;
	})

</script>

{#if pageContent}
	<div id="page-content">
		{@html pageContent}
	</div>
{:else}
	<Loading />
{/if}

{#if showCreateAnnotation}
	<CreateAnnotation
	 {...createAnnotationPosition}
	 onClose={() => showCreateAnnotation = false}
	 onAnnotate={onAnnotate}
	 />
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
