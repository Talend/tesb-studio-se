/*******************************************************************************
 *  Copyright (c) 2011-2017 Talend Inc. - www.talend.com
 *  All rights reserved.
 *
 *  This program and the accompanying materials are made available
 *  under the terms of the Apache License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 ******************************************************************************/
package routines.system.api;

/**
 * A factory interface to create to create specific kind of {@link TalendESBJob}. The factor allows the
 * Talend Runtime to create several instances of the job and to enable concurrent access.
 */
public interface TalendESBJobFactory {

    /**
     * Creates a new {@link TalendESBJob}. All instances returned must be different and of the same type.
     *
     * @return a new {@link ESBEndpointInfo} instance,  must not be <code>null</code>.
     */
    TalendESBJob newTalendESBJob();
}
