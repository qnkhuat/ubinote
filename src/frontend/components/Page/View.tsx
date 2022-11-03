// @ts-nocheck
// we don't use iframe to show
// return the dom inside iframe
import { useRef, useState, useEffect } from "react";
import { api, getStaticPage, createAnnotation } from "api";
import { TPage, TAnnotation } from "api/types";

import { TooltipNew, ToolTipModify } from "components/Page/Tooltip";

import { highlightRange } from "lib/highlight/higlight-dom-range";
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

const addAnnotation = async (pageId: number, selection: window.Selection, color: string = "red") => {
  if (selection == null) {
    console.log("Attempted to add annotation when selection is null");
    return
  }

  console.log("ADDING annotation?");
  const range = selection.getRangeAt(0);
  // need to calculate textpos before highlight, otherwise the position will be messed up
  // when try to highlight on re-load
  const textPos = fromRange(document.body, range);
  const [highlightElements, removeHighlights] = highlightRange(range, 'span', {class: colorToCSS[color]});
  // save it
  const resp = await createAnnotation(
    {coordinate: textPos,
      page_id: pageId})
    .catch(err => console.error("Failed to add annotaiton: ", err));
  console.log(resp);
  return highlightElements;
}

const PageView = (props: Props) => {
  const { page } = props;
  const [ content, setContent ] = useState<string>("");
  const [ selection, setSelection ] = useState(null);
  const [ showToolTip, setShowToolTip ] = useState<boolean>(false);
  const [ position, setPosition ] = useState({x: 0, y: 0});
  const [ annotations, setAnnotations ] = useState({});
  const contentDiv = useRef(null);

  useEffect(() => {
    document.addEventListener("mouseup", () => {
      const selection = window.getSelection();
      if (!selection.isCollapse) {
        const boundingRect = selection.getRangeAt(0).getBoundingClientRect();
        setShowToolTip(true);
        setSelection(selection);
        setPosition({
          x: boundingRect.left + window.scrollX,
          y: boundingRect.top + window.scrollY
        });
      }
      else {
        setsShowToolTip(false);
      }
    });

    // download the html as raw string and render it
    api.get(getStaticPage(page.path))
      .then(resp => {
        setContent(resp.data);

        const temp_annotations = {};
        page.annotations.forEach((annotation: TAnnotation) => {
          const range = toRange(document.body, annotation.coordinate);
          const [highlightNodes, removeHighlights] = highlightRange(range, 'span', {class: colorToCSS[annotation.color]});
          temp_annotations[annotation.id] = [highlightNodes, removeHighlights];
        });

        setAnnotations(temp_annotations);
      })
      .catch(e => console.error("Failed to get static page: ", e));
  }, [])

  return (<div className="">
    <div id ="ubinote-header">
      <h3 className="text-red-400">sup sup sup sup? okay that's </h3>
    </div>
    <div id="ubinote-page-content"
    className="w-full relative" ref={contentDiv} dangerouslySetInnerHTML={{__html: content}}></div>
    {showToolTip ?
      <TooltipNew
        {...position}
        onAddAnnotation={(_e:any) => addAnnotation(page.id, selection)}
      /> : null}
  </div>)
}

export default PageView;
