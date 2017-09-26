// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2017 Talend – www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.esb.camel.designer.codegen.jet;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.talend.camel.designer.codegen.jet.CamelJetFileProvider;
import org.talend.designer.codegen.config.TemplateUtil;

public class CamelJetFileProviderTest {

    /**
     * Test method for
     * {@link org.talend.camel.designer.codegen.jet.CamelJetFileProvider#initializeStubAdditionalJetFile()}.
     */
    @Test
    public void testInitialize() {
        List<String> resTemplates = Arrays.asList(new String[] { "header_route", "footer_route", "camel_speciallinks",
                "camel_run_if" });
        List<TemplateUtil> templates = new CamelJetFileProvider().initializeStubAdditionalJetFile();
        for (TemplateUtil t : templates) {
            Assert.assertTrue(resTemplates.contains(t.getType().getTemplateName()));
        }
    }
}
