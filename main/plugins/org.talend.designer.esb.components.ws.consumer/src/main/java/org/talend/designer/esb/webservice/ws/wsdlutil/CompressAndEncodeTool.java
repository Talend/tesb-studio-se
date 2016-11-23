package org.talend.designer.esb.webservice.ws.wsdlutil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;

import org.apache.commons.codec.binary.Base64OutputStream;

/**
 * Tool use to compress a wsdl definition to a compressed base64 String.
 */
public class CompressAndEncodeTool {

    /**
     * Gets WSDL as ZLIB-compressed and Base64-encoded String. Use ZipStream -> InflaterStream -> Base64Stream to read.
     * 
     * @return WSDL as String object. Or null in case errors/not possible to create object.
     * @throws WSDLException
     * @throws IOException
     */
    public static String compressAndEncode(Definition definition) throws IOException, WSDLException {
        ByteArrayOutputStream wsdlOs = new ByteArrayOutputStream();
        OutputStream os = compressAndEncodeStream(wsdlOs);
        ZipOutputStream zipOs = new ZipOutputStream(os);
        try {
            ZipEntry zipEntry = new ZipEntry("main.wsdl");
            zipOs.putNextEntry(zipEntry);
            WSDLFactory.newInstance().newWSDLWriter().writeWSDL(definition, zipOs);
            appendImportDifinitions(definition, zipOs);
        } finally {
            if (null != zipOs) {
                zipOs.close();
            }
        }
        return wsdlOs.toString();
    }

    private static OutputStream compressAndEncodeStream(OutputStream os) {
        return new DeflaterOutputStream(new Base64OutputStream(os));
    }

    @SuppressWarnings("unchecked")
    private static void appendImportDifinitions(Definition definition, ZipOutputStream zipOs) throws IOException, WSDLException {
        for (Collection<Import> vector : (Collection<Collection<Import>>) definition.getImports().values()) {
            for (Import impt : vector) {
                zipOs.putNextEntry(new ZipEntry(impt.getLocationURI()));
                WSDLFactory.newInstance().newWSDLWriter().writeWSDL(impt.getDefinition(), zipOs);
                appendImportDifinitions(impt.getDefinition(), zipOs);
            }
        }
    }
}
