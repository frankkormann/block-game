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
		<td>2.92 MB</td>
	</tr>
</table>

### Installation

Download the latest `jar` file from the releases tab and run it.

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

**Advanced**

<table>
	<tr>
		<td>Save replay of current level<t/d>
		<td>Control + shift + S</td>
	</tr>
	<tr>
		<td>Playback replay</td>
		<td>Control + shift + P</td>
	</tr>
	<tr>
		<td>Stop playback</td>
		<td>E</td>
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

### Goal

To create a 2D puzzle-platformer game engine with easy level creation.

### Creating / editing levels

Each level is built from a `JSON` file. See [JSON_FORMAT.md](JSON_FORMAT.md) for
formatting requirements. See `level_demo.json` for an example level that creates
every type of rectangle.

The game will automatically load `level_1.json` as its first level. Subsequent
level filenames are read from the activated Goal Area.

## Contributing

Outside contributions are not accepted because this is primarily a personal
project; see [Project Purpose](#project-purpose) below for more information.
However, feedback in all forms is welcomed. You are also free to extend the
project on your own.

## Author

The project is solely developed by Frank Kormann. While I have asked others to
help playtest and report bugs, all code (except where otherwise noted) is my own.

### Project purpose

I started, and continue to work on, this project to practice my software design,
development, and documentation skills. While I would love to create a fun game,
the main purpose is to gain experience working with a complex project<sup>1</sup>
To that end, I decided to, as much as possible, write the code and make the
design decisions myself without referring to outside help. This requires me to
think critically about each decision instead of simply following a tutorial.
Unfortunately, this means I cannot accept contributions from others.

<sup>1. Relatively complex; keep in mind the simplicity of "projects" you tend
to create in your first round of programming courses.</sup>

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
