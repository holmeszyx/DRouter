package com.didi.drouter.remote;

import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import androidx.collection.ArrayMap;
import androidx.collection.ArraySet;

import com.didi.drouter.api.DRouter;
import com.didi.drouter.utils.JsonConverter;
import com.didi.drouter.utils.ReflectUtil;
import com.didi.drouter.utils.RouterLogger;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by gaowei on 2019/1/25
 */
class RemoteStream {

    static Object transform(Object o) {
        if (isParcelable(o)) {
            return o;
        } else if (o.getClass().isArray()) {
            return new ArrayParcelable(o);
        } else if (o instanceof Map) {
            return new MapParcelable(o);
        } else if (o instanceof Collection) {
            return new CollectionParcelable(o);
        } else if (o instanceof IRemoteCallback) {
            return new RemoteCallbackParcelable(o);
        } else {
            return new ObjectParcelable(o);
        }
    }

    static Object reverse(Object o) {
        if (o instanceof ArrayParcelable) {
            return ((ArrayParcelable) o).getArray();
        } else if (o instanceof MapParcelable) {
            return ((MapParcelable) o).getMap();
        } else if (o instanceof CollectionParcelable) {
            return ((CollectionParcelable) o).getCollection();
        } else if (o instanceof ObjectParcelable) {
            return ((ObjectParcelable) o).getObject();
        } else if (o instanceof RemoteCallbackParcelable) {
            return ((RemoteCallbackParcelable) o).getObject();
        } else {
            return o;
        }
    }

    private static boolean isParcelable(Object object) {
        return  object == null ||
                object instanceof Boolean || object instanceof boolean[] ||
                object instanceof Byte || object instanceof byte[] ||
                object instanceof Character || object instanceof char[] ||
                object instanceof Short || object instanceof short[] ||
                object instanceof Integer || object instanceof int[] ||
                object instanceof Long || object instanceof long[] ||
                object instanceof Float || object instanceof float[] ||
                object instanceof Double || object instanceof double[] ||
                object instanceof CharSequence || object instanceof CharSequence[] ||
                object instanceof Parcelable || object instanceof Parcelable[] ||
                object instanceof Class || object instanceof IBinder;
    }

    static class ArrayParcelable implements Parcelable {

        Object[] array;

        ArrayParcelable(Object o) {
            array = (Object[]) o;
        }

