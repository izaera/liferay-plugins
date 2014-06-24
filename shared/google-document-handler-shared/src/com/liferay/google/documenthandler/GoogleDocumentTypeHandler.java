/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.google.documenthandler;

import com.liferay.google.api.configuration.GoogleAPIConfiguration;
import com.liferay.google.documenthandler.util.ResourceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.model.Company;
import com.liferay.portal.service.ClassNameLocalService;
import com.liferay.portal.service.CompanyLocalService;
import com.liferay.portal.service.UserLocalService;
import com.liferay.portlet.documentlibrary.documenttype.BaseDocumentTypeHandler;
import com.liferay.portlet.documentlibrary.documenttype.ContextAction;
import com.liferay.portlet.documentlibrary.documenttype.DocumentTypeHandler;
import com.liferay.portlet.documentlibrary.documenttype.PageFragment;
import com.liferay.portlet.documentlibrary.documenttype.ToolbarButton;
import com.liferay.portlet.documentlibrary.model.DLFileEntry;
import com.liferay.portlet.documentlibrary.model.DLFileEntryType;
import com.liferay.portlet.documentlibrary.model.DLFileVersion;
import com.liferay.portlet.documentlibrary.service.DLFileEntryMetadataLocalService;
import com.liferay.portlet.documentlibrary.service.DLFileEntryTypeLocalService;
import com.liferay.portlet.dynamicdatamapping.model.DDMStructure;
import com.liferay.portlet.dynamicdatamapping.service.DDMStructureLocalService;
import com.liferay.portlet.dynamicdatamapping.storage.StorageEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

// TODO: compose picker button HTML with JSP engine
// TODO: add icon and name of document to the right of the upload button
// TODO: move Javascript code to local scope
// TODO: disable download link in asset publisher
// TODO: show some message in asset publisher when there's no preview
// TODO: listen to newly created companies to register Google doctype
// TODO: remove upload warning from edit_file_entry.jsp

/**
 * @author Iván Zaera
 */
@Component(service = DocumentTypeHandler.class, immediate = true)
public class GoogleDocumentTypeHandler extends BaseDocumentTypeHandler {

	public static final UUID ID = UUID.fromString(
		"6E4493C1-268E-49A7-8AD1-A6A060F163A6");

	public GoogleDocumentTypeHandler() {
		super(ID);

		_filePickerHtml = ResourceUtil.get(this, _FILE_PICKER_HTML);
		_previewHtml = ResourceUtil.get(this, _PREVIEW_HTML);
	}

	@Activate
	public void activate() throws PortalException {
		_addGoogleDocumentTypes();
	}

	@Override
	public List<ContextAction> getExtraContextActions(DLFileEntry dlFileEntry)
		throws PortalException {

		List<ContextAction> contextActions = new ArrayList<ContextAction>();

		String editURL = _getEditURL(dlFileEntry.getFileVersion());

		if (!editURL.equals("")) {
			contextActions.add(
				new ContextAction("icon-edit", "Edit in Google", editURL));
		}

		return contextActions;
	}

	@Override
	public List<ToolbarButton> getExtraToolbarButtons(DLFileEntry dlFileEntry)
		throws PortalException {

		List<ToolbarButton> toolbarButtons = new ArrayList<ToolbarButton>();

		String editURL = _getEditURL(dlFileEntry.getFileVersion());

		if (!editURL.equals("")) {
			toolbarButtons.add(
				new ToolbarButton(
					"edit", "Edit in Google",
					"window.open('" + editURL + "');"));
		}

		return toolbarButtons;
	}

	@Override
	public PageFragment getFilePreview(DLFileVersion dlFileVersion)
		throws PortalException {

		String html = null;

		String embedURL = _getEmbedURL(dlFileVersion);

		if (embedURL.equals("")) {
			html = "<br>";
		}
		else {
			html = _previewHtml.replaceAll(
				_VIEW_URL_TOKEN, embedURL.toString());
		}

		return new PageFragment(PageFragment.Type.HTML, html);
	}

	@Override
	public PageFragment getSelectFileButton(
		DLFileEntryType dlFileEntryType, String callbackFunction) {

		long companyId = dlFileEntryType.getCompanyId();

		String apiKey = _googleAPIConfiguration.getAPIKey(companyId);

		String clientId = _googleAPIConfiguration.getClientID(companyId);

		String html = _filePickerHtml.replaceAll(_API_KEY_TOKEN, apiKey);

		html = html.replaceAll(_CLIENT_ID_TOKEN, clientId);

		html = html.replaceAll(_CALLBACK_FUNCTION_TOKEN, callbackFunction);

		return new PageFragment(PageFragment.Type.HTML, html);
	}

