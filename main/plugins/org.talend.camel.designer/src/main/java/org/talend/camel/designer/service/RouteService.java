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
package org.talend.camel.designer.service;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.talend.camel.designer.runprocess.maven.BundleJavaProcessor;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.runtime.process.IBuildJobHandler;
import org.talend.core.service.IESBRouteService;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.maven.MavenJavaProcessor;
import org.talend.repository.model.IRepositoryNode;

/**
 * DOC sunchaoqun  class global comment. Detailled comment
 * <br/>
 *
 * $Id$
 *
 */
public class RouteService implements IESBRouteService {

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.service.IESBMicroService#createJavaProcessor(org.talend.core.model.process.IProcess,
     * org.talend.core.model.properties.Property, boolean, boolean)
     */
    @Override
    public IProcessor createJavaProcessor(IProcess process, Property property, boolean filenameFromLabel, boolean isRoute) {
        return (MavenJavaProcessor) new BundleJavaProcessor(process, property, filenameFromLabel, isRoute);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.service.IESBMicroService#createRunnableWithProgress(java.util.Map, java.util.List,
     * java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public IRunnableWithProgress createRunnableWithProgress(Map exportChoiceMap, List<? extends IRepositoryNode> nodes,
            String version, String destinationPath, String context) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.service.IESBMicroService#buildJob(java.lang.String,
     * org.talend.core.model.properties.ProcessItem, java.lang.String, java.lang.String, java.util.Map,
     * org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void buildJob(String destinationPath, ProcessItem itemToExport, String version, String context, Map exportChoiceMap,
            IProgressMonitor monitor) throws Exception {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.service.IESBMicroService#createBuildJobHandler(org.talend.core.model.properties.ProcessItem,
     * java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    public IBuildJobHandler createBuildJobHandler(ProcessItem itemToExport, String version, String context, Map exportChoiceMap) {
        // TODO Auto-generated method stub
        return null;
    }

}
