// @ts-nocheck
// we don't use iframe to show
// return the dom inside iframe
import { useRef, useState, useEffect } from "react";
import { TPage, TAnnotation } from "api/types";
import { api, getStaticPage, createAnnotation } from "api";

import highlightRange from "lib/highlight/higlight-dom-range";
import { fromRange, toRange } from "dom-anchor-text-position";

interface Props {
  page: TPage;
}

const colorToCSS = {
  "red": "text-red-400",
  "green": "text-green-400",
  "blue": "text-blue-400",
  "yellow": "text-yellow-400"
}

const addHighlight = (pageId: number, selection: window.Selection, color = "red") => {
  const range = selection.getRangeAt(0);
  // need to calculate textpos before highlight, otherwise the position will be messed up
  // when try to highlight on re-load
  const textPos = fromRange(document.body, range);
  const highlightElements = highlightRange(range, 'span', {class: colorToCSS[color]});
  console.log("Elements: ", highlightElements);
  // save it
  createAnnotation(
    {coordinate: textPos,
      page_id: pageId})
    .catch(err => console.error("Failed to add annotaiton: ", err));
  return highlightElements;
}

const PageView = (props: Props) => {
  const { page } = props;
  const [ content, setContent ] = useState<string>("");
  const contentDiv = useRef(null);

  useEffect(() => {
    document.addEventListener("mouseup", () => {
      const selection = window.getSelection();
      if (!selection.isCollapsed) {
        addHighlight(page.id, selection);
      }
    });
    // download the html as raw string and render it
    api.get(getStaticPage(page.path))
      .then(resp => {
        setContent(resp.data);
        page.annotations.forEach((annotation: TAnnotation) => {
          const range = toRange(document.body, annotation.coordinate);
          highlightRange(range, 'span', {class: colorToCSS[annotation.color]});
        })
      })
      .catch(e => console.error("Failed to get static page: ", e));
  }, [])

  return (<div className="">
    <div>
      <h3 className="text-red-400">sup sup sup</h3>
    </div>
    <div className="w-full relative" ref={contentDiv} dangerouslySetInnerHTML={{__html: content}}></div>
  </div>)
}

export default PageView;