	@Override
	public boolean handles(DLFileEntryType dlFileEntryType) {
		List<DDMStructure> ddmStructures = dlFileEntryType.getDDMStructures();

		for (DDMStructure ddmStructure : ddmStructures) {
			String structureKey = ddmStructure.getStructureKey();

			if (structureKey.equals(Constants.GOOGLE_DOCUMENT_STRUCTURE_KEY)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isDownloadable(DLFileEntry dlFileEntry) {
		return false;
	}

	@Override
	public boolean isOfficeDocument(DLFileEntry dlFileEntry) {
		return false;
	}

	@Override
	public boolean isVisible(DDMStructure ddmStructure) {
		String structureKey = ddmStructure.getStructureKey();

		return !structureKey.equals(Constants.GOOGLE_DOCUMENT_STRUCTURE_KEY);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void processUpload(
			boolean newFileEntry, DLFileEntry dlFileEntry, String jsonPayload)
		throws PortalException {

		if (!jsonPayload.equals("")) {
			Map<String, ?> jsonObject =
				(Map<String, ?>)JSONFactoryUtil.looseDeserialize(jsonPayload);

			String id = (String)jsonObject.get(_ID_JSON_FIELD);
			String title = (String)jsonObject.get(_TITLE_JSON_FIELD);
			String icon = (String)jsonObject.get(_ICON_JSON_FIELD);
			String embedURL = (String)jsonObject.get(_EMBED_URL_JSON_FIELD);
			String editURL = (String)jsonObject.get(_EDIT_URL_JSON_FIELD);

			GoogleDocumentMetadataHelper googleDocumentMetadataHandler =
					new GoogleDocumentMetadataHelper(
						dlFileEntry.getFileVersion(),
						_dlFileEntryMetadataLocalService,
						_dlFileEntryTypeLocalService, _storageEngine);

			googleDocumentMetadataHandler.updateMetadata(
				id, title, icon, embedURL, editURL);
		}
	}

	@Reference
	public void setClassNameLocalService(
		ClassNameLocalService classNameLocalService) {

		_classNameLocalService = classNameLocalService;
	}

	@Reference
	public void setCompanyLocalService(
		CompanyLocalService companyLocalService) {

		_companyLocalService = companyLocalService;
	}

	@Reference
	public void setDDMStructureLocalService(
		DDMStructureLocalService ddmStructureLocalService) {

		_ddmStructureLocalService = ddmStructureLocalService;
	}

	@Reference
	public void setDLFileEntryMetadataLocalService(
		DLFileEntryMetadataLocalService dlFileEntryMetadataLocalService) {

		_dlFileEntryMetadataLocalService = dlFileEntryMetadataLocalService;
	}

	@Reference
	public void setDLFileEntryTypeLocalService(
		DLFileEntryTypeLocalService dlFileEntryTypeLocalService) {

		_dlFileEntryTypeLocalService = dlFileEntryTypeLocalService;
	}

	@Reference
	public void setGoogleAPIConfiguration(
		GoogleAPIConfiguration googleAPIConfiguration) {

		_googleAPIConfiguration = googleAPIConfiguration;
	}

	@Reference
	public void setStorageEngine(StorageEngine storageEngine) {
		_storageEngine = storageEngine;
	}

	@Reference
	public void setUserLocalService(UserLocalService userLocalService) {
		_userLocalService = userLocalService;
	}

	private void _addGoogleDocumentTypes() throws PortalException {
		List<Company> companies = _companyLocalService.getCompanies();

		for (Company company : companies) {
			GoogleDocumentTypeCreator googleDocumentTypeCreator =
				new GoogleDocumentTypeCreator(
					company, _classNameLocalService, _ddmStructureLocalService,
					_dlFileEntryTypeLocalService, _userLocalService);

			googleDocumentTypeCreator.addDocumentType();
		}
	}

	private String _getEditURL(DLFileVersion dlFileVersion)
		throws PortalException {

		GoogleDocumentMetadataHelper googleDocumentMetadataHandler =
				new GoogleDocumentMetadataHelper(
					dlFileVersion, _dlFileEntryMetadataLocalService,
					_dlFileEntryTypeLocalService, _storageEngine);

		return googleDocumentMetadataHandler.getFieldValue(
			Constants.EDIT_URL_DDM_FIELD);
	}

	private String _getEmbedURL(DLFileVersion dlFileVersion)
		throws PortalException {

		GoogleDocumentMetadataHelper googleDocumentMetadataHandler =
				new GoogleDocumentMetadataHelper(
					dlFileVersion, _dlFileEntryMetadataLocalService,
					_dlFileEntryTypeLocalService, _storageEngine);

		return googleDocumentMetadataHandler.getFieldValue(
			Constants.EMBED_URL_DDM_FIELD);
	}

	private static final String _API_KEY_TOKEN = Pattern.quote("[$$API_KEY$$]");

	private static final String _CALLBACK_FUNCTION_TOKEN = Pattern.quote(
		"[$$CALLBACK_FUNCTION$$]");

	private static final String _CLIENT_ID_TOKEN = Pattern.quote(
		"[$$CLIENT_ID$$]");

	private static final String _EDIT_URL_JSON_FIELD = "editURL";

	private static final String _EMBED_URL_JSON_FIELD = "embedURL";

	private static final String _FILE_PICKER_HTML = "FilePicker.html";

	private static final String _ICON_JSON_FIELD = "icon";

	private static final String _ID_JSON_FIELD = "id";

	private static final String _PREVIEW_HTML = "Preview.html";

	private static final String _TITLE_JSON_FIELD = "title";

	private static final String _VIEW_URL_TOKEN = Pattern.quote(
		"[$$VIEW_URL$$]");

	private ClassNameLocalService _classNameLocalService;
	private CompanyLocalService _companyLocalService;
	private DDMStructureLocalService _ddmStructureLocalService;
	private DLFileEntryMetadataLocalService _dlFileEntryMetadataLocalService;
	private DLFileEntryTypeLocalService _dlFileEntryTypeLocalService;
	private String _filePickerHtml;
	private GoogleAPIConfiguration _googleAPIConfiguration;
	private String _previewHtml;
	private StorageEngine _storageEngine;
	private UserLocalService _userLocalService;

}