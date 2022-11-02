import  React  from "react";
import IconButton from '@mui/material/IconButton';
import CreateIcon from '@mui/icons-material/Create';

interface PropsWrapper {
  x: number;
  y: number;
  children: React.ReactNode;
}

const ToolTipWrapper = (props: PropsWrapper) => {
  const {x, y, children} = props;
  return <div id="ubinote-tooltip"
    className="absolute flex bg-white z-[100000]
    shadow-lg
    translate-x-1/2 -translate-y-full mb-6"
    style={{left : x, top: y}}>
    {children}
  </div>
}

interface PropsNew {
  x: number;
  y: number;
}

const TooltipNew: React.FC<PropsNew>= (props) => {
  return <ToolTipWrapper
    {...props}>
    <IconButton aria-label="delete">
      <CreateIcon />
    </IconButton>
  </ToolTipWrapper>
}

interface PropsModify {
  x: number;
  y: number;
}

const ToolTipModify = (props: PropsModify) => {
  return <ToolTipWrapper
    {...props}>
    <IconButton aria-label="delete">
      <CreateIcon />
    </IconButton>
  </ToolTipWrapper>
}
export { TooltipNew, ToolTipModify };
