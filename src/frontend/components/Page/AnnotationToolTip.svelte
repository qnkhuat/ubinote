<script>
  import AddComment from "carbon-icons-svelte/lib/AddComment.svelte";
  import PaintBrushAlt from "carbon-icons-svelte/lib/PaintBrushAlt.svelte";
  import TrashCan from "carbon-icons-svelte/lib/TrashCan.svelte";
  import Send from "carbon-icons-svelte/lib/Send.svelte";
  import { Button, TextArea } from "carbon-components-svelte";

  import { formatTime } from "frontend/lib/utils.js";
  import eventOutside from "frontend/lib/eventOutside.js";

  //------------------------ props  ------------------------//
  // position of the bottom left of user's selection
  export let x, y;
  export let context; // enum: new, edit
  export let comments = [];
  export let onClose, onAnnotate, onNewComment, onDelete;

  let editingComment = "";

  const rtf1 = new Intl.RelativeTimeFormat('en', { style: 'short' });

  //------------------------ states  ------------------------//
  // BUG: there is a bug if you select text on an annotated range, you'll see the edit tool tip
  // but what you should see is a new button

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

  const editingModalWidth = 400; // in pixel
  function calcXPosition(xFromProp) {
    if (context == "edit") {
      const haflModalWidth =  editingModalWidth * 0.5;
      // ensure the modal doesn't go overlfow on the left
      if ((xFromProp - haflModalWidth) < 0) {
        return haflModalWidth;
      // ensure the modal doesn't go overlfow on the right
      } else if ((xFromProp + haflModalWidth + 15) > window.innerWidth){
        return window.innerWidth - haflModalWidth - 15; // 15px is the size of the scroll bar if any
      } else {
        return xFromProp;
      }
    } else {
      return xFromProp;
    }
  }

</script>

<div id="ubinote-annotation-tooltip"
     style="left: {calcXPosition(x)}px; top: {y}px;"
     >
     <div class="content"
          use:eventOutside={"mousedown"}
          on:eventOutside={handleClickOutside}>
       {#if context === "new"}
         <Button on:click={() => handleClick("new")} size="small" icon={PaintBrushAlt} iconDescription="Highlight"></Button>
       {:else if context === "edit"}
         <div class="editing" style={`width: ${editingModalWidth}px`}>
           <div class="btn-delete">
             <Button on:click={() => handleClick("delete")} size="small" icon={TrashCan} iconDescription="Delete"></Button>
           </div>

             <div class="comments">
               {#each comments as comment}
                 <div class="comment">
                   <div class="comment-header">
                      <p class="comment-author">{comment.creator_email}</p>
                      <p class="comment-updated-at">{formatTime(Date.now(), Date.parse(comment.updated_at))}</p>
                   </div>
                   <div class="comment-content">{comment.content}</div>
                 </div>
               {/each}
               <TextArea placeholder="Here is something interesting..." bind:value={editingComment} style="resize:none;"/>
               <div class="btn-new-comment">
                <Button on:click={() => handleClick("newComment")} size="small" icon={Send} iconDescription="Add Comment"></Button>
               </div>
             </div>
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
        position: relative;
      }
      .editing {
        display: flex;
        flex-direction: column;
        padding: 10px;
        background-color: white;
        border-radius: 10px;
        border: 1px solid black;
        width: 400px;
          .btn-delete {
            display: flex;
            justify-content: flex-end;
            padding-bottom: 10px;
            border-bottom: 1px solid black;
          }

          .comments {
            position: relative;
            .comment {
              background-color: white;
              padding: 10px;
              border-bottom: 1px solid black;
                .comment-header {
                  display: flex;
                  justify-content: space-between;
                    p {
                      font-size: 0.8rem;
                      font-weight: bold;
                      padding-bottom: 5px;
                    }
                }
            }
            .btn-new-comment {
              position: absolute;
              right: 5px;
              bottom: 5px;

            }
          }
      }
  }

</style>
