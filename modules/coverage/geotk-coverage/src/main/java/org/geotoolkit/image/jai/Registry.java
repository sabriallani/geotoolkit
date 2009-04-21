/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2005-2009, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.image.jai;

import java.util.List;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.awt.image.renderable.RenderedImageFactory;

import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;
import javax.media.jai.registry.RIFRegistry;
import javax.media.jai.registry.RenderedRegistryMode;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ImageReaderWriterSpi;

import org.geotoolkit.lang.Static;
import org.geotoolkit.lang.Configuration;
import org.geotoolkit.resources.Loggings;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.internal.image.jai.*;


/**
 * A set of static methods for managing JAI's {@linkplain OperationRegistry operation registry}.
 * Also provides convenience methods for setting the preferred order between standard and JAI
 * {@link javax.imageio.ImageReader}/{@link javax.imageio.ImageWriter}.
 *
 * @author Martin Desruisseaux (IRD)
 * @version 3.0
 *
 * @since 2.2
 * @module
 */
@Static
public final class Registry {
    /**
     * Do not allows instantiation of this class.
     */
    private Registry() {
    }

    /**
     * Unconditionnaly registers all JAI operations provided in the {@link org.geotoolkit.image.jai}
     * package. This method usually don't need to be invoked, since JAI should parse automatically
     * the {@code META-INF/registryFile.jai} file at startup time. However, this default mechanism
     * may fail when the geotoolkit JAR file is unreachable from the JAI class loader, in which case
     * the {@link org.geotoolkit.coverage.processing} package will invoke this method as a fallback.
     * <p>
     * Note to module maintainer: if this method is updated, remember to update the
     * {@code META-INF/registryFile.jai} file accordingly.
     *
     * @param  registry The operation registry to register with.
     * @return {@code true} if all registrations have been successful.
     */
    @Configuration
    public static boolean registerGeotoolkitServices(final OperationRegistry registry) {
        LogRecord record;
        String op = "org.geotoolkit";
        try {
            op = Combine.OPERATION_NAME;
            registry.registerDescriptor(new CombineDescriptor());
            RIFRegistry.register(registry, op, "org.geotoolkit", new CombineCRIF());

            op = Hysteresis.OPERATION_NAME;
            registry.registerDescriptor(new HysteresisDescriptor());
            RIFRegistry.register(registry, op, "org.geotoolkit", new HysteresisCRIF());

            op = NodataFilter.OPERATION_NAME;
            registry.registerDescriptor(new NodataFilterDescriptor());
            RIFRegistry.register(registry, op, "org.geotoolkit", new NodataFilterCRIF());

            op = SilhouetteMask.OPERATION_NAME;
            registry.registerDescriptor(new SilhouetteMaskDescriptor());
            RIFRegistry.register(registry, op, "org.geotoolkit", new SilhouetteMaskCRIF());

            record  = Loggings.format(Level.CONFIG, Loggings.Keys.REGISTERED_JAI_OPERATIONS);
            op = null;
        } catch (IllegalArgumentException exception) {
            /*
             * Logs a message with the WARNING level, because DefaultProcessing class initialization
             * is likely to fails (since it tries to load operations declared in META-INF/services,
             * and some of them depend on JAI operations).
             */
            record = Loggings.getResources(null).getLogRecord(Level.WARNING,
                    Loggings.Keys.CANT_REGISTER_JAI_OPERATION_$1, op);
            record.setThrown(exception);
        }
        log("registerGeotoolkitServices", record);
        return op == null;
    }

    /**
     * Allows or disallows native acceleration for the specified operation on the given JAI instance.
     * By default, JAI uses hardware accelerated methods when available. For example, it make use of
     * MMX instructions on Intel processors. Unfortunatly, some native method crash the Java Virtual
     * Machine under some circonstances. For example on JAI 1.1.2, the {@code "Affine"} operation on
     * an image with float data type, bilinear interpolation and an {@link javax.media.jai.ImageLayout}
     * rendering hint cause an exception in medialib native code. Disabling the native acceleration
     * (i.e using the pure Java version) is a convenient workaround until Sun fix the bug.
     * <p>
     * <strong>Implementation note:</strong> the current implementation assumes that factories for
     * native implementations are declared in the {@code com.sun.media.jai.mlib} package, while
     * factories for pure java implementations are declared in the {@code com.sun.media.jai.opimage}
     * package. It work for Sun's 1.1.2 implementation, but may change in future versions. If this
     * method doesn't recognize the package, it does nothing.
     *
     * @param operation The operation name (e.g. {@code "Affine"}).
     * @param allowed {@code false} to disallow native acceleration.
     * @param jai The instance of {@link JAI} we are going to work on. This argument can be
     *        omitted for the {@linkplain JAI#getDefaultInstance default JAI instance}.
     *
     * @see <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4906854">JAI bug report 4906854</a>
     *
     * @since 2.5
     */
    @Configuration
    public synchronized static void setNativeAccelerationAllowed(final String operation,
            final boolean allowed, final JAI jai)
    {
        final String product = "com.sun.media.jai";
        final OperationRegistry registry = jai.getOperationRegistry();

        @SuppressWarnings("unchecked")
        final List<RenderedImageFactory> factories = registry.getOrderedFactoryList(
                RenderedRegistryMode.MODE_NAME, operation, product);
        if (factories != null) {
            RenderedImageFactory   javaFactory = null;
            RenderedImageFactory nativeFactory = null;
            Boolean               currentState = null;
            for (final RenderedImageFactory factory : factories) {
                final String pack = factory.getClass().getPackage().getName();
                if (pack.equals("com.sun.media.jai.mlib")) {
                    nativeFactory = factory;
                    if (javaFactory != null) {
                        currentState = Boolean.FALSE;
                    }
                }
                if (pack.equals("com.sun.media.jai.opimage")) {
                    javaFactory = factory;
                    if (nativeFactory != null) {
                        currentState = Boolean.TRUE;
                    }
                }
            }
            if (currentState != null && currentState.booleanValue() != allowed) {
                RIFRegistry.unsetPreference(registry, operation, product,
                        allowed ? javaFactory : nativeFactory,
                        allowed ? nativeFactory : javaFactory);
                RIFRegistry.setPreference(registry, operation, product,
                        allowed ? nativeFactory : javaFactory,
                        allowed ? javaFactory : nativeFactory);
                final LogRecord record = Loggings.format(Level.CONFIG,
                        Loggings.Keys.NATIVE_ACCELERATION_STATE_$2,
                        operation, Integer.valueOf(allowed ? 1 : 0));
                log("setNativeAccelerationAllowed", record);
            }
        }
    }

