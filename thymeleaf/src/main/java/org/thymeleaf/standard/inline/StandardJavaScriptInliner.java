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
package org.thymeleaf.standard.inline;

import java.io.Writer;

import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.standard.serializer.IStandardJavaScriptSerializer;
import org.thymeleaf.standard.serializer.StandardSerializers;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.util.FastStringWriter;

/**
 *
 * @author Daniel Fern&aacute;ndez
 * @since 3.0.0
 * 
 */
public final class StandardJavaScriptInliner extends AbstractStandardInliner {

    private final IStandardJavaScriptSerializer serializer;


    public StandardJavaScriptInliner(final IEngineConfiguration configuration) {
        super(configuration, TemplateMode.JAVASCRIPT);
        this.serializer = StandardSerializers.getJavaScriptSerializer(configuration);
    }


    @Override
    protected String produceEscapedOutput(final Object input) {
        final Writer jsWriter = new FastStringWriter(input instanceof String? ((String)input).length() * 2 : 20);
        this.serializer.serializeValue(input, jsWriter);
        return jsWriter.toString();
    }

}
