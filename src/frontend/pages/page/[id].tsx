import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { TPage } from "api/types";
import { PageView } from "components/Page";
import { getPage } from "api";

const PageID = () => {
  const { id } = useParams();
  const [page, setPage] = useState<TPage | null>(null);

  useEffect(() => {
    getPage(id!)
      .then((resp) => setPage(resp.data))
      .catch(e => console.error(`Failed to get Page with id ${id}: `, e))
  }, []);

  if (!page) {
    return <></>
  }
  return (<>
    <PageView
      page={page}/>
  </>)
}

export default PageID;