        ArrayParcelable(Parcel in) {
            Class<?> clz = (Class<?>) in.readSerializable();
            Object[] tmp = in.readArray(getClass().getClassLoader());
            assert clz != null;
            assert tmp != null;
            array = (Object[]) Array.newInstance(clz, tmp.length);
            for (int i = 0; i < tmp.length; i++) {
                array[i] = reverse(tmp[i]);
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            Object[] tmp = new Object[array.length];  //type may change after transform
            for (int i = 0; i < array.length; i++) {
                tmp[i] = transform(array[i]);
            }
            assert array.getClass().getComponentType() != null;
            writeSerializable(dest, array.getClass().getComponentType());
            dest.writeArray(tmp);   //no shell type
        }

        Object[] getArray() {
            return array;
        }

        public static final Creator<ArrayParcelable> CREATOR = new Creator<ArrayParcelable>() {
            @Override
            public ArrayParcelable createFromParcel(Parcel in) {
                return new ArrayParcelable(in);
            }

            @Override
            public ArrayParcelable[] newArray(int size) {
                return new ArrayParcelable[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    static class MapParcelable implements Parcelable {

        Map<Object, Object> map;

        MapParcelable(Object o) {
            map = (Map<Object, Object>) o;
        }

        MapParcelable(Parcel in) {
            Class<?> clz = (Class<?>) in.readSerializable();
            if (clz == HashMap.class) {
                map = new HashMap<>();
            } else if (clz == ArrayMap.class) {
                map = new ArrayMap<>();
            } else if (clz == ConcurrentHashMap.class) {
                map = new ConcurrentHashMap<>();
            } else {
                assert clz != null;
                map = (Map<Object, Object>) ReflectUtil.getInstance(clz);
            }
            Map<Object, Object> tmp = in.readHashMap(getClass().getClassLoader());
            assert tmp != null;
            for (Map.Entry<Object, Object> entry : tmp.entrySet()) {
                map.put(reverse(entry.getKey()), reverse(entry.getValue()));
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            Class<?> clz = map.getClass();
            Map<Object, Object> tmp = new HashMap<>();
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                tmp.put(transform(entry.getKey()), transform(entry.getValue()));
            }
            writeSerializable(dest, clz);
            dest.writeMap(tmp);
        }

        Map<Object, Object> getMap() {
            return map;
        }

        public static final Creator<MapParcelable> CREATOR = new Creator<MapParcelable>() {
            @Override
            public MapParcelable createFromParcel(Parcel in) {
                return new MapParcelable(in);
            }

            @Override
            public MapParcelable[] newArray(int size) {
                return new MapParcelable[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    static class CollectionParcelable implements Parcelable {

        Collection<Object> collection;

        CollectionParcelable(Object o) {
            collection = (Collection<Object>) o;
        }

        CollectionParcelable(Parcel in) {
            Class<?> clz = (Class<?>) in.readSerializable();
            if (clz == ArrayList.class) {
                collection = new ArrayList<>();
            } else if (clz == HashSet.class) {
                collection = new HashSet<>();
            } else if (clz == ArraySet.class) {
                collection = new ArraySet<>();
            } else if (clz == LinkedList.class) {
                collection = new LinkedList<>();
            } else {
                assert clz != null;
                collection = (Collection<Object>) ReflectUtil.getInstance(clz);
            }
            List<Object> tmp = in.readArrayList(getClass().getClassLoader());
            assert tmp != null;
            for (Object object : tmp) {
                collection.add(reverse(object));
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            Class<?> clz = collection.getClass();
            List<Object> tmp = new ArrayList<>();
            for (Object object : collection) {
                tmp.add(transform(object));
            }
            writeSerializable(dest, clz);
            dest.writeList(tmp);
        }

        Collection<Object> getCollection() {
            return collection;
        }

        public static final Creator<CollectionParcelable> CREATOR = new Creator<CollectionParcelable>() {
            @Override
            public CollectionParcelable createFromParcel(Parcel in) {
                return new CollectionParcelable(in);
            }

            @Override
            public CollectionParcelable[] newArray(int size) {
                return new CollectionParcelable[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }
    }

    static class ObjectParcelable implements Parcelable {

        Object object;

        ObjectParcelable(Object object) {
            this.object = object;
        }

        ObjectParcelable(Parcel in) {
            int type = in.readInt();
            if (type == 0) {
                object = DRouter.getContext();
            } else {
                Class<?> clz = (Class<?>) in.readSerializable();
                object = JsonConverter.toObject(in.readString(), clz);
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            if (object instanceof Context) {
                dest.writeInt(0);
            } else {
                dest.writeInt(1);
                writeSerializable(dest, object.getClass());
                dest.writeString(JsonConverter.toString(object));
            }
        }

        Object getObject() {
            return object;
        }

        public static final Creator<ObjectParcelable> CREATOR = new Creator<ObjectParcelable>() {
            @Override
            public ObjectParcelable createFromParcel(Parcel in) {
                return new ObjectParcelable(in);
            }

            @Override
            public ObjectParcelable[] newArray(int size) {
                return new ObjectParcelable[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }
    }

    static class RemoteCallbackParcelable implements Parcelable {

        // key is client IRemoteCallback instance, value is Binder
        // no need to remove, for week hash map can be removed auto
        static Map<IRemoteCallback, IClientService> callbackPool =
                Collections.synchronizedMap(new WeakHashMap<IRemoteCallback, IClientService>());

        int type;
        IBinder binder;

        RemoteCallbackParcelable(Object object) {
            type = type(object);
            final IRemoteCallback callback = (IRemoteCallback) object;
            IClientService callbackBinder = callbackPool.get(callback);
            if (callbackBinder == null) {
                // avoid memory leak
                final WeakReference<IRemoteCallback> callbackWeak = new WeakReference<>(callback);
                callbackBinder = new IClientService.Stub() {
                    @Override
                    public RemoteResult callback(RemoteCommand callbackCommand) throws RemoteException {
                        RouterLogger.getCoreLogger().d("[Client] receive server callback from binder");
                        IRemoteCallback callback2 = callbackWeak.get();
                        if (callback2 != null) {
                            callback2.callback(callbackCommand.callbackData);
                        }
                        return null;
                    }
                };
                callbackPool.put(callback, callbackBinder);
            }
            binder = callbackBinder.asBinder();
        }

        RemoteCallbackParcelable(Parcel in) {
            binder = in.readStrongBinder();
            type = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeStrongBinder(binder);
            dest.writeInt(type);
        }

        Object getObject() {
            for (Map.Entry<IRemoteCallback, IClientService> entry : callbackPool.entrySet()) {
                if (entry.getValue().asBinder() == binder) {
                    return entry.getKey();
                }
            }
            final IClientService callbackService = IClientService.Stub.asInterface(binder);
            IRemoteCallback callback = create(type, new IRemoteCallback() {
                @Override
                public void callback(Object... data) throws RemoteException {
                    if (data == null) {
                        data = new Object[] {null};
                    }
                    RouterLogger.getCoreLogger().w("[Server] IRemoteCallback start callback invoke");
                    RemoteCommand callbackCommand = new RemoteCommand(RemoteCommand.SERVICE_CALLBACK);
                    callbackCommand.callbackData = data;
                    try {
                        callbackService.callback(callbackCommand);
                    } catch (RemoteException e) {
                        RouterLogger.getCoreLogger().e("[Server] IRemoteCallback invoke Exception %s", e);
                        throw e;
                    }
                }
            });
            callbackPool.put(callback, callbackService);
            return callback;
        }

        private static int type(Object object) {
            if (object instanceof IRemoteCallback.Type) {
                return 0;
            }
            if (object instanceof IRemoteCallback.Type1) {
                return 1;
            }
            if (object instanceof IRemoteCallback.Type2) {
                return 2;
            }
            if (object instanceof IRemoteCallback.Type3) {
                return 3;
            }
            if (object instanceof IRemoteCallback.Type4) {
                return 4;
            }
            if (object instanceof IRemoteCallback.Type5) {
                return 5;
            }
            throw new IllegalArgumentException(
                    String.format("object %s is not RemoteCallback type", object));
        }

        private static IRemoteCallback create(int type, final IRemoteCallback callback) {
            if (type == 0) {
                return new IRemoteCallback.Type() {
                    @Override
                    public void callback() throws RemoteException {
                        callback.callback();
                    }
                };
            }
            if (type == 1) {
                return new IRemoteCallback.Type1() {
                    @Override
                    public void callback(Object p1) throws RemoteException {
                        callback.callback(p1);
                    }
                };
            }
            if (type == 2) {
                return new IRemoteCallback.Type2() {
                    @Override
                    public void callback(Object p1, Object p2) throws RemoteException {
                        callback.callback(p1, p2);
                    }
                };
            }
            if (type == 3) {
                return new IRemoteCallback.Type3() {
                    @Override
                    public void callback(Object p1, Object p2, Object p3) throws RemoteException {
                        callback.callback(p1, p2, p3);
                    }
                };
            }
            if (type == 4) {
                return new IRemoteCallback.Type4() {
                    @Override
                    public void callback(Object p1, Object p2, Object p3, Object p4) throws RemoteException {
                        callback.callback(p1, p2, p3, p4);
                    }
                };
            }
            if (type == 5) {
                return new IRemoteCallback.Type5() {
                    @Override
                    public void callback(Object p1, Object p2, Object p3, Object p4, Object p5) throws RemoteException {
                        callback.callback(p1, p2, p3, p4, p5);
                    }
                };
            }
            throw new IllegalArgumentException(
                    String.format("object %s is not RemoteCallback type"));
        }

        public static final Creator<RemoteCallbackParcelable> CREATOR = new Creator<RemoteCallbackParcelable>() {
            @Override
            public RemoteCallbackParcelable createFromParcel(Parcel in) {
                return new RemoteCallbackParcelable(in);
            }

            @Override
            public RemoteCallbackParcelable[] newArray(int size) {
                return new RemoteCallbackParcelable[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }
    }

    private static void writeSerializable(Parcel dest, Class<?> clz) {
        boolean isSerializable = !((clz.isLocalClass() || clz.isMemberClass() || clz.isAnonymousClass()) &&
                (clz.getModifiers() & Modifier.STATIC) == 0);
        if (isSerializable) {
            dest.writeSerializable(clz);
        } else {
            // for instance
            throw new IllegalArgumentException(
                    String.format("non static inner class \"%s\" can not be serialized", clz.getName()));
        }
    }
}




