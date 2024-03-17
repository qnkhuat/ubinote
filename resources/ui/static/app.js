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
  while (walker.nextNode() && range.comparePoint(walker.currentNode, 0) !== 1)
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
// ---------------------- START EXTERNAL LIB: dom-seek ----------------------
const E_END = 'Iterator exhausted before seek ended.'
const E_SHOW = 'Argument 1 of seek must use filter NodeFilter.SHOW_TEXT.'
const E_WHERE = 'Argument 2 of seek must be an integer or a Text Node.'

const DOCUMENT_POSITION_PRECEDING = 2
const SHOW_TEXT = 4
const TEXT_NODE = 3

function seek(iter, where) {
  if (iter.whatToShow !== SHOW_TEXT) {
    let error

    // istanbul ignore next
    try {
      error = new DOMException(E_SHOW, 'InvalidStateError')
    } catch {
      error = new Error(E_SHOW);
      error.code = 11
      error.name = 'InvalidStateError'
      error.toString = () => `InvalidStateError: ${E_SHOW}`
    }

    throw error
  }

  let count = 0
  let node = iter.referenceNode
  let predicates = null

  if (isInteger(where)) {
    predicates = {
      forward: () => count < where,
      backward: () => count > where || !iter.pointerBeforeReferenceNode,
    }
  } else if (isText(where)) {
    let forward = before(node, where) ? () => false : () => node !== where
    let backward = () => node !== where || !iter.pointerBeforeReferenceNode
    predicates = {forward, backward}
  } else {
    throw new TypeError(E_WHERE)
  }

  while (predicates.forward()) {
    node = iter.nextNode()

    if (node === null) {
      throw new RangeError(E_END)
    }

    count += node.nodeValue.length
  }

  if (iter.nextNode()) {
    node = iter.previousNode()
  }

  while (predicates.backward()) {
    node = iter.previousNode()

    if (node === null) {
      throw new RangeError(E_END)
    }

    count -= node.nodeValue.length
  }

  if (!isText(iter.referenceNode)) {
    throw new RangeError(E_END);
  }

  return count
}


function isInteger(n) {
  if (typeof n !== 'number') return false;
  return isFinite(n) && Math.floor(n) === n;
}


function isText(node) {
  return node.nodeType === TEXT_NODE
}


function before(ref, node) {
  return ref.compareDocumentPosition(node) & DOCUMENT_POSITION_PRECEDING
}
// ---------------------- END EXTERNAL LIB: dom-seek ----------------------
// ---------------------- START EXTERNAL LIB: dom-anchor-text-position ----------------------

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

// ---------------------- END EXTERNAL LIB: dom-anchor-text-position ----------------------
// handle clickoutside a node, call the callback when it's clicked outside the node
// by default remove the listener after the first click outside
function withClickOutside(targetElement, callback, theDocument = document, once=true) {
  function handleClick(event) {
    let clickedEl = event.target;
    do {
      if (clickedEl === targetElement) {
        // This is a click inside, does nothing, just return.
        return;
      }
      // Go up the DOM
      clickedEl = clickedEl.parentNode;
    } while (clickedEl);
    // This is a click outside.
    callback();
    // remove event listener if only trigger once
    if (once) theDocument.removeEventListener("click", handleClick);
  }
  theDocument.addEventListener("click", handleClick);
}

function isSelecting(selection) {
  const boundingRect = selection.getRangeAt(0).getBoundingClientRect();
  return !selection.isCollapsed && boundingRect.width > 1;
}

function resizeIframe(obj) {
  // not sure why but if you set the height only once the height is a bit shorter than real height 🤷‍♂️
  obj.style.height = obj.contentWindow.document.documentElement.scrollHeight + 'px';
  obj.style.height = obj.contentWindow.document.documentElement.scrollHeight + 'px';
}

function rangeToToolTopPosition(event, range, tooltipWidth) {
  const iframeWidth = document.getElementById(PAGE_IFRAME_ID).contentWindow.innerWidth;
  const boundingRect = range.getBoundingClientRect()
  // - 18 to shift the tooltip so it aligns with the cursor
  let x = Math.min(event.clientX, boundingRect.right) - 18
  // ensure the tooltip will not be overflow
  if (x < 0) {
    x = 1
  } else if (x + tooltipWidth > iframeWidth){
    x = iframeWidth - tooltipWidth - 1;
  }
  return {
    x: x,
    // + 10 for some padding between selection and tooltip
    y: boundingRect.bottom + 10,
  }
}

