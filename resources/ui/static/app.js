// Credit to: https://github.com/Treora/dom-highlight-range
// Wrap each text node in a given DOM Range with a <mark> or other element.
// Breaks start and/or end node if needed.
// Returns a list of highlighted elements and a function that cleans up the created highlight (not a perfect undo: split text nodes are
// not merged again).
//
// Parameters:
// - range: a DOM Range object. Note that as highlighting modifies the DOM, the range may be
//   unusable afterwards
// - tagName: the element used to wrap text nodes. Defaults to 'mark'.
// - attributes: an Object defining any attributes to be set on the wrapper elements.
function highlightRange(range, tagName = 'mark', attributes = {}) {
  if (range.collapsed) return;

  // First put all nodes in an array (splits start and end nodes if needed)
  const nodes = textNodesInRange(range);

  // Highlight each node
  const highlightElements = [];
  for (const nodeIdx in nodes) {
    const highlightElement = wrapNodeInHighlight(nodes[nodeIdx], tagName, attributes);
    highlightElements.push(highlightElement);
  }

  // Return a function that cleans up the highlightElements.
  function removeHighlights() {
    // Remove each of the created highlightElements.
    for (const highlightIdx in highlightElements) {
      removeHighlight(highlightElements[highlightIdx]);
    }
  }
  return [highlightElements, removeHighlights];
}

// Return an array of the text nodes in the range. Split the start and end nodes if required.
function textNodesInRange(range) {
  // If the start or end node is a text node and only partly in the range, split it.
  if (range.startContainer.nodeType === Node.TEXT_NODE && range.startOffset > 0) {
    const endOffset = range.endOffset; // (this may get lost when the splitting the node)
    const createdNode = range.startContainer.splitText(range.startOffset);
    if (range.endContainer === range.startContainer) {
      // If the end was in the same container, it will now be in the newly created node.
      range.setEnd(createdNode, endOffset - range.startOffset);
    }
    range.setStart(createdNode, 0);
  }
  if (
    range.endContainer.nodeType === Node.TEXT_NODE
    && range.endOffset < range.endContainer.length
  ) {
    range.endContainer.splitText(range.endOffset);
  }

  // Collect the text nodes.
  const walker = range.startContainer.ownerDocument.createTreeWalker(
    range.commonAncestorContainer,
    NodeFilter.SHOW_TEXT,
    node => range.intersectsNode(node) ? NodeFilter.FILTER_ACCEPT : NodeFilter.FILTER_REJECT,
  );
  walker.currentNode = range.startContainer;

  // // Optimise by skipping nodes that are explicitly outside the range.
  // const NodeTypesWithCharacterOffset = [
  //  Node.TEXT_NODE,
  //  Node.PROCESSING_INSTRUCTION_NODE,
  //  Node.COMMENT_NODE,
  // ];
  // if (!NodeTypesWithCharacterOffset.includes(range.startContainer.nodeType)) {
  //   if (range.startOffset < range.startContainer.childNodes.length) {
  //     walker.currentNode = range.startContainer.childNodes[range.startOffset];
  //   } else {
  //     walker.nextSibling();
  //   }
  // }

  const nodes = [];
  if (walker.currentNode.nodeType === Node.TEXT_NODE)
    nodes.push(walker.currentNode);
  while (walker.nextNode() && range.comparePoint(walker.cukrentNode, 0) !== 1)
    nodes.push(walker.currentNode);
  return nodes;
}

// Replace [node] with <tagName ...attributes>[node]</tagName>
function wrapNodeInHighlight(node, tagName, attributes) {
  const highlightElement = node.ownerDocument.createElement(tagName);
  Object.keys(attributes).forEach(key => {
    highlightElement.setAttribute(key, attributes[key]);
  });
  const tempRange = node.ownerDocument.createRange();
  tempRange.selectNode(node);
  tempRange.surroundContents(highlightElement);
  return highlightElement;
}

// Remove a highlight element created with wrapNodeInHighlight.
function removeHighlight(highlightElement) {
  if (highlightElement.childNodes.length === 1) {
    highlightElement.parentNode.replaceChild(highlightElement.firstChild, highlightElement);
  } else {
    // If the highlight somehow contains multiple nodes now, move them all.
    while (highlightElement.firstChild) {
      highlightElement.parentNode.insertBefore(highlightElement.firstChild, highlightElement);
    }
    highlightElement.remove();
  }
}

function isSelecting(selection) {
  const boundingRect = selection.getRangeAt(0).getBoundingClientRect();
  return !selection.isCollapsed && boundingRect.width > 2;
}

function resizeIframe(obj) {
  // not sure why but if you set the height only once the height is a bit shorter than real height 🤷‍♂️
  obj.style.height = obj.contentWindow.document.documentElement.scrollHeight + 'px';
  obj.style.height = obj.contentWindow.document.documentElement.scrollHeight + 'px';
}

function rangeToToolTopPosition(event, range, iframe) {
  const boundingRect = range.getBoundingClientRect()
  return {
    x: Math.min(event.clientX, boundingRect.right),
    y: boundingRect.bottom + iframe.offsetTop,
  }
}

function onIframeLoad(iframe) {
  // step 0: update iframe Document state
  resizeIframe(iframe);

  // step 1 : inject css
  iframeWindow = iframe.contentWindow;
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

  // step 3: inject mouse up tracker
  iframeWindow.document.addEventListener("mouseup", (event) => {
    // if user is selecting, show tooltip
    const selection = iframeWindow?.getSelection();
    if (isSelecting(selection)) {
      console.log("MOUSE UP");
    }
  })

}

//htmx.logAll();
