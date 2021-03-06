/*
 * Created on 28-Apr-2005 at 13:31:31.
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
package de.enough.polish.finalize;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import de.enough.polish.BuildException;

import de.enough.polish.Device;
import de.enough.polish.Environment;
import de.enough.polish.ant.android.ArgumentHelper;
import de.enough.polish.ant.build.SignSetting;
import de.enough.polish.util.FileUtil;
import de.enough.polish.util.OsUtil;
import de.enough.polish.util.ProcessUtil;

/**
 * <p>Signs MIDlets. </p>
 * <p>You can use the sign task using the &lt;sign&gt; element within the &lt;build&gt; section: 
 * <pre>
 * &lt;sign
 * 	keystore=&quot;midlets.ks=&quot;
 * 	key==&quot;MyKey=&quot;
 * 	password=&quot;${pw}=&quot;
 * /&gt;
 * </pre>
 * </p>
 *
 * <p>Copyright Enough Software 2005-2011</p>
 * @author Robert Virkus, j2mepolish@enough.de
 */
public class SignFinalizer extends Finalizer {

	/**
	 * Creates a new signing finalizer.
	 */
	public SignFinalizer() {
		super();
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.finalize.Finalizer#finalize(java.io.File, java.io.File, de.enough.polish.Device, java.util.Locale, de.enough.polish.Environment)
	 */
	public void finalize(File jadFile, File jarFile, Device device,
			Locale locale, Environment env) {
		
		SignSetting setting = (SignSetting) getExtensionSetting();

		boolean isAndroidDevice = device.getFeatures().get("polish.android") != null;
		String alias = setting.getKey();
		
		if(isAndroidDevice) {
			
			File jarSigner;
			String path;
			
			if(OsUtil.isRunningWindows()) {
				path = "${java.home}/bin/jarsigner.exe";
			} else {
				path = "${java.home}/bin/jarsigner";
			}
			jarSigner = env.resolveFile(path);
			if( ! jarSigner.exists()) {
				String incorrectPath = jarSigner.getAbsolutePath();
				if(OsUtil.isRunningWindows()) {
					path = "${java.home}/../bin/jarsigner.exe";
				} else {
					path = "${java.home}/../bin/jarsigner";
				}
				jarSigner = env.resolveFile(path);

				if( ! jarSigner.exists()) {
					throw new BuildException("Could not find jarsigner tool at path '"+incorrectPath+"' or '"+jarSigner.getAbsolutePath()+"'. Make sure the 'java.home' variable is set in the build.xml file to the directory where the JDK is located.");
				}
			}
			
			File keystore = setting.getKeystore();
			if(keystore == null || ! keystore.exists()) {
				throw new BuildException("No keystore given.");
			}
			String keystorePath = keystore.getAbsolutePath();
			
			ArrayList<String> parametersList = new ArrayList<String>();
			parametersList.add(jarSigner.getAbsolutePath());
			parametersList.add("-verbose");
			parametersList.add("-keystore");
			parametersList.add(keystorePath);
			parametersList.add( "-storepass" );
			parametersList.add( setting.getStorePass() );
			parametersList.add( "-keypass" );
			parametersList.add( setting.getKeyPass() );
			String apkFile = ArgumentHelper.getPackage("apk", env);
			parametersList.add(apkFile);
			parametersList.add(alias);
			int result;
			try {
				System.out.println( "Adding signature for " + device.getIdentifier() );
				result = ProcessUtil.exec( parametersList, "sign: ", true );
			} catch (IOException e) {
				e.printStackTrace();
				throw new BuildException("Unable to sign application: unable to execute jarsigner: " + e.toString() );
			}
			if (result != 0) {
				System.out.println("Signing returned " + result + ", paramters were: ");
				System.out.println(ProcessUtil.toString(parametersList));
				throw new BuildException("Unable to sign application: unable to execute jarsigner. Returncode was '" + result + "'." );
			}
			return;
		} // end of Android specific code
		 
		// locate the JadTool:
		File jadTool = env.resolveFile("${wtk.home}/bin/JadTool.jar");
		if (! jadTool.exists() ) {
			jadTool = env.resolveFile("${polish.home}/bin/JadTool.jar");
			if (!jadTool.exists()) {
				throw new BuildException("Unable to sign application: neither ${wtk.home}/bin/JadTool.jar nor ${polish.home}/bin/JadTool.jar was found.\n${wtk.home}=" + env.getVariable("wtk.home") + ", ${polish.home}=" + env.getVariable("polish.home") );
			}
		}
		// copy JAD to JAD.original:
		File originalJadFile = new File(jadFile.getAbsolutePath() + ".original");
		try {
			FileUtil.copy(jadFile, originalJadFile);
		} catch (IOException e) {
			e.printStackTrace();
			throw new BuildException("Unablte to sign application: cannot copy " + jadFile.getAbsolutePath() + ": " + e);
		}
		ArrayList<String> parametersList = new ArrayList<String>();
		// sign the JAR file:
		parametersList.add( "java" );
		parametersList.add( "-jar" );
		parametersList.add( jadTool.getAbsolutePath() );
		parametersList.add( "-addjarsig" );
		parametersList.add( "-storepass" );
		parametersList.add( setting.getStorePass() );
		parametersList.add( "-keypass" );
		parametersList.add( setting.getKeyPass() );
		parametersList.add( "-alias" );
		parametersList.add( alias );
		if (setting.getKeystore() != null) {
			parametersList.add( "-keystore" );
			parametersList.add( setting.getKeystore().getAbsolutePath() );
		}
		parametersList.add( "-inputjad" );
		parametersList.add( originalJadFile.getAbsolutePath() );
		parametersList.add( "-outputjad" );
		parametersList.add( jadFile.getAbsolutePath() );
		parametersList.add( "-jarfile" );
		parametersList.add( jarFile.getAbsolutePath() );
		System.out.println( "Adding signature for " + device.getIdentifier() );
		try {
			int result = ProcessUtil.exec( parametersList, "sign: ", true );
			if (result != 0) {
				System.out.println("Signing returned " + result + ", parameters were: ");
				System.out.println(ProcessUtil.toString(parametersList));
				throw new BuildException("Unable to sign application: unable to execute JadTool - JadTool returned [" + result + "]." );
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new BuildException("Unable to sign application: unable to execute JadTool: " + e.toString() );
		}
		// add the certificate chain:
		if (this.extensionSetting != null && !((SignSetting)this.extensionSetting).isSkipCertificate()) { 
			parametersList.clear();
			parametersList.add( "java" );
			parametersList.add( "-jar" );
			parametersList.add( jadTool.getAbsolutePath() );
			parametersList.add( "-addcert" );
			parametersList.add( "-alias" );
			parametersList.add( alias );
			if (setting.getKeystore() != null) {
				parametersList.add( "-keystore" );
				parametersList.add( setting.getKeystore().getAbsolutePath() );
			}
			parametersList.add( "-inputjad" );
			parametersList.add( jadFile.getAbsolutePath() );
			parametersList.add( "-outputjad" );
			parametersList.add( jadFile.getAbsolutePath() );
			System.out.println( "Adding certificate for " + device.getIdentifier() );
			try {
				int result = ProcessUtil.exec( parametersList, "add certificate: ", true );
				if (result != 0) {
					System.out.println("JadTool arguments:");
					System.out.println(ProcessUtil.toString(parametersList));
					throw new BuildException("Unable to sign application: unable to execute JadTool - JadTool returned [" + result + "]." );
				}
			} catch (IOException e) {
				System.out.println("JadTool arguments:");
				System.out.println(ProcessUtil.toString(parametersList));
				e.printStackTrace();
				throw new BuildException("Unable to sign application: unable to execute JadTool: " + e.toString() );
			}
		}
	}
}