function onIframeLoad(iframe, newAnnotationBtnId, annotationTriggerId) {
  // step 0: update iframe Document state
  resizeIframe(iframe);

  // step 1 : inject css
  iframeWindow = iframe.contentWindow;
  const style = iframeWindow.document.createElement("style");
  style.textContent = `
.ubinote-highlight-yellow {
  background-color: #FACD5AA6;
  cursor:pointer;
}

.ubinote-highlight-green {
  background-color: #7CC868A6;
  cursor:pointer;
}

.ubinote-highlight-pink {
  background-color: #FB5C89A6;
  cursor:pointer;
}

.ubinote-highlight-blue {
  background-color: #69AFF0A6;
  cursor:pointer;
}
      `
  iframeWindow.document.head.appendChild(style);

  // step 3: inject mouse up tracker
  iframeWindow.document.addEventListener("mouseup", (event) => {
    // if user is selecting, show tooltip
    const selection = iframeWindow.getSelection();
    const tooltip = document.getElementById(newAnnotationBtnId);
    const tooltipWidth = tooltip.getBoundingClientRect().width;
    if (isSelecting(selection) ) {
      const {x, y} = rangeToToolTopPosition(event, selection.getRangeAt(0), tooltipWidth);
      tooltip.style.visibility = "visible";
      tooltip.style.top = `${y}px`;
      tooltip.style.left = `${x}px`;
    } else {
      tooltip.style.visibility = "hidden";
    }
  })

  // step 4: inject click on annotation tracker
  function onClickAnnotation(popoverId, start, end) {
    const event = iframeWindow.event;
    event.stopPropagation();
    const range = toRange(iframeWindow.document.body, {start, end});
    const targetElement = document.getElementById(popoverId);
    const position = rangeToToolTopPosition(event, range, targetElement.getBoundingClientRect().width);
    const iframeDistantToTop = window.pageYOffset + iframe.getBoundingClientRect().top;
    targetElement.style.top = iframeDistantToTop + position.y + "px";
    targetElement.style.left = position.x + "px";
    targetElement.style.visibility = "visible";
    withClickOutside(targetElement, () => {targetElement.style.visibility = "hidden";}, iframeWindow.document);
  }
  iframeWindow.onClickAnnotation = onClickAnnotation;

  // make sure htmx processed this DOM before triggering it
  // since it's possible for the iframe to load all contents before htmx proessed all the DOM
  htmx.process(document.getElementById(annotationTriggerId));
  htmx.trigger(`#${annotationTriggerId}`, "trigger-load-annotation", {})
}

// the id for iframe that contains the page view
const PAGE_IFRAME_ID = "ubinote-page-content";

// the dom id that wraps text for highlight
// it'll be created on the page iframe
function getAnnotationOnIframeId(annotationId) {
  return "ubinote-annotation-" + annotationId;
}

// the dom id that is used for popover of an annotation
// it'll be created on the main document
function getPopoverId(annotationId) {
  return "ubinote-popover-" + annotationId;
}

function deleteAnnotation(id) {
  // an annotation can be spanned as multiple divs
  document.getElementById(PAGE_IFRAME_ID).contentDocument.querySelectorAll("#" + getAnnotationOnIframeId(id)).forEach((node) => {
    removeHighlight(node)
  });
  document.getElementById(getPopoverId(id)).remove();
}

const IS_DEV = window.location.hostname == "localhost";
if (IS_DEV) htmx.logAll();

htmx.defineExtension("ubinote-swap-response", {
  //onEvent: function(name, evt) {
  //  return true;
  //},
  //transformResponse: function(text, xhr, elt) {
  //  return text;
  //},
  //isInlineSwap: function(swapStyle) {return false;},
  handleSwap: function(swapStyle, target, fragment, settleInfo) {
    const iframeBody = document.getElementById(PAGE_IFRAME_ID).contentWindow.document.body;
    const newNodes = []
    fragment.childNodes.forEach(function(node) {
      const attrs = node.getAttributeNames().reduce((acc, name) => {
        return {...acc, [name]: node.getAttribute(name)};
      }, {});
      const coordinate = JSON.parse(attrs["ubinote-annotation-coordinate"]);
      const annotationId = attrs["ubinote-annotation-id"];
      const range = toRange(iframeBody, coordinate);
      const popoverId = getPopoverId(annotationId);
      highlightRange(range, node.nodeName,
        {...attrs,
          onclick: `onClickAnnotation("${popoverId}", ${coordinate.start}, ${coordinate.end})`,
          id: getAnnotationOnIframeId(annotationId)});
      // these divs are apppend to the document, not iframedocument because it's easier to manage
      // bootstrap, htmx will works on it
      const popoverNode = document.createElement("div");
      //htmx.swap()
      node.childNodes.forEach((child) => {
        popoverNode.appendChild(child);
      });
      popoverNode.id = popoverId;
      popoverNode.style.position = "absolute";
      document.body.appendChild(popoverNode);
      newNodes.push(popoverNode);
    })
    // return the new nodes so htmx can manage them
    return newNodes;
  },
  //encodeParameters: function(xhr, parameters, elt) {
  //  return null;
  //}
})
