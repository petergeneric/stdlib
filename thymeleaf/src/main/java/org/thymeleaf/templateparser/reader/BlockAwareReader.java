/*
 * =============================================================================
 *
 *   Copyright (c) 2011-2018, The THYMELEAF team (http://www.thymeleaf.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * =============================================================================
 */
package org.thymeleaf.templateparser.reader;

import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author Daniel Fern&aacute;ndez
 * @since 3.0.0
 * 
 */
abstract class BlockAwareReader extends Reader {

    public enum BlockAction { DISCARD_ALL, DISCARD_CONTAINER }


    private final Reader reader;
    private final BlockAction action;
    private final char[] prefix, suffix;
    private final char p0, s0;

    
    private char[] overflowBuffer = null;
    private int overflowBufferLen = 0;

    private boolean insideComment = false;
    private int index = 0;
    private int discardFrom = -1;


    protected BlockAwareReader(final Reader reader, final BlockAction action, final char[] prefix, final char[] suffix) {
        super();
        this.reader = reader;
        this.action = action;
        this.prefix = prefix;
        this.suffix = suffix;
        this.p0 = this.prefix[0];
        this.s0 = this.suffix[0];
    }





    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException {


        int read = readBytes(cbuf, off, len);
        if (read <= 0) {

            if (read < 0 && this.insideComment) {
                // We have reached the end of the input but a block structure has been left unfinished, which
                // can lead to unexpected results --- better to throw an exception.
                throw new IOException(
                        "Unfinished block structure " + new String(this.prefix) + "..." + new String(this.suffix));
            }

            return read;

        }


        // At this point, discardFrom should only be -1 or 0, so we might have to adapt it to the specified offset
        this.discardFrom = (this.discardFrom < 0? this.discardFrom : Math.max(off, this.discardFrom));

        int maxi = off + read;

        char c;
        int i = off;
        while (i < maxi) {

            c = cbuf[i++];

            if (this.index == 0 && c != this.p0 && c != this.s0) {
                // Shortcut for most characters in a template: no further tests to be done
                continue;
            }

            if (!this.insideComment) {

                if (c == this.prefix[this.index]) {
                    this.index++;
                    if (this.index == this.prefix.length) {
                        // Remove the prefix, as if it was never there...
                        if (i < maxi) {
                            System.arraycopy(cbuf, i, cbuf, i - this.prefix.length, (maxi - i));
                        }
                        this.insideComment = true;
                        this.index = 0;
                        read -= this.prefix.length;
                        maxi -= this.prefix.length;
                        i -= this.prefix.length;
                        this.discardFrom = (this.action == BlockAction.DISCARD_ALL ? i : -1);
                    }
                } else {
                    if (this.index > 0) {
                        i -= this.index;
                    }
                    this.index = 0;
                }

            } else {

                if (c == this.suffix[this.index]) {
                    this.index++;
                    if (this.index == this.suffix.length) {

                        // Remove the suffix, as if it was never there...
                        if (i < maxi) {
                            System.arraycopy(cbuf, i, cbuf, i - this.suffix.length, (maxi - i));
                        }
                        this.insideComment = false;
                        this.index = 0;
                        read -= this.suffix.length;
                        maxi -= this.suffix.length;
                        i -= this.suffix.length;

                        // Once the suffix has been removed, we check whether we should actually also removed the
                        // block contents...
                        if (this.discardFrom >= 0) {
                            if (i < maxi) {
                                System.arraycopy(cbuf, i, cbuf, this.discardFrom, (maxi - i));
                            }
                            read -= (i - this.discardFrom);
                            maxi -= (i - this.discardFrom);
                            i = this.discardFrom;
                            this.discardFrom = -1;
                        }

                    }
                } else {
                    if (this.index > 0) {
                        i -= this.index;
                    }
                    this.index = 0;
                }

            }

        }


        if (this.index > 0) {
            // Oops, the buffer ended in something that could be a structure to be removed -- will need some more processing

            // First step is to copy the contents we doubt about to the overflow buffer and subtract them from cbuf
            overflowLastBytes(cbuf, maxi, this.index);
            read -= this.index;
            maxi -= this.index;

            // Second step is trying to complete the overflow buffer in order to make a decision on whether we are
            // really looking at a removable structure here or not...
            final char[] structure = (this.insideComment? this.suffix : this.prefix);

            // Check if we actually found a complete structure
            if (matchOverflow(structure)) {

                this.insideComment = !this.insideComment;
                // We don't modify the discardFrom flag here, as it will be needed for discarding (or not) part of the buffer afterwards
                this.overflowBufferLen -= structure.length;
                this.index = 0;

            } else {
                // We didn't find the structure we were looking for, and now we have an overflow that contains
                // several characters. Including the possibility that it includes the beginning of a structure...
                // At this stage, we know we can copy back JUST ONE of those "this.index" bytes into the cbuf array, so
                // that we don't try to match it against prefix/suffix again (but we allow any matches starting with
                // the next char)

                System.arraycopy(this.overflowBuffer, 0, cbuf, maxi, 1);
                read++;
                maxi++;
                System.arraycopy(this.overflowBuffer, 1, this.overflowBuffer, 0, (this.overflowBufferLen - 1));
                this.overflowBufferLen--;
                this.index = 0;

            }

        }


        if (this.discardFrom >= 0) {
            read -= (maxi - this.discardFrom);
            this.discardFrom = 0;
        }

        this.discardFrom = (this.insideComment && this.action == BlockAction.DISCARD_ALL? 0 : -1);

        return read;

    }




