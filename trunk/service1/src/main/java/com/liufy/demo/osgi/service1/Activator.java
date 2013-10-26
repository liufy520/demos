package com.liufy.demo.osgi.service1;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.liufy.demo.osgi.service1.impl.HelloImpl;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private List<ServiceRegistration> registrations = new ArrayList<ServiceRegistration>();

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
		registrations.add(context.registerService(Hello.class.getName(),
				new HelloImpl(), null));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		for (ServiceRegistration registration : registrations) {
	        System.out.println("unregistering: "+ registration);
	        registration.unregister();
	    }
		Activator.context = null;
	}

}
