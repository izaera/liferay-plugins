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

import com.liferay.portal.kernel.util.StreamUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portlet.documentlibrary.connector.capabilities.BaseAddDocumentsCapability;
import com.liferay.portlet.documentlibrary.connector.capabilities.DocumentType;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.regex.Pattern;

import org.osgi.framework.FrameworkUtil;

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

		InputStream is = null;

		try {
			URL entry = FrameworkUtil.getBundle(getClass()).getEntry(
				"com/liferay/googledriveconnector/FilePicker.html");

			is = entry.openStream();

			String html = StringUtil.read(is);

			return html.replaceAll(
				Pattern.quote("[$$CALLBACK_FUNCTION$$]"), callbackFunction);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			StreamUtil.cleanUp(is);
		}
	}

	@Override
	protected void doProcessRequest(DocumentType documentType, String json) {
		System.out.println("=======> " + documentType+"\n"+json);
	}

}