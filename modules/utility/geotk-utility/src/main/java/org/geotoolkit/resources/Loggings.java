/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2001-2009, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009, Geomatys
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
package org.geotoolkit.resources;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.logging.LogRecord;
import java.util.logging.Level;


/**
 * Locale-dependent resources for logging messages.
 *
 * @author Martin Desruisseaux (IRD)
 * @version 3.00
 *
 * @since 2.2
 * @module
 */
public final class Loggings extends IndexedResourceBundle {
    /**
     * Resource keys. This class is used when compiling sources, but no dependencies to
     * {@code Keys} should appear in any resulting class files. Since the Java compiler
     * inlines final integer values, using long identifiers will not bloat the constant
     * pools of compiled classes.
     *
     * @author Martin Desruisseaux (IRD)
     * @version 3.00
     *
     * @since 2.2
     */
    public static final class Keys {
        private Keys() {
        }

        /**
         * Grid geometry has been adjusted for coverage "{0}".
         */
        public static final int ADJUSTED_GRID_GEOMETRY_$1 = 0;

        /**
         * Ambiguity between inverse flattening and semi minor axis length. Using inverse flattening.
         */
        public static final int AMBIGUOUS_ELLIPSOID = 1;

        /**
         * {3,choice,0#Apply|Reuse} operation "{1}" on coverage "{0}" with interpolation "{2}".
         */
        public static final int APPLIED_OPERATION_$4 = 2;

        /**
         * Resampled coverage "{0}" from coordinate system "{1}" (for an image of size {2}×{3}) to
         * coordinate system "{4}" (image size {5}×{6}). JAI operation is "{7}" with "{9}"
         * interpolation on {8,choice,0#packed|1#geophysics} pixels values. Background value is ({10}).
         */
        public static final int APPLIED_RESAMPLE_$11 = 3;

        /**
         * Converted "{0}" from "{1}" to "{2}" units. We assume that this is the expected units for
         * computation purpose.
         */
        public static final int APPLIED_UNIT_CONVERSION_$3 = 4;

        /**
         * Failed to bind a "{0}" entry.
         */
        public static final int CANT_BIND_DATASOURCE_$1 = 5;

        /**
         * Failed to create a coordinate operation from "{0}" authority factory.
         */
        public static final int CANT_CREATE_COORDINATE_OPERATION_$1 = 6;

        /**
         * Failed to create an object for code "{0}". This entry will be ignored.
         */
        public static final int CANT_CREATE_OBJECT_FROM_CODE_$1 = 7;

        /**
         * Failed to dispose the backing store after timeout.
         */
        public static final int CANT_DISPOSE_BACKING_STORE = 8;

        /**
         * Can't load a service for category "{0}". Cause is "{1}".
         */
        public static final int CANT_LOAD_SERVICE_$2 = 9;

        /**
         * Can't read "{0}".
         */
        public static final int CANT_READ_FILE_$1 = 10;

        /**
         * Can't register JAI operation "{0}". Some grid coverage operations may not work.
         */
        public static final int CANT_REGISTER_JAI_OPERATION_$1 = 11;

        /**
         * Can't roll longitude for this {0} projection.
         */
        public static final int CANT_ROLL_LONGITUDE_$1 = 12;

        /**
         * Changed the renderer coordinate system. Cause is:
         */
        public static final int CHANGED_COORDINATE_REFERENCE_SYSTEM = 13;

        /**
         * Closed the EPSG database connection.
         */
        public static final int CLOSED_EPSG_DATABASE = 14;

        /**
         * Connected to EPSG database "{0}" on "{1}".
         */
        public static final int CONNECTED_EPSG_DATABASE_$2 = 15;

        /**
         * Created coordinate operation "{0}" for source CRS "{1}" and target CRS "{2}".
         */
        public static final int CREATED_COORDINATE_OPERATION_$3 = 16;

        /**
         * Created a "{0}" entry in the naming system.
         */
        public static final int CREATED_DATASOURCE_ENTRY_$1 = 17;

        /**
         * Created serializable image for coverage "{0}" using the "{1}" codec.
         */
        public static final int CREATED_SERIALIZABLE_IMAGE_$2 = 18;

        /**
         * Creating cached EPSG database version {0}. This operation may take a few minutes...
         */
        public static final int CREATING_CACHED_EPSG_DATABASE_$1 = 19;

        /**
         * Deferred painting for tile ({0},{1}).
         */
        public static final int DEFERRED_TILE_PAINTING_$2 = 20;

        /**
         * File "{0}" contains values that duplicate previously stored values.
         */
        public static final int DUPLICATED_CONTENT_IN_FILE_$1 = 21;

