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

### Solution

A level can have a `solution` field which is used to replay the puzzle's
solution. This field should contain the path to a valid recording file.

<details>
	<summary>Example</summary>
	
```
{
	"name": "Hello World!",
	"width": 800,
	"height": 500,
	"solution": "/solution.rec",
	...
}
```
</details>

### Rectangles

A level's rectangles are declared in the `movingRectangles`, `walls`, `areas`,
`goals`, and `hints` fields. Each of these fields corresponds to a list of
rectangle objects of its type.

Every rectangle must fill the `type`, `x`, `y`, `width`, and `height` fields
Additionally, it must contain data for any unique attributes listed below. This
data corresponds to the values taken by a rectangle's `@JsonCreator` constructor.

Areas are special rectangles that apply an effect to all Moving Rectangles that
are touching them.

<details>
	<summary>Example</summary>
	
```
{
	...
	"movingRectangles": [
		...
	],
	"walls": [
		...
	],
	"areas": [
		...
	],
	"goals": [
		...
	],
	"hints": [
		...
	]
}
```
</details>

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

#### movingRectangles

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

<details>
	<summary>Example</summary>
	
```
{
	"type": ".MovingRectangle",
	"controlledByPlayer": "true",
	"x": 250,
	"y": 470,
	"width": 20,
	"height": 20,
	"color": "PLAYER"
}
```
</details>

#### wallRectangles

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
		<td>One of STAY or PREVENT:
			<ul>
				<li>STAY: does not interact with window edges
				<li>PREVENT: stops window edge from passing through it
			</ul>
		</td>
	</tr>
</table>

<details>
	<summary>Example</summary>
	
```
{
	"type": ".WallRectangle",
	"x": 0,
	"y": 0,
	"width": 800,
	"height": 10,
	"resizeBehavior": "STAY"
}
```
</details>

#### areas

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
		<td>type</td>
		<td>.ForceArea</td>
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
<p align="center"><b>Grow Area</b></p>
<table>
	<tr>
		<td><i>Description</i></td>
		<td>Grow a smaller Moving Rectangle until it is as wide/tall as this
			</td>
	</tr>
	<tr>
		<td>type</td>
		<td>.GrowArea</td>
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
		<td>type</td>
		<td>.ShrinkArea</td>
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

<details>
	<summary>Example</summary>
	
```
{
	"type": ".GrowArea",
	"x": 0,
	"y": 430,
	"width": 20,
	"height": 50,
	"xGrowth": 0,
	"yGrowth": 1
}
```
</details>

#### goals

<p align="center"><b>Goal Area</b></p>
<table>
	<tr>
		<td><i>Description</i></td>
		<td>Load the next level after a controllable Moving Rectangle has been
			touching this for a long enough time</td>
	</tr>
	<tr>
		<td>type</td>
		<td>.GoalArea</td>
	</tr>
	<tr>
		<td>nextLevel</td>
		<td>Path to the next level's JSON</td>
	</tr>
</table>

<details>
	<summary>Example</summary>
	
```
{
	"type": ".GoalArea",
	"x": 280,
	"y": 630,
	"width": 50,
	"height": 50,
	"nextLevel": "/level_1.json"
}
```
</details>

#### hints

These rectangles become visible when the player requests a hint. They are
invisible otherwise and always intangible.

<p align="center"><b>Hint Rectangle</b></p>
<table>
	<tr>
		<td><i>Description</i></td>
		<td>Become visible to the player when they press the hint button</td>
	</tr>
	<tr>
		<td>type</td>
		<td>.HintRectangle</td>
	</tr>
	<tr>
		<td>color</td>
		<td>One of BLACK, BLUE, GREEN, GRAY, ORANGE, RED, or PLAYER (dark blue
			usually for the player)</td>
	</tr>
</table>

<details>
	<summary>Example</summary>
	
```
{
	"type": ".HintRectangle",
	"x": 440,
	"y": 460,
	"width": 30,
	"height": 30,
	"color": "GREEN"
}
```
</details>

### Attaching areas to rectangles

Rectangles can optionally have an `attachments` field that corresponds to areas
which should remain stuck to the rectangle as it moves. Each attachment object
requires an `area` field with an area object and an `options` field that
describes how the area is attached.

Attached areas do not need to include fields for their `x` and `y` position
because these will be inherited from the rectangle. If a width/height option is
used, the area similarly does not need to include a field for its `width` and/or
`height`.

The options should include exactly one directional option and any number of
width/height options.

#### Directional options

<table>
	<tr>
		<td>GLUED_NORTH<td>
		<td>Area remains stuck on top of the rectangle</td>
	</tr>
	<tr>
		<td>GLUED_SOUTH<td>
		<td>Area remains stuck on the bottom  of the rectangle</td>
	</tr>
	<tr>
		<td>GLUED_WEST<td>
		<td>Area remains stuck to the left of of the rectangle</td>
	</tr>
	<tr>
		<td>GLUED_EAST<td>
		<td>Area remains stuck to the right of the rectangle</td>
	</tr>
</table>

#### Width/height options

<table>
	<tr>
		<td>SAME_WIDTH</td>
		<td>Area will keep its width equal to the rectangle's width</td>
	</tr>
	<tr>
		<td>SAME_HEIGHT</td>
		<td>Area will keep its height equal to the rectangle's height</td>
	</tr>
</table>

<details>
	<summary>Example</summary>
	
```
{
	"type": ".MovingRectangle",
	...
	"attachments": [
		{
			"area": {
				"type": ".GrowArea",
				"width": 60,
				"xGrowth": 1,
				"yGrowth": 0
			},
			"options": ["GLUED_WEST", "SAME_HEIGHT"]
		},
		{
			"area": {
				"type": ".ShrinkArea",
				"height": 10,
				"xShrink": 1,
				"yShrink": 0
			},
			"options": ["GLUED_NORTH", "SAME_WIDTH"]
		}
	]
```
</details>
