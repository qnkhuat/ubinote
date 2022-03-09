import { TPage } from "api/types";

interface Props {
  pages: TPage[];
}

const PageList = (props: Props)=> {
  const { pages } = props;
  console.log("we have  a list of pages:", pages);

  // things to display as a table:
  // - Domain
  // - Title
  // - Paged date at
  return <div>
    {pages.map((page, i) =>
    <div key={i}>
    </div>
    )}
  </div>;
}

export default PageList;
