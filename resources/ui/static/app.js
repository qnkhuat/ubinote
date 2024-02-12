function resizeIframe(obj) {
  // not sure why but if you set the height only once the height is a bit shorter than real height 🤷‍♂️
  obj.style.height = obj.contentWindow.document.documentElement.scrollHeight + 'px';
  obj.style.height = obj.contentWindow.document.documentElement.scrollHeight + 'px';
}

function onIframeLoad(iframe) {
  // step 0: update iframe Document state
  resizeIframe(iframe);
}

//htmx.logAll();
