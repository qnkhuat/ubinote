// @ts-nocheck

// we don't use: iframe for this because mouseup event can't
// return the dom inside iframe
import { useRef, useState, useEffect } from "react";
import { TPage } from "api/types";
import { api, getStaticPage } from "api";
import Button from "@mui/material/Button";

import highlightRange from "./higlight-dom-range";

const PageView = (props) => {
  const { page } = props;
  const [ content, setContent ] = useState<string>("");
  const contentDiv = useRef(null);

  useEffect(() => {
    document.addEventListener("mouseup", () => {
      const selection = window.getSelection();
      console.log("User select:", selection);
      if (!selection.isCollapsed) {
        const range = selection.getRangeAt(0);
        console.log("Range: ", range);
        const removeHighlights = highlightRange(range, 'span', { class: 'text-red-400' });
        // Running removeHighlights() would remove the highlight again.
}
    });
    // download the html as raw string and render it
    api.get(getStaticPage(page.path))
      .then(resp => setContent(resp.data))
      .catch(e => console.error("Failed to get that; ',", e));
  }, [])

  return (<div className="">
    <div>
      <h3 className="text-red-400">sup sup sup</h3>
    </div>
    <div className="w-full relative" ref={contentDiv} dangerouslySetInnerHTML={{__html: content}}></div>
  </div>)
}

export default PageView;