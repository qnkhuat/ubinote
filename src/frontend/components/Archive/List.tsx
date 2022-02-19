import { TArchive } from "api/types";

interface Props {
  archives: TArchive[];
}

const ArchiveList = (props: Props)=> {
  const { archives } = props;
  console.log("we have  a list of archives:", archives);

  // things to display as a table:
  // - Domain
  // - Title
  // - Archived date at
  return <div>
    {archives.map((archive, i) =>
    <div key={i}>
    </div>
    )}
  </div>;
}

export default ArchiveList;
