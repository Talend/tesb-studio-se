package org.talend.designer.camel.resource.ui.util;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.talend.camel.core.model.camelProperties.RouteResourceItem;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.designer.camel.resource.editors.ResourceEditorListener;
import org.talend.designer.camel.resource.editors.RouteResourceEditor;
import org.talend.designer.camel.resource.editors.RouteResoureChangeListener;
import org.talend.designer.camel.resource.editors.input.RouteResourceInput;
import org.talend.designer.core.DesignerPlugin;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryService;

public class RouteResourceEditorUtil {

	private static RouteResourceEditorUtil INSTANCE = new RouteResourceEditorUtil();

	private RouteResourceEditorUtil() {
	}

	/**
	 * find a prefer editor from all existing editors according to the
	 * fileExtensions
	 * 
	 * @param fileExtension
	 * @return
	 */
	private String findPreferEditor(RouteResourceInput fileEditorInput) {
		String editorId = RouteResourceEditor.ID;

		Object underlingFile = fileEditorInput.getAdapter(IFile.class);
		if (underlingFile == null) {
			return editorId;
		}

		IEditorDescriptor ed = null;
		try {
			ed = IDE.getEditorDescriptor((IFile) underlingFile, true);
		} catch (PartInitException e) {
			return editorId;
		}
		if (ed == null) {
			return editorId;
		}
		String id = ed.getId();
		if (id == null || id.trim() == null) {
			return editorId;
		}
		return id;
	}

	/**
	 * Open default editor
	 */
	public static void openDefaultEditor(final IWorkbenchPage page,
			IRepositoryNode node, RouteResourceItem item) {
		RouteResourceInput fileEditorInput = RouteResourceInput
				.createInput(node, item);

		openEditor(page, fileEditorInput, item, RouteResourceEditor.ID);
	}

	/**
	 * Open or bind Route resource editor.
	 */
	public static void openEditor(final IWorkbenchPage page,
			IRepositoryNode node, RouteResourceItem item) {
		RouteResourceInput fileEditorInput = RouteResourceInput
				.createInput(node, item);
		if(fileEditorInput.isReadOnly()){
			openDefaultEditor(page, node, item);
		}else{
			openEditor(page, fileEditorInput, item,
					RouteResourceEditorUtil.INSTANCE.findPreferEditor(fileEditorInput));
		}
	}

	/**
	 * Open or bind Route resource editor by specifying editorID
	 */
	public static void openEditor(final IWorkbenchPage page,
			RouteResourceInput fileEditorInput, RouteResourceItem item,
			String editorId) {
		try {
			IEditorPart editorPart = page.findEditor(fileEditorInput);

			page.getWorkbenchWindow()
					.getPartService()
					.addPartListener(
							new ResourceEditorListener(fileEditorInput, page));

			if (!RouteResourceEditor.ID.endsWith(editorId)) {
				ResourcesPlugin.getWorkspace().addResourceChangeListener(
						new RouteResoureChangeListener(fileEditorInput));
			}

			if (editorPart == null) {
				editorPart = page.openEditor(fileEditorInput, editorId, true);
			} else {
				editorPart = page.openEditor(fileEditorInput, editorId);
			}
		} catch (PartInitException e) {
			try {
				ProxyRepositoryFactory.getInstance().unlock(item);
			} catch (Exception ie) {
			    ExceptionHandler.process(e);
			}
			MessageBoxExceptionHandler.process(e);
		}
	}
	
	public static boolean isReadOnly(IRepositoryNode node){
		IRepositoryService service = DesignerPlugin.getDefault().getRepositoryService();
		IProxyRepositoryFactory repFactory = service.getProxyRepositoryFactory();
		
		/*
		 * if user is readonly , then set enable as false
		 */
		if(repFactory.isUserReadOnlyOnCurrentProject()){
			return true;
		}
		
		// if it's not in current project, then it's disable
		if(!ProjectManager.getInstance().isInCurrentMainProject(node)){
			return true;
		}
		
		// if it's locked by others, then it's disable
		IRepositoryViewObject object = node.getObject();
		if(object == null){
			return false;
		}
		Property property = object.getProperty();
		if(property == null){
			return false;
		}
		
		Item item = property.getItem();
		if(item == null){
			return false;
		}
		ERepositoryStatus status = repFactory.getStatus(item);
		if(ERepositoryStatus.LOCK_BY_OTHER.equals(status) || ERepositoryStatus.DELETED.equals(status)){
			return true;
		}
		
		if(!isLatestVersion(property)){
			return true;
		}
		
		return false;
	}

	private static boolean isLatestVersion(Property property){
		try{
			List<IRepositoryViewObject> allVersion = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory().getAllVersion(property.getId());
			if (allVersion == null || allVersion.isEmpty()) {
				return false;
			}
			String lastVersion = VersionUtils.DEFAULT_VERSION;

			for (IRepositoryViewObject object : allVersion) {
				if (VersionUtils.compareTo(object.getVersion(), lastVersion) > 0) {
					lastVersion = object.getVersion();
				}
			}
			return VersionUtils.compareTo(property.getVersion(), lastVersion) == 0;
		} catch(PersistenceException e) {
		    ExceptionHandler.process(e);
		}
		return true;
	}
}
