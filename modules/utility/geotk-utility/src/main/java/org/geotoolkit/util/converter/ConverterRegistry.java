/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2001-2009, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.util.converter;

import java.util.Map;
import java.util.HashMap;

import org.geotoolkit.lang.ThreadSafe;
import org.geotoolkit.resources.Errors;
import org.geotoolkit.gui.swing.tree.Trees;
import org.geotoolkit.gui.swing.tree.DefaultMutableTreeNode;


/**
 * A central place where to register converters. The {@linkplain #system system} register is
 * initialized automatically with conversions between some basic Java and Geotoolkit object, like
 * conversions between {@link java.util.Date} and {@link java.lang.Long}. Those conversions are
 * defined for the lifetime of the JVM.
 * <p>
 * If a temporary set of converters is desired, a new instance of {@code ConverterRegistry}
 * should be created.
 *
 * @author Martin Desruisseaux (Geomatys)
 * @version 3.0
 *
 * @since 3.0
 * @module
 */
@ThreadSafe
public class ConverterRegistry {
    /**
     * The default system-wide instance.
     */
    private static ConverterRegistry system;

    /**
     * Returns the default system-wide instance.
     *
     * @return The system-wide registry instance.
     */
    public synchronized static ConverterRegistry system() {
        ConverterRegistry s = system;
        if (s == null) {
            s = new ConverterRegistry();
            s.register(StringConverter.Number.INSTANCE); // Better to make it first.
            s.register(StringConverter.Double.INSTANCE);
            s.register(StringConverter.Float.INSTANCE);
            s.register(StringConverter.Long.INSTANCE);
            s.register(StringConverter.Integer.INSTANCE);
            s.register(StringConverter.Short.INSTANCE);
            s.register(StringConverter.Byte.INSTANCE);
            s.register(StringConverter.Boolean.INSTANCE);
            s.register(StringConverter.Color.INSTANCE);
            s.register(StringConverter.Locale.INSTANCE);
            s.register(StringConverter.Charset.INSTANCE);
            s.register(StringConverter.File.INSTANCE);
            s.register(StringConverter.URI.INSTANCE);
            s.register(StringConverter.URL.INSTANCE);
            s.register(NumberConverter.String.INSTANCE);
            s.register(NumberConverter.Boolean.INSTANCE);
            s.register(NumberConverter.Color.INSTANCE);
            s.register(DateConverter.Long.INSTANCE);
            s.register(LongConverter.Date.INSTANCE);
            system = s; // Only on success.
        }
        return s;
    }

    /**
     * The map of converters of any kind. All access of this map must be synchronized,
     * including read operations. Note that read operations are sometime followed by a
     * write, so a read/write lock may not be the best match here.
     */
    private final Map<ClassPair<?,?>, ObjectConverter<?,?>> converters;

    /**
     * Creates an initially empty set of object converters.
     */
    public ConverterRegistry() {
        converters = new HashMap<ClassPair<?,?>, ObjectConverter<?,?>>();
    }

    /**
     * Registers a new converter. This method should be invoked only once for a given converter,
     * typically in class static initializer. For example if a {@code Angle} class is defined,
     * the static initializer of that class could register a converter from {@code Angle} to
     * {@code Double}.
     * <p>
     * This method registers the converter for its {@linkplain ObjectConverter#getTargetClass
     * target class}, some of its parent classes and some interfaces. For example a converter
     * producing {@link Double} can be used for clients that just ask for a {@link Number} or
     * a {@link Comparable}.
     *
     * @param converter The converter to register.
     */
    public void register(final ObjectConverter<?,?> converter) {
        /*
         * If the given converter is a FallbackConverter (maybe obtained from an other
         * ConvergerRegistry), unwraps it and registers its component individually.
         */
        if (converter instanceof FallbackConverter) {
            final FallbackConverter<?,?> fc = (FallbackConverter<?,?>) converter;
            final ObjectConverter<?,?> primary, fallback;
            synchronized (fc) {
                primary  = fc.converter(true);
                fallback = fc.converter(false);
            }
            register(primary);
            register(fallback);
            return;
        }
        /*
         * Registers an individual converter.
         */
        final Class<?> source = converter.getSourceClass();
        final Class<?> target = converter.getTargetClass();
        final Class<?> stopAt = Classes.commonClass(source, target);
        synchronized (converters) {
            for (Class<?> i=target; i!=null && !i.equals(stopAt); i=i.getSuperclass()) {
                @SuppressWarnings("unchecked")
                final ClassPair<?,?> key = new ClassPair(source, i);
                register(key, converter);
            }
            /*
             * At this point, the given class and parent classes
             * have been registered. Now registers interfaces.
             */
            for (final Class<?> i : target.getInterfaces()) {
                if (Cloneable.class.isAssignableFrom(i)) {
                    /*
                     * Exclude this special case. If we were accepting it, we would basically
                     * provide converters from immutable to mutable objects (e.g. from String
                     * to Locale), which is not something we would like to encourage. Even if
                     * the user really wanted a mutable object, in order to modify it he needs
                     * to known the exact type, so asking for a conversion to Cloneable is too
                     * vague.
                     */
                    continue;
                }
                if (Comparable.class.isAssignableFrom(i)) {
                    /*
                     * Exclude this special case. java.lang.Number does not implement Comparable,
                     * but its subclasses do. Accepting this case would lead FactoryRegistry to
                     * offers converter from Number to String, which is not the best move if the
                     * user want to compare numbers.
                     */
                    continue;
                }
                if (!i.isAssignableFrom(source)) {
                    @SuppressWarnings("unchecked")
                    final ClassPair<?,?> key = new ClassPair(source, i);
                    register(key, converter);
                }
            }
        }
    }

