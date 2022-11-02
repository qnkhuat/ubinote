interface Props {
  x: number;
  y: number;
}
const Tooltip = (
  props: Props
) => {
  const { x, y } = props;
  return <div id="ubinote-tooltip"
    className="absolute flex w-40 h-40 bg-black z-[100000]
    translate-x-1/2 -translate-y-full mb-6"
    style={{left : x, top: y}}>
    This is just a tool tip that show
    </div>
}

export default Tooltip;
