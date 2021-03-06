/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\Android_research\\SystemSens-master\\app\\src\\main\\aidl\\edu\\ucla\\cens\\systemsens\\IApplication.aidl
 */
package edu.ucla.cens.systemsens;
public interface IApplication extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements edu.ucla.cens.systemsens.IApplication
{
private static final java.lang.String DESCRIPTOR = "edu.ucla.cens.systemsens.IApplication";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an edu.ucla.cens.systemsens.IApplication interface,
 * generating a proxy if needed.
 */
public static edu.ucla.cens.systemsens.IApplication asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof edu.ucla.cens.systemsens.IApplication))) {
return ((edu.ucla.cens.systemsens.IApplication)iin);
}
return new edu.ucla.cens.systemsens.IApplication.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getName:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getName();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_identifyList:
{
data.enforceInterface(DESCRIPTOR);
java.util.List _result = this.identifyList();
reply.writeNoException();
reply.writeList(_result);
return true;
}
case TRANSACTION_getWork:
{
data.enforceInterface(DESCRIPTOR);
java.util.List _result = this.getWork();
reply.writeNoException();
reply.writeList(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements edu.ucla.cens.systemsens.IApplication
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
     * Returns the name of this application. 
     * E.g. WiFiGPSLocation
     *
     * @return      name of the application
     */
@Override public java.lang.String getName() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getName, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Returns the list of names of work units.
     * E.g. <"GPS", "WiFiScan">
     *
     * @return      names of work units.
     */
@Override public java.util.List identifyList() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_identifyList, _data, _reply, 0);
_reply.readException();
java.lang.ClassLoader cl = (java.lang.ClassLoader)this.getClass().getClassLoader();
_result = _reply.readArrayList(cl);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Return a vector of doubles indicating the amount of work done.
     * Each adaptive application should count its its major energy
     * consuming operations. Each time this method is called the
     * cumulative count of all the activities should be returned. 
     * The order should be consistant. The counts can be fractional 
     * values (double).
     * E.g. <126.0, 1763.0>
     *
     * @return      List of doubles indicating work units
     *
     */
@Override public java.util.List getWork() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getWork, _data, _reply, 0);
_reply.readException();
java.lang.ClassLoader cl = (java.lang.ClassLoader)this.getClass().getClassLoader();
_result = _reply.readArrayList(cl);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getName = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_identifyList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getWork = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
/**
     * Returns the name of this application. 
     * E.g. WiFiGPSLocation
     *
     * @return      name of the application
     */
public java.lang.String getName() throws android.os.RemoteException;
/**
     * Returns the list of names of work units.
     * E.g. <"GPS", "WiFiScan">
     *
     * @return      names of work units.
     */
public java.util.List identifyList() throws android.os.RemoteException;
/**
     * Return a vector of doubles indicating the amount of work done.
     * Each adaptive application should count its its major energy
     * consuming operations. Each time this method is called the
     * cumulative count of all the activities should be returned. 
     * The order should be consistant. The counts can be fractional 
     * values (double).
     * E.g. <126.0, 1763.0>
     *
     * @return      List of doubles indicating work units
     *
     */
public java.util.List getWork() throws android.os.RemoteException;
}
