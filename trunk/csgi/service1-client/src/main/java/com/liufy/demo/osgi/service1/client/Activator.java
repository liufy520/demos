package com.liufy.demo.osgi.service1.client;


import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.liufy.demo.osgi.service1.Hello;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;

		ServiceReference ref = context.getServiceReference(Hello.class.getName());
		if (ref != null) {
			Hello hello = null;
			try {
				hello = (Hello) context.getService(ref);
				if (hello != null)
					System.out.println(hello.getHelloString("liufy"));
				else
					System.out.println("Service:Hello---object null");
			} catch (RuntimeException e) {
				e.printStackTrace();
			} finally {
				context.ungetService(ref);
				hello = null;
			}
		} else {
			System.out.println("Service:Hello---not exists");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
