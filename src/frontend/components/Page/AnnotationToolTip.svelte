<script>
  import AddComment from "carbon-icons-svelte/lib/AddComment.svelte";
  import PaintBrushAlt from "carbon-icons-svelte/lib/PaintBrushAlt.svelte";
  import TrashCan from "carbon-icons-svelte/lib/TrashCan.svelte";
  import { Button, TextArea } from "carbon-components-svelte";

  import eventOutside from "frontend/lib/eventOutside.js";

  //------------------------ props  ------------------------//
  // position of the bottom left of user's selection
  export let x, y;
  export let context; // enum: new, edit
  export let comments = [];
  export let onClose, onAnnotate, onNewComment, onDelete;


  let editingComment = "";

  //------------------------ states  ------------------------//

  function handleClickOutside(_e) {
    onClose();
  }

  function handleClick(action) {
    switch (action) {
      case "new":
        onAnnotate("yellow");
        onClose();
        break;
      case "delete":
        onDelete();
        onClose();
        break;
      case "newComment":
        onNewComment(editingComment);
        editingComment = "";
        break;
    }
  }

</script>

<div id="ubinote-annotation-tooltip"
     style="left: {x}px; top: {y}px;"
     >
     <div class="content"
          use:eventOutside={"mousedown"}
          on:eventOutside={handleClickOutside}>
       {#if context === "new"}
         <Button on:click={() => handleClick("new")} size="small" icon={PaintBrushAlt} iconDescription="Highlight"></Button>
       {:else if context === "edit"}
         <Button on:click={() => handleClick("delete")} size="small" icon={TrashCan} iconDescription="Delete"></Button>
           {#each comments as comment}
             <div class="comment">
               <div class="comment-author">{comment.creator_email}</div>
               <div class="comment-content">{comment.content}</div>
             </div>
           {/each}
           <div id="ubinote-annotation-tooltip-comments">
             <TextArea placeholder="Here is something interesting..." bind:value={editingComment}/>
             <Button on:click={() => handleClick("newComment")} size="small" iconDescription="Add Comment">Submit</Button>
           </div>
       {/if}
     </div>
</div>

<style lang="scss">
  #ubinote-annotation-tooltip {
    position: absolute;
    display: flex;
    z-index: 1000;
    transform: translate(-50%);
    margin-top: 10px;

      .content {
        background-color: black;
        position: relative;
      }

      .comment {
        background-color: white;
        padding: 10px;
        border-bottom: 1px solid black;
      }
  }

</style>
