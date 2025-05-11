**Spoiler warning:** This file documents every object in the game. If you want
to experience the puzzles as they were intended, play the game before reading.

___

See `level_demo.json` for an example level that creates every type of rectangle.

### Name, width, height

Every level must have a `name`, `width`, and `height` field. The width/height
are actually the size of the play area, not the window; the window is a little
bigger to accommodate its title bar and other decorations.

<table>
	<tr>
		<td>name</td>
		<td>Level title</td>
	</tr>
	<tr>
		<td>width</td>
		<td>Initial level width</td>
	</tr>
	<tr>
		<td>height</td>
		<td>Initial level height</td>
	</tr>
</table>

### Rectangles

Every level must have a `rectangles` field corresponding to a list of rectangle
objects. Every rectangle must fill the `type`, `x`, `y`, `width`, and `height`
fields. Additionally, it must contain data for any unique attributes listed
below. This data corresponds to the values taken by a rectangle's `@JsonCreator`
constructor.

Areas are special rectangles that apply an effect to all Moving Rectangles that
are touching them.

<p align="center"><b>All rectangles</b></p>
<table>
	<tr>
		<td>type</td>
		<td>Identifier for type of rectangle</td>
	</tr>
	<tr>
		<td>x</td>
		<td>X position</td>
	</tr>
	<tr>
		<td>y</td>
		<td>Y position</td>
	</tr>
	<tr>
		<td>width</td>
		<td>Width in the X dimension</td>
	</tr>
	<tr>
		<td>height</td>
		<td>Height in the Y dimension</td>
	</tr>
</table>
<p align="center"><b>Moving Rectangle</b></p>
<table>
	<tr>
		<td><i>Description</i></td>
		<td>Can be pushed around or controlled by the player</td>
	</tr>
	<tr>
		<td>type</td>
		<td>.MovingRectangle</td>
	</tr>
	<tr>
		<td>color</td>
		<td>One of BLACK, BLUE, GREEN, GRAY, ORANGE, RED, or PLAYER (dark blue
			usually used for a controllable rectangle)</td>
	</tr>
	<tr>
		<td>controlledByPlayer</td>
		<td>(Optional) Set to "true" to enable player control</td>
	</tr>
</table>
<p align="center"><b>Wall Rectangle</b></p>
<table>
	<tr>
		<td><i>Description</i>
		<td>Immovable wall</td>
	</tr>
	<tr>
		<td>type</td>
		<td>.WallRectangle</td>
	</tr>
	<tr>
		<td>resizeBehavior</td>
		<td>One of STAY, PREVENT, or MOVE
			<ul>
				<li>STAY: does not interact with window edges
				<li>PREVENT: stops window edge from passing through it
				<li>MOVE: is pushed by window edge
			</ul>
		</td>
	</tr>
</table>
<p align="center"><b>Antigravity Area</b></p>
<table>
	<tr>
		<td><i>Description</i></td>
		<td>Disables Moving Rectangle's gravity while touching</td>
	</tr>
	<tr>
		<td>type</td>
		<td>.AntigravityArea</td>
	</tr>
</table>
<p align="center"><b>Force Area</b></p>
<table>
	<tr>
		<td><i>Description</i></td>
		<td>Apply a constant force to Moving Rectangles</td>
	</tr>
	<tr>
		<td>xForce</td>
		<td>Value of force to apply in the X direction</td>
	</tr>
	<tr>
		<td>yForce</td>
		<td>Value of force to apply in the Y direction</td>
	</tr>
</table>
<p align="center"><b>Goal Area</b></p>
<table>
	<tr>
		<td><i>Description</i></td>
		<td>Load the next level after a controllable Moving Rectangle has been
			touching this for a long enough time</td>
	</tr>
	<tr>
		<td>nextLevel</td>
		<td>Path to the next level's JSON</td>
	</tr>
</table>
<p align="center"><b>Grow Area</b></p>
<table>
	<tr>
		<td><i>Description</i></td>
		<td>Grow a smaller Moving Rectangle until it is as wide/tall as this
			</td>
	</tr>
	<tr>
		<td>xGrowth</td>
		<td>Rate to increase width in pixels/frame</td>
	</tr>
	<tr>
		<td>yGrowth</td>
		<td>Rate to increase height in pixels/frame</td>
	</tr>
</table>
<p align="center"><b>Shrink Area</b></p>
<table>
	<tr>
		<td><i>Description</i></td>
		<td>Shrink a larger Moving Rectangle until it is as wide/tall as this
			</td>
	</tr>
	<tr>
		<td>xShrink</td>
		<td>Rate to decrease width in pixels/frame</td>
	</tr>
	<tr>
		<td>yShrink</td>
		<td>Rate to decrease height in pixels/frame</td>
	</tr>
</table>

### Attaching areas to rectangles

Rectangles can optionally have an `attachments` field that corresponds to an
array of areas, similar to the level's `rectangles` field. These attached areas
will have their position updated in sync with the rectangle they are attached
to. Their `x` and `y` values should still be relative to the level, not to the
rectangle.
