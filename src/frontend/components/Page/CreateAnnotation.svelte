<script>
	import CloseFilled from "carbon-icons-svelte/lib/CloseFilled.svelte";

	//------------------------ props  ------------------------//
	// position of the bottom left of user's selection
	export let x, y;
	export let onClose;


</script>

<div id = "create-annotation-tooltip"
		 style="left: {x}px; top: {y}px;">
	<div class="content">
		<div class="close-button">
			<CloseFilled/>
		</div>
		something
	</div>
</div>

<style lang="scss">
	#create-annotation-tooltip {
		position: absolute;
		display: flex;
		width: 60px;
		height: 50px;
		background-color: red;
		z-index: 1000;
		transform: translate(-50%);
		margin-top: 10px;

			.content {
				position: relative;

					.close-button {
						position: absolute;
						top: 0;
						right: 0;
					}

			}
	}

</style>
