package com.sun.faban.harness.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

/**
 * Business logic for deploy functionality in fabancli
 * @author simonedavico (simonedavico@gmail.com)
 *
 */
public class DeployLogic {

	private String harness;
	private String user;
	private String password;
	private File jarFile;
	private boolean clearConfig;
	
	private static final String defaultHarness = "http://localhost:9980/";
	private static final String defaultUser = "deployer";
	private static final String defaultPassword = "adminadmin";
	
	
	public DeployLogic(final File jarFile) {
		this(defaultUser, defaultPassword, defaultHarness, jarFile, true);
	}
	
	public DeployLogic(final String user, final String password, 
			   final String harness, final File jarFile) {
		this(user, password, harness, jarFile, true);
	}
	
	public DeployLogic(final String user, final String password, 
					   final String harness, final File jarFile,
					   final boolean clearConfig) {
		this.user = user != null ? user : defaultUser;
		this.password = password != null ? password : defaultPassword;
		this.harness = harness != null ? harness : defaultHarness;
		this.jarFile = jarFile;
		this.clearConfig = clearConfig;
	}
	
	/**
	 * Sets the URI of the harness
	 * @param target
	 */
	public void setTarget(final String target) {
		this.harness = target;
	}

	/**
	 * Sets the user credential
	 * @param user
	 */
	public void setUser(final String user) {
		this.user = user;
	}

	/**
	 * Sets the password credential
	 * @param password
	 */
	public void setPassword(final String password) {
		this.password = password;
	}

	/**
	 * Sets the benchmark to deploy on the harness
	 * @param jarFile
	 */
	public void setBenchmark(final File jarFile) {
		this.jarFile = jarFile;
	}

	/**
	 * Whether to clear the configuration files or not
	 * @param clearConfig
	 */
	public void clearConfig(final boolean clearConfig) {
		this.clearConfig = clearConfig;
	}

	private Part[] prepareRequestParts() throws FileNotFoundException {

		ArrayList<Part> params = new ArrayList<Part>(4);

		if (user != null)
			params.add(new StringPart("user", user));
		if (password != null)
			params.add(new StringPart("password", password));
		if (clearConfig)
			params.add(new StringPart("clearconfig", "true"));
		params.add(new FilePart("jarfile", jarFile));
		Part[] parts = new Part[params.size()];

		return parts = params.toArray(parts);
	}

	/**
	 * Actual deployment logic
	 */
	public void deploy() throws DeployException {
		
//		System.err.println("[DeployStatus] User " + user);
//		System.err.println("[DeployStatus] Password " + password);
//		System.err.println("[DeployStatus] Harness " + harness);
//		System.err.println("[DeployStatus] Jar " + jarFile.getAbsolutePath());
		
		if (jarFile == null)
			throw new DeployException(
					"jar attribute missing for target deploy.");
		if (!jarFile.isFile())
			throw new DeployException("jar file not found.");
		String jarName = jarFile.getName();
		if (!jarName.endsWith(".jar"))
			throw new DeployException(
					"Jar file name must end with \".jar\"");
		jarName = jarName.substring(0, jarName.length() - 4);
		if (jarName.indexOf('.') > -1) {
			throw new DeployException("Jar file name must not have any " +
					"dots except ending with \".jar\"");
		}

		try{    	

			Part[] parts = prepareRequestParts();
			PostMethod post = new PostMethod(harness + "/deploy");
			
			// Ensure text/plain is the only accept header.
			post.removeRequestHeader("Accept");
			post.setRequestHeader("Accept", "text/plain");
			post.setRequestEntity(
					new MultipartRequestEntity(parts, post.getParams()));

			// Execute the multi-part post method.
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().
			setConnectionTimeout(5000);
			int status = client.executeMethod(post);
			StringBuilder b = new StringBuilder();
			InputStream respBody  = post.getResponseBodyAsStream();
			byte[] readBuffer = new byte[8192];
			for (;;) {
				int size = respBody.read(readBuffer);
				if (size == -1)
					break;
				b.append(new String(readBuffer, 0, size));
			}

//			String response = b.toString();
//			System.err.println(response);

			// Check status.
			if (status == HttpStatus.SC_CONFLICT) {
				throw new DeployException("Benchmark to deploy is currently " +
						"run or queued to be run. Please clear run queue " +
						"of this benchmark before deployment");
			} else if (status == HttpStatus.SC_NOT_ACCEPTABLE) {
				throw new DeployException("Benchmark deploy name or deploy " +
						"file invalid. Deploy file may contain errors. Name " +
						"must have no '.' and file must have the " +
						"'.jar' extensions.");
			} else if (status != HttpStatus.SC_CREATED) {
				throw new DeployException("Faban responded with status code " +
						status + ". Status code 201 (SC_CREATED) expected.");
			}

		} catch(HttpException e) {
			throw new DeployException(e);
		} catch (FileNotFoundException e) {
			throw new DeployException(e);
		} catch (IOException e) {
			throw new DeployException(e);
		}
	}

}