    private int readBytes(final char[] buffer, final int off, final int len) throws IOException {

        if (len == 0) {
            return 0;
        }

        if (this.overflowBufferLen == 0) {
            return this.reader.read(buffer, off, len);
        }

        if (this.overflowBufferLen <= len) {
            // Our overflow fits in the cbuf len, so we copy and ask the delegate reader to write from there

            System.arraycopy(this.overflowBuffer, 0, buffer, off, this.overflowBufferLen);
            int read = this.overflowBufferLen;
            this.overflowBufferLen = 0;

            if (read < len) {
                final int delegateRead = this.reader.read(buffer, (off + read), (len - read));
                if (delegateRead > 0) {
                    read += delegateRead;
                }
            }

            return read;

        }

        // we are asking for less characters than we currently have in overflow

        System.arraycopy(this.overflowBuffer, 0, buffer, off, len);
        if (len < this.overflowBufferLen) {
            System.arraycopy(this.overflowBuffer, len, this.overflowBuffer, 0, (this.overflowBufferLen - len));
        }
        this.overflowBufferLen -= len;
        return len;

    }




    private void overflowLastBytes(final char[] buffer, final int maxi, final int overflowCount) {
        if (this.overflowBuffer == null) {
            this.overflowBuffer = new char[Math.max(this.prefix.length, this.suffix.length)];
        }
        if (this.overflowBufferLen > 0) {
            System.arraycopy(this.overflowBuffer, 0, this.overflowBuffer, overflowCount, this.overflowBufferLen);
        }
        System.arraycopy(buffer, maxi - overflowCount, this.overflowBuffer, 0, overflowCount);
        this.overflowBufferLen += overflowCount;
    }




    private boolean matchOverflow(final char[] structure) throws IOException {

        if (this.overflowBufferLen > 0) {
            for (int i = 0; i < this.overflowBufferLen; i++) {
                if (this.overflowBuffer[i] != structure[i]) {
                    return false;
                }
            }
        }

        int overflowRead = 0;
        while (overflowRead >= 0 && this.overflowBufferLen < structure.length) {
            overflowRead = this.reader.read(this.overflowBuffer, this.overflowBufferLen, 1); // can only return 0 or 1
            if (overflowRead > 0) {
                this.overflowBufferLen++;
                if (this.overflowBuffer[this.overflowBufferLen - 1] != structure[this.overflowBufferLen - 1]) {
                    return false;
                }
            }
        }

        return (this.overflowBufferLen == structure.length);

    }




    @Override
    public void close() throws IOException {
        this.reader.close();
    }




}
