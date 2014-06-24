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

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portlet.documentlibrary.model.DLFileEntryMetadata;
import com.liferay.portlet.documentlibrary.model.DLFileEntryType;
import com.liferay.portlet.documentlibrary.model.DLFileVersion;
import com.liferay.portlet.documentlibrary.service.DLFileEntryMetadataLocalService;
import com.liferay.portlet.documentlibrary.service.DLFileEntryTypeLocalService;
import com.liferay.portlet.dynamicdatamapping.model.DDMStructure;
import com.liferay.portlet.dynamicdatamapping.storage.Field;
import com.liferay.portlet.dynamicdatamapping.storage.Fields;
import com.liferay.portlet.dynamicdatamapping.storage.StorageEngine;
import com.liferay.portlet.dynamicdatamapping.util.DDMImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Iván Zaera
 */
public class GoogleDocumentMetadataHelper {

	public GoogleDocumentMetadataHelper(
		DLFileVersion dlFileVersion,
		DLFileEntryMetadataLocalService dlFileEntryMetadataLocalService,
		DLFileEntryTypeLocalService dlFileEntryTypeLocalService,
		StorageEngine storageEngine) {

		_dlFileVersion = dlFileVersion;
		_dlFileEntryMetadataLocalService = dlFileEntryMetadataLocalService;
		_dlFileEntryTypeLocalService = dlFileEntryTypeLocalService;
		_storageEngine = storageEngine;
	}

	public String getFieldValue(String fieldName) throws PortalException {
		Fields fields = _getDDMFields();

		Field field = fields.get(fieldName);

		return (String)field.getValue();
	}

	public void updateMetadata(
			String id, String title, String icon, String embedURL,
			String editURL)
		throws PortalException {

		Fields fields = _getDDMFields();
		DDMStructure ddmStructure = _getGoogleDDMStructure();

		fields.put(_createField(ddmStructure, Constants.ID_DDM_FIELD, id));

		fields.put(
			_createField(ddmStructure, Constants.TITLE_DDM_FIELD, title));

		fields.put(_createField(ddmStructure, Constants.ICON_DDM_FIELD, icon));

		fields.put(
			_createField(
				ddmStructure, Constants.EMBED_URL_DDM_FIELD, embedURL));

		fields.put(
			_createField(ddmStructure, Constants.EDIT_URL_DDM_FIELD, editURL));

		fields.put(
			_createField(
				ddmStructure, DDMImpl.FIELDS_DISPLAY_NAME,
				_getFieldsDisplayNameValue(fields)));

		_dlFileEntryMetadataLocalService.updateFileEntryMetadata(
			_dlFileVersion.getFileEntryTypeId(),
			_dlFileVersion.getFileEntryId(), _dlFileVersion.getFileVersionId(),
			_createFieldsMap(ddmStructure, fields), _createServiceContext());
	}

	private Field _createField(
		DDMStructure ddmStructure, String name, String value) {

		if (value == null) {
			value = "";
		}

		Field field = new Field(name, value);

		field.setDDMStructureId(ddmStructure.getStructureId());
		field.setDefaultLocale(LocaleUtil.getDefault());

		return field;
	}

	private Map<String, Fields> _createFieldsMap(
		DDMStructure ddmStructure, Fields fields) {

		Map<String, Fields> fieldsMap = new HashMap<String, Fields>();

		fieldsMap.put(ddmStructure.getStructureKey(), fields);
		return fieldsMap;
	}

	private ServiceContext _createServiceContext() {
		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setScopeGroupId(_dlFileVersion.getGroupId());
		serviceContext.setUserId(_dlFileVersion.getUserId());

		return serviceContext;
	}

	private Fields _getDDMFields() throws PortalException {
		DDMStructure ddmStructure = _getGoogleDDMStructure();

		DLFileEntryMetadata dlFileEntryMetadata =
			_dlFileEntryMetadataLocalService.getFileEntryMetadata(
				ddmStructure.getStructureId(),
				_dlFileVersion.getFileVersionId());

		return _storageEngine.getFields(dlFileEntryMetadata.getDDMStorageId());
	}

	private String _getFieldsDisplayNameValue(Fields fields) {
		List<String> fieldsDisplayValues = new ArrayList<String>();

		for (Field field : fields) {
			String fieldDisplayValue =
				field.getName() + DDMImpl.INSTANCE_SEPARATOR +
					StringUtil.randomString();

			fieldsDisplayValues.add(fieldDisplayValue);
		}

		return StringUtil.merge(fieldsDisplayValues);
	}

	private DDMStructure _getGoogleDDMStructure() {
		DLFileEntryType dlFileEntryType =
			_dlFileEntryTypeLocalService.fetchFileEntryType(
				_dlFileVersion.getFileEntryTypeId());

		if (dlFileEntryType != null) {
			List<DDMStructure> ddmStructures =
				dlFileEntryType.getDDMStructures();

			for (DDMStructure ddmStructure : ddmStructures) {
				String structureKey = ddmStructure.getStructureKey();

				if (structureKey.equals(
						Constants.GOOGLE_DOCUMENT_STRUCTURE_KEY)) {

					return ddmStructure;
				}
			}
		}

		throw new IllegalArgumentException(
			"The given file entry has no associated Google Document " +
				"Metadata set");
	}

	private DLFileEntryMetadataLocalService _dlFileEntryMetadataLocalService;
	private DLFileEntryTypeLocalService _dlFileEntryTypeLocalService;
	private DLFileVersion _dlFileVersion;
	private StorageEngine _storageEngine;

}