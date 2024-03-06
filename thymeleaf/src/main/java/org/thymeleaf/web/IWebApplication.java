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
package org.thymeleaf.web;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Daniel Fern&aacute;ndez
 *
 * @since 3.1.0
 * 
 */
public interface IWebApplication {

    public boolean containsAttribute(final String name);
    public int getAttributeCount();
    public Set<String> getAllAttributeNames();
    public Map<String,Object> getAttributeMap();
    public Object getAttributeValue(final String name);
    public void setAttributeValue(final String name, final Object value);
    public void removeAttribute(final String name);

    public boolean resourceExists(final String path);
    public InputStream getResourceAsStream(final String path);

}
