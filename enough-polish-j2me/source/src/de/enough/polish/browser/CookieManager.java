//#condition polish.usePolishGui

/*
 * Created on 20-March-2012 at 19:20:28.
 * 
 * Copyright (c) 2012 Robert Virkus / Enough Software
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
package de.enough.polish.browser;

import de.enough.polish.util.ArrayList;

public class CookieManager {
	
	private final ArrayList cookiesList;
	
	public CookieManager() {
		this.cookiesList = new ArrayList();
	}

	public void addCookie( String setCookieDefinition) {
		addCookie( new Cookie(setCookieDefinition) );
	}

	public void addCookie( Cookie cookie) {
		int index = this.cookiesList.indexOf(cookie);
		if (index != -1) {
			if (cookie.isExpired()) {
				this.cookiesList.remove(index);
			} else {
				this.cookiesList.set(index, cookie);
			}
		} else if (!cookie.isExpired()) {
			this.cookiesList.add(cookie);
		}
	}
	
	public String getCookiesForDomain(String domain) {
		Object[] cookies = this.cookiesList.getInternalArray();
		StringBuffer buffer = null;
		for (int i = 0; i < cookies.length; i++) {
			Cookie cookie = (Cookie) cookies[i];
			if (cookie == null) {
				break;
			}
			if (cookie.matchesDomain(domain)) {
				if (buffer == null) {
					buffer = new StringBuffer(cookie.getNameValuePair());
				} else {
					buffer.append("; ").append(cookie.getNameValuePair());
				}
			}
		}
		if (buffer == null) {
			return null;
		}
		return buffer.toString();
	}
}