    /**
     * Allows or disallows native acceleration for the specified operation on the
     * {@linkplain JAI#getDefaultInstance default JAI instance}. This method is
     * a shortcut for <code>{@linkplain #setNativeAccelerationAllowed(String,boolean,JAI)
     * setNativeAccelerationAllowed}(operation, allowed, JAI.getDefaultInstance())</code>.
     *
     * @param operation The operation name (e.g. {@code "Affine"}).
     * @param allowed {@code false} to disallow native acceleration.
     *
     * @see #setNativeAccelerationAllowed(String, boolean, JAI)
     */
    @Configuration
    public static void setNativeAccelerationAllowed(final String operation, final boolean allowed) {
        setNativeAccelerationAllowed(operation, allowed, JAI.getDefaultInstance());
    }

    /**
     * Allows or disallows native acceleration for the specified image format. By default, the
     * image I/O extension for JAI provides native acceleration for PNG and JPEG. Unfortunatly,
     * those native codec has bug in their 1.0 version. Invoking this method will force the use
     * of standard codec provided in J2SE 1.4.
     *
     * {@note The current implementation assumes that JAI codec class name start with
     *        <code>"com.sun.media"</code> package name. It works for JAI Image I/O 1.1
     *        implementation, but may change in future versions. If this method doesn't
     *        recognize the class name, then it does nothing.}
     *
     * @param <T> The category ({@code ImageReaderSpi} or {@code ImageWriterSpi}).
     * @param format The format name (e.g. {@code "png"}).
     * @param category {@code ImageReaderSpi.class} to set the reader, or
     *        {@code ImageWriterSpi.class} to set the writer.
     * @param allowed {@code false} to disallow native acceleration.
     *
     * @since 3.0
     */
    @Configuration
    public static synchronized <T extends ImageReaderWriterSpi> void setNativeCodecAllowed(
            final String format, final Class<T> category, final boolean allowed)
    {
        T standard = null;
        T codeclib = null;
        final IIORegistry registry = IIORegistry.getDefaultInstance();
        for (final Iterator<T> it = registry.getServiceProviders(category, false); it.hasNext();) {
            final T provider = it.next();
            final String[] formats = provider.getFormatNames();
            for (int i=0; i<formats.length; i++) {
                if (formats[i].equalsIgnoreCase(format)) {
                    if (provider.getClass().getName().startsWith("com.sun.media.")) {
                        codeclib = provider;
                    } else {
                        standard = provider;
                    }
                    break;
                }
            }
        }
        if (standard!=null && codeclib!=null) {
            final boolean changed;
            if (allowed) {
                changed = registry.setOrdering(category, codeclib, standard);
            } else {
                changed = registry.setOrdering(category, standard, codeclib);
            }
            if (changed) {
                final LogRecord record = Loggings.format(Level.CONFIG,
                        Loggings.Keys.NATIVE_CODEC_STATE_$3,
                        format, Integer.valueOf(allowed ? 1 : 0),
                        Integer.valueOf(ImageWriterSpi.class.isAssignableFrom(category) ? 1 : 0));
                log("setNativeCodecAllowed", record);
            }
        }
    }

    /**
     * Sets the default preferred order for JAI and Java standard codec. This method invokes
     * {@link #setNativeCodecAllowed setNativeCodecAllowed} for some well-known formats.
     * <p>
     * <strong>TIP:</strong> Experience suggests that on some platforms, new codecs appear
     * magically after an AWT window has been created (e.g. "Standard TIFF image reader").
     * If this method is to be invoked for a graphical application, it is recommanded to
     * invoke this method <strong>after</strong> the {@linkplain java.awt.Window} has been
     * created but before it is made visible.
     *
     * @since 3.0
     */
    @Configuration
    public static synchronized void setDefaultCodecPreferences() {
        setNativeCodecAllowed("PNG",   ImageReaderSpi.class, false);
        setNativeCodecAllowed("PNG",   ImageWriterSpi.class, false);
        setNativeCodecAllowed("BMP",   ImageReaderSpi.class, false);
        setNativeCodecAllowed("BMP",   ImageWriterSpi.class, false);
        setNativeCodecAllowed("WBMP",  ImageReaderSpi.class, false);
        setNativeCodecAllowed("WBMP",  ImageWriterSpi.class, false);
        setNativeCodecAllowed("TIFF",  ImageReaderSpi.class, true);
        setNativeCodecAllowed("TIFF",  ImageWriterSpi.class, true);
    }

    /**
     * Logs the specified record.
     */
    private static void log(final String method, final LogRecord record) {
        record.setSourceClassName(Registry.class.getName());
        record.setSourceMethodName(method);
        final Logger logger = Logging.getLogger(Registry.class);
        record.setLoggerName(logger.getName());
        logger.log(record);
    }
}
