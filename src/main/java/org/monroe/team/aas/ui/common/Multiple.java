package org.monroe.team.aas.ui.common;

/**
 * User: MisterJBee
 * Date: 7/13/13 Time: 1:17 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class Multiple {

    private final Object[] mFields;

    public Multiple(int mSize) {
        mFields = new Object[mSize];
    }

    public<Type> Type get(int indx, Class<Type> expectedClass){
        return (Type) get(indx);
    }

    public Object get(int indx){
        checkIndex(indx);
        return mFields[indx];
    }

    public void set(int indx, Object value){
        setImpl(indx,value,true);
    }

    public void setRelax(int indx, Object value){
        setImpl(indx,value,false);
    }

    private void setImpl(int indx, Object value, boolean strict){
        checkIndex(indx);
        Should.beTrue("Value already set",!strict || mFields[indx] == null );
        mFields[indx] = value;
    }

    private void checkIndex(int indx) {
        if (indx < 0 || indx > mFields.length-1) throw new IndexOutOfBoundsException();
    }
}
