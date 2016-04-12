package com.enterpriseosgi.tycho.utils

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.koco.konnektor.ndesign.ak.cardterminalservice.impl.CTService;

public class TestPlatformOverview {
    private static final Logger LOG = LoggerFactory.getLogger(TestPlatformOverviewTest.class);
    private BundleContext ctx;

    /**
     * Loggt vor jedem Test den jeweiligen Testnamen.
     */
    @Rule
    public TestRule watcher = new TestWatcher() {
        @Override
        protected void starting(final Description description) {
            LOG.debug("Starte Test: {}", description.getMethodName());
        }
    };

    @Test
    public void test() {
        this.ctx = FrameworkUtil.getBundle(CTService.class).getBundleContext();
        LOG.debug(getClass().getName());
        LOG.debug("{}", FrameworkUtil.getBundle(getClass()).getState());
        logOSGIBundles();
        logOSGIServices();
    }

    public void logOSGIBundles() {
        assertNotNull(this.ctx);

        int maxLen = 0;
        for (Bundle bundle : this.ctx.getBundles()) {
            if (bundle.getSymbolicName() != null) {
                String name = bundle.getSymbolicName() + " (" + bundle.getVersion() + ")";
                if (maxLen < name.length()) {
                    maxLen = name.length();
                }
            }
        }

        StringBuffer dashes = new StringBuffer();
        for (int i = 0; i < maxLen; i++) {
            dashes.append("-");
        }
        LOG.info("INSTALLED BUNDLES");
        LOG.info("-----------------");
        LOG.info("Level | Bundle | Bundle State");
        LOG.info("--------{}------------", dashes);
        String format = "%1$5s | %2$-" + maxLen + "s | ";
        for (Bundle bundle : this.ctx.getBundles()) {
            BundleStartLevel bsl = bundle.adapt(BundleStartLevel.class);
            String b = bundle.getSymbolicName() + " (" + bundle.getVersion() + ")";
            String msg = String.format(format, bsl.getStartLevel(), b) + getBundleStateString(bundle);
            LOG.info(msg);
        }
    }

    private String getBundleStateString(final Bundle bundle) {
        int state = bundle.getState();
        if (state == Bundle.ACTIVE) {
            return "Active";
        } else if (state == Bundle.INSTALLED) {
            return "Installed";
        } else if (state == Bundle.RESOLVED) {
            return "Resolved";
        } else if (state == Bundle.STARTING) {
            return "Starting";
        } else if (state == Bundle.STOPPING) {
            return "Stopping";
        } else {
            return "Unknown";
        }
    }

    public void logOSGIServices() {
        try {
            ServiceReference<?>[] refs = this.ctx.getAllServiceReferences(null, null);
            List<String> serviceNames = new ArrayList<String>();
            if (refs != null) {
                LOG.info("OSGi Services ({})", refs.length);
                LOG.info("-------------------------------");
                for (ServiceReference<?> r : refs) {
                    Object o = this.ctx.getService(r);
                    Class<?> c = o.getClass();
                    LOG.info("Service: {}", c.getName());
                    Type[] intfs = c.getGenericInterfaces();
                    if (intfs.length != 0) {
                        for (Type intf : intfs) {
                            LOG.info(" - interface: {}", intf);
                        }
                    }
                    serviceNames.add(o.getClass().getName());
                    this.ctx.ungetService(r);
                }

                /*
                 * LOG.info("OSGi Services ({})", refs.length); LOG.info("-------------------------------"); for (String
                 * name : serviceNames) { LOG.info("Service: {}", name); }
                 */
            }
        } catch (InvalidSyntaxException e) {
            LOG.error("logOSGIServices() exception thrown.", e);
        }
    }
}
