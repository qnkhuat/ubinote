<script>
	import PaintBrushAlt from "carbon-icons-svelte/lib/PaintBrushAlt.svelte";
	import TrashCan from "carbon-icons-svelte/lib/TrashCan.svelte";
	import { Button } from "carbon-components-svelte";

	//------------------------ props  ------------------------//
	// position of the bottom left of user's selection
	export let x, y;
	export let context; // could be "new" or "edit"
	export let onClose, onAnnotate, onDelete;
	console.log("context", context, x, y);

	function handleClick(action) {
		switch (action) {
			case "annotate":
				onAnnotate("red");
				break;
			case "delete":
				onDelete();
				break;
		}
		onClose();
	}

</script>

<div id="annotation-tooltip"
	style="left: {x}px; top: {y}px;">
	<div class="content">
		{#if context === "new"}
			<Button on:click={() => handleClick("annotate")} size="small" icon={PaintBrushAlt} iconDescription="Highlight"></Button>
		{:else}
			<Button on:click={() => handleClick("delete")} size="small" icon={TrashCan} iconDescription="Delete"></Button>
		{/if}
	</div>
</div>

<style lang="scss">
	#annotation-tooltip {
		position: absolute;
		display: flex;
		background-color: red;
		z-index: 1000;
		transform: translate(-50%);
		margin-top: 10px;

		.content {
			position: relative;
		}
	}

</style>
