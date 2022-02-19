import {TArchive} from "api/types";
const ArchiveItem = (archive: TArchive) => {
  return <h3>{JSON.stringify(archive)}</h3>
}

export default ArchiveItem;