    /**
     * Registers the given converter under the given key. If a previous converter is already
     * registered for the given key, then there is a choice:
     * <p>
     * <ul>
     *   <li>If one converter {@linkplain ClassPair#isDefining is defining} while the
     *       other is not, then the defining converter replaces the non-defining one.</li>
     *   <li>Otherwise the new converter is registered in addition of the old one in a
     *       chain of fallbacks.</li>
     * </ul>
     *
     * @param key The key under which to register the converter.
     * @param converter The converter to register.
     */
    private void register(final ClassPair<?,?> key, ObjectConverter<?,?> converter) {
        assert Thread.holdsLock(converters);
        final ObjectConverter<?,?> existing = converters.get(key);
        if (existing != null) {
            final boolean isDefining = key.isDefining(converter);
            if (key.isDefining(existing) == isDefining) {
                // Both the new converter and the old one are specific or are not specific.
                // Creates a chain of fallbacks.
                converter = FallbackConverter.createUnsafe(existing, converter);
            } else if (!isDefining) {
                // Existing converter is specific while the new one is not.
                // Keep the old converter untouched, discard the new one.
                return;
            } else {
                // New converter is specific while the old one was not.
                // Replace the old converter.
            }
        }
        if (converter != existing) {
            converters.put(key, converter);
        }
    }

    /**
     * Returns a converter for the specified source and target classes.
     *
     * @param  <S> The source class.
     * @param  <T> The target class.
     * @param  source The source class.
     * @param  target The target class, or {@code Object.class} for any.
     * @return The converter from the specified source class to the target class.
     * @throws NonconvertibleObjectException if no converter is found.
     */
    public <S,T> ObjectConverter<S,T> converter(final Class<S> source, final Class<T> target)
            throws NonconvertibleObjectException
    {
        final ClassPair<S,T> key = new ClassPair<S,T>(source, target);
        synchronized (converters) {
            ObjectConverter<?,?> converter = converters.get(key);
            if (converter != null) {
                return key.cast(converter);
            }
            /*
             * At this point, no converter were found explicitly for the given key. Searches
             * a converter accepting some subclass, and if we find any cache the result.
             */
            ClassPair<? super S,T> candidate = key;
            while ((candidate = candidate.parentSource()) != null) {
                converter = converters.get(candidate);
                if (converter != null) {
                    register(candidate, converter);
                    return key.cast(converter);
                }
            }
        }
        throw new NonconvertibleObjectException(Errors.format(Errors.Keys.UNKNOW_TYPE_$1, key));
    }

    /**
     * Returns a string representation of registered converters.
     * Used mostly for debugging purpose.
     *
     * @return A string representation of registered converters.
     */
    @Override
    public String toString() {
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode(Classes.getShortClassName(this));
        synchronized (converters) {
            for (final Map.Entry<ClassPair<?,?>, ObjectConverter<?,?>> entry : converters.entrySet()) {
                final DefaultMutableTreeNode node = new DefaultMutableTreeNode(entry.getKey());
                final ObjectConverter<?,?> value = entry.getValue();
                if (value instanceof FallbackConverter) {
                    ((FallbackConverter<?,?>) value).toTree(node);
                }
                root.add(node);
            }
        }
        return Trees.toString(root);
    }
}
