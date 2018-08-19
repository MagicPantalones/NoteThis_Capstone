## Markdown Cheatsheet

Based on Markwon library's [Cheatsheet](https://github.com/noties/Markwon/blob/master/README.md)

---
```no-highlight
# Header 1
## Header 2
### Header 3
#### Header 4
##### Header 5
###### Header 6
```
## Headers

# Header 1
## Header 2
### Header 3
#### Header 4
##### Header 5
###### Header 6

<sup></sup>
## Emphasis

```no-highlight
Emphasis, aka italics, with *asterisks* or _underscores_.

Strong emphasis, aka bold, with **asterisks** or __underscores__.

Combined emphasis with **asterisks and _underscores_**.

Strikethrough uses two tildes. ~~Scratch this.~~
```

Emphasis, aka italics, with *asterisks* or _underscores_.

Strong emphasis, aka bold, with **asterisks** or __underscores__.

Combined emphasis with **asterisks and _underscores_**.

Strikethrough uses two tildes. ~~Scratch this.~~

---
<sup></sup>
## Lists
```no-highlight
1. First ordered list item
2. Another item
  * Unordered sub-list.
1. Actual numbers don't matter, just that it's a number
   1. Ordered sub-list
4. And another item.

   You can have properly indented paragraphs within list items. Notice the blank line above, and the leading spaces (at least one, but we'll use three here to also align the raw Markdown).

   To have a line break without a paragraph, you will need to use two trailing spaces.
   Note that this line is separate, but within the same paragraph.
   (This is contrary to the typical GFM line break behaviour, where trailing spaces are not required.)

* Unordered list can use asterisks
- Or minuses
+ Or pluses
```

1. First ordered list item
2. Another item
  * Unordered sub-list.
1. Actual numbers don't matter, just that it's a number
   1. Ordered sub-list
4. And another item.

   You can have properly indented paragraphs within list items. Notice the blank line above, and the leading spaces (at least one, but we'll use three here to also align the raw Markdown).

   To have a line break without a paragraph, you will need to use two trailing spaces.
   Note that this line is separate, but within the same paragraph.
   (This is contrary to the typical GFM line break behaviour, where trailing spaces are not required.)

* Unordered list can use asterisks
- Or minuses
+ Or pluses
---

<sup></sup>
## Links

```no-highlight

[I'm an inline-style link](https://www.google.com)

Or use the link text itself [https://www.google.com](https://www.google.com).

```

[I'm an inline-style link](https://www.google.com)

Or use the link text itself [https://www.google.com](https://www.google.com).

---

## Images
```no-highlight
Images can be added this way:
![alt-text](https://www.placecage.com/c/300/150)
```
![alt-text](https://www.placecage.com/c/300/150)

![alt-text](https://www.placecage.com/c/200/300)


Alternatively you can upload an image from your phone to Imgur and from the app easily by either
pressing the image icon on the bottom bar in the editor or by going to `Imgur Uploads` from the 
navigation menu.

<sup></sup>
## Code

```no-highlight
Inline `code` has `back-ticks around` it.

```
Inline `code` has `back-ticks around` it.

Blocks of code are either fenced by lines with three back-ticks <code>```</code>, or are indented with four spaces. I recommend only using the fenced code blocks -- they're easier and only they support syntax highlighting. 

Syntax highlighting is done by writing the language name after the opening back-ticks <code>```java</code>

```javascript
var s = "JavaScript syntax highlighting";
alert(s);
```

```python
s = "Python syntax highlighting"
print s
```

No language indicated, so no syntax highlighting.
But let's throw in a <b>tag</b>.

---
<sup></sup>
## Tables

Colons can be used to align columns.
```no-highlight
| Tables        | Are           | Cool  |
| ------------- |:-------------:| -----:|
| col 3 is      | right-aligned | $1600 |
| col 2 is      | centered      |   $12 |
| zebra stripes | are neat      |    $1 |

```
| Tables        | Are           | Cool  |
| ------------- |:-------------:| -----:|
| col 3 is      | right-aligned | $1600 |
| col 2 is      | centered      |   $12 |
| zebra stripes | are neat      |    $1 |

There must be at least 3 dashes separating each header cell.
The outer pipes (|) are optional, and you don't need to make the
raw Markdown line up prettily. You can also use inline Markdown.

```no-highlight
Markdown | Less | Pretty
--- | --- | ---
*Still* | `renders` | **nicely**
1 | 2 | 3
```

Markdown | Less | Pretty
--- | --- | ---
*Still* | `renders` | **nicely**
1 | 2 | 3

---
<sup></sup>
## Blockquotes

```no-highlight
> Blockquotes are very handy in email to emulate reply text.
> This line is part of the same quote.
```

> Blockquotes are very handy in email to emulate reply text.
> This line is part of the same quote.

Quote break.
```no-highlight
> This is a very long line that will still be quoted properly when it wraps. Oh boy let's keep writing to make sure this is long enough to actually wrap for everyone. Oh, you can *put* **Markdown** into a blockquote.
```
> This is a very long line that will still be quoted properly when it wraps. Oh boy let's keep writing to make sure this is long enough to actually wrap for everyone. Oh, you can *put* **Markdown** into a blockquote.

Nested quotes
```no-highlight
> Hello!
>> And to you!
```
> Hello!
>> And to you!

---
<sup></sup>
## Inline HTML


* Emphasis (`<i>`, `<em>`, `<cite>`, `<dfn>`)
* Strong emphasis (`<b>`, `<strong>`)
* SuperScript (`<sup>`)
* SubScript (`<sub>`)
* Underline (`<u>`)
* Strike-through (`<s>`, `<strike>`, `<del>`)

Let's use it:
<u><i>H<sup>T<sub>M</sub></sup><b><s>L</s></b></i></u>

---
<sup></sup>
## Horizontal Rule

Three or more...


Hyphens (`-`)
---

Asterisks (`*`)
***

Underscores (`_`)
---

## License

```
  Copyright 2017 Dimitry Ivanov (mail@dimitryivanov.ru)

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
```
License: [CC-BY](https://creativecommons.org/licenses/by/3.0/)

[arbitrary case-insensitive reference text]: https://www.mozilla.org
[1]: http://slashdot.org
[link text itself]: http://www.reddit.com
[cheatsheet]: https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet