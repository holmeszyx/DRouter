package com.didi.drouter.remote;

import android.os.RemoteException;

/**
 * Created by gaowei on 2019/2/27
 *
 * IRemoteCallback in server can be stored and reused indefinitely.
 */
public interface IRemoteCallback {

    int MAIN = 0;
    int WORKER = 1;
    int BINDER = 2;

    /**
     * @param data the data of return to client.
     * @throws RemoteException the exception to throw if client is dead.
     */
    void callback(Object... data) throws RemoteException;


    //----------------------------------------------------------------------------------------//
    abstract class Type implements IRemoteCallback {
        public abstract void callback() throws RemoteException;
        @Override
        public void callback(Object... data) throws RemoteException {
            callback();
        }
        public int mode() {
            return MAIN;
        }
    }

    @SuppressWarnings("unchecked")
    abstract class Type1<Param1> implements IRemoteCallback {
        public abstract void callback(Param1 p1) throws RemoteException;
        @Override
        public void callback(Object... data) throws RemoteException {
            if (data.length == 0) callback((Param1) null);
            else callback((Param1) data[0]);
        }
        public int mode() {
            return MAIN;
        }
    }
    
    @SuppressWarnings("unchecked")
    abstract class Type2<Param1, Param2> implements IRemoteCallback {
        public abstract void callback(Param1 p1, Param2 p2) throws RemoteException;
        @Override
        public void callback(Object... data) throws RemoteException {
            if (data.length == 0) callback(null, null);
            else callback((Param1) data[0], (Param2) data[1]);
        }
        public int mode() {
            return MAIN;
        }
    }
    @SuppressWarnings("unchecked")
    abstract class Type3<Param1, Param2, Param3> implements IRemoteCallback {
        public abstract void callback(Param1 p1, Param2 p2, Param3 p3) throws RemoteException;
        @Override
        public void callback(Object... data) throws RemoteException {
            if (data.length == 0) callback(null, null, null);
            else callback((Param1) data[0], (Param2) data[1], (Param3) data[2]);
        }
        public int mode() {
            return MAIN;
        }
    }
    @SuppressWarnings("unchecked")
    abstract class Type4<Param1, Param2, Param3, Param4> implements IRemoteCallback {
        public abstract void callback(Param1 p1, Param2 p2, Param3 p3, Param4 p4) throws RemoteException;
        @Override
        public void callback(Object... data) throws RemoteException {
            if (data.length == 0) callback(null, null, null, null);
            else callback((Param1) data[0], (Param2) data[1], (Param3) data[2], (Param4) data[3]);
        }
        public int mode() {
            return MAIN;
        }
    }
    @SuppressWarnings("unchecked")
    abstract class Type5<Param1, Param2, Param3, Param4, Param5> implements IRemoteCallback {
        public abstract void callback(Param1 p1, Param2 p2, Param3 p3, Param4 p4, Param5 p5) throws RemoteException;
        @Override
        public void callback(Object... data) throws RemoteException {
            if (data.length == 0) callback(null, null, null, null, null);
            else callback((Param1) data[0], (Param2) data[1], (Param3) data[2], (Param4) data[3], (Param5) data[4]);
        }
        public int mode() {
            return MAIN;
        }
    }
}