        /**
         * Excessive memory usage.
         */
        public static final int EXCESSIVE_MEMORY_USAGE = 22;

        /**
         * Tile cache capacity exceed maximum heap size ({0} Mb).
         */
        public static final int EXCESSIVE_TILE_CACHE_$1 = 23;

        /**
         * Factory implementations for category {0}:
         */
        public static final int FACTORY_IMPLEMENTATIONS_$1 = 24;

        /**
         * {1} ({0} authority) replaces {2} for {3,choice,0#standard|1#XY} axis order.
         */
        public static final int FACTORY_REPLACED_FOR_AXIS_ORDER_$4 = 54;

        /**
         * Failure in the primary factory: {0} Now trying the fallback factory...
         */
        public static final int FALLBACK_FACTORY_$1 = 25;

        /**
         * Found {0} reference systems in {1} elements. The most frequent appears {2} time and the less
         * frequent appears {3} times.
         */
        public static final int FOUND_MISMATCHED_CRS_$4 = 26;

        /**
         * Ignored "{0}" hint.
         */
        public static final int HINT_IGNORED_$1 = 27;

        /**
         * Initializing transformation from {0} to {1}.
         */
        public static final int INITIALIZING_TRANSFORMATION_$2 = 28;

        /**
         * {0} JDBC driver version {1}.{2}.
         */
        public static final int JDBC_DRIVER_VERSION_$3 = 29;

        /**
         * Loading datum aliases from "{0}".
         */
        public static final int LOADING_DATUM_ALIASES_$1 = 30;

        /**
         * Text were discarted for some locales.
         */
        public static final int LOCALES_DISCARTED = 31;

        /**
         * No coordinate operation from "{0}" to "{1}" because of mismatched factories.
         */
        public static final int MISMATCHED_COORDINATE_OPERATION_FACTORIES_$2 = 32;

        /**
         * The type of the requested object doesn't match the "{0}" URN type.
         */
        public static final int MISMATCHED_URN_TYPE_$1 = 33;

        /**
         * Native acceleration {1,choice,0#disabled|1#enabled} for "{0}" operation.
         */
        public static final int NATIVE_ACCELERATION_STATE_$2 = 34;

        /**
         * JAI codec {1,choice,0#disabled|1#enabled} for {2,choice,0#reading|1#writing} "{0}" format.
         */
        public static final int NATIVE_CODEC_STATE_$3 = 35;

        /**
         * Offscreen rendering failed for layer "{0}". Fall back on default rendering.
         */
        public static final int OFFSCREEN_RENDERING_FAILED_$1 = 36;

        /**
         * Renderer "{0}" painted in {1} seconds.
         */
        public static final int PAINTING_LAYER_$2 = 37;

        /**
         * Polygons drawn with {0,number,percent} of available points, reusing {1,number,percent} from
         * the cache (resolution: {2} {3}).
         */
        public static final int POLYGON_CACHE_USE_$4 = 38;

        /**
         * Failed to allocate {0} Mb of memory. Trying a smaller memory allocation.
         */
        public static final int RECOVERABLE_OUT_OF_MEMORY_$1 = 39;

        /**
         * Log records are redirected to Apache commons logging.
         */
        public static final int REDIRECTED_TO_COMMONS_LOGGING = 40;

        /**
         * Registered Geotoolkit extensions to JAI operations.
         */
        public static final int REGISTERED_JAI_OPERATIONS = 41;

        /**
         * Registered RMI services for {0}.
         */
        public static final int REGISTERED_RMI_SERVICES_$1 = 55;

        /**
         * Select an image of "{0}" decimated to level {1} of {2}.
         */
        public static final int RESSAMPLING_RENDERED_IMAGE_$3 = 42;

        /**
         * Creates a {1,choice,0#packed|1#geophysics|2#photographic} view of grid coverage "{0}" using
         * operation "{2}".
         */
        public static final int SAMPLE_TRANSCODE_$3 = 43;

        /**
         * Layer "{0}" send a repaint event for the whole widget area.
         */
        public static final int SEND_REPAINT_EVENT_$1 = 44;

        /**
         * Layer "{0}" send a repaint event for pixels x=[{1}..{2}] and y=[{3}..{4}] in widget area.
         */
        public static final int SEND_REPAINT_EVENT_$5 = 45;

        /**
         * Unavailable authority factory: {0}
         */
        public static final int UNAVAILABLE_AUTHORITY_FACTORY_$1 = 46;

        /**
         * Attempt to recover from unexpected exception.
         */
        public static final int UNEXPECTED_EXCEPTION = 47;

