/*
 * Created on 25-Oct-2005 at 00:18:24.
 * 
 * Copyright (c) 2005 Robert Virkus / Enough Software
 *
 * This file is part of J2ME Polish.
 *
 * J2ME Polish is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * J2ME Polish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with J2ME Polish; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Commercial licenses are also available, please
 * refer to the accompanying LICENSE.txt or visit
 * http://www.j2mepolish.org for details.
 */
package de.enough.polish.manifest;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import de.enough.polish.Attribute;
import de.enough.polish.Device;
import de.enough.polish.Environment;

/**
 * <p>Does not create a manifest file. This is useful for DoJa applications, for example.</p>
 *
 * <p>Copyright Enough Software 2005</p>
 * <pre>
 * history
 *        25-Oct-2005 - rob creation
 * </pre>
 * @author Robert Virkus, j2mepolish@enough.de
 */
public class NoManifestCreator extends ManifestCreator {

	/**
	 * Creates a new instance
	 */
	public NoManifestCreator() {
		super();
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.manifest.ManifestCreator#createManifest(java.io.File, de.enough.polish.Attribute[], java.lang.String, de.enough.polish.Device, java.util.Locale, de.enough.polish.Environment)
	 */
	public void createManifest(File manifestFile,
			Attribute[] manifestAttributes, String encoding, Device device,
			Locale locale, Environment env) 
	throws IOException 
	{
		// this creator doesn't do anything :-)
	}

}
