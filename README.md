# Block Game

This is a puzzle game about moving blocks around. Solve 15 hand-crafted levels
by interacting with the environment in varied, creative ways.

## Playing the game

### System requirements

<table>
	<tr>
		<td>Java</td>
		<td>Download Java 8 or higher from
			<a href="https://www.java.com/en/download/">java.com</a>.</td>
	</tr>
	<tr>
		<td>Operating system</td>
		<td>Developed and tested on Windows 11. It probably works on other
			platforms supported by Java, but this is not guaranteed.</td>
	</tr>
	<tr>
		<td>Processor</td>
		<td>1 GHz</td>
	</tr>
	<tr>
		<td>Memory</td>
		<td>256 MB RAM</td>
	</tr>
	<tr>
		<td>Storage</td>
		<td>2.94 MB</td>
	</tr>
</table>

### Installation

Download the latest `jar` file from the
[releases tag](https://github.com/frankkormann/block-game/releases) and run it.

### Controls

<table>
	<tr>
		<td>Move</td>
		<td>A/D or arrow keys</td>
	</tr>
	<tr>
		<td>Jump</td>
		<td>W, space, or up arrow</td>
	</tr>
	<tr>
		<td>Pause</td>
		<td>K</td>
	</tr>
	<tr>
		<td>Restart level</td>
		<td>R</td>
	</tr>
</table>

**Hints**

<table>
	<tr>
		<td>Show hint</td>
		<td>H</td>
	</tr>
	<tr>
		<td>Show solution</td>
		<td>Control + Shift + H</td>
	</tr>
</table>


**Advanced**

<table>
	<tr>
		<td>Save replay of current level<t/d>
		<td>Control + Shift + S</td>
	</tr>
	<tr>
		<td>Playback replay</td>
		<td>Control + Shift + P</td>
	</tr>
	<tr>
		<td>Stop playback</td>
		<td>S</td>
	</tr>
	<tr>
		<td>Advance frame while paused</td>
		<td>L</td>
	</tr>
</table>

### Reporting a bug

If you encounter a bug, press `Control + Shift + S` to save a recording of the
current level. Then open an issue on this GitHub page and include:

- The level you encountered the bug
- The recording file
- A description of the bug
- Any other information you think is relevant

## Technical details

### Version information

The project is developed in Eclipse version 2025-03 (4.35.0) using:

- JDK 24, compiled for Java 8
- Maven 3.8.1
- [FlatLaf](https://github.com/JFormDesigner/FlatLaf) 3.5
- [Jackson Databind](https://github.com/FasterXML/jackson-databind/) 2.18.2

### Creating / editing levels

Each level is built from a `JSON` file. See [JSON_FORMAT.md](JSON_FORMAT.md) for
formatting requirements. See `level_demo.json` for an example level that creates
every type of rectangle.

The game will automatically load `level_1.json` as its first level. Subsequent
level filenames are read from the activated Goal Area.

## Contributing

Outside contributions are not accepted because this is primarily a personal
project. However, feedback in all forms is welcomed. You are also free to extend the
project on your own.

## License

MIT License

Copyright (c) 2025 Frank Kormann

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
