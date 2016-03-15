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

precondition: implicitResourceList* explicitResourceList*;

implicitResourceList:          list+;
explicitResourceList: resource list+;

list:     '(' condition+ ')';
resource: '<' URL_TOKEN '>';

condition: ( lock | etag );

lock: '<' LOCK_TOKEN '>';
etag: '[' ETAG_TOKEN ']';

ETAG_TOKEN: (DIGIT | LETTER               )+;
LOCK_TOKEN: (DIGIT | LETTER | LOCK_SPECIAL)+;
URL_TOKEN:  (DIGIT | LETTER | URL_SPECIAL )+;

DIGIT:        ('0' .. '9');
LETTER:       ('a' .. 'z') | ('A' .. 'Z');
LOCK_SPECIAL: ('-' | ':');
URL_SPECIAL:  ('$' | '-' | '_' | '.' | '+' | '!' | '*' | '\'' | '(' | ')' | ',' | '/' | ':' );

WS: [ \r\n\t]+ -> channel (HIDDEN);