        /**
         * Unexpected unit "{0}". Map scale may be inacurate.
         */
        public static final int UNEXPECTED_UNIT_$1 = 48;

        /**
         * Ignoring unknow parameter: "{0}" = {1} {2}.
         */
        public static final int UNKNOW_PARAMETER_$3 = 49;

        /**
         * Can't handle style of class {0}. Consequently, geometry "{1}" will ignore its style
         * information.
         */
        public static final int UNKNOW_STYLE_$2 = 50;

        /**
         * Unrecognized scale type: "{0}". Default to linear.
         */
        public static final int UNRECOGNIZED_SCALE_TYPE_$1 = 51;

        /**
         * Update the cache for layer "{0}".
         */
        public static final int UPDATE_RENDERER_CACHE_$1 = 52;

        /**
         * Using "{0}" as {1} factory.
         */
        public static final int USING_FILE_AS_FACTORY_$2 = 53;
    }

    /**
     * Constructs a new resource bundle loading data from the given UTF file.
     *
     * @param filename The file or the JAR entry containing resources.
     */
    Loggings(final String filename) {
        super(filename);
    }

    /**
     * Returns resources in the given locale.
     *
     * @param  locale The locale, or {@code null} for the default locale.
     * @return Resources in the given locale.
     * @throws MissingResourceException if resources can't be found.
     */
    public static Loggings getResources(Locale locale) throws MissingResourceException {
        return getBundle(Loggings.class, locale);
    }

    /**
     * Gets a log record for the given key from this resource bundle or one of its parents.
     *
     * @param  level The log record level.
     * @param  key The key for the desired string.
     * @return The string for the given key.
     * @throws MissingResourceException If no object for the given key can be found.
     */
    public static LogRecord format(final Level level,
                                   final int key) throws MissingResourceException
    {
        return getResources(null).getLogRecord(level, key);
    }

    /**
     * Gets a log record for the given key. Replaces all occurence of "{0}"
     * with values of {@code arg0}.
     *
     * @param  level The log record level.
     * @param  key The key for the desired string.
     * @param  arg0 Value to substitute to "{0}".
     * @return The formatted string for the given key.
     * @throws MissingResourceException If no object for the given key can be found.
     */
    public static LogRecord format(final Level level,
                                   final int     key,
                                   final Object arg0) throws MissingResourceException
    {
        return getResources(null).getLogRecord(level, key, arg0);
    }

    /**
     * Gets a log record for the given key. Replaces all occurence of "{0}",
     * "{1}", with values of {@code arg0}, {@code arg1}.
     *
     * @param  level The log record level.
     * @param  key The key for the desired string.
     * @param  arg0 Value to substitute to "{0}".
     * @param  arg1 Value to substitute to "{1}".
     * @return The formatted string for the given key.
     * @throws MissingResourceException If no object for the given key can be found.
     */
    public static LogRecord format(final Level level,
                                   final int     key,
                                   final Object arg0,
                                   final Object arg1) throws MissingResourceException
    {
        return getResources(null).getLogRecord(level, key, arg0, arg1);
    }

    /**
     * Gets a log record for the given key. Replaces all occurence of "{0}",
     * "{1}", with values of {@code arg0}, {@code arg1}, etc.
     *
     * @param  level The log record level.
     * @param  key The key for the desired string.
     * @param  arg0 Value to substitute to "{0}".
     * @param  arg1 Value to substitute to "{1}".
     * @param  arg2 Value to substitute to "{2}".
     * @return The formatted string for the given key.
     * @throws MissingResourceException If no object for the given key can be found.
     */
    public static LogRecord format(final Level level,
                                   final int     key,
                                   final Object arg0,
                                   final Object arg1,
                                   final Object arg2) throws MissingResourceException
    {
        return getResources(null).getLogRecord(level, key, arg0, arg1, arg2);
    }

    /**
     * Gets a log record for the given key. Replaces all occurence of "{0}",
     * "{1}", with values of {@code arg0}, {@code arg1}, etc.
     *
     * @param  level The log record level.
     * @param  key The key for the desired string.
     * @param  arg0 Value to substitute to "{0}".
     * @param  arg1 Value to substitute to "{1}".
     * @param  arg2 Value to substitute to "{2}".
     * @param  arg3 Value to substitute to "{3}".
     * @return The formatted string for the given key.
     * @throws MissingResourceException If no object for the given key can be found.
     */
    public static LogRecord format(final Level level,
                                   final int     key,
                                   final Object arg0,
                                   final Object arg1,
                                   final Object arg2,
                                   final Object arg3) throws MissingResourceException
    {
        return getResources(null).getLogRecord(level, key, arg0, arg1, arg2, arg3);
    }
}
