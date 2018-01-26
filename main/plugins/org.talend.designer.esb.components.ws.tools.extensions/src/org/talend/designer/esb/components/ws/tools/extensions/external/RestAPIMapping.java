package org.talend.designer.esb.components.ws.tools.extensions.external;
// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

/**
 * DOC dsergent class global comment. Detailled comment
 */
public class RestAPIMapping {

    private String outputFlow = "";

    private String httpVerb = "";

    private String uriPattern = "";

    private EESBMediaType consumes;

    private EESBMediaType produces;

    /**
     * DOC dsergent RestAPIMapping constructor comment.
     */
    public RestAPIMapping() {
        super();
    }

    /**
     * Getter for outputFlow.
     * 
     * @return the outputFlow
     */
    public String getOutputFlow() {
        return this.outputFlow;
    }

    /**
     * Sets the outputFlow.
     * 
     * @param outputFlow the outputFlow to set
     */
    public void setOutputFlow(String outputFlow) {
        this.outputFlow = outputFlow;
    }

    /**
     * Getter for httpVerb.
     * 
     * @return the httpVerb
     */
    public String getHttpVerb() {
        return this.httpVerb;
    }

    /**
     * Sets the httpVerb.
     * 
     * @param httpVerb the httpVerb to set
     */
    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    /**
     * Getter for uriPattern.
     * 
     * @return the uriPattern
     */
    public String getUriPattern() {
        return this.uriPattern;
    }

    /**
     * Sets the uriPattern.
     * 
     * @param uriPattern the uriPattern to set
     */
    public void setUriPattern(String uriPattern) {
        this.uriPattern = uriPattern;
    }

    /**
     * Getter for consumes.
     * 
     * @return the consumes
     */
    public EESBMediaType getConsumes() {
        return this.consumes;
    }

    /**
     * Sets the consumes.
     * 
     * @param consumes the consumes to set
     */
    public void setConsumes(EESBMediaType consumes) {
        this.consumes = consumes;
    }

    /**
     * Getter for produces.
     * 
     * @return the produces
     */
    public EESBMediaType getProduces() {
        return this.produces;
    }

    /**
     * Sets the produces.
     * 
     * @param produces the produces to set
     */
    public void setProduces(EESBMediaType produces) {
        this.produces = produces;
    }

}
