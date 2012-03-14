package org.ptolemy.fmi;

import java.util.HashMap;
import java.util.Map;

import com.sun.jna.DefaultTypeMapper;
import com.sun.jna.FromNativeContext;
import com.sun.jna.Pointer;
import com.sun.jna.Library;
import com.sun.jna.StringArray;
import com.sun.jna.ToNativeContext;
import com.sun.jna.TypeConverter;
import com.sun.jna.TypeMapper;
import com.sun.jna.WString;

/** Provide standard conversion for W32 API types.  This comprises the 
 * following native types:
 * <ul>
 * <li>Unicode or ASCII/MBCS strings and arrays of string, as appropriate
 * <li>BOOL
 * </ul>
 * @author twall
 */
public class FMITypeMapper extends DefaultTypeMapper {
    
    public static final TypeMapper FMITYPEMAPPER = new FMITypeMapper();
    

    protected FMITypeMapper() {
        System.out.println("FMITypeMapper()");

        // From: jna/src/com/sun/jna/win32/W32APITypeMapper.java

        // if (unicode) {
        //     TypeConverter stringConverter = new TypeConverter() {
        //         public Object toNative(Object value, ToNativeContext context) {
        //             if (value == null)
        //                 return null;
        //             if (value instanceof String[]) {
        //                 return new StringArray((String[])value, true);
        //             }
        //             return new WString(value.toString());
        //         }
        //         public Object fromNative(Object value, FromNativeContext context) {
        //             if (value == null)
        //                 return null;
        //             return value.toString();
        //         }
        //         public Class nativeType() {
        //             return WString.class;
        //         }
        //     };
        //     addTypeConverter(String.class, stringConverter);
        //     addToNativeConverter(String[].class, stringConverter);
        // }
        // TypeConverter booleanConverter = new TypeConverter() {
        //     public Object toNative(Object value, ToNativeContext context) {
        //         System.out.println("FMITypeMapper().toNative(): " + value + " " + context);
        //         return new Integer(Boolean.TRUE.equals(value) ? 1 : 0);
        //     }
        //     public Object fromNative(Object value, FromNativeContext context) {
        //         System.out.println("FMITypeMapper().fromNative(): " + value + " " + context);
        //         return ((Integer)value).intValue() != 0 ? Boolean.TRUE : Boolean.FALSE;
        //     }
        //     public Class nativeType() {
        //         // BOOL is 32-bit int
        //         return Integer.class;
        //     }
        // };
        // addTypeConverter(Boolean.class, booleanConverter);

        TypeConverter pointerConverter = new TypeConverter() {
            public Object toNative(Object value, ToNativeContext context) {
                System.out.println("FMITypeMapper().pointerConverter.toNative(): " + value + " " + context);
                if (value == null) {
                    return null;
                }
                if (value instanceof Pointer []) {
                    return ((Pointer[])value)[0].getPointerArray(0);
                }
                return new Long(((Pointer)value).indexOf((long)0,(byte)0));
            }
            public Object fromNative(Object value, FromNativeContext context) {
                if (value == null) {
                    return null;
                }
                System.out.println("FMITypeMapper().pointerConverter.fromNative(): " + value
                        + (value instanceof Pointer [] ? " Pointer []" : value.getClass().getName())
                        + " " + context);

                if (value instanceof Pointer []) {
                    //return new Long(((Pointer[])value)[0].getLong(0));
                    Pointer [] pointerArray = (Pointer[])value;
                    Long [] result = new Long[pointerArray.length];
                    for (int i = 0; i < pointerArray.length; i++) {
                        System.out.println("FMITypeMapper().pointerConverter.fromNative(): " + value + " copying " + i);
                        result[i] = new Long(pointerArray[i].getLong(0));
                    }
                    return result;
                }
                new Exception ("FMITypeMapper().pointerConverter.fromNative(): " + value + " getLong").printStackTrace();
                return new Long(((Pointer)value).getLong(0));
            }
            public Class nativeType() {
                return Pointer.class;
            }
        };

        //addTypeConverter(Pointer.class, pointerConverter);
        addTypeConverter(Pointer[].class, pointerConverter);
        //addToNativeConverter(Pointer[].class, pointerConverter);

        TypeConverter objectConverter = new TypeConverter() {
            public Object toNative(Object value, ToNativeContext context) {
                System.out.println("FMITypeMapper().objectConverter.toNative(): " + value + " " + context);
                return value.toString();
            }
            public Object fromNative(Object value, FromNativeContext context) {
                System.out.println("FMITypeMapper().objectConverter.fromNative(): " + value + " " + context);
                return value.toString();
            }
            public Class nativeType() {
                return String.class;
            }
        };

        //addTypeConverter(Object.class, objectConverter);
    }
}
