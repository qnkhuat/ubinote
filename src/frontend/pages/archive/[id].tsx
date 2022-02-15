import axios from "axios";
import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { Archive } from "../../api/types";

const ArchivePage = () => {
  const { id } = useParams();
  const [archive, setArchive] = useState<Archive | null>(null);

  useEffect(() => {
    axios.get(`http://localhost:8000/api/archive/${id}`).then((resp) => setArchive(resp.data)).catch(e => console.log(e))
  }, []);

  if (!archive) {
    return <h1>Loading</h1>
  }
  return <div><iframe className="w-screen h-screen" src={`http://localhost:8000/static/${archive.path}`} title="description"></iframe></div>;
}

export default ArchivePage;
