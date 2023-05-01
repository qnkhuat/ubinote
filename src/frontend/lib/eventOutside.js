/** Dispatch an event when `eventType` outisde of a node */
export default function eventOutside(node, eventType="click") {

  const handleClick = event => {
    if (node && !node.contains(event.target) && !event.defaultPrevented) {
      node.dispatchEvent(
        new CustomEvent('eventOutside', node)
      )
    }
  }

  document.getElementById("ubinote-iframe-content").contentWindow.document.addEventListener(eventType, handleClick, true);

  return {
    destroy() {
      document.getElementById("ubinote-iframe-content").contentWindow.document.removeEventListener(eventType, handleClick, true);
    }
  }
}
