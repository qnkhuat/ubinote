<script>
	import { onMount } from "svelte";
	import { fromRange, toRange } from "dom-anchor-text-position";

	import * as api from "frontend/api.js";
	import { highlightRange } from "frontend/lib/highlight/higlight-dom-range";
	import AnnotationToolTip from "frontend/components/Page/AnnotationToolTip.svelte";

	//------------------------ props  ------------------------//
	export let page;
	export let isPublic = false; // is this page a public page?
	let pageId = isPublic ? page.public_uuid : page.id ;

	//------------------------ states  ------------------------//
	let annotationToolTipContext; // `null` to turn off, `new` to create annotation, `edit` to edit
	let annotationToolTipPosition = {x: 0, y: 0};
	let annotations = {}; // {annotationId: removeHighlightFunction}
	let activeAnnotation = null; // for edit
	let iframeWindow = null; // store the pointer for iframeWindow, should get filled on iframe load

	//------------------------ constants  ------------------------//

	const colorToCSS = {
		"pink": "highlight-pink",
		"green": "highlight-green",
		"blue": "highlight-blue",
		"yellow": "highlight-yellow"
	}

	//------------------------ utils  ------------------------//
	function resizeIframe(obj) {
    obj.style.height = obj.contentWindow.document.documentElement.scrollHeight + 'px';
  }
	// from range wrt body
	function fromRangeBody (range) {
		return fromRange(iframeWindow?.document.body, range)
	};

	// to range wrt body
	function toRangeBody (range) {
		return toRange(iframeWindow?.document.body, range)
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
		return addAnnotation(pageId, iframeWindow.getSelection(), color).
			then((resp) => {
				const [range, annotation] = resp;
				annotateOnDOM(range, annotation);
				iframeWindow.getSelection().empty(); // remove users selection
			}).catch((err) => {
				console.error("Failed to add annotation", err);
			})
	}

	function rangeToToolTopPosition(event, range) {
		const boundingRect = range.getBoundingClientRect()
		return {
			x: event.clientX,
			y: boundingRect.bottom + document.getElementById("ubinote-iframe-content").getBoundingClientRect().top
		}
	}

	//------------------------ reactive functions  ------------------------//

	function onIframeLoad() {
		// step 0: update iframe Document state
		iframeWindow = document.getElementById("ubinote-iframe-content").contentWindow;
		resizeIframe(this);

		// step 1 : inject css
		const style = iframeWindow.document.createElement("style");
		style.textContent = `
.highlight-yellow {
	background-color: #FACD5AA6;
	cursor:pointer;
}

.highlight-green {
	background-color: #7CC868A6;
	cursor:pointer;
}

.highlight-pink {
	background-color: #FB5C89A6;
	cursor:pointer;
}

.highlight-blue {
	background-color: #69AFF0A6;
	cursor:pointer;
}
			`
		iframeWindow.document.head.appendChild(style);

		// step 2:
		page.annotations?.forEach(annotation => {
			const range = toRangeBody(annotation.coordinate);
			try {
				annotateOnDOM(range, annotation);
			} catch(e) {
				console.error("Failed to annotate", annotation, e);
			}
		});

		// step 3: inject mouse up tracker

		if (!isPublic) {
			iframeWindow.document.addEventListener("mouseup", (event) => {
				// if user is selecting, show tooltip
				const selection = iframeWindow?.getSelection();
				if (isSelecting(selection)) {
					annotationToolTipPosition = rangeToToolTopPosition(event, selection.getRangeAt(0));
					annotationToolTipContext = "new";
				}
			})
		}

		// step 4: inject click on annotation tracker
		function onClickAnnotation(annotationId) {
			const annotation = annotations[annotationId];

			annotationToolTipPosition = rangeToToolTopPosition(iframeWindow.event, toRangeBody(annotation.coordinate));
			annotationToolTipContext = "edit";
			activeAnnotation = annotation;
		}

		// maybe we should allow click to see comments though
		// but that's story for later day
		if (!isPublic) {
			iframeWindow.onClickAnnotation = onClickAnnotation;
		}
	}

	onMount(function loadContent() {
		const iframe = document.getElementById("ubinote-iframe-content");
		iframe.onload = onIframeLoad;
	});

</script>

<iframe
	id="ubinote-iframe-content"
 title="Ubinote content"
 src={ isPublic ? `/api/public/page/${pageId}/content` : `/api/page/${pageId}/content`}
 style="width:100%; display:flex;"/>

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
	}
</style>
