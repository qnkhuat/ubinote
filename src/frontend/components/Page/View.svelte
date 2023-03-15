<script>
	import { onMount } from "svelte";
	import { fromRange, toRange } from "dom-anchor-text-position";

	import { Loading } from "carbon-components-svelte";

	import * as api from "frontend/api.js";
	import { highlightRange } from "frontend/lib/highlight/higlight-dom-range";
	import AnnotationToolTip from "frontend/components/Page/AnnotationToolTip.svelte";

	//------------------------ props  ------------------------//
	export let page;
	let pageId = page.id;

	//------------------------ states  ------------------------//
	let pageDetail;
	let pageContent;
	let annotationToolTipContext; // `null` to turn off, `new` to create annotation, `edit` to edit
	let annotationToolTipPosition = {x: 0, y: 0};
	let annotations = {}; // {annotationId: removeHighlightFunction}
	let activeAnnotation = null; // for edit

	//------------------------ constants  ------------------------//

	const colorToCSS = {
		"pink": "highlight-pink",
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
	function annotateOnDOM(range, annotation) {
		const [_, removeHighlight] =  highlightRange(range, 'span',
			{
				class: colorToCSS[annotation.color],
				onclick: `onClickAnnotation(${annotation.id})`,
			})
		annotations[annotation.id] = {
			...annotation,
			remove: removeHighlight
		};
	}

	// call API to add an annotation
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
			return [range, resp.data];
		});
	}

	// call API to delete an annotation
	function deleteAnnotation(annotationId) {
		api.deleteAnnotation(annotationId).
			then((_err) => {
				// remove annotation on DOM
				annotations[annotationId]["remove"]()
			}).
			catch((err) => {
				console.error("Failed to delete annotation", annotationId, err)
			})
	}

	function onAnnotate(color) {
		return addAnnotation(pageId, window.getSelection(), color).
			then((resp) => {
				const [range, annotation] = resp;
				annotateOnDOM(range, annotation);
				window.getSelection().empty(); // remove users selection
			}).catch((err) => {
				console.log("Failed to add annotation", err);
			})
	}

	function rangeToToolTopPosition(range) {
		const boundingRect = range.getBoundingClientRect()
		return {
			x: event.clientX,
			y: boundingRect.bottom + window.scrollY
		}
	}

	//------------------------ reactive functions  ------------------------//

	$ : if (pageContent && pageDetail) {
		pageDetail.annotations?.forEach(annotation => {
			const range = toRangeBody(annotation.coordinate);
			try {
				annotateOnDOM(range, annotation);
			} catch(e) {
				console.error("Failed to annotate", annotation, e);
			}
		});
	}

	onMount(function loadContent() {
		api.getPageContent(pageId)
			.then(resp => {
				pageContent = resp.data;
			}).catch(err => {
				console.error("Failed to load page content: ", err);
			});

		document.addEventListener("mouseup", () => {
			// if user is selecting, show tooltip
			const selection = window.getSelection();
			if (isSelecting(selection)) {
				annotationToolTipPosition = rangeToToolTopPosition(selection.getRangeAt(0));
				annotationToolTipContext = "new";
			}
		});
	});

	onMount(function registerGlobalFunctions() {
		function onClickAnnotation(annotationId) {
			const annotation = annotations[annotationId];

			annotationToolTipPosition = rangeToToolTopPosition(toRangeBody(annotation.coordinate));
			annotationToolTipContext = "edit";
			activeAnnotation = annotation;
		}

		window.onClickAnnotation = onClickAnnotation;
	})

</script>

{#if pageContent}
	<div id="ubinote-page-content">
		{@html pageContent}
	</div>
{:else}
	<Loading />
{/if}

{#if annotationToolTipContext != null}
	<div>
		<AnnotationToolTip
			{...annotationToolTipPosition}
		context={annotationToolTipContext}
		onAnnotate={onAnnotate}
		onDelete={() => deleteAnnotation(activeAnnotation.id)}
		onClose={() => annotationToolTipContext = null}
		/>
	</div>
{/if}

<style lang="scss">
	#ubinote-page-content {
		position: relative;
			/* Make sures we respect the font setting of the page */
			:global(*) {
				font-family: inherit;
			}
	}

	/* highlight colors */
	/* color code taken from the Preview app on mac */
	:global(.highlight-yellow) {
		background-color: #FACD5AA6;
		cursor:pointer;
	}

	:global(.highlight-green) {
		background-color: #7CC868A6;
		cursor:pointer;
	}

	:global(.highlight-pink) {
		background-color: #FB5C89A6;
		cursor:pointer;
	}

	:global(.highlight-blue) {
		background-color: #69AFF0A6;
		cursor:pointer;
	}


</style>
