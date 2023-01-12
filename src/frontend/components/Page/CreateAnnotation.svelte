<script>
	import PaintBrushAlt from "carbon-icons-svelte/lib/PaintBrushAlt.svelte";
	import { Button } from "carbon-components-svelte";

	//------------------------ props  ------------------------//
	// position of the bottom left of user's selection
	export let x, y;
	export let onClose, onAnnotate;

	function handleClick() {
		onAnnotate("red");
		onClose();
	}

</script>

<div id = "create-annotation-tooltip"
	style="left: {x}px; top: {y}px;">
	<div class="content">
		<Button on:click={handleClick} size="small" icon={PaintBrushAlt} iconDescription="Highlight"></Button>
	</div>
</div>

<style lang="scss">
	#create-annotation-tooltip {
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
