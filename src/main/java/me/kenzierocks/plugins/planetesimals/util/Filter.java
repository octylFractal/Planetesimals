/*
 * This file is part of Planetesimals, licensed under the MIT License (MIT).
 *
 * Copyright (c) kenzierocks (Kenzie Togami) <https://kenzierocks.me>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.kenzierocks.plugins.planetesimals.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.IntPredicate;

public interface Filter {
    
    Filter NONE = new Builder().build();

    class Builder {

        private static final class FilterImpl implements Filter {

            private static final IntPredicate TRUE = anything -> true;

            private final IntPredicate x;
            private final IntPredicate y;
            private final IntPredicate z;

            private FilterImpl(IntPredicate x, IntPredicate y, IntPredicate z) {
                this.x = x == null ? TRUE : x;
                this.y = y == null ? TRUE : y;
                this.z = z == null ? TRUE : z;
            }

            @Override
            public IntPredicate getXFilter() {
                return this.x;
            }

            @Override
            public IntPredicate getYFilter() {
                return this.y;
            }

            @Override
            public IntPredicate getZFilter() {
                return this.z;
            }

        }

        private IntPredicate x;
        private IntPredicate y;
        private IntPredicate z;

        private Builder() {
        }

        public Builder x(IntPredicate x) {
            this.x = checkNotNull(x);
            return this;
        }

        public Builder y(IntPredicate y) {
            this.y = checkNotNull(y);
            return this;
        }

        public Builder z(IntPredicate z) {
            this.z = checkNotNull(z);
            return this;
        }

        public Filter build() {
            return new FilterImpl(this.x, this.y, this.z);
        }

    }

    static Builder x(IntPredicate xFilt) {
        return new Builder().x(xFilt);
    }

    static Builder y(IntPredicate yFilt) {
        return new Builder().y(yFilt);
    }

    static Builder z(IntPredicate zFilt) {
        return new Builder().z(zFilt);
    }

    IntPredicate getXFilter();

    IntPredicate getYFilter();

    IntPredicate getZFilter();

}
