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

package com.liferay.googledriveconnector;

import com.liferay.googledriveconnector.util.OSGiUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portlet.documentlibrary.connector.capabilities.BaseAddDocumentsCapability;
import com.liferay.portlet.documentlibrary.connector.capabilities.DocumentType;

import java.util.regex.Pattern;

/**
 * @author Iván Zaera
 */
public class GoogleDriveAddDocumentsCapability
	extends BaseAddDocumentsCapability {

	public static final DocumentType GOOGLE_DOCUMENT_TYPE =
		new DocumentType(
			"2B97158E-1B38-49FA-BA0B-D0CA2EA34F97", "Google Document",
			"icon-file");

	public GoogleDriveAddDocumentsCapability() {
		addDocumentType(GOOGLE_DOCUMENT_TYPE);
	}

	@Override
	protected String doGetFilePickerHTML(
		DocumentType documentType, String callbackFunction) {

		String html = OSGiUtil.get(
			getClass(), "com/liferay/googledriveconnector/FilePicker.html");

		return html.replaceAll(
			Pattern.quote("[$$CALLBACK_FUNCTION$$]"), callbackFunction);
	}

	@Override
	protected void doProcessRequest(DocumentType documentType, String json) {
		Object jsonObject = JSONFactoryUtil.deserialize(json);

		System.out.println("=======> " + documentType+"\n"+json);
	}

}