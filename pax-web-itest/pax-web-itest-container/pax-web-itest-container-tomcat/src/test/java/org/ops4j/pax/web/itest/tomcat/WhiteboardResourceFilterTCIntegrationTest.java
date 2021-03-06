/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package org.ops4j.pax.web.itest.tomcat;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;

import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.web.extender.samples.whiteboard.internal.WhiteboardServlet;
import org.ops4j.pax.web.itest.base.VersionUtil;
import org.ops4j.pax.web.itest.base.support.SimpleFilter;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Toni Menzel (tonit)
 * @since Mar 3, 2009
 */
@RunWith(PaxExam.class)
public class WhiteboardResourceFilterTCIntegrationTest extends ITestBase {

	private static final Logger LOG = LoggerFactory.getLogger(WhiteboardResourceFilterTCIntegrationTest.class);
	
	private ServiceRegistration<Servlet> service;

	@Configuration
	public Option[] configure() {
		return combine(
				configureTomcat(),
				mavenBundle().groupId("org.ops4j.pax.web.samples")
						.artifactId("whiteboard").version(VersionUtil.getProjectVersion())
						.noStart());

	}

	@Before
	public void setUp() throws Exception {
		int count = 0;
		while (!testClient.checkServer("http://127.0.0.1:8282/") && count < 100) {
			synchronized (this) {
				this.wait(100);
				count++;
			}
		}
		
		LOG.info("waiting for Server took {} ms", (count * 1000));
		
		initServletListener(null);

		Dictionary<String, String> initParams = new Hashtable<String, String>();
		initParams.put("alias", "/test-resources");
		service = bundleContext.registerService(Servlet.class,
				new WhiteboardServlet("/test-resources"), initParams);
		
		waitForServletListener();

	}

	@After
	public void tearDown() throws BundleException {
		service.unregister();

	}

	@Test
	public void testWhiteBoardFiltered() throws Exception {
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put("urlPatterns", "*");
		SimpleFilter simpleFilter = new SimpleFilter();
		ServiceRegistration<Filter> filter = bundleContext.registerService(
				Filter.class, simpleFilter, props);

		testClient.testWebPath("http://127.0.0.1:8282/test-resources",
				"Hello Whiteboard Extender");

		URL resource = simpleFilter.getResource();
		assertNotNull(resource);

		filter.unregister();

	}

}
