import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { TArchive } from "api/types";
import { ArchiveView } from "components/Archive";
import { getArchive } from "api";

const ArchiveID = () => {
  const { id } = useParams();
  const [archive, setArchive] = useState<TArchive | null>(null);

  useEffect(() => {
    getArchive(id!)
      .then((resp) => setArchive(resp.data))
      .catch(e => console.error(`Failed to get Archive with id ${id}: `, e))
  }, []);

  if (!archive) {
    return <></>
  }
  return (<>
    <ArchiveView
      archive={archive}/>
  </>)
}

export default ArchiveID;
