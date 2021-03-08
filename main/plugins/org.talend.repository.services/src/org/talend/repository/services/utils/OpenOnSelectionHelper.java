// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.services.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.wst.wsdl.Definition;
import org.eclipse.wst.wsdl.ui.internal.WSDLEditorPlugin;
import org.eclipse.wst.xsd.ui.internal.adt.editor.ADTReadOnlyFileEditorInput;
import org.eclipse.wst.xsd.ui.internal.editor.XSDEditorPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.properties.Item;
import org.talend.repository.services.action.ServiceEditorInput;


@SuppressWarnings("restriction")
public class OpenOnSelectionHelper extends org.eclipse.wst.wsdl.ui.internal.util.OpenOnSelectionHelper{

	public OpenOnSelectionHelper(Definition definition) {
		super(definition);
	}

	@Override
	protected void openEditor(String resource, String spec) {
	    String pattern = "platform:/resource"; //$NON-NLS-1$
	    IWorkbenchPage workbenchPage = WSDLEditorPlugin.getInstance().getWorkbench().getActiveWorkbenchWindow().getActivePage();
	    IEditorPart editorPart = workbenchPage.getActiveEditor();
	    String currentEditorId = editorPart.getEditorSite().getId();
	    if (resource != null && resource.startsWith(pattern)) {
	        Path path = new Path(resource.substring(pattern.length()));
	        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);

	        if (editorPart.getEditorInput() instanceof IFileEditorInput &&
	        		((IFileEditorInput)editorPart.getEditorInput()).getFile().equals(file)) {
	        	workbenchPage.getNavigationHistory().markLocation(editorPart);
	        } else {
	        	try {
	        		Item item=((ServiceEditorInput)editorPart.getEditorInput()).getItem();
	        		// TODO: Use content type as below
	        		if (resource.endsWith("xsd")) { //$NON-NLS-1$
	        			editorPart = workbenchPage.openEditor(new ServiceEditorInput(file,item), WSDLEditorPlugin.XSD_EDITOR_ID);
	        		} else {
	        			// Since we are already in the wsdleditor
	        			editorPart =  workbenchPage.openEditor(new ServiceEditorInput(file,item), editorPart.getEditorSite().getId());
	        		}
	        	} catch (PartInitException initEx) {
	        	    ExceptionHandler.process(initEx);
	        	}
	        }

	        try {
                Class<? extends IEditorPart> theClass = editorPart.getClass();
                Class<?>[] methodArgs = { String.class };
                Method method = theClass.getMethod("openOnSelection", methodArgs); //$NON-NLS-1$
                Object args[] = { spec };
                method.invoke(editorPart, args);
                workbenchPage.getNavigationHistory().markLocation(editorPart);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
	    } else if (resource != null && resource.startsWith("http")) {
	      IEditorPart newEditorPart = null;
	      boolean doOpenWsdlEditor = true;
	      if (resource.endsWith("xsd")) //$NON-NLS-1$
	      {
	        doOpenWsdlEditor = false;
	      }
	      try
	      {
	        IEditorReference[] refs = workbenchPage.getEditorReferences();
	        int length = refs.length;
	        // Need to find if an editor on that schema has already been opened
	        for (int i = 0; i < length; i++)
	        {
	          IEditorInput input = refs[i].getEditorInput();
	          if (input instanceof ADTReadOnlyFileEditorInput) {
	            ADTReadOnlyFileEditorInput readOnlyEditorInput = (ADTReadOnlyFileEditorInput) input;
	            if (readOnlyEditorInput.getUrlString().equals(resource) &&
	                (!doOpenWsdlEditor && readOnlyEditorInput.getEditorID().equals(WSDLEditorPlugin.XSD_EDITOR_ID)
	                || doOpenWsdlEditor && readOnlyEditorInput.getEditorID().equals(WSDLEditorPlugin.WSDL_EDITOR_ID))) {
	              newEditorPart = refs[i].getEditor(true);
	              workbenchPage.activate(refs[i].getPart(true));
	              break;
	            }
	          }
	        }
	        if (newEditorPart == null)
	        {
	          ADTReadOnlyFileEditorInput readOnlyStorageEditorInput = new ADTReadOnlyFileEditorInput(resource);
	          IContentType contentType = null;
	          try (InputStream iStream = readOnlyStorageEditorInput.getStorage().getContents()) {
                    contentType = Platform.getContentTypeManager().findContentTypeFor(iStream, resource);
              }
	          // content type more reliable check
	          if (contentType != null && contentType.equals(XSDEditorPlugin.XSD_CONTENT_TYPE_ID) || resource.endsWith("xsd")) //$NON-NLS-1$
	          {
	            readOnlyStorageEditorInput.setEditorID(WSDLEditorPlugin.XSD_EDITOR_ID);
	            workbenchPage.openEditor(readOnlyStorageEditorInput, WSDLEditorPlugin.XSD_EDITOR_ID, true, 0); //$NON-NLS-1$
	          }
	          else
	          {
	            readOnlyStorageEditorInput.setEditorID(currentEditorId);
	            workbenchPage.openEditor(readOnlyStorageEditorInput, currentEditorId, true, 0); //$NON-NLS-1$
	          }
	        }
	      } catch (IOException | CoreException e) {
              ExceptionHandler.process(e);
	      }
	    }

	}
}
