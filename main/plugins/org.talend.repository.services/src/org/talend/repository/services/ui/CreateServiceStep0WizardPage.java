//============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2013 Talend – www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
//============================================================================
package org.talend.repository.services.ui;

import org.eclipse.core.runtime.IPath;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.ui.wizards.NewProcessWizardPage;
import org.talend.repository.services.utils.ESBRepositoryNodeType;


/**
 * DOC sunchaoqun  class global comment. Detailled comment
 * <br/>
 *
 * $Id$
 *
 */
public class CreateServiceStep0WizardPage extends NewProcessWizardPage {

    /**
     * DOC sunchaoqun CreateServiceStep0WizardPage constructor comment.
     * @param property
     * @param destinationPath
     */
    public CreateServiceStep0WizardPage(Property property, IPath destinationPath) {
        super(property, destinationPath);
        setTitle("New Service");
        setDescription("Add a Service in the repository");
    }

    @Override
    public ERepositoryObjectType getRepositoryObjectType() {
        return ESBRepositoryNodeType.SERVICES;
    }
}
