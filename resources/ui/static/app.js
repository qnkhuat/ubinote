// ---------------------- START EXTERNAL LIB: dom-highlight-range ----------------------
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

// ---------------------- END EXTERNAL LIB: dom-highlight-range ----------------------
// ---------------------- START EXTERNAL LIB: dom-anchor-text-position  ----------------------
/**
 * Return the next node after `node` in a tree order traversal of the document.
 */
function nextNode(node, skipChildren) {
  if (!skipChildren && node.firstChild) {
    return node.firstChild
  }

  do {
    if (node.nextSibling) {
      return node.nextSibling
    }
    node = node.parentNode
  } while (node)

  /* istanbul ignore next */
  return node
}

function firstNode(range) {
  if (range.startContainer.nodeType === Node.ELEMENT_NODE) {
    const node = range.startContainer.childNodes[range.startOffset]
    return node || nextNode(range.startContainer, true /* skip children */)
  }
  return range.startContainer
}

function firstNodeAfter(range) {
  if (range.endContainer.nodeType === Node.ELEMENT_NODE) {
    const node = range.endContainer.childNodes[range.endOffset]
    return node || nextNode(range.endContainer, true /* skip children */)
  }
  return nextNode(range.endContainer)
}

function forEachNodeInRange(range, cb) {
  let node = firstNode(range)
  const pastEnd = firstNodeAfter(range)
  while (node !== pastEnd) {
    cb(node)
    node = nextNode(node)
  }
}

/**
 * A ponyfill for Range.toString().
 * Spec: https://dom.spec.whatwg.org/#dom-range-stringifier
 *
 * Works around the buggy Range.toString() implementation in IE and Edge.
 * See https://github.com/tilgovi/dom-anchor-text-position/issues/4
 */
function rangeToString(range) {
  // This is a fairly direct translation of the Range.toString() implementation
  // in Blink.
  let text = ''
  forEachNodeInRange(range, (node) => {
    if (node.nodeType !== Node.TEXT_NODE) {
      return
    }
    const start = node === range.startContainer ? range.startOffset : 0
    const end = node === range.endContainer ? range.endOffset : node.textContent.length
    text += node.textContent.slice(start, end)
  })
  return text
}

const SHOW_TEXT = 4

function fromRange(root, range) {
  if (root === undefined) {
    throw new Error('missing required parameter "root"')
  }
  if (range === undefined) {
    throw new Error('missing required parameter "range"')
  }

  let document = root.ownerDocument
  let prefix = document.createRange()

  let startNode = range.startContainer
  let startOffset = range.startOffset

  prefix.setStart(root, 0)
  prefix.setEnd(startNode, startOffset)

  let start = rangeToString(prefix).length
  let end = start + rangeToString(range).length

  return {
    start: start,
    end: end,
  }
}


function toRange(root, selector = {}) {
  if (root === undefined) {
    throw new Error('missing required parameter "root"')
  }

  const document = root.ownerDocument
  const range = document.createRange()
  const iter = document.createNodeIterator(root, SHOW_TEXT)

  const start = selector.start || 0
  const end = selector.end || start

  const startOffset = start - seek(iter, start);
  const startNode = iter.referenceNode;

  const remainder = end - start + startOffset;

  const endOffset = remainder - seek(iter, remainder);
  const endNode = iter.referenceNode;

  range.setStart(startNode, startOffset)
  range.setEnd(endNode, endOffset)

  return range
}

// ---------------------- END EXTERNAL LIB: dom-anchor-text-position  ----------------------

function isSelecting(selection) {
  const boundingRect = selection.getRangeAt(0).getBoundingClientRect();
  return !selection.isCollapsed && boundingRect.width > 1;
}

function resizeIframe(obj) {
  // not sure why but if you set the height only once the height is a bit shorter than real height 🤷‍♂️
  obj.style.height = obj.contentWindow.document.documentElement.scrollHeight + 'px';
  obj.style.height = obj.contentWindow.document.documentElement.scrollHeight + 'px';
}

function rangeToToolTopPosition(event, range) {
  const boundingRect = range.getBoundingClientRect()
  return {
    // - 18 to make the button left-aligned with the cursor
    x: Math.min(event.clientX, boundingRect.right) - 18,
    // + 10 to shift the tooltop down a bit
    y: boundingRect.bottom + 10,
  }
}

function onIframeLoad(iframe, tooltipId) {
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
    const selection = iframeWindow.getSelection();
    const tooltip = document.getElementById(tooltipId);
    if (isSelecting(selection) ) {
      const {x, y} = rangeToToolTopPosition(event, selection.getRangeAt(0));
      console.log("GOT", x, y);
      tooltip.style.display = "flex";
      tooltip.style.top = `${y}px`;
      tooltip.style.left = `${x}px`;
      console.log("UP");
    } else {
      tooltip.style.display = "none";
      console.log("DOWN");
    }
  })
}

const IS_DEV = window.location.hostname == "localhost";
if (IS_DEV) htmx.logAll();
