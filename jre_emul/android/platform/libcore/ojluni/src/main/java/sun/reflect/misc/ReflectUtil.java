/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 2005, 2013 Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */


package sun.reflect.misc;
import java.lang.reflect.Proxy;

public final class ReflectUtil {

    private ReflectUtil() {
    }

    public static Class forName(String name)
        throws ClassNotFoundException {
        checkPackageAccess(name);
        return Class.forName(name);
    }

    public static Object newInstance(Class cls)
        throws InstantiationException, IllegalAccessException {
        checkPackageAccess(cls);
        return cls.newInstance();
    }

    private static boolean isSubclassOf(Class queryClass,
                                Class ofClass)
    {
        while (queryClass != null) {
            if (queryClass == ofClass) {
                return true;
            }
            queryClass = queryClass.getSuperclass();
        }
        return false;
    }

    /**
     * Checks package access on the given class.
     *
     * If it is a {@link Proxy#isProxyClass(java.lang.Class)} that implements
     * a non-public interface (i.e. may be in a non-restricted package),
     * also check the package access on the proxy interfaces.
     */
    public static void checkPackageAccess(Class<?> clazz) {
        checkPackageAccess(clazz.getName());
        if (isNonPublicProxyClass(clazz)) {
            checkProxyPackageAccess(clazz);
        }
    }

    /**
     * Checks package access on the given classname.
     * This method is typically called when the Class instance is not
     * available and the caller attempts to load a class on behalf
     * the true caller (application).
     */
    public static void checkPackageAccess(String name) {
        // SecurityManager s = System.getSecurityManager();
        // if (s != null) {
        //     String cname = name.replace('/', '.');
        //     if (cname.startsWith("[")) {
        //         int b = cname.lastIndexOf('[') + 2;
        //         if (b > 1 && b < cname.length()) {
        //             cname = cname.substring(b);
        //         }
        //     }
        //     int i = cname.lastIndexOf('.');
        //     if (i != -1) {
        //         s.checkPackageAccess(cname.substring(0, i));
        //     }
        // }
    }

    public static boolean isPackageAccessible(Class clazz) {
        try {
            checkPackageAccess(clazz);
        } catch (SecurityException e) {
            return false;
        }
        return true;
    }

    // Returns true if p is an ancestor of cl i.e. class loader 'p' can
    // be found in the cl's delegation chain
    private static boolean isAncestor(ClassLoader p, ClassLoader cl) {
        ClassLoader acl = cl;
        do {
            acl = acl.getParent();
            if (p == acl) {
                return true;
            }
        } while (acl != null);
        return false;
    }

    /**
     * Returns true if package access check is needed for reflective
     * access from a class loader 'from' to classes or members in
     * a class defined by class loader 'to'.  This method returns true
     * if 'from' is not the same as or an ancestor of 'to'.  All code
     * in a system domain are granted with all permission and so this
     * method returns false if 'from' class loader is a class loader
     * loading system classes.  On the other hand, if a class loader
     * attempts to access system domain classes, it requires package
     * access check and this method will return true.
     */
    public static boolean needsPackageAccessCheck(ClassLoader from, ClassLoader to) {
        if (from == null || from == to)
            return false;

        if (to == null)
            return true;

        return !isAncestor(from, to);
    }

    /**
     * Check package access on the proxy interfaces that the given proxy class
     * implements.
     *
     * @param clazz Proxy class object
     */
    public static void checkProxyPackageAccess(Class<?> clazz) {
        // SecurityManager s = System.getSecurityManager();
        // if (s != null) {
        //     // check proxy interfaces if the given class is a proxy class
        //     if (Proxy.isProxyClass(clazz)) {
        //         for (Class<?> intf : clazz.getInterfaces()) {
        //             checkPackageAccess(intf);
        //         }
        //     }
        // }
    }

    /**
     * Access check on the interfaces that a proxy class implements and throw
     * {@code SecurityException} if it accesses a restricted package from
     * the caller's class loader.
     *
     * @param ccl the caller's class loader
     * @param interfaces the list of interfaces that a proxy class implements
     */
    public static void checkProxyPackageAccess(ClassLoader ccl,
                                               Class<?>... interfaces)
    {
        // SecurityManager sm = System.getSecurityManager();
        // if (sm != null) {
        //     for (Class<?> intf : interfaces) {
        //         ClassLoader cl = intf.getClassLoader();
        //         if (needsPackageAccessCheck(ccl, cl)) {
        //             checkPackageAccess(intf);
        //         }
        //     }
        // }
    }

    /**
     * Test if the given class is a proxy class that implements
     * non-public interface.  Such proxy class may be in a non-restricted
     * package that bypasses checkPackageAccess.
     */
    public static boolean isNonPublicProxyClass(Class<?> cls) {
        String name = cls.getName();
        int i = name.lastIndexOf('.');
        String pkg = (i != -1) ? name.substring(0, i) : "";

        // NOTE: Android creates proxies in the "default" package (and not com.sun.proxy), which
        // makes this check imprecise. However, this function is only ever called if there's
        // a security manager installed (which is the never case on android).
        return Proxy.isProxyClass(cls) && !pkg.isEmpty();
    }
}
