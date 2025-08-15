# Block Game

This is a puzzle game about moving blocks around. Solve 35 hand-crafted levels
by interacting with the environment in varied, creative ways.

### Features

- 35 fun levels<sup>1</sup>
- Interactive title screen
- Hint system
    - Ability to see the solution if you are really stuck
- Expansive options menu
    - Accessibility options for colorblindness, key remapping, increasing GUI
    size, and slowing the game speed
- Occasional cryptic messages printed to `stderr`

<sup>1. Joy is not guaranteed</sup>

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
		<td>3.01 MB</td>
	</tr>
</table>

### Installation

If you do not have Java, install Java 8 or higher from
[java.com](https://www.java.com/en/download/). Then, download the latest `jar`
file from the [releases tab](https://github.com/frankkormann/block-game/releases) 
and run it.

### Controls

You can change these in the Options menu.

<table>
	<tr>
		<td>Move</td>
		<td>A and D</td>
	</tr>
	<tr>
		<td>Jump</td>
		<td>W</td>
	</tr>
</table>

**Keyboard resizing**

First, select a direction to resize. Then adjust it.

<table>
	<tr>
		<td>Select a direction</td>
		<td>Shift + I, J, K, or L<br>I: Top
			<br>K: Bottom<br>J: Left<br>L: Right</td>
	</tr>
	<tr>
		<td>Move side up/down</td>
		<td>I and K</td>
	</tr>
	<tr>
		<td>Move side left/right</td>
		<td>J and L</td>
	</tr>
</table>

### Reporting a bug

If you encounter a bug, save a recording of the current level in
`Recordings > Save current`. Then open an issue on this GitHub page and include
as much as you can:

- The full copied text of any error message you received, if there was an error
  message. Make sure to click "Details" for the full text
- The recording file
- Your Options settings, particularly Game Scaling
- The level you encountered the bug
- A description of the bug
- Any other information you think is relevant

### Setting a custom save directory

By default, save data is put in the current user's `AppData` folder on Windows
and the user's home directory on other systems. You can set the 
`BLOCKGAME_DIRECTORY` environment variable to change this behavior. If it is
set, its value will be used as the directory to save data in.

## Technical details

### Version information

The project is developed in using:

- JDK 24, compiled for Java 8
- Maven 3.8.1
- [FlatLaf](https://github.com/JFormDesigner/FlatLaf) 3.5
- [Jackson Databind](https://github.com/FasterXML/jackson-databind/) 2.18.2

### Documentation

The most up-to-date documentation can be found through the wiki:
[https://github.com/frankkormann/block-game/wiki](https://github.com/frankkormann/block-game/wiki).

### Creating / editing levels

Each level is built from a `JSON` file. See
[the wiki](https://github.com/frankkormann/block-game/wiki) for formatting
requirements. See `level_demo.json` for an example level that creates every type
of rectangle.

The game will automatically load `level_1-1.json` as its first level. Subsequent
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
