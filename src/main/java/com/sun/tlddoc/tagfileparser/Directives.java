/*
 * <license>
 * Copyright (c) 2003-2004, Sun Microsystems, Inc.
 * Copyright (c) 2022-2022, Web-Legacy
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </license>
 */

package com.sun.tlddoc.tagfileparser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates all directives in a tag file.
 *
 * @author  ste-gr
 */
public class Directives {

    /**
     * List of all {@link Directive}s.
     */
    private final ArrayList<Directive> directives = new ArrayList<>();

    /**
     * Creates a new instance of Directives
     */
    public Directives() {
    }

    /**
     * Adds an directive to the list of directives.
     *
     * @param directive to add to the list of directives
     */
    public void addDirective( Directive directive ) {
        this.directives.add( directive );
    }

    /**
     * Returns an iterator through the set of directives.
     *
     * @return an iterator through the set of directives
     */
    public List<Directive> getDirectives() {
        return Collections.unmodifiableList(directives);
    }

    /**
     * Returns a string representation of this directives
     *
     * @return a string representation of this directives
     */
    @Override
    public String toString() {
        return directives.toString();
    }
}