/**
 * This file is part of Shadowhunt WebDav Servlet.
 *
 * Shadowhunt WebDav Servlet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Shadowhunt WebDav Servlet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Shadowhunt WebDav Servlet.  If not, see <http://www.gnu.org/licenses/>.
 */
grammar Precondition;

precondition: (noTagList | tagList)+;

noTagList: (list)+;
tagList: resourceTag (list)+;

list: '(' (condition)+ ')';
resourceTag: '<' URL '>';

condition: (NOT)? ( '<' STATE_TOKEN '>' | entityTag);

entityTag: '[' STRING ']';

NOT: [Nn] [Oo] [Tt];

STATE_TOKEN: (DIGIT | LETTER | STATE_SPECIAL)+;
STRING: (DIGIT | LETTER)+;
URL: (DIGIT | LETTER | URL_SPECIAL)+;

DIGIT: ('0' .. '9');
LETTER: ('a' .. 'z') | ('A' .. 'Z');
URL_SPECIAL: '$' | '-' | '_' | '.' | '+' | '!' | '*' | '\'' | '(' | ')' | ',';
STATE_SPECIAL: '-' | ':';

WS: [ \r\n\t]+ -> channel (HIDDEN);
