/*
 * Copyright 2012 Goran Ehrsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugins.texttemplate

/**
 * Basic implementation that stores the current tenant in a threadlocal variable.
 */
class TestCurrentTenant {

    private static final ThreadLocal<Integer> contextHolder = new ThreadLocal<Long>()

    public Integer get() {
        return contextHolder.get()
    }

    public void set(Integer tenant) {
        contextHolder.set(tenant)
    }
}
